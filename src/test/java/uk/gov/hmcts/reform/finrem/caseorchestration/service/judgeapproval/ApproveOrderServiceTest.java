package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Approvable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTimeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.PSA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService.SEPARATOR;

@ExtendWith(MockitoExtension.class)
class ApproveOrderServiceTest {

    private static final CaseDocument TARGET_DOC = mock(CaseDocument.class);

    @Spy
    @InjectMocks
    private ApproveOrderService underTest;

    @Mock
    private IdamService idamService;

    @ParameterizedTest
    @MethodSource("provideApprovableCollections")
    void testProcessApprovableCollection(List<? extends Approvable> approvables,
                                         CaseDocument targetDoc,
                                         JudgeApproval judgeApproval,
                                         int expectedHandledCount) {
        // Mock the method to count calls to handleApprovable
        lenient().doNothing().when(underTest).handleApprovable(any(), any(), any());

        underTest.processApprovableCollection(approvables, targetDoc, judgeApproval, AUTH_TOKEN);

        // Verify the number of handleApprovable invocations
        verify(underTest, times(expectedHandledCount)).handleApprovable(any(Approvable.class), eq(judgeApproval), eq(AUTH_TOKEN));
    }

    private static Stream<Arguments> provideApprovableCollections() {
        CaseDocument targetDoc = mock(CaseDocument.class);
        JudgeApproval judgeApproval = mock(JudgeApproval.class);

        Approvable matchingApprovable = mock(Approvable.class);
        when(matchingApprovable.match(targetDoc)).thenReturn(true);

        Approvable nonMatchingApprovable = mock(Approvable.class);
        when(nonMatchingApprovable.match(targetDoc)).thenReturn(false);

        return Stream.of(
            Arguments.of(List.of(matchingApprovable), targetDoc, judgeApproval, 1),
            Arguments.of(List.of(nonMatchingApprovable), targetDoc, judgeApproval, 0),
            Arguments.of(List.of(matchingApprovable, nonMatchingApprovable), targetDoc, judgeApproval, 1),
            Arguments.of(null, targetDoc, judgeApproval, 0),
            Arguments.of(List.of(), targetDoc, judgeApproval, 0)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "1, READY_TO_BE_SEALED, DRAFT_ORDER#1, DraftDocument1.pdf",
        "2, JUDGE_NEEDS_TO_MAKE_CHANGES, DRAFT_ORDER#2, AmendedDraftDocument2.pdf",
        "3, REVIEW_LATER, , "
    })
    void testBuildWhichOrderDynamicListWithoutReviewablePsa(int index, String decision, String expectedCode, String expectedLabel) {
        // Arrange
        DraftOrdersWrapper draftOrdersWrapper = new DraftOrdersWrapper();
        setJudgeApprovalToDraftOrdersWrapper(index,draftOrdersWrapper, createJudgeApproval(index, decision, null));

        // Act
        DynamicList dynamicList = underTest.buildWhichOrderDynamicList(draftOrdersWrapper);

        // Assert
        if (expectedCode == null) {
            Assertions.assertTrue(dynamicList.getListItems().isEmpty());
        } else {
            assertEquals(1, dynamicList.getListItems().size());
            DynamicListElement element = dynamicList.getListItems().get(0);
            assertEquals(expectedCode, element.getCode());
            assertEquals(expectedLabel, element.getLabel());
        }
    }

    private String expectedDocumentNamePrefix(String decision) {
        return "READY_TO_BE_SEALED".equals(decision) ? "" : "Amended";
    }

    @Test
    void testBuildWhichOrderDynamicListMultipleItems() {
        // Arrange
        DraftOrdersWrapper draftOrdersWrapper = new DraftOrdersWrapper();
        setJudgeApprovalToDraftOrdersWrapper(1, draftOrdersWrapper, createJudgeApproval(1, "READY_TO_BE_SEALED", null));
        setJudgeApprovalToDraftOrdersWrapper(2, draftOrdersWrapper, createJudgeApproval(2,   null, "REVIEW_LATER"));
        setJudgeApprovalToDraftOrdersWrapper(3, draftOrdersWrapper, createJudgeApproval(3, null, "JUDGE_NEEDS_TO_MAKE_CHANGES"));

        // Act
        DynamicList dynamicList = underTest.buildWhichOrderDynamicList(draftOrdersWrapper);

        // Assert
        assertEquals(2,dynamicList.getListItems().size());

        List<DynamicListElement> listItems = dynamicList.getListItems();

        // Additional checks to validate correct elements
        assertEquals("DRAFT_ORDER#" + 1, listItems.get(0).getCode());
        assertEquals(expectedDocumentNamePrefix("READY_TO_BE_SEALED") + "DraftDocument" + 1 + ".pdf", listItems.get(0).getLabel());

        assertEquals("PSA#" + 3, listItems.get(1).getCode());
        assertEquals(expectedDocumentNamePrefix("JUDGE_NEEDS_TO_MAKE_CHANGES") + "PsaDocument" + 3 + ".pdf", listItems.get(1).getLabel());
    }

    private void setJudgeApprovalToDraftOrdersWrapper(int index, DraftOrdersWrapper draftOrdersWrapper, JudgeApproval target) {
        try {
            DraftOrdersWrapper.class
                .getMethod("setJudgeApproval" + index, JudgeApproval.class)
                .invoke(draftOrdersWrapper, target);
        } catch (Exception e) {
            throw new RuntimeException("Error setting judge approval", e);
        }
    }

    private JudgeApproval createJudgeApproval(int index, String draftDecision, String psaDecision) {
        JudgeApproval judgeApproval = new JudgeApproval();

        if (draftDecision != null) {
            judgeApproval.setJudgeDecision(JudgeDecision.valueOf(draftDecision));
            CaseDocument document = new CaseDocument();
            CaseDocument amendedDocument = new CaseDocument();

            document.setDocumentFilename("DraftDocument" + index + ".pdf");
            amendedDocument.setDocumentFilename("AmendedDraftDocument" + index + ".pdf");

            judgeApproval.setDocument(document);
            judgeApproval.setAmendedDocument(amendedDocument);
            judgeApproval.setTitle(DRAFT_ORDER.getTitle());
            judgeApproval.setDocType(DRAFT_ORDER);
        }

        if (psaDecision != null) {
            judgeApproval.setJudgeDecision(JudgeDecision.valueOf(psaDecision));
            CaseDocument document = new CaseDocument();
            CaseDocument amendedDocument = new CaseDocument();

            document.setDocumentFilename("PsaDocument" + index + ".pdf");
            amendedDocument.setDocumentFilename("AmendedPsaDocument" + index + ".pdf");

            judgeApproval.setDocument(document);
            judgeApproval.setAmendedDocument(amendedDocument);
            judgeApproval.setTitle(PSA.getTitle());
            judgeApproval.setDocType(PSA);
        }

        return judgeApproval;
    }

    @Test
    void testResolveJudgeApproval() {
        // Mock DraftOrdersWrapper
        DraftOrdersWrapper draftOrdersWrapper = mock(DraftOrdersWrapper.class);

        // Mock JudgeApproval objects
        JudgeApproval judgeApproval1 = new JudgeApproval();
        JudgeApproval judgeApproval2 = new JudgeApproval();
        JudgeApproval judgeApproval3 = new JudgeApproval();
        JudgeApproval judgeApproval4 = new JudgeApproval();
        JudgeApproval judgeApproval5 = new JudgeApproval();

        // Stub the DraftOrdersWrapper methods
        when(draftOrdersWrapper.getJudgeApproval1()).thenReturn(judgeApproval1);
        when(draftOrdersWrapper.getJudgeApproval2()).thenReturn(judgeApproval2);
        when(draftOrdersWrapper.getJudgeApproval3()).thenReturn(judgeApproval3);
        when(draftOrdersWrapper.getJudgeApproval4()).thenReturn(judgeApproval4);
        when(draftOrdersWrapper.getJudgeApproval5()).thenReturn(judgeApproval5);

        // Test valid indices
        assertEquals(judgeApproval1, underTest.resolveJudgeApproval(draftOrdersWrapper, 1));
        assertEquals(judgeApproval2, underTest.resolveJudgeApproval(draftOrdersWrapper, 2));
        assertEquals(judgeApproval3, underTest.resolveJudgeApproval(draftOrdersWrapper, 3));
        assertEquals(judgeApproval4, underTest.resolveJudgeApproval(draftOrdersWrapper, 4));
        assertEquals(judgeApproval5, underTest.resolveJudgeApproval(draftOrdersWrapper, 5));

        // Test invalid index
        assertNull(underTest.resolveJudgeApproval(draftOrdersWrapper, 6));
        assertNull(underTest.resolveJudgeApproval(draftOrdersWrapper, 0));
        assertNull(underTest.resolveJudgeApproval(draftOrdersWrapper, -1));
    }

    @ParameterizedTest
    @MethodSource("provideJudgeApprovalTestCases")
    void testIsJudgeApproved(JudgeDecision judgeDecision, boolean expectedApproval) {
        boolean result = underTest.isJudgeApproved(JudgeApproval.builder().judgeDecision(judgeDecision).build());
        assertEquals(expectedApproval, result);
    }

    private static Stream<Arguments> provideJudgeApprovalTestCases() {
        return Stream.of(
            Arguments.of(READY_TO_BE_SEALED, true),
            Arguments.of(JUDGE_NEEDS_TO_MAKE_CHANGES, true),
            Arguments.of(null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidateJudgeApprovalDocumentTestData")
    void testValidateJudgeApprovalDocument(JudgeApproval judgeApproval, int index, boolean expectException) {
        if (expectException) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> underTest.validateJudgeApprovalDocument(judgeApproval, index)
            );
            assertEquals(
                format("Document is null for JudgeApproval at index %d. Please check the data integrity.", index),
                exception.getMessage()
            );
        } else {
            CaseDocument result = assertDoesNotThrow(() -> underTest.validateJudgeApprovalDocument(judgeApproval, index));
            assertNotNull(result);
            assertSame(judgeApproval.getDocument(), result);
        }
    }

    static Stream<Arguments> provideValidateJudgeApprovalDocumentTestData() {
        JudgeApproval validApproval = mock(JudgeApproval.class);
        CaseDocument validDoc = mock(CaseDocument.class);
        when(validApproval.getDocument()).thenReturn(validDoc);

        JudgeApproval invalidApproval = mock(JudgeApproval.class);
        when(invalidApproval.getDocument()).thenReturn(null);

        return Stream.of(
            Arguments.of(validApproval, 0, false),  // Valid case: should not throw exception
            Arguments.of(invalidApproval, 1, true) // Invalid case: should throw exception
        );
    }

    @ParameterizedTest
    @MethodSource("provideHearingInstructionTestData")
    void testProcessHearingInstruction(List<? extends HearingInstructionProcessable> hearingInstructionProcessables,
                                       CaseDocument targetDoc,
                                       AnotherHearingRequest anotherHearingRequest) {
        underTest.processHearingInstruction(hearingInstructionProcessables, targetDoc, anotherHearingRequest);

        if (hearingInstructionProcessables != null) {
            for (HearingInstructionProcessable element : hearingInstructionProcessables) {
                if (element.match(targetDoc)) {
                    if (element instanceof DraftOrderDocumentReview draftOrderDocumentReview) {
                        assertEquals(YesOrNo.YES, draftOrderDocumentReview.getAnotherHearingToBeListed());
                        assertEquals(anotherHearingRequest.getTypeOfHearing().name(), draftOrderDocumentReview.getHearingType());
                        assertEquals(anotherHearingRequest.getAdditionalTime(), draftOrderDocumentReview.getAdditionalTime());
                        assertEquals(anotherHearingRequest.getTimeEstimate().getValue(), draftOrderDocumentReview.getHearingTimeEstimate());
                        assertEquals(anotherHearingRequest.getAnyOtherListingInstructions(), draftOrderDocumentReview.getOtherListingInstructions());
                    } else if (element instanceof PsaDocumentReview psaDocumentReview) {
                        assertEquals(YesOrNo.YES, psaDocumentReview.getAnotherHearingToBeListed());
                        assertEquals(anotherHearingRequest.getTypeOfHearing().name(), psaDocumentReview.getHearingType());
                        assertEquals(anotherHearingRequest.getAdditionalTime(), psaDocumentReview.getAdditionalTime());
                        assertEquals(anotherHearingRequest.getTimeEstimate().getValue(), psaDocumentReview.getHearingTimeEstimate());
                        assertEquals(anotherHearingRequest.getAnyOtherListingInstructions(), psaDocumentReview.getOtherListingInstructions());
                    }
                } else {
                    if (element instanceof DraftOrderDocumentReview draftOrderDocumentReview) {
                        assertNull(draftOrderDocumentReview.getAnotherHearingToBeListed());
                        assertNull(draftOrderDocumentReview.getHearingType());
                        assertNull(draftOrderDocumentReview.getAdditionalTime());
                        assertNull(draftOrderDocumentReview.getHearingTimeEstimate());
                        assertNull(draftOrderDocumentReview.getOtherListingInstructions());
                    } else if (element instanceof PsaDocumentReview psaDocumentReview) {
                        assertNull(psaDocumentReview.getAnotherHearingToBeListed());
                        assertNull(psaDocumentReview.getHearingType());
                        assertNull(psaDocumentReview.getAdditionalTime());
                        assertNull(psaDocumentReview.getHearingTimeEstimate());
                        assertNull(psaDocumentReview.getOtherListingInstructions());
                    }
                }
            }
        }
    }

    static Stream<Arguments> provideHearingInstructionTestData() {
        DraftOrderDocumentReview matchingElement = spy(DraftOrderDocumentReview.class);
        CaseDocument targetDoc = mock(CaseDocument.class);
        when(matchingElement.match(targetDoc)).thenReturn(true);

        DraftOrderDocumentReview nonMatchingElement = spy(DraftOrderDocumentReview.class);
        when(nonMatchingElement.match(targetDoc)).thenReturn(false);

        AnotherHearingRequest anotherHearingRequest = spy(AnotherHearingRequest.class);
        when(anotherHearingRequest.getTypeOfHearing()).thenReturn(InterimTypeOfHearing.FH);
        when(anotherHearingRequest.getAdditionalTime()).thenReturn("30 minutes");
        when(anotherHearingRequest.getTimeEstimate()).thenReturn(HearingTimeDirection.STANDARD_TIME);
        when(anotherHearingRequest.getAnyOtherListingInstructions()).thenReturn("Test instructions");

        Stream<Arguments> r1 = Stream.of(
            Arguments.of(List.of(matchingElement), targetDoc, anotherHearingRequest),
            Arguments.of(List.of(nonMatchingElement), targetDoc, anotherHearingRequest),
            Arguments.of(List.of(matchingElement, nonMatchingElement), targetDoc, anotherHearingRequest),
            Arguments.of(null, targetDoc, anotherHearingRequest)
        );

        PsaDocumentReview matchingElement1 = spy(PsaDocumentReview.class);
        when(matchingElement1.match(targetDoc)).thenReturn(true);

        PsaDocumentReview nonMatchingElement1 = spy(PsaDocumentReview.class);
        when(nonMatchingElement1.match(targetDoc)).thenReturn(false);

        Stream<Arguments> r2 = Stream.of(
            Arguments.of(List.of(matchingElement1), targetDoc, anotherHearingRequest),
            Arguments.of(List.of(nonMatchingElement1), targetDoc, anotherHearingRequest),
            Arguments.of(List.of(matchingElement1, nonMatchingElement1), targetDoc, anotherHearingRequest),
            Arguments.of(null, targetDoc, anotherHearingRequest)
        );
        return Stream.concat(r1, r2);
    }

    @ParameterizedTest
    @MethodSource("provideApprovableTestData")
    void testHandleApprovable(Approvable approvable, JudgeApproval judgeApproval, boolean documentReplaced) {
        String mockJudgeName = "Judge TestName";
        when(idamService.getIdamFullName(any())).thenReturn(mockJudgeName);

        underTest.handleApprovable(approvable, judgeApproval, AUTH_TOKEN);

        verify(approvable).setOrderStatus(APPROVED_BY_JUDGE);
        verify(approvable).setApprovalDate(LocalDate.now());
        verify(approvable).setApprovalJudge(mockJudgeName);

        if (documentReplaced) {
            verify(approvable).replaceDocument(judgeApproval.getAmendedDocument());
        } else {
            verify(approvable, never()).replaceDocument(any());
        }
    }

    static Stream<Arguments> provideApprovableTestData() {
        CaseDocument amendedDocument = mock(CaseDocument.class);

        JudgeApproval judgeNeedsChanges = mock(JudgeApproval.class);
        when(judgeNeedsChanges.getJudgeDecision()).thenReturn(JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES);
        when(judgeNeedsChanges.getAmendedDocument()).thenReturn(amendedDocument);

        JudgeApproval judgeApproves = mock(JudgeApproval.class);
        when(judgeApproves.getJudgeDecision()).thenReturn(READY_TO_BE_SEALED);

        return Stream.of(
            Arguments.of(mock(PsaDocumentReview.class), judgeNeedsChanges, true),
            Arguments.of(mock(PsaDocumentReview.class), judgeApproves, false),
            Arguments.of(mock(DraftOrderDocumentReview.class), judgeNeedsChanges, true),
            Arguments.of(mock(DraftOrderDocumentReview.class), judgeApproves, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessHearingInstructionData")
    void testProcessHearingInstruction2(DraftOrdersWrapper draftOrdersWrapper, AnotherHearingRequest anotherHearingRequest,
                                        boolean invokeProcessHearingInstructionExpected, boolean illegalStateExceptionExpected) {
        if (!illegalStateExceptionExpected) {
            underTest.processHearingInstruction(draftOrdersWrapper, anotherHearingRequest);
            verify(underTest, times(invokeProcessHearingInstructionExpected ? 2 : 0))
                .processHearingInstruction(
                    anyList(),  // Matcher for List type
                    eq(TARGET_DOC),  // Matcher for CaseDocument
                    any(AnotherHearingRequest.class)  // Matcher for AnotherHearingRequest
                );
        } else {
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> underTest.processHearingInstruction(draftOrdersWrapper, anotherHearingRequest)
            );
            assertEquals("Missing selected value in AnotherHearingRequest.whichOrder", exception.getMessage());
        }
    }

    static Stream<Arguments> provideProcessHearingInstructionData() {
        return Stream.of(
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder().build()).build()))
                    .judgeApproval1(JudgeApproval.builder().document(TARGET_DOC).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("DRAFT_ORDER%s1", SEPARATOR)).build()).build())
                    .build(),
                true, false
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder().build()).build()))
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("DRAFT_ORDER%s1", SEPARATOR)).build()).build())
                    .build(),
                false, false
            ),
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder().build()).build()))
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("PSA%s1", SEPARATOR)).build()).build())
                    .build(),
                false, false
            ),
            Arguments.of(
                DraftOrdersWrapper.builder().build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().build()).build())
                    .build(),
                false, true
            )
        );
    }
}
