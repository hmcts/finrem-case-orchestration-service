package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

public class ManageCaseDocumentsServiceTest extends BaseServiceTest {

    @Autowired
    private ManageCaseDocumentsService manageCaseDocumentsService;

    private FinremCaseDetails caseDetails;

    private FinremCaseData caseData;

    private final List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(false);
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        caseDetails = buildFinremCaseDetails();
        caseData = caseDetails.getCaseData();
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenApplicantDocumentsUploaded() {
        uploadDocumentList.add(createContestedUploadDocumentItem(UUID.randomUUID(), CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.APPLICANT, YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setAppChronologiesCollection(uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection(), hasSize(1));
    }

    @Test
    public void givenCaseData_whenSetApplicantAndRespondentDocumentsCollection_thenRespondentDocumentsUploaded() {

        uploadDocumentList.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setRespChronologiesCollection(uploadDocumentList);

        manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails);

        assertThat(caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection(), hasSize(1));
    }

    @Test
    public void givenCaseDataMap_whenRemoveDeletedFilesFromCaseData_thenApplicantAndRespondentKeysDoNotExistInCaseData() {

        manageCaseDocumentsService.manageCaseDocuments(populateCaseData().getCaseData());

        assertThat(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection(), hasSize(1));
    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToRespChronologiesCollection() {

        List<UploadCaseDocumentCollection> chronologyDocs = new ArrayList<>();

        UUID someId = UUID.randomUUID();

        chronologyDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT,
            YesOrNo.NO, null));

        List<UploadCaseDocumentCollection> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(), CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocs.add(createContestedUploadDocumentItem(someId,CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocs);
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setAppChronologiesCollection(chronologyDocs);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getCaseData());

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection(), hasSize(0));
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection(), hasSize(4));
    }

//    @Test
//    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToRightCollectionFormH() {
//
//        List<UploadCaseDocumentCollection> chronologyDocs = new ArrayList<>();
//
//        UUID someId = UUID.randomUUID();
//
//        chronologyDocs.add(createContestedUploadDocumentItem(someId, CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT, YesOrNo.NO, null));
//
//        List<UploadCaseDocumentCollection> caseDocs = new ArrayList<>();
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(someId, CaseDocumentType.FORM_H, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocs);
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setRespChronologiesCollection(chronologyDocs);
//
//        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getCaseData());
//
//        assertThat(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection(), hasSize(0));
//        assertThat(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection(), hasSize(3));
//        assertThat(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection(), hasSize(1));
//    }

//    @Test
//    public void givenCaseDataManageCaseDocuments_whenDocumentInWrongCollection_thenMoveItToAppChronologiesCollection() {
//
//        List<UploadCaseDocumentCollection> formH = new ArrayList<>();
//
//        UUID someId = UUID.randomUUID();
//
//        formH.add(createContestedUploadDocumentItem(someId, CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//
//        List<UploadCaseDocumentCollection> caseDocs = new ArrayList<>();
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(), CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(), CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(), CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(someId, CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT, YesOrNo.NO, null));
//
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocs);
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setAppFormsHCollection(formH);
//
//
//        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getCaseData());
//
//        assertThat(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection(), hasSize(0));
//        assertThat(caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection(), hasSize(3));
//        assertThat(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection(), hasSize(1));
//    }

    @Test
    public void givenCaseDataManageCaseDocuments_whenDocumentStaysInCollection_thenRemoveItFromCaseCollection() {

        List<UploadCaseDocumentCollection> formH = new ArrayList<>();

        UUID someId = UUID.randomUUID();

        formH.add(createContestedUploadDocumentItem(someId, CaseDocumentType.FORM_H, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));

        List<UploadCaseDocumentCollection> caseDocuments = new ArrayList<>();
        caseDocuments.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocuments.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocuments.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.FORM_H, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        caseDocuments.add(createContestedUploadDocumentItem(someId,CaseDocumentType.FORM_H, CaseDocumentParty.APPLICANT,
            YesOrNo.NO, null));

        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocuments);
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setAppFormsHCollection(formH);

        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getCaseData());

        assertThat(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection(), hasSize(1));
        assertThat(caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection(), hasSize(3));
    }

//    @Test
//    public void givenCaseDataManageCaseDocuments_whenDocumentIsNotFdrDocument_thenMoveItToRightCollectionFromFdr() {
//
//        List<UploadCaseDocumentCollection> formHFdrDocs = new ArrayList<>();
//
//        UUID someId = UUID.randomUUID();
//
//        formHFdrDocs.add(createContestedUploadDocumentItem(someId,CaseDocumentType.FORM_H, null, YesOrNo.NO, null));
//
//        List<UploadCaseDocumentCollection> caseDocs = new ArrayList<>();
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(UUID.randomUUID(),CaseDocumentType.CHRONOLOGY, CaseDocumentParty.APPLICANT,
//            YesOrNo.NO, null));
//        caseDocs.add(createContestedUploadDocumentItem(someId, CaseDocumentType.FORM_H, CaseDocumentParty.APPLICANT, YesOrNo.NO, null));
//
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocs);
//        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setFdrCaseDocumentCollection(formHFdrDocs);
//
//        manageCaseDocumentsService.manageCaseDocuments(caseDetails.getCaseData());
//
//        assertThat(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection(), hasSize(3));
//        assertThat(caseData.getUploadCaseDocumentWrapper().getFdrCaseDocumentCollection(), hasSize(0));
//    }

    private FinremCaseDetails populateCaseData() {

        UUID someId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        uploadDocumentList.add(createContestedUploadDocumentItem(someId,CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));
        uploadDocumentList.add(createContestedUploadDocumentItem(otherId,CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT,
            YesOrNo.NO, null));

        List<UploadCaseDocumentCollection> caseDocs = new ArrayList<>();
        caseDocs.add(createContestedUploadDocumentItem(someId, CaseDocumentType.CHRONOLOGY, CaseDocumentParty.RESPONDENT, YesOrNo.NO, null));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocs);
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setRespChronologiesCollection(uploadDocumentList);

        return caseDetails;
    }

    private UploadCaseDocumentCollection createContestedUploadDocumentItem(UUID id, CaseDocumentType type, CaseDocumentParty party,
                                                                           YesOrNo isConfidential, String other) {

        return UploadCaseDocumentCollection.builder()
            .id(id)
            .value(UploadCaseDocument
                .builder()
                .caseDocuments(new Document())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(YesOrNo.NO)
                .hearingDetails("hearingDetails")
                .build())
            .build();
    }
}