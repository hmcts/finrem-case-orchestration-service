package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ADDITIONAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_LIST;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedAboutToSubmitHandlerTest {

    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();
    @InjectMocks
    private SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private GeneralOrderService generalOrderService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintArgumentCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        callbackRequest.getCaseDetails().getData().remove(GENERAL_ORDER_LATEST_DOCUMENT);

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(callbackRequest.getCaseDetails(), AUTH_TOKEN, EMPTY_LIST);
        verify(bulkPrintService, never()).printRespondentDocuments(callbackRequest.getCaseDetails(), AUTH_TOKEN, EMPTY_LIST);
    }

    @Test
    public void givenShouldPrintAppAndResp_whenPrintAndMailGeneralOrderTriggered_thenBothAppAndRespPacksPrinted() {
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(any(), any()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl(TestSetUpUtils.BINARY_URL).build());

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();

        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    public void givenShouldNotPrintPackForApplicant_whenPrintAndMailGeneralOrderTriggered_thenOnlyRespondentPacksIsPrinted() {
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.RESP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, never()).printApplicantDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    public void givenShouldThrowErrorAndNotPrintPackForResondent_whenPrintAndMailGeneralOrderTriggeredAndAddressIsNotPresent() {
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        doThrow(new InvalidCaseDataException(BAD_REQUEST.value(), "CCD address field applicantAddress"
            + " needs to contain both first line of address and postcode")).when(bulkPrintService)
            .printApplicantDocuments(any(CaseDetails.class), any(), any());

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetailsWithoutSolicitorAddress()).build();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0), is("CCD address field applicantAddress"
            + " needs to contain both first line of address and postcode"));
        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenPrintAndMailHearingDocuments_ThenSendToBulkPrintWhenPaperCase() {
        CallbackRequest request = getEmptyCallbackRequest();
        Map<String, Object> data = request.getCaseDetails().getData();
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue()));
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(data, AUTH_TOKEN))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("GeneralOrderBinaryURL").build());
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

        data.put(ORDER_LIST, selectedDocs);
        when(generalOrderService.getList(any())).thenReturn(selectedDocs);
        data.put(GENERAL_ORDER_LATEST_DOCUMENT, TestSetUpUtils.caseDocument());
        when(documentHelper.getHearingOrderDocuments(data))
            .thenReturn(List.of(HearingOrderCollectionData.builder()
                .id(uuid)
                .hearingOrderDocuments(HearingOrderDocument.builder()
                    .uploadDraftDocument(caseDocument())
                    .build())
                .build()));

        sendOrderContestedAboutToSubmitHandler.handle(request, AUTH_TOKEN);

        verify(bulkPrintService, times(2)).printApplicantDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, times(2)).printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(documentHelper).getHearingDocumentsAsBulkPrintDocuments(any(), any());
    }

    @Test
    public void givenAllHearingDocumentsArePresent_WhenHandle_ThenSendToBulkPrintWhenPaperCase_noNextHearing() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), any(), bulkPrintArgumentCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), any(), any());

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
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue()));
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        CallbackRequest emptyCallbackRequest = getEmptyCallbackRequest();
        Map<String, Object> data = emptyCallbackRequest.getCaseDetails().getData();
        data.put(GENERAL_ORDER_LATEST_DOCUMENT, TestSetUpUtils.caseDocument());
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

        data.put(ORDER_LIST, selectedDocs);
        data.put(CONTESTED_ORDER_APPROVED_COVER_LETTER, caseDocument());

        when(documentHelper.convertToCaseDocumentIfObjNotNull(any())).thenReturn(caseDocument());
        when(generalOrderService.getList(any())).thenReturn(selectedDocs);
        when(documentHelper.getDocumentLinkAsBulkPrintDocument(data, CONTESTED_ORDER_APPROVED_COVER_LETTER)).thenReturn(
            Optional.of(BulkPrintDocument.builder().binaryFileUrl("CoversheetBINARYURL").build()));
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument()))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("HearingBINARYURL").build());
        when(genericDocumentService
            .convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.stampDocument(any(), any(), any())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = sendOrderContestedAboutToSubmitHandler.handle(emptyCallbackRequest, AUTH_TOKEN);
        List<HearingOrderCollectionData> finalColl = getCollection(response.getData().get(
            FINAL_ORDER_COLLECTION));
        assertEquals(1, finalColl.size());

        List<HearingOrderCollectionData> finalOtherColl = getCollection(response.getData().get(
            FINAL_ADDITIONAL_ORDER_COLLECTION));
        assertEquals(1, finalOtherColl.size());

        verify(bulkPrintService, times(2))
            .printApplicantDocuments(any(CaseDetails.class), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("CoversheetBINARYURL", "HearingBINARYURL",
            "HearingBINARYURL", "AdditionalHearingDocumentURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(1).stream().map(BulkPrintDocument::getBinaryFileUrl).toList(),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenLatestDraftedHearingOrderDocumentIsNotAddedToPack_WhenHandle_ThenNoNextHearing() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(false);
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);

        sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), any(), bulkPrintArgumentCaptor.capture());

        String notExpectedBulkPrintDocument = "CoversheetBINARYURL";

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            not(contains(notExpectedBulkPrintDocument)));
    }


    @Test
    public void givenLatestAdditionalHearingDocumentIsNotAddedToPack_WhenHandle_ThenPrintApplicantDocuments() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(documentHelper.hasAnotherHearing(any())).thenReturn(true);
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.empty());

        CallbackRequest emptyCallbackRequest = getEmptyCallbackRequest();
        CaseDetails caseDetails = emptyCallbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(data, AUTH_TOKEN))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("GeneralOrderBinaryURL").build());
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

        data.put(ORDER_LIST, selectedDocs);
        when(generalOrderService.getList(any())).thenReturn(selectedDocs);
        data.put(GENERAL_ORDER_LATEST_DOCUMENT, TestSetUpUtils.caseDocument());
        when(documentHelper.getHearingOrderDocuments(data))
            .thenReturn(List.of(HearingOrderCollectionData.builder()
                .id(uuid)
                .hearingOrderDocuments(HearingOrderDocument.builder()
                    .uploadDraftDocument(caseDocument())
                    .build())
                .build()));


        sendOrderContestedAboutToSubmitHandler.handle(emptyCallbackRequest, AUTH_TOKEN);

        verify(bulkPrintService, times(2)).printApplicantDocuments(any(CaseDetails.class), any(), bulkPrintArgumentCaptor.capture());

        List<String> expectedBulkPrintDocuments = asList("GeneralOrderBinaryURL");

        assertThat(bulkPrintArgumentCaptor.getAllValues().get(0).stream().map(BulkPrintDocument::getBinaryFileUrl).collect(Collectors.toList()),
            containsInAnyOrder(expectedBulkPrintDocuments.toArray()));
    }

    @Test
    public void givenFinalOrderSuccess_WhenHandle_ThenStampFinalOrder() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue()));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP)))
            .thenReturn(caseDocument());
        CallbackRequest emptyCallbackRequest = getEmptyCallbackRequest();
        CaseDetails caseDetails = emptyCallbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(data, AUTH_TOKEN))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("GeneralOrderBinaryURL").build());
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

        data.put(ORDER_LIST, selectedDocs);
        when(generalOrderService.getList(any())).thenReturn(selectedDocs);
        data.put(GENERAL_ORDER_LATEST_DOCUMENT, TestSetUpUtils.caseDocument());
        when(documentHelper.getHearingOrderDocuments(data))
            .thenReturn(List.of(HearingOrderCollectionData.builder()
                .id(uuid)
                .hearingOrderDocuments(HearingOrderDocument.builder()
                    .uploadDraftDocument(caseDocument())
                    .build())
                .build()));

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            sendOrderContestedAboutToSubmitHandler.handle(emptyCallbackRequest, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(caseDocument(), AUTH_TOKEN, StampType.FAMILY_COURT_STAMP);

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) response.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(1));
        assertThat(expectedFinalOrderCollection.get(0).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    @Test
    public void givenFinalOrderSuccessWithoutAnyHearingOrder_WhenHandle_ThenStampFinalOrder() {

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            sendOrderContestedAboutToSubmitHandler.handle(getEmptyCallbackRequest(), AUTH_TOKEN);

        assertThat(response.getData(), not(hasKey(FINAL_ORDER_COLLECTION)));
    }

    @Test
    public void givenFinalOrderSuccessWithFinalOrder_WhenHandle_ThenStampDocument() {
        mockDocumentHelperToReturnDefaultExpectedDocuments();
        CallbackRequest request = getEmptyCallbackRequest();
        Map<String, Object> data = request.getCaseDetails().getData();
        when(generalOrderService.getPartyList(any(CaseDetails.class))).thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue()));
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(false);
        when(generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(data, AUTH_TOKEN))
            .thenReturn(BulkPrintDocument.builder().binaryFileUrl("GeneralOrderBinaryURL").build());
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

        data.put(ORDER_LIST, selectedDocs);
        when(generalOrderService.getList(any())).thenReturn(selectedDocs);
        data.put(GENERAL_ORDER_LATEST_DOCUMENT, TestSetUpUtils.caseDocument());
        when(documentHelper.getHearingOrderDocuments(data))
            .thenReturn(List.of(HearingOrderCollectionData.builder()
                .id(uuid)
                .hearingOrderDocuments(HearingOrderDocument.builder()
                    .uploadDraftDocument(caseDocument())
                    .build())
                .build()));

        when(documentHelper.getFinalOrderDocuments(any())).thenReturn(new ArrayList<>(List.of(HearingOrderCollectionData.builder().build())));
        when(genericDocumentService.stampDocument(isA(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP)))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            sendOrderContestedAboutToSubmitHandler.handle(request, AUTH_TOKEN);

        verify(genericDocumentService).stampDocument(eq(caseDocument()), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP));

        List<HearingOrderCollectionData> expectedFinalOrderCollection =
            (List<HearingOrderCollectionData>) response.getData().get(FINAL_ORDER_COLLECTION);

        assertThat(expectedFinalOrderCollection, hasSize(2));
        assertThat(expectedFinalOrderCollection.get(1).getHearingOrderDocuments().getUploadDraftDocument(), is(caseDocument()));
    }

    private void mockDocumentHelperToReturnDefaultExpectedDocuments() {
        when(documentHelper.getStampType(anyMap())).thenReturn(StampType.FAMILY_COURT_STAMP);

        CaseDocument additionalHearingDocument = CaseDocument.builder().documentBinaryUrl("AdditionalHearingDocumentURL").build();
        when(documentHelper.getLatestAdditionalHearingDocument(any())).thenReturn(Optional.of(additionalHearingDocument));
        when(documentHelper.getCaseDocumentAsBulkPrintDocument(eq(additionalHearingDocument))).thenReturn(
            BulkPrintDocument.builder().binaryFileUrl(additionalHearingDocument.getDocumentBinaryUrl()).build());

        when(documentHelper.getHearingOrderDocuments(any())).thenReturn(singletonList(HearingOrderCollectionData.builder()
            .id(UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString())
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

    private CaseDetails generalOrderContestedCaseDetailsWithoutSolicitorAddress() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            caseDetails.getData().remove("respondentAddress");
            caseDetails.getData().remove("applicantAddress");
            caseDetails.getData().remove("solicitorAddress");
            return caseDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<HearingOrderCollectionData> getCollection(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<>() {
        });
    }
}
