package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedAboutToSubmitHandlerTest extends BaseHandlerTest {

    public static final String PATH = "/fixtures/general-order-contested.json";

    @InjectMocks
    private SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private PaperNotificationService paperNotificationService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private GeneralOrderService generalOrderService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintArgumentCaptor;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    @Test
    public void givenNoGeneralOrderPresent_whenHandlePrintAndMailGeneralOrderTriggered_thenDocumentsAreNotPrinted() {
        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldPrintAppAndResp_whenPrintAndMailGeneralOrderTriggered_thenBothAppAndRespPacksPrinted() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(any()))
            .thenReturn(BulkPrintDocument.builder().build());

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(
            Document.builder().build());
        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, atLeastOnce()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, atLeastOnce()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(
            Document.builder().build());
        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, atLeastOnce()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenPrintAndMailHearingDocuments_ThenSendToBulkPrintWhenPaperCase() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();

        sendOrderContestedAboutToSubmitHandler.handle(getCallbackRequestFromResource(PATH), AUTH_TOKEN);

        verify(bulkPrintService, atLeastOnce()).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService, atLeastOnce()).printRespondentDocuments(any(), any(), any());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL",
            "AdditionalHearingDocumentURL",
            "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getValue().stream()
                .map(BulkPrintDocument::getBinaryFileUrl)
                .collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenHandle_ThenSendToBulkPrintWhenPaperCase_noNextHearing() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);

        sendOrderContestedAboutToSubmitHandler.handle(getCallbackRequestFromResource(PATH), AUTH_TOKEN);

        verify(bulkPrintService, atLeastOnce()).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService, atLeastOnce()).printRespondentDocuments(any(), any(), any());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getValue().stream()
                .map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenHandle_ThenDoNotSendToBulkPrintWhenDigitalCase() {
        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void givenLatestDraftedHearingOrderDocumentIsNotAddedToPack_WhenHandle_ThenPrintApplicantDocuments() {
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getDocumentAsBulkPrintDocument(eq(latestDraftHearingOrder()))).thenReturn(Optional.empty());
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);
        callbackRequest.getCaseDetails().getCaseData().setOrderApprovedCoverLetter(null);

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("AdditionalHearingDocumentURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getValue().stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenLatestDraftedHearingOrderDocumentIsNotAddedToPack_WhenHandle_ThenNoNextHearing() {
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getValue().stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }


    @Test
    public void givenLatestAdditionalHearingDocumentIsNotAddedToPack_WhenHandle_ThenPrintApplicantDocuments() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getLatestAdditionalHearingDocument(isA(FinremCaseData.class))).thenReturn(Optional.empty());

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getValue().stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenFinalOrderSuccess_WhenHandle_ThenStampFinalOrder() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(genericDocumentService.stampDocument(isA(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(newDocument());

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);

        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(uploadHearingOrderLatestDocument(), AUTH_TOKEN);

        List<DirectionOrderCollection> expectedFinalOrderCollection = response.getData().getFinalOrderCollection();

        assertThat(expectedFinalOrderCollection, hasSize(1));
        assertThat(expectedFinalOrderCollection.get(0).getValue().getUploadDraftDocument(), is(newDocument()));
    }

    @Test
    public void givenFinalOrderSuccessWithoutAnyHearingOrder_WhenHandle_ThenStampFinalOrder() {
        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        assertThat(response.getData().getFinalOrderCollection(), nullValue());
    }

    @Test
    public void givenFinalOrderSuccessWithFinalOrder_WhenHandle_ThenStampDocument() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(genericDocumentService.stampDocument(isA(Document.class), eq(AUTH_TOKEN))).thenReturn(newDocument());

        CallbackRequest callbackRequest = getCallbackRequestFromResource(PATH);
        callbackRequest.getCaseDetails().getCaseData().getGeneralOrderWrapper().setGeneralOrderLatestDocument(null);
        List<DirectionOrderCollection> finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(orderApprovedCoverLetter()).build())
            .build());
        callbackRequest.getCaseDetails().getCaseData().setFinalOrderCollection(finalOrderCollection);

        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(uploadHearingOrderLatestDocument(), AUTH_TOKEN);

        List<DirectionOrderCollection> expectedFinalOrderCollection =
            response.getData().getFinalOrderCollection();

        assertThat(expectedFinalOrderCollection, hasSize(2));
        assertThat(expectedFinalOrderCollection.get(1).getValue().getUploadDraftDocument(), is(newDocument()));
    }

    private void mockDocumentHelperToReturnDefaultExpectedDocuments() {

        when(documentHelper.getDocumentAsBulkPrintDocument(eq(orderApprovedCoverLetter()))).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl("HearingOrderBinaryURL").build()));

        when(documentHelper.getDocumentsAsBulkPrintDocuments(eq(List.of(hearingOtherDocument())))).thenReturn(
            List.of(BulkPrintDocument.builder().binaryFileUrl("OtherHearingOrderDocumentsURL").build()));

        Document additionalHearingDocument = Document.builder().binaryUrl("AdditionalHearingDocumentURL").build();
        when(documentHelper.getLatestAdditionalHearingDocument(isA(FinremCaseData.class)))
            .thenReturn(Optional.of(additionalHearingDocument));

        when(documentHelper.getDocumentAsBulkPrintDocument(eq(additionalHearingDocument))).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl(additionalHearingDocument.getBinaryUrl()).build()));
    }

    private CallbackRequest getEmptyCallbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(FinremCaseDetails.builder().caseData(FinremCaseData.builder()
                .ccdCaseType(CaseType.CONTESTED)
                .build()).build())
            .build();

    }

    private Document latestDraftHearingOrder() {
        return Document.builder()
            .url("http://dm-store/lhjbyuivu87y989hijbb")
            .binaryUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .filename("generalOrder.pdf")
            .build();
    }

    private Document orderApprovedCoverLetter() {
        return Document.builder()
            .url("http://dm-store/lhjbyuivu87y989hijbb")
            .binaryUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .filename("Other.pdf")
            .build();
    }

    private Document hearingOtherDocument() {
        return Document.builder()
            .url("http://dm-store/lhjbyuivu87y989hijbb")
            .binaryUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .filename("hearingOrderOtherDocuments.pdf")
            .build();
    }

    private Document uploadHearingOrderLatestDocument() {
        return Document.builder()
            .url("http://dm-store/lhjbyuivu87y989hijbb")
            .binaryUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .filename("uploadHearingOrder.pdf")
            .build();
    }
}
