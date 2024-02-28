package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.PartyFdrDocumentCategoriser;

@Component
public class IntervenerOneFdrDocumentCategoriser extends PartyFdrDocumentCategoriser {
    @Override
    protected DocumentCategory getDefaultDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1;
    }

    @Override
    protected DocumentCategory getHearingDraftOrderDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getQuestionnairesDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_QUESTIONNAIRES;
    }

    @Override
    protected DocumentCategory getWithoutPrejudiceDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getPositionStatementsDocumentCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_POSITION_STATEMENTS;
    }
}
