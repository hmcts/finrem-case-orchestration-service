package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.HashMap;
import java.util.Map;

@Component
public abstract class AbstractManageHearingsLetterMapper {
    protected static final String CASE_DETAILS = "caseDetails";
    protected static final String CASE_DATA = "case_data";
    protected static final String ID = "id";

    private final ObjectMapper objectMapper;
    protected final CourtDetailsConfiguration courtDetailsConfiguration;

    protected AbstractManageHearingsLetterMapper(ObjectMapper objectMapper, CourtDetailsConfiguration courtDetailsConfiguration) {
        this.objectMapper = objectMapper;
        this.courtDetailsConfiguration = courtDetailsConfiguration;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public abstract DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails);

    public Map<String, Object> getDocumentTemplateDetailsAsMap(FinremCaseDetails caseDetails) {
        Map<String, Object> documentTemplateDetails =
            objectMapper.convertValue(buildDocumentTemplateDetails(caseDetails),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, documentTemplateDetails,
            ID, caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
