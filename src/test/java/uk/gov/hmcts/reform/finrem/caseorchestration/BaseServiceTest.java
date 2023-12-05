package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATIVE_UPDATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPDATE_CONTACT_DETAILS_EVENT;

@TestPropertySource(locations = "/application.properties")
@DirtiesContext
public abstract class BaseServiceTest extends BaseTest {

    @Autowired
    protected ObjectMapper mapper;
    @Autowired
    protected FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockBean
    protected EmailService emailService;

    public static final String CASE_DETAILS = "caseDetails";
    public static final String CASE_DATA = "case_data";

    public static final byte[] SOME_BYTES = "ainhsdcnoih".getBytes();

    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    protected String caseId = "123123123";

    protected String formattedNowDate = DateTimeFormatter.ofPattern(CCDConfigConstant.LETTER_DATE_FORMAT).format(LocalDate.now());

    protected CaseDetails buildCaseDetailsBefore() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
    }

    protected FinremCaseDetails buildFinremCaseDetails() {
        return FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(123L)
            .build();
    }

    protected CallbackRequest getConsentedCallbackRequestForVariationOrder() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Variation Order",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected FinremCallbackRequest getConsentedFinremCallbackRequestForVariationOrder() {
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Variation Order",
            "Property Adjustment Order");
        ContactDetailsWrapper contactDetailsWrapper = ContactDetailsWrapper.builder()
            .appRespondentFmName("David")
            .appRespondentLName("Goodman")
            .applicantFmName("Victoria")
            .applicantLname("Goodman")
            .solicitorEmail(TEST_SOLICITOR_EMAIL)
            .solicitorName(TEST_SOLICITOR_NAME)
            .solicitorReference(TEST_SOLICITOR_REFERENCE)
            .respondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL)
            .respondentSolicitorName(TEST_RESP_SOLICITOR_NAME)
            .respondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetailsWrapper)
            .divorceCaseNumber(TEST_DIVORCE_CASE_NUMBER)
            .natureApplicationWrapper(NatureApplicationWrapper.builder()
                .natureOfApplication2(Arrays.stream(NatureApplication.values()).toList()).build()).build();

        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }


    protected CallbackRequest getConsentedCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected CallbackRequest getConsentedCallbackRequestUpdateDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONSENTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, YES_VALUE);
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CallbackRequest.builder()
            .eventId(UPDATE_CONTACT_DETAILS_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONSENTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected FinremCallbackRequest getConsentedNewCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setAppRespondentFmName("David");
        caseData.getContactDetailsWrapper().setAppRespondentLName("Goodman");
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected CallbackRequest getContestedCallbackRequest() {
        Map<String, Object> caseData = getCaseData();
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Goodman");
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected CallbackRequest getContestedCallbackRequestWithCaseDataValues(Map<String, Object> caseDataValuesToAdd) {
        Map<String, Object> caseData = getCaseData();
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.putAll(caseDataValuesToAdd);
        //caseDataValuesToAdd.keySet().stream().forEach(k ->  caseData.put( k, caseDataValuesToAdd.get(k)));
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected FinremCallbackRequest getContestedNewCallbackRequest() {
        FinremCaseData caseData = getFinremCaseData();
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    private Map<String, Object> getCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        return caseData;
    }

    private FinremCaseData getFinremCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }

    protected String getResource(String resourcePath) throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource(resourcePath));
        return new String(Files.readAllBytes(file.toPath()));
    }

    protected CallbackRequest getContestedCallbackRequestUpdateDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "David");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Goodman");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Victoria");
        caseData.put(APPLICANT_LAST_NAME, "Goodman");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseData.put(RESP_SOLICITOR_REFERENCE, TEST_RESP_SOLICITOR_REFERENCE);
        caseData.put(SOLICITOR_REFERENCE, TEST_SOLICITOR_REFERENCE);
        caseData.put(DIVORCE_CASE_NUMBER, TEST_DIVORCE_CASE_NUMBER);
        caseData.put(GENERAL_APPLICATION_REFER_TO_JUDGE_EMAIL, TEST_JUDGE_EMAIL);
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(BULK_PRINT_LETTER_ID_RES, NOTTINGHAM);
        caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, YES_VALUE);
        return CallbackRequest.builder()
            .eventId(UPDATE_CONTACT_DETAILS_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseTypeId(CaseType.CONTESTED.getCcdType())
                .id(12345L)
                .data(caseData)
                .build())
            .build();
    }

    protected Map<String, Object> convertCaseDataToStringRepresentation(Map<String, Object> caseData) {
        try {
            return mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected CaseDocument buildCaseDocument(String url, String binaryUrl, String filename) {
        CaseDocument document = new CaseDocument();
        document.setDocumentUrl(url);
        document.setDocumentBinaryUrl(binaryUrl);
        document.setDocumentFilename(filename);
        return document;
    }

    protected CallbackRequest buildHearingCallbackRequest(String payloadJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payloadJson)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected FinremCallbackRequest buildHearingFinremCallbackRequest(String payloadJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payloadJson)) {
            return mapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }

    protected CallbackRequest buildCallbackRequest(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected FinremCallbackRequest buildFinremCallbackRequest(String testJson) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {

            CallbackRequest callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails());
            return FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .eventType(EventType.getEventType(callbackRequest.getEventId()))
                .build();
        }
    }

    protected Map<String, Object> getDataFromCaptor(ArgumentCaptor<Map<String, Object>> documentGenerationRequestCaseDetailsCaptor) {
        Map<String, Object> placeholdersMap = documentGenerationRequestCaseDetailsCaptor.getValue();
        Map<String, Object> caseDetails = (Map) placeholdersMap.get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDetails.get(CASE_DATA);
        return data;
    }

    protected CaseDetails buildCaseDetailsFromJson(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return caseDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
