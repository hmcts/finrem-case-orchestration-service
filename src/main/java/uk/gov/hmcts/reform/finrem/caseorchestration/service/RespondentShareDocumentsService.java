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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION;

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

        buildDynamicListElements(otherCollection, "otherCollection:: filename {} caseId {}", caseId, dynamicListElements, RESP_OTHER_COLLECTION);

        List<UploadCaseDocumentCollection> chronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespChronologiesCollection();
        buildDynamicListElements(chronologiesCollection, "chronologiesCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_CHRONOLOGIES_STATEMENTS_COLLECTION);

        List<UploadCaseDocumentCollection> statementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection();
        buildDynamicListElements(statementsExhibitsCollection, "statementsExhibitsCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_STATEMENTS_EXHIBITS_COLLECTION);

        List<UploadCaseDocumentCollection> hearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespHearingBundlesCollection();
        buildDynamicListElements(hearingBundlesCollection, "hearingBundlesCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_HEARING_BUNDLES_COLLECTION);


        List<UploadCaseDocumentCollection> formEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespFormEExhibitsCollection();
        buildDynamicListElements(formEExhibitsCollection, "formEExhibitsCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_FORM_E_EXHIBITS_COLLECTION);

        List<UploadCaseDocumentCollection> qaCollection
            = caseData.getUploadCaseDocumentWrapper().getRespQaCollection();
        buildDynamicListElements(qaCollection, "qaCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_QUESTIONNAIRES_ANSWERS_COLLECTION);

        List<UploadCaseDocumentCollection> caseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getRespCaseSummariesCollection();
        buildDynamicListElements(caseSummariesCollection, "caseSummariesCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_CASE_SUMMARIES_COLLECTION);

        List<UploadCaseDocumentCollection> formsHCollection
            = caseData.getUploadCaseDocumentWrapper().getRespFormsHCollection();
        buildDynamicListElements(formsHCollection, "formsHCollection:: filename {} caseId {}", caseId, dynamicListElements, RESP_FORM_H_COLLECTION);


        List<UploadCaseDocumentCollection> expertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getRespExpertEvidenceCollection();
        buildDynamicListElements(expertEvidenceCollection, "expertEvidenceCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESP_EXPERT_EVIDENCE_COLLECTION);


        List<UploadCaseDocumentCollection> correspondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getRespCorrespondenceDocsColl();
        buildDynamicListElements(correspondenceDocsCollection, "correspondenceDocsCollection:: filename {} caseId {}", caseId, dynamicListElements,
            RESPONDENT_CORRESPONDENCE_COLLECTION);

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }

    private void buildDynamicListElements(List<UploadCaseDocumentCollection> otherCollection, String format, String caseId,
                                          List<DynamicMultiSelectListElement> dynamicListElements, CaseDocumentCollectionType respOtherCollection) {
        if (ObjectUtils.isNotEmpty(otherCollection)) {
            otherCollection.forEach(doc -> {
                final String filename = doc.getUploadCaseDocument().getCaseDocuments().getDocumentFilename();
                log.info(format, filename, caseId);
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + respOtherCollection.getCcdKey(),
                    respOtherCollection.getCcdKey() + " -> " + filename));
            });
        }
    }
}
