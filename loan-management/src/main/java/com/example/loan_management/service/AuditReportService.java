
package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.AuditReportDto;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.FundAccountRepository;
import com.example.loan_management.repository.FundTransactionRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditReportService {

    private final CurrentUserService currentUserService;
    private final FundAccountRepository fundAccountRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final VoAlfRepository voAlfRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

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

            periodLabel = financialYear + "-"
                    + String.format("%02d", (financialYear + 1) % 100);

        } else if ("MONTHLY".equals(normalizedType)) {

            if (month == null || month < 1 || month > 12) {
                throw new IllegalArgumentException(
                        "For monthly reports, month must be between 1 and 12");
            }

            // January-March belong to the next calendar year
            // of the selected financial year.
            int calendarYear = month >= 4
                    ? financialYear
                    : financialYear + 1;

            startDate = LocalDate.of(calendarYear, month, 1);
            endDate = startDate.withDayOfMonth(
                    startDate.lengthOfMonth());

            periodLabel = startDate.getMonth().name()
                    + " " + calendarYear;

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
        BigDecimal totalDisbursement = BigDecimal.ZERO;
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPenalty = BigDecimal.ZERO;

        int totalPending = 0;

        // Loan retrieval follows the current user's CMRC scope.
        List<Loan> authorizedLoans = superAdmin
                ? loanRepository.findAll()
                : loanRepository.findByWoman_Group_CmrcId(currentCmrcId);

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

            AuditReportDto.AuditLine line = buildLine(
                    voAlf,
                    account,
                    transactions,
                    startDate,
                    endDate
            );

            // Add loan, group, woman and repayment details.
            populateLoanHierarchy(
                    line,
                    voAlf,
                    authorizedLoans,
                    startDate,
                    endDate
            );

            report.getLines().add(line);

            totalOpening = totalOpening.add(line.getOpeningBalance());
            totalReceipts = totalReceipts.add(line.getReceipts());
            totalPayments = totalPayments.add(line.getPayments());
            totalClosing = totalClosing.add(line.getClosingBalance());

            totalDisbursement =
                    totalDisbursement.add(line.getLoanDisbursement());

            totalPrincipal =
                    totalPrincipal.add(line.getPrincipalRecovered());

            totalInterest =
                    totalInterest.add(line.getInterestReceived());

            totalPenalty =
                    totalPenalty.add(line.getPenaltyReceived());

            totalPending += line.getPendingApprovals();

            report.getExceptions().addAll(line.getExceptions());
        }

        report.setTotalOpeningBalance(totalOpening);
        report.setTotalReceipts(totalReceipts);
        report.setTotalPayments(totalPayments);
        report.setTotalClosingBalance(totalClosing);
        report.setTotalLoanDisbursement(totalDisbursement);
        report.setTotalPrincipalRecovered(totalPrincipal);
        report.setTotalInterestReceived(totalInterest);
        report.setTotalPenaltyReceived(totalPenalty);
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
                        : " | Review the listed audit exceptions.")
        );

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
        BigDecimal principalRecovered = BigDecimal.ZERO;
        BigDecimal interestReceived = BigDecimal.ZERO;
        BigDecimal penaltyReceived = BigDecimal.ZERO;

        BigDecimal fundReceived = BigDecimal.ZERO;
        BigDecimal bankFundReceived = BigDecimal.ZERO;
        BigDecimal loanRepayment = BigDecimal.ZERO;
        BigDecimal otherIncome = BigDecimal.ZERO;

        BigDecimal bankCharges = BigDecimal.ZERO;
        BigDecimal smsCharges = BigDecimal.ZERO;
        BigDecimal recordFees = BigDecimal.ZERO;
        BigDecimal accountantExpense = BigDecimal.ZERO;
        BigDecimal cmrcPayment = BigDecimal.ZERO;
        BigDecimal kitExpense = BigDecimal.ZERO;
        BigDecimal otherExpense = BigDecimal.ZERO;
        BigDecimal transferToCmrc = BigDecimal.ZERO;

        int pending = 0;

        for (FundTransaction tx : transactions) {

            if (tx.getTransactionDate() == null) {
                continue;
            }

            FundTransaction.FundCategory category = tx.getCategory();

            boolean inPeriod =
                    !tx.getTransactionDate().isBefore(startDate)
                            && !tx.getTransactionDate().isAfter(endDate);

            if (tx.getApprovalStatus()
                    == FundTransaction.ApprovalStatus.PENDING
                    && inPeriod) {
                pending++;
            }

            // Pending and rejected transactions never affect balances.
            if (tx.getApprovalStatus()
                    != FundTransaction.ApprovalStatus.APPROVED) {
                continue;
            }

            // Opening balance is a baseline, not an ordinary transaction.
            if (category
                    == FundTransaction.FundCategory.OPENING_BALANCE) {
                continue;
            }

            BigDecimal amount = zero(tx.getAmount());

            if (tx.getTransactionDate().isBefore(startDate)) {

                if (tx.getTransactionType()
                        == FundTransaction.TransactionType.RECEIPT) {
                    opening = opening.add(amount);
                } else if (tx.getTransactionType()
                        == FundTransaction.TransactionType.PAYMENT) {
                    opening = opening.subtract(amount);
                }

                continue;
            }

            if (tx.getTransactionDate().isAfter(endDate)) {
                continue;
            }

            AuditReportDto.TransactionAudit transactionAudit =
                    new AuditReportDto.TransactionAudit();

            transactionAudit.setTransactionId(tx.getId());
            transactionAudit.setTransactionDate(tx.getTransactionDate());
            transactionAudit.setTransactionType(
                    tx.getTransactionType() == null
                            ? null
                            : tx.getTransactionType().name());
            transactionAudit.setCategory(
                    category == null ? null : category.name());
            transactionAudit.setApprovalStatus(
                    tx.getApprovalStatus() == null
                            ? null : tx.getApprovalStatus().name());
            transactionAudit.setAmount(amount);
            transactionAudit.setRemark(tx.getRemark());
            transactionAudit.setReferenceType(tx.getReferenceType());
            transactionAudit.setReferenceId(tx.getReferenceId());

            line.getTransactions().add(transactionAudit);

            boolean isReceipt = tx.getTransactionType()
                    == FundTransaction.TransactionType.RECEIPT;

            boolean isPayment = tx.getTransactionType()
                    == FundTransaction.TransactionType.PAYMENT;

            if (isReceipt) {
                receipts = receipts.add(amount);
            } else if (isPayment) {
                payments = payments.add(amount);
            }

            if (category == null) {
                continue;
            }

            switch (category) {

                case FUND_RECEIVED:
                    if (isReceipt) {
                        fundReceived = fundReceived.add(amount);
                    }
                    break;

                case BANK_FUND_RECEIVED:
                    if (isReceipt) {
                        bankFundReceived =
                                bankFundReceived.add(amount);
                    }
                    break;

                case LOAN_REPAYMENT:
                    if (isReceipt) {
                        loanRepayment = loanRepayment.add(amount);
                    }
                    break;

                case INTEREST_RECEIVED:
                    if (isReceipt) {
                        interestReceived =
                                interestReceived.add(amount);
                    }
                    break;

                case PENALTY_RECEIVED:
                    if (isReceipt) {
                        penaltyReceived =
                                penaltyReceived.add(amount);
                    }
                    break;

                case OTHER_INCOME:
                    if (isReceipt) {
                        otherIncome = otherIncome.add(amount);
                    }
                    break;

                case LOAN_DISBURSEMENT:
                    if (isPayment) {
                        loanDisbursement =
                                loanDisbursement.add(amount);
                    }
                    break;

                case BANK_CHARGES:
                    if (isPayment) {
                        bankCharges = bankCharges.add(amount);
                    }
                    break;

                case SMS_CHARGES:
                    if (isPayment) {
                        smsCharges = smsCharges.add(amount);
                    }
                    break;

                case RECORD_FEES:
                    if (isPayment) {
                        recordFees = recordFees.add(amount);
                    }
                    break;

                case ACCOUNTANT_EXPENSE:
                    if (isPayment) {
                        accountantExpense =
                                accountantExpense.add(amount);
                    }
                    break;

                case CMRC_PAYMENT:
                    if (isPayment) {
                        cmrcPayment = cmrcPayment.add(amount);
                    }
                    break;

                case KIT_EXPENSE:
                    if (isPayment) {
                        kitExpense = kitExpense.add(amount);
                    }
                    break;

                case OTHER_EXPENSE:
                    if (isPayment) {
                        otherExpense = otherExpense.add(amount);
                    }
                    break;

                case TRANSFER_TO_CMRC:
                    if (isPayment) {
                        transferToCmrc =
                                transferToCmrc.add(amount);
                    }
                    break;

                default:
                    break;
            }
        }

        BigDecimal closing = opening.add(receipts).subtract(payments);

        line.setOpeningBalance(opening);
        line.setReceipts(receipts);
        line.setPayments(payments);
        line.setClosingBalance(closing);

        line.setLoanDisbursement(loanDisbursement);
        line.setPrincipalRecovered(principalRecovered);
        line.setInterestReceived(interestReceived);
        line.setPenaltyReceived(penaltyReceived);

        line.setFundReceived(fundReceived);
        line.setBankFundReceived(bankFundReceived);
        line.setLoanRepayment(loanRepayment);
        line.setOtherIncome(otherIncome);

        line.setBankCharges(bankCharges);
        line.setSmsCharges(smsCharges);
        line.setRecordFees(recordFees);
        line.setAccountantExpense(accountantExpense);
        line.setCmrcPayment(cmrcPayment);
        line.setKitExpense(kitExpense);
        line.setOtherExpense(otherExpense);
        line.setTransferToCmrc(transferToCmrc);

        BigDecimal currentFundBalance = zero(account.getBalance());

        BigDecimal voAlfBalance = BigDecimal.valueOf(
                voAlf.getReceivedFund() == null
                        ? 0.0
                        : voAlf.getReceivedFund()
        );

        line.setFundAccountBalance(currentFundBalance);
        line.setCurrentVoAlfBalance(voAlfBalance);
        line.setPendingApprovals(pending);

        if (currentFundBalance.compareTo(voAlfBalance) != 0) {
            line.getExceptions().add(
                    "FundAccount balance and VO/ALF receivedFund mismatch for "
                            + voAlf.getVoAlfName()
            );
        }

        if (pending > 0) {
            line.getExceptions().add(
                    pending + " pending transaction(s) for "
                            + voAlf.getVoAlfName()
            );
        }

        return line;
    }

    private void populateLoanHierarchy(
            AuditReportDto.AuditLine line,
            VoAlf voAlf,
            List<Loan> authorizedLoans,
            LocalDate startDate,
            LocalDate endDate) {

        Map<Long, AuditReportDto.GroupAudit> groups =
                new LinkedHashMap<>();

        Map<Long, AuditReportDto.WomanAudit> women =
                new LinkedHashMap<>();

        BigDecimal periodDisbursement = BigDecimal.ZERO;
        BigDecimal periodPrincipal = BigDecimal.ZERO;
        BigDecimal periodInterest = BigDecimal.ZERO;
        BigDecimal periodPenalty = BigDecimal.ZERO;

        for (Loan loan : authorizedLoans) {

            if (loan.getWoman() == null
                    || loan.getWoman().getGroup() == null) {
                continue;
            }

            com.example.loan_management.entity.Group group =
                    loan.getWoman().getGroup();

            Long loanVoAlfId = loan.getVoAlfId() != null
                    ? loan.getVoAlfId()
                    : group.getVoAlfId();

            if (loanVoAlfId == null
                    || !loanVoAlfId.equals(voAlf.getId())) {
                continue;
            }

            AuditReportDto.GroupAudit groupAudit =
                    groups.computeIfAbsent(group.getId(), id -> {
                        AuditReportDto.GroupAudit item =
                                new AuditReportDto.GroupAudit();
                        item.setGroupId(group.getId());
                        item.setGroupName(group.getGroupName());
                        item.setVillageName(group.getVillageName());
                        return item;
                    });

            AuditReportDto.WomanAudit womanAudit =
                    women.computeIfAbsent(loan.getWoman().getId(), id -> {
                        AuditReportDto.WomanAudit item =
                                new AuditReportDto.WomanAudit();
                        item.setWomanId(loan.getWoman().getId());
                        item.setWomanName(
                                loan.getWoman().getWomanName());
                        item.setHusbandName(
                                loan.getWoman().getHusbandName());
                        item.setMobileNo(
                                loan.getWoman().getMobileNo());
                        item.setStatus(loan.getWoman().getStatus());

                        groupAudit.getWomen().add(item);
                        return item;
                    });

            AuditReportDto.LoanAudit loanAudit =
                    new AuditReportDto.LoanAudit();

            loanAudit.setLoanId(loan.getId());
            loanAudit.setLoanGivenDate(loan.getLoanGivenDate());
            loanAudit.setLoanPurpose(loan.getLoanPurpose());
            loanAudit.setLoanStatus(loan.getLoanStatus());
            loanAudit.setInterestType(loan.getInterestType());

            loanAudit.setSanctionedAmount(
                    decimal(loan.getSanctionedAmount()));
            loanAudit.setProcessingFee(
                    decimal(loan.getProcessingFee()));
            loanAudit.setDisbursedAmount(
                    decimal(loan.getLoanAmount()));
            loanAudit.setInterestRate(
                    decimal(loan.getInterestRate()));
            loanAudit.setRepaymentPeriodMonths(
                    loan.getRepaymentPeriodMonths());

            List<Repayment> repayments =
                    repaymentRepository.findByLoanId(loan.getId());

            BigDecimal scheduledTotal = BigDecimal.ZERO;
            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal principalPaid = BigDecimal.ZERO;
            BigDecimal interestPaid = BigDecimal.ZERO;
            BigDecimal penaltyPaid = BigDecimal.ZERO;

            for (Repayment repayment : repayments) {

                BigDecimal scheduled = decimal(
                        repayment.getScheduledAmount());

                BigDecimal paid = decimal(
                        repayment.getPaidAmount());

                BigDecimal penalty = decimal(
                        repayment.getPenaltyAmount());

                scheduledTotal = scheduledTotal.add(scheduled);

                boolean hasPayment = paid.signum() > 0;

                String status = repayment.getPaymentStatus() == null
                        ? ""
                        : repayment.getPaymentStatus()
                        .trim().toUpperCase();

                boolean paidOrPartial = "PAID".equals(status)
                        || "PARTIAL".equals(status)
                        || hasPayment;

                if (paidOrPartial) {
                    totalPaid = totalPaid.add(paid);
                    penaltyPaid = penaltyPaid.add(penalty);

                    boolean paymentInPeriod =
                            repayment.getRepaymentDate() != null
                                    && !repayment.getRepaymentDate()
                                    .isBefore(startDate)
                                    && !repayment.getRepaymentDate()
                                    .isAfter(endDate);

                    if (paymentInPeriod) {
                        principalPaid = principalPaid.add(
                                decimal(repayment.getPrincipalAmount()));

                        interestPaid = interestPaid.add(
                                decimal(repayment.getInterestAmount()));
                    }
                }

                AuditReportDto.EmiAudit emi =
                        new AuditReportDto.EmiAudit();

                emi.setRepaymentId(repayment.getId());
                emi.setInstallmentNo(repayment.getInstallmentNo());
                emi.setInstallmentDate(repayment.getInstallmentDate());
                emi.setRepaymentDate(repayment.getRepaymentDate());
                emi.setScheduledAmount(scheduled);
                emi.setPaidAmount(paid);
                emi.setRemainingAmount(
                        scheduled.subtract(paid).max(BigDecimal.ZERO));

                emi.setPrincipalAmount(
                        decimal(repayment.getPrincipalAmount()));
                emi.setInterestAmount(
                        decimal(repayment.getInterestAmount()));
                emi.setPenaltyAmount(penalty);
                emi.setPaymentStatus(repayment.getPaymentStatus());
                emi.setRegularRepayment(repayment.getRegularRepayment());
                emi.setRemark(repayment.getRemark());

                loanAudit.getInstallments().add(emi);
            }

            loanAudit.getInstallments().sort(
                    Comparator.comparing(
                            AuditReportDto.EmiAudit::getInstallmentNo,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
            );

            BigDecimal disbursed = decimal(loan.getLoanAmount());

            BigDecimal outstanding = disbursed
                    .subtract(principalPaid)
                    .max(BigDecimal.ZERO);

            loanAudit.setScheduledAmount(scheduledTotal);
            loanAudit.setTotalPaid(totalPaid);
            loanAudit.setPrincipalRecovered(principalPaid);
            loanAudit.setInterestReceived(interestPaid);
            loanAudit.setPenaltyReceived(penaltyPaid);
            loanAudit.setOutstandingAmount(outstanding);

            womanAudit.getLoans().add(loanAudit);

            womanAudit.setTotalLoanAmount(
                    womanAudit.getTotalLoanAmount().add(disbursed));

            womanAudit.setTotalPrincipalRecovered(
                    womanAudit.getTotalPrincipalRecovered()
                            .add(principalPaid));

            womanAudit.setTotalInterestPaid(
                    womanAudit.getTotalInterestPaid()
                            .add(interestPaid));

            womanAudit.setTotalPenaltyPaid(
                    womanAudit.getTotalPenaltyPaid()
                            .add(penaltyPaid));

            womanAudit.setTotalOutstanding(
                    womanAudit.getTotalOutstanding().add(outstanding));

            groupAudit.setTotalLoanDisbursement(
                    groupAudit.getTotalLoanDisbursement().add(disbursed));

            groupAudit.setTotalPrincipalRecovered(
                    groupAudit.getTotalPrincipalRecovered()
                            .add(principalPaid));

            groupAudit.setTotalInterestReceived(
                    groupAudit.getTotalInterestReceived()
                            .add(interestPaid));

            groupAudit.setTotalPenaltyReceived(
                    groupAudit.getTotalPenaltyReceived()
                            .add(penaltyPaid));

            groupAudit.setTotalOutstanding(
                    groupAudit.getTotalOutstanding().add(outstanding));

            // Report period disbursement only for loans disbursed
            // within the selected report period.
            LocalDate loanDate = loan.getLoanGivenDate();

            if (loanDate != null
                    && !loanDate.isBefore(startDate)
                    && !loanDate.isAfter(endDate)) {
                periodDisbursement =
                        periodDisbursement.add(disbursed);
            }

            periodPrincipal = periodPrincipal.add(principalPaid);
            periodInterest = periodInterest.add(interestPaid);
            periodPenalty = periodPenalty.add(penaltyPaid);
        }

        for (AuditReportDto.GroupAudit group : groups.values()) {
            group.getWomen().sort(
                    Comparator.comparing(
                            AuditReportDto.WomanAudit::getWomanName,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                    )
            );

            for (AuditReportDto.WomanAudit woman : group.getWomen()) {
                woman.getLoans().sort(
                        Comparator.comparing(
                                AuditReportDto.LoanAudit::getLoanId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                );
            }

            line.getGroups().add(group);
        }

        line.getGroups().sort(
                Comparator.comparing(
                        AuditReportDto.GroupAudit::getGroupName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                )
        );

        // Ledger amounts remain the financial statement source.
        // These totals are derived from repayment records for the period.
        line.setPrincipalRecovered(periodPrincipal);

        /*
         * Interest and penalty are shown in the financial statement
         * from their approved ledger categories. The nested loan detail
         * separately shows interest and penalty from repayment records.
         * Do not add both sources together: that would double-count them.
         */
    }

    private BigDecimal decimal(Double value) {
        return value == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(value);
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
