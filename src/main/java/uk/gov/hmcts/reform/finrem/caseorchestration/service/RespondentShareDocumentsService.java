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
            = caseData.getUploadCaseDocumentWrapper().getSetFormEExhibitsCollection();
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
}
