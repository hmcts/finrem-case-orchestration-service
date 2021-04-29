package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.io.grpc.netty.shaded.io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;

public class  CaseDataServiceTest extends BaseServiceTest {

    @Autowired CaseDataService caseDataService;
    public Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_shouldReturnTrue() {
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);
        assertTrue(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_appSolicitor() {
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void isRespondentSolicitorResponsibleToDraftOrder_fieldNotExist() {
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    public void shouldSuccessfullyMoveValues() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();

        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(3));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void shouldSuccessfullyMoveValuesToNewCollections() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(1));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void shouldDoNothingWithNonArraySourceValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.is("nonarrayValue"));
    }

    @Test
    public void shouldDoNothingWithNonArrayDestinationValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(caseData.get("uploadHearingOrderRO"), Matchers.is("nonarrayValue"));
        assertThat(((Collection<CaseDocument>)caseData.get(HEARING_ORDER_COLLECTION)), hasSize(1));
    }

    @Test
    public void shouldDoNothingWhenSourceIsEmptyMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));
        assertThat(caseData.get(HEARING_ORDER_COLLECTION), Matchers.nullValue());
    }

    @Test
    public void shouldOverwriteTargetCollection() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        assertThat(((Collection<CaseDocument>)caseData.get(HEARING_ORDER_COLLECTION)), hasSize(1));
        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(2));

        caseDataService.overwriteCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>)caseData.get("uploadHearingOrderRO")), hasSize(1));
    }

    @Test
    public void nullToEmptyShouldReturnEmptyWhenNull() {
        assertThat(caseDataService.nullToEmpty(null), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnEmptyWhenEmpty() {
        assertThat(caseDataService.nullToEmpty(""), is(""));
    }

    @Test
    public void nullToEmptyShouldReturnStringWhenString() {
        assertThat(caseDataService.nullToEmpty("this is my value"), is("this is my value"));
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private static String F_NAME = "f";
    private static String L_NAME = "l";

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnTrueWhenLineOneAndPostCodeArePopulated() {
        assertThat(
            caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(asList("London Road", "sw2 3rf"))), is(true)
        );
    }

    @Test
    public void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalseWhenNull() {
        assertThat(caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(null), is(false));
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
            caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(data)),
            is(false))
        );
    }

    @Test
    public void buildFullNameShouldBuildFullName() {
        assertThat(caseDataService.buildFullName(fullName("Pit", "Smith"), F_NAME, L_NAME), is("Pit Smith"));
        assertThat(caseDataService.buildFullName(fullName("", "Smith"), F_NAME, L_NAME), is("Smith"));
        assertThat(caseDataService.buildFullName(fullName("Pit Adam", "Smith"), F_NAME, L_NAME), is("Pit Adam Smith"));
        assertThat(caseDataService.buildFullName(fullName("Pit", "Smith-Johnson"), F_NAME, L_NAME), is("Pit Smith-Johnson"));
        assertThat(caseDataService.buildFullName(fullName("Pit JK", "Smith"), F_NAME, L_NAME), is("Pit JK Smith"));
        assertThat(caseDataService.buildFullName(fullName("Pit", ""), F_NAME, L_NAME), is("Pit"));
        assertThat(caseDataService.buildFullName(fullName("", ""), F_NAME, L_NAME), is(""));
        assertThat(caseDataService.buildFullName(fullName(null, ""), F_NAME, L_NAME), is(""));
        assertThat(caseDataService.buildFullName(fullName("", null), F_NAME, L_NAME), is(""));
        assertThat(caseDataService.buildFullName(fullName("     ", "    "), F_NAME, L_NAME), is(""));
        assertThat(caseDataService.buildFullName(fullName("    Pit   ", "     Smith    "), F_NAME, L_NAME), is("Pit Smith"));
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
        assertThat(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(YES_VALUE)), is(true));
    }

    @Test
    public void isApplicantRepresentedByASolicitorShouldReturnFalse() {
        asList(
            NO_VALUE,
            "",
            null,
            "this is some random string, that doesn't make any sense"
        ).forEach(value -> assertThat(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(value)), is(false)));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnTrueWhenAppSolAgreedToReceiveEmailsIsYesForConsented() {
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(caseData).build();

        assertThat(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails), is(true));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnFalseWhenAppSolAgreedToReceiveEmailsIsNoForConsented() {
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, null);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONSENTED).data(caseData).build();

        assertThat(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails), is(false));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnTrueWhenAppSolAgreedToReceiveEmailsIsYesForContested() {
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();

        assertThat(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails), is(true));
    }

    @Test
    public void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnFalseWhenAppSolAgreedToReceiveEmailsIsNoForContested() {
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, null);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).data(caseData).build();

        assertThat(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails), is(false));
    }

    @Test
    public void isConsentedRespondentRepresentedByASolicitorShouldReturnTrueWhenRepresentedSolicitorIsYes() {
        assertThat(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(YES_VALUE)), is(true));
    }

    @Test
    public void isContestedRespondentRepresentedByASolicitorShouldReturnTrueWhenRepresentedSolicitorIsYes() {
        assertThat(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(YES_VALUE)), is(true));
    }

    @Test
    public void isConsentedRespondentRepresentedByASolicitorShouldReturnFalseWhenRepresentedSolicitorIsNo() {
        assertThat(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(NO_VALUE)), is(false));
    }

    @Test
    public void isContestedRespondentRepresentedByASolicitorShouldReturnFalseWhenRepresentedSolicitorIsNo() {
        assertThat(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(NO_VALUE)), is(false));
    }

    @Test
    public void isConsentedInContestedCaseShouldReturnTrueWhenIsContestedCaseAndConsentD81QuestionIsPopulated() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-consented-in-contested.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedInContestedCase(caseDetails), is(true));
    }

    @Test
    public void isConsentedInContestedCaseShouldReturnFalseWhenIsContestedCaseAndConsentD81QuestionIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-contested.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedInContestedCase(caseDetails), is(false));
    }

    @Test
    public void isConsentedInContestedCaseShouldReturnFalseWhenIsConsentedCaseAndConsentD81QuestionIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-consented.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedInContestedCase(caseDetails), is(false));
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
            caseDataService.isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(value)), is(true))
        );
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenEmptyMap() {
        assertThat(caseDataService.isNotEmpty(APPLICANT_REPRESENTED, ImmutableMap.of()), is(false));
    }

    @Test
    public void isNotEmptyShouldReturnFalseWhenFieldIsEmpty() {
        assertThat(caseDataService.isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(EMPTY_STRING)), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void isNotEmptyShouldThrowNullPointerException() {
        caseDataService.isNotEmpty(APPLICANT_REPRESENTED, null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseForDefaultEmptyObject() {
        RespondToOrderData data = new RespondToOrderData();
        data.setRespondToOrder(new RespondToOrder());

        assertThat(caseDataService.isAmendedConsentOrderType(data), is(false));
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnFalseWhenDocumentTypeIsNotAmendedConsentOrder() {
        assertThat(caseDataService.isAmendedConsentOrderType(getRespondToOrderData("ble ble ble")), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void isAmendedConsentOrderTypeShouldThrowNullPointerException() {
        caseDataService.isAmendedConsentOrderType(null);
    }

    @Test
    public void isAmendedConsentOrderTypeShouldReturnTrueWhenDocumentTypeIsAmendedConsentOrder() {
        assertThat(caseDataService.isAmendedConsentOrderType(getRespondToOrderData(AMENDED_CONSENT_ORDER)), is(true));
    }

    @Test
    public void isApplicantSolicitorResponsibleToDraftOrderTrueWhenTheyHaveBeenNominated() {
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);

        assertThat(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData), is(true));
    }

    @Test
    public void isApplicantSolicitorResponsibleToDraftOrderFalseWhenRespondentSolicitorIsNominated() {
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);

        assertThat(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData), is(false));
    }

    @Test
    public void isApplicantSolicitorResponsibleToDraftOrderFalseWhenNull() {
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, null);

        assertThat(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData), is(false));
    }

    @Test
    public void isConsentedApplicationShouldReturnTrueWheCaseTypeIsSetToConsentedCaseType() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedApplication(caseDetails), is(true));
    }

    @Test
    public void isConsentedApplicationShouldReturnFalseWhenCaseTypeIsSetToContested() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedApplication(caseDetails), is(false));
    }

    @Test
    public void isConsentedApplicationShouldReturnFalseWhenCaseTypeIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isConsentedApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedApplicationShouldReturnTrueWheCaseTypeIsSetToContested() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedApplication(caseDetails), is(true));
    }

    @Test
    public void isContestedApplicationShouldReturnFalseWheCaseTypeIsSetToConsented() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedApplicationShouldReturnFalseWheCaseTypeIsSetToNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedPaperApplicationShouldReturnTrueWhenCaseTypeIsSetToContestedAndIsPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedPaperApplication(caseDetails), is(true));
    }

    @Test
    public void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToConsentedAndIsPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/bulkprint/bulk-print-paper-application.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedPaperApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToContestedAndNotPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedPaperApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToConsented() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedPaperApplication(caseDetails), is(false));
    }

    @Test
    public void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertThat(caseDataService.isContestedPaperApplication(caseDetails), is(false));
    }


    @Test
    public void isApplicantAddressConfidentialTrueWhenApplicantAddressIsMarkedAsConfidential() {
        caseData.put(APPLICANT_CONFIDENTIAL_ADDRESS, "Yes");

        assertThat(caseDataService.isApplicantAddressConfidential(caseData), is(true));
    }

    @Test
    public void isApplicantAddressConfidentialFalseWhenApplicantAddressIsNotMarkedAsConfidential() {
        caseData.put(APPLICANT_CONFIDENTIAL_ADDRESS, "No");

        assertThat(caseDataService.isApplicantAddressConfidential(caseData), is(false));
    }

    @Test
    public void isApplicantAddressConfidentialFalseWhenApplicantAddressConfidentialFieldIsNotPresent() {
        assertThat(caseDataService.isApplicantAddressConfidential(caseData), is(false));
    }

    @Test
    public void isRespondentAddressConfidentialTrueWhenRespondentAddressIsMarkedAsConfidential() {
        caseData.put(RESPONDENT_CONFIDENTIAL_ADDRESS, "Yes");

        assertThat(caseDataService.isRespondentAddressConfidential(caseData), is(true));
    }

    @Test
    public void isRespondentAddressConfidentialFalseWhenRespondentAddressIsNotMarkedAsConfidential() {
        caseData.put(RESPONDENT_CONFIDENTIAL_ADDRESS, "No");

        assertThat(caseDataService.isRespondentAddressConfidential(caseData), is(false));
    }

    @Test
    public void isRespondentAddressConfidentialFalseWhenRespondentAddressConfidentialFieldIsNotPresent() {
        assertThat(caseDataService.isRespondentAddressConfidential(caseData), is(false));
    }

    @Test
    public void isRespondentSolicitorAgreeToReceiveEmails_whenYes() {
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        assertThat(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseData), is(true));
    }

    @Test
    public void isRespondentSolicitorAgreeToReceiveEmails_whenNo() {
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, NO_VALUE);

        assertThat(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseData), is(false));
    }

    @Test
    public void isRespondentSolicitorAgreeToReceiveEmails_whenNullOrFieldNotPresent() {
        assertThat(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseData), is(false));
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

    private static Map<String, Object> createCaseDataRespRepresentedConsented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(CONSENTED_RESPONDENT_REPRESENTED, value);

        return data;
    }

    private static Map<String, Object> createCaseDataRespRepresentedContested(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(CONTESTED_RESPONDENT_REPRESENTED, value);

        return data;
    }
}
