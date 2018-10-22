package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DocumentDataTest {
    DocumentData doc;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        doc = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/document-data.json").toURI()), DocumentData.class);
    }

    @Test
    public void shouldCreateDocumentDataFromJson() {
        assertThat(doc.getId(), is("1"));
        assertThat(doc.getDocumentType().getTypeOfDocument(), is("pdf"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentUrl(), is("http://doc1"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentFilename(), is("doc1"));
        assertThat(doc.getDocumentType().getUploadedDocument().getDocumentBinaryUrl(), is("http://doc1.binary"));
    }
}