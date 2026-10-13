package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.BANK_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_APPLICATION_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CitizenPartyType.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CitizenPartyType.RESPONDENT;

class CitizenDocumentSorterTest {

    private CitizenDocumentSorter sorter;

    @BeforeEach
    void setUp() {
        sorter = new CitizenDocumentSorter();
    }

    @Test
    void shouldSortApplicantDocumentsIntoTargetCollectionsAndClearSourceUploads() {
        CitizenDocumentCollectionItem withoutPrejudice = document(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT);
        CitizenDocumentCollectionItem divorceDocument = document(DIVORCE_APPLICATION_PETITION);
        CitizenDocumentCollectionItem hearingBundle = document(HEARING_BUNDLE);
        CitizenDocumentCollectionItem fdrBundle = document(FDR_BUNDLE);
        CitizenDocumentCollectionItem otherDocument = document(BANK_STATEMENTS);

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setCitizenApplicantDocument(new ArrayList<>(List.of(
            withoutPrejudice,
            divorceDocument,
            hearingBundle,
            fdrBundle,
            otherDocument
        )));

        sorter.sortDocuments(wrapper, APPLICANT);

        assertThat(wrapper.getCitizenApplicantDocument()).isEmpty();
        assertThat(wrapper.getFdrApplicantDocCollection()).containsExactly(withoutPrejudice);
        assertThat(wrapper.getDivorceApplicantDocCollection()).containsExactly(divorceDocument);
        assertThat(wrapper.getHearingApplicantDocCollection()).containsExactly(hearingBundle, fdrBundle);
        assertThat(wrapper.getApplicantDocCollection()).containsExactly(otherDocument);
    }

    @Test
    void shouldSortRespondentDocumentsIntoTargetCollectionsAndClearSourceUploads() {
        CitizenDocumentCollectionItem withoutPrejudice = document(WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT);
        CitizenDocumentCollectionItem divorceDocument = document(DIVORCE_APPLICATION_PETITION);
        CitizenDocumentCollectionItem hearingBundle = document(HEARING_BUNDLE);
        CitizenDocumentCollectionItem fdrBundle = document(FDR_BUNDLE);
        CitizenDocumentCollectionItem otherDocument = document(BANK_STATEMENTS);

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setCitizenRespondentDocument(new ArrayList<>(List.of(
            withoutPrejudice,
            divorceDocument,
            hearingBundle,
            fdrBundle,
            otherDocument
        )));

        sorter.sortDocuments(wrapper, RESPONDENT);

        assertThat(wrapper.getCitizenRespondentDocument()).isEmpty();
        assertThat(wrapper.getFdrRespondentDocCollection()).containsExactly(withoutPrejudice);
        assertThat(wrapper.getDivorceRespondentDocCollection()).containsExactly(divorceDocument);
        assertThat(wrapper.getHearingRespondentDocCollection()).containsExactly(hearingBundle, fdrBundle);
        assertThat(wrapper.getRespondentDocCollection()).containsExactly(otherDocument);
    }

    private CitizenDocumentCollectionItem document(CitizenUploadDocumentType documentType) {
        return CitizenDocumentCollectionItem.builder()
            .value(CitizenUploadDocument.builder()
                .documentType(documentType)
                .build())
            .build();
    }
}
