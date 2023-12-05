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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APPLICANT_CORRESPONDENCE_DOC_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION;

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

        buildDynamicListElements(appOtherCollection, dynamicListElements, APP_OTHER_COLLECTION);

        List<UploadCaseDocumentCollection> appChronologiesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppChronologiesCollection();
        buildDynamicListElements(appChronologiesCollection, dynamicListElements, APP_CHRONOLOGIES_STATEMENTS_COLLECTION);

        List<UploadCaseDocumentCollection> appStatementsExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppStatementsExhibitsCollection();
        buildDynamicListElements(appStatementsExhibitsCollection, dynamicListElements, APP_STATEMENTS_EXHIBITS_COLLECTION);

        List<UploadCaseDocumentCollection> appHearingBundlesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppHearingBundlesCollection();
        buildDynamicListElements(appHearingBundlesCollection, dynamicListElements, APP_HEARING_BUNDLES_COLLECTION);


        List<UploadCaseDocumentCollection> appFormEExhibitsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormEExhibitsCollection();
        buildDynamicListElements(appFormEExhibitsCollection, dynamicListElements, APP_FORM_E_EXHIBITS_COLLECTION);

        List<UploadCaseDocumentCollection> appQaCollection
            = caseData.getUploadCaseDocumentWrapper().getAppQaCollection();
        buildDynamicListElements(appQaCollection, dynamicListElements, APP_QUESTIONNAIRES_ANSWERS_COLLECTION);

        List<UploadCaseDocumentCollection> appCaseSummariesCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCaseSummariesCollection();
        buildDynamicListElements(appCaseSummariesCollection, dynamicListElements, APP_CASE_SUMMARIES_COLLECTION);

        List<UploadCaseDocumentCollection> appFormsHCollection
            = caseData.getUploadCaseDocumentWrapper().getAppFormsHCollection();
        buildDynamicListElements(appFormsHCollection, dynamicListElements, APP_FORMS_H_COLLECTION);


        List<UploadCaseDocumentCollection> appExpertEvidenceCollection
            = caseData.getUploadCaseDocumentWrapper().getAppExpertEvidenceCollection();
        buildDynamicListElements(appExpertEvidenceCollection, dynamicListElements, APP_EXPERT_EVIDENCE_COLLECTION);


        List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection
            = caseData.getUploadCaseDocumentWrapper().getAppCorrespondenceDocsCollection();
        buildDynamicListElements(appCorrespondenceDocsCollection, dynamicListElements, APPLICANT_CORRESPONDENCE_DOC_COLLECTION);

        return getSelectedDocumentList(dynamicListElements, caseData.getSourceDocumentList());
    }

    private void buildDynamicListElements(List<UploadCaseDocumentCollection> appOtherCollection,
                                          List<DynamicMultiSelectListElement> dynamicListElements,
                                          CaseDocumentCollectionType appOtherCollection1) {
        if (ObjectUtils.isNotEmpty(appOtherCollection)) {
            appOtherCollection.forEach(doc -> {
                final String filename = doc.getUploadCaseDocument().getCaseDocuments().getDocumentFilename();
                dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId() + "#" + appOtherCollection1.getCcdKey(),
                    appOtherCollection1.getCcdKey() + " -> " + filename));
            });
        }
    }
}
