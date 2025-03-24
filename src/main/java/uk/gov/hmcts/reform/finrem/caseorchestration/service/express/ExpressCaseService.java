package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LabelForExpressCaseAmendment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXPRESS_CASE_PARTICIPATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.WITHDRAWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Service
@RequiredArgsConstructor
public class ExpressCaseService {

    @Value("${finrem.expressCase.frcs}")
    private List<String> expressCaseFrcs;

    private final FeatureToggleService featureToggleService;

    /**
     * Used to withdraw a case from being processed as an Express case.
     *
     * @param caseData the case data
     */
    public void setExpressCaseEnrollmentStatusToWithdrawn(FinremCaseData caseData) {
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(WITHDRAWN);
    }

    /**
     * Sets the Express Case participation status based on qualifying criteria.
     * If the Case is suitable to process as an Express Case, then the status is set to ENROLLED
     * If the Case is unsuitable to process as an Express Case, then the status is set to DOES_NOT_QUALIFY
     *
     * @param caseData the case data as an instance of FinremCaseDate
     */
    public void setExpressCaseEnrollmentStatus(FinremCaseData caseData) {
        caseData.getExpressCaseWrapper().setExpressCaseParticipation((qualifiesForExpress(caseData) ? ENROLLED : DOES_NOT_QUALIFY));
    }

    /**
     * Used when amending an application.
     * Picks the label that a User should see, depending on how they have changed
     * their case with regard to the express pilot.
     * If the Case was suitable to process as an Express Case, but isn't now, then
     * labelForExpressCaseAmendment is set to UNSUITABLE_FOR_EXPRESS_LABEL.
     * If the Case becomes or remains suitable to process as an Express Case, then
     * labelForExpressCaseAmendment is set to SUITABLE_FOR_EXPRESS_LABEL.
     * If was never suitable to process as an Express Case and still isn't, then
     * labelForExpressCaseAmendment is set to SHOW_NEITHER_PAGE_NOR_LABEL.

     * show the right content to a user.
     * @param amendedCaseData newly amended case data
     * @param caseDataBeforeAmending the data after the last submitted event
     */
    public void setWhichExpressCaseAmendmentLabelToShow(FinremCaseData amendedCaseData, FinremCaseData caseDataBeforeAmending) {

        ExpressCaseParticipation statusBefore = caseDataBeforeAmending.getExpressCaseWrapper().getExpressCaseParticipation();
        ExpressCaseParticipation statusNow = amendedCaseData.getExpressCaseWrapper().getExpressCaseParticipation();

        if (ENROLLED.equals(statusNow)) {
            amendedCaseData.getExpressCaseWrapper().setLabelForExpressCaseAmendment(LabelForExpressCaseAmendment.SUITABLE_FOR_EXPRESS_LABEL);
        } else if (ENROLLED.equals(statusBefore) && DOES_NOT_QUALIFY.equals(statusNow)) {
            amendedCaseData.getExpressCaseWrapper().setLabelForExpressCaseAmendment(LabelForExpressCaseAmendment.UNSUITABLE_FOR_EXPRESS_LABEL);
        } else {
            amendedCaseData.getExpressCaseWrapper().setLabelForExpressCaseAmendment(LabelForExpressCaseAmendment.SHOW_NEITHER_PAGE_NOR_LABEL);
        }
    }

    /**
     * Checks if the case is enrolled in the express case pilot.
     *
     * @param caseDetails the legacy case details
     * @return true if the case is enrolled in the express case pilot and the express pilot feature is enabled, false otherwise
     */
    public boolean isExpressCase(CaseDetails caseDetails) {
        ExpressCaseParticipation expressCaseParticipation = Optional.ofNullable(caseDetails.getData().get(EXPRESS_CASE_PARTICIPATION))
            .map(Object::toString)
            .map(ExpressCaseParticipation::forValue)
            .orElse(DOES_NOT_QUALIFY);

        return featureToggleService.isExpressPilotEnabled()
            && ExpressCaseParticipation.ENROLLED.equals(expressCaseParticipation);
    }

    /**
     * Checks if the case is enrolled in the express case pilot.
     *
     * @param caseData the FinRem case data
     * @return true if the case is enrolled in the express case pilot and the express pilot feature is enabled, false otherwise
     */
    public boolean isExpressCase(FinremCaseData caseData) {
        ExpressCaseParticipation expressCaseParticipation =
                Optional.ofNullable(caseData.getExpressCaseWrapper().getExpressCaseParticipation())
                        .orElse(DOES_NOT_QUALIFY);

        return featureToggleService.isExpressPilotEnabled()
            && ExpressCaseParticipation.ENROLLED.equals(expressCaseParticipation);
    }

    /**
     * Determines if the case qualifies for express case participation based on several conditions.
     *  - is within a participating FRC
     *  - is matrimonial application
     *  - has asset value under 250k
     *  - does not include variation order
     *  - is not marked for fast track procedure.
     *
     * @param caseData the case data
     * @return true if the case qualifies for express case participation, false otherwise
     */
    private boolean qualifiesForExpress(FinremCaseData caseData) {

        List<NatureApplication> natureOfApplicationCheckList = caseData
            .getNatureApplicationWrapper().getNatureOfApplicationChecklist();

        Schedule1OrMatrimonialAndCpList typeOfApplication = caseData.getScheduleOneWrapper().getTypeOfApplication();

        EstimatedAssetV2 assetValue = caseData.getEstimatedAssetsChecklistV2();

        return caseHasExpressParticipatingCourt(caseData)
            && MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.equals(typeOfApplication)
            && UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS.equals(assetValue)
            && !ListUtils.emptyIfNull(natureOfApplicationCheckList).contains(CONTESTED_VARIATION_ORDER)
            && YesOrNo.isNoOrNull(caseData.getFastTrackDecision());
    }

    /* Checks that a region has been selected before calling getSelectedAllocatedCourt, as an indication
     * that the User has selected their court.  Without that, mapping in getSelectedAllocatedCourt fails.
     */
    private boolean caseHasExpressParticipatingCourt(FinremCaseData caseData) {

        if (caseData.getRegionWrapper().getAllocatedRegionWrapper().getRegionList() == null) {
            return false;
        }

        String selectedCourtId = caseData.getSelectedAllocatedCourt();

        return selectedCourtId != null && expressCaseFrcs.stream()
               .map(String::trim)
               .anyMatch(court -> court.equalsIgnoreCase(selectedCourtId.trim()));
    }
}
