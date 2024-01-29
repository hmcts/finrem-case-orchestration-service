package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApplicantShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RespondentShareDocumentsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
class ShareSelectedDocumentsAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private final ThreadLocal<UUID> uuid = new ThreadLocal<>();
    @InjectMocks
    private ShareSelectedDocumentsAboutToStartHandler handler;

    @Mock
    private ApplicantShareDocumentsService applicantDocumentsService;
    @Mock
    private RespondentShareDocumentsService respondentShareDocumentsService;
    @Mock
    private IntervenerShareDocumentsService intervenerShareDocumentsService;
    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;
    @Mock
    private AssignCaseAccessService accessService;

    @Test
    void givenContestedCase_whenRequiredEventCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredEventCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredEventTypeIsNotValid_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredConditionSatisfied_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(true));
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsApplicantSolicitor_thenHandlerCanHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()),
            AUTH_TOKEN)).thenReturn(getCaseAssignedUserRolesResource("[APPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(applicantDocumentsService.applicantSourceDocumentList(caseDetails)).thenReturn(getDynamicList(data));
        when(applicantDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(getCaseRoles(data));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertEquals(3, handle.getData().getSolicitorRoleList().getListItems().size());

        verify(applicantDocumentsService).applicantSourceDocumentList(any());
        verify(applicantDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsApplicantSolicitorShouldNotSeeDuplicateRolesAndNonAcceptedRoles() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()),
            AUTH_TOKEN)).thenReturn(getCaseAssignedUserRolesResource("[APPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRoles());
        when(applicantDocumentsService.applicantSourceDocumentList(caseDetails)).thenReturn(getDynamicList(data));
        when(applicantDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(getCaseRoles2(data));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertEquals(4, handle.getData().getSolicitorRoleList().getListItems().size());

        verify(applicantDocumentsService).applicantSourceDocumentList(any());
        verify(applicantDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsRespondentSolicitor_thenHandlerCanHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()),
            AUTH_TOKEN)).thenReturn(getCaseAssignedUserRolesResource("[RESPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(respondentShareDocumentsService.respondentSourceDocumentList(caseDetails)).thenReturn(getDynamicList(data));
        when(respondentShareDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(getCaseRoles(data));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertEquals(3, handle.getData().getSolicitorRoleList().getListItems().size());

        verify(respondentShareDocumentsService).respondentSourceDocumentList(any());
        verify(respondentShareDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsIntervenerSolicitor_thenHandlerCanHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[INTVRSOLICITOR1]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(intervenerShareDocumentsService.intervenerSourceDocumentList(caseDetails, "[INTVRSOLICITOR1]")).thenReturn(getDynamicList(data));
        when(intervenerShareDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(getCaseRoles(data));
        when(applicantDocumentsService.isIntervenerRole(any())).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertEquals(3, handle.getData().getSolicitorRoleList().getListItems().size());

        verify(intervenerShareDocumentsService).intervenerSourceDocumentList(any(), any());
        verify(intervenerShareDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsIntervenerSolicitorNoOtherPartiesDigital__thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[INTVRSOLICITOR1]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(intervenerShareDocumentsService.intervenerSourceDocumentList(caseDetails,
            "[INTVRSOLICITOR1]")).thenReturn(getDynamicList(data));
        when(intervenerShareDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(new DynamicMultiSelectList());
        when(applicantDocumentsService.isIntervenerRole(any())).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertNull(handle.getData().getSolicitorRoleList());
        assertTrue(handle.getErrors().contains("\nThere is/are no party/parties available to share documents."));

        verify(intervenerShareDocumentsService).intervenerSourceDocumentList(any(), any());
        verify(intervenerShareDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsIntervenerSolicitorNoDocumentAvailableToShare_thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[INTVRSOLICITOR1]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(intervenerShareDocumentsService.intervenerSourceDocumentList(caseDetails,
            "[INTVRSOLICITOR1]")).thenReturn(new DynamicMultiSelectList());
        when(applicantDocumentsService.isIntervenerRole(any())).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNull(handle.getData().getSourceDocumentList());
        assertNull(handle.getData().getSolicitorRoleList());
        assertTrue(handle.getErrors().contains("\nThere are no documents available to share."));
        //assertEquals("\nThere is/are no party/parties available to share documents.", handle.getErrors().get(1));

        verify(intervenerShareDocumentsService).intervenerSourceDocumentList(any(), any());
        verify(intervenerShareDocumentsService, times(0)).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServicAppSolicitorNoDocumentAvailableToShare_thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[APPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(applicantDocumentsService.applicantSourceDocumentList(caseDetails)).thenReturn(new DynamicMultiSelectList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNull(handle.getData().getSourceDocumentList());
        assertNull(handle.getData().getSolicitorRoleList());

        assertTrue(handle.getErrors().contains("\nThere are no documents available to share."));

        verify(applicantDocumentsService).applicantSourceDocumentList(any());
        verify(applicantDocumentsService, times(0)).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsAppSolicitorNoOtherPartiesDigital__thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[APPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(applicantDocumentsService.applicantSourceDocumentList(caseDetails)).thenReturn(getDynamicList(data));
        when(applicantDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(new DynamicMultiSelectList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertNull(handle.getData().getSolicitorRoleList());
        assertTrue(handle.getErrors().contains("\nThere is/are no party/parties available to share documents."));

        verify(applicantDocumentsService).applicantSourceDocumentList(any());
        verify(applicantDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsRespondentSolicitorNoDocumentsAvailable_thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[RESPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(respondentShareDocumentsService.respondentSourceDocumentList(caseDetails)).thenReturn(new DynamicMultiSelectList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertNull(handle.getData().getSourceDocumentList());
        assertNull(handle.getData().getSolicitorRoleList());

        assertTrue(handle.getErrors().contains("\nThere are no documents available to share."));

        verify(respondentShareDocumentsService).respondentSourceDocumentList(any());
        verify(respondentShareDocumentsService, times(0)).getOtherSolicitorRoleList(any(), any(), any());
    }

    @Test
    void givenContestedCase_whenInvokedSharedServiceAsRespSolicitorNoOtherPartiesDigital__thenHandlerWillShowMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), AUTH_TOKEN))
            .thenReturn(getCaseAssignedUserRolesResource("[RESPSOLICITOR]"));
        when(accessService.getAllCaseRole(any())).thenReturn(getAllCaseRole());
        when(respondentShareDocumentsService.respondentSourceDocumentList(caseDetails)).thenReturn(getDynamicList(data));
        when(respondentShareDocumentsService.getOtherSolicitorRoleList(any(), any(), any())).thenReturn(new DynamicMultiSelectList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertEquals(10, handle.getData().getSourceDocumentList().getListItems().size());
        assertNull(handle.getData().getSolicitorRoleList());
        assertTrue(handle.getErrors().contains("\nThere is/are no party/parties available to share documents."));

        verify(respondentShareDocumentsService).respondentSourceDocumentList(any());
        verify(respondentShareDocumentsService).getOtherSolicitorRoleList(any(), any(), any());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    private CaseAssignedUserRolesResource getCaseAssignedUserRolesResource(String role) {
        List<CaseAssignedUserRole> list = new ArrayList<>();
        CaseAssignedUserRole userRole = CaseAssignedUserRole.builder().caseDataId("123").userId("user-123").caseRole(role).build();
        list.add(userRole);
        return CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(list).build();
    }

    private List<CaseAssignmentUserRole> getAllCaseRole() {
        List<CaseAssignmentUserRole> allCaseRole = new ArrayList<>();
        CaseAssignmentUserRole userRole0 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[APPSOLICITOR]").build();
        allCaseRole.add(userRole0);
        CaseAssignmentUserRole userRole1 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR1]").build();
        allCaseRole.add(userRole1);
        CaseAssignmentUserRole userRole2 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR2]").build();
        allCaseRole.add(userRole2);
        CaseAssignmentUserRole userRole3 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR3]").build();
        allCaseRole.add(userRole3);
        return allCaseRole;
    }

    private List<CaseAssignmentUserRole> getAllCaseRoles() {
        List<CaseAssignmentUserRole> allCaseRole = new ArrayList<>();
        CaseAssignmentUserRole userRole0 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[APPSOLICITOR]").build();
        allCaseRole.add(userRole0);
        CaseAssignmentUserRole userRole1 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR1]").build();
        allCaseRole.add(userRole1);
        CaseAssignmentUserRole userRole7 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR1]").build();
        allCaseRole.add(userRole7);
        CaseAssignmentUserRole userRole2 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR2]").build();
        allCaseRole.add(userRole2);
        CaseAssignmentUserRole userRole3 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[INTVRSOLICITOR3]").build();
        allCaseRole.add(userRole3);
        CaseAssignmentUserRole userRole4 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[RESPSOLICITOR]").build();
        allCaseRole.add(userRole4);
        CaseAssignmentUserRole userRole5 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[RESPSOLICITOR]").build();
        allCaseRole.add(userRole5);
        CaseAssignmentUserRole userRole6 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[CREATOR]").build();
        allCaseRole.add(userRole6);
        CaseAssignmentUserRole userRole8 = CaseAssignmentUserRole.builder()
            .caseDataId("123").userId("user-123").caseRole("[CASEWORKER]").build();
        allCaseRole.add(userRole8);
        return allCaseRole;
    }

    private static DynamicMultiSelectListElement getSelectedDoc(List<UploadCaseDocumentCollection> coll,
                                                                CaseDocument doc,
                                                                CaseDocumentCollectionType type) {
        return DynamicMultiSelectListElement.builder()
            .label(type.getCcdKey() + " -> " + doc.getDocumentFilename())
            .code(coll.get(0).getId() + "#" + type.getCcdKey())
            .build();
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
        return List.of(UploadCaseDocumentCollection.builder().id(String.valueOf(uuid.get())).uploadCaseDocument(document).build());
    }

    private DynamicMultiSelectList getDynamicList(FinremCaseData data) {
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
        sourceDocumentList.setListItems(List.of(getSelectedDoc(coll, doc, APPLICANT_CORRESPONDENCE_DOC_COLLECTION),
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
        return sourceDocumentList;
    }

    private DynamicMultiSelectList getCaseRoles(FinremCaseData data) {

        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR1]", "[INTVRSOLICITOR1]"));
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR2]", "[INTVRSOLICITOR2]"));
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR3]", "[INTVRSOLICITOR3]"));


        return getRoleList(dynamicListElements, data.getSolicitorRoleList());
    }

    private DynamicMultiSelectList getCaseRoles2(FinremCaseData data) {

        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR1]", "[INTVRSOLICITOR1]"));
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR2]", "[INTVRSOLICITOR2]"));
        dynamicListElements.add(getDynamicMultiSelectListElement("[INTVRSOLICITOR3]", "[INTVRSOLICITOR3]"));
        dynamicListElements.add(getDynamicMultiSelectListElement("[RESPSOLICITOR]", "[RESPSOLICITOR]"));

        return getRoleList(dynamicListElements, data.getSolicitorRoleList());
    }

    private DynamicMultiSelectList getRoleList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                               DynamicMultiSelectList selectedRoles) {
        if (selectedRoles != null) {
            return DynamicMultiSelectList.builder()
                .value(selectedRoles.getValue())
                .listItems(dynamicMultiSelectListElement)
                .build();
        } else {
            return DynamicMultiSelectList.builder()
                .listItems(dynamicMultiSelectListElement)
                .build();
        }
    }

    private DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }
}
