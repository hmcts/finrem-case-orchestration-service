package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

@Component
public class LitigantSolicitorAddedNocDocumentService extends NocDocumentService {

    private DocumentConfiguration documentConfiguration;

    @Autowired
    public LitigantSolicitorAddedNocDocumentService(GenericDocumentService genericDocumentService,
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
