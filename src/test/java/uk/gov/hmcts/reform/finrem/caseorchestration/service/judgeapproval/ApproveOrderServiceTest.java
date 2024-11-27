package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.PSA;

@ExtendWith(MockitoExtension.class)
class ApproveOrderServiceTest {

    @InjectMocks
    private ApproveOrderService underTest;

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

}
