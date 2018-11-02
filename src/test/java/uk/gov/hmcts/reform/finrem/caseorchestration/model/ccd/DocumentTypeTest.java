package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DocumentTypeTest {
    protected DocumentType doc;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        doc = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/document-type.json").toURI()), DocumentType.class);
    }

    @Test
    public void shouldCreateDocumentTypeFromJson() {
        assertThat(doc.getTypeOfDocument(), is("pdf"));
        assertThat(doc.getUploadedDocument().getDocumentUrl(), is("http://doc1"));
        assertThat(doc.getUploadedDocument().getDocumentFilename(), is("doc1"));
        assertThat(doc.getUploadedDocument().getDocumentBinaryUrl(), is("http://doc1.binary"));
    }
}