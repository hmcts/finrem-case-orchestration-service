package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CitizenUploadDocumentTest {

    @Test
    void shouldBuildObjectUsingBuilder() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        CaseDocument doc = new CaseDocument();
        doc.setDocumentUrl("http://example.com");

        CitizenUploadDocument result = CitizenUploadDocument.builder()
            .documentType(CitizenUploadDocumentType.ATTACHMENTS_TO_FORM_E)
            .isFdr(YesOrNo.YES)
            .documentEmailContent("email")
            .documentLink(doc)
            .documentDateAdded(date)
            .documentComment("comment")
            .documentFileName("file.pdf")
            .generalDocumentUploadDateTime(dateTime)
            .build();

        assertThat(result.getDocumentType()).isEqualTo(CitizenUploadDocumentType.ATTACHMENTS_TO_FORM_E);
        assertThat(result.getIsFdr()).isEqualTo(YesOrNo.YES);
        assertThat(result.getDocumentEmailContent()).isEqualTo("email");
        assertThat(result.getDocumentLink()).isEqualTo(doc);
        assertThat(result.getDocumentDateAdded()).isEqualTo(date);
        assertThat(result.getDocumentComment()).isEqualTo("comment");
        assertThat(result.getDocumentFileName()).isEqualTo("file.pdf");
        assertThat(result.getGeneralDocumentUploadDateTime()).isEqualTo(dateTime);
    }

    @Test
    void shouldSetValuesUsingSetters() {
        CitizenUploadDocument doc = new CitizenUploadDocument();

        doc.setDocumentType(CitizenUploadDocumentType.ATTACHMENTS_TO_FORM_E);
        doc.setIsFdr(YesOrNo.NO);
        doc.setDocumentEmailContent("content");
        doc.setDocumentComment("comment");
        doc.setDocumentFileName("file.txt");
        doc.setDocumentDateAdded(LocalDate.of(2023, 5, 10));

        assertThat(doc.getDocumentType()).isEqualTo(CitizenUploadDocumentType.ATTACHMENTS_TO_FORM_E);
        assertThat(doc.getIsFdr()).isEqualTo(YesOrNo.NO);
        assertThat(doc.getDocumentEmailContent()).isEqualTo("content");
        assertThat(doc.getDocumentComment()).isEqualTo("comment");
        assertThat(doc.getDocumentFileName()).isEqualTo("file.txt");
        assertThat(doc.getDocumentDateAdded()).isEqualTo(LocalDate.of(2023, 5, 10));
    }

}
