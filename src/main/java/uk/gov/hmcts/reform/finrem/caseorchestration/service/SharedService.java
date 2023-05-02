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
import java.util.Optional;

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

    default void setIntv1CorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1CorrespDocsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv1CorrespDocsShared(list);
            }
        });
    }

    default void setIntv1ExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> intv1Collection =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1ExpertEvidenceShared())
                        .orElse(new ArrayList<>());

                intv1Collection.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv1ExpertEvidenceShared(intv1Collection);
            }
        });
    }

    default void setIntv1FormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> intv1Collection =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1FormHsShared())
                        .orElse(new ArrayList<>());

                intv1Collection.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv1FormHsShared(intv1Collection);
            }
        });
    }

    default void setIntv1HearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1HearingBundlesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv1HearingBundlesShared(list);
            }
        });
    }

    default void setIntv1SummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> intv1Collection =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1SummariesShared())
                        .orElse(new ArrayList<>());

                intv1Collection.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv1SummariesShared(intv1Collection);
            }
        });
    }

    default void setIntv1StmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1StmtsExhibitsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv1StmtsExhibitsShared(list);
            }
        });
    }

    default void setIntv1QaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appQaCollection) {
        appQaCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1QaShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv1QaShared(list);
            }
        });
    }

    default void setIntv1ChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appChronologiesCollection) {
        appChronologiesCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1ChronologiesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv1ChronologiesShared(list);
            }
        });
    }

    default void setIntv1FormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
        appFormEExhibitsCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> intv1Collection =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1FormEsExhibitsShared())
                        .orElse(new ArrayList<>());

                intv1Collection.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv1FormEsExhibitsShared(intv1Collection);
            }
        });
    }

    default void setIntv1OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
        appOtherCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1OtherShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv1OtherShared(list);
            }
        });
    }

    default void setIntv2CorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2CorrespDocsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2CorrespDocsShared(list);
            }
        });
    }

    default void setIntv2ExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2ExpertEvidenceShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv2ExpertEvidenceShared(list);
            }
        });
    }

    default void setIntv2FormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2FormHsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2FormHsShared(list);
            }
        });
    }

    default void setIntv2HearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2HearingBundlesShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv2HearingBundlesShared(list);
            }
        });
    }

    default void setIntv2SummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2SummariesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2SummariesShared(list);
            }
        });
    }

    default void setIntv2StmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2StmtsExhibitsShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2StmtsExhibitsShared(list);
            }
        });
    }

    default void setIntv2QaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appQaCollection) {
        appQaCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2QaShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2QaShared(list);
            }
        });
    }

    default void setIntv2ChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appChronologiesCollection) {
        appChronologiesCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2ChronologiesShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv2ChronologiesShared(list);
            }
        });
    }

    default void setIntv2FormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
        appFormEExhibitsCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2FormEsExhibitsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv2FormEsExhibitsShared(list);
            }
        });
    }

    default void setIntv2OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
        appOtherCollection.forEach(d -> {
            if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2OtherShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(d));

                caseData.getUploadCaseDocumentWrapper().setIntv2OtherShared(list);
            }
        });
    }

    default void setIntv3CorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3CorrespDocsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3CorrespDocsShared(list);
            }
        });
    }

    default void setIntv3ExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3ExpertEvidenceShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv3ExpertEvidenceShared(list);
            }
        });
    }


    default void setIntv3FormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3FormHsShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv3FormHsShared(list);
            }
        });
    }

    default void setIntv3HearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3HearingBundlesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3HearingBundlesShared(list);
            }
        });
    }

    default void setIntv3SummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3SummariesShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv3SummariesShared(list);
            }
        });
    }

    default void setIntv3StmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3StmtsExhibitsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3StmtsExhibitsShared(list);
            }
        });
    }

    default void setIntv3QaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appQaCollection) {
        appQaCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3QaShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv3QaShared(list);
            }
        });
    }

    default void setIntv3ChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appChronologiesCollection) {
        appChronologiesCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3ChronologiesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3ChronologiesShared(list);
            }
        });
    }

    default void setIntv3FormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
        appFormEExhibitsCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3FormEsExhibitsShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3FormEsExhibitsShared(list);
            }
        });
    }

    default void setIntv3OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
        appOtherCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3OtherShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv3OtherShared(list);
            }
        });
    }


    default void setIntv4CorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4CorrespDocsShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4CorrespDocsShared(list);
            }
        });
    }

    default void setIntv4ExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4ExpertEvidenceShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4ExpertEvidenceShared(list);
            }
        });
    }

    default void setIntv4FormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4FormHsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv4FormHsShared(list);
            }
        });
    }

    default void setIntv4HearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4HearingBundlesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4HearingBundlesShared(list);
            }
        });
    }

    default void setIntv4SummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4SummariesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv4SummariesShared(list);
            }
        });
    }

    default void setIntv4StmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4StmtsExhibitsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4StmtsExhibitsShared(list);
            }
        });
    }

    default void setIntv4QaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appQaCollection) {
        appQaCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4QaShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv4QaShared(list);
            }
        });
    }

    default void setIntv4ChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appChronologiesCollection) {
        appChronologiesCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4ChronologiesShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4ChronologiesShared(list);
            }
        });
    }

    default void setIntv4FormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
        appFormEExhibitsCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4FormEsExhibitsShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));

                caseData.getUploadCaseDocumentWrapper().setIntv4FormEsExhibitsShared(list);
            }
        });
    }

    default void setIntv4OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
        appOtherCollection.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4OtherShared())
                        .orElse(new ArrayList<>());

                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setIntv4OtherShared(list);
            }
        });
    }
}
