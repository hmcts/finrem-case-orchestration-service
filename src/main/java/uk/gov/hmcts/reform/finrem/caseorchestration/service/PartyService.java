package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DISPLAY_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyService {

    /**
     * Updates the correspondence-enabled flags for the applicant, respondent and each
     * intervener based on the parties checked in a party-selection question such as
     * "Who should receive this order?".
     *
     * <p>
     * A party's flag is set to {@code true} if either their solicitor or their barrister
     * role code is among the checked parties returned by
     * {@link #getCheckedActiveParties(FinremCaseDetails)}, and {@code false} otherwise.
     * Every flag is set on each call, so parties who are not checked are explicitly
     * disabled rather than left unchanged.
     * </p>
     *
     * <p>
     * This method modifies the case data in place. It assumes that the intervener wrappers
     * for interveners one to four are present on the case data.
     * </p>
     *
     * @param caseDetails the case details containing the party selection, which is updated
     *                    with the correspondence-enabled flags
     */
    public void updateCorrespondenceEnabledFromSelectedParties(FinremCaseDetails caseDetails) {
        applyCorrespondenceEnabled(caseDetails.getData(), getCheckedActiveParties(caseDetails));
    }

    /**
     * Updates the correspondence-enabled flags as above, using the parties checked on the
     * given working hearing, as returned by {@link #getCheckedActiveParties(WorkingHearing)}.
     *
     * @param finremCaseData the case data to update with the correspondence-enabled flags
     * @param workingHearing the working hearing containing the party selection
     */
    public void updateCorrespondenceEnabledFromSelectedParties(FinremCaseData finremCaseData,
                                                               WorkingHearing workingHearing) {
        applyCorrespondenceEnabled(finremCaseData, getCheckedActiveParties(workingHearing));
    }

    /**
     * Returns the role codes of the active parties that have been checked in the
     * working hearing's party-selection question, for example "Who should see this order?"
     *
     * <p>The codes are taken from the selected values of the working hearing's
     * {@code partiesOnCaseMultiSelectList}, for example {@code [APP_SOLICITOR]}.
     *
     * @param caseDetails the case details containing the working hearing
     * @return the role codes of the checked parties, or an empty list if none are selected
     */
    public List<String> getCheckedActivePartiesForWorkingHearing(FinremCaseDetails caseDetails) {
        return getCheckedCodes(caseDetails, data ->
            data.getManageHearingsWrapper().getWorkingHearing().getPartiesOnCaseMultiSelectList());
    }

    /**
     * Returns the role codes of the active parties that have been checked in a
     * party-selection question such as "Who should receive this order?".
     *
     * <p>The codes are taken from the selected values of the {@code partiesOnCase}
     * multi-select list, for example {@code [APP_SOLICITOR]}.
     *
     * @param caseDetails the case details containing the party selection
     * @return the role codes of the checked parties
     */
    public List<String> getCheckedActiveParties(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        DynamicMultiSelectList parties = data.getPartiesOnCase();
        return emptyIfNull(parties.getValue()).stream().map(DynamicMultiSelectListElement::getCode).toList();
    }

    /**
     * Returns the role codes of the active parties that have been checked in the
     * party-selection question on the given working hearing, such as "Who should
     * receive this order?".
     *
     * <p>The codes are taken from the selected values of the working hearing's
     * {@code partiesOnCaseMultiSelectList}, for example {@code [APP_SOLICITOR]}.
     *
     * @param workingHearing the working hearing containing the party selection
     * @return the role codes of the checked parties
     */
    public List<String> getCheckedActiveParties(WorkingHearing workingHearing) {
        DynamicMultiSelectList parties = workingHearing.getPartiesOnCaseMultiSelectList();
        return emptyIfNull(parties.getValue()).stream().map(DynamicMultiSelectListElement::getCode).toList();
    }

    /**
     * Checks whether the applicant has been selected in a party-selection
     * question such as "Who should receive this order?".
     *
     * <p>The applicant's role is taken from the case-assigned role on the
     * applicant organisation policy and compared with the checked party codes
     * returned by {@link #getCheckedActiveParties(FinremCaseDetails)}. If the
     * applicant organisation policy or its role is not set, the applicant is
     * treated as not selected.
     *
     * @param caseDetails the case details containing the party selection
     * @return {@code true} if the applicant is among the checked parties,
     *         {@code false} otherwise
     */
    public boolean isApplicantPartySelected(FinremCaseDetails caseDetails) {
        return isPolicyRoleChecked(caseDetails.getData().getApplicantOrganisationPolicy(),
            getCheckedActiveParties(caseDetails));
    }

    /**
     * Checks whether the applicant has been selected in the party-selection
     * question on the given working hearing, such as "Who should receive this
     * order?".
     *
     * <p>The applicant's role is taken from the case-assigned role on the
     * applicant organisation policy of the given case data and compared with the
     * checked party codes returned by
     * {@link #getCheckedActiveParties(WorkingHearing)}. If the applicant
     * organisation policy or its role is not set, the applicant is treated as not
     * selected.
     *
     * @param finremCaseData the case data containing the applicant organisation policy
     * @param workingHearing the working hearing containing the party selection
     * @return {@code true} if the applicant is among the checked parties,
     *         {@code false} otherwise
     */
    public boolean isApplicantPartySelected(FinremCaseData finremCaseData, WorkingHearing workingHearing) {
        return isPolicyRoleChecked(finremCaseData.getApplicantOrganisationPolicy(),
            getCheckedActiveParties(workingHearing));
    }

    /**
     * Checks whether the respondent has been selected in a party-selection
     * question such as "Who should receive this order?".
     *
     * <p>The respondent's role is taken from the case-assigned role on the
     * respondent organisation policy and compared with the checked party codes
     * returned by {@link #getCheckedActiveParties(FinremCaseDetails)}. If the
     * respondent organisation policy or its role is not set, the respondent is
     * treated as not selected.
     *
     * @param caseDetails the case details containing the party selection
     * @return {@code true} if the respondent is among the checked parties,
     *         {@code false} otherwise
     */
    public boolean isRespondentPartySelected(FinremCaseDetails caseDetails) {
        return isPolicyRoleChecked(caseDetails.getData().getRespondentOrganisationPolicy(),
            getCheckedActiveParties(caseDetails));
    }

    /**
     * Checks whether the respondent has been selected in the party-selection
     * question on the given working hearing, such as "Who should receive this
     * order?".
     *
     * <p>The respondent's role is taken from the case-assigned role on the
     * respondent organisation policy of the given case data and compared with the
     * checked party codes returned by
     * {@link #getCheckedActiveParties(WorkingHearing)}. If the respondent
     * organisation policy or its role is not set, the respondent is treated as not
     * selected.
     *
     * @param finremCaseData the case data containing the respondent organisation policy
     * @param workingHearing the working hearing containing the party selection
     * @return {@code true} if the respondent is among the checked parties,
     *         {@code false} otherwise
     */
    public boolean isRespondentPartySelected(FinremCaseData finremCaseData, WorkingHearing workingHearing) {
        return isPolicyRoleChecked(finremCaseData.getRespondentOrganisationPolicy(),
            getCheckedActiveParties(workingHearing));
    }

    /**
     * Builds the list of all active parties on the case, used to populate
     * party-selection checkboxes such as "Who should receive this order?".
     *
     * <p>Delegates to {@link #getAllActivePartyList(FinremCaseData)} using the
     * data from the supplied case details.
     *
     * @param caseDetails the case details containing the case data
     * @return a {@link DynamicMultiSelectList} of the active parties that can be selected,
     *         with the applicant and respondent parties pre-selected
     * @see #getAllActivePartyList(FinremCaseData)
     */
    public DynamicMultiSelectList getAllActivePartyList(FinremCaseDetails caseDetails) {
        return getAllActivePartyList(caseDetails.getData());
    }

    /**
     * Builds the list of all active parties on the case, used to populate
     * party-selection checkboxes such as "Who should receive this order?".
     *
     * <p>The returned list items contain, in order:
     * <ol>
     *   <li>active solicitors (applicant and respondent),</li>
     *   <li>unrepresented parties, and</li>
     *   <li>active interveners.</li>
     * </ol>
     *
     * <p>Only the solicitors and unrepresented parties are pre-selected in the
     * returned {@link DynamicMultiSelectList#getValue() value}. Interveners are
     * available to select but are not selected by default.
     *
     * @param caseData the case data used to determine which parties are active
     * @return a {@link DynamicMultiSelectList} whose list items are all active parties
     *         and whose value is the pre-selected subset (solicitors and unrepresented parties)
     */
    public DynamicMultiSelectList getAllActivePartyList(FinremCaseData caseData) {
        List<DynamicMultiSelectListElement> activeSolicitors = getActiveSolicitors(caseData);
        List<DynamicMultiSelectListElement> unrepresentedParties = getUnrepresentedParties(caseData);

        List<DynamicMultiSelectListElement> selectedActiveCaseParties = new ArrayList<>();
        selectedActiveCaseParties.addAll(activeSolicitors);
        selectedActiveCaseParties.addAll(unrepresentedParties);

        List<DynamicMultiSelectListElement> activeCaseParties = new ArrayList<>();
        activeCaseParties.addAll(activeSolicitors);
        activeCaseParties.addAll(unrepresentedParties);
        activeCaseParties.addAll(getActiveInterveners(caseData));

        return DynamicMultiSelectList.builder()
            .value(selectedActiveCaseParties)
            .listItems(activeCaseParties)
            .build();
    }

    private List<DynamicMultiSelectListElement> getActiveSolicitors(FinremCaseData caseData) {
        List<DynamicMultiSelectListElement> activeSolicitors = new ArrayList<>();
        if (caseData.getApplicantOrganisationPolicy() != null
            && caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedAppRole = caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement appMultiSelectListElement = getDynamicMultiSelectListElement(assignedAppRole,
                DISPLAY_LABEL.formatted(APPLICANT, caseData.getFullApplicantName()));
            activeSolicitors.add(appMultiSelectListElement);
        }

        if (caseData.getRespondentOrganisationPolicy() != null
            && caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedRespRole = caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement respMultiSelectListElement = getDynamicMultiSelectListElement(assignedRespRole,
                DISPLAY_LABEL.formatted(RESPONDENT, caseData.getRespondentFullName()));
            activeSolicitors.add(respMultiSelectListElement);
        }

        return activeSolicitors;
    }

    private List<DynamicMultiSelectListElement> getUnrepresentedParties(FinremCaseData caseData) {

        List<DynamicMultiSelectListElement> unrepresentedParties = new ArrayList<>();

        if (!caseData.isApplicantRepresentedByASolicitor() && caseData.getApplicantOrganisationPolicy() == null) {
            DynamicMultiSelectListElement appMultiSelectListElement = getDynamicMultiSelectListElement(CaseRole.APP_SOLICITOR.getCcdCode(),
                DISPLAY_LABEL.formatted(APPLICANT, caseData.getFullApplicantName()));
            unrepresentedParties.add(appMultiSelectListElement);
        }

        if (!caseData.isRespondentRepresentedByASolicitor() && caseData.getRespondentOrganisationPolicy() == null) {
            DynamicMultiSelectListElement respMultiSelectListElement = getDynamicMultiSelectListElement(CaseRole.RESP_SOLICITOR.getCcdCode(),
                DISPLAY_LABEL.formatted(RESPONDENT, caseData.getRespondentFullName()));
            unrepresentedParties.add(respMultiSelectListElement);
        }

        return unrepresentedParties;
    }

    private List<DynamicMultiSelectListElement> getActiveInterveners(FinremCaseData caseData) {

        List<DynamicMultiSelectListElement> activeInterveners = new ArrayList<>();

        IntervenerWrapper oneWrapper = caseData.getIntervenerOneWrapperIfPopulated();
        setIntervener(activeInterveners, oneWrapper, INTERVENER_ONE);

        IntervenerWrapper twoWrapper = caseData.getIntervenerTwoWrapperIfPopulated();
        setIntervener(activeInterveners, twoWrapper, INTERVENER_TWO);

        IntervenerWrapper threeWrapper = caseData.getIntervenerThreeWrapperIfPopulated();
        setIntervener(activeInterveners, threeWrapper, INTERVENER_THREE);

        IntervenerWrapper fourWrapper = caseData.getIntervenerFourWrapperIfPopulated();
        setIntervener(activeInterveners, fourWrapper, INTERVENER_FOUR);

        return activeInterveners;
    }

    private void setIntervener(List<DynamicMultiSelectListElement> activeCaseParties,
                               IntervenerWrapper wrapper,
                               String intervener) {
        if (wrapper != null && ObjectUtils.isNotEmpty(wrapper.getIntervenerOrganisation())) {
            String assignedRole = wrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            activeCaseParties.add(getDynamicMultiSelectListElement(assignedRole,
                DISPLAY_LABEL.formatted(capitalize(intervener), wrapper.getIntervenerName())));
        }
    }

    private DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private void applyCorrespondenceEnabled(FinremCaseData data, List<String> activeParties) {
        data.setApplicantCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.APP_SOLICITOR, CaseRole.APP_BARRISTER));
        data.setRespondentCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.RESP_SOLICITOR, CaseRole.RESP_BARRISTER));
        data.getIntervenerOne().setIntervenerCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.INTVR_SOLICITOR_1, CaseRole.INTVR_BARRISTER_1));
        data.getIntervenerTwo().setIntervenerCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.INTVR_SOLICITOR_2, CaseRole.INTVR_BARRISTER_2));
        data.getIntervenerThree().setIntervenerCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.INTVR_SOLICITOR_3, CaseRole.INTVR_BARRISTER_3));
        data.getIntervenerFour().setIntervenerCorrespondenceEnabled(
            isSharedWithAny(activeParties, CaseRole.INTVR_SOLICITOR_4, CaseRole.INTVR_BARRISTER_4));
    }

    private static boolean isPolicyRoleChecked(OrganisationPolicy policy, List<String> checkedParties) {
        return checkedParties.contains(
            ofNullable(policy)
                .map(OrganisationPolicy::getOrgPolicyCaseAssignedRole)
                .orElse(""));
    }

    private static boolean isSharedWithAny(List<String> activeParties, CaseRole... roles) {
        return Arrays.stream(roles)
            .map(CaseRole::getCcdCode)
            .anyMatch(activeParties::contains);
    }
}
