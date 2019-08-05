package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@Service
@Slf4j
public class OnlineFormDocumentService extends AbstractDocumentService {

    private final OptionIdToValueTranslator optionIdToValueTranslator;

    @Autowired
    public OnlineFormDocumentService(DocumentClient documentClient,
                                     DocumentConfiguration config,
                                     OptionIdToValueTranslator optionIdToValueTranslator,
                                     ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
        this.optionIdToValueTranslator = optionIdToValueTranslator;
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        return generateDocument(authorisationToken, caseDetails,
                config.getMiniFormTemplate(),
                config.getMiniFormFileName());
    }

    public CaseDocument generateContestedMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        return generateDocument(authorisationToken, translateOptions(caseDetails),
                config.getContestedMiniFormTemplate(),
                config.getContestedMiniFormFileName());
    }

    public CaseDocument generateDraftContestedMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        CaseDocument caseDocument = generateDocument(authorisationToken, translateOptions(caseDetails),
                config.getContestedDraftMiniFormTemplate(),
                config.getContestedDraftMiniFormFileName());

        Optional.ofNullable(miniFormData(caseDetails)).ifPresent(data -> deleteOldMiniFormA(data, authorisationToken));
        return caseDocument;
    }

    private CaseDetails translateOptions(CaseDetails caseDetails) {
        CaseDetails copy = copyOf(caseDetails);
        optionIdToValueTranslator.translateOptionsValues.accept(copy);

        return copy;
    }

    private Map<String, Object> miniFormData(CaseDetails caseDetails) {
        return (Map<String, Object>) caseDetails.getData().get(MINI_FORM_A);
    }

    private void deleteOldMiniFormA(Map<String, Object> documentData, String authorisationToken) {
        String documentUrl = (String) documentData.get("document_url");
        CompletableFuture.runAsync(() -> {
            try {
                deleteDocument(documentUrl, authorisationToken);
            } catch (Exception e) {
                log.info("Failed to delete existing mini-form-a. Error occurred: {}", e.getMessage());
            }
        });
    }


}

