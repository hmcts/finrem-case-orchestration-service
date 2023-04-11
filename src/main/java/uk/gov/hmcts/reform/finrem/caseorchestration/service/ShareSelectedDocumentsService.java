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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;

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
public class ShareSelectedDocumentsService {

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
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appChronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
        if (ObjectUtils.isNotEmpty(appChronologiesCollection)) {
            appChronologiesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appStatementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
        if (ObjectUtils.isNotEmpty(appStatementsExhibitsCollection)) {
            appStatementsExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appHearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
        if (ObjectUtils.isNotEmpty(appHearingBundlesCollection)) {
            appHearingBundlesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_HEARING_BUNDLES_COLLECTION.getCcdKey(),
                    filename));
            });
        }


        List<UploadCaseDocumentCollection> appFormEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
        if (ObjectUtils.isNotEmpty(appFormEExhibitsCollection)) {
            appFormEExhibitsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appQaCollection
            = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
        if (ObjectUtils.isNotEmpty(appQaCollection)) {
            appQaCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appCaseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
        if (ObjectUtils.isNotEmpty(appCaseSummariesCollection)) {
            appCaseSummariesCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_CASE_SUMMARIES_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        List<UploadCaseDocumentCollection> appFormsHCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
        if (ObjectUtils.isNotEmpty(appFormsHCollection)) {
            appFormsHCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_FORMS_H_COLLECTION.getCcdKey(),
                    filename));
            });
        }


        List<UploadCaseDocumentCollection> appExpertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
        if (ObjectUtils.isNotEmpty(appExpertEvidenceCollection)) {
            appExpertEvidenceCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey(),
                    filename));
            });
        }


        List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
        if (ObjectUtils.isNotEmpty(appCorrespondenceDocsCollection)) {
            appCorrespondenceDocsCollection.forEach(doc -> {
                final String filename = doc.getValue().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + APPLICANT_CORRESPONDENCE_DOC_COLLECTION.getCcdKey(),
                    filename));
            });
        }

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }

    private DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private DynamicMultiSelectList getSelectedDocumentList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
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

    private DynamicMultiSelectList getRoleList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
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
        //Hack
        roleList.add("[RESPSOLICITOR]");
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
            log.info("setting role list for case {}", caseDetails.getId());
            roleList.forEach(role -> dynamicListElements.add(getDynamicMultiSelectListElement(role, role)));
            log.info("dynamic role list {} for case {}", roleList, caseDetails.getId());
        }

        return getRoleList(dynamicListElements, caseDetails.getData().getSolicitorRoleList());
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
    }


    private static void copySelectedFormEFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespFormEExhibitsCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedOtherFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();
            appOtherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespOtherCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespOtherCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedCorresFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespCorrespondenceDocsCollShared(list);
                }
            });
        }
    }

    private static void copySelectedExpertFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespExpertEvidenceCollShared(list);
                }
            });
        }
    }

    private static void copySelectedFormHFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespFormsHCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespFormsHCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedHearingFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespHearingBundlesCollShared(list);
                }
            });
        }
    }

    private static void copySelectedSummariesFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollectionShared())
                            .orElse(new ArrayList<>());
                    list.add(UploadCaseDocumentCollection.builder().value(sd.getValue()).build());
                    caseData.getUploadCaseDocumentWrapper().setRespCaseSummariesCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedStmtExhibitsFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespStatementsExhibitsCollShared(list);
                }
            });
        }
    }

    private static void copySelectedQaFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespQaCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespQaCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedChronologiesFilesToResp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setRespChronologiesCollectionShared(list);
                }
            });
        }
    }

}
