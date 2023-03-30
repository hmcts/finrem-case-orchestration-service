package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequestWithSiteID;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentStatusHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation.OrganisationEntityResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation.PBAAccount;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation.PBAOrganisationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation.SuperUserResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;


public class SetUpUtils {

    public static final int STATUS_CODE = INTERNAL_SERVER_ERROR.value();
    public static final String ACCOUNT_NUMBER = "Account12345";
    public static final String PAYMENT_SUCCESS_STATUS = "Success";
    public static final String PAYMENT_FAILED_STATUS = "Failed";
    public static final String PAYMENT_REF = "RC-12345-2323-0712321320-23221";

    public static final String FEE_CODE = "CODE";
    public static final String FEE_DESC = "Description";
    public static final BigDecimal CONSENTED_FEE_AMOUNT = BigDecimal.TEN;
    public static final BigDecimal CONTESTED_FEE_AMOUNT = BigDecimal.valueOf(255);
    public static final String FEE_VERSION = "v1";

    public static final String PBA_NUMBER = "PBA0222";
    public static final String CONSENTED_CASE_TYPE = "FinancialRemedyMVP2";
    public static final String CONTESTED_CASE_TYPE = "FinancialRemedyContested";

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static FeignException feignError() {
        Response response = Response.builder().status(STATUS_CODE).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }

    public static InvalidTokenException invalidTokenException() {
        return new InvalidTokenException("Invalid User Token");
    }

    public static String pbaAccount() {
        PBAAccount pbaAccount = PBAAccount.builder().accountList(ImmutableList.of(PBA_NUMBER)).build();
        return objectToJson(pbaAccount);
    }

    public static String pbaOrganisationResponse() {
        SuperUserResponse superUserResponse = SuperUserResponse.builder()
            .email("test@email.com")
            .firstName("TestFirstName")
            .lastName("TestLastName")
            .build();
        OrganisationEntityResponse organisationEntityResponse = OrganisationEntityResponse.builder()
            .companyNumber("1110111")
            .name("Test org")
            .organisationIdentifier("111")
            .paymentAccount(Arrays.asList("PBA0222", "PBA0333"))
            .sraId("s001")
            .sraRegulated(true)
            .status("ACTIVE")
            .superUser(superUserResponse)
            .companyUrl("http://testorg2.co.uk")
            .build();
        PBAOrganisationResponse pbaOrganisationResponse = PBAOrganisationResponse
            .builder()
            .organisationEntityResponse(organisationEntityResponse)
            .build();
        return objectToJson(pbaOrganisationResponse);
    }

    public static FeeResponse feeResponse(ApplicationType applicationType) {
        FeeResponse feeResponse = new FeeResponse();
        feeResponse.setCode(FEE_CODE);
        feeResponse.setDescription(FEE_DESC);
        feeResponse.setFeeAmount(applicationType == CONSENTED ? CONSENTED_FEE_AMOUNT : CONTESTED_FEE_AMOUNT);
        feeResponse.setVersion(FEE_VERSION);
        return feeResponse;
    }

    public static String feeResponseString(ApplicationType applicationType) {
        return objectToJson(feeResponse(applicationType));
    }

    public static PaymentResponse paymentResponse() {
        return PaymentResponse.builder()
            .status(PAYMENT_SUCCESS_STATUS)
            .reference(PAYMENT_REF)
            .statusHistories(ImmutableList.of()).build();
    }

    public static PaymentResponse paymentDuplicateError() {
        return PaymentResponse.builder()
            .error(BAD_REQUEST.toString())
            .message("duplicate payment")
            .build();
    }

    public static PaymentResponse paymentResponseClient422Error() {
        return PaymentResponse.builder()
            .error(UNPROCESSABLE_ENTITY.toString())
            .message("Invalid or missing attribute")
            .build();
    }

    public static PaymentResponse paymentResponseClient404Error() {
        return PaymentResponse.builder()
            .error(NOT_FOUND.toString())
            .message("Account information could not be found")
            .build();
    }

    public static PaymentResponse paymentResponseClient401Error() {
        return PaymentResponse.builder()
            .status(PAYMENT_FAILED_STATUS)
            .reference(PAYMENT_REF)
            .statusHistories(ImmutableList.of(paymentStatusHistory())).build();
    }

    public static String paymentResponseErrorToString() {
        return objectToJson(paymentResponseClient401Error());
    }

    public static String paymentResponseToString() {
        return objectToJson(paymentResponse());
    }

    public static String paymentRequestStringContent() {
        return objectToJson(paymentRequest());
    }

    public static String paymentRequestStringContentWithCaseType() {
        return objectToJson(paymentRequestWithCaseType());
    }

    public static PaymentRequestWithSiteID paymentRequest() {
        BigDecimal amountToPay = new BigDecimal("12");
        FeeRequest fee = FeeRequest.builder()
            .calculatedAmount(amountToPay)
            .code("FEE0640")
            .version("v1")
            .build();
        return PaymentRequestWithSiteID.builder()
            .accountNumber(ACCOUNT_NUMBER)
            .siteId(ACCOUNT_NUMBER)
            .customerReference("SOL1")
            .ccdCaseNumber("ED12345")
            .organisationName("ORG SOL1")
            .amount(amountToPay)
            .feesList(Collections.singletonList(fee))
            .build();
    }

    private static PaymentStatusHistory paymentStatusHistory() {
        return PaymentStatusHistory.builder().errorCode("ERR").errorMessage("error").status("S").build();
    }

    private static String objectToJson(Object object) {
        try {
            String value = objectMapper.writeValueAsString(object);
            System.out.println(" value = [" + value + "]");
            return value;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static PaymentRequest paymentRequestWithCaseType() {
        BigDecimal amountToPay = new BigDecimal("12");
        FeeRequest fee = FeeRequest.builder()
            .calculatedAmount(amountToPay)
            .code("FEE0640")
            .version("v1")
            .build();
        return PaymentRequest.builder()
            .accountNumber(ACCOUNT_NUMBER)
            .caseType(CONSENTED_CASE_TYPE)
            .ccdCaseNumber("ED12345")
            .customerReference("SOL1")
            .organisationName("ORG SOL1")
            .amount(amountToPay)
            .feesList(Collections.singletonList(fee))
            .build();
    }
}
