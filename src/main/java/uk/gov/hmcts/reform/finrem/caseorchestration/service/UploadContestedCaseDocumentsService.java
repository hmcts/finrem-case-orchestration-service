package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_TRIAL_BUNDLE_COLLECTION;
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
public class UploadContestedCaseDocumentsService {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";

    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) {

        boolean respondentJourneyEnabled = featureToggleService.isRespondentJourneyEnabled();
        log.info("Respondent Solicitor Journey toggle is: {}", respondentJourneyEnabled);

        List<ContestedUploadedDocumentData> uploadedDocuments = getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS);

        if (respondentJourneyEnabled) {
            filterHearingBundlesNoParty(uploadedDocuments, caseData, HEARING_BUNDLES_COLLECTION);

            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_HEARING_BUNDLES_COLLECTION, APPLICANT, this::isTypeValidForHearingBundle);
            filterConfidentialDocs(uploadedDocuments, caseData, APPLICANT_CONFIDENTIAL_DOCS_COLLECTION, APPLICANT);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_FORM_E_EXHIBITS_COLLECTION, APPLICANT, this::isTypeValidForFormEExhibits);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION, APPLICANT,
                this::isTypeValidForChronologiesStatements);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_QUESTIONNAIRES_ANSWERS_COLLECTION, APPLICANT,
                this::isTypeValidForQuestionnairesAnswers);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_STATEMENTS_EXHIBITS_COLLECTION, APPLICANT,
                this::isTypeValidForStatementsExhibits);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_CASE_SUMMARIES_COLLECTION, APPLICANT, this::isTypeValidForCaseSummaries);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_FORMS_H_COLLECTION, APPLICANT, this::isTypeValidForFormsH);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_EXPERT_EVIDENCE_COLLECTION, APPLICANT, this::isTypeValidForExpertEvidence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_CORRESPONDENCE_COLLECTION, APPLICANT,
                this::isTypeValidForCorrespondenceDocs);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APP_OTHER_COLLECTION, APPLICANT, this::isTypeValidForOtherDocs);

            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_HEARING_BUNDLES_COLLECTION, RESPONDENT,
                this::isTypeValidForHearingBundle);
            filterConfidentialDocs(uploadedDocuments, caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION, RESPONDENT);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_FORM_E_EXHIBITS_COLLECTION, RESPONDENT,
                this::isTypeValidForFormEExhibits);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, RESPONDENT,
                this::isTypeValidForChronologiesStatements);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION, RESPONDENT,
                this::isTypeValidForQuestionnairesAnswers);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_STATEMENTS_EXHIBITS_COLLECTION, RESPONDENT,
                this::isTypeValidForStatementsExhibits);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_CASE_SUMMARIES_COLLECTION, RESPONDENT, this::isTypeValidForCaseSummaries);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_FORM_H_COLLECTION, RESPONDENT, this::isTypeValidForFormsH);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_EXPERT_EVIDENCE_COLLECTION, RESPONDENT,
                this::isTypeValidForExpertEvidence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_CORRESPONDENCE_COLLECTION, RESPONDENT,
                this::isTypeValidForCorrespondenceDocs);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESP_OTHER_COLLECTION, RESPONDENT, this::isTypeValidForOtherDocs);
        } else {
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APPLICANT_CORRESPONDENCE_COLLECTION, APPLICANT,
                this::isTypeValidForCorrespondence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APPLICANT_FR_FORM_COLLECTION, APPLICANT, this::isTypeValidForForms);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APPLICANT_EVIDENCE_COLLECTION, APPLICANT, this::isTypeValidForEvidence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION, APPLICANT, this::isTypeValidForTrialBundle);

            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESPONDENT_CORRESPONDENCE_COLLECTION, RESPONDENT,
                this::isTypeValidForCorrespondence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESPONDENT_FR_FORM_COLLECTION, RESPONDENT, this::isTypeValidForForms);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESPONDENT_EVIDENCE_COLLECTION, RESPONDENT, this::isTypeValidForEvidence);
            filterDocumentsByPartyAndType(uploadedDocuments, caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION, RESPONDENT,
                this::isTypeValidForTrialBundle);
        }

        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);

        return caseData;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {});
    }

    private boolean isTypeValidForCorrespondence(String caseDocumentType) {
        return caseDocumentType.equals("Letter from Applicant");
    }

    private boolean isTypeValidForForms(String caseDocumentType) {
        return caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Applicant - Form E")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Form G")
            || caseDocumentType.equals("Form H");
    }

    private boolean isTypeValidForEvidence(String caseDocumentType) {
        return caseDocumentType.equals("Statement of Issues")
            || caseDocumentType.equals("Chronology")
            || caseDocumentType.equals("Case Summary")
            || caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire")
            || caseDocumentType.equals("Valuation Report")
            || caseDocumentType.equals("Pension Plan")
            || caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Expert Evidence")
            || caseDocumentType.equals("Witness Statement/Affidavit")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Offers")
            || caseDocumentType.equals("other");
    }

    private boolean isTypeValidForTrialBundle(String caseDocumentType) {
        return caseDocumentType.equals("Trial Bundle");
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

    private void filterHearingBundlesNoParty(List<ContestedUploadedDocumentData> uploadedDocuments,
                                             Map<String, Object> caseData,
                                             String collection) {
        List<ContestedUploadedDocumentData> filteredHearingBundle = filterDocumentsByType(uploadedDocuments, this::isTypeValidForHearingBundle)
            .stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() == null)
            .collect(Collectors.toList());

        addFilteredDocumentsToCaseData(filteredHearingBundle, uploadedDocuments, caseData, collection);
    }

    private void filterConfidentialDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                                        Map<String, Object> caseData,
                                        String collection,
                                        String party) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> confidentialFiltered = filterDocumentsByParty(uploadedDocuments, party)
            .stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null)
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentConfidential() != null)
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentConfidential().equalsIgnoreCase("Yes"))
            .collect(Collectors.toList());

        addFilteredDocumentsToCaseData(confidentialFiltered, uploadedDocuments, caseData, collection);
    }

    private void filterDocumentsByPartyAndType(List<ContestedUploadedDocumentData> uploadedDocuments,
                                       Map<String, Object> caseData,
                                       String collection,
                                       String party,
                                       Predicate<String> p) {
        List<ContestedUploadedDocumentData> filteredDocuments = filterDocumentsByParty(uploadedDocuments, party);
        filteredDocuments = filterDocumentsByType(filteredDocuments, p);

        addFilteredDocumentsToCaseData(filteredDocuments, uploadedDocuments, caseData, collection);
    }

    private List<ContestedUploadedDocumentData> filterDocumentsByType(
        List<ContestedUploadedDocumentData> uploadedDocuments, Predicate<String> p) {
        return uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null)
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null)
            .filter(d -> p.test(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
    }

    private List<ContestedUploadedDocumentData> filterDocumentsByParty(
        List<ContestedUploadedDocumentData> uploadedDocuments, String party) {
        return uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null)
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null)
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .collect(Collectors.toList());
    }

    private void addFilteredDocumentsToCaseData(List<ContestedUploadedDocumentData> filteredDocuments,
                                                List<ContestedUploadedDocumentData> uploadedDocuments,
                                                Map<String, Object> caseData,
                                                String collection) {
        List<ContestedUploadedDocumentData> documentCollection = getDocumentCollection(caseData, collection);
        documentCollection.addAll(filteredDocuments);
        log.info("Adding items: {}, to {}", filteredDocuments, collection);

        if (!documentCollection.isEmpty()) {
            caseData.put(collection, documentCollection);
        }

        uploadedDocuments.removeAll(filteredDocuments);
    }
}
