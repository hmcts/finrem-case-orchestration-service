package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

@Service
public class RespondentFormsHHandler extends FormsHHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public RespondentFormsHHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_FORM_H_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
