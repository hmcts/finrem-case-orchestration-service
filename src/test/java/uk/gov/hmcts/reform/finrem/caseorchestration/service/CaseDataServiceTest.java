package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMEND_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;

@ExtendWith(MockitoExtension.class)
class CaseDataServiceTest {

    @InjectMocks
    CaseDataService caseDataService;
    
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

        finremCaseDetailsMapper =  new FinremCaseDetailsMapper(objectMapper);
    }

    @Test
    void isRespondentSolicitorResponsibleToDraftOrder_shouldReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);
        assertTrue(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    void isRespondentSolicitorResponsibleToDraftOrder_appSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    void isRespondentSolicitorResponsibleToDraftOrder_fieldNotExist() {
        Map<String, Object> caseData = new HashMap<>();
        assertFalse(caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData));
    }

    @Test
    void shouldSuccessfullyMoveValues() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();

        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO")).hasSize(3);
        assertNull(caseData.get(HEARING_ORDER_COLLECTION));
    }

    @Test
    void shouldSuccessfullyMoveValuesToNewCollections() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO")).hasSize(1);
        assertNull(caseData.get(HEARING_ORDER_COLLECTION));
    }

    @Test
    void shouldDoNothingWithNonArraySourceValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO")).hasSize(2);
        assertThat(caseData.get(HEARING_ORDER_COLLECTION)).isEqualTo("nonarrayValue");
    }

    @Test
    void shouldDoNothingWithNonArrayDestinationValueMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put("uploadHearingOrderRO", "nonarrayValue");
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertEquals("nonarrayValue", caseData.get("uploadHearingOrderRO"));
        assertThat((Collection<CaseDocument>) caseData.get(HEARING_ORDER_COLLECTION)).hasSize(1);
    }

    @Test
    void shouldDoNothingWhenSourceIsEmptyMove() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        caseData.put(HEARING_ORDER_COLLECTION, null);
        caseDataService.moveCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO")).hasSize(2);
        assertNull(caseData.get(HEARING_ORDER_COLLECTION));
    }

    @Test
    void shouldOverwriteTargetCollection() {
        Map<String, Object> caseData = TestSetUpUtils.caseDataWithUploadHearingOrder();
        assertThat((Collection<CaseDocument>) caseData.get(HEARING_ORDER_COLLECTION)).hasSize(1);
        assertThat(((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO"))).hasSize(2);

        caseDataService.overwriteCollection(caseData, HEARING_ORDER_COLLECTION, "uploadHearingOrderRO");

        assertThat(((Collection<CaseDocument>) caseData.get("uploadHearingOrderRO"))).hasSize(1);
    }

    @Test
    void nullToEmptyShouldReturnEmptyWhenNull() {
        assertEquals("", caseDataService.nullToEmpty(null));
    }

    @Test
    void nullToEmptyShouldReturnEmptyWhenEmpty() {
        assertEquals("", caseDataService.nullToEmpty(""));
    }

    @Test
    void nullToEmptyShouldReturnStringWhenString() {
        assertEquals("this is my value", caseDataService.nullToEmpty("this is my value"));
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String F_NAME = "f";
    private static final String L_NAME = "l";

    @Test
    void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnTrueWhenLineOneAndPostCodeArePopulated() {
        assertTrue(caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(asList("London Road", "sw2 3rf"))));
    }

    @Test
    void addressLineOneAndPostCodeAreBothEmptyShouldReturnFalse() {
        assertFalse(caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(null));
    }

    @Test
    void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalseWhenNull() {
        assertFalse(caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(null));
    }

    @Test
    void addressLineOneAndPostCodeAreBothNotEmptyShouldReturnFalse() {
        asList(
            asList("", "sw2 3rf"),
            asList("", ""),
            asList("London Road", ""),
            asList("London Road", null),
            asList(null, null),
            asList(null, "Sw8 7ty")
        ).forEach(data -> assertFalse(caseDataService.addressLineOneAndPostCodeAreBothNotEmpty(createAddressObject(data))));
    }

    @Test
    void buildFullNameShouldBuildFullName() {
        assertEquals("Pit Smith", caseDataService.buildFullName(fullName("Pit", "Smith"), F_NAME, L_NAME));
        assertEquals("Smith", caseDataService.buildFullName(fullName("", "Smith"), F_NAME, L_NAME));
        assertEquals("Pit Adam Smith", caseDataService.buildFullName(fullName("Pit Adam", "Smith"), F_NAME, L_NAME));
        assertEquals("Pit Smith-Johnson", caseDataService.buildFullName(fullName("Pit", "Smith-Johnson"), F_NAME, L_NAME));
        assertEquals("Pit JK Smith", caseDataService.buildFullName(fullName("Pit JK", "Smith"), F_NAME, L_NAME));
        assertEquals("Pit", caseDataService.buildFullName(fullName("Pit", ""), F_NAME, L_NAME));
        assertEquals("", caseDataService.buildFullName(fullName("", ""), F_NAME, L_NAME));
        assertEquals("", caseDataService.buildFullName(fullName(null, ""), F_NAME, L_NAME));
        assertEquals("", caseDataService.buildFullName(fullName("", null), F_NAME, L_NAME));
        assertEquals("", caseDataService.buildFullName(fullName("     ", "    "), F_NAME, L_NAME));
        assertEquals("Pit Smith", caseDataService.buildFullName(fullName("    Pit   ", "     Smith    "), F_NAME, L_NAME));
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
    void shouldPopulateFinancialRemediesCourtDetails() {
        Map<String, Object> data = new HashMap<>();
        data.put(REGION, LONDON);
        data.put(LONDON_FRC_LIST, LONDON_CFC);
        data.put(CFC_COURTLIST, "FR_s_CFCList_11");
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        caseDataService.setFinancialRemediesCourtDetails(caseDetails);

        assertThat(caseDetails.getData()).containsEntry(CONSENT_ORDER_FRC_NAME,
            "East London Family Court");
        assertThat(caseDetails.getData()).containsEntry(CONSENT_ORDER_FRC_ADDRESS,
            "East London Family Court, 6th and 7th Floor, 11 Westferry Circus, London, E14 4HD");
        assertThat(caseDetails.getData()).containsEntry(CONSENT_ORDER_FRC_EMAIL, "FRCLondon@justice.gov.uk");
        assertThat(caseDetails.getData()).containsEntry(CONSENT_ORDER_FRC_PHONE, "0300 123 5577");
    }

    @Test
    void isApplicantRepresentedByASolicitorShouldReturnTrueWhenApplicantRepresentedIsYes() {
        assertTrue(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(YES_VALUE)));
        assertTrue(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(finremCaseDetailsMapper, YES_VALUE)));
    }

    @Test
    void isApplicantRepresentedByASolicitorShouldReturnFalse() {
        asList(
            NO_VALUE,
            "",
            null,
            "this is some random string, that doesn't make any sense"
        ).forEach(value ->
            assertFalse(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(value)))
        );
    }

    @Test
    void isApplicantRepresentedByASolicitorShouldReturnFalseAfterRefactoring() {
        asList(
            NO_VALUE,
            null
        ).forEach(value ->
            assertFalse(caseDataService.isApplicantRepresentedByASolicitor(createCaseDataApplRepresented(finremCaseDetailsMapper, value)))
        );
    }

    @Test
    void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnTrueWhenAppSolAgreedToReceiveEmailsIsYesForConsented() {
        Map<String, Object> data = new HashMap<>();
        data.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(data).build();

        assertTrue(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails));
        assertTrue(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
    }

    @Test
    void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnFalseWhenAppSolAgreedToReceiveEmailsIsNoForConsented() {
        Map<String, Object> data = new HashMap<>();
        data.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, null);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(data).build();

        assertFalse(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
    }

    @Test
    void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnTrueWhenAppSolAgreedToReceiveEmailsIsYesForContested() {
        Map<String, Object> data = new HashMap<>();
        data.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).data(data).build();

        assertTrue(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails));
        assertTrue(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
    }

    @Test
    void isApplicantSolicitorAgreeToReceiveEmailsShouldReturnFalseWhenAppSolAgreedToReceiveEmailsIsNoForContested() {
        Map<String, Object> data = new HashMap<>();
        data.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, null);
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).data(data).build();

        assertFalse(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails));
    }

    @Test
    void isConsentedRespondentRepresentedByASolicitorShouldReturnTrueWhenRepresentedSolicitorIsYes() {
        assertTrue(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(YES_VALUE)));
        assertTrue(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(finremCaseDetailsMapper,
            YES_VALUE)));
    }

    @Test
    void isContestedRespondentRepresentedByASolicitorShouldReturnTrueWhenRepresentedSolicitorIsYes() {
        assertTrue(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(YES_VALUE)));
        assertTrue(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(finremCaseDetailsMapper,
            YES_VALUE)));
    }

    @Test
    void isConsentedRespondentRepresentedByASolicitorShouldReturnFalseWhenRepresentedSolicitorIsNo() {
        assertFalse(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(NO_VALUE)));
        assertFalse(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedConsented(finremCaseDetailsMapper,
            NO_VALUE)));
    }

    @Test
    void isContestedRespondentRepresentedByASolicitorShouldReturnFalseWhenRepresentedSolicitorIsNo() {
        assertFalse(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(NO_VALUE)));
        assertFalse(caseDataService.isRespondentRepresentedByASolicitor(createCaseDataRespRepresentedContested(finremCaseDetailsMapper,
            NO_VALUE)));
    }

    @Test
    void isConsentedInContestedCaseShouldReturnTrueWhenIsContestedCaseAndConsentD81QuestionIsPopulated() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-consented-in-contested.json"), CallbackRequest.class).getCaseDetails();

        assertTrue(caseDataService.isConsentedInContestedCase(caseDetails));
    }

    @Test
    void isConsentedInContestedCaseShouldReturnFalseWhenIsContestedCaseAndConsentD81QuestionIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-contested.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isConsentedInContestedCase(caseDetails));
    }

    @Test
    void isConsentedInContestedCaseShouldReturnFalseWhenIsConsentedCaseAndConsentD81QuestionIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/general-order-consented.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isConsentedInContestedCase(caseDetails));
    }

    @Test
    void isNotEmptyShouldReturnTrueWhenPopulated() {
        asList(
            YES_VALUE,
            "    ",
            "any value makes it not empty",
            "1234",
            "@#$R@#F@$T"
        ).forEach(value -> assertTrue(caseDataService.isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented(value))));
    }

    @Test
    void isNotEmptyShouldReturnFalseWhenEmptyMap() {
        assertFalse(caseDataService.isNotEmpty(APPLICANT_REPRESENTED, Map.of()));
    }

    @Test
    void isNotEmptyShouldReturnFalseWhenFieldIsEmpty() {
        assertFalse(caseDataService.isNotEmpty(APPLICANT_REPRESENTED, createCaseDataApplRepresented("")));
    }

    @Test
    void isNotEmptyShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> caseDataService.isNotEmpty(APPLICANT_REPRESENTED, null));
    }

    @Test
    void isAmendedConsentOrderTypeShouldReturnFalseForDefaultEmptyObject() {
        RespondToOrderData data = new RespondToOrderData();
        data.setRespondToOrder(new RespondToOrder());

        assertFalse(caseDataService.isAmendedConsentOrderType(data));
    }

    @Test
    void isAmendedConsentOrderTypeShouldReturnFalseWhenDocumentTypeIsNotAmendedConsentOrder() {
        assertFalse(caseDataService.isAmendedConsentOrderType(getRespondToOrderData("ble ble ble")));
    }

    @Test
    void isAmendedConsentOrderTypeShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> caseDataService.isAmendedConsentOrderType(null));
    }

    @Test
    void isAmendedConsentOrderTypeShouldReturnTrueWhenDocumentTypeIsAmendedConsentOrder() {
        assertTrue(caseDataService.isAmendedConsentOrderType(getRespondToOrderData(AMEND_CONSENT_ORDER)));
    }

    @Test
    void isApplicantSolicitorResponsibleToDraftOrderTrueWhenTheyHaveBeenNominated() {
        Map<String, Object> data = new HashMap<>();
        data.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, APPLICANT_SOLICITOR);

        assertTrue(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(data));
    }

    @Test
    void isApplicantSolicitorResponsibleToDraftOrderFalseWhenRespondentSolicitorIsNominated() {
        Map<String, Object> data = new HashMap<>();
        data.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, RESPONDENT_SOLICITOR);

        assertFalse(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(data));
    }

    @Test
    void isApplicantSolicitorResponsibleToDraftOrderFalseWhenNull() {
        Map<String, Object> data = new HashMap<>();
        data.put(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER, null);

        assertFalse(caseDataService.isApplicantSolicitorResponsibleToDraftOrder(data));
    }

    @Test
    void isConsentedApplicationShouldReturnTrueWheCaseTypeIsSetToConsentedCaseType() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertTrue(caseDataService.isConsentedApplication(caseDetails));
    }

    @Test
    void isConsentedApplicationShouldReturnFalseWhenCaseTypeIsSetToContested() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isConsentedApplication(caseDetails));
    }

    @Test
    void isConsentedApplicationShouldReturnFalseWhenCaseTypeIsNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isConsentedApplication(caseDetails));
    }

    @Test
    void isContestedApplicationShouldReturnTrueWheCaseTypeIsSetToContested() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertTrue(caseDataService.isContestedApplication(caseDetails));
    }

    @Test
    void isContestedApplicationShouldReturnFalseWheCaseTypeIsSetToConsented() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedApplication(caseDetails));
    }

    @Test
    void isContestedApplicationShouldReturnFalseWheCaseTypeIsSetToNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedApplication(caseDetails));
    }

    @Test
    void isContestedPaperApplicationShouldReturnTrueWhenCaseTypeIsSetToContestedAndIsPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json"), CallbackRequest.class).getCaseDetails();

        assertTrue(caseDataService.isContestedPaperApplication(caseDetails));
    }

    @Test
    void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToConsentedAndIsPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/bulkprint/bulk-print-paper-application.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedPaperApplication(caseDetails));
    }

    @Test
    void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToContestedAndNotPaperCase() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/contested/contested-hwf-without-solicitor-consent.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedPaperApplication(caseDetails));
    }

    @Test
    void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToConsented() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/valid-latest-consent-order.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedPaperApplication(caseDetails));
    }

    @Test
    void isContestedPaperApplicationShouldReturnFalseWhenCaseTypeIsSetToNull() throws IOException {
        CaseDetails caseDetails = mapper.readValue(getClass().getResourceAsStream(
            "/fixtures/empty-casedata.json"), CallbackRequest.class).getCaseDetails();

        assertFalse(caseDataService.isContestedPaperApplication(caseDetails));
    }

    @Test
    void isApplicantAddressConfidentialTrueWhenApplicantAddressIsMarkedAsConfidential() {
        Map<String, Object> data = new HashMap<>();
        data.put(APPLICANT_CONFIDENTIAL_ADDRESS, "Yes");

        assertTrue(caseDataService.isApplicantAddressConfidential(data));
    }

    @Test
    void isApplicantAddressConfidentialFalseWhenApplicantAddressIsNotMarkedAsConfidential() {
        Map<String, Object> data = new HashMap<>();
        data.put(APPLICANT_CONFIDENTIAL_ADDRESS, "No");

        assertFalse(caseDataService.isApplicantAddressConfidential(data));
    }

    @Test
    void isApplicantAddressConfidentialFalseWhenApplicantAddressConfidentialFieldIsNotPresent() {
        Map<String, Object> data = new HashMap<>();

        assertFalse(caseDataService.isApplicantAddressConfidential(data));
    }

    @Test
    void isRespondentAddressConfidentialTrueWhenRespondentAddressIsMarkedAsConfidential() {
        Map<String, Object> data = new HashMap<>();
        data.put(RESPONDENT_CONFIDENTIAL_ADDRESS, "Yes");

        assertTrue(caseDataService.isRespondentAddressConfidential(data));
    }

    @Test
    void isRespondentAddressConfidentialFalseWhenRespondentAddressIsNotMarkedAsConfidential() {
        Map<String, Object> data = new HashMap<>();
        data.put(RESPONDENT_CONFIDENTIAL_ADDRESS, "No");

        assertFalse(caseDataService.isRespondentAddressConfidential(data));
    }

    @Test
    void isRespondentAddressConfidentialFalseWhenRespondentAddressConfidentialFieldIsNotPresent() {
        Map<String, Object> data = new HashMap<>();

        assertFalse(caseDataService.isRespondentAddressConfidential(data));
    }

    @Test
    void shouldBuildFullIntervener1Name() {
        Map<String, Object> data = new HashMap<>();
        data.put(INTERVENER1_FIRST_MIDDLE_NAME, "Sarah John");
        data.put(INTERVENER1_LAST_NAME, "Smith");
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        assertEquals("Sarah John Smith", caseDataService.buildFullIntervener1Name(caseDetails));
    }

    @Test
    void isContestedFinremCaseDetailsApplication() {
        FinremCaseDetails finremCaseDetails
            = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L).build();
        assertTrue(caseDataService.isContestedApplication(finremCaseDetails));
    }

    @Test
    void shouldBuildFullIntervener2Name() {
        Map<String, Object> data = new HashMap<>();
        data.put(INTERVENER2_FIRST_MIDDLE_NAME, "John Taylor");
        data.put(INTERVENER2_LAST_NAME, "Fitzgerald");
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        assertEquals(
            "John Taylor Fitzgerald", caseDataService.buildFullIntervener2Name(caseDetails)
        );
    }

    @Test
    void shouldBuildFullIntervener3Name() {
        Map<String, Object> data = new HashMap<>();
        data.put(INTERVENER3_FIRST_MIDDLE_NAME, "Sam Tyler");
        data.put(INTERVENER3_LAST_NAME, "Peters");
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        assertEquals(
            "Sam Tyler Peters", caseDataService.buildFullIntervener3Name(caseDetails)
        );
    }

    @Test
    void shouldBuildFullIntervener4Name() {
        Map<String, Object> data = new HashMap<>();
        data.put(INTERVENER4_FIRST_MIDDLE_NAME, "Yousef Luke");
        data.put(INTERVENER4_LAST_NAME, "Brown");
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        assertEquals(
            "Yousef Luke Brown", caseDataService.buildFullIntervener4Name(caseDetails)
        );
    }

    @Test
    void testHasConsentOrderIsTrue() {
        ConsentOrderWrapper consentOrderWrapper = new ConsentOrderWrapper();
        consentOrderWrapper.setConsentD81Question(YesOrNo.YES);
        FinremCaseData caseData = FinremCaseData.builder()
            .consentOrderWrapper(consentOrderWrapper)
            .build();

        assertTrue(caseDataService.hasConsentOrder(caseData));
    }

    @Test
    void testHasConsentOrderIsFalse() {
        assertFalse(caseDataService.hasConsentOrder(new FinremCaseData()));
    }

    @Test
    void shouldReturnTrueIfConsentInContestedEvent() {
        assertTrue(caseDataService.isConsentInContestedGeneralOrderEvent(GENERAL_ORDER_CONSENT_IN_CONTESTED));
    }

    @Test
    void givenRepresentedFlagOnApplicant_whenIsLitigantRepresented_thenTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, "Yes");
        CaseDetails finremCaseDetails
            = CaseDetails.builder().data(caseData).caseTypeId(CaseType.CONTESTED.getCcdType()).id(123L).build();

        assertTrue(caseDataService.isLitigantRepresented(finremCaseDetails, true));
    }

    @Test
    void givenContestedRepresentedFlagOnRespondent_whenIsLitigantRepresented_thenTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, "No");
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, "Yes");
        CaseDetails finremCaseDetails
            = CaseDetails.builder().data(caseData).caseTypeId(CaseType.CONTESTED.getCcdType()).id(123L).build();

        assertTrue(caseDataService.isLitigantRepresented(finremCaseDetails, false));
    }

    @Test
    void givenConsentedRepresentedFlagOnRespondent_whenIsLitigantRepresented_thenTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, "No");
        caseData.put(CONSENTED_RESPONDENT_REPRESENTED, "Yes");
        CaseDetails finremCaseDetails
            = CaseDetails.builder().data(caseData).caseTypeId(CaseType.CONSENTED.getCcdType()).id(123L).build();

        assertTrue(caseDataService.isLitigantRepresented(finremCaseDetails, false));
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

    private static FinremCaseData createCaseDataApplRepresented(FinremCaseDetailsMapper objectMapper, String value) {
        return objectMapper.mapToFinremCaseData(createCaseDataApplRepresented(value));
    }

    private static Map<String, Object> createCaseDataRespRepresentedConsented(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(CONSENTED_RESPONDENT_REPRESENTED, value);

        return data;
    }

    private static FinremCaseData createCaseDataRespRepresentedConsented(FinremCaseDetailsMapper objectMapper, String value) {
        return objectMapper.mapToFinremCaseData(createCaseDataRespRepresentedConsented(value));
    }

    private static Map<String, Object> createCaseDataRespRepresentedContested(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(CONTESTED_RESPONDENT_REPRESENTED, value);

        return data;
    }

    private static FinremCaseData createCaseDataRespRepresentedContested(FinremCaseDetailsMapper objectMapper, String value) {
        return objectMapper.mapToFinremCaseData(createCaseDataRespRepresentedContested(value));
    }

}
