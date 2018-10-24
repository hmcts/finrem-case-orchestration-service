package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConsentOrderTest {
    protected ConsentOrder order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/consent-order.json").toURI()), ConsentOrder.class);
    }

    @Test
    public void shouldCreateConsentOrderFromJson() {
        assertThat(order.getDocumentType(), is("pdf"));
        assertThat(order.getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(order.getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getDocumentEmailContent(), is("email-content"));
        assertThat(order.getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(order.getDocumentComment(), is("doc-comment"));
        assertThat(order.getDocumentFileName(), is("file1"));
    }
}