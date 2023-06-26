package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
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
            = caseData.getUploadCaseDocumentWrapper().getOtherCollection();

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
}
