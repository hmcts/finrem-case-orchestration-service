package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateContestedAboutToSubmitHandlerTest {

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
    @Mock
    FeatureToggleService featureToggleService;

    @Before
    public void setup() {
        handler = new SolicitorCreateContestedAboutToSubmitHandler(
            finremCaseDetailsMapper,
            onlineFormDocumentService,
            caseFlagsService,
            idamService);
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void givenContestedCase_whenHandledAndUserIsAdminAndCaseFileViewEnabled_thenReturnExpectedResponseCaseData() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());
        when(idamService.isUserRoleAdmin(anyString())).thenReturn(true);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(anyString(),
            any(FinremCaseDetails.class))).thenReturn(caseDocument());
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        FinremCaseData responseCaseData = handler.handle(callbackRequest, AUTH_TOKEN).getData();

        expectedAdminResponseCaseData(responseCaseData);
    }

    @Test
    public void givenContestedCase_whenHandledAndUserIsNotAdminAndCaseFileViewDisabled_thenReturnExpectedResponseCaseData() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class)))
            .thenReturn(finremCallbackRequest.getCaseDetails());
        when(idamService.isUserRoleAdmin(anyString())).thenReturn(false);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(anyString(),
            any(FinremCaseDetails.class))).thenReturn(caseDocument());
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(false);

        FinremCaseData responseCaseData = handler.handle(callbackRequest, AUTH_TOKEN).getData();

        expectedNonAdminResponseCaseData(responseCaseData);
    }

    private void expectedAdminResponseCaseData(FinremCaseData responseCaseData) {
        assertEquals(YesOrNo.NO, responseCaseData.getCivilPartnership());
        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            responseCaseData.getScheduleOneWrapper().getTypeOfApplication());
        assertEquals(YesOrNo.NO, responseCaseData.getPromptForUrgentCaseQuestion());
        assertNull(responseCaseData.getContactDetailsWrapper().getApplicantRepresented());
        assertEquals(caseDocument(), responseCaseData.getMiniFormA());
        assertEquals(DocumentCategory.APPLICATIONS_FORM_A_OR_A1_OR_B.getDocumentCategoryId(),
            responseCaseData.getUploadAdditionalDocument().get(0).getValue().getAdditionalDocuments().getCategoryId()
        );
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
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId(EventType.UPLOAD_APPROVED_ORDER.getCcdType()).caseDetails(caseDetails).build();
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
