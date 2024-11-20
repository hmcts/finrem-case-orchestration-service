package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ApproveOrderServiceTest {

    @InjectMocks
    private ApproveOrderService underTest;

    @ParameterizedTest
    @CsvSource({
        "1, READY_TO_BE_SEALED, draftOrder_1, DraftDocument1.pdf",
        "2, JUDGE_NEEDS_TO_MAKE_CHANGES, draftOrder_2, AmendedDraftDocument2.pdf",
        "3, REVIEW_LATER, , "
    })
    void testBuildWhichOrderDynamicListWithoutReviewablePsa(int index, String decision, String expectedCode, String expectedLabel) {
        // Arrange
        JudgeApproval judgeApproval = createJudgeApproval(index, decision, null);

        // Act
        DynamicList dynamicList = underTest.buildWhichOrderDynamicList(judgeApproval);

        // Assert
        if (expectedCode == null) {
            Assertions.assertTrue(dynamicList.getListItems().isEmpty());
        } else {
            Assertions.assertEquals(1, dynamicList.getListItems().size());
            DynamicListElement element = dynamicList.getListItems().get(0);
            Assertions.assertEquals(expectedCode, element.getCode());
            Assertions.assertEquals(expectedLabel, element.getLabel());
        }
    }

    private String expectedDocumentNamePrefix(String decision) {
        return "READY_TO_BE_SEALED".equals(decision) ? "" : "Amended";
    }

    @ParameterizedTest
    @CsvSource({
        "1, READY_TO_BE_SEALED, JUDGE_NEEDS_TO_MAKE_CHANGES",
        "2, JUDGE_NEEDS_TO_MAKE_CHANGES, READY_TO_BE_SEALED",
        "3, READY_TO_BE_SEALED, READY_TO_BE_SEALED"
    })
    void testBuildWhichOrderDynamicListMultipleItems(int index, String draftDecision, String psaDecision) {
        // Arrange
        JudgeApproval judgeApproval = createJudgeApproval(index, draftDecision, psaDecision);

        // Act
        DynamicList dynamicList = underTest.buildWhichOrderDynamicList(judgeApproval);

        // Assert
        Assertions.assertTrue(dynamicList.getListItems().size() > 1, "DynamicList should contain multiple items");

        List<DynamicListElement> listItems = dynamicList.getListItems();

        // Additional checks to validate correct elements
        Assertions.assertEquals("draftOrder_" + index, listItems.get(0).getCode());
        Assertions.assertEquals(expectedDocumentNamePrefix(draftDecision) + "DraftDocument" + index + ".pdf", listItems.get(0).getLabel());

        Assertions.assertEquals("psa_" + index, listItems.get(1).getCode());
        Assertions.assertEquals(expectedDocumentNamePrefix(psaDecision) + "PsaDocument" + index + ".pdf", listItems.get(1).getLabel());
    }

    private JudgeApproval createJudgeApproval(int index, String draftDecision, String psaDecision) {
        JudgeApproval judgeApproval = new JudgeApproval();

        // Populate ReviewableDraftOrder
        if (draftDecision != null) {
            ReviewableDraftOrder draftOrder = new ReviewableDraftOrder();
            draftOrder.setJudgeDecision(JudgeDecision.valueOf(draftDecision));
            CaseDocument document = new CaseDocument();
            CaseDocument amendedDocument = new CaseDocument();

            document.setDocumentFilename("DraftDocument" + index + ".pdf");
            amendedDocument.setDocumentFilename("AmendedDraftDocument" + index + ".pdf");

            draftOrder.setDocument(document);
            draftOrder.setAmendedDocument(amendedDocument);

            try {
                judgeApproval.getClass()
                    .getMethod("setReviewableDraftOrder" + index, ReviewableDraftOrder.class)
                    .invoke(judgeApproval, draftOrder);
            } catch (Exception e) {
                throw new RuntimeException("Error setting draft order", e);
            }
        }

        // Populate ReviewablePsa
        if (psaDecision != null) {
            ReviewablePsa psa = new ReviewablePsa();
            psa.setJudgeDecision(JudgeDecision.valueOf(psaDecision));
            CaseDocument document = new CaseDocument();
            CaseDocument amendedDocument = new CaseDocument();

            document.setDocumentFilename("PsaDocument" + index + ".pdf");
            amendedDocument.setDocumentFilename("AmendedPsaDocument" + index + ".pdf");

            psa.setDocument(document);
            psa.setAmendedDocument(amendedDocument);

            try {
                judgeApproval.getClass()
                    .getMethod("setReviewablePsa" + index, ReviewablePsa.class)
                    .invoke(judgeApproval, psa);
            } catch (Exception e) {
                throw new RuntimeException("Error setting PSA", e);
            }
        }

        return judgeApproval;
    }

}
