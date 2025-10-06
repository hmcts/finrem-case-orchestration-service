package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderAboutToStartHandlerTest {

    @InjectMocks
    private JudgeDraftOrderAboutToStartHandler handler;
    @Mock
    private IdamService idamService;

    @Test
    void canHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenLegacyDraftOrdersExist_whenHandle_thenShouldPopulateJudgeApprovedOrderCollection() {
        DraftDirectionOrderCollection previousJudgeApprovedOrder;
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder();
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .draftDirectionOrderCollection(List.of(
                legacyDraftOrder("1"),
                legacyDraftOrder("2"),
                previousJudgeApprovedOrder = previousJudgeApprovedOrder("1")
            ))
            .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData toBeTested = response.getData();

        assertThat(toBeTested.getDraftDirectionWrapper())
            .extracting(DraftDirectionWrapper::getJudgeApprovedOrderCollection)
            .satisfies(judgeApprovedOrderCollection ->
                assertThat(judgeApprovedOrderCollection).containsExactly(previousJudgeApprovedOrder));
    }

    @Test
    void givenNoPreviousJudgesApprovedOrder_whenHandle_thenShouldPopulateEmptyJudgeApprovedOrderCollection() {
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder();
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .draftDirectionOrderCollection(List.of(
                legacyDraftOrder("1"),
                legacyDraftOrder("2")
            ))
            .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData toBeTested = response.getData();

        assertThat(toBeTested.getDraftDirectionWrapper())
            .extracting(DraftDirectionWrapper::getJudgeApprovedOrderCollection)
            .satisfies(judgeApprovedOrderCollection ->
                assertThat(judgeApprovedOrderCollection).isEmpty());
    }

    @Test
    void givenUploadApproveOrder_whenHandle_shouldPrepopulateFields() {
        //Given
        String judgeName = "expected judge name";
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder();
        builder.orderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        builder.orderApprovedDate(LocalDate.now());
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));

        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn(judgeName);

        //When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        //Assert
        FinremCaseData responseData = response.getData();
        assertThat(responseData)
            .returns(judgeName, FinremCaseData::getOrderApprovedJudgeName)
            .returns(null, FinremCaseData::getOrderApprovedJudgeType)
            .returns(null, FinremCaseData::getOrderApprovedDate);
    }

    private DraftDirectionOrderCollection legacyDraftOrder(String id) {
        return DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .purposeOfDocument("legacyDraftOrder") // non-null purposeOfDocument means it is a legacy draft order
                .uploadDraftDocument(caseDocument(format("legacyUrl%s", id), format("legacyFilename%s", id))).build())
            .build();
    }

    private DraftDirectionOrderCollection previousJudgeApprovedOrder(String id) {
        return DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .purposeOfDocument(null) // null means judge's approved order
                .uploadDraftDocument(caseDocument(format("previousJaUrl%s", id), format("previousJaFilename%s", id))).build())
            .build();
    }
}
