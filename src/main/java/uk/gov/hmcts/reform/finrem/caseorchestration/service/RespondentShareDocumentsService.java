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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
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
public class RespondentShareDocumentsService {

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
    }


    private static void copySelectedFormEFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> formEExhibits = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            formEExhibits.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppFormEExhibitsCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedOtherFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> otherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            otherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppOtherCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppOtherCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedCorresFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppCorrespondenceDocsCollShared(list);
                }
            });
        }
    }

    private static void copySelectedExpertFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppExpertEvidenceCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedFormHFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppFormsHCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppFormsHCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedHearingFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppHearingBundlesCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedSummariesFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollectionShared())
                            .orElse(new ArrayList<>());
                    list.add(UploadCaseDocumentCollection.builder().value(sd.getValue()).build());
                    caseData.getUploadCaseDocumentWrapper().setAppCaseSummariesCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedStmtExhibitsFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppStatementsExhibitsCollShared(list);
                }
            });
        }
    }

    private static void copySelectedQaFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> qaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            qaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppQaCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppQaCollectionShared(list);
                }
            });
        }
    }

    private static void copySelectedChronologiesFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> chronologiesList = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            chronologiesList.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollectionShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setAppChronologiesCollectionShared(list);
                }
            });
        }
    }

}
