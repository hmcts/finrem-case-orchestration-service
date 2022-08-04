package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;

import java.time.LocalDate;
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

    public List<GeneralApplicationCollectionData> getGeneralApplicationList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(GENERAL_APPLICATION_COLLECTION))
            .map(this::covertToGeneralApplicationData).orElse(new ArrayList<>());

    }

    public List<GeneralApplicationCollectionData> covertToGeneralApplicationData(Object object) {
        return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public CaseDocument convertToCaseDocument(Object object) {
        if (object != null) {
            return objectMapper.convertValue(object, CaseDocument.class);
        }
        return null;
    }

    public String objectToString(Object object) {
        if (object != null) {
            return objectMapper.convertValue(object, String.class);
        }
        return null;
    }

    public LocalDate objectToDateTime(Object object) {
        if (object != null) {
            return objectMapper.convertValue(object, LocalDate.class);
        }
        return null;
    }
}
