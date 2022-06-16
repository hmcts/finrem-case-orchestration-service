package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICANT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;

public class ManageCaseDocumentsServiceTest extends BaseServiceTest {

    @Autowired
    private ManageCaseDocumentsService manageCaseDocumentsService;

    private CaseDetails caseDetails;

    private Map<String, Object> caseData;

    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(false);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        caseDetails = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenApplicantDocumentsUploaded() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "Applicant", "no", null));

        caseDetails.getData().put(APP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(getDocumentCollection(caseData, CONTESTED_APPLICANT_DOCUMENTS_UPLOADED), hasSize(1));
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenRespondentDocumentsUploaded() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "Respondent", "no", null));

        caseDetails.getData().put(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(getDocumentCollection(caseData, CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED), hasSize(1));
    }

    @Test
    public void givenCaseDataMap_whenRemoveDeletedFilesFromCaseData_thenApplicantAndRespondentKeysDoNotExistInCaseData() {

        manageCaseDocumentsService.removeDeletedFilesFromCaseData(populateCaseData().getData());

        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(1));
        assertNull(caseData.get(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED));
        assertNull(caseData.get(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED));
    }

    private CaseDetails populateCaseData() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));

        uploadDocumentList.get(0).setId("123");
        uploadDocumentList.get(1).setId("456");

        DocumentDetailsData data = new DocumentDetailsData();
        data.setId("123");
        data.setDocumentDetails(new DocumentDetailsCollection());

        List<DocumentDetailsData> documentDetailsData = new ArrayList<>();
        documentDetailsData.add(data);

        caseDetails.getData().put(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED, documentDetailsData);
        caseDetails.getData().put(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED, documentDetailsData);

        caseDetails.getData().put(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        return caseDetails;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<>() {
            });
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
                .hearingDetails(null)
                .build())
            .build();
    }
}