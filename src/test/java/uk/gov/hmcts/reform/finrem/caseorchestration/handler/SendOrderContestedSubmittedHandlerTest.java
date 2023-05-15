package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedSubmittedHandlerTest {
    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();
    private static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private SendOrderContestedSubmittedHandler sendOrderContestedSubmittedHandler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private CcdService ccdService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService genericDocumentService;


    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    private void setupData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
    }


    @Test
    void givenPrepareForHearingPostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);

        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("anyBinaryURL").build());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.PREPARE_FOR_HEARING.getCcdType());
    }

    @Test
    void givenClosePostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);

        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.CLOSE);
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("anyBinaryURL").build());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.CLOSE.getCcdType());
    }

    @Test
    void givenOrderSentPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);

        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("anyBinaryURL").build());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.SEND_ORDER.getCcdType());
    }

    @Test
    void givenNoPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.NONE);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any(), any());
    }

    @Test
    void givenAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmail() {
        when(generalOrderService.getParties(any(FinremCaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setFinalOrderCollection(List.of(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendContestOrderApprovedEmailApplicant(any(FinremCaseDetails.class));
        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any(FinremCaseDetails.class));
    }

    @Test
    void givenNotAgreedToReceiveEmails_WhenHandle_ThenDoNotSendContestOrderApprovedEmail() {
        sendOrderContestedSubmittedHandler.handle(buildCallbackRequest(), AUTH_TOKEN);
        verifyNoInteractions(notificationService);
    }

    @Test
    void givenRespAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmailToRespondent() {
        when(generalOrderService.getParties(any(FinremCaseDetails.class))).thenReturn(List.of(CaseRole.RESP_SOLICITOR.getValue()));
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setFinalOrderCollection(List.of(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendContestOrderApprovedEmailRespondent(any(CaseDetails.class));
        verify(notificationService, never()).sendContestOrderApprovedEmailApplicant(any(CaseDetails.class));
        verify(notificationService, never()).sendContestOrderApprovedEmailApplicant(any(CaseDetails.class));
    }

    @Test
    void givenRespNotAgreedToReceiveEmails_WhenHandle_ThenDoNotSendContestOrderApprovedEmailToRespondent() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);

        FinremCaseData data = caseDetails.getData();
        data.setFinalOrderCollection(List.of(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));

        when(documentHelper.getCaseDocumentAsBulkPrintDocument(any()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("anyBinaryURL").build());

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService, never()).isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class));
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list =  new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getValue())
                .label(CaseRole.APP_SOLICITOR.getValue())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue(), CaseRole.INTVR_SOLICITOR_1.getValue(), CaseRole.INTVR_SOLICITOR_2.getValue(),
            CaseRole.INTVR_SOLICITOR_3.getValue(), CaseRole.INTVR_SOLICITOR_4.getValue());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}