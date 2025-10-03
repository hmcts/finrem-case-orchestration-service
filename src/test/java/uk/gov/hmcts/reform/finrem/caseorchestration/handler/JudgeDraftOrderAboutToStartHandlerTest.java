package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
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

    private JudgeDraftOrderAboutToStartHandler handler;
    @Mock
    private IdamService idamService;
    @Spy
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        handler = new JudgeDraftOrderAboutToStartHandler(finremCaseDetailsMapper, idamService);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONTESTED, EventType.JUDGE_DRAFT_ORDER);
    }

    @Test
    void givenLegacyDraftOrdersExist_whenHandle_thenShouldPopulateAnEmptyDraftDirectionOrder() {
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder();
        builder.draftDirectionWrapper(DraftDirectionWrapper.builder()
            .draftDirectionOrderCollection(List.of(
                legacyDraftOrder("1"),
                legacyDraftOrder("2"),
                previousJudgeApprovedOrder()
            ))
            .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData toBeTested = response.getData();

        assertThat(toBeTested.getDraftDirectionWrapper().getJudgeApprovedOrderCollection())
            .containsOnly(DraftDirectionOrderCollection.EMPTY_COLLECTION);
    }

    @Test
    void givenNoPreviousJudgesApprovedOrder_whenHandle_thenShouldPopulateAnEmptyDraftDirectionOrder() {
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

        assertThat(toBeTested.getDraftDirectionWrapper().getJudgeApprovedOrderCollection())
            .containsOnly(DraftDirectionOrderCollection.EMPTY_COLLECTION);
    }

    @Test
    void shouldPrepopulateFields() {
        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder();
        builder.orderApprovedJudgeType(JudgeType.DISTRICT_JUDGE);
        builder.orderApprovedJudgeName("UNKNOWN");
        builder.orderApprovedDate(LocalDate.now());

        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("expected judge name");

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData toBeTested = response.getData();

        assertThat(toBeTested).extracting(
            FinremCaseData::getOrderApprovedJudgeType,
            FinremCaseData::getOrderApprovedDate,
            FinremCaseData::getOrderApprovedJudgeName
        ).containsExactly(null, null, "expected judge name");
    }

    private DraftDirectionOrderCollection legacyDraftOrder(String id) {
        return DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .purposeOfDocument("legacyDraftOrder") // non-null purposeOfDocument means it is a legacy draft order
                .uploadDraftDocument(caseDocument(format("legacyUrl%s", id), format("legacyFilename%s", id))).build())
            .build();
    }

    private DraftDirectionOrderCollection previousJudgeApprovedOrder() {
        String id = "previousId";
        return DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .purposeOfDocument(null) // null means judge's approved order
                .uploadDraftDocument(caseDocument(format("previousJaUrl%s", id), format("previousJaFilename%s", id))).build())
            .build();
    }
}
