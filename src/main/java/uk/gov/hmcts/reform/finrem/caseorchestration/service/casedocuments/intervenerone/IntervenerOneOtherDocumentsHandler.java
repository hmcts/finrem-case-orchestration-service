package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;

@Component
public class IntervenerOneOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerOneOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_OTHER_COLLECTION, INTERVENER_ONE, featureToggleService);
    }

    @Override
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_PENSION_PLAN;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES1() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_ES1;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES2() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_ES2;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryMortgageCapacities() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_MORTGAGE_CAPACITIES;
    }

    @Override
    protected DocumentCategory getHouseParticularsDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_HOUSING_PARTICULARS;
    }

    @Override
    protected DocumentCategory getPreHearingDraftOrderDocumentCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1;
    }
}
