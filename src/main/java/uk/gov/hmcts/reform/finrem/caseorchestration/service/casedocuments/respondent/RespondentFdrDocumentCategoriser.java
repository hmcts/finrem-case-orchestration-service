package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.PartyFdrDocumentCategoriser;

@Component
public class RespondentFdrDocumentCategoriser extends PartyFdrDocumentCategoriser {

    @Override
    protected DocumentCategory getDefaultDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT;
    }


    @Override
    protected DocumentCategory getHearingDraftOrderDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER;
    }


    @Override
    protected DocumentCategory getOtherDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER;
    }


    @Override
    protected DocumentCategory getWithoutPrejudiceDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getPositionStatementsDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS;
    }

    @Override
    protected DocumentCategory getPointsOfClaimOrDefenceDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POINTS_OF_CLAIM_OR_DEFENCE;
    }
}
