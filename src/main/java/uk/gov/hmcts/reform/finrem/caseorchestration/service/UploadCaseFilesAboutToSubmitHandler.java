package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_STATEMENTS_EXHIBITS_COLLECTION;


@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCaseFilesAboutToSubmitHandler {

    public static final String TRIAL_BUNDLE_SELECTED_ERROR =
        "To upload a hearing bundle please use the Manage hearing "
            + "bundles event which can be found on the drop-down list on the home page";

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    public static final String TRIAL_BUNDLE_TYPE = "Trial Bundle";

    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    private void setUploadedDocumentsToCollections(Map<String, Object> caseData) {



        List<ContestedUploadedDocumentData> uploadedDocuments = getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS);

        filterConfidentialDocs(uploadedDocuments, caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        filterFdrDocs(uploadedDocuments, caseData, FDR_DOCS_COLLECTION);

        filterHearingBundles(uploadedDocuments, caseData, APP_HEARING_BUNDLES_COLLECTION, APPLICANT);
        filterFormEExhibits(uploadedDocuments, caseData, APP_FORM_E_EXHIBITS_COLLECTION, APPLICANT);
        filterChronologiesStatements(uploadedDocuments, caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION, APPLICANT);
        filterQuestionnairesAnswers(uploadedDocuments, caseData, APP_QUESTIONNAIRES_ANSWERS_COLLECTION, APPLICANT);
        filterStatementsExhibits(uploadedDocuments, caseData, APP_STATEMENTS_EXHIBITS_COLLECTION, APPLICANT);
        filterCaseSummaries(uploadedDocuments, caseData, APP_CASE_SUMMARIES_COLLECTION, APPLICANT);
        filterFormsH(uploadedDocuments, caseData, APP_FORMS_H_COLLECTION, APPLICANT);
        filterExpertEvidence(uploadedDocuments, caseData, APP_EXPERT_EVIDENCE_COLLECTION, APPLICANT);
        filterCorrespondenceDocs(uploadedDocuments, caseData, APP_CORRESPONDENCE_COLLECTION, APPLICANT);
        filterOtherDocs(uploadedDocuments, caseData, APP_OTHER_COLLECTION, APPLICANT);

        filterHearingBundles(uploadedDocuments, caseData, RESP_HEARING_BUNDLES_COLLECTION, RESPONDENT);
        filterFormEExhibits(uploadedDocuments, caseData, RESP_FORM_E_EXHIBITS_COLLECTION, RESPONDENT);
        filterChronologiesStatements(uploadedDocuments, caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, RESPONDENT);
        filterQuestionnairesAnswers(uploadedDocuments, caseData, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION, RESPONDENT);
        filterStatementsExhibits(uploadedDocuments, caseData, RESP_STATEMENTS_EXHIBITS_COLLECTION, RESPONDENT);
        filterCaseSummaries(uploadedDocuments, caseData, RESP_CASE_SUMMARIES_COLLECTION, RESPONDENT);
        filterFormsH(uploadedDocuments, caseData, RESP_FORM_H_COLLECTION, RESPONDENT);
        filterExpertEvidence(uploadedDocuments, caseData, RESP_EXPERT_EVIDENCE_COLLECTION, RESPONDENT);
        filterCorrespondenceDocs(uploadedDocuments, caseData, RESP_CORRESPONDENCE_COLLECTION, RESPONDENT);
        filterOtherDocs(uploadedDocuments, caseData, RESP_OTHER_COLLECTION, RESPONDENT);


        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }

    private boolean isTypeValidForHearingBundle(String caseDocumentType) {
        return caseDocumentType.equals("Trial Bundle");
    }

    private boolean isTypeValidForFormEExhibits(String caseDocumentType) {
        return caseDocumentType.equals("Applicant - Form E");
    }

    private boolean isTypeValidForChronologiesStatements(String caseDocumentType) {
        return caseDocumentType.equals("Statement of Issues")
            || caseDocumentType.equals("Chronology")
            || caseDocumentType.equals("Form G");
    }

    private boolean isTypeValidForQuestionnairesAnswers(String caseDocumentType) {
        return caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire");
    }

    private boolean isTypeValidForStatementsExhibits(String caseDocumentType) {
        return caseDocumentType.equals("Statement/Affidavit")
            || caseDocumentType.equals("Witness Statement/Affidavit");
    }

    private boolean isTypeValidForCaseSummaries(String caseDocumentType) {
        return caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Case Summary");
    }

    private boolean isTypeValidForFormsH(String caseDocumentType) {
        return caseDocumentType.equals("Form H");
    }

    private boolean isTypeValidForExpertEvidence(String caseDocumentType) {
        return caseDocumentType.equals("Valuation Report")
            || caseDocumentType.equals("Expert Evidence");
    }

    private boolean isTypeValidForCorrespondenceDocs(String caseDocumentType) {
        return caseDocumentType.equals("Offers")
            || caseDocumentType.equals("Letter from Applicant");
    }

    private boolean isTypeValidForOtherDocs(String caseDocumentType) {
        return caseDocumentType.equals("other")
            || caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Pension Plan");
    }


    private void filterConfidentialDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                                        Map<String, Object> caseData,
                                        String collectionName) {

        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> confidentialFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentType() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential().equalsIgnoreCase("Yes"))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> confidentialDocsCollection = getDocumentCollection(caseData, collectionName);
        confidentialDocsCollection.addAll(confidentialFiltered);
        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        if (!confidentialDocsCollection.isEmpty()) {
            List<ConfidentialUploadedDocumentData> confidentialDocs = confidentialDocsCollection.stream().map(
                doc -> ConfidentialUploadedDocumentData.builder()
                    .confidentialUploadedDocument(ConfidentialUploadedDocument.builder()
                        .documentFileName(doc.getUploadedCaseDocument().getCaseDocuments().getDocumentFilename())
                        .documentComment(doc.getUploadedCaseDocument().getHearingDetails())
                        .documentLink(doc.getUploadedCaseDocument().getCaseDocuments())
                        .build()).build()).collect((Collectors.toList()));
            caseData.put(collectionName, confidentialDocs);
        }
    }

    private void filterFdrDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                               Map<String, Object> caseData,
                               String collectionName) {

        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentType() != null
                && d.getUploadedCaseDocument().getCaseDocumentFdr() != null
                && d.getUploadedCaseDocument().getCaseDocumentFdr().equalsIgnoreCase("Yes"))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> fdrDocsCollection = getDocumentCollection(caseData, collectionName);
        fdrDocsCollection.addAll(fdrFiltered);
        log.info("Adding items: {}, to FDR Documents Collection", fdrFiltered);
        uploadedDocuments.removeAll(fdrFiltered);


        if (!fdrDocsCollection.isEmpty()) {
            caseData.put(collectionName, fdrDocsCollection);
        }

    }

    private void filterHearingBundles(List<ContestedUploadedDocumentData> uploadedDocuments,
                                      Map<String, Object> caseData,
                                      String collection,
                                      String party) {
        List<ContestedUploadedDocumentData> hearingBundlesFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForHearingBundle(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> hearingBundlesCollection = getDocumentCollection(caseData, collection);
        hearingBundlesCollection.addAll(hearingBundlesFiltered);
        log.info("Adding items: {}, to Hearing Bundles Collection", hearingBundlesFiltered);
        uploadedDocuments.removeAll(hearingBundlesFiltered);

        if (!hearingBundlesCollection.isEmpty()) {
            caseData.put(collection, hearingBundlesCollection);
        }
    }

    private void filterFormEExhibits(List<ContestedUploadedDocumentData> uploadedDocuments,
                                     Map<String, Object> caseData,
                                     String collection,
                                     String party) {
        List<ContestedUploadedDocumentData> formEExhibitsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForFormEExhibits(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> formEExhibitsCollection = getDocumentCollection(caseData, collection);
        formEExhibitsCollection.addAll(formEExhibitsFiltered);
        log.info("Adding items: {}, to Forms E & Exhibits Collection", formEExhibitsFiltered);
        uploadedDocuments.removeAll(formEExhibitsFiltered);

        if (!formEExhibitsCollection.isEmpty()) {
            caseData.put(collection, formEExhibitsCollection);
        }
    }

    private void filterChronologiesStatements(List<ContestedUploadedDocumentData> uploadedDocuments,
                                              Map<String, Object> caseData,
                                              String collection,
                                              String party) {
        List<ContestedUploadedDocumentData> chronologiesStatementsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForChronologiesStatements(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> chronologiesStatementsCollection = getDocumentCollection(caseData, collection);
        chronologiesStatementsCollection.addAll(chronologiesStatementsFiltered);
        log.info("Adding items: {}, to Chronologies and Statements of Issues Collection", chronologiesStatementsFiltered);
        uploadedDocuments.removeAll(chronologiesStatementsFiltered);

        if (!chronologiesStatementsCollection.isEmpty()) {
            caseData.put(collection, chronologiesStatementsCollection);
        }
    }

    private void filterQuestionnairesAnswers(List<ContestedUploadedDocumentData> uploadedDocuments,
                                             Map<String, Object> caseData,
                                             String collection,
                                             String party) {
        List<ContestedUploadedDocumentData> questionnairesAnswersFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForQuestionnairesAnswers(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> questionnairesAnswersCollection = getDocumentCollection(caseData, collection);
        questionnairesAnswersCollection.addAll(questionnairesAnswersFiltered);
        log.info("Adding items: {}, to Questionnaires & Answers to Questionnaires & Exhibits Collection", questionnairesAnswersFiltered);
        uploadedDocuments.removeAll(questionnairesAnswersFiltered);

        if (!questionnairesAnswersCollection.isEmpty()) {
            caseData.put(collection, questionnairesAnswersCollection);
        }
    }

    private void filterStatementsExhibits(List<ContestedUploadedDocumentData> uploadedDocuments,
                                          Map<String, Object> caseData,
                                          String collection,
                                          String party) {
        List<ContestedUploadedDocumentData> statementsExhibitsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForStatementsExhibits(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> statementsExhibitsCollection = getDocumentCollection(caseData, collection);
        statementsExhibitsCollection.addAll(statementsExhibitsFiltered);
        log.info("Adding items: {}, to Statements & Exhibits Collection", statementsExhibitsFiltered);
        uploadedDocuments.removeAll(statementsExhibitsFiltered);

        if (!statementsExhibitsCollection.isEmpty()) {
            caseData.put(collection, statementsExhibitsCollection);
        }
    }

    private void filterCaseSummaries(List<ContestedUploadedDocumentData> uploadedDocuments,
                                     Map<String, Object> caseData,
                                     String collection,
                                     String party) {
        List<ContestedUploadedDocumentData> caseSummariesFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForCaseSummaries(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> caseSummariesCollection = getDocumentCollection(caseData, collection);
        caseSummariesCollection.addAll(caseSummariesFiltered);
        log.info("Adding items: {}, to Case Summaries Collection", caseSummariesFiltered);
        uploadedDocuments.removeAll(caseSummariesFiltered);

        if (!caseSummariesCollection.isEmpty()) {
            caseData.put(collection, caseSummariesCollection);
        }
    }

    private void filterFormsH(List<ContestedUploadedDocumentData> uploadedDocuments,
                              Map<String, Object> caseData,
                              String collection,
                              String party) {
        List<ContestedUploadedDocumentData> formsHFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForFormsH(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> formsHCollection = getDocumentCollection(caseData, collection);
        formsHCollection.addAll(formsHFiltered);
        log.info("Adding items: {}, to Forms H Collection", formsHFiltered);
        uploadedDocuments.removeAll(formsHFiltered);

        if (!formsHCollection.isEmpty()) {
            caseData.put(collection, formsHCollection);
        }
    }

    private void filterExpertEvidence(List<ContestedUploadedDocumentData> uploadedDocuments,
                                      Map<String, Object> caseData,
                                      String collection,
                                      String party) {
        List<ContestedUploadedDocumentData> expertEvidenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForExpertEvidence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> expertEvidenceCollection = getDocumentCollection(caseData, collection);
        expertEvidenceCollection.addAll(expertEvidenceFiltered);
        log.info("Adding items: {}, to Expert Evidence Collection", expertEvidenceFiltered);
        uploadedDocuments.removeAll(expertEvidenceFiltered);

        if (!expertEvidenceCollection.isEmpty()) {
            caseData.put(collection, expertEvidenceCollection);
        }
    }

    private void filterCorrespondenceDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                                          Map<String, Object> caseData,
                                          String collection,
                                          String party) {
        List<ContestedUploadedDocumentData> correspondenceDocsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForCorrespondenceDocs(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> correspondenceCollection = getDocumentCollection(caseData, collection);
        correspondenceCollection.addAll(correspondenceDocsFiltered);
        log.info("Adding items: {}, to Correspondence Docs Collection", correspondenceDocsFiltered);
        uploadedDocuments.removeAll(correspondenceDocsFiltered);

        if (!correspondenceCollection.isEmpty()) {
            caseData.put(collection, correspondenceCollection);
        }
    }

    private void filterOtherDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                                 Map<String, Object> caseData,
                                 String collection,
                                 String party) {
        List<ContestedUploadedDocumentData> otherDocsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForOtherDocs(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> otherCollection = getDocumentCollection(caseData, collection);
        otherCollection.addAll(otherDocsFiltered);
        log.info("Adding items: {}, to Other Docs Collection", otherDocsFiltered);
        uploadedDocuments.removeAll(otherDocsFiltered);

        if (!otherCollection.isEmpty()) {
            caseData.put(collection, otherCollection);
        }
    }

    public AboutToStartOrSubmitCallbackResponse handle(Map<String, Object> caseData) {

        AboutToStartOrSubmitCallbackResponse response = getCallBackResponse(caseData);

        setWarningsAndErrors(caseData, response);
        if (isNotEmpty(response.getErrors())) {
            return response;
        }

        setUploadedDocumentsToCollections(caseData);
        response.setData(caseData);
        return response;
    }

    private void setWarningsAndErrors(Map<String, Object> caseData, AboutToStartOrSubmitCallbackResponse response) {
        if (featureToggleService.isManageBundleEnabled()
            && isTrialBundleSelectedInAnyUploadedFile(caseData)) {
            response.getErrors().add(TRIAL_BUNDLE_SELECTED_ERROR);
        }
    }

    private AboutToStartOrSubmitCallbackResponse getCallBackResponse(Map<String, Object> caseData) {
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();
    }

    private boolean isTrialBundleSelectedInAnyUploadedFile(Map<String, Object> caseData) {
        return !getTrialBundleUploadedList(getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS)).isEmpty();
    }

    private List<ContestedUploadedDocumentData> getTrialBundleUploadedList(List<ContestedUploadedDocumentData> uploadedDocuments) {

        return uploadedDocuments.stream()
            .filter(d -> isTrialBundle(d.getUploadedCaseDocument()))
            .collect(Collectors.toList());
    }

    private boolean isTrialBundle(ContestedUploadedDocument uploadedCaseDocument) {
        return Optional.ofNullable(uploadedCaseDocument)
            .map(ContestedUploadedDocument::getCaseDocumentType)
            .filter(type -> type.equals(TRIAL_BUNDLE_TYPE))
            .isPresent();
    }
}
