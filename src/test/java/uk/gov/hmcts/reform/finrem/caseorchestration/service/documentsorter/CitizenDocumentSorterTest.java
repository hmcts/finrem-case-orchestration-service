package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.BANK_STATEMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.CERTIFICATE_OF_SERVICE_FORM_FP6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.DIVORCE_APPLICATION_PETITION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.ESTIMATE_OF_COSTS_INCURRED_FORM_H;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.FDR_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.HEARING_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocumentType.REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS;
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
        CitizenDocumentCollectionItem fdrMarkedDocument = document(BANK_STATEMENTS, YesOrNo.YES);
        CitizenDocumentCollectionItem otherDocument = document(BANK_STATEMENTS);

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setUploadedCuiApplicantDocuments(new ArrayList<>(List.of(
            withoutPrejudice,
            divorceDocument,
            hearingBundle,
            fdrBundle,
            fdrMarkedDocument,
            otherDocument
        )));

        sorter.sortDocuments(wrapper, APPLICANT);

        assertThat(wrapper.getUploadedCuiApplicantDocuments()).isEmpty();
        assertThat(wrapper.getFdrApplicantDocCollection()).containsExactly(withoutPrejudice, fdrMarkedDocument);
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
        CitizenDocumentCollectionItem fdrMarkedDocument = document(BANK_STATEMENTS, YesOrNo.YES);
        CitizenDocumentCollectionItem otherDocument = document(BANK_STATEMENTS);

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setUploadedCuiRespondentDocuments(new ArrayList<>(List.of(
            withoutPrejudice,
            divorceDocument,
            hearingBundle,
            fdrBundle,
            fdrMarkedDocument,
            otherDocument
        )));

        sorter.sortDocuments(wrapper, RESPONDENT);

        assertThat(wrapper.getUploadedCuiRespondentDocuments()).isEmpty();
        assertThat(wrapper.getFdrRespondentDocCollection()).containsExactly(withoutPrejudice, fdrMarkedDocument);
        assertThat(wrapper.getDivorceRespondentDocCollection()).containsExactly(divorceDocument);
        assertThat(wrapper.getHearingRespondentDocCollection()).containsExactly(hearingBundle, fdrBundle);
        assertThat(wrapper.getRespondentDocCollection()).containsExactly(otherDocument);
    }

    @Test
    void shouldKeepApplicantDocumentsWithNoFdrCollectionInPartyCollectionWhenMarkedFdr() {
        List<CitizenDocumentCollectionItem> noFdrCollectionDocuments = noFdrCollectionDocuments();

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setUploadedCuiApplicantDocuments(new ArrayList<>(noFdrCollectionDocuments));

        sorter.sortDocuments(wrapper, APPLICANT);

        assertThat(wrapper.getUploadedCuiApplicantDocuments()).isEmpty();
        assertThat(wrapper.getApplicantDocCollection()).containsExactlyElementsOf(noFdrCollectionDocuments);
        assertThat(wrapper.getFdrApplicantDocCollection()).isEmpty();
    }

    @Test
    void shouldKeepRespondentDocumentsWithNoFdrCollectionChangeInPartyCollectionWhenMarkedFdr() {
        List<CitizenDocumentCollectionItem> noFdrCollectionDocuments = noFdrCollectionDocuments();

        CitizenDocumentWrapper wrapper = new CitizenDocumentWrapper();
        wrapper.setUploadedCuiRespondentDocuments(new ArrayList<>(noFdrCollectionDocuments));

        sorter.sortDocuments(wrapper, RESPONDENT);

        assertThat(wrapper.getUploadedCuiRespondentDocuments()).isEmpty();
        assertThat(wrapper.getRespondentDocCollection()).containsExactlyElementsOf(noFdrCollectionDocuments);
        assertThat(wrapper.getFdrRespondentDocCollection()).isEmpty();
    }

    private List<CitizenDocumentCollectionItem> noFdrCollectionDocuments() {
        return List.of(
            document(REPLY_TO_QUESTIONNAIRE, YesOrNo.YES),
            document(REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES, YesOrNo.YES),
            document(REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS, YesOrNo.YES),
            document(REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS, YesOrNo.YES),
            document(CERTIFICATE_OF_SERVICE_FORM_FP6, YesOrNo.YES),
            document(ESTIMATE_OF_COSTS_INCURRED_FORM_H, YesOrNo.YES)
        );
    }

    private CitizenDocumentCollectionItem document(CitizenUploadDocumentType documentType) {
        return document(documentType, null);
    }

    private CitizenDocumentCollectionItem document(CitizenUploadDocumentType documentType, YesOrNo isFdr) {
        return CitizenDocumentCollectionItem.builder()
            .value(CitizenUploadDocument.builder()
                .documentType(documentType)
                .isFdr(isFdr)
                .build())
            .build();
    }
}
