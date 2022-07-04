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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_H_COLLECTION;

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

        assertThat(getDocumentCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION), hasSize(1));
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenRespondentDocumentsUploaded() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));

        uploadDocumentList.get(0).setId("1");
        caseDetails.getData().put(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(getDocumentCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION), hasSize(1));
    }

    @Test
    public void givenCaseDataMap_whenRemoveDeletedFilesFromCaseData_thenApplicantAndRespondentKeysDoNotExistInCaseData() {

        manageCaseDocumentsService.manageCaseDocuments(populateCaseData().getData());

        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(1));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToRespChronologiesCollection() {

        List<ContestedUploadedDocumentData> chronologyDocs = new ArrayList<>();

        chronologyDocs.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        chronologyDocs.get(0).setId("4");

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));

        caseDocs.get(0).setId("1");
        caseDocs.get(1).setId("2");
        caseDocs.get(2).setId("3");
        caseDocs.get(3).setId("4");

        caseDocs.get(3).getUploadedCaseDocument().setCaseDocuments(
            CaseDocument.builder().documentFilename("File1")
                .documentUrl("Url")
                .documentBinaryUrl("binaryUrl").build());

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);
        caseDetails.getData().put(APP_CHRONOLOGIES_STATEMENTS_COLLECTION, chronologyDocs);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(0));
        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(4));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToRightCollectionFormH() {

        List<ContestedUploadedDocumentData> chronologyDocs = new ArrayList<>();

        chronologyDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));
        chronologyDocs.get(0).setId("4");

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Form H", "applicant", "no", null));

        caseDocs.get(3).getUploadedCaseDocument().setCaseDocuments(
            CaseDocument.builder().documentFilename("File1")
            .documentUrl("Url")
            .documentBinaryUrl("binaryUrl").build());

        caseDocs.get(0).setId("1");
        caseDocs.get(1).setId("2");
        caseDocs.get(2).setId("3");
        caseDocs.get(3).setId("4");

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);
        caseDetails.getData().put(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, chronologyDocs);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(0));
        assertThat(getDocumentCollection(caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(3));
        assertThat(getDocumentCollection(caseData, APP_FORMS_H_COLLECTION), hasSize(1));
    }


    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToAppChronologiesCollection() {

        List<ContestedUploadedDocumentData> formH = new ArrayList<>();

        formH.add(createContestedUploadDocumentItem("Form H", "applicant", "no", null));
        formH.get(0).setId("4");

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));

        caseDocs.get(0).setId("1");
        caseDocs.get(1).setId("2");
        caseDocs.get(2).setId("3");
        caseDocs.get(3).setId("4");

        caseDocs.get(3).getUploadedCaseDocument().setCaseDocuments(
            CaseDocument.builder().documentFilename("File1")
                .documentUrl("Url")
                .documentBinaryUrl("binaryUrl").build());

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);
        caseDetails.getData().put(APP_FORMS_H_COLLECTION, formH);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_FORMS_H_COLLECTION), hasSize(0));
        assertThat(getDocumentCollection(caseData, RESP_FORM_H_COLLECTION), hasSize(3));
        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(1));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentStaysInCollection_thenRemoveItFromCaseCollection() {

        List<ContestedUploadedDocumentData> formH = new ArrayList<>();

        formH.add(createContestedUploadDocumentItem("Form H", "applicant", "no", null));
        formH.get(0).setId("4");

        List<ContestedUploadedDocumentData> caseDocuments = new ArrayList<>();
        caseDocuments.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("Form H", "applicant", "no", null));

        caseDocuments.get(0).setId("1");
        caseDocuments.get(1).setId("2");
        caseDocuments.get(2).setId("3");
        caseDocuments.get(3).setId("4");

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocuments);
        caseDetails.getData().put(APP_FORMS_H_COLLECTION, formH);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_FORMS_H_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, RESP_FORM_H_COLLECTION), hasSize(3));
    }

    private CaseDetails populateCaseData() {

        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "Respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("Chronology", "Respondent", "no", null));

        uploadDocumentList.get(0).setId("123");
        uploadDocumentList.get(1).setId("456");

        ContestedUploadedDocumentData data = new ContestedUploadedDocumentData();
        data.setId("123");
        data.setUploadedCaseDocument(new ContestedUploadedDocument());

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("Chronology", "respondent", "no", null));

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);

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
                .hearingDetails("hearingDetails")
                .build())
            .build();
    }
}