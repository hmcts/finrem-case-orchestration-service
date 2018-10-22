package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConsentOrderDataTest {
    ConsentOrderData order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/consent-order-data.json").toURI()), ConsentOrderData.class);
    }

    @Test
    public void shouldCreateConsentOrderDataFromJson() {
        assertThat(order.getId(), is("1"));
        assertThat(order.getConsentOrder().getDocumentType(), is("pdf"));
        assertThat(order.getConsentOrder().getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getConsentOrder().getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(order.getConsentOrder().getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getConsentOrder().getDocumentEmailContent(), is("email-content"));
        assertThat(order.getConsentOrder().getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(order.getConsentOrder().getDocumentComment(), is("doc-comment"));
        assertThat(order.getConsentOrder().getDocumentFileName(), is("file1"));
    }
}