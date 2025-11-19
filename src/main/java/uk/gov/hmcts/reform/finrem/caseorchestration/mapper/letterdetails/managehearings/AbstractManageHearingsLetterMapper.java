package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;

public abstract class AbstractManageHearingsLetterMapper {
    protected static final String CASE_DETAILS = "caseDetails";
    protected static final String CASE_DATA = "case_data";
    protected static final String ID = "id";

    private final ObjectMapper objectMapper;
    protected final CourtDetailsConfiguration courtDetailsConfiguration;

    protected AbstractManageHearingsLetterMapper(ObjectMapper objectMapper,
                                                 CourtDetailsConfiguration courtDetailsConfiguration) {
        this.objectMapper = objectMapper;
        this.courtDetailsConfiguration = courtDetailsConfiguration;
    }

    /**
     * Builds the document template that is used to map generated document details for the given case details.
     *
     * @param caseDetails the {@link FinremCaseDetails} containing case-specific data
     * @return a {@link DocumentTemplateDetails} object with the template details
     */
    public abstract DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails);

    /**
     * Converts the document template details for the given case details into a map structure.
     * The resulting map contains the case details, including the case data and case ID,
     * structured to be processed by general document generation.
     *
     * @param caseDetails the {@link FinremCaseDetails} containing case-specific data
     * @return a {@link Map} with the document template details organized under the "caseDetails" key
     */
    public Map<String, Object> getDocumentTemplateDetailsAsMap(FinremCaseDetails caseDetails) {
        Map<String, Object> documentTemplateDetails =
            objectMapper.convertValue(buildDocumentTemplateDetails(caseDetails),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        Map<String, Object> caseDetailsMap = Map.of(
            CASE_DATA, documentTemplateDetails,
            ID, caseDetails.getId());

        return Map.of(CASE_DETAILS, caseDetailsMap);
    }

    /*
    * Returns the type of application from the case data. If not present, returns the default value.
    * This can be used by a template for conditional logic that determines content.
    * @param caseData the {@link FinremCaseData} containing case-specific data
    * @return the type of application or the default value if not present
    */
    public String getDefaultTypeOfApplicationIfNotPresent(FinremCaseData caseData) {
        if (ObjectUtils.isNotEmpty(caseData.getScheduleOneWrapper().getTypeOfApplication())) {
            return caseData.getScheduleOneWrapper().getTypeOfApplication().getValue();
        }
        return TYPE_OF_APPLICATION_DEFAULT_TO;
    }

    protected CourtDetailsTemplateFields buildCourtDetailsTemplateFields(String courtSelection) {
        CourtDetails courtDetails = courtDetailsConfiguration.getCourts().get(courtSelection);

        return CourtDetailsTemplateFields.builder()
            .courtName(courtDetails.getCourtName())
            .courtAddress(courtDetails.getCourtAddress())
            .phoneNumber(courtDetails.getPhoneNumber())
            .email(courtDetails.getEmail())
            .build();
    }

    protected Hearing getWorkingHearing(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getManageHearingsWrapper())
            .map(ManageHearingsWrapper::getWorkingHearing)
            .map(WorkingHearing::transformHearingInputsToHearing)
            .orElseThrow(() -> new IllegalArgumentException("Working hearing is null"));
    }
}
