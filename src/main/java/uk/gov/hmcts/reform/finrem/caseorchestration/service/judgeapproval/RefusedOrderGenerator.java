package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.contestordernotapproved.ContestedDraftOrderNotApprovedDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.FileUtils.insertTimestamp;

@Component
@RequiredArgsConstructor
public class RefusedOrderGenerator {

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final ContestedDraftOrderNotApprovedDetailsMapper contestedDraftOrderNotApprovedDetailsMapper;

    /**
     * Generates a refused order document for the given case details.
     * This method sets temporary values in the DraftOrdersWrapper for use during document generation,
     * then clears them after the document has been created.
     *
     * @param finremCaseDetails   the details of the financial remedy case
     * @param refusalReason       the reason for refusing the order
     * @param refusedDate         the date and time the order was refused
     * @param judgeName           the name of the judge refusing the order
     * @param judgeType           the type of judge refusing the order
     * @param authorisationToken  the authorisation token for accessing document services
     * @return                    the generated CaseDocument representing the refused order
     */
    public CaseDocument generateRefuseOrder(FinremCaseDetails finremCaseDetails, String refusalReason, LocalDateTime refusedDate,
                                            String judgeName, JudgeType judgeType, String authorisationToken) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseDetails.getData().getDraftOrdersWrapper();
        draftOrdersWrapper.setGeneratedOrderReason(refusalReason);
        draftOrdersWrapper.setGeneratedOrderRefusedDate(refusedDate);
        draftOrdersWrapper.setGeneratedOrderJudgeName(judgeName);
        draftOrdersWrapper.setGeneratedOrderJudgeType(judgeType);

        try {
            return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
                contestedDraftOrderNotApprovedDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
                    finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList()
                ),
                documentConfiguration.getContestedDraftOrderNotApprovedTemplate(finremCaseDetails),
                insertTimestamp(documentConfiguration.getContestedDraftOrderNotApprovedFileName()),
                finremCaseDetails.getId().toString());
        } finally {
            // Clear the temp values as they are for report generation purpose.
            draftOrdersWrapper.setGeneratedOrderReason(null);
            draftOrdersWrapper.setGeneratedOrderRefusedDate(null);
            draftOrdersWrapper.setGeneratedOrderJudgeType(null);
            draftOrdersWrapper.setGeneratedOrderJudgeName(null);
        }
    }

}
