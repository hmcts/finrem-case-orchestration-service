package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {

    @InjectMocks
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private IdamService idamService;

    @Mock
    private HearingProcessor hearingProcessor;

    @ParameterizedTest
    @MethodSource("provideShouldInvokeProcessHearingInstructionData")
    void shouldInvokeProcessHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, int expectHearingInvocationCount) {
        // Execute the method being tested
        judgeApprovalResolver.populateJudgeDecision(
            draftOrdersWrapper,
            CaseDocument.builder().build(),
            JudgeApproval.builder().judgeDecision(READY_TO_BE_SEALED).build(),
            AUTH_TOKEN
        );

        // Verify the expected number of invocations to processHearingInstruction
        verify(hearingProcessor, times(expectHearingInvocationCount))
            .processHearingInstruction(eq(draftOrdersWrapper), any(AnotherHearingRequest.class));
    }

    static Stream<Arguments> provideShouldInvokeProcessHearingInstructionData() {
        return Stream.of(
            // No hearing requests
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder().build())
                    .build(),
                0 // No hearing request means no invocation
            ),
            // Single hearing request
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder()
                        .anotherHearingRequestCollection(List.of(
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build()
                        ))
                        .build())
                    .build(),
                1 // Expect a single invocation for one hearing request
            ),
            // Multiple hearing requests
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .hearingInstruction(HearingInstruction.builder()
                        .anotherHearingRequestCollection(List.of(
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build(),
                            AnotherHearingRequestCollection.builder().value(AnotherHearingRequest.builder().build()).build()
                        ))
                        .build())
                    .build(),
                2 // Expect two invocations for two hearing requests
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessApprovableCollectionDataWithHandleApprovable")
    void shouldProcessApprovablesAndUpdateTheirState(DraftOrdersWrapper draftOrdersWrapper,
                                                     List<? extends Approvable> approvables,
                                                     JudgeApproval judgeApproval,
                                                     boolean shouldBeApproved,
                                                     CaseDocument targetDoc,
                                                     OrderStatus expectedOrderStatus,
                                                     CaseDocument expectedAmendedDocument) {

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Judge Full Name");

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 11, 4, 9, 0, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            judgeApprovalResolver.populateJudgeDecision(draftOrdersWrapper, targetDoc, judgeApproval, AUTH_TOKEN);

            if (approvables != null) {
                for (Approvable approvable : approvables) {
                    if (approvable.match(targetDoc)) {
                        if (shouldBeApproved) {
                            assertEquals(expectedOrderStatus, approvable.getOrderStatus());
                            assertEquals(fixedDateTime, approvable.getApprovalDate());
                            assertEquals("Judge Full Name", approvable.getApprovalJudge());
                            if (expectedAmendedDocument != null) {
                                assertEquals(expectedAmendedDocument, approvable.getReplaceDocument());
                            }
                        } else {
                            assertNull(approvable.getOrderStatus());
                            assertNull(approvable.getApprovalDate());
                            assertNull(approvable.getApprovalJudge());
                        }
                    }
                }
            }
        }
    }

    static Stream<Arguments> provideProcessApprovableCollectionDataWithHandleApprovable() {

        // Mock approvable objects
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument psaDocument = CaseDocument.builder().documentUrl("NEW_DOC2.doc").build();
        CaseDocument amendedDocument = CaseDocument.builder().documentUrl("AMENDED_DOC.doc").build();

        DraftOrderDocumentReview draftReview = DraftOrderDocumentReview.builder()
            .draftOrderDocument(draftOrderDocument)
            .build();
        PsaDocumentReview psaReview = PsaDocumentReview.builder()
            .psaDocument(psaDocument)
            .build();

        List<AgreedDraftOrder> agreedDrafts = List.of(AgreedDraftOrder.builder().build());

        JudgeApproval approvedJudgeApproval = JudgeApproval.builder()
            .judgeDecision(READY_TO_BE_SEALED)
            .build();

        JudgeApproval approvedJudgeApprovalWithChanges = JudgeApproval.builder()
            .judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES)
            .amendedDocument(amendedDocument)
            .build();

        JudgeApproval notApprovedJudgeApproval = JudgeApproval.builder().judgeDecision(null).build();

        return Stream.of(
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder()
                                .value(draftReview).build()))
                            .build()).build()))
                    .build(),
                List.of(draftReview),
                approvedJudgeApprovalWithChanges,
                true, // should be approved
                draftOrderDocument,
                OrderStatus.APPROVED_BY_JUDGE,
                amendedDocument
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                            .psaDocReviewCollection(List.of(PsaDocReviewCollection.builder()
                                .value(psaReview).build()))
                            .build()).build()))
                    .build(),
                List.of(psaReview),
                approvedJudgeApproval,
                true, // should be approved
                psaDocument,
                OrderStatus.APPROVED_BY_JUDGE,
                null
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .agreedDraftOrderCollection(List.of(
                        AgreedDraftOrderCollection.builder().value(agreedDrafts.get(0)).build()))
                    .build(),
                agreedDrafts,
                notApprovedJudgeApproval,
                false, // should not be approved
                CaseDocument.builder().build(),
                null,
                null
            ),
            Arguments.of(DraftOrdersWrapper.builder().build(), null, approvedJudgeApproval, false, null, null, null)
        );
    }
}
