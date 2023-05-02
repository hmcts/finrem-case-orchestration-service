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


        if (role.equals(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedOtherFilesToIntv1(caseData, collId, collName);
                copySelectedFormEFilesToIntv1(caseData, collId, collName);
                copySelectedChronologiesFilesToIntv1(caseData, collId, collName);
                copySelectedQaFilesToIntv1(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToIntv1(caseData, collId, collName);
                copySelectedSummariesFilesToIntv1(caseData, collId, collName);
                copySelectedHearingFilesToIntv1(caseData, collId, collName);
                copySelectedFormHFilesToIntv1(caseData, collId, collName);
                copySelectedExpertFilesToIntv1(caseData, collId, collName);
                copySelectedCorresFilesToIntv1(caseData, collId, collName);
            });
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedOtherFilesToIntv2(caseData, collId, collName);
                copySelectedFormEFilesToIntv2(caseData, collId, collName);
                copySelectedChronologiesFilesToIntv2(caseData, collId, collName);
                copySelectedQaFilesToIntv2(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToIntv2(caseData, collId, collName);
                copySelectedSummariesFilesToIntv2(caseData, collId, collName);
                copySelectedHearingFilesToIntv2(caseData, collId, collName);
                copySelectedFormHFilesToIntv2(caseData, collId, collName);
                copySelectedExpertFilesToIntv2(caseData, collId, collName);
                copySelectedCorresFilesToIntv2(caseData, collId, collName);
            });
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedOtherFilesToIntv3(caseData, collId, collName);
                copySelectedFormEFilesToIntv3(caseData, collId, collName);
                copySelectedChronologiesFilesToIntv3(caseData, collId, collName);
                copySelectedQaFilesToIntv3(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToIntv3(caseData, collId, collName);
                copySelectedSummariesFilesToIntv3(caseData, collId, collName);
                copySelectedHearingFilesToIntv3(caseData, collId, collName);
                copySelectedFormHFilesToIntv3(caseData, collId, collName);
                copySelectedExpertFilesToIntv3(caseData, collId, collName);
                copySelectedCorresFilesToIntv3(caseData, collId, collName);
            });
        }

        if (role.equals(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            documentList.forEach(doc -> {
                String[] collectionIdAndFilename = doc.getCode().split("#");
                String collId = collectionIdAndFilename[0];
                String collName = collectionIdAndFilename[1];

                copySelectedOtherFilesToIntv4(caseData, collId, collName);
                copySelectedFormEFilesToIntv4(caseData, collId, collName);
                copySelectedChronologiesFilesToIntv4(caseData, collId, collName);
                copySelectedQaFilesToIntv4(caseData, collId, collName);
                copySelectedStmtExhibitsFilesToIntv4(caseData, collId, collName);
                copySelectedSummariesFilesToIntv4(caseData, collId, collName);
                copySelectedHearingFilesToIntv4(caseData, collId, collName);
                copySelectedFormHFilesToIntv4(caseData, collId, collName);
                copySelectedExpertFilesToIntv4(caseData, collId, collName);
                copySelectedCorresFilesToIntv4(caseData, collId, collName);
            });
        }
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


    private void copySelectedCorresFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
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
    }

    private void copySelectedExpertFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
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
    }

    private void copySelectedHearingFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1HearingBundlesShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setIntv1HearingBundlesShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedSummariesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1StmtsExhibitsShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setIntv1StmtsExhibitsShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedQaFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
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
    }

    private void copySelectedChronologiesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1ChronologiesShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(setSharedDocument(sd));

                    caseData.getUploadCaseDocumentWrapper().setIntv1ChronologiesShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedFormEFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
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
    }

    private void copySelectedOtherFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1OtherShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setIntv1OtherShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedCorresFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
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
    }

    private void copySelectedExpertFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
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
    }

    private void copySelectedHearingFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
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
    }

    private void copySelectedSummariesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
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
    }

    private void copySelectedQaFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
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
    }

    private void copySelectedChronologiesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
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
    }

    private void copySelectedFormEFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
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
    }

    private void copySelectedOtherFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2OtherShared())
                            .orElse(new ArrayList<>());

                    list.add(setSharedDocument(sd));
                    caseData.getUploadCaseDocumentWrapper().setIntv2OtherShared(list);
                }
            });
        }
    }


    private void copySelectedCorresFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
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
    }

    private void copySelectedExpertFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
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
    }

    private void copySelectedHearingFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
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
    }

    private void copySelectedSummariesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
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
    }

    private void copySelectedQaFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
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
    }

    private void copySelectedChronologiesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
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
    }

    private void copySelectedFormEFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
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
    }

    private void copySelectedOtherFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
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
    }

    private void copySelectedCorresFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
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
    }

    private void copySelectedExpertFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
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
    }

    private void copySelectedFormHFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
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
    }

    private void copySelectedHearingFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
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
    }

    private void copySelectedSummariesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
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
    }

    private void copySelectedStmtExhibitsFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
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
    }

    private void copySelectedQaFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
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
    }

    private void copySelectedChronologiesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
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
    }

    private void copySelectedFormEFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
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
    }

    private void copySelectedOtherFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
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
}
