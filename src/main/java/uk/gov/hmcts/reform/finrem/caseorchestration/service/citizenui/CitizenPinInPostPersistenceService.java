package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AccessCodeGenerator;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

/**
 * Handles persistence of Citizen UI access codes generated during postal notifications.
 *
 * <p>Postal notifications may be processed after the CCD event has completed,
 * so newly generated PINs must be saved through a separate CCD update.
 *
 * <p>Only the relevant party's access codes are updated. The persisted codes
 * are then copied back to the current case data to prevent duplicate generation
 * during subsequent cover sheet creation.
 */
@Service
@RequiredArgsConstructor
public class CitizenPinInPostPersistenceService {

    private final CoreCaseDataService coreCaseDataService;
    private final FinremCaseDetailsMapper caseDetailsMapper;
    private final CitizenPinInPostService citizenPinInPostService;
    private final FeatureToggleService featureToggleService;

    /**
     * Ensures that an eligible applicant or respondent has a PIN persisted in CCD.
     * Existing valid PINs are retained without generating a replacement.
     *
     * @param caseDetails the case being processed for postal notification
     * @param recipient the party receiving the postal notification
     */
    public void ensurePinPersisted(FinremCaseDetails caseDetails,
                                   PaperNotificationRecipient recipient) {

        if (caseDetails.getCaseType() != CaseType.CONTESTED
            || (recipient != APPLICANT && recipient != RESPONDENT)
            || !featureToggleService.isFinremCitizenUiEnabled()) {
            return;
        }

        if (!AccessCodeGenerator.doesRecipientNeedNewAccessCode(
            caseDetails.getData(), recipient)) {
            return;
        }

        CaseDetails updatedCase = coreCaseDataService.performPostSubmitCallback(
            caseDetails.getCaseType(),
            caseDetails.getId(),
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(),
            latestCase -> generatePinUpdate(latestCase, recipient)
        );

        copyAccessCodesToCase(caseDetails, updatedCase, recipient);
    }

    private Map<String, Object> generatePinUpdate(
        CaseDetails latestCase,
        PaperNotificationRecipient recipient) {

        FinremCaseDetails latestDetails =
            caseDetailsMapper.mapToFinremCaseDetails(latestCase);

        if (!citizenPinInPostService.ensurePinForRecipient(
            latestDetails, recipient)) {
            return Collections.emptyMap();
        }

        Map<String, Object> updatedData =
            caseDetailsMapper.finremCaseDataToMap(latestDetails.getData());

        String fieldName = recipient == APPLICANT
            ? "applicantAccessCodes"
            : "respondentAccessCodes";

        return Map.of(fieldName, updatedData.get(fieldName));
    }

    private void copyAccessCodesToCase(
        FinremCaseDetails caseDetails,
        CaseDetails updatedCase,
        PaperNotificationRecipient recipient) {

        FinremCaseData latestData = caseDetailsMapper.mapToFinremCaseData(
            updatedCase.getData(),
            updatedCase.getCaseTypeId()
        );

        if (recipient == APPLICANT) {
            caseDetails.getData().setApplicantAccessCodes(
                latestData.getApplicantAccessCodes()
            );
        } else {
            caseDetails.getData().setRespondentAccessCodes(
                latestData.getRespondentAccessCodes()
            );
        }
    }
}
