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
    private final RepaymentService repaymentService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    // =========================================================
    // SUBMIT CREATE LOAN REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto submitCreateRequest(LoanDto loanDto) {

        User user = requireUser();

        validateLoanDto(loanDto);
        loanService.getByWomanId(loanDto.getWomanId());

        LoanApprovalRequest request = new LoanApprovalRequest();

        request.setRequestType("CREATE");
        request.setLoanId(null);
        request.setRequestData(toJson(loanDto));
        request.setRequestedBy(user);
        request.setCmrcId(user.getCmrc().getId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // SUBMIT UPDATE LOAN REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto submitUpdateRequest(
            Long loanId,
            LoanDto loanDto) {

        User user = requireUser();

        validateLoanDto(loanDto);

        loanService.getById(loanId);
        loanService.getByWomanId(loanDto.getWomanId());

        LoanApprovalRequest request = new LoanApprovalRequest();

        request.setRequestType("UPDATE");
        request.setLoanId(loanId);
        request.setRequestData(toJson(loanDto));
        request.setRequestedBy(user);
        request.setCmrcId(user.getCmrc().getId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // SUBMIT DELETE LOAN REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto submitDeleteRequest(Long loanId) {

        User user = requireUser();

        loanService.getById(loanId);

        LoanApprovalRequest request = new LoanApprovalRequest();

        request.setRequestType("DELETE");
        request.setLoanId(loanId);
        request.setRequestData("{}");
        request.setRequestedBy(user);
        request.setCmrcId(user.getCmrc().getId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // SUBMIT EMI PAYMENT REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto submitPayEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment) {

        User user = requireUser();

        validatePaymentAmounts(paidAmount, penaltyAmount);

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if ("PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This EMI is already fully paid."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("repaymentId", repaymentId);
        data.put("paidAmount", paidAmount);
        data.put(
                "penaltyAmount",
                penaltyAmount == null ? 0.0 : penaltyAmount
        );
        data.put(
                "regularRepayment",
                Boolean.TRUE.equals(regularRepayment)
        );

        LoanApprovalRequest request = new LoanApprovalRequest();

        request.setRequestType("PAY_EMI");
        request.setLoanId(repayment.getLoanId());
        request.setRequestData(toJsonObject(data));
        request.setRequestedBy(user);
        request.setCmrcId(user.getCmrc().getId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // SUBMIT EDIT PAID EMI REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto submitEditPaidEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        User user = requireUser();

        validatePaymentAmounts(paidAmount, penaltyAmount);

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if (!"PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only a fully paid EMI can be submitted for editing."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("repaymentId", repaymentId);
        data.put("paidAmount", paidAmount);
        data.put(
                "penaltyAmount",
                penaltyAmount == null ? 0.0 : penaltyAmount
        );

        LoanApprovalRequest request = new LoanApprovalRequest();

        request.setRequestType("EDIT_EMI");
        request.setLoanId(repayment.getLoanId());
        request.setRequestData(toJsonObject(data));
        request.setRequestedBy(user);
        request.setCmrcId(user.getCmrc().getId());
        request.setStatus("PENDING");

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // GET PENDING REQUESTS
    // =========================================================

    @Override
    public List<LoanApprovalRequestDto> getPendingRequests() {

        User admin = requireAdmin();

        return requestRepository
                .findByCmrcIdAndStatusOrderByCreatedAtDesc(
                        admin.getCmrc().getId(),
                        "PENDING"
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    @Override
    public List<LoanApprovalRequestDto> getMyRequests() {

        User user = requireUser();

        return requestRepository
                .findByRequestedBy_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto approve(Long requestId, String remarks) {

        User admin = requireAdmin();

        LoanApprovalRequest request = requestRepository
                .findByIdAndCmrcId(
                        requestId,
                        admin.getCmrc().getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval request not found."
                ));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed."
            );
        }

        String requestType = normalizeRequestType(
                request.getRequestType()
        );

        switch (requestType) {

            case "PAY_EMI" -> approveEmiPayment(request);

            case "EDIT_EMI" -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Paid EMI editing is disabled until a ledger "
                            + "correction/refund workflow is implemented."
            );

            case "CREATE" -> {
                LoanDto loanDto = readLoanDto(request.getRequestData());
                LoanDto createdLoan = loanService.create(loanDto);
                request.setLoanId(createdLoan.getId());
            }

            case "UPDATE" -> {
                if (request.getLoanId() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Loan ID is missing from the update request."
                    );
                }

                LoanDto loanDto = readLoanDto(request.getRequestData());
                loanService.update(request.getLoanId(), loanDto);
            }

            case "DELETE" -> {
                if (request.getLoanId() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Loan ID is missing from the delete request."
                    );
                }

                loanService.delete(request.getLoanId());
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported approval request type: " + requestType
            );
        }

        request.setStatus("APPROVED");
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminRemarks(remarks);

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // APPROVE EMI PAYMENT
    // =========================================================

    private void approveEmiPayment(LoanApprovalRequest request) {

        JsonNode data = readRequestData(request.getRequestData());

        if (!data.hasNonNull("repaymentId")
                || !data.hasNonNull("paidAmount")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Repayment ID and paid amount are required."
            );
        }

        Long repaymentId = data.get("repaymentId").asLong();

        RepaymentDto repayment = repaymentService.getById(repaymentId);

        if (request.getLoanId() == null
                || repayment.getLoanId() == null
                || !request.getLoanId().equals(repayment.getLoanId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The repayment does not belong to the requested loan."
            );
        }

        if ("PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This EMI is already fully paid."
            );
        }

        double paidAmount = data.get("paidAmount").asDouble();

        double penaltyAmount = data.hasNonNull("penaltyAmount")
                ? data.get("penaltyAmount").asDouble()
                : 0.0;

        boolean regularRepayment =
                data.hasNonNull("regularRepayment")
                        && data.get("regularRepayment").asBoolean();

        validatePaymentAmounts(paidAmount, penaltyAmount);

        String paymentEventId =
                "EMI_APPROVAL_REQUEST:" + request.getId();

        repaymentService.payEmi(
                repaymentId,
                paidAmount,
                penaltyAmount,
                regularRepayment,
                paymentEventId
        );
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @Override
    @Transactional
    public LoanApprovalRequestDto reject(Long requestId, String remarks) {

        User admin = requireAdmin();

        LoanApprovalRequest request = requestRepository
                .findByIdAndCmrcId(
                        requestId,
                        admin.getCmrc().getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Approval request not found."
                ));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed."
            );
        }

        request.setStatus("REJECTED");
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminRemarks(remarks);

        return toDto(requestRepository.save(request));
    }

    // =========================================================
    // REPAYMENT DETAILS FOR APPROVAL POPUP
    // =========================================================

    private void addRepaymentDetails(
            LoanApprovalRequest request,
            LoanApprovalRequestDto dto) {

        String requestType = normalizeRequestType(
                request.getRequestType()
        );

        if (!"PAY_EMI".equals(requestType)
                && !"EDIT_EMI".equals(requestType)) {
            return;
        }

        JsonNode data = readRequestData(request.getRequestData());

        if (!data.hasNonNull("repaymentId")) {
            return;
        }

        Long repaymentId = data.get("repaymentId").asLong();

        RepaymentDto repayment;

        try {
            repayment = repaymentService.getById(repaymentId);
        } catch (RuntimeException ex) {
            return;
        }

        if (request.getLoanId() == null
                || repayment.getLoanId() == null
                || !request.getLoanId().equals(repayment.getLoanId())) {
            return;
        }

        Map<String, Object> details = new LinkedHashMap<>();

        double currentPaid = valueOrZero(repayment.getPaidAmount());
        double scheduledAmount = valueOrZero(
                repayment.getScheduledAmount()
        );

        double requestedPaidAmount = data.hasNonNull("paidAmount")
                ? data.get("paidAmount").asDouble()
                : 0.0;

        double requestedPenaltyAmount = data.hasNonNull("penaltyAmount")
                ? data.get("penaltyAmount").asDouble()
                : 0.0;

        double currentPenalty = valueOrZero(
                repayment.getPenaltyAmount()
        );

        double remainingBeforeRequest = Math.max(
                0.0,
                scheduledAmount - currentPaid
        );

        double remainingAfterRequest = Math.max(
                0.0,
                scheduledAmount - currentPaid - requestedPaidAmount
        );

        details.put("repaymentId", repayment.getId());
        details.put("loanId", repayment.getLoanId());
        details.put("installmentNo", repayment.getInstallmentNo());
        details.put("installmentDate", repayment.getInstallmentDate());
        details.put("scheduledEmi", scheduledAmount);
        details.put("currentPaidAmount", currentPaid);
        details.put("requestedPaidAmount", requestedPaidAmount);
        details.put("currentPenaltyAmount", currentPenalty);
        details.put("requestedPenaltyAmount", requestedPenaltyAmount);
        details.put("principalAmount", repayment.getPrincipalAmount());
        details.put("interestAmount", repayment.getInterestAmount());
        details.put("remainingBeforeRequest", remainingBeforeRequest);
        details.put("remainingAfterRequest", remainingAfterRequest);
        details.put("status", repayment.getPaymentStatus());
        details.put(
                "regularRepayment",
                data.hasNonNull("regularRepayment")
                        && data.get("regularRepayment").asBoolean()
        );

        // Matches LoanApprovalRequestDto field name.
        dto.setRepaymentDetails(details);
    }

    // =========================================================
    // DTO MAPPING
    // =========================================================

    private LoanApprovalRequestDto toDto(LoanApprovalRequest request) {

        LoanApprovalRequestDto dto = new LoanApprovalRequestDto();

        dto.setId(request.getId());
        dto.setRequestType(request.getRequestType());
        dto.setLoanId(request.getLoanId());
        dto.setRequestData(request.getRequestData());
        dto.setCmrcId(request.getCmrcId());
        dto.setStatus(request.getStatus());
        dto.setAdminRemarks(request.getAdminRemarks());
        dto.setReviewedAt(request.getReviewedAt());

        if (request.getRequestedBy() != null) {
            dto.setRequestedByUserId(
                    request.getRequestedBy().getId()
            );
            dto.setRequestedByName(
                    request.getRequestedBy().getName()
            );
        }

        if (request.getReviewedBy() != null) {
            dto.setReviewedByUserId(
                    request.getReviewedBy().getId()
            );
            dto.setReviewedByName(
                    request.getReviewedBy().getName()
            );
        }

        addRepaymentDetails(request, dto);

        return dto;
    }

    // =========================================================
    // AUTHORIZATION
    // =========================================================

    private User requireUser() {

        User user = currentUserService.getCurrentUser();

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated."
            );
        }

        if (!"USER".equals(normalizeRole(user.getRole()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only USER accounts can submit approval requests."
            );
        }

        if (user.getCmrc() == null || user.getCmrc().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The user is not assigned to a CMRC."
            );
        }

        return user;
    }

    private User requireAdmin() {

        User user = currentUserService.getCurrentUser();

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated."
            );
        }

        if (!"ADMIN".equals(normalizeRole(user.getRole()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN accounts can review approval requests."
            );
        }

        if (user.getCmrc() == null || user.getCmrc().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The admin is not assigned to a CMRC."
            );
        }

        return user;
    }

    private String normalizeRole(String role) {

        if (role == null) {
            return "";
        }

        return role.trim()
                .toUpperCase(Locale.ROOT)
                .replace("ROLE_", "");
    }

    private String normalizeRequestType(String requestType) {

        if (requestType == null) {
            return "";
        }

        return requestType.trim().toUpperCase(Locale.ROOT);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateLoanDto(LoanDto loanDto) {

        if (loanDto == null || loanDto.getWomanId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Woman ID is required."
            );
        }
    }

    private void validatePaymentAmounts(
            Double paidAmount,
            Double penaltyAmount) {

        if (paidAmount == null || paidAmount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Paid amount must be greater than zero."
            );
        }

        if (penaltyAmount != null && penaltyAmount < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Penalty amount cannot be negative."
            );
        }
    }

    private double valueOrZero(Double value) {
        return value != null ? value : 0.0;
    }

    // =========================================================
    // JSON HELPERS
    // =========================================================

    private ObjectMapper configuredMapper() {

        ObjectMapper mapper = objectMapper.copy();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    private String toJson(LoanDto loanDto) {

        try {
            return configuredMapper().writeValueAsString(loanDto);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to serialize loan request data."
            );
        }
    }

    private String toJsonObject(Map<String, Object> data) {

        try {
            return configuredMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to serialize approval request data."
            );
        }
    }

    private JsonNode readRequestData(String requestData) {

        try {
            if (requestData == null || requestData.isBlank()) {
                return configuredMapper().createObjectNode();
            }

            return configuredMapper().readTree(requestData);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid approval request data."
            );
        }
    }

    private LoanDto readLoanDto(String requestData) {

        try {
            return configuredMapper().readValue(
                    requestData,
                    new TypeReference<LoanDto>() {}
            );
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid loan request data."
            );
        }
    }
}
