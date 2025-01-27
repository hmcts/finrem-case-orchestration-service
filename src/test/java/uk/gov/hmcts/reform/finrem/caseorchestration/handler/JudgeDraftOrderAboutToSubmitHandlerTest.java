package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeApprovedOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";

    @Mock
    private JudgeDraftOrderAboutToSubmitHandler handler;
    @Mock
    private HearingOrderService hearingOrderService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setUp() {
        handler = new JudgeDraftOrderAboutToSubmitHandler(
            finremCaseDetailsMapper,
            hearingOrderService,
            genericDocumentService,
            contestedOrderApprovedLetterService,
            caseDataService,
            uploadedDraftOrderCategoriser
        );
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenNoAdditionalDocuments_whenHandle_thenAllInvocationsAreExecuted() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        //No additional Documents
        FinremCallbackRequest callbackRequest = setupTestData(Collections.emptyList());

        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);
        verify(caseDataService).moveCollection(caseDetails.getData(), DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);
        verify(uploadedDraftOrderCategoriser).categorise(callbackRequest.getCaseDetails().getData());
        verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
        verifyNoInteractions(genericDocumentService); // Ensure no unnecessary document conversions
    }

    @Test
    void givenMultipleAdditionalDocuments_whenHandle_thenAllAdditionalDocumentsAreConvertedToPdf() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseDocument document1 = CaseDocument.builder().documentFilename("additional doc 1.docx").build();
        CaseDocument document2 = CaseDocument.builder().documentFilename("additional doc 2.docx").build();
        CaseDocument pdfConverted1 = caseDocument(FILE_URL, "additional doc 1.pdf", FILE_BINARY_URL);
        CaseDocument pdfConverted2 = caseDocument(FILE_URL, "additional doc 2.pdf", FILE_BINARY_URL);

        when(genericDocumentService.convertDocumentIfNotPdfAlready(eq(document1), eq(AUTH_TOKEN), any(String.class))).thenReturn(pdfConverted1);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(eq(document2), eq(AUTH_TOKEN), any(String.class))).thenReturn(pdfConverted2);
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);

        JudgeApprovedOrderAdditionalDocumentsCollection additionalDocument1 = JudgeApprovedOrderAdditionalDocumentsCollection.builder()
            .value(document1)
            .build();
        JudgeApprovedOrderAdditionalDocumentsCollection additionalDocument2 = JudgeApprovedOrderAdditionalDocumentsCollection.builder()
            .value(document2)
            .build();

        FinremCallbackRequest callbackRequest = setupTestData(List.of(additionalDocument1, additionalDocument2));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        DraftDirectionOrder draftOrderResult = response.getData().getDraftDirectionWrapper().getDraftDirectionOrderCollection().get(0).getValue();
        assertThat(draftOrderResult.getJudgeApprovedOrderAdditionalDocumentsCollection())
            .extracting(JudgeApprovedOrderAdditionalDocumentsCollection::getValue)
            .containsExactlyInAnyOrder(pdfConverted1, pdfConverted2)
            .hasSize(2);

        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(CaseDocument.class),
            any(String.class), any(String.class));
        verify(hearingOrderService).convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, AUTH_TOKEN);
        verify(caseDataService).moveCollection(caseDetails.getData(), DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);
        verify(uploadedDraftOrderCategoriser).categorise(callbackRequest.getCaseDetails().getData());
        verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);
    }

    private FinremCallbackRequest setupTestData(List<JudgeApprovedOrderAdditionalDocumentsCollection> additionalDocuments) {
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        //Create direction order with additional documents (if any)
        DraftDirectionOrder directionOrder =
            DraftDirectionOrder.builder()
                .purposeOfDocument("test")
                .uploadDraftDocument(caseDocument)
                .judgeApprovedOrderAdditionalDocumentsCollection(additionalDocuments)
                .build();

        DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();

        //Set up draft direction order collection list
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(directionOrderCollection);

        FinremCaseData caseData = FinremCaseData.builder().build();
        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setDraftDirectionOrderCollection(draftDirectionOrderCollection);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();

        return FinremCallbackRequest.builder()
            .eventType(EventType.JUDGE_DRAFT_ORDER)
            .caseDetails(finremCaseDetails)
            .build();
    }
}
