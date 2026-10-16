
package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.AuditReportDto;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.FundAccountRepository;
import com.example.loan_management.repository.FundTransactionRepository;
import com.example.loan_management.repository.VoAlfRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditReportService {

    private final CurrentUserService currentUserService;
    private final FundAccountRepository fundAccountRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final VoAlfRepository voAlfRepository;

    public AuditReportDto generateReport(
            String reportType,
            int financialYear,
            Integer month) {

        String normalizedType = reportType == null
                ? "MONTHLY"
                : reportType.trim().toUpperCase();

        LocalDate startDate;
        LocalDate endDate;
        String periodLabel;

        if ("YEARLY".equals(normalizedType)) {
            startDate = LocalDate.of(financialYear, Month.APRIL, 1);
            endDate = startDate.plusYears(1).minusDays(1);

            periodLabel = financialYear + "-" +
                    String.format("%02d",
                            (financialYear + 1) % 100);

        } else if ("MONTHLY".equals(normalizedType)) {

            if (month == null || month < 1 || month > 12) {
                throw new IllegalArgumentException(
                        "For monthly reports, month must be between 1 and 12");
            }

            startDate = LocalDate.of(financialYear, month, 1);
            endDate = startDate.withDayOfMonth(
                    startDate.lengthOfMonth());

            periodLabel = startDate.getMonth().name()
                    + " " + financialYear;

        } else {
            throw new IllegalArgumentException(
                    "reportType must be MONTHLY or YEARLY");
        }

        boolean superAdmin = currentUserService.isSuperAdmin();
        Long currentCmrcId = superAdmin
                ? null
                : currentUserService.getCurrentCmrcId();

        List<VoAlf> voAlfs = superAdmin
                ? voAlfRepository.findAll()
                : voAlfRepository.findByCmrcId(currentCmrcId);

        AuditReportDto report = new AuditReportDto();
        report.setReportTitle("Comprehensive Audit Report");
        report.setPeriodLabel(periodLabel);
        report.setGeneratedAt(LocalDateTime.now());

        BigDecimal totalOpening = BigDecimal.ZERO;
        BigDecimal totalReceipts = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal totalClosing = BigDecimal.ZERO;

        int totalPending = 0;

        for (VoAlf voAlf : voAlfs) {
            FundAccount account = fundAccountRepository
                    .findByVoAlfId(voAlf.getId())
                    .orElse(null);

            if (account == null) {
                report.getExceptions().add(
                        "Missing fund account for VO/ALF: "
                                + voAlf.getVoAlfName());
                continue;
            }

            List<FundTransaction> transactions =
                    fundTransactionRepository
                            .findByFundAccountIdOrderByTransactionDateDescIdDesc(
                                    account.getId());

            AuditReportDto.AuditLine line =
                    buildLine(voAlf, account, transactions,
                            startDate, endDate);

            report.getLines().add(line);

            totalOpening = totalOpening.add(line.getOpeningBalance());
            totalReceipts = totalReceipts.add(line.getReceipts());
            totalPayments = totalPayments.add(line.getPayments());
            totalClosing = totalClosing.add(line.getClosingBalance());
            totalPending += line.getPendingApprovals();

            report.getExceptions().addAll(line.getExceptions());
        }

        report.setPendingApprovals(totalPending);

        boolean reconciled = report.getExceptions().isEmpty();

        report.setReconciled(reconciled);
        report.setMessage(
                "Opening: " + totalOpening
                        + " | Receipts: " + totalReceipts
                        + " | Payments: " + totalPayments
                        + " | Closing: " + totalClosing
                        + (reconciled
                        ? " | No detected balance mismatches."
                        : " | Review the listed audit exceptions."));

        return report;
    }

    private AuditReportDto.AuditLine buildLine(
            VoAlf voAlf,
            FundAccount account,
            List<FundTransaction> transactions,
            LocalDate startDate,
            LocalDate endDate) {

        AuditReportDto.AuditLine line =
                new AuditReportDto.AuditLine();

        line.setVoAlfId(voAlf.getId());
        line.setVoAlfName(voAlf.getVoAlfName());
        line.setVillageName(voAlf.getVillageName());
        line.setAccountNo(voAlf.getAccountNo());

        if (voAlf.getCmrc() != null) {
            line.setCmrcId(voAlf.getCmrc().getId());
            line.setCmrcName(voAlf.getCmrc().getCmrcName());
        }

        BigDecimal opening = zero(account.getOpeningBalance());
        BigDecimal receipts = BigDecimal.ZERO;
        BigDecimal payments = BigDecimal.ZERO;
        BigDecimal loanDisbursement = BigDecimal.ZERO;
        BigDecimal interestReceived = BigDecimal.ZERO;
        BigDecimal penaltyReceived = BigDecimal.ZERO;

        int pending = 0;

        for (FundTransaction tx : transactions) {
            if (tx.getTransactionDate() == null) {
                continue;
            }

            FundTransaction.FundCategory category = tx.getCategory();

            if (tx.getApprovalStatus()
                    == FundTransaction.ApprovalStatus.PENDING
                    && !tx.getTransactionDate().isBefore(startDate)
                    && !tx.getTransactionDate().isAfter(endDate)) {
                pending++;
            }

            if (tx.getApprovalStatus()
                    != FundTransaction.ApprovalStatus.APPROVED) {
                continue;
            }

            if (category == FundTransaction.FundCategory.OPENING_BALANCE) {
                continue;
            }

            BigDecimal amount = zero(tx.getAmount());

            if (tx.getTransactionDate().isBefore(startDate)) {
                if (tx.getTransactionType()
                        == FundTransaction.TransactionType.RECEIPT) {
                    opening = opening.add(amount);
                } else {
                    opening = opening.subtract(amount);
                }
                continue;
            }

            if (tx.getTransactionDate().isAfter(endDate)) {
                continue;
            }

            if (tx.getTransactionType()
                    == FundTransaction.TransactionType.RECEIPT) {
                receipts = receipts.add(amount);
            } else {
                payments = payments.add(amount);
            }

            if (category
                    == FundTransaction.FundCategory.LOAN_DISBURSEMENT) {
                loanDisbursement = loanDisbursement.add(amount);
            }

            if (category
                    == FundTransaction.FundCategory.INTEREST_RECEIVED) {
                interestReceived = interestReceived.add(amount);
            }

            if (category
                    == FundTransaction.FundCategory.PENALTY_RECEIVED) {
                penaltyReceived = penaltyReceived.add(amount);
            }
        }

        BigDecimal closing = opening.add(receipts).subtract(payments);

        line.setOpeningBalance(opening);
        line.setReceipts(receipts);
        line.setPayments(payments);
        line.setClosingBalance(closing);

        line.setLoanDisbursement(loanDisbursement);
        line.setInterestReceived(interestReceived);
        line.setPenaltyReceived(penaltyReceived);

        // Principal/interest split is not derivable reliably
        // from the fund ledger alone.
        line.setPrincipalRecovered(BigDecimal.ZERO);

        BigDecimal currentFundBalance = zero(account.getBalance());
        BigDecimal voAlfBalance = BigDecimal.valueOf(
                voAlf.getReceivedFund() == null
                        ? 0.0
                        : voAlf.getReceivedFund());

        line.setFundAccountBalance(currentFundBalance);
        line.setCurrentVoAlfBalance(voAlfBalance);
        line.setPendingApprovals(pending);

        if (currentFundBalance.compareTo(voAlfBalance) != 0) {
            line.getExceptions().add(
                    "FundAccount balance and VO/ALF receivedFund mismatch for "
                            + voAlf.getVoAlfName());
        }

        if (pending > 0) {
            line.getExceptions().add(
                    pending + " pending transaction(s) for "
                            + voAlf.getVoAlfName());
        }

        return line;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
