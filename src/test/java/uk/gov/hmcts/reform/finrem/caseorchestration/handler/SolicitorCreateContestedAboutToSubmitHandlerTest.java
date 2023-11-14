package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrgPolicyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private SolicitorCreateContestedAboutToSubmitHandler handler;

    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    OnlineFormDocumentService onlineFormDocumentService;
    @Mock
    CaseFlagsService caseFlagsService;
    @Mock
    IdamService idamService;

    @BeforeEach
    void setup() {
        handler = new SolicitorCreateContestedAboutToSubmitHandler(
            finremCaseDetailsMapper,
            onlineFormDocumentService,
            caseFlagsService,
            idamService,
            new OrgPolicyService());
    }

    @Test
    void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE));
    }

    @Test
    void givenContestedCase_whenHandledAndUserIsAdminAndCaseFileViewEnabled_thenReturnExpectedResponseCaseData() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        finremCallbackRequest.getCaseDetails().getData().getUploadAdditionalDocument().forEach(ad ->
            ad.getValue().getAdditionalDocuments().setCategoryId(
                DocumentCategory.APPLICATIONS_FORM_A_OR_A1_OR_B.getDocumentCategoryId()));
        when(idamService.isUserRoleAdmin(anyString())).thenReturn(true);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(anyString(),
            any(FinremCaseDetails.class))).thenReturn(caseDocument());

        FinremCaseData responseCaseData = handler.handle(finremCallbackRequest, AUTH_TOKEN).getData();

        expectedAdminResponseCaseData(responseCaseData);
    }

    @Test
    void givenContestedCase_whenHandledAndUserIsNotAdminAndCaseFileViewDisabled_thenReturnExpectedResponseCaseData() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        when(idamService.isUserRoleAdmin(anyString())).thenReturn(false);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(anyString(),
            any(FinremCaseDetails.class))).thenReturn(caseDocument());

        FinremCaseData responseCaseData = handler.handle(finremCallbackRequest, AUTH_TOKEN).getData();

        expectedNonAdminResponseCaseData(responseCaseData);
    }

    void expectedAdminResponseCaseData(FinremCaseData responseCaseData) {
        assertEquals(YesOrNo.NO, responseCaseData.getCivilPartnership());
        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            responseCaseData.getScheduleOneWrapper().getTypeOfApplication());
        assertEquals(YesOrNo.NO, responseCaseData.getPromptForUrgentCaseQuestion());
        assertNull(responseCaseData.getContactDetailsWrapper().getApplicantRepresented());
        assertEquals(caseDocument(), responseCaseData.getMiniFormA());
        assertEquals(DocumentCategory.APPLICATIONS_FORM_A_OR_A1_OR_B.getDocumentCategoryId(),
            responseCaseData.getUploadAdditionalDocument().get(0).getValue().getAdditionalDocuments().getCategoryId()
        );
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            responseCaseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            responseCaseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }

    private void expectedNonAdminResponseCaseData(FinremCaseData responseCaseData) {
        assertEquals(YesOrNo.NO, responseCaseData.getCivilPartnership());
        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            responseCaseData.getScheduleOneWrapper().getTypeOfApplication());
        assertEquals(YesOrNo.NO, responseCaseData.getPromptForUrgentCaseQuestion());
        assertEquals(YesOrNo.YES, responseCaseData.getContactDetailsWrapper().getApplicantRepresented());
        assertEquals(caseDocument(), responseCaseData.getMiniFormA());
        assertNull(responseCaseData.getUploadAdditionalDocument().get(0)
            .getValue().getAdditionalDocuments().getCategoryId());
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(),
            responseCaseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(),
            responseCaseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
    }


    private FinremCallbackRequest buildFinremCallbackRequest() {
        ScheduleOneWrapper wrapper = ScheduleOneWrapper.builder().typeOfApplication(
            Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS).build();
        UploadAdditionalDocument uploadAdditionalDocument = UploadAdditionalDocument.builder().additionalDocuments(caseDocument()).build();
        UploadAdditionalDocumentCollection collection = UploadAdditionalDocumentCollection.builder().value(uploadAdditionalDocument).build();
        FinremCaseData caseData = FinremCaseData.builder().civilPartnership(YesOrNo.NO)
            .promptForUrgentCaseQuestion(YesOrNo.NO).uploadAdditionalDocument(List.of(collection))
            .scheduleOneWrapper(wrapper).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }
}
