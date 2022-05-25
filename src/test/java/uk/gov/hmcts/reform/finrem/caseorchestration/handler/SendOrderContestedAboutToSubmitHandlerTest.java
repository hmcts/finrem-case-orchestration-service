package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedAboutToSubmitHandlerTest {

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
    private CaseDataService caseDataService;
    @Mock
    private GeneralOrderService generalOrderService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintArgumentCaptor;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    public void givenNoGeneralOrderPresent_whenHandlePrintAndMailGeneralOrderTriggered_thenDocumentsAreNotPrinted() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().remove(GENERAL_ORDER_LATEST_DOCUMENT);

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

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenPrintAndMailHearingDocuments_ThenSendToBulkPrintWhenPaperCase() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL", "AdditionalHearingDocumentURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenHandle_ThenSendToBulkPrintWhenPaperCase_noNextHearing() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenHandle_ThenDoNotSendToBulkPrintWhenDigitalCase() {

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void givenLatestDraftedHearingOrderDocumentIsNotAddedToPack_WhenHandle_ThenPrintApplicantDocuments() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getDocumentLinkAsBulkPrintDocument(any(), eq(LATEST_DRAFT_HEARING_ORDER))).thenReturn(Optional.empty());
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("AdditionalHearingDocumentURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenLatestDraftedHearingOrderDocumentIsNotAddedToPack_WhenHandle_ThenNoNextHearing() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        String notExpectedBulkPrintDocument = "AdditionalHearingDocumentURL";

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }


    @Test
    public void givenLatestAdditionalHearingDocumentIsNotAddedToPack_WhenHandle_ThenPrintApplicantDocuments() {
        when(caseDataService.isContestedPaperApplication(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.empty());

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("HearingOrderBinaryURL", "OtherHearingOrderDocumentsURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenFinalOrderSuccess_WhenHandle_ThenStampFinalOrder() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(caseDocument());

        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(caseDocument(), AUTH_TOKEN);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) response.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(1));
        assertThat(expectedFinalOrderCollection.get(0).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    @Test
    public void givenFinalOrderSuccessWithoutAnyHearingOrder_WhenHandle_ThenStampFinalOrder() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        assertThat(response.getData(), not(hasKey(FINAL_ORDER_COLLECTION)));
    }

    @Test
    public void givenFinalOrderSuccessWithFinalOrder_WhenHandle_ThenStampDocument() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getFinalOrderDocuments(any())).thenReturn(new ArrayList<>(List.of(HearingOrderCollectionData.builder().build())));
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(caseDocument());

        AboutToStartOrSubmitCallbackResponse response =
            sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(caseDocument(), AUTH_TOKEN);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) response.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(2));
        assertThat(expectedFinalOrderCollection.get(1).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    private void mockDocumentHelperToReturnDefaultExpectedDocuments() {
        when(documentHelper.getDocumentLinkAsBulkPrintDocument(any(), eq(LATEST_DRAFT_HEARING_ORDER))).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl("HearingOrderBinaryURL").build()));
        when(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(any(), eq(HEARING_ORDER_OTHER_COLLECTION))).thenReturn(
            singletonList(BulkPrintDocument.builder().binaryFileUrl("OtherHearingOrderDocumentsURL").build()));

        CaseDocument additionalHearingDocument = CaseDocument.builder().documentBinaryUrl("AdditionalHearingDocumentURL").build();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.of(additionalHearingDocument));
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(eq(additionalHearingDocument))).thenReturn(
            BulkPrintDocument.builder().binaryFileUrl(additionalHearingDocument.getDocumentBinaryUrl()).build());

        when(documentHelper.getHearingOrderDocuments(any())).thenReturn(singletonList(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CallbackRequest getEmptyCallbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build())
            .build();

    }
}
