package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FinremAdditionalHearingCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    @BeforeEach
    public void setUpTest() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        applicantAndRespondentMultiLetterCorresponder =
            new FinremAdditionalHearingCorresponder(bulkPrintService, notificationService, documentHelper);
        caseDetails = TestSetUpUtils.finremCaseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
    }

    @Test
    void shouldGetDocumentsToPrint() {
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDocumentsToPrintIfGetAdditionalHearingDocumentDateIsNull(boolean twoNulls) {
        List<AdditionalHearingDocumentCollection> additionalHearingDocumentCollections = new ArrayList<>();
        AdditionalHearingDocumentCollection document1 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("1").build())
                .additionalHearingDocumentDate(null).build())
            .build();
        AdditionalHearingDocumentCollection document2 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("2").build())
                .additionalHearingDocumentDate(twoNulls ? null : LocalDateTime.now().minusDays(9)).build())
            .build();
        AdditionalHearingDocumentCollection document3 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("latest").build())
                .additionalHearingDocumentDate(LocalDateTime.now()).build())
            .build();
        AdditionalHearingDocumentCollection document4 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("4").build())
                .additionalHearingDocumentDate(LocalDateTime.now().minusDays(10)).build())
            .build();
        additionalHearingDocumentCollections.add(document1);
        additionalHearingDocumentCollections.add(document2);
        additionalHearingDocumentCollections.add(document3);
        additionalHearingDocumentCollections.add(document4);
        caseDetails.getData().setAdditionalHearingDocuments(additionalHearingDocumentCollections);
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
        assertThat(documentsToPrint).contains(CaseDocument.builder().documentFilename("latest").build());
    }

    @Test
    void shouldGetDocumentsToPrintIfGetAdditionalHearingDocumentDateIsNotNull() {
        List<AdditionalHearingDocumentCollection> additionalHearingDocumentCollections = new ArrayList<>();
        AdditionalHearingDocumentCollection document1 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("1").build())
                .additionalHearingDocumentDate(LocalDateTime.now().minusDays(8)).build())
            .build();
        AdditionalHearingDocumentCollection document2 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("2").build())
                .additionalHearingDocumentDate(LocalDateTime.now().minusDays(9)).build())
            .build();
        AdditionalHearingDocumentCollection document3 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("latest").build())
                .additionalHearingDocumentDate(LocalDateTime.now()).build())
            .build();
        AdditionalHearingDocumentCollection document4 = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(CaseDocument.builder().documentFilename("4").build())
                .additionalHearingDocumentDate(LocalDateTime.now().minusDays(10)).build())
            .build();
        additionalHearingDocumentCollections.add(document1);
        additionalHearingDocumentCollections.add(document2);
        additionalHearingDocumentCollections.add(document3);
        additionalHearingDocumentCollections.add(document4);
        caseDetails.getData().setAdditionalHearingDocuments(additionalHearingDocumentCollections);
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(2, documentsToPrint.size());
        assertThat(documentsToPrint).contains(CaseDocument.builder().documentFilename("latest").build());
    }

}
