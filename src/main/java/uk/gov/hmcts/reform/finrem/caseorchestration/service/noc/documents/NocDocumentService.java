package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class NocDocumentService {

    static final String CASE_DETAILS = "caseDetails";
    static final String CASE_DATA = "case_data";
    private final GenericDocumentService genericDocumentService;
    private final ObjectMapper objectMapper;

    protected NocDocumentService(GenericDocumentService genericDocumentService,
                              ObjectMapper objectMapper) {
        this.genericDocumentService = genericDocumentService;
        this.objectMapper = objectMapper;
    }

    public CaseDocument generateNoticeOfChangeLetter(String authToken,
                                                     NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                                     String caseId) {

        log.info("Calling the GenericDocumentService with template {} and filename {}",
            getNocDocumentTemplate().getTemplateName(), getNocDocumentTemplate().getDocumentFileName());
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authToken,
            convertNoticeOfChangeLetterDetailsToMap(noticeOfChangeLetterDetails),
            getNocDocumentTemplate().getTemplateName(),
            getNocDocumentTemplate().getDocumentFileName(), caseId);
    }

    private Map<String, Object> convertNoticeOfChangeLetterDetailsToMap(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails) {
        HashMap<String, Object> caseDetailsMap = new HashMap<>();
        HashMap<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_DATA, objectMapper.convertValue(noticeOfChangeLetterDetails, Map.class));
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }

    abstract NocDocumentTemplate getNocDocumentTemplate();
}
