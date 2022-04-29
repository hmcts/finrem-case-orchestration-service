package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocDocumentTemplate;

@Component
public class LitigantSolicitorAddedNocLetterGenerator extends NocLetterGenerator {

    private DocumentConfiguration documentConfiguration;

    @Autowired
    public LitigantSolicitorAddedNocLetterGenerator(GenericDocumentService genericDocumentService,
                                                    ObjectMapper objectMapper, DocumentConfiguration documentConfiguration) {
        super(genericDocumentService, objectMapper);
        this.documentConfiguration = documentConfiguration;
    }

    @Override
    NocDocumentTemplate getNocDocumentTemplate() {
        return new NocDocumentTemplate(documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedTemplate(),
            documentConfiguration.getNocLetterNotificationLitigantSolicitorAddedFileName());
    }
}
