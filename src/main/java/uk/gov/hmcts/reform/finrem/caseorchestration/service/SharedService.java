package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

import java.util.ArrayList;
import java.util.List;

public interface SharedService {
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

    default List<DynamicMultiSelectListElement> intervenerCaseRoleList(FinremCaseData caseData, List<String> roleList) {
        //intervener1
        IntervenerOneWrapper oneWrapper = caseData.getIntervenerOneWrapper();
        if (ObjectUtils.isNotEmpty(oneWrapper)
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervener1Organisation().getOrganisation().getOrganisationID())) {
            roleList.add(oneWrapper.getIntervener1Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener2
        IntervenerTwoWrapper twoWrapper = caseData.getIntervenerTwoWrapper();
        if (ObjectUtils.isNotEmpty(twoWrapper)
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervener2Organisation().getOrganisation().getOrganisationID())) {
            roleList.add(twoWrapper.getIntervener2Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener3
        IntervenerThreeWrapper threeWrapper = caseData.getIntervenerThreeWrapper();
        if (ObjectUtils.isNotEmpty(threeWrapper)
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervener3Organisation().getOrganisation().getOrganisationID())) {
            roleList.add(threeWrapper.getIntervener3Organisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener4
        IntervenerFourWrapper fourWrapper = caseData.getIntervenerFourWrapper();
        if (ObjectUtils.isNotEmpty(fourWrapper)
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation().getOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervener4Organisation().getOrganisation().getOrganisationID())) {
            roleList.add(fourWrapper.getIntervener4Organisation().getOrgPolicyCaseAssignedRole());
        }

        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(roleList)) {
            roleList.forEach(role -> dynamicListElements.add(getDynamicMultiSelectListElement(role, role)));
        }
        return dynamicListElements;
    }

    default UploadCaseDocumentCollection setSharedDocument(UploadCaseDocumentCollection sd) {
        return UploadCaseDocumentCollection.builder()
            .id(sd.getId())
            .value(sd.getValue()).build();
    }
}
