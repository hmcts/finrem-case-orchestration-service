package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.io.grpc.netty.shaded.io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.addressLineOneAndPostCodeAreBothNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isAmendedConsentOrderType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isRespondentRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

public class CommonFunctionTest {

    private static String F_NAME = "f";
    private static String L_NAME = "l";

    @Test
    public void nullToEmptyShouldReturnEmptyWhenNull() {
        assertThat(nullToEmpty(null), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnEmptyWhenEmpty() {
        assertThat(nullToEmpty(""), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnStringWhenString() {
        assertThat(nullToEmpty("this is my value"), is("this is my value"));
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnTrueWhenLineOneAndPostCodeArePopulated() {
        assertThat(
            addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(asList("London Road", "sw2 3rf"))), is(true)
        );
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalseWhenNull() {
        assertThat(addressLineOneAndPostCodeAreBothNotEmpty(null), is(false));
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalse() {
        asList(
            asList("", "sw2 3rf"),
            asList("", ""),
            asList("London Road", ""),
            asList("London Road", null),
            asList(null, null),
            asList(null, "Sw8 7ty")
        ).forEach(data -> assertThat(
            addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(data)),
            is(false))
        );
    }

    @Test
    public void buildFullNameShouldBuildFullName() {
        assertThat(buildFullName(fullName("Pit", "Smith"), F_NAME, L_NAME), is("Pit Smith"));
        assertThat(buildFullName(fullName("", "Smith"), F_NAME, L_NAME), is("Smith"));
        assertThat(buildFullName(fullName("Pit Adam", "Smith"), F_NAME, L_NAME), is("Pit Adam Smith"));
        assertThat(buildFullName(fullName("Pit", "Smith-Johnson"), F_NAME, L_NAME), is("Pit Smith-Johnson"));
        assertThat(buildFullName(fullName("Pit JK", "Smith"), F_NAME, L_NAME), is("Pit JK Smith"));
        assertThat(buildFullName(fullName("Pit", ""), F_NAME, L_NAME), is("Pit"));
        assertThat(buildFullName(fullName("", ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName(null, ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("", null), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("     ", "    "), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("    Pit   ", "     Smith    "), F_NAME, L_NAME), is("Pit Smith"));
    }

    private static Map<String, String> createAddressObject(List<?> data) {
        Map<String, String> address = new HashMap<>();

        address.put("AddressLine1", (String) data.get(0));
        address.put("PostCode", (String) data.get(1));

        return address;
    }

    private static Map<String, Object> fullName(String firstName, String lastName) {
        Map<String, Object> fullNameMap = new HashMap<>();
        fullNameMap.put(F_NAME, firstName);
        fullNameMap.put(L_NAME, lastName);

        return fullNameMap;
    }

    @Test
    public void isApplicantRepresentedByASolicitorShouldReturnTrueWhenApplicantRepresentedIsYes() {
        assertThat(isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(YES_VALUE)), is(true));
    }

    @Test
    public void isApplicantRepresentedByASolicitorShouldReturnFalse() {
        asList(
            NO_VALUE,
            "",
            null,
            "this is some random string, that doesn't make any sense"
        ).forEach(value -> assertThat(isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(value)), is(false)));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnTrueWhenApplicantRepresentedIsYes() {
        Map<String, Object> data = new HashMap<>();
        data.put(SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);

        assertThat(isApplicantSolicitorAgreeToReceiveEmails(data), is(true));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnFalseWhenApplicantRepresentedIsNo() {
        Map<String, Object> data = new HashMap<>();
        data.put(SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, null);

        assertThat(isApplicantSolicitorAgreeToReceiveEmails(data), is(false));
    }

    @Test
    public void isRespondentRepresentedByASolicitorShouldReturnTrueWhenRepresentedSolicitorIsNotEmpty() {
        asList("John Wayne", "     ", "234@#$@$@#REWF#@REWFR@#")
            .forEach(value -> assertThat(
                isRespondentRepresentedByASolicitor(createCaseDataRespRepresented(value)), is(true)));
    }

    @Test
    public void isRespondentRepresentedByASolicitorShouldReturnFalse() {
        asList("", null)
            .forEach(value -> assertThat(
                isRespondentRepresentedByASolicitor(createCaseDataRespRepresented(value)), is(false)));
    }

    @Test
    public void isNotEmptyShouldReturnTrueWhenPopulated() {
        asList(
            YES_VALUE,
            "    ",
            "any value makes it not empty",
            "1234",
            "@#$R@#F@$T"
        ).forEach(value -> assertThat(
            isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(value)), is(true))
        );
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenEmptyMap() {
        assertThat(isNotEmpty(APPLICANT_REPRESENTED, ImmutableMap.of()), is(false));
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenFieldIsEmpty() {
        assertThat(isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(EMPTY_STRING)), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void isNotEmptyShouldThrowNullPointerException() {
        isNotEmpty(APPLICANT_REPRESENTED, null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseForDefaultEmptyObject() {
        RespondToOrderData data = new RespondToOrderData();
        data.setRespondToOrder(new RespondToOrder());

        assertThat(isAmendedConsentOrderType(data), is(false));
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseWhenDocumentTypeIsNotAmendedConsentOrder() {
        assertThat(isAmendedConsentOrderType(getRespondToOrderData("ble ble ble")), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void isAmendedConsentOrderTypeShouldThrowNullPointerException() {
        isAmendedConsentOrderType(null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnTrueWhenDocumentTypeIsAmendedConsentOrder() {
        assertThat(isAmendedConsentOrderType(getRespondToOrderData(AMENDED_CONSENT_ORDER)), is(true));
    }

    private static RespondToOrderData getRespondToOrderData(String s) {
        RespondToOrderData data = new RespondToOrderData();
        RespondToOrder respondToOrder = new RespondToOrder();
        respondToOrder.setDocumentType(s);
        data.setRespondToOrder(respondToOrder);

        return data;
    }

    private static Map<String, Object> createCaseDataApplRepresented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(APPLICANT_REPRESENTED, value);

        return data;
    }

    private static Map<String, Object> createCaseDataRespRepresented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(RESP_SOLICITOR_NAME, value);

        return data;
    }
}