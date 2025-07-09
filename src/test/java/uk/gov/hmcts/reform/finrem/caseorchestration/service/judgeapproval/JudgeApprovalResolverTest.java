package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.LocalDateTimeExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;

@ExtendWith(MockitoExtension.class)
class JudgeApprovalResolverTest {

    @RegisterExtension
    LocalDateTimeExtension timeExtension = new LocalDateTimeExtension(FIXED_DATE_TIME);

    private static final String APPROVED_JUDGE_NAME = "Mary Chapman";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2024, 11, 4, 9, 0, 0);
    private static final CaseDocument TARGET_DOCUMENT = CaseDocument.builder().documentFilename("Order name.pdf").documentUrl("targetUrl").build();

    @InjectMocks
    private JudgeApprovalResolver judgeApprovalResolver;

    @Mock
    private IdamService idamService;

    @Mock
    private HearingProcessor hearingProcessor;

    @Mock
    private RefusedOrderProcessor refusedOrderProcessor;

    @Mock
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;

    @ParameterizedTest
    @MethodSource("provideShouldInvokeProcessHearingInstructionData")
    void shouldInvokeProcessHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, int expectHearingInvocationCount) {
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(draftOrdersWrapper)
                .build())
            .build();

        // Mocking cover letter generation
        CaseDocument coverLetter = CaseDocument.builder().documentUrl("coverLetterUrl").documentFilename("document.pdf").build();
        lenient().when(contestedOrderApprovedLetterService.generateAndStoreContestedApprovedCoverLetter(any(), any(), any(), any()))
            .thenReturn(coverLetter);

        // Execute the method being tested
        judgeApprovalResolver.populateJudgeDecision(finremCaseDetails,
            draftOrdersWrapper,
            TARGET_DOCUMENT,
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
                    .extraReportFieldsInput(ExtraReportFieldsInput.builder().judgeType(JudgeType.DISTRICT_JUDGE).build())
                    .hearingInstruction(HearingInstruction.builder().build())
                    .build(),
                0 // No hearing request means no invocation
            ),
            // Single hearing request
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .extraReportFieldsInput(ExtraReportFieldsInput.builder().judgeType(JudgeType.DISTRICT_JUDGE).build())
                    .hearingInstruction(HearingInstruction.builder()
                        .requireAnotherHearing(YesOrNo.YES)
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
                    .extraReportFieldsInput(ExtraReportFieldsInput.builder().judgeType(JudgeType.DISTRICT_JUDGE).build())
                    .hearingInstruction(HearingInstruction.builder()
                        .requireAnotherHearing(YesOrNo.YES)
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
    @MethodSource("provideNotPopulateAnythingForReviewLaterDecisionArguments")
    void shouldNotPopulateAnythingForReviewLaterDecision(DraftOrdersWrapper draftOrdersWrapper,
                                                         List<? extends Approvable> approvablesToBeExamined) {
        JudgeApproval judgeApproval = JudgeApproval.builder().judgeDecision(JudgeDecision.REVIEW_LATER).build();

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        judgeApprovalResolver.populateJudgeDecision(FinremCaseDetails.builder().build(), draftOrdersWrapper, TARGET_DOCUMENT, judgeApproval,
            AUTH_TOKEN);

        if (approvablesToBeExamined != null) {
            for (Approvable approvable : approvablesToBeExamined) {
                assertEquals(TO_BE_REVIEWED, approvable.getOrderStatus());
                assertNull(approvable.getApprovalDate());
                assertNull(approvable.getApprovalJudge());
                assertNull(approvable.getFinalOrder());
            }
        }
    }

    static Stream<Arguments> provideNotPopulateAnythingForReviewLaterDecisionArguments() {
        DraftOrderDocumentReview draftOrderDocumentReview = createDraftOrderDocumentReview();
        PsaDocumentReview psaDocumentReview = createPsaDocumentReview();
        AgreedDraftOrder agreedDraftOrder = createAgreedDraftOrder(true);
        AgreedDraftOrder agreedPsa = createAgreedDraftOrder(false);
        return Stream.of(
            Arguments.of(createDraftOrdersWrapper(draftOrderDocumentReview, null, null), List.of(draftOrderDocumentReview)),
            Arguments.of(createDraftOrdersWrapper(null, psaDocumentReview, null), List.of(psaDocumentReview)),
            Arguments.of(createDraftOrdersWrapper(null, null, agreedDraftOrder), List.of(agreedDraftOrder)),
            Arguments.of(createDraftOrdersWrapper(null, null, agreedPsa), List.of(agreedPsa))
        );
    }

    @ParameterizedTest
    @MethodSource("providePopulateJudgeDecisionForApprovedDocumentsArguments")
    void shouldPopulateJudgeDecisionForApprovedDocuments(DraftOrdersWrapper draftOrdersWrapper,
                                                         List<? extends Approvable> approvablesToBeExamined,
                                                         JudgeApproval judgeApproval,
                                                         CaseDocument expectedAmendedDocument,
                                                         YesOrNo expectedFinalOrder) {
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .draftOrdersWrapper(draftOrdersWrapper)
                .build())
            .build();

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        // Mocking cover letter generation
        CaseDocument coverLetter = CaseDocument.builder().documentUrl("coverLetterUrl").build();
        lenient().when(contestedOrderApprovedLetterService.generateAndStoreContestedApprovedCoverLetter(any(), any(), any(), any()))
            .thenReturn(coverLetter);

        judgeApprovalResolver.populateJudgeDecision(finremCaseDetails,
            draftOrdersWrapper, TARGET_DOCUMENT, judgeApproval, AUTH_TOKEN);

        boolean approvedDocument = false;
        if (approvablesToBeExamined != null) {
            for (Approvable approvable : approvablesToBeExamined) {
                if (approvable.match(expectedAmendedDocument != null ? expectedAmendedDocument : TARGET_DOCUMENT)) {
                    assertEquals(APPROVED_BY_JUDGE, approvable.getOrderStatus());
                    if (!(approvable instanceof AgreedDraftOrder)) {
                        assertEquals(FIXED_DATE_TIME, approvable.getApprovalDate());
                        assertEquals(APPROVED_JUDGE_NAME, approvable.getApprovalJudge());
                        assertEquals(expectedFinalOrder, approvable.getFinalOrder());
                        assertEquals(coverLetter, approvable.getCoverLetter());
                    } else {
                        assertNull(approvable.getApprovalDate());
                        assertNull(approvable.getApprovalJudge());
                        assertNull(approvable.getFinalOrder());
                    }
                    if (expectedAmendedDocument != null) {
                        assertEquals(expectedAmendedDocument, approvable.getTargetDocument());
                    }
                    approvedDocument = true;
                }
            }
        }
        assertTrue(approvedDocument, "One of the approvables should be got approved");
    }

    static DraftOrderDocumentReview createDraftOrderDocumentReview() {
        return DraftOrderDocumentReview.builder().draftOrderDocument(TARGET_DOCUMENT).orderStatus(TO_BE_REVIEWED).build();
    }

    static PsaDocumentReview createPsaDocumentReview() {
        return PsaDocumentReview.builder().psaDocument(TARGET_DOCUMENT).orderStatus(TO_BE_REVIEWED).build();
    }

    static AgreedDraftOrder createAgreedDraftOrder(boolean draftOrderOrPsa) {
        if (draftOrderOrPsa) {
            return AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT).orderStatus(TO_BE_REVIEWED).build();
        } else {
            return AgreedDraftOrder.builder().pensionSharingAnnex(TARGET_DOCUMENT).orderStatus(TO_BE_REVIEWED).build();
        }
    }

    static DraftOrdersWrapper createDraftOrdersWrapper(DraftOrderDocumentReview draftReview, PsaDocumentReview psaReview,
                                                       AgreedDraftOrder agreedDraftOrder) {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder draftOrdersWrapperBuilder = DraftOrdersWrapper.builder();
        if (draftReview != null || psaReview != null) {
            DraftOrdersReview.DraftOrdersReviewBuilder b = DraftOrdersReview.builder();
            if (draftReview != null) {
                b.draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder().value(draftReview).build()));
            }
            if (psaReview != null) {
                b.psaDocReviewCollection(List.of(PsaDocReviewCollection.builder().value(psaReview).build()));
            }
            draftOrdersWrapperBuilder.draftOrdersReviewCollection(List.of(
                DraftOrdersReviewCollection.builder()
                    .value(b.build())
                    .build()));
        }
        if (agreedDraftOrder != null) {
            draftOrdersWrapperBuilder.agreedDraftOrderCollection(List.of(
                AgreedDraftOrderCollection.builder().value(agreedDraftOrder).build()
            ));
        }
        return draftOrdersWrapperBuilder.build();
    }

    static Arguments checkJudgeNeedsToMakeChanges(DraftOrderDocumentReview draftReview, PsaDocumentReview psaReview,
                                                  AgreedDraftOrder agreedDraftOrder, Boolean isFinalOrder) {
        CaseDocument amendedDocument = CaseDocument.builder().documentUrl("AMENDED_DOC.doc").build();
        JudgeApproval.JudgeApprovalBuilder judgeApprovalBuilder = JudgeApproval.builder()
            .judgeDecision(JUDGE_NEEDS_TO_MAKE_CHANGES)
            .amendedDocument(amendedDocument);
        return buildArgumentsForApprovingDocument(judgeApprovalBuilder, draftReview, psaReview, agreedDraftOrder, amendedDocument, isFinalOrder);
    }

    static Arguments checkReadyToBeSealed(DraftOrderDocumentReview draftReview, PsaDocumentReview psaReview, AgreedDraftOrder agreedDraftOrder,
                                          Boolean isFinalOrder) {
        JudgeApproval.JudgeApprovalBuilder judgeApprovalBuilder = JudgeApproval.builder()
            .judgeDecision(READY_TO_BE_SEALED);
        return buildArgumentsForApprovingDocument(judgeApprovalBuilder, draftReview, psaReview, agreedDraftOrder, null, isFinalOrder);
    }

    static Arguments buildArgumentsForApprovingDocument(JudgeApproval.JudgeApprovalBuilder judgeApprovalBuilder,
                                                        DraftOrderDocumentReview draftReview, PsaDocumentReview psaReview,
                                                        AgreedDraftOrder agreedDraftOrder,
                                                        CaseDocument amendedDocument, Boolean isFinalOrder) {
        JudgeApproval judgeApproval;
        if (isFinalOrder == null) {
            judgeApproval = judgeApprovalBuilder.build();
        } else {
            judgeApproval = judgeApprovalBuilder
                .isFinalOrder(DynamicMultiSelectList.builder()
                    .value(TRUE.equals(isFinalOrder)
                        ? List.of(
                            DynamicMultiSelectListElement.builder()
                                .code(YesOrNo.YES.getYesOrNo())
                                .build())
                        : List.of()
                    )
                    .build())
                .build();
        }
        return Arguments.of(
            createDraftOrdersWrapper(draftReview, psaReview, agreedDraftOrder),
            Stream.of(draftReview, psaReview, agreedDraftOrder).filter(Objects::nonNull).toList(),
            judgeApproval,
            amendedDocument,
            YesOrNo.forValue(isFinalOrder == null ? FALSE : isFinalOrder)
        );
    }

    static Stream<Arguments> providePopulateJudgeDecisionForApprovedDocumentsArguments() {
        return Stream.of(
            checkJudgeNeedsToMakeChanges(createDraftOrderDocumentReview(), null, null, TRUE),
            checkJudgeNeedsToMakeChanges(createDraftOrderDocumentReview(), null, null, FALSE),
            checkJudgeNeedsToMakeChanges(createDraftOrderDocumentReview(), null, null, null),
            checkJudgeNeedsToMakeChanges(null, createPsaDocumentReview(), null, TRUE),
            checkJudgeNeedsToMakeChanges(null, createPsaDocumentReview(), null, FALSE),
            checkJudgeNeedsToMakeChanges(null, createPsaDocumentReview(), null, null),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(true), TRUE),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(true), FALSE),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(true), null),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(false), TRUE),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(false), FALSE),
            checkJudgeNeedsToMakeChanges(null, null, createAgreedDraftOrder(false), null),

            checkReadyToBeSealed(createDraftOrderDocumentReview(), null, null, TRUE),
            checkReadyToBeSealed(createDraftOrderDocumentReview(), null, null, FALSE),
            checkReadyToBeSealed(createDraftOrderDocumentReview(), null, null, null),
            checkReadyToBeSealed(null, createPsaDocumentReview(), null, TRUE),
            checkReadyToBeSealed(null, createPsaDocumentReview(), null, FALSE),
            checkReadyToBeSealed(null, createPsaDocumentReview(),null, null),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(true), TRUE),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(true), FALSE),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(true), null),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(false), TRUE),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(false), FALSE),
            checkReadyToBeSealed(null, null, createAgreedDraftOrder(false), null)
        );
    }

    @Test
    void shouldProcessRefusedApprovablesAndUpdateTheirState() {
        AgreedDraftOrder sample1;
        DraftOrderDocumentReview sample2;
        DraftOrdersWrapper.DraftOrdersWrapperBuilder dowBuilder = DraftOrdersWrapper.builder();
        dowBuilder.agreedDraftOrderCollection(List.of(
            AgreedDraftOrderCollection.builder().value(sample1 = AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT).build()).build()
        ));
        dowBuilder.draftOrdersReviewCollection(List.of(
            DraftOrdersReviewCollection.builder()
                .value(DraftOrdersReview.builder()
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(sample2 = DraftOrderDocumentReview.builder().draftOrderDocument(TARGET_DOCUMENT).build())
                            .build()
                    ))
                    .build())
                .build()
        ));

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().build();
        DraftOrdersWrapper draftOrdersWrapper;
        JudgeApproval ja;

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        judgeApprovalResolver.populateJudgeDecision(finremCaseDetails,
            draftOrdersWrapper = dowBuilder.build(), TARGET_DOCUMENT, ja = JudgeApproval.builder()
                .judgeDecision(JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE)
                .document(TARGET_DOCUMENT)
                .changesRequestedByJudge("FEEDBACK")
                .build(), AUTH_TOKEN);

        assertEquals(OrderStatus.REFUSED, sample1.getOrderStatus());
        assertEquals(OrderStatus.REFUSED, sample2.getOrderStatus());
        assertEquals(FIXED_DATE_TIME, sample2.getRefusedDate());
        assertNull(sample1.getApprovalDate());
        assertNull(sample2.getApprovalDate());
        assertNull(sample1.getApprovalJudge()); // AgreedDraftOrder doesn't store approvalJudge
        assertEquals(APPROVED_JUDGE_NAME, sample2.getApprovalJudge());
        verify(refusedOrderProcessor).processRefusedOrders(finremCaseDetails, draftOrdersWrapper, ja, AUTH_TOKEN);
        verify(hearingProcessor, never()).processHearingInstruction(eq(draftOrdersWrapper), any(AnotherHearingRequest.class));
    }

    @Test
    void shouldProcessRefusedIntervenerApprovablesAndUpdateTheirState() {
        AgreedDraftOrder intervenerSample;
        DraftOrdersWrapper.DraftOrdersWrapperBuilder dowBuilder = DraftOrdersWrapper.builder();
        dowBuilder.intvAgreedDraftOrderCollection(List.of(
            AgreedDraftOrderCollection.builder().value(intervenerSample = AgreedDraftOrder.builder().draftOrder(TARGET_DOCUMENT).build()).build()
        ));

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().build();
        DraftOrdersWrapper draftOrdersWrapper;
        JudgeApproval ja;

        // Mocking IDAM service for getting judge's full name
        lenient().when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(APPROVED_JUDGE_NAME);

        judgeApprovalResolver.populateJudgeDecision(finremCaseDetails,
            draftOrdersWrapper = dowBuilder.build(), TARGET_DOCUMENT, ja = JudgeApproval.builder()
                .judgeDecision(JudgeDecision.LEGAL_REP_NEEDS_TO_MAKE_CHANGE)
                .document(TARGET_DOCUMENT)
                .changesRequestedByJudge("FEEDBACK")
                .build(), AUTH_TOKEN);

        assertEquals(OrderStatus.REFUSED, intervenerSample.getOrderStatus());
        assertNull(intervenerSample.getApprovalDate());
        assertNull(intervenerSample.getApprovalJudge()); // AgreedDraftOrder doesn't store approvalJudge
        verify(refusedOrderProcessor).processRefusedOrders(finremCaseDetails, draftOrdersWrapper, ja, AUTH_TOKEN);
        verify(hearingProcessor, never()).processHearingInstruction(eq(draftOrdersWrapper), any(AnotherHearingRequest.class));
    }
}
