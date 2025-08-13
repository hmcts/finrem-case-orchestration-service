package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderAboutToSubmitHandlerTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";

    private JudgeDraftOrderAboutToSubmitHandler handler;
    @Mock
    private HearingOrderService hearingOrderService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Mock
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;
    @Mock
    private DocumentWarningsHelper documentWarningsHelper;
    @Spy
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        handler = new JudgeDraftOrderAboutToSubmitHandler(
                finremCaseDetailsMapper,
            hearingOrderService,
            genericDocumentService,
            contestedOrderApprovedLetterService,
            uploadedDraftOrderCategoriser,
            documentWarningsHelper
        );
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenDraftOrderUploaded_whenDocumentWarningDetected_thenPopulateWarnings() {
        //No additional Documents
        FinremCallbackRequest callbackRequest = setupTestData(Collections.emptyList());

        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warnings 1"));

        assertThat(handler.handle(callbackRequest, AUTH_TOKEN))
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getWarnings)
                .satisfies(warnings -> assertThat(warnings).containsExactly("warnings 1"));

        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN));
    }

    @Test
    void givenNoAdditionalDocuments_whenHandle_thenAllInvocationsAreExecuted() {
        //No additional Documents
        FinremCallbackRequest callbackRequest = setupTestData(Collections.emptyList());

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingOrderService).convertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder(any(), eq(AUTH_TOKEN));
        verify(uploadedDraftOrderCategoriser).categorise(any(FinremCaseData.class));
        verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(any(CaseDetails.class), eq(AUTH_TOKEN));
        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN));
        verifyNoInteractions(genericDocumentService); // Ensure no unnecessary document conversions
    }

    @Test
    void givenMultipleAdditionalDocuments_whenHandle_thenAllAdditionalDocumentsAreConvertedToPdf() {
        CaseDocument document1 = CaseDocument.builder().documentFilename("additional doc 1.docx").build();
        CaseDocument document2 = CaseDocument.builder().documentFilename("additional doc 2.docx").build();
        CaseDocument pdfConverted1 = caseDocument(FILE_URL, "additional doc 1.pdf", FILE_BINARY_URL);
        CaseDocument pdfConverted2 = caseDocument(FILE_URL, "additional doc 2.pdf", FILE_BINARY_URL);

        when(genericDocumentService.convertDocumentIfNotPdfAlready(document1, AUTH_TOKEN, "123")).thenReturn(pdfConverted1);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(document2, AUTH_TOKEN, "123")).thenReturn(pdfConverted2);

        DocumentCollectionItem additionalDocument1 = DocumentCollectionItem.builder()
            .value(document1)
            .build();
        DocumentCollectionItem additionalDocument2 = DocumentCollectionItem.builder()
            .value(document2)
            .build();

        FinremCallbackRequest callbackRequest = setupTestData(List.of(additionalDocument1, additionalDocument2));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getDraftDirectionWrapper().getDraftDirectionOrderCollection())
            .isNotEmpty()
            .extracting(DraftDirectionOrderCollection::getValue)
            .flatExtracting(DraftDirectionOrder::getAdditionalDocuments)
            .extracting(DocumentCollectionItem::getValue)
            .containsExactlyInAnyOrder(pdfConverted1, pdfConverted2);

        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(CaseDocument.class),
            any(String.class), any(String.class));
        verify(uploadedDraftOrderCategoriser).categorise(any(FinremCaseData.class));
        verify(hearingOrderService).convertLastJudgeApprovedOrderToPdfAndStampAndStoreLatestDraftHearingOrder(
            any(FinremCaseData.class), eq(AUTH_TOKEN));
        verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    private FinremCallbackRequest setupTestData(List<DocumentCollectionItem> additionalDocuments) {
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        //Create direction order with additional documents (if any)
        DraftDirectionOrder directionOrder =
            DraftDirectionOrder.builder()
                .purposeOfDocument("test")
                .uploadDraftDocument(caseDocument)
                .additionalDocuments(additionalDocuments)
                .build();

        DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();

        //Set up draft direction order collection list
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(directionOrderCollection);

        FinremCaseData caseData = FinremCaseData.builder().build();
        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setJudgeApprovedOrderCollection(draftDirectionOrderCollection);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .id(123L)
            .caseType(CaseType.CONTESTED)
            .state(State.SCHEDULING_AND_HEARING)
            .data(caseData).build();

        return FinremCallbackRequest.builder()
            .eventType(EventType.JUDGE_DRAFT_ORDER)
            .caseDetails(finremCaseDetails)
            .build();
    }
}
