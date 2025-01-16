package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.REVIEW_LATER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApproveDraftOrdersMidEventHandlerTest {

    @InjectMocks
    private ApproveDraftOrdersMidEventHandler handler;

    @Mock
    private ApproveOrderService approveOrderService;

    @BeforeEach
    void setup() {
        lenient().when(approveOrderService.resolveJudgeApproval(any(), anyInt())).thenCallRealMethod();
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.APPROVE_ORDERS);
    }

    @Test
    void shouldPopulateAnEmptyAnotherHearingRequestEntry() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(12345L)
                .data(FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .judgeApproval1(JudgeApproval.builder()
                            .judgeDecision(REVIEW_LATER)
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertNotNull(response);
        FinremCaseData responseData = response.getData();
        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();

        List<AnotherHearingRequestCollection> actualCollection =
            responseDraftOrdersWrapper.getHearingInstruction().getAnotherHearingRequestCollection();

        assertNotNull(actualCollection, "anotherHearingRequestCollection should not be null");
        assertEquals(1, actualCollection.size(), "anotherHearingRequestCollection should contain exactly one element");
        AnotherHearingRequest actualRequest = actualCollection.get(0).getValue();
        assertNotNull(actualRequest, "The AnotherHearingRequest object should not be null");

        DynamicList expectedDynamicList = DynamicList.builder().listItems(new ArrayList<>()).build();
        assertEquals(actualRequest.getWhichOrder(), expectedDynamicList);
        assertNull(actualRequest.getTypeOfHearing(), "typeOfHearing should be null");
        assertNull(actualRequest.getTimeEstimate(), "timeEstimate should be null");
        assertNull(actualRequest.getAdditionalTime(), "additionalTime should be null");
        assertNull(actualRequest.getAnyOtherListingInstructions(), "anyOtherListingInstructions should be null");
    }

    @Test
    void shouldPopulateWhichOrder() {
        // Arrange
        DraftOrdersWrapper draftOrdersWrapper = DraftOrdersWrapper.builder()
            .judgeApproval1(
                JudgeApproval.builder()
                    .judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES)
                    .docType(JudgeApprovalDocType.DRAFT_ORDER)
                    .amendedDocument(CaseDocument.builder().documentFilename("AMENDED_DOC.doc").build())
                    .build()
            )
            .judgeApproval2(
                JudgeApproval.builder()
                    .judgeDecision(READY_TO_BE_SEALED)
                    .docType(JudgeApprovalDocType.PSA)
                    .document(CaseDocument.builder().documentFilename("PSA.doc").build())
                    .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(12345L)
                .data(FinremCaseData.builder()
                    .draftOrdersWrapper(draftOrdersWrapper)
                    .build())
                .build())
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertNotNull(response);
        FinremCaseData responseData = response.getData();
        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();

        List<AnotherHearingRequestCollection> actualCollection =
            responseDraftOrdersWrapper.getHearingInstruction().getAnotherHearingRequestCollection();
        assertEquals(YES, responseDraftOrdersWrapper.getHearingInstruction().getShowRequireAnotherHearingQuestion());

        assertNotNull(actualCollection, "anotherHearingRequestCollection should not be null");
        assertEquals(1, actualCollection.size(), "anotherHearingRequestCollection should contain exactly one element");
        AnotherHearingRequest actualRequest = actualCollection.get(0).getValue();
        assertNotNull(actualRequest, "The AnotherHearingRequest object should not be null");

        DynamicList expectedDynamicList = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().code("DRAFT_ORDER#1").label("AMENDED_DOC.doc").build(),
            DynamicListElement.builder().code("PSA#2").label("PSA.doc").build()
        )).build();
        assertEquals(actualRequest.getWhichOrder(), expectedDynamicList);
    }

    @Test
    void shouldPopulateExtraReportFieldsInput() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .id(12345L)
                .data(FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .judgeApproval1(JudgeApproval.builder()
                            .judgeDecision(LEGAL_REP_NEEDS_TO_MAKE_CHANGE)
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertNotNull(response);
        FinremCaseData responseData = response.getData();
        DraftOrdersWrapper responseDraftOrdersWrapper = responseData.getDraftOrdersWrapper();

        ExtraReportFieldsInput extraReportFieldsInput = responseDraftOrdersWrapper.getExtraReportFieldsInput();
        assertEquals(NO, responseDraftOrdersWrapper.getHearingInstruction().getShowRequireAnotherHearingQuestion());
        assertNotNull(extraReportFieldsInput, "extraReportFieldsInput should not be null");
        assertEquals(YES, extraReportFieldsInput.getShowRequireExtraReportFieldsInputQuestion());
    }
}
