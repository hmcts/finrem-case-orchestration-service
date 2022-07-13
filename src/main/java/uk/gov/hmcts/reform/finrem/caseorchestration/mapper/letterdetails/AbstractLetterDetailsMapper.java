package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.CourtListWrapper;

import java.util.HashMap;
import java.util.Map;

@Component
public abstract class AbstractLetterDetailsMapper {

    protected static final String CASE_DETAILS = "caseDetails";
    protected static final String CASE_DATA = "case_data";

    protected final CourtDetailsMapper courtDetailsMapper;
    protected final ObjectMapper objectMapper;

    public AbstractLetterDetailsMapper(CourtDetailsMapper courtDetailsMapper, ObjectMapper objectMapper) {
        this.courtDetailsMapper = courtDetailsMapper;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public abstract DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails,
                                                                         CourtListWrapper courtList);

    public Map<String, Object> getDocumentTemplateDetailsAsMap(FinremCaseDetails caseDetails,
                                                               CourtListWrapper courtList) {
        Map<String, Object> documentTemplateDetails =
            objectMapper.convertValue(buildDocumentTemplateDetails(caseDetails, courtList),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, documentTemplateDetails,
            "id", caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
