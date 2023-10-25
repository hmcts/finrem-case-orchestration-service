package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class ApprovedConsentOrderSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private ApprovedConsentOrderSubmittedHandler handler;

    @Mock
    private FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Mock
    private DocumentHelper documentHelper;
    private FinremCaseDetails caseDetails;


    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitCallback_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenContestedCaseType_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.APPROVE_ORDER),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenConsentOrderCase_WhenAppAndRespConsentToEmail_AndNoPensionDocs_ThenSendNotifications() {
        FinremCallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(consentOrderAvailableCorresponder).sendCorrespondence(caseDetails);

    }

    @Test
    public void givenConsentOrderCase_WhenAppAndRespConsentToEmail_AndHasPensionDocs_ThenDoNotSendNotifications() {
        FinremCallbackRequest callbackRequest = getConsentedCallbackRequestForConsentOrder();

        when(documentHelper.getPensionDocumentsData(caseDetails.getData())).thenReturn(List.of(CaseDocument.builder().build()));

        handler.handle(callbackRequest, AUTH_TOKEN);

        verifyNoMoreInteractions(consentOrderAvailableCorresponder);

    }

    protected FinremCallbackRequest getConsentedCallbackRequestForConsentOrder() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .solicitorEmail(TEST_SOLICITOR_EMAIL)
                .solicitorName(TEST_SOLICITOR_NAME)
                .solicitorReference(TEST_SOLICITOR_REFERENCE)
                .respondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL)
                .respondentSolicitorName(TEST_RESP_SOLICITOR_NAME)
                .applicantSolicitorConsentForEmails(YesOrNo.YES)
                .respondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE)
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .respSolNotificationsEmailConsent(YesOrNo.YES)
            .divorceCaseNumber(TEST_DIVORCE_CASE_NUMBER)
            .natureApplicationWrapper(NatureApplicationWrapper.builder()
                .natureOfApplication2(List.of(NatureApplication.LUMP_SUM_ORDER,
                    NatureApplication.PERIODICAL_PAYMENT_ORDER,
                    NatureApplication.PENSION_SHARING_ORDER,
                    NatureApplication.PENSION_ATTACHMENT_ORDER,
                    NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
                    NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
                    NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY,
                    NatureApplication.PROPERTY_ADJUSTMENT_ORDER))
                .build()).build();

        caseDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONSENTED)
            .id(12345L)
            .data(caseData)
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

}
