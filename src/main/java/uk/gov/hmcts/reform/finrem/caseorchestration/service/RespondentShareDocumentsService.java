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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESPONDENT_CORRESPONDENCE_COLLECTION;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class RespondentShareDocumentsService implements SharedService {

    public DynamicMultiSelectList respondentSourceDocumentList(FinremCaseDetails caseDetails) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("setting respondent source document list for case {}", caseId);
        FinremCaseData caseData = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        List<UploadCaseDocumentCollection> otherCollection
            = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();

        if (ObjectUtils.isNotEmpty(otherCollection)) {
            otherCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("otherCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_OTHER_COLLECTION.getCcdKey(),
                    RESP_OTHER_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> chronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
        if (ObjectUtils.isNotEmpty(chronologiesCollection)) {
            chronologiesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("chronologiesCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey(),
                    RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> statementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
        if (ObjectUtils.isNotEmpty(statementsExhibitsCollection)) {
            statementsExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("statementsExhibitsCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey(),
                    RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> hearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
        if (ObjectUtils.isNotEmpty(hearingBundlesCollection)) {
            hearingBundlesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("hearingBundlesCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_HEARING_BUNDLES_COLLECTION.getCcdKey(),
                    RESP_HEARING_BUNDLES_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> formEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
        if (ObjectUtils.isNotEmpty(formEExhibitsCollection)) {
            formEExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("formEExhibitsCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey(),
                    RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> qaCollection
            = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
        if (ObjectUtils.isNotEmpty(qaCollection)) {
            qaCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("qaCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey(),
                    RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> caseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
        if (ObjectUtils.isNotEmpty(caseSummariesCollection)) {
            caseSummariesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("caseSummariesCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_CASE_SUMMARIES_COLLECTION.getCcdKey(),
                    RESP_CASE_SUMMARIES_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        List<UploadCaseDocumentCollection> formsHCollection
            = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
        if (ObjectUtils.isNotEmpty(formsHCollection)) {
            formsHCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("formsHCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_FORM_H_COLLECTION.getCcdKey(),
                    RESP_FORM_H_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> expertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
        if (ObjectUtils.isNotEmpty(expertEvidenceCollection)) {
            expertEvidenceCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("expertEvidenceCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey(),
                    RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }


        List<UploadCaseDocumentCollection> correspondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
        if (ObjectUtils.isNotEmpty(correspondenceDocsCollection)) {
            correspondenceDocsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                log.info("correspondenceDocsCollection:: filename {} caseId {}", filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + RESPONDENT_CORRESPONDENCE_COLLECTION.getCcdKey(),
                    RESPONDENT_CORRESPONDENCE_COLLECTION.getCcdKey() + " -> " + filename));
            });
        }

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }

    public DynamicMultiSelectList getRespondentToOtherSolicitorRoleList(FinremCaseDetails caseDetails) {
        log.info("fetching all partys solicitor case role for caseId {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();
        List<String> roleList = new ArrayList<>();
        //applicant
        if (ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation())
            && ObjectUtils.isNotEmpty(caseData.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID())) {
            roleList.add(caseData.getApplicantOrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }

        return getRoleList(intervenerCaseRoleList(caseData, roleList),
            caseDetails.getData().getSolicitorRoleList());
    }


    public void copyDocumentOnTheirRespectiveCollectionForSelectedSolicitors(FinremCaseData caseData) {
        DynamicMultiSelectList sourceDocumentList = caseData.getSourceDocumentList();
        DynamicMultiSelectList solicitorRoleList = caseData.getSolicitorRoleList();

        if (ObjectUtils.isNotEmpty(sourceDocumentList) && ObjectUtils.isNotEmpty(solicitorRoleList)) {
            List<DynamicMultiSelectListElement> roleList = solicitorRoleList.getValue();
            roleList.forEach(role -> {
                List<DynamicMultiSelectListElement> documentList = sourceDocumentList.getValue();
                copySelectedFilesToTargetCollection(caseData, role.getCode(), documentList);
            });
        }
    }


    private void copySelectedFilesToTargetCollection(FinremCaseData caseData, String role, List<DynamicMultiSelectListElement> documentList) {

        if (role.equals(CaseRole.APP_SOLICITOR.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedChronologiesFilesToApp(caseData, collId, collName);
                copySelectedQaFilesToApp(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToApp(caseData, collId, collName);
                copySelectedSummariesFilesToApp(caseData, collId, collName);
                copySelectedHearingFilesToApp(caseData, collId, collName);
                copySelectedFormHFilesToApp(caseData, collId, collName);
                copySelectedExpertFilesToApp(caseData, collId, collName);
                copySelectedCorresFilesToApp(caseData, collId, collName);
                copySelectedFormEFilesToApp(caseData, collId, collName);
                copySelectedOtherFilesToApp(caseData, collId, collName);
            });
        }

        copyIntervenersDocuments(caseData, role, documentList);
    }


    private void copySelectedFormEFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> formEExhibits = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            formEExhibits.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedOtherFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> otherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            otherCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppOtherCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setAppOtherCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedCorresFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
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
    }

    private void copySelectedExpertFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
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
    }

    private void copySelectedHearingFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
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
    }

    private void copySelectedSummariesFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
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
    }

    private void copySelectedQaFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> qaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            qaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppQaCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setAppQaCollectionShared(list);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> chronologiesList = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            chronologiesList.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setAppChronologiesCollectionShared(list);
                }
            });
        }
    }
}
