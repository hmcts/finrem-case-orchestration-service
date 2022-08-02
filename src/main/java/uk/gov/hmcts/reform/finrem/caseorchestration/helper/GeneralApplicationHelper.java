package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationHelper {

    private final ObjectMapper objectMapper;

    public List<GeneralApplicationCollectionData> getExistingGeneralApplications(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(GENERAL_APPLICATION_COLLECTION))
            .map(this::generalApplicationList).orElse(new ArrayList<>());

    }

    public List<GeneralApplicationCollectionData> generalApplicationList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }
}
