package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.PartyFdrDocumentCategoriser;

@Component
public class IntervenerFourFdrDocumentCategoriser extends PartyFdrDocumentCategoriser {
    @Override
    protected DocumentCategory getDefaultDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4;
    }

    @Override
    protected DocumentCategory getHearingDraftOrderDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getQuestionnairesDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_QUESTIONNAIRES;
    }

    @Override
    protected DocumentCategory getWithoutPrejudiceDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getPositionStatementsDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_POSITION_STATEMENTS;
    }
}
