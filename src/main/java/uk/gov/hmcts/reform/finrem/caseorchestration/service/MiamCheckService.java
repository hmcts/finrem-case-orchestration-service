package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_ABUSE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_VIOLENCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_TEXTBOX;

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
        if (miamExemptionErrors != null) {
            return miamExemptionErrors;
        }

        Map<String, List<String>> errors = new HashMap<>();
        addEvidenceUnavailableErrors(errors, caseData);

        return errors.values().stream().filter(Objects::nonNull).findFirst().orElse(List.of());
    }

    private void addEvidenceUnavailableErrors(Map<String, List<String>> errors, Map<String, Object> caseData) {
        errors.put(MIAM_DOMESTIC_VIOLENCE_CHECKLIST, getMiamEvidenceUnavailableErrors(caseData,
            MIAM_DOMESTIC_VIOLENCE_CHECKLIST, MIAM_DOMESTIC_ABUSE_TEXTBOX, "FR_ms_MIAMDomesticViolenceChecklist_Value_23"));
        errors.put(MIAM_URGENCY_CHECKLIST, getMiamEvidenceUnavailableErrors(caseData,
            MIAM_URGENCY_CHECKLIST, MIAM_URGENCY_TEXTBOX, "FR_ms_MIAMUrgencyReasonChecklist_Value_6"));
        errors.put(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, getMiamEvidenceUnavailableErrors(caseData,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, MIAM_PREVIOUS_ATTENDANCE_TEXTBOX, "FR_ms_MIAMPreviousAttendanceChecklist_Value_3"));
        errors.put(MIAM_OTHER_GROUNDS_CHECKLIST, getMiamEvidenceUnavailableErrors(caseData,
            MIAM_OTHER_GROUNDS_CHECKLIST, MIAM_OTHER_GROUNDS_TEXTBOX, "FR_ms_MIAMOtherGroundsChecklist_Value_7"));
    }

    private static List<String> miamExemptionAttendanceCheck(Map<String, Object> caseData) {
        String applicantAttended = Objects.toString(caseData.get(APPLICANT_ATTENDED_MIAM));
        String claimingExemption = Objects.toString(caseData.get(CLAIMING_EXEMPTION_MIAM));

        if (applicantAttended.equalsIgnoreCase("no") && claimingExemption.equalsIgnoreCase("no")) {
            return List.of(MIAM_EXEMPT_ERROR);
        }
        return null;
    }

    private List<String> getMiamEvidenceUnavailableErrors(Map<String, Object> caseData, String checklistKey,
                                                          String textboxKey, String checklistValue) {
        String checklist = Objects.toString(caseData.get(checklistKey));
        String textbox = convertObjectToString(caseData.get(textboxKey));

        if (checklist != null && checklist.contains(checklistValue) && StringUtils.isBlank(textbox)) {
            return List.of(MIAM_EVIDENCE_UNAVAILABLE_ERROR);
        }
        return null;
    }

    private String convertObjectToString(Object object) {
        return Optional.ofNullable(object).map(Object::toString).orElse(null);
    }
}