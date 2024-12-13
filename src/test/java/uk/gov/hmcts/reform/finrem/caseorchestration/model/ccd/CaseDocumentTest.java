package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CaseDocumentTest {
    CaseDocument document;

    @BeforeEach
    public void setUp() throws Exception {
        String json = "{"
            + " \"document_url\" : \"http://doc1\", "
            + " \"document_filename\" : \"doc1\", "
            + " \"document_binary_url\" : \"http//doc1.binary\" "
            + "} ";
        ObjectMapper mapper = new ObjectMapper();
        document = mapper.readValue(json, CaseDocument.class);
    }

    @Test
    void shouldCreateCaseDocumentFromJson() {
        assertThat(document.getDocumentUrl(), is("http://doc1"));
        assertThat(document.getDocumentFilename(), is("doc1"));
        assertThat(document.getDocumentBinaryUrl(), is("http//doc1.binary"));
    }

    @Test
    void shouldCreateCaseDocumentFromDocument() {
        uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document document =
            uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document.builder()
            .binaryUrl("http//doc1.binary")
            .fileName("doc1")
            .url("http://doc1")
            .build();

        CaseDocument caseDocument = CaseDocument.from(document);
        assertThat(caseDocument.getDocumentUrl(), is("http://doc1"));
        assertThat(caseDocument.getDocumentFilename(), is("doc1"));
        assertThat(caseDocument.getDocumentBinaryUrl(), is("http//doc1.binary"));
    }
}
