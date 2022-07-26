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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;
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

        uploadDocumentList.add(createContestedUploadDocumentItem("1", "Chronology", "Applicant", "no", null));

        caseDetails.getData().put(APP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(getDocumentCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION), hasSize(1));
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenRespondentDocumentsUploaded() {

        uploadDocumentList.add(createContestedUploadDocumentItem("1","Chronology", "respondent", "no", null));

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

        chronologyDocs.add(createContestedUploadDocumentItem("4","Chronology", "applicant", "no", null));

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("1","Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("2","Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("3","Chronology", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("4","Chronology", "respondent", "no", null));

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);
        caseDetails.getData().put(APP_CHRONOLOGIES_STATEMENTS_COLLECTION, chronologyDocs);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(0));
        assertThat(getDocumentCollection(caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(4));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToRightCollectionFormH() {

        List<ContestedUploadedDocumentData> chronologyDocs = new ArrayList<>();

        chronologyDocs.add(createContestedUploadDocumentItem("4","Chronology", "respondent", "no", null));

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("1","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("2","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("3","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("4","Form H", "applicant", "no", null));

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

        formH.add(createContestedUploadDocumentItem("4","Form H", "applicant", "no", null));

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("1","Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("2","Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("3","Form H", "respondent", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("4","Chronology", "respondent", "no", null));

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

        formH.add(createContestedUploadDocumentItem("4","Form H", "applicant", "no", null));

        List<ContestedUploadedDocumentData> caseDocuments = new ArrayList<>();
        caseDocuments.add(createContestedUploadDocumentItem("1","Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("2","Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("3","Form H", "respondent", "no", null));
        caseDocuments.add(createContestedUploadDocumentItem("4","Form H", "applicant", "no", null));

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocuments);
        caseDetails.getData().put(APP_FORMS_H_COLLECTION, formH);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_FORMS_H_COLLECTION), hasSize(1));
        assertThat(getDocumentCollection(caseData, RESP_FORM_H_COLLECTION), hasSize(3));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentIsNotFdrDocument_thenMoveItToRightCollectionFromFdr() {

        List<ContestedUploadedDocumentData> formHFdrDocs = new ArrayList<>();

        formHFdrDocs.add(createContestedUploadDocumentItem("4","Form H", null, "no", null));

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("1","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("2","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("3","Chronology", "applicant", "no", null));
        caseDocs.add(createContestedUploadDocumentItem("4","Form H", "applicant", "no", null));

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);
        caseDetails.getData().put(FDR_DOCS_COLLECTION, formHFdrDocs);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getData());

        assertThat(getDocumentCollection(caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION), hasSize(3));
        assertThat(getDocumentCollection(caseData, FDR_DOCS_COLLECTION), hasSize(0));
    }

    private CaseDetails populateCaseData() {

        uploadDocumentList.add(createContestedUploadDocumentItem("123","Chronology", "Respondent", "no", null));
        uploadDocumentList.add(createContestedUploadDocumentItem("456","Chronology", "Respondent", "no", null));

        List<ContestedUploadedDocumentData> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem("123", "Chronology", "respondent", "no", null));

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocs);

        caseDetails.getData().put(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, uploadDocumentList);

        return caseDetails;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field),
            new TypeReference<>() {
            });
    }

    private ContestedUploadedDocumentData createContestedUploadDocumentItem(String id, String type, String party,
                                                                            String isConfidential, String other) {

        return ContestedUploadedDocumentData.builder()
            .id(id)
            .uploadedCaseDocument(ContestedUploadedDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr("no")
                .hearingDetails("hearingDetails")
                .build())
            .build();
    }
}