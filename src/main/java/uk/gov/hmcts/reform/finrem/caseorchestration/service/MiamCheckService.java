package uk.gov.hmcts.reform.finrem.caseorchestration.service;

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

    private static final String MIAM_LEGACY_OPTION_ERROR = "You have selected an outdated MIAM exemption option which " +
        "needs to be unchecked before you can continue.";

    public List<String> validateMiamFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<String> miamExemptionErrors = miamExemptionAttendanceCheck(caseData);
        if (!miamExemptionErrors.isEmpty()) {
            return miamExemptionErrors;
        }

        Map<String, List<String>> errors = new HashMap<>();
        addEvidenceUnavailableErrors(errors, caseData);
        addLegacyOptionErrors(errors, caseData);

        return errors.values().stream().filter(error -> !error.isEmpty()).findFirst().orElse(List.of());
    }

    private void addEvidenceUnavailableErrors(Map<String, List<String>> errors, Map<String, Object> caseData) {
        errors.put("MiamDomesticViolenceEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_DOMESTIC_VIOLENCE_CHECKLIST, MIAM_DOMESTIC_ABUSE_TEXTBOX, "FR_ms_MIAMDomesticViolenceChecklist_Value_23"));
        errors.put("MiamUrgencyEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_URGENCY_CHECKLIST, MIAM_URGENCY_TEXTBOX, "FR_ms_MIAMUrgencyReasonChecklist_Value_6"));
        errors.put("MiamPreviousAttendanceEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, MIAM_PREVIOUS_ATTENDANCE_TEXTBOX, "FR_ms_MIAMPreviousAttendanceChecklist_Value_3"));
        errors.put("MiamOtherGroundsEvidenceUnavailable", getMiamEvidenceUnavailableErrors(caseData,
            MIAM_OTHER_GROUNDS_CHECKLIST, MIAM_OTHER_GROUNDS_TEXTBOX, "FR_ms_MIAMOtherGroundsChecklist_Value_7"));
    }

    private void addLegacyOptionErrors(Map<String, List<String>> errors, Map<String, Object> caseData) {
        errors.put("MiamPreviousAttendanceLegacyOptions", getMiamLegacyErrors(caseData,
            MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, List.of("FR_ms_MIAMPreviousAttendanceChecklist_Value_4",
                "FR_ms_MIAMPreviousAttendanceChecklist_Value_5")));
        errors.put("MiamOtherGroundsLegacyOptions", getMiamLegacyErrors(caseData,
            MIAM_OTHER_GROUNDS_CHECKLIST, List.of("FR_ms_MIAMOtherGroundsChecklist_Value_8",
                "FR_ms_MIAMOtherGroundsChecklist_Value_9", "FR_ms_MIAMOtherGroundsChecklist_Value_10",
                "FR_ms_MIAMOtherGroundsChecklist_Value_11")));
    }

    private static List<String> miamExemptionAttendanceCheck(Map<String, Object> caseData) {
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

    private List<String> getMiamLegacyErrors(Map<String, Object> caseData, String checklistKey,
                                             List<String> checklistValue) {
        String checklist = Objects.toString(caseData.get(checklistKey));

        if (checklist != null && checklistValue.stream().anyMatch(checklist::contains)) {
            return List.of(MIAM_LEGACY_OPTION_ERROR);
        }

        return Collections.emptyList();
    }

    private String convertObjectToString(Object object) {
        return Optional.ofNullable(object).map(Object::toString).orElse(null);
    }
}