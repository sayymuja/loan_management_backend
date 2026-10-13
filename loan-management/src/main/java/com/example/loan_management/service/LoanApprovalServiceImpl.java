        package com.example.loan_management.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.entity.LoanApprovalRequest;
import com.example.loan_management.repository.LoanApprovalRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoanApprovalServiceImpl implements LoanApprovalService {

    private final LoanApprovalRequestRepository requestRepository;
    private final LoanService loanService;
    private final ClScheduleService clScheduleService;
    private final RepaymentService repaymentService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    // USER: SUBMIT CREATE REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto submitCreateRequest(LoanDto loanDto) {
        requireUser();
        validateLoanDto(loanDto);

        loanService.getByWomanId(loanDto.getWomanId());

        LoanApprovalRequest request = new LoanApprovalRequest();
        request.setRequestType("CREATE");
        request.setLoanId(null);
        request.setRequestData(toJson(loanDto));
        request.setRequestedBy(currentUserService.getCurrentUser());
        request.setCmrcId(currentUserService.getCurrentCmrcId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // USER: SUBMIT UPDATE REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto submitUpdateRequest(
            Long loanId,
            LoanDto loanDto
    ) {
        requireUser();
        validateLoanDto(loanDto);

        loanService.getById(loanId);
        loanService.getByWomanId(loanDto.getWomanId());

        LoanApprovalRequest request = new LoanApprovalRequest();
        request.setRequestType("UPDATE");
        request.setLoanId(loanId);
        request.setRequestData(toJson(loanDto));
        request.setRequestedBy(currentUserService.getCurrentUser());
        request.setCmrcId(currentUserService.getCurrentCmrcId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // USER: SUBMIT DELETE REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto submitDeleteRequest(Long loanId) {
        requireUser();

        LoanDto existingLoan = loanService.getById(loanId);

        LoanApprovalRequest request = new LoanApprovalRequest();
        request.setRequestType("DELETE");
        request.setLoanId(loanId);
        request.setRequestData(toJson(existingLoan));
        request.setRequestedBy(currentUserService.getCurrentUser());
        request.setCmrcId(currentUserService.getCurrentCmrcId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // ADMIN: GET PENDING REQUESTS
    @Override
    public List<LoanApprovalRequestDto> getPendingRequests() {
        requireAdmin();

        Long cmrcId = currentUserService.getCurrentCmrcId();

        return requestRepository
                .findByCmrcIdAndStatusOrderByCreatedAtDesc(
                        cmrcId, "PENDING"
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // USER: GET MY REQUESTS
    @Override
    public List<LoanApprovalRequestDto> getMyRequests() {
        requireUser();

        return requestRepository
                .findByRequestedBy_IdOrderByCreatedAtDesc(
                        currentUserService.getCurrentUserId()
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ADMIN: APPROVE REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto approve(Long requestId, String remarks) {
        requireAdmin();

        Long cmrcId = currentUserService.getCurrentCmrcId();

        LoanApprovalRequest request = requestRepository
                .findByIdAndCmrcId(requestId, cmrcId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval request not found"
                ));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed"
            );
        }

        String requestType = request.getRequestType() == null
                ? ""
                : request.getRequestType()
                .trim()
                .toUpperCase(Locale.ROOT);

        switch (requestType) {
            case "PAY_EMI" -> {
                JsonNode data = readRequestData(request.getRequestData());
                Long repaymentId = data.path("repaymentId").asLong(-1);

                if (repaymentId <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid repayment ID in approval request."
                    );
                }

                RepaymentDto repayment =
                        repaymentService.getById(repaymentId);

                if (request.getLoanId() == null
                        || !request.getLoanId().equals(
                        repayment.getLoanId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Repayment does not belong to the requested loan."
                    );
                }

                if ("PAID".equalsIgnoreCase(
                        repayment.getPaymentStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "This EMI is already paid."
                    );
                }

                if (!data.hasNonNull("paidAmount")
                        || !data.hasNonNull("penaltyAmount")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Payment amount or penalty amount is missing."
                    );
                }

                validatePaymentAmounts(
                        data.get("paidAmount").asDouble(),
                        data.get("penaltyAmount").asDouble()
                );

                repaymentService.payEmi(
                        repaymentId,
                        data.get("paidAmount").asDouble(),
                        data.get("penaltyAmount").asDouble(),
                        data.hasNonNull("regularRepayment")
                                && data.get("regularRepayment").asBoolean()
                );
            }

            case "EDIT_EMI" -> {
                JsonNode data = readRequestData(request.getRequestData());
                Long repaymentId = data.path("repaymentId").asLong(-1);

                if (repaymentId <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid repayment ID in approval request."
                    );
                }

                RepaymentDto repayment =
                        repaymentService.getById(repaymentId);

                if (request.getLoanId() == null
                        || !request.getLoanId().equals(
                        repayment.getLoanId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Repayment does not belong to the requested loan."
                    );
                }

                if (!"PAID".equalsIgnoreCase(
                        repayment.getPaymentStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "This EMI is no longer in PAID status."
                    );
                }

                if (!data.hasNonNull("paidAmount")
                        || !data.hasNonNull("penaltyAmount")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Payment amount or penalty amount is missing."
                    );
                }

                validatePaymentAmounts(
                        data.get("paidAmount").asDouble(),
                        data.get("penaltyAmount").asDouble()
                );

                repaymentService.editPaidEmi(
                        repaymentId,
                        data.get("paidAmount").asDouble(),
                        data.get("penaltyAmount").asDouble()
                );
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported approval request type: " + requestType
            );
        }

        request.setStatus("APPROVED");
        request.setReviewedBy(currentUserService.getCurrentUser());
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminRemarks(remarks);

        return toDto(requestRepository.save(request));
    }

    // ADMIN: REJECT REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto reject(Long requestId, String remarks) {
        requireAdmin();

        Long cmrcId = currentUserService.getCurrentCmrcId();

        LoanApprovalRequest request = requestRepository
                .findByIdAndCmrcId(requestId, cmrcId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval request not found"
                ));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed"
            );
        }

        request.setStatus("REJECTED");
        request.setReviewedBy(currentUserService.getCurrentUser());
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminRemarks(remarks);

        return toDto(requestRepository.save(request));
    }

    // USER: SUBMIT PAY EMI REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto submitPayEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment
    ) {
        requireUser();
        validatePaymentAmounts(paidAmount, penaltyAmount);

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if ("PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This EMI is already paid. Use Edit Payment instead."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("repaymentId", repaymentId);
        data.put("paidAmount", paidAmount);
        data.put("penaltyAmount", penaltyAmount);
        data.put("regularRepayment", regularRepayment);

        LoanApprovalRequest request = new LoanApprovalRequest();
        request.setRequestType("PAY_EMI");
        request.setLoanId(repayment.getLoanId());
        request.setRequestData(toJsonObject(data));
        request.setRequestedBy(currentUserService.getCurrentUser());
        request.setCmrcId(currentUserService.getCurrentCmrcId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // USER: SUBMIT EDIT PAID EMI REQUEST
    @Override
    @Transactional
    public LoanApprovalRequestDto submitEditPaidEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount
    ) {
        requireUser();
        validatePaymentAmounts(paidAmount, penaltyAmount);

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if (!"PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only a fully paid EMI can be edited through this request."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("repaymentId", repaymentId);
        data.put("paidAmount", paidAmount);
        data.put("penaltyAmount", penaltyAmount);

        LoanApprovalRequest request = new LoanApprovalRequest();
        request.setRequestType("EDIT_EMI");
        request.setLoanId(repayment.getLoanId());
        request.setRequestData(toJsonObject(data));
        request.setRequestedBy(currentUserService.getCurrentUser());
        request.setCmrcId(currentUserService.getCurrentCmrcId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // VALIDATION
    private void validateLoanDto(LoanDto loanDto) {
        if (loanDto == null || loanDto.getWomanId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Woman is required"
            );
        }
    }

    private void validatePaymentAmounts(
            Double paidAmount,
            Double penaltyAmount
    ) {
        if (paidAmount == null
                || penaltyAmount == null
                || !Double.isFinite(paidAmount)
                || !Double.isFinite(penaltyAmount)
                || paidAmount < 0
                || penaltyAmount < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment and penalty amounts must be valid non-negative numbers."
            );
        }
    }

    private void requireUser() {
        User user = currentUserService.getCurrentUser();

        if (!"USER".equals(normalizeRole(user.getRole()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only USER accounts can submit approval requests"
            );
        }

        if (user.getCmrc() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No CMRC is assigned to this user"
            );
        }
    }

    private void requireAdmin() {
        User user = currentUserService.getCurrentUser();

        if (!"ADMIN".equals(normalizeRole(user.getRole()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can review loan requests"
            );
        }

        if (user.getCmrc() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No CMRC is assigned to this admin"
            );
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        return role.trim()
                .toUpperCase(Locale.ROOT)
                .replaceFirst("^ROLE_", "");
    }

    // JSON HELPERS
    private ObjectMapper configuredMapper() {
        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private String toJson(LoanDto loanDto) {
        try {
            return configuredMapper().writeValueAsString(loanDto);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not serialize loan request data: "
                            + e.getOriginalMessage(),
                    e
            );
        }
    }

    private String toJsonObject(Object data) {
        try {
            return configuredMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not serialize repayment approval request.",
                    e
            );
        }
    }

    private JsonNode readRequestData(String json) {
        try {
            return configuredMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid approval request data.",
                    e
            );
        }
    }

    // ENTITY -> DTO
    private LoanApprovalRequestDto toDto(LoanApprovalRequest request) {
        LoanApprovalRequestDto dto = new LoanApprovalRequestDto();

        dto.setId(request.getId());
        dto.setRequestType(request.getRequestType());
        dto.setStatus(request.getStatus());
        dto.setLoanId(request.getLoanId());
        dto.setRequestData(request.getRequestData());
        dto.setCmrcId(request.getCmrcId());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setReviewedAt(request.getReviewedAt());
        dto.setAdminRemarks(request.getAdminRemarks());

        if (request.getRequestedBy() != null) {
            dto.setRequestedByUserId(request.getRequestedBy().getId());
            dto.setRequestedByName(request.getRequestedBy().getName());
        }

        if (request.getReviewedBy() != null) {
            dto.setReviewedByUserId(request.getReviewedBy().getId());
            dto.setReviewedByName(request.getReviewedBy().getName());
        }

        // IMPORTANT FIX:
        // Populate repaymentDetails and linkedLoanDetails.
        addRepaymentDetails(request, dto);

        return dto;
    }

    // REPAYMENT + LINKED LOAN DETAILS
    private void addRepaymentDetails(
            LoanApprovalRequest request,
            LoanApprovalRequestDto dto
    ) {
        String type = request.getRequestType() == null
                ? ""
                : request.getRequestType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!"PAY_EMI".equals(type) && !"EDIT_EMI".equals(type)) {
            return;
        }

        JsonNode data = readRequestData(request.getRequestData());
        long repaymentId = data.path("repaymentId").asLong(-1);

        if (repaymentId <= 0) {
            return;
        }

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if (repayment.getLoanId() == null
                || !repayment.getLoanId().equals(request.getLoanId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Repayment does not belong to this approval request."
            );
        }

        Map<String, Object> details = new LinkedHashMap<>();

        // Existing repayment values from the database
        details.put("repaymentId", repayment.getId());
        details.put("loanId", repayment.getLoanId());
        details.put("installmentNo", repayment.getInstallmentNo());
        details.put("installmentDate", repayment.getInstallmentDate());
        details.put("repaymentDate", repayment.getRepaymentDate());
        details.put("scheduledAmount", repayment.getScheduledAmount());
        details.put("currentPaidAmount", repayment.getPaidAmount());
        details.put("principalAmount", repayment.getPrincipalAmount());
        details.put("interestAmount", repayment.getInterestAmount());
        details.put("currentPenaltyAmount", repayment.getPenaltyAmount());
        details.put("paymentStatus", repayment.getPaymentStatus());
        details.put("regularRepayment", repayment.getRegularRepayment());
        details.put("remark", repayment.getRemark());

        // Requested values from requestData
        double requestedPaidAmount =
                data.path("paidAmount").asDouble(0);

        double requestedPenaltyAmount =
                data.path("penaltyAmount").asDouble(0);

        details.put("requestedPaidAmount", requestedPaidAmount);
        details.put("requestedPenaltyAmount", requestedPenaltyAmount);

        double scheduledAmount = repayment.getScheduledAmount() == null
                ? 0.0
                : repayment.getScheduledAmount();

        double currentPaidAmount = repayment.getPaidAmount() == null
                ? 0.0
                : repayment.getPaidAmount();

        details.put(
                "remainingBeforeRequest",
                Math.max(0.0, scheduledAmount - currentPaidAmount)
        );

        details.put(
                "remainingAfterRequest",
                Math.max(0.0, scheduledAmount - requestedPaidAmount)
        );

        details.put(
                "requestedRegularRepayment",
                data.hasNonNull("regularRepayment")
                        ? data.get("regularRepayment").asBoolean()
                        : repayment.getRegularRepayment()
        );

        dto.setRepaymentDetails(details);

        // Linked loan information
        if (request.getLoanId() != null) {
            LoanDto loan = loanService.getById(request.getLoanId());

            Map<String, Object> loanDetails = configuredMapper()
                    .convertValue(
                            loan,
                            new TypeReference<Map<String, Object>>() {}
                    );

            dto.setLinkedLoanDetails(loanDetails);
        }
    }
}
