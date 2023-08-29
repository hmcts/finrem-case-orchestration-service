package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.*;

@ExtendWith(MockitoExtension.class)
class RespondentShareDocumentsServiceTest {

    private RespondentShareDocumentsService service;
    private IntervenerShareDocumentsService intervenerShareDocumentsService;
    private final ThreadLocal<UUID> uuid = new ThreadLocal<>();

    @BeforeEach
    void beforeEach() {
        service = new RespondentShareDocumentsService();
        intervenerShareDocumentsService = new IntervenerShareDocumentsService();
        uuid.set(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
    }


    @Test
    void applicantSourceDocumentListWhenDocNotPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails details = request.getCaseDetails();

        DynamicMultiSelectList list = service.respondentSourceDocumentList(details);
        assertEquals("document size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }

    @Test
    void respondentSourceDocumentListWhenDocPresent() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();

        data.getUploadCaseDocumentWrapper().setRespOtherCollection(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setRespChronologiesCollection(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setRespStatementsExhibitsCollection(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setRespHearingBundlesCollection(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollection(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setRespQaCollection(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setRespCaseSummariesCollection(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setRespFormsHCollection(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setRespExpertEvidenceCollection(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setRespCorrespondenceDocsColl(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getRespOtherCollection();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, RESP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = service.respondentSourceDocumentList(details);
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertEquals("no document selected from list", 1, list.getValue().size());
    }

    @Test
    void getRespondentToOtherSolicitorRoleList() {

        FinremCallbackRequest<FinremCaseDataContested> request = buildCallbackRequest();
        FinremCaseDataContested caseData = request.getCaseDetails().getData();

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(RESP_SOLICITOR)));
        caseData.setSolicitorRoleList(roleList);

        DynamicMultiSelectList list = service.getOtherSolicitorRoleList(request.getCaseDetails(), getCaseRoleList(), RESP_SOLICITOR.getCcdCode());
        assertEquals("role size for sharing", 11, list.getListItems().size());
        assertEquals("no document selected from list", 1, list.getValue().size());
    }

    @Test
    void getRespondentToOtherSolicitorRoleListButNoCaseRoleAvailable() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = service.getOtherSolicitorRoleList(request.getCaseDetails(), new ArrayList<>(), RESP_SOLICITOR.getCcdCode());
        assertEquals("role size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }


    @Test
    void shareOneDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {
        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();


        data.getUploadCaseDocumentWrapper().setRespOtherCollection(getTestDocument(OTHER));
        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getRespOtherCollection();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, RESP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(APP_SOLICITOR)));
        data.setSolicitorRoleList(roleList);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(data);

        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppOtherCollectionShared().size());
    }

    @Test
    void shareDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails<FinremCaseDataContested> details = request.getCaseDetails();
        FinremCaseDataContested data = details.getData();


        data.getUploadCaseDocumentWrapper().setRespOtherCollection(getTestDocument(OTHER));
        data.getUploadCaseDocumentWrapper().setRespChronologiesCollection(getTestDocument(CHRONOLOGY));
        data.getUploadCaseDocumentWrapper().setRespStatementsExhibitsCollection(getTestDocument(STATEMENT_AFFIDAVIT));
        data.getUploadCaseDocumentWrapper().setRespHearingBundlesCollection(getTestDocument(TRIAL_BUNDLE));
        data.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollection(getTestDocument(APPLICANT_FORM_E));
        data.getUploadCaseDocumentWrapper().setRespQaCollection(getTestDocument(QUESTIONNAIRE));
        data.getUploadCaseDocumentWrapper().setRespCaseSummariesCollection(getTestDocument(CASE_SUMMARY));
        data.getUploadCaseDocumentWrapper().setRespFormsHCollection(getTestDocument(FORM_H));
        data.getUploadCaseDocumentWrapper().setRespExpertEvidenceCollection(getTestDocument(EXPERT_EVIDENCE));
        data.getUploadCaseDocumentWrapper().setRespCorrespondenceDocsColl(getTestDocument(CARE_PLAN));

        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();

        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
        CaseDocument doc = coll.get(0).getUploadCaseDocument().getCaseDocuments();
        sourceDocumentList.setValue(List.of(getSelectedDoc(coll, doc, RESP_CORRESPONDENCE_COLLECTION),
            getSelectedDoc(coll, doc, RESP_OTHER_COLLECTION),
            getSelectedDoc(coll, doc, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION),
            getSelectedDoc(coll, doc, RESP_STATEMENTS_EXHIBITS_COLLECTION),
            getSelectedDoc(coll, doc, RESP_HEARING_BUNDLES_COLLECTION),
            getSelectedDoc(coll, doc, RESP_FORM_E_EXHIBITS_COLLECTION),
            getSelectedDoc(coll, doc, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION),
            getSelectedDoc(coll, doc, RESP_CASE_SUMMARIES_COLLECTION),
            getSelectedDoc(coll, doc, RESP_FORM_H_COLLECTION),
            getSelectedDoc(coll, doc, RESP_EXPERT_EVIDENCE_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(List.of(getSelectedParty(APP_SOLICITOR),
            getSelectedParty(INTVR_SOLICITOR_1),
            getSelectedParty(INTVR_SOLICITOR_2),
            getSelectedParty(INTVR_SOLICITOR_3),
            getSelectedParty(INTVR_SOLICITOR_4)));

        data.setSolicitorRoleList(roleList);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(data);
        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();

        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppCorrespondenceDocsCollShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppOtherCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppExpertEvidenceCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppFormsHCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppHearingBundlesCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppCaseSummariesCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppStatementsExhibitsCollShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppQaCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppChronologiesCollectionShared().size());
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppFormEExhibitsCollectionShared().size());

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
            .caseDocumentParty(RESPONDENT)
            .caseDocumentOther("No")
            .caseDocumentConfidentiality(YesOrNo.NO)
            .hearingDetails("UK 1400 hours")
            .caseDocumentFdr(YesOrNo.NO)
            .caseDocumentUploadDateTime(LocalDateTime.now()).build();
        return List.of(UploadCaseDocumentCollection.builder().id(uuid.get().toString()).uploadCaseDocument(document).build());
    }

    private FinremCallbackRequest<FinremCaseDataContested> buildCallbackRequest() {
        FinremCaseDataContested caseData = new FinremCaseDataContested();
        return FinremCallbackRequest
            .<FinremCaseDataContested>builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetailsBefore(FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .caseDetails(FinremCaseDetails.<FinremCaseDataContested>builder().id(123L).caseType(CONTESTED)
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