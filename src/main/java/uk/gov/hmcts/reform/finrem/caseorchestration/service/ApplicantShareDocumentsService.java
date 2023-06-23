package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicantShareDocumentsService implements SharedService {

    public DynamicMultiSelectList applicantSourceDocumentList(FinremCaseDetails caseDetails) {

        log.info("setting source document list for case {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        List<UploadCaseDocumentCollection> appOtherCollection
            = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();

        if (ObjectUtils.isNotEmpty(appOtherCollection)) {
            appOtherCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_OTHER_COLLECTION.getCcdKey(),
                    APP_OTHER_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appChronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
        if (ObjectUtils.isNotEmpty(appChronologiesCollection)) {
            appChronologiesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey(),
                    APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appStatementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
        if (ObjectUtils.isNotEmpty(appStatementsExhibitsCollection)) {
            appStatementsExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey(),
                    APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appHearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
        if (ObjectUtils.isNotEmpty(appHearingBundlesCollection)) {
            appHearingBundlesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_HEARING_BUNDLES_COLLECTION.getCcdKey(),
                    APP_HEARING_BUNDLES_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> appFormEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
        if (ObjectUtils.isNotEmpty(appFormEExhibitsCollection)) {
            appFormEExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey(),
                    APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appQaCollection
            = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
        if (ObjectUtils.isNotEmpty(appQaCollection)) {
            appQaCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey(),
                    APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appCaseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
        if (ObjectUtils.isNotEmpty(appCaseSummariesCollection)) {
            appCaseSummariesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_CASE_SUMMARIES_COLLECTION.getCcdKey(),
                    APP_CASE_SUMMARIES_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> appFormsHCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
        if (ObjectUtils.isNotEmpty(appFormsHCollection)) {
            appFormsHCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_FORMS_H_COLLECTION.getCcdKey(),
                    APP_FORMS_H_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> appExpertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
        if (ObjectUtils.isNotEmpty(appExpertEvidenceCollection)) {
            appExpertEvidenceCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey(),
                    APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
        if (ObjectUtils.isNotEmpty(appCorrespondenceDocsCollection)) {
            appCorrespondenceDocsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APPLICANT_CORRESPONDENCE_DOC_COLLECTION.getCcdKey(),
                    APPLICANT_CORRESPONDENCE_DOC_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }


    public DynamicMultiSelectList getApplicantToOtherSolicitorRoleList(FinremCaseDetails caseDetails) {
        log.info("fetching all partys solicitor case role for caseId {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> roleList = new ArrayList<>();
        //respondent
        if (ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID())) {
            roleList.add(caseData.getRespondentOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        return getRoleList(intervenerCaseRoleList(caseData, roleList),
            caseDetails.getData().getSolicitorRoleList());
    }

    public void shareSelectedDocumentWithOtherSelectedSolicitors(FinremCaseData caseData) {
        DynamicMultiSelectList sourceDocumentList = caseData.getSourceDocumentList();
        DynamicMultiSelectList solicitorRoleList = caseData.getSolicitorRoleList();

        if (ObjectUtils.isNotEmpty(sourceDocumentList) && ObjectUtils.isNotEmpty(solicitorRoleList)) {
            List<DynamicMultiSelectListElement> roleList = solicitorRoleList.getValue();
            roleList.forEach(role -> {
                List<DynamicMultiSelectListElement> documentList = sourceDocumentList.getValue();
                copySelectedFilesToTargetCollection(caseData, role.getCode(), documentList);
                copyIntervenersDocuments(caseData, role.getCode(), documentList);
            });
        }
    }


    public void copySelectedFilesToTargetCollection(FinremCaseData caseData, String role, List<DynamicMultiSelectListElement> documentList) {

        if (role.equals(CaseRole.RESP_SOLICITOR.getValue()) || role.equals(CaseRole.RESP_BARRISTER.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedChronologiesFilesToResp(caseData, collId, collName);
                copySelectedQaFilesToResp(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToResp(caseData, collId, collName);
                copySelectedSummariesFilesToResp(caseData, collId, collName);
                copySelectedHearingFilesToResp(caseData, collId, collName);
                copySelectedFormHFilesToResp(caseData, collId, collName);
                copySelectedExpertFilesToResp(caseData, collId, collName);
                copySelectedCorresFilesToResp(caseData, collId, collName);
                copySelectedFormEFilesToResp(caseData, collId, collName);
                copySelectedOtherFilesToResp(caseData, collId, collName);
            });
        }
    }

    private void copySelectedFormEFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollectionShared())
                            .orElse(new ArrayList<>());
                    list.add(setSharedDocument(sd));

                    caseData.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedOtherFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            appOtherCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespOtherCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setRespOtherCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedCorresFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
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
    }

    private void copySelectedExpertFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
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

    private void copySelectedHearingFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
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
    }

    private void copySelectedSummariesFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
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
    }

    private void copySelectedQaFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespQaCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));

                    caseData.getUploadCaseDocumentWrapper().setRespQaCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));

                    caseData.getUploadCaseDocumentWrapper().setRespChronologiesCollectionShared(list);
                }
            });
        }
    }

}
