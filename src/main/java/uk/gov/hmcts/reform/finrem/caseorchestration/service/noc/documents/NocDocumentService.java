package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.Map;

@Slf4j
public abstract class NocDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final ObjectMapper objectMapper;

    public NocDocumentService(GenericDocumentService genericDocumentService,
                              ObjectMapper objectMapper) {
        this.genericDocumentService = genericDocumentService;
        this.objectMapper = objectMapper;
    }

    public CaseDocument generateNoticeOfChangeLetter(String authToken, NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {

        log.info("Calling the GenericDocumentService with template {} and filename {}",
            getNocDocumentTemplate().getTemplateName(), getNocDocumentTemplate().getDocumentFileName());
        CaseDocument caseDocument = genericDocumentService.generateDocumentFromPlaceholdersMap(authToken,
            convertNoticeOfChangeLetterDetailsToMap(noticeOfChangeLetterDetails),
            getNocDocumentTemplate().getTemplateName(),
            getNocDocumentTemplate().getDocumentFileName());
        return caseDocument;
    }

    private Map convertNoticeOfChangeLetterDetailsToMap(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        return objectMapper.convertValue(noticeOfChangeLetterDetails, Map.class);

    }

    abstract NocDocumentTemplate getNocDocumentTemplate();
}
