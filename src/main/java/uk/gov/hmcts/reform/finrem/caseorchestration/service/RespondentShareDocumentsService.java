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


    private static void copySelectedFormEFilesToApp(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(RESP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_OTHER_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_FORM_H_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
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
        if (collName.equalsIgnoreCase(RESP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
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


    private void copySelectedCorresFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1CorrespDocsShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1CorrespDocsShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedExpertFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1ExpertEvidenceShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1ExpertEvidenceShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedFormHFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1FormHsShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1FormHsShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedHearingFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1HearingBundlesShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1HearingBundlesShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedSummariesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1SummariesShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1SummariesShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1Collection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1StmtsExhibitsShared())
                            .orElse(new ArrayList<>());

                    intv1Collection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1StmtsExhibitsShared(intv1Collection);
                }
            });
        }
    }

    private void copySelectedQaFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1AppQaCollection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1QaShared())
                            .orElse(new ArrayList<>());

                    intv1AppQaCollection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1QaShared(intv1AppQaCollection);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1AppChronologiesCollection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1ChronologiesShared())
                            .orElse(new ArrayList<>());

                    intv1AppChronologiesCollection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1ChronologiesShared(intv1AppChronologiesCollection);
                }
            });
        }
    }

    private void copySelectedFormEFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1AppFormEExhibitsCollection =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1FormEsExhibitsShared())
                            .orElse(new ArrayList<>());

                    intv1AppFormEExhibitsCollection.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1FormEsExhibitsShared(intv1AppFormEExhibitsCollection);
                }
            });
        }
    }

    private void copySelectedOtherFilesToIntv1(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> intv1AppOtherCollectionShared =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv1OtherShared())
                            .orElse(new ArrayList<>());

                    intv1AppOtherCollectionShared.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv1OtherShared(intv1AppOtherCollectionShared);
                }
            });
        }
    }

    private void copySelectedCorresFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2CorrespDocsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2CorrespDocsShared(list);
                }
            });
        }
    }

    private void copySelectedExpertFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2ExpertEvidenceShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2ExpertEvidenceShared(list);
                }
            });
        }
    }

    private void copySelectedFormHFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2FormHsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2FormHsShared(list);
                }
            });
        }
    }

    private void copySelectedHearingFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2HearingBundlesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2HearingBundlesShared(list);
                }
            });
        }
    }

    private void copySelectedSummariesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2SummariesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2SummariesShared(list);
                }
            });
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2StmtsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2StmtsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedQaFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2QaShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2QaShared(list);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2ChronologiesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2ChronologiesShared(list);
                }
            });
        }
    }

    private void copySelectedFormEFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2FormEsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2FormEsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedOtherFilesToIntv2(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv2OtherShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv2OtherShared(list);
                }
            });
        }
    }


    private void copySelectedCorresFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3CorrespDocsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3CorrespDocsShared(list);
                }
            });
        }
    }

    private void copySelectedExpertFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3ExpertEvidenceShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3ExpertEvidenceShared(list);
                }
            });
        }
    }

    private void copySelectedFormHFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3FormHsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3FormHsShared(list);
                }
            });
        }
    }

    private void copySelectedHearingFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3HearingBundlesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3HearingBundlesShared(list);
                }
            });
        }
    }

    private void copySelectedSummariesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3SummariesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3SummariesShared(list);
                }
            });
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3StmtsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3StmtsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedQaFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3QaShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3QaShared(list);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3ChronologiesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3ChronologiesShared(list);
                }
            });
        }
    }

    private void copySelectedFormEFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3FormEsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3FormEsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedOtherFilesToIntv3(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv3OtherShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv3OtherShared(list);
                }
            });
        }
    }

    private void copySelectedCorresFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CORRESPONDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4CorrespDocsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4CorrespDocsShared(list);
                }
            });
        }
    }

    private void copySelectedExpertFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_EXPERT_EVIDENCE_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4ExpertEvidenceShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4ExpertEvidenceShared(list);
                }
            });
        }
    }

    private void copySelectedFormHFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORMS_H_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4FormHsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4FormHsShared(list);
                }
            });
        }
    }

    private void copySelectedHearingFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_HEARING_BUNDLES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4HearingBundlesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4HearingBundlesShared(list);
                }
            });
        }
    }

    private void copySelectedSummariesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CASE_SUMMARIES_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4SummariesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4SummariesShared(list);
                }
            });
        }
    }

    private void copySelectedStmtExhibitsFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_STATEMENTS_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> coll = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
            coll.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4StmtsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4StmtsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedQaFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_QUESTIONNAIRES_ANSWERS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appQaCollection = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
            appQaCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4QaShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4QaShared(list);
                }
            });
        }
    }

    private void copySelectedChronologiesFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_CHRONOLOGIES_STATEMENTS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appChronologiesCollection = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
            appChronologiesCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4ChronologiesShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4ChronologiesShared(list);
                }
            });
        }
    }

    private void copySelectedFormEFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_FORM_E_EXHIBITS_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appFormEExhibitsCollection = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
            appFormEExhibitsCollection.forEach(sd -> {
                if (String.valueOf(sd.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4FormEsExhibitsShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(sd.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4FormEsExhibitsShared(list);
                }
            });
        }
    }

    private void copySelectedOtherFilesToIntv4(FinremCaseData caseData, String collId, String collName) {
        if (collName.equalsIgnoreCase(APP_OTHER_COLLECTION.getCcdKey())) {
            List<UploadCaseDocumentCollection> appOtherCollection = caseData.getUploadCaseDocumentWrapper().getRespOtherCollection();
            appOtherCollection.forEach(d -> {
                if (String.valueOf(d.getId()).equalsIgnoreCase(collId)) {
                    List<UploadCaseDocumentCollection> list =
                        Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getIntv4OtherShared())
                            .orElse(new ArrayList<>());

                    list.add(UploadCaseDocumentCollection.builder()
                        .value(d.getValue()).build());

                    caseData.getUploadCaseDocumentWrapper().setIntv4OtherShared(list);
                }
            });
        }
    }
}
