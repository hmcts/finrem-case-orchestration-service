package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentHandlerBase {

    private final ObjectMapper objectMapper;

    public DocumentHandlerBase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }
}
