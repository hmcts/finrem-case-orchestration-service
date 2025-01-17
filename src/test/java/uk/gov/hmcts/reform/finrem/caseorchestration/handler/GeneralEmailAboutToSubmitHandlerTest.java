package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralEmailDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.generalemail.GeneralEmailWrapperUtils;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralEmailAboutToSubmitHandlerTest {

    private GeneralEmailAboutToSubmitHandler handler;

    @Mock
    private GeneralEmailService generalEmailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void init() {
        GeneralEmailDocumentCategoriser categoriser = new GeneralEmailDocumentCategoriser(featureToggleService);
        handler =  new GeneralEmailAboutToSubmitHandler(finremCaseDetailsMapper,
            notificationService,
            generalEmailService,
            genericDocumentService,
            categoriser);
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
                Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CREATE_GENERAL_EMAIL),
                Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CREATE_GENERAL_EMAIL)
        );
    }

    @Test
    void givenACcdCallbackCallbackGeneralEmailAboutToSubmitHandler_WhenHandle_thenSendEmail() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(true);
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendConsentGeneralEmail(any(FinremCaseDetails.class), anyString());
        verify(generalEmailService).storeGeneralEmail(any(FinremCaseDetails.class));
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForApplicantRecipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        callbackRequest.getCaseDetails().getData().getContactDetailsWrapper().setApplicantEmail("test@gmail.com");
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_APPLICANT);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForRespondentRecipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        callbackRequest.getCaseDetails().getData().getContactDetailsWrapper().setRespondentSolicitorEmail("test@gmail.com");
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_RESPONDENT);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForIntervener1Recipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        setIntervenerEmail(callbackRequest.getCaseDetails().getData().getIntervenerOne());
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_1);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForIntervener2Recipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        setIntervenerSolEmail(callbackRequest.getCaseDetails().getData().getIntervenerTwo());
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_2);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForIntervener3Recipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        setIntervenerEmail(callbackRequest.getCaseDetails().getData().getIntervenerThree());
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_3);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForIntervener4Recipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        setIntervenerSolEmail(callbackRequest.getCaseDetails().getData().getIntervenerFour());
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_INTERVENER_4);
    }

    @Test
    void givenContestedCallbackRequest_whenHandledForUnrecognisedRecipient_thenDocumentIsCategorised() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(false);
        verifyDocumentCategory(callbackRequest, DocumentCategory.COURT_CORRESPONDENCE_OTHER);
    }

    @Test
    void givenConsentedCallbackRequest_whenHandledForRecipient_thenNotCategorised() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(true);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNull(response.getData().getGeneralEmailWrapper().getGeneralEmailCollection().get(0)
                .getValue().getGeneralEmailUploadedDocument().getCategoryId());
    }

    /**
     * Verifies that the method {@code setGeneralEmailValuesToNull} in {@link GeneralEmailWrapperUtils}
     * is invoked so that GeneralEmailWrapper details are cleared for both consented and contested cases.
     * Parameterised tests a little awkward owing to their static nature.  So instead iterate over a list
     * containing a single consented and contested callback request, to check the method is called for both case types.
     */
    @Test
    void testSetGeneralEmailValuesToNullIsUsed() {
        FinremCallbackRequest consentedRequest = buildFinremCallbackRequest(true);
        FinremCallbackRequest contestedRequest = buildFinremCallbackRequest(false);

        for (FinremCallbackRequest callbackRequest : List.of(contestedRequest, consentedRequest)) {
            FinremCaseDetails consentedCaseDetails = callbackRequest.getCaseDetails();
            try (MockedStatic<GeneralEmailWrapperUtils> mockedStatic = mockStatic(GeneralEmailWrapperUtils.class)) {
                handler.handle(callbackRequest, AUTH_TOKEN);
                mockedStatic.verify(() -> GeneralEmailWrapperUtils.setGeneralEmailValuesToNull(consentedCaseDetails),
                        times(1));
            }
        }
    }

    private void verifyDocumentCategory(FinremCallbackRequest callbackRequest, DocumentCategory category) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getData().getGeneralEmailWrapper().getGeneralEmailCollection().get(0)
                .getValue().getGeneralEmailUploadedDocument().getCategoryId(),
            is(category.getDocumentCategoryId()));
    }

    private void setIntervenerSolEmail(IntervenerWrapper wrapper) {
        wrapper.setIntervenerSolEmail("test@gmail.com");
    }

    private void setIntervenerEmail(IntervenerWrapper wrapper) {
        wrapper.setIntervenerEmail("test@gmail.com");
    }

    private FinremCallbackRequest buildFinremCallbackRequest(boolean isConsented) {
        CaseDocument document = CaseDocument.builder().build();
        CaseType caseType = isConsented ? CaseType.CONSENTED : CaseType.CONTESTED;
        GeneralEmailHolder holder = GeneralEmailHolder.builder().generalEmailRecipient("test@gmail.com")
            .generalEmailUploadedDocument(document).build();
        List<GeneralEmailCollection> collection = List.of(GeneralEmailCollection.builder().value(holder).build());
        FinremCaseData caseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailRecipient("Test")
                .generalEmailCreatedBy("Test")
                .generalEmailBody("body")
                .generalEmailUploadedDocument(document)
                .generalEmailCollection(collection)
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(caseType).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_EMAIL).caseDetails(caseDetails).build();
    }
}