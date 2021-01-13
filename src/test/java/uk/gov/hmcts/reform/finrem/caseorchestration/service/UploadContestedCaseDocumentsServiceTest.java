package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_TRIAL_BUNDLE_COLLECTION;

public class UploadContestedCaseDocumentsServiceTest extends BaseServiceTest {

    @Autowired
    private UploadContestedCaseDocumentsService service;

    @Autowired
    private ObjectMapper mapper;

    private CaseDetails caseDetails;

    private Map<String, Object> caseData;

    private List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void applicantCorrespondenceDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "applicant", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION), hasSize(1));
    }

    @Test
    public void applicantFrFormsDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form G", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "applicant", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_FR_FORM_COLLECTION), hasSize(5));
    }

    @Test
    public void applicantEvidenceDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement of Issues", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION), hasSize(14));
    }

    @Test
    public void applicantTrialBundleDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "applicant", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION), hasSize(1));
    }

    @Test
    public void applicantConfidentialDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_CONFIDENTIAL_DOCS_COLLECTION), hasSize(1));
    }

    @Test
    public void respondentCorrespondenceDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "respondent", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, RESPONDENT_CORRESPONDENCE_COLLECTION), hasSize(1));
    }

    @Test
    public void respondentFrFormsDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Applicant - Form E", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form F", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form G", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, RESPONDENT_FR_FORM_COLLECTION), hasSize(5));
    }

    @Test
    public void respondentEvidenceDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Statement of Issues", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Case Summary", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Questionnaire", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Reply to Questionnaire", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Valuation Report", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Pension Plan", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Position Statement", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Skeleton Argument", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Expert Evidence", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Witness Statement/Affidavit", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Care Plan", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Offers", "respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "respondent", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, RESPONDENT_EVIDENCE_COLLECTION), hasSize(14));
    }

    @Test
    public void respondentTrialBundleDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "respondent", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION), hasSize(1));
    }

    @Test
    public void respondentConfidentialDocumentsFiltered() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION), hasSize(1));
    }

    @Test
    public void documentsAreNotFilteredWithoutDocTypeOrPartySpecified() {

        //simulating user not uploading a document into CCD Document field
        uploadDocumentList.add(createNullDocumentUploadItem("Form B", "applicant", "no", null));

        uploadDocumentList.add(createContestedUploadDocumentItem(null, "applicant", "yes", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Form B", null, "yes", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS), hasSize(3));
    }

    @Test
    public void otherDocumentTypeIsFiltered() {
        //Valid filter to Evidence Collection
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", null, null));
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "no", "Other Example"));

        //Valid filter to Confidential Collection
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "yes", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("other", "applicant", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION), hasSize(3));
        assertThat(getDocumentCollection(caseData, APPLICANT_CONFIDENTIAL_DOCS_COLLECTION), hasSize(2));
    }

    @Test
    public void documentsUploadedDoNotFilterToRespondentUnlessSelected() {
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "applicant", "no", null));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION), hasSize(1));

        assertNull(getDocumentCollection(caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, RESPONDENT_CORRESPONDENCE_COLLECTION));
        assertNull(getDocumentCollection(caseData, RESPONDENT_FR_FORM_COLLECTION));
        assertNull(getDocumentCollection(caseData, RESPONDENT_EVIDENCE_COLLECTION));
        assertNull(getDocumentCollection(caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION));
    }

    @Test
    public void applicantAndRespondentConfidentialDocumentsAreNotStoredWhenToggleOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);

        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", "yes", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "yes", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        service.filterDocumentsToRelevantParty(caseData);

        assertNull(getDocumentCollection(caseData, APPLICANT_CONFIDENTIAL_DOCS_COLLECTION));
        assertNull(getDocumentCollection(caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION));
    }


    private ContestedUploadedDocumentData createContestedUploadDocumentItem(String type, String party,
                                                                            String isConfidential, String other) {

        return ContestedUploadedDocumentData.builder()
                .uploadedCaseDocument(ContestedUploadedDocument
                    .builder()
                    .caseDocuments(new CaseDocument())
                    .caseDocumentType(type)
                    .caseDocumentParty(party)
                    .caseDocumentConfidential(isConfidential)
                    .caseDocumentOther(other)
                    .build())
                .build();
    }

    private ContestedUploadedDocumentData createNullDocumentUploadItem(String type, String party,
                                                                       String isConfidential, String other) {

        return ContestedUploadedDocumentData.builder()
                .uploadedCaseDocument(ContestedUploadedDocument
                    .builder()
                    .caseDocuments(null)
                    .caseDocumentType(type)
                    .caseDocumentParty(party)
                    .caseDocumentConfidential(isConfidential)
                    .caseDocumentOther(other)
                    .build())
                .build();
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<List<ContestedUploadedDocumentData>>() {
            });
    }
}