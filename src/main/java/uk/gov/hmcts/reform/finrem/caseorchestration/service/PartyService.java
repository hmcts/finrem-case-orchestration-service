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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        FinremCaseData data = caseDetails.getData();
        data.setApplicantCorrespondenceEnabled(isOrderSharedWithApplicant(caseDetails));
        data.setRespondentCorrespondenceEnabled(isOrderSharedWithRespondent(caseDetails));
        data.getIntervenerOne()
            .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener1(caseDetails));
        data.getIntervenerTwo()
            .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener2(caseDetails));
        data.getIntervenerThree()
            .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener3(caseDetails));
        data.getIntervenerFour()
            .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener4(caseDetails));
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
        return parties.getValue().stream().map(DynamicMultiSelectListElement::getCode).toList();
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
        FinremCaseData data = caseDetails.getData();
        return getCheckedActiveParties(caseDetails).contains(
            Optional.ofNullable(data.getApplicantOrganisationPolicy())
                .map(OrganisationPolicy::getOrgPolicyCaseAssignedRole)
                .orElse(""));
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
        FinremCaseData data = caseDetails.getData();
        return getCheckedActiveParties(caseDetails).contains(
            Optional.ofNullable(data.getRespondentOrganisationPolicy())
                .map(OrganisationPolicy::getOrgPolicyCaseAssignedRole)
                .orElse(""));
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

    private boolean isOrderSharedWithApplicant(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.APP_SOLICITOR.getCcdCode())
            || parties.contains(CaseRole.APP_BARRISTER.getCcdCode()));
    }

    private boolean isOrderSharedWithRespondent(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.RESP_SOLICITOR.getCcdCode())
            || parties.contains(CaseRole.RESP_BARRISTER.getCcdCode()));
    }

    private boolean isOrderSharedWithIntervener1(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_1.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_1.getCcdCode()));
    }

    private boolean isOrderSharedWithIntervener2(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_2.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_2.getCcdCode()));
    }

    private boolean isOrderSharedWithIntervener3(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_3.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_3.getCcdCode()));
    }

    private boolean isOrderSharedWithIntervener4(FinremCaseDetails caseDetails) {
        List<String> parties = getCheckedActiveParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_4.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_4.getCcdCode()));
    }
}
