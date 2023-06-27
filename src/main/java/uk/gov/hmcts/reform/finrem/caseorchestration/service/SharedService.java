package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_FOUR_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_ONE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_THREE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.INTERVENER_TWO_SUMMARIES_COLLECTION;
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

    default boolean getIntervenerRoles(String role) {
        return role.equals(CaseRole.INTVR_SOLICITOR_1.getValue())
            || role.equals(CaseRole.INTVR_SOLICITOR_2.getValue())
            || role.equals(CaseRole.INTVR_SOLICITOR_3.getValue())
            || role.equals(CaseRole.INTVR_SOLICITOR_4.getValue())
            || role.equals(CaseRole.INTVR_BARRISTER_1.getValue())
            || role.equals(CaseRole.INTVR_BARRISTER_2.getValue())
            || role.equals(CaseRole.INTVR_BARRISTER_3.getValue())
            || role.equals(CaseRole.INTVR_BARRISTER_4.getValue());
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

    default UploadCaseDocumentCollection setSharedDocument(UploadCaseDocumentCollection sd) {
        return UploadCaseDocumentCollection.builder()
            .id(UUID.randomUUID())
            .value(sd.getValue()).build();
    }

    default void copySharedDocumentsInSharedCollection(FinremCaseData caseData,
                                                       String role,
                                                       List<DynamicMultiSelectListElement> documentList) {
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
        docs = getUploadCaseOtherDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespOtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppOtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4OtherShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseOtherDocumentCollections(FinremCaseData caseData,
                                                                                            String collName,
                                                                                            List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getOtherCollection();
        } else if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespOtherCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1Other();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2Other();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3Other();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4Other();
        }
        return docs;
    }

    private void copySelectedFormEFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseFormEsExDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespFormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppFormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4FormEsExhibitsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseFormEsExDocumentCollections(FinremCaseData caseData,
                                                                                        String collName,
                                                                                        List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppFormEExhibitsCollection();
        } else if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespFormEExhibitsCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1FormEsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3FormEsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3FormEsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4FormEsExhibits();
        }
        return docs;
    }


    private void copySelectedCorresFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseCorresDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespCorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppCorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4CorrespDocsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseCorresDocumentCollections(FinremCaseData caseData,
                                                                                             String collName,
                                                                                             List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppCorrespondenceDocsCollection();
        } else if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespCorrespondenceDocsColl();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1CorrespDocs();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2CorrespDocs();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3CorrespDocs();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4CorrespDocs();
        }
        return docs;
    }

    private void copySelectedExpertFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseExpertDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4ExpertEvidenceShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseExpertDocumentCollections(FinremCaseData caseData,
                                                                                      String collName,
                                                                                      List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppExpertEvidenceCollection();
        } else if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespExpertEvidenceCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1ExpertEvidence();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2ExpertEvidence();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3ExpertEvidence();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4ExpertEvidence();
        }
        return docs;
    }

    private void copySelectedFormHFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseFormHsDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespFormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppFormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4FormHsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseFormHsDocumentCollections(FinremCaseData caseData,
                                                                                      String collName,
                                                                                      List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppFormsHCollection();
        } else if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespFormsHCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1FormHs();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2FormHs();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3FormHs();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_FORM_H_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4FormHs();
        }
        return docs;
    }

    private void copySelectedHearingFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseHearingDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespHearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppHearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4HearingBundlesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseHearingDocumentCollections(FinremCaseData caseData,
                                                                                              String collName,
                                                                                              List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppHearingBundlesCollection();
        } else if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespHearingBundlesCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1HearingBundles();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2HearingBundles();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3HearingBundles();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4HearingBundles();
        }
        return docs;
    }

    private void copySelectedSummariesFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseSummeriesDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespSummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppSummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4SummariesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseSummeriesDocumentCollections(FinremCaseData caseData,
                                                                                         String collName,
                                                                                         List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppCaseSummariesCollection();
        } else if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespCaseSummariesCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1Summaries();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2Summaries();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3Summaries();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_SUMMARIES_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4Summaries();
        }
        return docs;
    }

    private void copySelectedStmtExhibitsFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseStmtDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespStmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppStmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4StmtsExhibitsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseStmtDocumentCollections(FinremCaseData caseData,
                                                                                           String collName,
                                                                                           List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppStatementsExhibitsCollection();
        } else if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespStatementsExhibitsCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1StmtsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2StmtsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3StmtsExhibits();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4StmtsExhibits();
        }
        return docs;
    }

    private void copySelectedQaFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseQaDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespQaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppQaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4QaShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseQaDocumentCollections(FinremCaseData caseData,
                                                                                  String collName,
                                                                                  List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppQaCollection();
        } else if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespQaCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1Qa();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2Qa();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3Qa();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4Qa();
        }
        return docs;
    }

    private void copySelectedChronologiesFiles(FinremCaseData caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseChronoDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            setRespChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            setAppChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
            setIntv1ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
            setIntv2ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
            setIntv3ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
            setIntv4ChronologiesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseChronoDocumentCollections(FinremCaseData caseData,
                                                                                      String collName,
                                                                                      List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppChronologiesCollection();
        } else if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getRespChronologiesCollection();
        } else if (collName.equalsIgnoreCase(INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv1Chronologies();
        } else if (collName.equalsIgnoreCase(INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv2Chronologies();
        } else if (collName.equalsIgnoreCase(INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv3Chronologies();
        } else if (collName.equalsIgnoreCase(INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getIntv4Chronologies();
        }
        return docs;
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

    default void setIntv1OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
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

    default void setIntv2OtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(d -> {
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

    default void setAppOtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppOtherCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppOtherCollectionShared(list);
            }
        });
    }

    default void setAppFormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollectionShared(list);
            }
        });
    }

    default void setAppCorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppCorrespondenceDocsCollShared(list);
            }
        });
    }

    default void setAppExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollectionShared(list);
            }
        });
    }

    default void setAppHearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppHearingBundlesCollectionShared(list);
            }
        });
    }

    default void setAppSummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppCaseSummariesCollectionShared(list);
            }
        });
    }

    default void setAppStmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppStatementsExhibitsCollShared(list);
            }
        });
    }

    default void setAppQaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppQaCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppQaCollectionShared(list);
            }
        });
    }

    default void setAppChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppChronologiesCollectionShared(list);
            }
        });
    }

    default void setAppFormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setAppFormsHCollectionShared(list);
            }
        });
    }



    default void setRespOtherShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespOtherCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespOtherCollectionShared(list);
            }
        });
    }

    default void setRespFormEsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollectionShared(list);
            }
        });
    }

    default void setRespCorrespDocsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespCorrespondenceDocsCollShared(list);
            }
        });
    }

    default void setRespExpertEvidenceShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespExpertEvidenceCollShared(list);
            }
        });
    }

    default void setRespHearingBundlesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespHearingBundlesCollShared(list);
            }
        });
    }

    default void setRespSummariesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespCaseSummariesCollectionShared(list);
            }
        });
    }

    default void setRespStmtsExhibitsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespStatementsExhibitsCollShared(list);
            }
        });
    }

    default void setRespQaShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespQaCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespQaCollectionShared(list);
            }
        });
    }

    default void setRespChronologiesShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespChronologiesCollectionShared(list);
            }
        });
    }

    default void setRespFormHsShared(FinremCaseData caseData, String collId, List<UploadCaseDocumentCollection> coll) {
        coll.forEach(sd -> {
            if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                List<UploadCaseDocumentCollection> list =
                    Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespFormsHCollectionShared())
                        .orElse(new ArrayList<>());
                list.add(setSharedDocument(sd));
                caseData.getUploadCaseDocumentWrapper().setRespFormsHCollectionShared(list);
            }
        });
    }
}
