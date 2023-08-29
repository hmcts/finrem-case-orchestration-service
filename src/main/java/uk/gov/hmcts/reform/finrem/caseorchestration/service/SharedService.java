package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION;

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

    default DynamicMultiSelectList getOtherSolicitorRoleList(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                                             List<CaseAssignmentUserRole> caseAssignedUserRoleList,
                                                             String loggedInUserCaseRole) {
        FinremCaseDataContested caseData = caseDetails.getData();
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
            .id(UUID.randomUUID().toString())
            .uploadCaseDocument(sd.getUploadCaseDocument()).build();
    }

    default void copySharedDocumentsInSharedCollection(FinremCaseDataContested caseData,
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

    private void copySelectedOtherFiles(FinremCaseDataContested caseData,
                                        String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseOtherDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespOtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppOtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3OtherShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4OtherShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseOtherDocumentCollections(FinremCaseDataContested caseData,
                                                                                     String collName,
                                                                                     List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            docs = documentWrapper.getAppOtherCollection();
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

    private void copySelectedFormEFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseFormEsExDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespFormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppFormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3FormEsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4FormEsExhibitsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseFormEsExDocumentCollections(FinremCaseDataContested caseData,
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


    private void copySelectedCorresFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseCorresDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespCorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppCorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3CorrespDocsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4CorrespDocsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseCorresDocumentCollections(FinremCaseDataContested caseData,
                                                                                      String collName,
                                                                                      List<UploadCaseDocumentCollection> docs) {
        UploadCaseDocumentWrapper documentWrapper = caseData.getUploadCaseDocumentWrapper();
        if (collName.equalsIgnoreCase(APPLICANT_CORRESPONDENCE_DOC_COLLECTION.getCcdKey())) {
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

    private void copySelectedExpertFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseExpertDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3ExpertEvidenceShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4ExpertEvidenceShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseExpertDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedFormHFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseFormHsDocumentCollections(caseData, collName, docs);

        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespFormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppFormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3FormHsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4FormHsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseFormHsDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedHearingFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseHearingDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespHearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppHearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3HearingBundlesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4HearingBundlesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseHearingDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedSummariesFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseSummeriesDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespSummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppSummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3SummariesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4SummariesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseSummeriesDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedStmtExhibitsFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseStmtDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespStmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppStmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3StmtsExhibitsShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4StmtsExhibitsShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseStmtDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedQaFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseQaDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespQaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppQaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3QaShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4QaShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseQaDocumentCollections(FinremCaseDataContested caseData,
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

    private void copySelectedChronologiesFiles(FinremCaseDataContested caseData, String collId, String collName, String caseRole) {
        List<UploadCaseDocumentCollection> docs = new ArrayList<>();
        docs = getUploadCaseChronoDocumentCollections(caseData, collName, docs);


        if (caseRole.equals(CaseRole.RESP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.RESP_BARRISTER.getCcdCode())) {
            setRespChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.APP_SOLICITOR.getCcdCode()) || caseRole.equals(CaseRole.APP_BARRISTER.getCcdCode())) {
            setAppChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_1.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            setIntv1ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_2.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            setIntv2ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_3.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            setIntv3ChronologiesShared(caseData, collId, docs);
        } else if (caseRole.equals(CaseRole.INTVR_SOLICITOR_4.getCcdCode()) || caseRole.equals(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            setIntv4ChronologiesShared(caseData, collId, docs);
        }
    }

    private List<UploadCaseDocumentCollection> getUploadCaseChronoDocumentCollections(FinremCaseDataContested caseData,
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

    default void setIntv1CorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1ExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1FormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1HearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1SummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1StmtsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv1QaShared(FinremCaseDataContested caseData,
                                  String collId, List<UploadCaseDocumentCollection> appQaCollection) {
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

    default void setIntv1ChronologiesShared(FinremCaseDataContested caseData,
                                            String collId,
                                            List<UploadCaseDocumentCollection> appChronologiesCollection) {
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

    default void setIntv1FormEsExhibitsShared(FinremCaseDataContested caseData, String collId,
                                              List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
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

    default void setIntv1OtherShared(FinremCaseDataContested caseData,
                                     String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2CorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2ExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2FormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2HearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2SummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2StmtsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv2QaShared(FinremCaseDataContested caseData,
                                  String collId, List<UploadCaseDocumentCollection> appQaCollection) {
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

    default void setIntv2ChronologiesShared(FinremCaseDataContested caseData,
                                            String collId,
                                            List<UploadCaseDocumentCollection> appChronologiesCollection) {
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

    default void setIntv2FormEsExhibitsShared(FinremCaseDataContested caseData,
                                              String collId,
                                              List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
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

    default void setIntv2OtherShared(FinremCaseDataContested caseData,
                                     String collId,
                                     List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3CorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3ExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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


    default void setIntv3FormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3HearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3SummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3StmtsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv3QaShared(FinremCaseDataContested caseData,
                                  String collId,
                                  List<UploadCaseDocumentCollection> appQaCollection) {
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

    default void setIntv3ChronologiesShared(FinremCaseDataContested caseData,
                                            String collId,
                                            List<UploadCaseDocumentCollection> appChronologiesCollection) {
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

    default void setIntv3FormEsExhibitsShared(FinremCaseDataContested caseData,
                                              String collId,
                                              List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
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

    default void setIntv3OtherShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
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


    default void setIntv4CorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4ExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4FormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4HearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4SummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4StmtsExhibitsShared(FinremCaseDataContested caseData,
                                             String collId,
                                             List<UploadCaseDocumentCollection> coll) {
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

    default void setIntv4QaShared(FinremCaseDataContested caseData,
                                  String collId,
                                  List<UploadCaseDocumentCollection> appQaCollection) {
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

    default void setIntv4ChronologiesShared(FinremCaseDataContested caseData,
                                            String collId,
                                            List<UploadCaseDocumentCollection> appChronologiesCollection) {
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

    default void setIntv4FormEsExhibitsShared(FinremCaseDataContested caseData,
                                              String collId,
                                              List<UploadCaseDocumentCollection> appFormEExhibitsCollection) {
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

    default void setIntv4OtherShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> appOtherCollection) {
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

    default void setAppOtherShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppFormEsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppCorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppHearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppSummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppStmtsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppQaShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppChronologiesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setAppFormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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


    default void setRespOtherShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespFormEsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespCorrespDocsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespExpertEvidenceShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespHearingBundlesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespSummariesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespStmtsExhibitsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespQaShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespChronologiesShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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

    default void setRespFormHsShared(FinremCaseDataContested caseData, String collId, List<UploadCaseDocumentCollection> coll) {
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
