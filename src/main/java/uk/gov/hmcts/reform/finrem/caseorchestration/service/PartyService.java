package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;

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

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public DynamicMultiSelectList getActiveIntervenersMultiselectList(CaseDetails caseDetails) {
        return getActiveIntervenersMultiselectList(
            finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails).getData());
    }

    public DynamicMultiSelectList getActiveIntervenersMultiselectList(FinremCaseData caseData) {
        return DynamicMultiSelectList.builder()
            .value(new ArrayList<>())
            .listItems(getActiveInterveners(caseData))
            .build();
    }

    public DynamicMultiSelectList getActiveSolicitorsMultiselectList(FinremCaseData caseData) {
        return DynamicMultiSelectList.builder()
            .value(new ArrayList<>())
            .listItems(getActiveSolicitors(caseData))
            .build();
    }

    public DynamicMultiSelectList getAllActivePartyMultiselectList(FinremCaseDetails caseDetails) {
        log.info("Event {} fetching all parties solicitor case role for Case ID: {}", EventType.SEND_ORDER, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> activeCaseParties = new ArrayList<>();

        activeCaseParties.addAll(getActiveSolicitors(caseData));
        activeCaseParties.addAll(getActiveInterveners(caseData));

        return DynamicMultiSelectList.builder()
            .value(new ArrayList<>())
            .listItems(activeCaseParties)
            .build();
    }

    public void addDefaultNotificationPartiesToCase(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        addDefaultNotificationPartiesToCase(finremCaseDetails.getData());
        caseDetails.setData(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails).getData());
    }

    public void addDefaultNotificationPartiesToCase(FinremCaseData finremCaseData) {
        List<DynamicMultiSelectListElement> activeSolicitors =
            getActiveSolicitorsMultiselectList(finremCaseData).getListItems();
        if (!activeSolicitors.isEmpty()) {

            finremCaseData.getPartiesOnCase().getListItems().addAll(
                activeSolicitors);
            finremCaseData.getPartiesOnCase().getValue().addAll(
                activeSolicitors);
        }
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

    private List<DynamicMultiSelectListElement> getActiveInterveners(FinremCaseData caseData) {

        List<DynamicMultiSelectListElement> activeIntervenerParties = new ArrayList<>();

        IntervenerWrapper oneWrapper = caseData.getIntervenerOneWrapperIfPopulated();
        setIntervener(activeIntervenerParties, oneWrapper, INTERVENER_ONE);

        IntervenerWrapper twoWrapper = caseData.getIntervenerTwoWrapperIfPopulated();
        setIntervener(activeIntervenerParties, twoWrapper, INTERVENER_TWO);

        IntervenerWrapper threeWrapper = caseData.getIntervenerThreeWrapperIfPopulated();
        setIntervener(activeIntervenerParties, threeWrapper, INTERVENER_THREE);

        IntervenerWrapper fourWrapper = caseData.getIntervenerFourWrapperIfPopulated();
        setIntervener(activeIntervenerParties, fourWrapper, INTERVENER_FOUR);

        return activeIntervenerParties;
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

    public DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }
}
