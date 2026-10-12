package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_APPLICATION_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_CONDITIONAL_ORDER_DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser.Party.APPLICANT;

/**
 * Sorts uploaded citizen documents into their target CCD collections.
 */
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

    /**
     * Sorts applicant or respondent uploads into the corresponding collections.
     *
     * @param citizenDocumentWrapper wrapper containing source uploads and destination collections
     * @param party party whose documents should be sorted
     */
    //TODO: Create seperate list for applicant and respondent FDR/DIVORCE/HEARING tabs
    public void sortDocuments(CitizenDocumentWrapper citizenDocumentWrapper, CUIDocumentsCategoriser.Party party) {
        if (APPLICANT.equals(party)) {
            sortPartyDocuments(
                citizenDocumentWrapper.getCitizenApplicantDocument(),
                citizenDocumentWrapper.getApplicantDocCollection(),
                citizenDocumentWrapper.getFdrDocCollection(),
                citizenDocumentWrapper.getDivorceSolutionDocCollection(),
                citizenDocumentWrapper.getHearingBundleDocCollection()
            );
        } else {
            sortPartyDocuments(
                citizenDocumentWrapper.getCitizenRespondentDocument(),
                citizenDocumentWrapper.getRespondentDocCollection(),
                citizenDocumentWrapper.getFdrDocCollection(),
                citizenDocumentWrapper.getDivorceSolutionDocCollection(),
                citizenDocumentWrapper.getHearingBundleDocCollection()
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

            if (WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT.equals(documentType) || FDR_BUNDLE.equals(documentType)) {
                fdrDocumentsDocumentsCollection.add(documentItem);
            } else if (DIVORCE_DOCUMENT_TYPES.contains(documentType)) {
                divorceSolutionDocumentsCollection.add(documentItem);
            } else if (HEARING_BUNDLE_DOCUMENT_TYPES.contains(documentType)) {
                hearingBundleDocCollectionCollection.add(documentItem);
            } else {
                partyDocumentsCollection.add(documentItem);
            }
            documents.remove();
        }
    }
}
