package uk.gov.hmcts.reform.finrem.caseorchestration.service.miam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_ABUSE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_VIOLENCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST_V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6;

@Service
public class MiamCheckService {

    private static final String MIAM_EXEMPT_ERROR = "You cannot make this application to court unless the applicant has "
        + "either attended, or is exempt from attending a MIAM. Please refer to "
        + "https://www.familymediationcouncil.org.uk/family-mediation/assessment-meeting-miam/ "
        + "for further information on what to do next and how to arrange a MIAM.";

    private static final String MIAM_EVIDENCE_UNAVAILABLE_ERROR = "Please explain in the textbox why you are unable to "
        + "provide the required evidence with your application.";

    public List<String> validateMiamFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<String> miamExemptionErrors = miamExemptionAttendanceCheck(caseData);
        if (!miamExemptionErrors.isEmpty()) {
            return miamExemptionErrors;
        }

        Map<String, List<String>> errors = new HashMap<>();
        addEvidenceUnavailableErrors(errors, caseData);

        return errors.values().stream().filter(error -> !error.isEmpty()).findFirst().orElse(List.of());
    }

    private void addEvidenceUnavailableErrors(Map<String, List<String>> errors, Map<String, Object> caseData) {
        errors.put("MiamDomesticViolenceEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_DOMESTIC_VIOLENCE_CHECKLIST, MIAM_DOMESTIC_ABUSE_TEXTBOX,
            FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23.getValue()));

        errors.put("MiamUrgencyEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_URGENCY_CHECKLIST, MIAM_URGENCY_TEXTBOX,
            FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6.getValue()));

        errors.put("MiamPreviousAttendanceEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2, MIAM_PREVIOUS_ATTENDANCE_TEXTBOX,
            FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_6.getValue()));

        errors.put("MiamOtherGroundsEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_OTHER_GROUNDS_CHECKLIST_V2, MIAM_OTHER_GROUNDS_TEXTBOX,
            FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_16.getValue()));
    }

    private List<String> miamExemptionAttendanceCheck(Map<String, Object> caseData) {
        String applicantAttended = Objects.toString(caseData.get(APPLICANT_ATTENDED_MIAM));
        String claimingExemption = Objects.toString(caseData.get(CLAIMING_EXEMPTION_MIAM));

        if (applicantAttended.equalsIgnoreCase("no") && claimingExemption.equalsIgnoreCase("no")) {
            return List.of(MIAM_EXEMPT_ERROR);
        }
        return Collections.emptyList();
    }

    private List<String> getMiamEvidenceUnavailableErrors(Map<String, Object> caseData, String checklistKey,
                                                          String textboxKey, String checklistValue) {

        String checklist = Objects.toString(caseData.get(checklistKey));
        String textbox = convertObjectToString(caseData.get(textboxKey));

        if (checklist != null && checklist.contains(checklistValue) && StringUtils.isBlank(textbox)) {
            return List.of(MIAM_EVIDENCE_UNAVAILABLE_ERROR);
        }
        return Collections.emptyList();
    }

    private String convertObjectToString(Object object) {
        return Optional.ofNullable(object).map(Object::toString).orElse(null);
    }
}
