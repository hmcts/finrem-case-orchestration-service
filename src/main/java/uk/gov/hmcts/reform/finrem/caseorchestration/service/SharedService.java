package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION;

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
        IntervenerWrapper oneWrapper = caseData.getIntervenerOneWrapper();
        if (ObjectUtils.isNotEmpty(oneWrapper)
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(oneWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID())) {
            roleList.add(oneWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener2
        IntervenerWrapper twoWrapper = caseData.getIntervenerTwoWrapper();
        if (ObjectUtils.isNotEmpty(twoWrapper)
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(twoWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID())) {
            roleList.add(twoWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener3
        IntervenerWrapper threeWrapper = caseData.getIntervenerThreeWrapper();
        if (ObjectUtils.isNotEmpty(threeWrapper)
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(threeWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID())) {
            roleList.add(threeWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
        }
        //intervener4
        IntervenerWrapper fourWrapper = caseData.getIntervenerFourWrapper();
        if (ObjectUtils.isNotEmpty(fourWrapper)
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation().getOrganisation())
            && ObjectUtils.isNotEmpty(fourWrapper.getIntervenerOrganisation().getOrganisation().getOrganisationID())) {
            roleList.add(fourWrapper.getIntervenerOrganisation().getOrgPolicyCaseAssignedRole());
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


    default void copyIntervenersDocuments(FinremCaseData caseData, String role, List<DynamicMultiSelectListElement> documentList) {
        if (role.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            copyShareDocumentsInSharedCollection(caseData, role, documentList);
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            copyShareDocumentsInSharedCollection(caseData, role, documentList);
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            copyShareDocumentsInSharedCollection(caseData, role, documentList);
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            copyShareDocumentsInSharedCollection(caseData, role, documentList);
        }
    }

    private void copyShareDocumentsInSharedCollection(FinremCaseData caseData, String role, List<DynamicMultiSelectListElement> documentList) {
        documentList.forEach(doc -> {
            String[] collectionIdAndFilename = doc.getCode().split("#");
            String collId = collectionIdAndFilename[0];
            String collName = collectionIdAndFilename[1];

            copySelectedOtherFiles(caseData, collId, collName, role);
            copySelectedFormEFiles(caseData, collId, collName, role);
            copySelectedChronologiesFiles(caseData, collId, collName, role);
            copySelectedQaFiles(caseData, collId, collName, role);
            copySelectedStmtExhibitsFiles(caseData, collId, collName, role);
            copySelectedSummariesFiles(caseData, collId, collName, role);
            copySelectedHearingFiles(caseData, collId, collName, role);
            copySelectedFormHFiles(caseData, collId, collName, role);
            copySelectedExpertFiles(caseData, collId, collName, role);
            copySelectedCorresFiles(caseData, collId, collName, role);
        });
    }

    private void copySelectedOtherFiles(FinremCaseData caseData,
                                        String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppOtherCollection();
        } else if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespOtherCollection();
        }

        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4OtherShared(caseData, collId, docs);
        }
    }

    private void copySelectedFormEFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppFormEExhibitsCollection();
        } else if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespFormEExhibitsCollection();
        }

        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4FormEsExhibitsShared(caseData, collId, docs);
        }
    }


    private void copySelectedCorresFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppFormEExhibitsCollection();
        } else if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespFormEExhibitsCollection();
        }

        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4CorrespDocsShared(caseData, collId, docs);
        }
    }

    private void copySelectedExpertFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppExpertEvidenceCollection();
        } else if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespExpertEvidenceCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4ExpertEvidenceShared(caseData, collId, docs);
        }
    }

    private void copySelectedFormHFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppFormsHCollection();
        } else if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespFormsHCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4FormHsShared(caseData, collId, docs);
        }
    }

    private void copySelectedHearingFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppHearingBundlesCollection();
        } else if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespHearingBundlesCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4HearingBundlesShared(caseData, collId, docs);
        }
    }

    private void copySelectedSummariesFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppCaseSummariesCollection();
        } else if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespCaseSummariesCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4SummariesShared(caseData, collId, docs);
        }
    }

    private void copySelectedStmtExhibitsFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppStatementsExhibitsCollection();
        } else if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespStatementsExhibitsCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4StmtsExhibitsShared(caseData, collId, docs);
        }
    }

    private void copySelectedQaFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppQaCollection();
        } else if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespQaCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4QaShared(caseData, collId, docs);
        }
    }

    private void copySelectedChronologiesFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppChronologiesCollection();
        } else if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespChronologiesCollection();
        }


        if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            setIntv1ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            setIntv2ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            setIntv3ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            setIntv4ChronologiesShared(caseData, collId, docs);
        }
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
