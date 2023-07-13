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

import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.*;

@Slf4j
@Service
public class UploadConsentOrderAboutToStartHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private static final String CASE_ROLE_LABEL = "%s - %s";

    public UploadConsentOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
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

        finremCaseData.setAdditionalDocument(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }


    private DynamicMultiSelectList getAllActivePartyList(FinremCaseDetails caseDetails) {
        log.info("Event {} fetching each party's solicitor case role for caseId {}", EventType.SEND_ORDER, caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        if ((ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(caseData.getFullApplicantName())) {
            String assignedRole = caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(APPLICANT, caseData.getFullApplicantName())));
        }

        if ((ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(caseData.getRespondentFullName())) {

            String assignedRole = caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(RESPONDENT, caseData.getRespondentFullName())));
        }
        return getRoleList(intervenerCaseRoleList(caseData, dynamicListElements),
            caseDetails.getData().getPartiesOnCase());
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

    private List<DynamicMultiSelectListElement> intervenerCaseRoleList(FinremCaseData caseData,
                                                                       List<DynamicMultiSelectListElement> dynamicListElements) {
        IntervenerOneWrapper oneWrapper = caseData.getIntervenerOneWrapper();
        if ((ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(oneWrapper.getIntervenerName())) {

            String assignedRole = oneWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(capitalize(INTERVENER_ONE), oneWrapper.getIntervenerName())));
        }
        IntervenerTwoWrapper twoWrapper = caseData.getIntervenerTwoWrapper();
        if ((ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(twoWrapper.getIntervenerName())) {

            String assignedRole = twoWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(capitalize(INTERVENER_TWO), twoWrapper.getIntervenerName())));
        }
        IntervenerThreeWrapper threeWrapper = caseData.getIntervenerThreeWrapper();
        if ((ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(threeWrapper.getIntervenerName())) {

            String assignedRole = threeWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(capitalize(INTERVENER_THREE), threeWrapper.getIntervenerName())));
        }
        IntervenerFourWrapper fourWrapper = caseData.getIntervenerFourWrapper();
        if ((ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID()))
            || ObjectUtils.isNotEmpty(fourWrapper.getIntervenerName())) {

            String assignedRole = fourWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole();
            dynamicListElements.add(generalOrderService.getDynamicMultiSelectListElement(assignedRole,
                CASE_ROLE_LABEL.formatted(capitalize(INTERVENER_FOUR), fourWrapper.getIntervenerName())));
        }
        return dynamicListElements;
    }
}
