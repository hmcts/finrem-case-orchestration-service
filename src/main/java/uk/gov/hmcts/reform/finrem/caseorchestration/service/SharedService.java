package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;

public interface SharedService {

    default boolean getIntervenerRoles(String role) {
        return role.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
            || role.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
            || role.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
            || role.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
            || role.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())
            || role.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())
            || role.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())
            || role.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode());
    }

    default DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    default DynamicMultiSelectList getSelectedDocumentList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                                           DynamicMultiSelectList selectedDocuments) {
        if (selectedDocuments != null) {
            return DynamicMultiSelectList.builder()
                .value(selectedDocuments.getValue())
                .listItems(dynamicMultiSelectListElement)
                .build();
        } else {
            return DynamicMultiSelectList.builder()
                .listItems(dynamicMultiSelectListElement)
                .build();
        }
    }

    default DynamicMultiSelectList getOtherSolicitorRoleList(FinremCaseDetails caseDetails,
                                                             List<CaseAssignmentUserRole> caseAssignedUserRoleList,
                                                             String loggedInUserCaseRole) {
        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        if (!caseAssignedUserRoleList.isEmpty()) {
            caseAssignedUserRoleList.forEach(role ->
                dynamicListElements.add(getDynamicMultiSelectListElement(role.getCaseRole(), role.getCaseRole())));
        }

        List<DynamicMultiSelectListElement> recipientUsers
            = dynamicListElements.stream().filter(e -> !e.getCode().equals(loggedInUserCaseRole)).toList();

        return getRoleList(recipientUsers, caseData.getSolicitorRoleList());
    }

    default DynamicMultiSelectList getRoleList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
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

}
