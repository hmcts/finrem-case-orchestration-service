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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Slf4j
@Service
public class SendOrderContestedAboutToStartHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private static final String DISPLAY_LABEL = "%s - %s";

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
        finremCaseData.setPartiesOnCase(roleList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }


    private DynamicMultiSelectList getAllActivePartyList(FinremCaseDetails caseDetails) {
        log.info("Event {} fetching all partys solicitor case role for caseId {}", EventType.SEND_ORDER, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        List<DynamicMultiSelectListElement> defaultDynamicListElements = new ArrayList<>();

        if (caseData.getApplicantOrganisationPolicy() != null && caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedAppRole = caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement appMultiSelectListElement = generalOrderService.getDynamicMultiSelectListElement(assignedAppRole,
                DISPLAY_LABEL.formatted(APPLICANT, caseData.getFullApplicantName()));
            dynamicListElements.add(appMultiSelectListElement);
            defaultDynamicListElements.add(appMultiSelectListElement);
        }

        if (caseData.getRespondentOrganisationPolicy() != null && caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole() != null) {
            String assignedRespRole = caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            DynamicMultiSelectListElement respMultiSelectListElement = generalOrderService.getDynamicMultiSelectListElement(assignedRespRole,
                DISPLAY_LABEL.formatted(RESPONDENT, caseData.getRespondentFullName()));
            dynamicListElements.add(respMultiSelectListElement);
            defaultDynamicListElements.add(respMultiSelectListElement);
        }
        return getRoleList(intervenerCaseRoleList(caseData, dynamicListElements),
            caseDetails.getData().getPartiesOnCase(), defaultDynamicListElements);
    }

    private DynamicMultiSelectList getRoleList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                               DynamicMultiSelectList selectedRoles,
                                               List<DynamicMultiSelectListElement> defaultDynamicListElements) {
        if (selectedRoles != null) {
            return DynamicMultiSelectList.builder()
                .value(selectedRoles.getValue())
                .listItems(dynamicMultiSelectListElement)
                .build();
        } else {
            return DynamicMultiSelectList.builder()
                .value(defaultDynamicListElements)
                .listItems(dynamicMultiSelectListElement)
                .build();
        }
    }

    private List<DynamicMultiSelectListElement> intervenerCaseRoleList(FinremCaseData caseData,
                                                                       List<DynamicMultiSelectListElement> dynamicListElements) {
        //intervener1
        IntervenerWrapper oneWrapper = caseData.getIntervenerOneWrapperIfPopulated();
        if (oneWrapper != null && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation())) {
            String assignedRole = oneWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                DISPLAY_LABEL.formatted(capitalize(INTERVENER_ONE), oneWrapper.getIntervenerName())));
        }
        //intervener2
        IntervenerWrapper twoWrapper = caseData.getIntervenerTwoWrapperIfPopulated();
        if (twoWrapper != null && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation())) {
            String assignedRole = twoWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                DISPLAY_LABEL.formatted(capitalize(INTERVENER_TWO), twoWrapper.getIntervenerName())));
        }
        //intervener3
        IntervenerWrapper threeWrapper = caseData.getIntervenerThreeWrapperIfPopulated();
        if (threeWrapper != null && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation())) {
            String assignedRole = threeWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                DISPLAY_LABEL.formatted(capitalize(INTERVENER_THREE), threeWrapper.getIntervenerName())));
        }
        //intervener4
        IntervenerWrapper fourWrapper = caseData.getIntervenerFourWrapperIfPopulated();
        if (fourWrapper != null && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation())) {
            String assignedRole = fourWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                DISPLAY_LABEL.formatted(capitalize(INTERVENER_FOUR), fourWrapper.getIntervenerName())));
        }
        return dynamicListElements;
    }
}
