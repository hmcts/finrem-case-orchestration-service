package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.CREATED_ON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.MIME_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class DocumentTest {

    @Test
    public void properties() {
        Document doc = document();

        assertThat(doc.getUrl(), is(URL));
        assertThat(doc.getBinaryUrl(), is(BINARY_URL));
        assertThat(doc.getFileName(), is(FILE_NAME));
        assertThat(doc.getCreatedOn(), is(CREATED_ON));
        assertThat(doc.getMimeType(), is(MIME_TYPE));
    }

    @Test
    public void equality() {
        Document doc = document();
        Document doc1 = document();

        assertThat(doc, is(equalTo(doc1)));
    }
}