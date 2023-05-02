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

        if (role.equals(CaseRole.RESP_SOLICITOR.getValue())) {
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


    private void copySelectedCorresFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
            setIntv1CorrespDocsShared(caseData, collId, coll);
        }
    }

    private void copySelectedExpertFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
            setIntv1ExpertEvidenceShared(caseData, collId, coll);
        }
    }

    private void copySelectedFormHFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
            setIntv1FormHsShared(caseData, collId, coll);
        }
    }

    private void copySelectedHearingFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
            setIntv1HearingBundlesShared(caseData, collId, coll);
        }
    }

    private void copySelectedSummariesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
            setIntv1SummariesShared(caseData, collId, coll);
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
            setIntv1StmtsExhibitsShared(caseData, collId, coll);
        }
    }

    private void copySelectedQaFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            setIntv1QaShared(caseData, collId, appQaCollection);
        }
    }

    private void copySelectedChronologiesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            setIntv1ChronologiesShared(caseData, collId, appChronologiesCollection);
        }
    }

    private void copySelectedFormEFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            setIntv1FormEsExhibitsShared(caseData, collId, appFormEExhibitsCollection);
        }
    }

    private void copySelectedOtherFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            setIntv1OtherShared(caseData, collId, appOtherCollection);
        }
    }

    private void copySelectedCorresFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
            setIntv2CorrespDocsShared(caseData, collId, coll);
        }
    }

    private void copySelectedExpertFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
            setIntv2ExpertEvidenceShared(caseData, collId, coll);
        }
    }

    private void copySelectedFormHFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
            setIntv2FormHsShared(caseData, collId, coll);
        }
    }

    private void copySelectedHearingFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
            setIntv2HearingBundlesShared(caseData, collId, coll);
        }
    }

    private void copySelectedSummariesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
            setIntv2SummariesShared(caseData, collId, coll);
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
            setIntv2StmtsExhibitsShared(caseData, collId, coll);
        }
    }

    private void copySelectedQaFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            setIntv2QaShared(caseData, collId, appQaCollection);
        }
    }

    private void copySelectedChronologiesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            setIntv2ChronologiesShared(caseData, collId, appChronologiesCollection);
        }
    }

    private void copySelectedFormEFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            setIntv2FormEsExhibitsShared(caseData, collId, appFormEExhibitsCollection);
        }
    }

    private void copySelectedOtherFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            setIntv2OtherShared(caseData, collId, appOtherCollection);
        }
    }


    private void copySelectedCorresFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
            setIntv3CorrespDocsShared(caseData, collId, coll);
        }
    }

    private void copySelectedExpertFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
            setIntv3ExpertEvidenceShared(caseData, collId, coll);
        }
    }

    private void copySelectedFormHFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
            setIntv3FormHsShared(caseData, collId, coll);
        }
    }

    private void copySelectedHearingFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
            setIntv3HearingBundlesShared(caseData, collId, coll);
        }
    }

    private void copySelectedSummariesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
            setIntv3SummariesShared(caseData, collId, coll);
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
            setIntv3StmtsExhibitsShared(caseData, collId, coll);
        }
    }

    private void copySelectedQaFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            setIntv3QaShared(caseData, collId, appQaCollection);
        }
    }

    private void copySelectedChronologiesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            setIntv3ChronologiesShared(caseData, collId, appChronologiesCollection);
        }
    }

    private void copySelectedFormEFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            setIntv3FormEsExhibitsShared(caseData, collId, appFormEExhibitsCollection);
        }
    }

    private void copySelectedOtherFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            setIntv3OtherShared(caseData, collId, appOtherCollection);
        }
    }

    private void copySelectedCorresFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
            setIntv4CorrespDocsShared(caseData, collId, coll);
        }
    }


    private void copySelectedExpertFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
            setIntv4ExpertEvidenceShared(caseData, collId, coll);
        }
    }

    private void copySelectedFormHFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
            setIntv4FormHsShared(caseData, collId, coll);
        }
    }

    private void copySelectedHearingFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
            setIntv4HearingBundlesShared(caseData, collId, coll);
        }
    }

    private void copySelectedSummariesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
            setIntv4SummariesShared(caseData, collId, coll);
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
            setIntv4StmtsExhibitsShared(caseData, collId, coll);
        }
    }

    private void copySelectedQaFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            setIntv4QaShared(caseData, collId, appQaCollection);
        }
    }

    private void copySelectedChronologiesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            setIntv4ChronologiesShared(caseData, collId, appChronologiesCollection);
        }
    }

    private void copySelectedFormEFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            setIntv4FormEsExhibitsShared(caseData, collId, appFormEExhibitsCollection);
        }
    }

    private void copySelectedOtherFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            setIntv4OtherShared(caseData, collId, appOtherCollection);
        }
    }

}
