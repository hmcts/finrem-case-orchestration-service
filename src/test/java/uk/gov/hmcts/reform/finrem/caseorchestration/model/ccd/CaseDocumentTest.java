package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CaseDocumentTest {
    CaseDocument document;

    @Before
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
    public void shouldCreateCaseDocumentFromJson() {
        assertThat(document.getDocumentUrl(), is("http://doc1"));
        assertThat(document.getDocumentFilename(), is("doc1"));
        assertThat(document.getDocumentBinaryUrl(), is("http//doc1.binary"));
    }
}