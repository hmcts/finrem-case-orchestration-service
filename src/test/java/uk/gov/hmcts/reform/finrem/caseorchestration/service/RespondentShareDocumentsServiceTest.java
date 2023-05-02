package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.RESPONDENT;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION;

@ExtendWith(MockitoExtension.class)
class RespondentShareDocumentsServiceTest {

    private RespondentShareDocumentsService service;
    private static final String TEST_ORG = "HSKEOS";
    private final ThreadLocal<UUID> uuid = new ThreadLocal<>();

    @BeforeEach
    void beforeEach() {
        service = new RespondentShareDocumentsService();
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
        FinremCaseDetails details = request.getCaseDetails();
        FinremCaseData data = details.getData();

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
        CaseDocument doc = coll.get(0).getValue().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, RESP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList list = service.respondentSourceDocumentList(details);
        assertEquals("document size for sharing", 10, list.getListItems().size());
        assertEquals("no document selected from list", 1, list.getValue().size());
    }

    @Test
    void getRespondentToOtherSolicitorRoleList() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseData caseData = request.getCaseDetails().getData();
        setCaseRole(request.getCaseDetails().getData());

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(APP_SOLICITOR)));
        caseData.setSolicitorRoleList(roleList);

        DynamicMultiSelectList list = service.getRespondentToOtherSolicitorRoleList(request.getCaseDetails());
        assertEquals("role size for sharing", 5, list.getListItems().size());
        assertEquals("no document selected from list", 1, list.getValue().size());

        FinremCaseData data = request.getCaseDetails().getData();
        data.setIntervenerOneWrapper(null);
        data.setIntervenerTwoWrapper(null);
        data.setIntervenerThreeWrapper(null);
        data.setIntervenerFourWrapper(null);
        data.setApplicantOrganisationPolicy(null);
        data.setSolicitorRoleList(new DynamicMultiSelectList());

        list = service.getRespondentToOtherSolicitorRoleList(request.getCaseDetails());

        assertEquals("role size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }

    @Test
    void getApplicantToOtherSolicitorRoleListButNoCaseRoleAvailable() {
        FinremCallbackRequest request = buildCallbackRequest();
        DynamicMultiSelectList list = service.getRespondentToOtherSolicitorRoleList(request.getCaseDetails());
        assertEquals("role size for sharing", 0, list.getListItems().size());
        assertNull("no document selected from list", list.getValue());
    }


    @Test
    void shareOneDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {
        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails details = request.getCaseDetails();
        FinremCaseData data = details.getData();
        setCaseRole(data);

        data.getUploadCaseDocumentWrapper().setRespOtherCollection(getTestDocument(OTHER));
        DynamicMultiSelectList sourceDocumentList = new DynamicMultiSelectList();
        List<UploadCaseDocumentCollection> coll = data.getUploadCaseDocumentWrapper().getRespOtherCollection();
        CaseDocument doc = coll.get(0).getValue().getCaseDocuments();
        sourceDocumentList.setValue(singletonList(getSelectedDoc(coll, doc, RESP_OTHER_COLLECTION)));
        data.setSourceDocumentList(sourceDocumentList);

        DynamicMultiSelectList roleList = new DynamicMultiSelectList();
        roleList.setValue(singletonList(getSelectedParty(APP_SOLICITOR)));
        data.setSolicitorRoleList(roleList);

        service.copyDocumentOnTheirRespectiveCollectionForSelectedSolicitors(data);

        UploadCaseDocumentWrapper wrapper = data.getUploadCaseDocumentWrapper();
        assertEquals("one document shared with applicant solicitor", 1,
            wrapper.getAppOtherCollectionShared().size());
    }

    @Test
    void shareDocumentOnTheirRespectiveCollectionForSelectedSolicitors() {

        FinremCallbackRequest request = buildCallbackRequest();
        FinremCaseDetails details = request.getCaseDetails();
        FinremCaseData data = details.getData();
        setCaseRole(data);

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
        CaseDocument doc = coll.get(0).getValue().getCaseDocuments();
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

        service.copyDocumentOnTheirRespectiveCollectionForSelectedSolicitors(data);
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
                                                                ContestedUploadCaseFilesCollectionType type) {
        return DynamicMultiSelectListElement.builder()
            .label(type.getCcdKey() + " -> " + doc.getDocumentFilename())
            .code(coll.get(0).getId() + "#" + type.getCcdKey())
            .build();
    }

    private DynamicMultiSelectListElement getSelectedParty(CaseRole role) {
        return DynamicMultiSelectListElement.builder()
            .label(role.getValue()).code(role.getValue()).build();
    }

    private List<UploadCaseDocumentCollection> getTestDocument(CaseDocumentType documentType) {
        UploadCaseDocument document = UploadCaseDocument.builder()
            .caseDocuments(TestSetUpUtils.caseDocument())
            .caseDocumentType(documentType)
            .caseDocumentParty(RESPONDENT)
            .caseDocumentOther("No")
            .caseDocumentConfidential(YesOrNo.NO)
            .hearingDetails("UK 1400 hours")
            .caseDocumentFdr(YesOrNo.NO)
            .caseDocumentUploadDateTime(LocalDateTime.now()).build();
        return List.of(UploadCaseDocumentCollection.builder().id(uuid.get()).value(document).build());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(caseData).build())
            .build();
    }

    private static void setCaseRole(FinremCaseData caseData) {
        Organisation testOrg = Organisation.builder().organisationID(TEST_ORG).build();
        caseData.setApplicantOrganisationPolicy(OrganisationPolicy.builder()
            .organisation(testOrg).orgPolicyCaseAssignedRole(APP_SOLICITOR.getValue()).build());
        caseData.setIntervenerOneWrapper(IntervenerOneWrapper.builder().intervener1Organisation(OrganisationPolicy.builder()
            .organisation(testOrg).orgPolicyCaseAssignedRole(INTVR_SOLICITOR_1.getValue()).build()).build());
        caseData.setIntervenerTwoWrapper(IntervenerTwoWrapper.builder().intervener2Organisation(OrganisationPolicy.builder()
            .organisation(testOrg).orgPolicyCaseAssignedRole(INTVR_SOLICITOR_2.getValue()).build()).build());
        caseData.setIntervenerThreeWrapper(IntervenerThreeWrapper.builder().intervener3Organisation(OrganisationPolicy.builder()
            .organisation(testOrg).orgPolicyCaseAssignedRole(INTVR_SOLICITOR_3.getValue()).build()).build());
        caseData.setIntervenerFourWrapper(IntervenerFourWrapper.builder().intervener4Organisation(OrganisationPolicy.builder()
            .organisation(testOrg).orgPolicyCaseAssignedRole(INTVR_SOLICITOR_4.getValue()).build()).build());
    }
}