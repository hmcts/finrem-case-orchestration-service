package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveOrderService {

    public DynamicList buildWhichOrderDynamicList(DraftOrdersWrapper draftOrdersWrapper) {
        List<DynamicListElement> listItems = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = getJudgeApproval(draftOrdersWrapper, i);
            if (judgeApproval != null) {
                String codePrefix = JudgeApprovalDocType.DRAFT_ORDER == judgeApproval.getDocType() ? "draftOrder" : "psa";
                String code = codePrefix + "_" + i;

                String filename = getDocumentFileName(judgeApproval);
                if (!StringUtils.isEmpty(filename)) {
                    listItems.add(DynamicListElement.builder().code(code).label(filename).build());
                }
            }
        }

        return DynamicList.builder().listItems(listItems).build();
    }

    private static String getDocumentFileName(JudgeApproval judgeApproval) {
        String filename = null;
        if (JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES == judgeApproval.getJudgeDecision()) {
            filename = judgeApproval.getAmendedDocument() != null
                ? judgeApproval.getAmendedDocument().getDocumentFilename()
                : "Unknown Filename";
        } else if (JudgeDecision.READY_TO_BE_SEALED == judgeApproval.getJudgeDecision()) {
            filename = judgeApproval.getDocument() != null
                ? judgeApproval.getDocument().getDocumentFilename()
                : "Unknown Filename";
        }
        return filename;
    }

    private JudgeApproval getJudgeApproval(DraftOrdersWrapper draftOrdersWrapper, int index) {
        return switch (index) {
            case 1 -> draftOrdersWrapper.getJudgeApproval1();
            case 2 -> draftOrdersWrapper.getJudgeApproval2();
            case 3 -> draftOrdersWrapper.getJudgeApproval3();
            case 4 -> draftOrdersWrapper.getJudgeApproval4();
            case 5 -> draftOrdersWrapper.getJudgeApproval5();
            default -> null;
        };
    }


}
