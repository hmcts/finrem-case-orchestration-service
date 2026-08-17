package uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDetailsWrapper {
    @CCD(
            label = "Has the applicant applied for help with fees online?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    private YesOrNo helpWithFeesQuestion;
    @CCD(
            label = "Please enter your Help With Fees reference number",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    @JsonProperty("HWFNumber")
    private String hwfNumber;
    @CCD(
            label = "Amount to pay",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountToPay;
    @CCD(
            label = "Enter your account number",
            hint = "Example:PBA0896366",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonProperty("PBANumber")
    private String pbaNumber;
    @CCD(
            label = "Enter your reference",
            hint = "This will appear on your statement to help identify this payment",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonProperty("PBAreference")
    private String pbaReference;
    @CCD(
            label = "PBA Payment reference",
            hint = "This will appear on your statement to help identify this payment",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesAvwswjAccess.class}
    )
    @JsonProperty("PBAPaymentReference")
    private String pbaPaymentReference;
    @CCD(
            label = "Order Summary",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    private OrderSummary orderSummary;
}
