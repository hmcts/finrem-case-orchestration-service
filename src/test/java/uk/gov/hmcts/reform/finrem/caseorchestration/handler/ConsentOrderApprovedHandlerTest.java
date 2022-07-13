package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.List;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@RunWith(MockitoJUnitRunner.class)
public class ConsentOrderApprovedHandlerTest extends BaseServiceTest {

    @Mock private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock private GenericDocumentService genericDocumentService;
    @Mock private ConsentOrderPrintService consentOrderPrintService;
    @Mock private NotificationService notificationService;
    @Mock private FeatureToggleService featureToggleService;

    @InjectMocks
    private ConsentOrderApprovedHandler consentOrderApprovedHandler;

    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        callbackRequest = getContestedNewCallbackRequest();
    }

    @Test
    public void givenValidData_whenHandleConsentOrderApproved_thenHandle() {
        setUpHandleConsentOrderApprovedMockContext();
        setCallbackRequestDetails();

        AboutToStartOrSubmitCallbackResponse response =
            consentOrderApprovedHandler.handleConsentOrderApproved(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();

        assertThat(caseData.getApprovedOrderCollection(), hasSize(1));
        assertEquals(caseData.getApprovedOrderCollection().get(0).getValue().getConsentOrder(), newDocument());

        verify(consentOrderApprovedDocumentService, atLeastOnce()).stampPensionDocuments(any(), eq(AUTH_TOKEN));
        verify(consentOrderApprovedDocumentService, times(1))
            .generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(1)).annexStampDocument(isA(Document.class), eq(AUTH_TOKEN));

    }

    @Test
    public void givenValidDataAndEmptyPensionCollection_whenHandleConsentOrderApproved_thenHandle() {
        setUpHandleConsentOrderApprovedEmptyPensionCollectionMockContext();
        setCallbackRequestDetailsNoPensionDocs();

        AboutToStartOrSubmitCallbackResponse response =
            consentOrderApprovedHandler.handleConsentOrderApproved(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();

        assertThat(caseData.getApprovedOrderCollection(), hasSize(1));
        assertEquals(caseData.getApprovedOrderCollection().get(0).getValue().getConsentOrder(), newDocument());
        assertEquals(caseData.getState(), ConsentedStatus.CONSENT_ORDER_MADE.getId());

        verify(consentOrderApprovedDocumentService, times(1))
            .generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, times(1)).annexStampDocument(isA(Document.class), eq(AUTH_TOKEN));
        verify(consentOrderPrintService, times(1))
            .sendConsentOrderToBulkPrint(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(notificationService, times(1))
            .sendConsentOrderAvailableCtscEmail(isA(FinremCaseDetails.class));
        verify(notificationService, times(1))
            .sendConsentOrderAvailableEmailToApplicantSolicitor(isA(FinremCaseDetails.class));
        verify(notificationService, times(1))
            .sendConsentOrderAvailableEmailToRespondentSolicitor(isA(FinremCaseDetails.class));
    }

    @Test
    public void givenValidDataAndNoLatestConsentOrder_whenHandleConsentOrderApproved_thenReturn() {
        setCallbackRequestDetails();
        callbackRequest.getCaseDetails().getCaseData().setLatestConsentOrder(null);

        consentOrderApprovedHandler.handleConsentOrderApproved(callbackRequest, AUTH_TOKEN);

        verify(consentOrderApprovedDocumentService, never())
            .generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(genericDocumentService, never()).annexStampDocument(isA(Document.class), eq(AUTH_TOKEN));
        verify(consentOrderPrintService, never())
            .sendConsentOrderToBulkPrint(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(notificationService, never())
            .sendConsentOrderAvailableCtscEmail(isA(FinremCaseDetails.class));
        verify(notificationService, never())
            .sendConsentOrderAvailableEmailToApplicantSolicitor(isA(FinremCaseDetails.class));
        verify(notificationService, never())
            .sendConsentOrderAvailableEmailToRespondentSolicitor(isA(FinremCaseDetails.class));
    }

    @Test
    public void givenValidData_whenHandleConsentInContestConsentOrderApproved_thenHandle() {
        setCallbackRequestDetails();

        AboutToStartOrSubmitCallbackResponse response =
            consentOrderApprovedHandler.handleConsentInContestConsentOrderApproved(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(caseData, callbackRequest.getCaseDetails().getCaseData());

        verify(consentOrderApprovedDocumentService, times(1))
            .stampAndPopulateContestedConsentApprovedOrderCollection(isA(FinremCaseData.class), eq(AUTH_TOKEN));
        verify(consentOrderApprovedDocumentService, times(1))
            .generateAndPopulateConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    public void givenValidData_whenHandleConsentInContestSendOrder_thenHandle() {
        setCallbackRequestDetails();

        AboutToStartOrSubmitCallbackResponse response =
            consentOrderApprovedHandler.handleConsentInContestSendOrder(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(caseData, callbackRequest.getCaseDetails().getCaseData());

        verify(consentOrderPrintService, times(1))
            .sendConsentOrderToBulkPrint(isA(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    private void setUpHandleConsentOrderApprovedMockContext() {
        when(consentOrderApprovedDocumentService
            .generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(newDocument());
        when(genericDocumentService.annexStampDocument(isA(Document.class), eq(AUTH_TOKEN))).thenReturn(newDocument());
        when(consentOrderApprovedDocumentService.stampPensionDocuments(any(), any())).thenReturn(getPensionDocs());
    }

    private void setUpHandleConsentOrderApprovedEmptyPensionCollectionMockContext() {
        when(consentOrderApprovedDocumentService
            .generateApprovedConsentOrderLetter(isA(FinremCaseDetails.class), eq(AUTH_TOKEN))).thenReturn(newDocument());
        when(genericDocumentService.annexStampDocument(isA(Document.class), eq(AUTH_TOKEN))).thenReturn(newDocument());
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
    }

    private List<PensionTypeCollection> getPensionDocs() {
        return List.of(
            PensionTypeCollection.builder()
                .value(PensionType.builder()
                    .typeOfDocument(PensionDocumentType.FORM_PPF1)
                    .uploadedDocument(newDocument())
                    .build())
                .build());
    }

    private void setCallbackRequestDetails() {
        callbackRequest.getCaseDetails().getCaseData().setCcdCaseType(CaseType.CONTESTED);
        callbackRequest.getCaseDetails().getCaseData().setLatestConsentOrder(newDocument());
        callbackRequest.getCaseDetails().getCaseData().getContactDetailsWrapper()
            .setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        callbackRequest.getCaseDetails().getCaseData().setPensionCollection(getPensionDocs());
    }

    private void setCallbackRequestDetailsNoPensionDocs() {
        callbackRequest.getCaseDetails().getCaseData().setCcdCaseType(CaseType.CONTESTED);
        callbackRequest.getCaseDetails().getCaseData().setLatestConsentOrder(newDocument());
        callbackRequest.getCaseDetails().getCaseData().getContactDetailsWrapper()
            .setApplicantSolicitorConsentForEmails(YesOrNo.YES);
        callbackRequest.getCaseDetails().getCaseData().setRespSolNotificationsEmailConsent(YesOrNo.YES);
    }
}
