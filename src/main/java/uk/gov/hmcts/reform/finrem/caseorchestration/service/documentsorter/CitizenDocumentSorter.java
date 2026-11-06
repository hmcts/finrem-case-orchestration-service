package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CitizenPartyType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CERTIFICATE_OF_SERVICE_FORM_FP6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_APPLICATION_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_CONDITIONAL_ORDER_DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.ESTIMATE_OF_COSTS_INCURRED_FORM_H;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT;

@Slf4j
@Service
public class CitizenDocumentSorter {

    private static final Set<CitizenUploadDocumentType> DIVORCE_DOCUMENT_TYPES = EnumSet.of(
        DIVORCE_APPLICATION_PETITION,
        DIVORCE_CONDITIONAL_ORDER_DECREE_NISI,
        DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE
    );

    private static final Set<CitizenUploadDocumentType> HEARING_BUNDLE_DOCUMENT_TYPES = EnumSet.of(
        HEARING_BUNDLE,
        FDR_BUNDLE
    );

    private static final Set<CitizenUploadDocumentType> NON_FDR_DOCUMENT_TYPES = EnumSet.of(
        REPLY_TO_QUESTIONNAIRE,
        REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES,
        REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS,
        REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS,
        CERTIFICATE_OF_SERVICE_FORM_FP6,
        ESTIMATE_OF_COSTS_INCURRED_FORM_H
    );

    /**
     * Sorts applicant or respondent uploads into the corresponding destination collections.
     *
     * <p>The {@code citizenApplicantDocument} and {@code citizenRespondentDocument}
     * fields are temporary transfer collections used between COS and CUI. Their
     * contents are moved into the relevant applicant or respondent document
     * collections and then cleared as part of the sorting process.</p>
     *
     * @param citizenDocumentWrapper wrapper containing the temporary uploads and destination collections
     * @param party                  the party whose documents should be sorted
     */
    public void sortDocuments(CitizenDocumentWrapper citizenDocumentWrapper, CitizenPartyType party) {
        if (CitizenPartyType.APPLICANT.equals(party)) {
            sortPartyDocuments(
                citizenDocumentWrapper.getUploadedCuiApplicantDocuments(),
                citizenDocumentWrapper.ensureApplicantDocCollection(),
                citizenDocumentWrapper.ensureFdrApplicantDocCollection(),
                citizenDocumentWrapper.ensureDivorceApplicantDocCollection(),
                citizenDocumentWrapper.ensureHearingApplicantDocCollection()
            );
        } else {
            sortPartyDocuments(
                citizenDocumentWrapper.getUploadedCuiRespondentDocuments(),
                citizenDocumentWrapper.ensureRespondentDocCollection(),
                citizenDocumentWrapper.ensureFdrRespondentDocCollection(),
                citizenDocumentWrapper.ensureDivorceRespondentDocCollection(),
                citizenDocumentWrapper.ensureHearingRespondentDocCollection()
            );
        }
    }

    private void sortPartyDocuments(
        List<CitizenDocumentCollectionItem> citizenUploads,
        List<CitizenDocumentCollectionItem> partyDocumentsCollection,
        List<CitizenDocumentCollectionItem> fdrDocumentsDocumentsCollection,
        List<CitizenDocumentCollectionItem> divorceSolutionDocumentsCollection,
        List<CitizenDocumentCollectionItem> hearingBundleDocCollectionCollection
    ) {
        for (var documents = citizenUploads.iterator(); documents.hasNext(); ) {
            var documentItem = documents.next();
            var documentType = documentItem.getValue().getDocumentType();
            var documentMarkedFdr = documentItem.getValue().getIsFdr();

            if (DIVORCE_DOCUMENT_TYPES.contains(documentType)) {
                divorceSolutionDocumentsCollection.add(documentItem);
            } else if (HEARING_BUNDLE_DOCUMENT_TYPES.contains(documentType)) {
                hearingBundleDocCollectionCollection.add(documentItem);
            } else if ((YesOrNo.isYes(documentMarkedFdr)
                && !NON_FDR_DOCUMENT_TYPES.contains(documentType))
                || WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT.equals(documentType)) {
                fdrDocumentsDocumentsCollection.add(documentItem);
            } else {
                partyDocumentsCollection.add(documentItem);
            }
            documents.remove();
        }
    }
}
