package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantUploadedDocumentData;
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

        assertThat(getUploadedCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS), hasSize(21));
        assertThat(getApplicantCorrespondenceCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION), hasSize(1));
        assertThat(getApplicantCorrespondenceCollection(caseData, APPLICANT_FR_FORM_COLLECTION), hasSize(5));
        assertThat(getApplicantCorrespondenceCollection(caseData, APPLICANT_EVIDENCE_COLLECTION), hasSize(14));
        assertThat(getApplicantCorrespondenceCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION), hasSize(1));
    }

    private CaseDetails contestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/contested-upload-case-documents.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private List<ContestedUploadedDocumentData> getUploadedCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<List<ContestedUploadedDocumentData>>() {
            });
    }

    private List<ApplicantUploadedDocumentData> getApplicantCorrespondenceCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }
}