package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
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
        return CaseDetails.builder().id(Long.valueOf(123)).caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
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
                .caseTypeId(CASE_TYPE_ID_CONSENTED)
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
                .caseTypeId(CASE_TYPE_ID_CONSENTED)
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
                .caseTypeId(CASE_TYPE_ID_CONSENTED)
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
                .caseTypeId(CASE_TYPE_ID_CONTESTED)
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
                .caseTypeId(CASE_TYPE_ID_CONTESTED)
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
                .caseTypeId(CASE_TYPE_ID_CONTESTED)
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

    protected CallbackRequest buildHearingCallbackRequest(String payloadJson)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(payloadJson)) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class);
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
}
