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

    public DynamicMultiSelectList getAllActivePartyList(CaseDetails caseDetails) {
        return getAllActivePartyList(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails));
    }


    public DynamicMultiSelectList getAllActivePartyList(FinremCaseDetails caseDetails) {
        log.info("Event {} fetching all partys solicitor case role for Case ID: {}", EventType.SEND_ORDER, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> activeCasePartiesSelectedList = new ArrayList<>();
        List<DynamicMultiSelectListElement> activeCasePartiesList = new ArrayList<>();

        if (caseData.getApplicantOrganisationPolicy() != null
            && caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedAppRole = caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement appMultiSelectListElement = getDynamicMultiSelectListElement(assignedAppRole,
                DISPLAY_LABEL.formatted(APPLICANT, caseData.getFullApplicantName()));
            activeCasePartiesSelectedList.add(appMultiSelectListElement);
        }

        if (caseData.getRespondentOrganisationPolicy() != null
            && caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedRespRole = caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement respMultiSelectListElement = getDynamicMultiSelectListElement(assignedRespRole,
                DISPLAY_LABEL.formatted(RESPONDENT, caseData.getRespondentFullName()));
            activeCasePartiesSelectedList.add(respMultiSelectListElement);
        }

        activeCasePartiesList.addAll(activeCasePartiesSelectedList);

        addActiveIntervenersToPartyList(caseData, activeCasePartiesList);
        return DynamicMultiSelectList.builder()
            .value(activeCasePartiesSelectedList)
            .listItems(activeCasePartiesList)
            .build();
    }

    private void addActiveIntervenersToPartyList(FinremCaseData caseData,
                                                 List<DynamicMultiSelectListElement> activePartyList) {
        IntervenerWrapper oneWrapper = caseData.getIntervenerOneWrapperIfPopulated();
        setIntervener(activePartyList, oneWrapper, INTERVENER_ONE);

        IntervenerWrapper twoWrapper = caseData.getIntervenerTwoWrapperIfPopulated();
        setIntervener(activePartyList, twoWrapper, INTERVENER_TWO);

        IntervenerWrapper threeWrapper = caseData.getIntervenerThreeWrapperIfPopulated();
        setIntervener(activePartyList, threeWrapper, INTERVENER_THREE);

        IntervenerWrapper fourWrapper = caseData.getIntervenerFourWrapperIfPopulated();
        setIntervener(activePartyList, fourWrapper, INTERVENER_FOUR);
    }

    private void setIntervener(List<DynamicMultiSelectListElement> activePartyList,
                               IntervenerWrapper wrapper,
                               String intervener) {
        if (wrapper != null && ObjectUtils.isNotEmpty(wrapper.getIntervenerOrganisation())) {
            String assignedRole = wrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            activePartyList.add(getDynamicMultiSelectListElement(assignedRole,
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
