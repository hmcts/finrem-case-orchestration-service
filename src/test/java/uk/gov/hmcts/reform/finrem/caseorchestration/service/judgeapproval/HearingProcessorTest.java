package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTimeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingProcessorTest {

    private static final String SEPARATOR = "#";

    private static final CaseDocument TARGET_DOC = mock(CaseDocument.class);

    @InjectMocks
    private HearingProcessor hearingProcessor;

    @ParameterizedTest
    @MethodSource("provideHearingInstructionTestData")
    void testProcessHearingInstruction(List<? extends HearingInstructionProcessable> hearingInstructionProcessables,
                                       CaseDocument targetDoc,
                                       AnotherHearingRequest anotherHearingRequest) {
        hearingProcessor.processHearingInstruction(hearingInstructionProcessables, targetDoc, anotherHearingRequest);

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
    @MethodSource("provideProcessHearingInstructionData")
    void testProcessHearingInstruction2(DraftOrdersWrapper draftOrdersWrapper, AnotherHearingRequest anotherHearingRequest,
                                        boolean processHearingInstructionInvokedExpected, String expectedIllegalStateExceptionMessage) {
        if (expectedIllegalStateExceptionMessage == null) {
            hearingProcessor.processHearingInstruction(draftOrdersWrapper, anotherHearingRequest);
            //Check the expected changes in data
            if (processHearingInstructionInvokedExpected) {
                //Verify that specific fields are updated
                assertTrue(draftOrdersWrapper.getDraftOrdersReviewCollection().stream()
                    .flatMap(el -> el.getValue().getDraftOrderDocReviewCollection().stream())
                    .anyMatch(doc -> YesOrNo.YES.equals(doc.getValue().getAnotherHearingToBeListed())));
            } else {
                // Verify no changes are made
                assertTrue(draftOrdersWrapper.getDraftOrdersReviewCollection().stream()
                    .flatMap(el -> el.getValue().getDraftOrderDocReviewCollection().stream())
                    .noneMatch(doc -> YesOrNo.YES.equals(doc.getValue().getAnotherHearingToBeListed())));
            }

        } else {
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> hearingProcessor.processHearingInstruction(draftOrdersWrapper, anotherHearingRequest)
            );
            assertEquals(expectedIllegalStateExceptionMessage, exception.getMessage());
        }
    }

    static Stream<Arguments> provideProcessHearingInstructionData() {
        CaseDocument draftOrderDocument = CaseDocument.builder().documentUrl("NEW_DOC1.doc").build();
        CaseDocument psaDocument = CaseDocument.builder().documentUrl("NEW_DOC2.doc").build();


        DraftOrderDocumentReview draftReview = DraftOrderDocumentReview.builder()
            .draftOrderDocument(draftOrderDocument)
            .build();
        PsaDocumentReview psaReview = PsaDocumentReview.builder()
            .psaDocument(psaDocument)
            .build();

        return Stream.of(
            // happy path 1
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                        .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder()
                            .value(draftReview).build())).build()).build()))
                    .judgeApproval1(JudgeApproval.builder().document(draftOrderDocument).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .typeOfHearing(InterimTypeOfHearing.FH)
                    .additionalTime("Test additional time")
                    .timeEstimate(HearingTimeDirection.STANDARD_TIME)
                    .anyOtherListingInstructions("Test other listing instruction")
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("DRAFT_ORDER%s1", SEPARATOR)).build()).build())
                    .build(),
                true, null
            ),
            // happy path 2
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                        .draftOrderDocReviewCollection(List.of(DraftOrderDocReviewCollection.builder()
                            .value(draftReview).build())).build()).build()))
                    .judgeApproval1(JudgeApproval.builder().document(draftOrderDocument).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .typeOfHearing(InterimTypeOfHearing.FH)
                    .additionalTime("Test additional time")
                    .timeEstimate(HearingTimeDirection.STANDARD_TIME)
                    .anyOtherListingInstructions("Test other listing instruction")
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("PSA%s1", SEPARATOR)).build()).build())
                    .build(),
                true, null
            ),
            // missing corresponding judgeApproval when a draft order in whichOrder selected
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder().build()).build()))
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("DRAFT_ORDER%s1", SEPARATOR)).build()).build())
                    .build(),
                false, null
            ),
            // missing corresponding judgeApproval when a PSA in whichOrder selected
            Arguments.of(
                DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                        .psaDocReviewCollection(List.of(PsaDocReviewCollection.builder()
                            .value(psaReview).build()))
                        .build()).build()))
                    .judgeApproval2(JudgeApproval.builder().document(TARGET_DOC).build())
                    .build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("PSA%s1", SEPARATOR)).build()).build())
                    .build(),
                false, null
            ),
            // missing whichOrder
            Arguments.of(
                DraftOrdersWrapper.builder().build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().build()).build())
                    .build(),
                false, "Missing selected value in AnotherHearingRequest.whichOrder"
            ),
            // unexpected code value
            Arguments.of(
                DraftOrdersWrapper.builder().build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code("XXX").build()).build())
                    .build(),
                false, "Unexpected selected value in AnotherHearingRequest.whichOrder: XXX"
            ),
            // unexpected code value 2
            Arguments.of(
                DraftOrdersWrapper.builder().build(),
                AnotherHearingRequest.builder()
                    .whichOrder(DynamicList.builder().value(DynamicListElement.builder().code(format("PSA%s6", SEPARATOR)).build()).build())
                    .build(),
                false, "Unexpected method \"getJudgeApproval6\" was invoked"
            )
        );
    }
}
