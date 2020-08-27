package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
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


    @Before
    public void setUp() throws Exception {
        caseDetails = contestedCaseDetails();
    }

    @Test
    public void documentsAreFilteredBasedOnPartySpecified() throws Exception {
        Map<String, Object> caseData = caseDetails.getData();
        service.filterDocumentsToRelevantParty(caseData);

        assertThat(getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS), hasSize(4));
        assertThat(getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, APPLICANT_FR_FORM_COLLECTION), hasSize(5));
        assertThat(getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION), hasSize(14));
        assertThat(getDocumentCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION), hasSize(1));

        assertThat(getDocumentCollection(caseData, RESPONDENT_CORRESPONDENCE_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, RESPONDENT_FR_FORM_COLLECTION), hasSize(5));
        assertThat(getDocumentCollection(caseData, RESPONDENT_EVIDENCE_COLLECTION), hasSize(14));
        assertThat(getDocumentCollection(caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION), hasSize(1));
    }

    private CaseDetails contestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/contested-upload-case-documents.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<List<ContestedUploadedDocumentData>>() {
            });
    }
}