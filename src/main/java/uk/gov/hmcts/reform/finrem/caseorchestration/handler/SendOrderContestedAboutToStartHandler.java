package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SendOrderContestedAboutToStartHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;

    public SendOrderContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 GeneralOrderService generalOrderService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();
        generalOrderService.setOrderList(caseDetails);

        DynamicMultiSelectList roleList = getAllActivePartyList(caseDetails);
        finremCaseData.setPartiesInCase(roleList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }


    private DynamicMultiSelectList getAllActivePartyList(FinremCaseDetails caseDetails) {
        log.info("Event {} fetching all partys solicitor case role for caseId {}", EventType.SEND_ORDER, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> roleList = new ArrayList<>();

        if ((ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(caseData.getFullApplicantName())) {
            roleList.add(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        if ((ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(caseData.getRespondentFullName())) {
            roleList.add(caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        return getRoleList(intervenerCaseRoleList(caseData, roleList),
            caseDetails.getData().getPartiesInCase());
    }

    private DynamicMultiSelectList getRoleList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                               DynamicMultiSelectList selectedRoles) {
        if (selectedRoles != null) {
            return DynamicMultiSelectList.builder()
                .value(selectedRoles.getValue())
                .listItems(dynamicMultiSelectListElement)
                .build();
        } else {
            return DynamicMultiSelectList.builder()
                .listItems(dynamicMultiSelectListElement)
                .build();
        }
    }

    private List<DynamicMultiSelectListElement> intervenerCaseRoleList(FinremCaseData caseData, List<String> roleList) {
        //intervener1
        IntervenerOneWrapper oneWrapper = caseData.getIntervenerOneWrapper();
        if ((ObjectUtils.isNotEmpty(oneWrapper)
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Name())) {
            roleList.add(oneWrapper.getIntervener1Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener2
        IntervenerTwoWrapper twoWrapper = caseData.getIntervenerTwoWrapper();
        if ((ObjectUtils.isNotEmpty(twoWrapper)
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Name())) {
            roleList.add(twoWrapper.getIntervener2Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener3
        IntervenerThreeWrapper threeWrapper = caseData.getIntervenerThreeWrapper();
        if ((ObjectUtils.isNotEmpty(threeWrapper)
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Name())) {
            roleList.add(threeWrapper.getIntervener3Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener4
        IntervenerFourWrapper fourWrapper = caseData.getIntervenerFourWrapper();
        if ((ObjectUtils.isNotEmpty(fourWrapper)
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Name())) {
            roleList.add(fourWrapper.getIntervener4Organisation().getOrgPolicyCaseAssignedRole());
        }

        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(roleList)) {
            roleList.forEach(role -> dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(role, role)));
        }
        return dynamicListElements;
    }
}
