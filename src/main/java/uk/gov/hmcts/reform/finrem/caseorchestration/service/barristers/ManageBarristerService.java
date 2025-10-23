package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManageBarristerService {

    private final BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;

    /**
     * Gets the party that a barrister is being added or removed for in the Manage Barrister event.
     *
     * @param caseDetails the case details
     * @param caseRole    the case role of the logged-in user
     * @return the barrister party
     */
    public BarristerParty getManageBarristerParty(FinremCaseDetails caseDetails, CaseRole caseRole) {
        return switch (caseRole) {
            case CASEWORKER -> caseDetails.getData().getBarristerParty();
            case APP_SOLICITOR -> BarristerParty.APPLICANT;
            case RESP_SOLICITOR -> BarristerParty.RESPONDENT;
            case INTVR_SOLICITOR_1 -> BarristerParty.INTERVENER1;
            case INTVR_SOLICITOR_2 -> BarristerParty.INTERVENER2;
            case INTVR_SOLICITOR_3 -> BarristerParty.INTERVENER3;
            case INTVR_SOLICITOR_4 -> BarristerParty.INTERVENER4;
            default -> throw new IllegalStateException(String.format("Case ID %d: Unexpected case role value %s",
                caseDetails.getId(), caseRole));
        };
    }

    /**
     * Gets the case role to be applied when assigning a user to a case as a barrister for a specific party.
     *
     * @param barristerParty the barrister party
     * @return the case role
     */
    public CaseRole getBarristerCaseRole(BarristerParty barristerParty) {
        return switch (barristerParty) {
            case APPLICANT -> CaseRole.APP_BARRISTER;
            case RESPONDENT -> CaseRole.RESP_BARRISTER;
            case INTERVENER1 -> CaseRole.INTVR_BARRISTER_1;
            case INTERVENER2 -> CaseRole.INTVR_BARRISTER_2;
            case INTERVENER3 -> CaseRole.INTVR_BARRISTER_3;
            case INTERVENER4 -> CaseRole.INTVR_BARRISTER_4;
        };
    }

    /**
     * Gets the list of barristers updated in the Manage Barrister event. If no barristers are present then returns an empty list.
     *
     * @param caseData       the case data
     * @param barristerParty the barrister party
     * @return the list of barristers
     */
    public List<BarristerCollectionItem> getEventBarristers(FinremCaseData caseData, BarristerParty barristerParty) {
        BarristerCollectionWrapper wrapper = caseData.getBarristerCollectionWrapper();
        List<BarristerCollectionItem> barristers = switch (barristerParty) {
            case APPLICANT -> wrapper.getApplicantBarristers();
            case RESPONDENT -> wrapper.getRespondentBarristers();
            case INTERVENER1 -> wrapper.getIntvr1Barristers();
            case INTERVENER2 -> wrapper.getIntvr2Barristers();
            case INTERVENER3 -> wrapper.getIntvr3Barristers();
            case INTERVENER4 -> wrapper.getIntvr4Barristers();
        };

        return emptyIfNull(barristers);
    }

    /**
     * Adds IdAM user IDs to each barrister in the provided list by looking up their email addresses.
     *
     * @param barristers the list of barristers to update
     */
    public void addUserIdToBarristerData(List<BarristerCollectionItem> barristers) {
        String authToken = systemUserService.getSysUserToken();

        barristers.stream()
            .map(BarristerCollectionItem::getValue)
            .forEach(barrister -> organisationService.findUserByEmail(barrister.getEmail(), authToken)
                .ifPresent(barrister::setUserId));
    }

    /**
     * Calculates the changes made to barristers in the Manage Barrister event.
     *
     * @param caseDetails    the current case details
     * @param caseDataBefore the case data before the event
     * @param caseRole       the case role of the logged-in user
     * @return the barrister changes
     */
    public BarristerChange getBarristerChange(FinremCaseDetails caseDetails, FinremCaseData caseDataBefore,
                                              CaseRole caseRole) {
        BarristerParty barristerParty = getManageBarristerParty(caseDetails, caseRole);
        List<Barrister> barristers = getEventBarristers(caseDetails.getData(), barristerParty)
            .stream()
            .map(BarristerCollectionItem::getValue)
            .toList();
        List<Barrister> barristersBefore = getEventBarristers(caseDataBefore, barristerParty)
            .stream()
            .map(BarristerCollectionItem::getValue)
            .toList();

        return barristerUpdateDifferenceCalculator.calculate(barristerParty, barristersBefore, barristers);
    }
}
