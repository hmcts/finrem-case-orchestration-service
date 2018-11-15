package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
public class DocumentService {

    @Value("${document.miniFormA.template}")
    private String miniFormATemplate;

    public CaseDocument generateMiniFormA(String authorisationToken, CCDRequest request) {
        return null;
    }
}
