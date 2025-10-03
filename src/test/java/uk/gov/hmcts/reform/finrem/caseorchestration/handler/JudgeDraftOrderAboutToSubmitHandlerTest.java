package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderAboutToSubmitHandlerTest {

    private JudgeDraftOrderAboutToSubmitHandler handler;
    @Mock
    private HearingOrderService hearingOrderService;
    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    @Mock
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;
    @Mock
    private DocumentWarningsHelper documentWarningsHelper;
    @Spy
    private ObjectMapper objectMapper;

    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        handler = new JudgeDraftOrderAboutToSubmitHandler(
            finremCaseDetailsMapper,
            hearingOrderService,
            contestedOrderApprovedLetterService,
            uploadedDraftOrderCategoriser,
            documentWarningsHelper
        );
        inOrder = Mockito.inOrder(hearingOrderService, uploadedDraftOrderCategoriser, contestedOrderApprovedLetterService);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenDraftOrderUploaded_whenDocumentWarningDetected_thenPopulateWarnings() {
        FinremCallbackRequest callbackRequest = setupTestDataWithoutAdditionalDocs();

        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warnings 1"));

        assertThat(handler.handle(callbackRequest, AUTH_TOKEN))
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getWarnings)
                .satisfies(warnings -> assertThat(warnings).containsExactly("warnings 1"));

        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN));
    }

    @Test
    void givenApprovedOrdersProvided_whenHandle_thenMoveToDraftDirectionOrderCollection() {
        List<DraftDirectionOrderCollection> uploadingApprovedOrders = List.of(
            DraftDirectionOrderCollection.builder().value(DraftDirectionOrder.builder().build()).build()
        );

        FinremCaseData caseData = FinremCaseData.builder().build();
        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setJudgeApprovedOrderCollection(uploadingApprovedOrders);
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .caseType(CaseType.CONTESTED)
            .state(State.SCHEDULING_AND_HEARING)
            .data(caseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .eventType(EventType.JUDGE_DRAFT_ORDER)
            .caseDetails(finremCaseDetails)
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData result = response.getData();

        assertThat(result.getDraftDirectionWrapper().getJudgeApprovedOrderCollection()).isNull();
        assertThat(result.getDraftDirectionWrapper().getDraftDirectionOrderCollection()).isEqualTo(uploadingApprovedOrders);
    }

    @Test
    void givenNoAdditionalDocsUploaded_whenHandle_thenNoAdditionalDocsProcessed() {
        FinremCallbackRequest callbackRequest = setupTestDataWithoutAdditionalDocs();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verifyExpectedInvocations(callbackRequest);
    }

    private void verifyExpectedInvocations(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        inOrder.verify(hearingOrderService).stampAndStoreJudgeApprovedOrders(finremCaseData, AUTH_TOKEN);
        inOrder.verify(contestedOrderApprovedLetterService).generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);
        inOrder.verify(uploadedDraftOrderCategoriser).categorise(finremCaseData);
    }

    private FinremCallbackRequest setupTestDataWithoutAdditionalDocs() {
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();

        FinremCaseData caseData = FinremCaseData.builder().build();
        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setJudgeApprovedOrderCollection(draftDirectionOrderCollection);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .caseType(CaseType.CONTESTED)
            .state(State.SCHEDULING_AND_HEARING)
            .data(caseData).build();

        return FinremCallbackRequest.builder()
            .eventType(EventType.JUDGE_DRAFT_ORDER)
            .caseDetails(finremCaseDetails)
            .build();
    }
}
