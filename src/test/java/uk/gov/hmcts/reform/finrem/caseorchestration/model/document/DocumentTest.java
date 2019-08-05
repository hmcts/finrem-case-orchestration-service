package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DocumentTest {

    public static final String BINARY_URL = "test/binary";
    public static final String FILE_NAME = "doc";
    public static final String URL = "/test";

    @Test
    public void properties() {
        Document doc = document();

        assertThat(doc.getUrl(), is(URL));
        assertThat(doc.getBinaryUrl(), is(BINARY_URL));
        assertThat(doc.getFileName(), is(FILE_NAME));
    }

    @Test
    public void equality() {
        Document doc = document();
        Document doc1 = document();

        assertThat(doc, is(equalTo(doc1)));
    }

    private Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(URL);

        return document;
    }
}