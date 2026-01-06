package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.getIntervenerPartyByType;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerService {

    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final IdamService idamService;

    /**
     * Revokes an intervener solicitor role for the given case.
     *
     * <p>
     * The role is revoked only if both the intervener organisation ID and
     * solicitor email address are present and not blank.
     * If either value is missing or empty, the method returns without
     * performing any action.
     *
     * @param caseId            the CCD case ID
     * @param intervenerWrapper the intervener details containing organisation,
     *                          solicitor email, and case role information
     */
    public void revokeIntervenerSolicitor(long caseId, IntervenerWrapper intervenerWrapper) {
        String orgId = ofNullable(intervenerWrapper.getIntervenerOrganisation())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null);

        String email = intervenerWrapper.getIntervenerSolEmail();

        if (!StringUtils.hasText(orgId) || !StringUtils.hasText(email)) {
            return;
        }

        revokeIntervenerRole(caseId, email, orgId, intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode());
    }

    public IntervenerChangeDetails removeIntervenerDetails(IntervenerWrapper intervenerWrapper,
                                                           List<String> errors,
                                                           FinremCaseData caseData,
                                                           Long caseId) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(intervenerWrapper);

        if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.YES)) {
            log.info("Revoke case role for {} for Case ID: {}", intervenerWrapper.getIntervenerSolicitorCaseRole(), caseId);
            String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            String email = intervenerWrapper.getIntervenerSolEmail();
            revokeIntervenerRole(caseId, email, orgId,
                intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
        }

        intervenerWrapper.removeIntervenerWrapperFromCaseData(caseData);
        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails updateIntervenerDetails(IntervenerWrapper intervenerWrapper,
                                                           List<String> errors,
                                                           FinremCallbackRequest callbackRequest) {
        validateIntervenerCountryOfResident(intervenerWrapper, errors);
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());

        Long caseId = callbackRequest.getCaseDetails().getId();
        if (intervenerWrapper.getIntervenerDateAdded() == null) {
            log.info("{} date intervener added to Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
            intervenerWrapper.setIntervenerDateAdded(LocalDate.now());
        }

        final String caseRole = intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        if (isRepresented(intervenerWrapper)) {
            log.info("Add {} case role for Case ID: {}", caseRole, caseId);
            String orgId = intervenerWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            String email = intervenerWrapper.getIntervenerSolEmail();
            checkIfIntervenerSolicitorDetailsChanged(intervenerWrapper, caseDetailsBefore, orgId, email, errors);
            addIntervenerRole(caseId, email, orgId, caseRole, errors);
        } else {
            FinremCaseData beforeData = caseDetailsBefore.getData();
            IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);
            if (ObjectUtils.isNotEmpty(beforeIntv)
                && isRepresented(beforeIntv)) {

                log.info("{} now not represented for Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
                revokeIntervenerRole(caseId, beforeIntv.getIntervenerSolEmail(),
                    beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID(),
                    intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
                intervenerWrapper.setIntervenerSolEmail(null);
                intervenerWrapper.setIntervenerSolName(null);
                intervenerWrapper.setIntervenerSolPhone(null);
                intervenerWrapper.setIntervenerSolicitorFirm(null);
                intervenerWrapper.setIntervenerSolicitorReference(null);
            }
            log.info("{} add default case role and organisation for Case ID: {}", intervenerWrapper.getIntervenerType(), caseId);
            setDefaultOrgForintervener(intervenerWrapper);
        }
        intervenerChangeDetails.setIntervenerDetails(intervenerWrapper);
        return intervenerChangeDetails;
    }

    private boolean isRepresented(IntervenerWrapper intervenerWrapper) {
        return YesOrNo.YES.equals(intervenerWrapper.getIntervenerRepresented());
    }

    private void validateIntervenerCountryOfResident(IntervenerWrapper intervenerWrapper, List<String> errors) {
        if (intervenerWrapper.getIntervenerRepresented().equals(YesOrNo.NO)) {
            YesOrNo intervenerResideOutsideUK = intervenerWrapper.getIntervenerResideOutsideUK();
            String country = intervenerWrapper.getIntervenerAddress() != null
                ? intervenerWrapper.getIntervenerAddress().getCountry() : "";
            if (ObjectUtils.isNotEmpty(intervenerResideOutsideUK) && intervenerResideOutsideUK.equals(YesOrNo.YES) && ObjectUtils.isEmpty(country)) {
                errors.add("If intervener resides outside of UK, please provide the country of residence.");
            }
        }
    }

    private void checkIfIntervenerSolicitorDetailsChanged(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetailsBefore, String orgId,
                                                          String email, List<String> errors) {
        FinremCaseData beforeData = caseDetailsBefore.getData();
        IntervenerWrapper beforeIntv = intervenerWrapper.getIntervenerWrapperFromCaseData(beforeData);

        if (ObjectUtils.isNotEmpty(beforeIntv)
            && isRepresented(beforeIntv)) {
            String beforeOrgId = beforeIntv.getIntervenerOrganisation().getOrganisation().getOrganisationID();
            if (ObjectUtils.notEqual(beforeOrgId, orgId) || !beforeIntv.getIntervenerSolEmail().equals(email)) {
                revokeIntervenerRole(caseDetailsBefore.getId(), beforeIntv.getIntervenerSolEmail(),
                    beforeOrgId,
                    intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode(), errors);
            }
        }
    }

    private boolean checkIfIntervenerOneSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerOne().getIntervenerRepresented())
            && (caseData.getIntervenerOne().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerOne().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerTwoSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerTwo().getIntervenerRepresented())
            && (caseData.getIntervenerTwo().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerTwo().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerThreeSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerThree().getIntervenerRepresented())
            && (caseData.getIntervenerThree().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerThree().getIntervenerRepresented()));
    }

    private boolean checkIfIntervenerFourSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return YesOrNo.YES.equals(caseDataBefore.getIntervenerFour().getIntervenerRepresented())
            && (caseData.getIntervenerFour().getIntervenerRepresented() == null
            || YesOrNo.NO.equals(caseData.getIntervenerFour().getIntervenerRepresented()));
    }

    public boolean checkIfAnyIntervenerSolicitorRemoved(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        return checkIfIntervenerOneSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerTwoSolicitorRemoved(caseData, caseDataBefore)
            || checkIfIntervenerThreeSolicitorRemoved(caseData, caseDataBefore) || checkIfIntervenerFourSolicitorRemoved(caseData, caseDataBefore);
    }

    private void setDefaultOrgForintervener(IntervenerWrapper intervenerWrapper) {
        Organisation organisation = Organisation.builder().organisationID(null).organisationName(null).build();
        intervenerWrapper.getIntervenerOrganisation().setOrganisation(organisation);
        intervenerWrapper.getIntervenerOrganisation().setOrgPolicyCaseAssignedRole(intervenerWrapper.getIntervenerSolicitorCaseRole().getCcdCode());
        intervenerWrapper.getIntervenerOrganisation().setOrgPolicyReference(null);
    }

    public IntervenerChangeDetails setIntervenerAddedChangeDetails(IntervenerWrapper intervenerWrapper) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(
            intervenerWrapper);

        return intervenerChangeDetails;
    }

    public IntervenerChangeDetails setIntervenerRemovedChangeDetails(IntervenerWrapper intervenerWrapper) {
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        intervenerChangeDetails.setIntervenerType(intervenerWrapper.getIntervenerType());
        intervenerChangeDetails.setIntervenerDetails(
            intervenerWrapper);
        return intervenerChangeDetails;
    }

    /**
     * Updates the representation update history when an intervener’s solicitor
     * stops representing their client.
     *
     * <p>The method compares the original and current case data for each of the
     * four interveners (Intervener 1–4). If an intervener was previously marked as
     * represented and is now marked as not represented, a
     * {@code STOP_REPRESENTING_CLIENT} history entry is generated.</p>
     *
     * <p>The history entry records the user performing the action and the details
     * of the removed solicitor, and is appended to the existing representation
     * update history on the current {@link FinremCaseData}.</p>
     *
     * @param finremCaseData the current case data to be updated
     * @param originalFinremCaseData the original case data used to detect changes
     *                                in intervener representation
     * @param userAuthorisation the authorisation token of the user making the change
     */
    public void updateIntervenerSolicitorStopRepresentingHistory(FinremCaseData finremCaseData, FinremCaseData originalFinremCaseData,
                                                                 String userAuthorisation) {
        IntStream.range(0, 4).forEach(i -> {
            IntervenerWrapper originalIntervener = originalFinremCaseData.getInterveners().get(i);
            IntervenerWrapper currentIntervener = finremCaseData.getInterveners().get(i);

            boolean hasChange =
                YesOrNo.isYes(originalIntervener.getIntervenerRepresented())
                    && YesOrNo.isNo(currentIntervener.getIntervenerRepresented());

            if (!hasChange) {
                return;
            }

            RepresentationUpdateHistory history =
                changeOfRepresentationService.generateRepresentationUpdateHistory(
                    ChangeOfRepresentationRequest.builder()
                        .by(idamService.getIdamFullName(userAuthorisation))
                        .party(getIntervenerPartyByType(originalIntervener.getIntervenerType()))
                        .removedRepresentative(
                            ChangedRepresentative.builder()
                                .name(originalIntervener.getIntervenerSolName())
                                .email(originalIntervener.getIntervenerSolEmail())
                                .organisation(
                                    ofNullable(originalIntervener.getIntervenerOrganisation())
                                        .map(OrganisationPolicy::getOrganisation)
                                        .orElse(null)
                                )
                                .build()
                        )
                        .build(),
                    STOP_REPRESENTING_CLIENT
                );

            finremCaseData.setRepresentationUpdateHistory(
                new ArrayList<>(Stream.concat(
                    emptyIfNull(finremCaseData.getRepresentationUpdateHistory()).stream(),
                    emptyIfNull(history.getRepresentationUpdateHistory()).stream()
                        .map(e -> RepresentationUpdateHistoryCollection.builder()
                            .id(e.getId())
                            .value(e.getValue())
                            .build())
                ).toList())
            );
        });
    }

    private void addIntervenerRole(Long caseId, String email, String orgId, String caseRole, List<String> errors) {
        Optional<String> userId = organisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        if (userId.isPresent()) {
            assignCaseAccessService.grantCaseRoleToUser(caseId, userId.get(), caseRole, orgId);
        } else {
            logError(caseId, errors);
        }
    }

    private void revokeIntervenerRole(Long caseId, String email, String orgId, String caseRole) {
        revokeIntervenerRole(caseId, email, orgId, caseRole, null);
    }

    private void revokeIntervenerRole(Long caseId, String email, String orgId, String caseRole, List<String> errors) {
        Optional<String> userId = organisationService.findUserByEmail(email, systemUserService.getSysUserToken());
        if (userId.isPresent()) {
            assignCaseAccessService.removeCaseRoleToUser(caseId, userId.get(), caseRole, orgId);
        } else {
            logError(caseId, errors);
        }
    }

    private void logError(Long caseId, List<String> errors) {
        String error = "Could not find intervener with provided email";
        log.info(String.format(error + " for caseId %s", caseId));
        if (errors != null) {
            errors.add(error);
        }
    }
}
