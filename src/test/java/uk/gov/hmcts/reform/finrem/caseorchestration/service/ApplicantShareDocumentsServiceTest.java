package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.APPLICANT_FORM_E;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.CHRONOLOGY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.FORM_H;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.STATEMENT_AFFIDAVIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.TRIAL_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION;

@ExtendWith(MockitoExtension.class)
class ApplicantShareDocumentsServiceTest {

    private ApplicantShareDocumentsService service;
    private IntervenerShareDocumentsService intervenerShareDocumentsService;
    private final ThreadLocal<UUID> uuid = new ThreadLocal<>();

    @BeforeEach
    void beforeEach() {
        service = new ApplicantShareDocumentsService();
        intervenerShareDocumentsService = new IntervenerShareDocumentsService();
        uuid.set(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
    }


    @Test
    void applicantSourceDocumentListWhenDocNotPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails details = request.getCaseDetails();

        DynamicMultiSelectList list = service.applicantSourceDocumentList(details);
        assertEquals("document size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }

    @Test
    void applicantSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setAppOtherCollection(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setAppChronologiesCollection(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setAppStatementsExhibitsCollection(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setAppHearingBundlesCollection(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollection(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setAppQaCollection(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setAppCaseSummariesCollection(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setAppFormsHCollection(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollection(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setAppCorrespondenceDocsCollection(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getAppOtherCollection();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, APP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = service.applicantSourceDocumentList(details);
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertEquals("no document selected from list", 1, list.getValue().size());
    }

    @Test
    void getApplicantToOtherSolicitorRoleList() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = service.getOtherSolicitorRoleList(request.getCaseDetails(), getCaseRoleList(), APP_SOLICITOR.getCcdCode());
        assertEquals("role size for sharing", 11, list.getListItems().size());
    }

    @Test
    void getApplicantToOtherSolicitorRoleListButNoCaseRoleAvailable() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = service.getOtherSolicitorRoleList(request.getCaseDetails(), new ArrayList<>(), APP_SOLICITOR.getCcdCode());
        assertEquals("role size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }


    @Test
    void shareOneDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {
        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();


        data.getUploadCaseDocumentWrapper().setAppOtherCollection(getTestDocument(OTHER));
        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getAppOtherCollection();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, APP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(RESP_SOLICITOR)));
        data.setSolicitorRoleList(roleList);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(data);

        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespOtherCollectionShared().size());
    }

    @Test
    void shareDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();


        data.getUploadCaseDocumentWrapper().setAppOtherCollection(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setAppChronologiesCollection(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setAppStatementsExhibitsCollection(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setAppHearingBundlesCollection(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollection(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setAppQaCollection(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setAppCaseSummariesCollection(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setAppFormsHCollection(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollection(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setAppCorrespondenceDocsCollection(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();

        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(List.of(getSelectedDoc(coll, doc, APPLICANT_CORRESPONDENCE_DOC_COLLECTION),
            getSelectedDoc(coll, doc, APP_OTHER_COLLECTION),
            getSelectedDoc(coll, doc, APP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            getSelectedDoc(coll, doc, APP_STATEMENTS_EXHIBITS_COLLECTION),
            getSelectedDoc(coll, doc, APP_HEARING_BUNDLES_COLLECTION),
            getSelectedDoc(coll, doc, APP_FORM_E_EXHIBITS_COLLECTION),
            getSelectedDoc(coll, doc, APP_QUESTIONNAIRES_ANSWERS_COLLECTION),
            getSelectedDoc(coll, doc, APP_CASE_SUMMARIES_COLLECTION),
            getSelectedDoc(coll, doc, APP_FORMS_H_COLLECTION),
            getSelectedDoc(coll, doc, APP_EXPERT_EVIDENCE_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = service.getOtherSolicitorRoleList(request.getCaseDetails(), getCaseRoleList(), APP_SOLICITOR.getCcdCode());
        list.setValue(List.of(getSelectedParty(RESP_SOLICITOR),
            getSelectedParty(INTVR_SOLICITOR_1),
            getSelectedParty(INTVR_SOLICITOR_2),
            getSelectedParty(INTVR_SOLICITOR_3),
            getSelectedParty(INTVR_SOLICITOR_4)));

        data.setSolicitorRoleList(list);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(data);
        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();

        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespCorrespondenceDocsCollShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespOtherCollectionShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespExpertEvidenceCollShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespFormsHCollectionShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespHearingBundlesCollShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespCaseSummariesCollectionShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespStatementsExhibitsCollShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespQaCollectionShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespChronologiesCollectionShared().size());
        assertEquals("one document shared with respondent solicitor", 1,
            wrapper.getRespFormEExhibitsCollectionShared().size());


        assertEquals("one document shared with intervener 1 solicitor", 1,
            wrapper.getIntv1CorrespDocsShared().size());

        assertEquals("one document shared with intervener 2 solicitor", 1,
            wrapper.getIntv2CorrespDocsShared().size());

        assertEquals("one document shared with intervener 3 solicitor", 1,
            wrapper.getIntv3CorrespDocsShared().size());

        assertEquals("one document shared with intervener 4 solicitor", 1,
            wrapper.getIntv4CorrespDocsShared().size());
    }

    private static DynamicMultiSelectListElement getSelectedDoc(List<UploadCaseDocumentCollection> coll,
                                                                CaseDocument doc,
                                                                CaseDocumentCollectionType type) {
        return DynamicMultiSelectListElement.builder()
            .label(type.getCcdKey() + " -> " + doc.getDocumentFilename())
            .code(coll.get(0).getId() + "#" + type.getCcdKey())
            .build();
    }

    private DynamicMultiSelectListElement getSelectedParty(CaseRole role) {
        return DynamicMultiSelectListElement.builder()
            .label(role.getCcdCode()).code(role.getCcdCode()).build();
    }

    private List<UploadCaseDocumentCollection> getTestDocument(CaseDocumentType documentType) {
        UploadCaseDocument document = UploadCaseDocument.builder()
            .caseDocuments(TestSetUpUtils.caseDocument())
            .caseDocumentType(documentType)
            .caseDocumentParty(APPLICANT)
            .caseDocumentOther("No")
            .caseDocumentConfidentiality(YesOrNo.NO)
            .hearingDetails("UK 1400 hours")
            .caseDocumentFdr(YesOrNo.NO)
            .caseDocumentUploadDateTime(LocalDateTime.now()).build();
        return List.of(UploadCaseDocumentCollection.builder().id(uuid.get().toString()).uploadCaseDocument(document).build());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseDataContested caseData = new FinremCaseDataContested();
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .build();
    }

    private List<CaseAssignmentUserRole> getCaseRoleList() {

        List<String> roleList = List.of("[APPSOLICITOR]", "[APPBARRISTER]", "[RESPSOLICITOR]",
            "[RESPBARRISTER]", "[INTVRSOLICITOR1]", "[INTVRSOLICITOR2]", "[INTVRSOLICITOR3]", "[INTVRSOLICITOR4]",
            "[INTVRBARRISTER1]", "[INTVRBARRISTER2]", "[INTVRBARRISTER3]", "[INTVRBARRISTER4]");
        List<CaseAssignmentUserRole> caseAssignedUserRoleList = new ArrayList<>();
        roleList.forEach(role -> {
            caseAssignedUserRoleList.add(CaseAssignmentUserRole.builder().userId(role).caseRole(role).caseDataId(String.valueOf(123L)).build());
        });
        return caseAssignedUserRoleList;
    }
}