package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RespondToOrderTest {
    protected RespondToOrder order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/respond-to-order.json").toURI()), RespondToOrder.class);
    }

    @Test
    public void shouldCreateAmendedConsentOrderFromJson() {
        assertThat(order.getDocumentType(), is("pdf"));
        assertThat(order.getDocumentLink().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getDocumentLink().getDocumentFilename(), is("doc1"));
        assertThat(order.getDocumentLink().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getDocumentDateAdded(), is(Date.valueOf("2010-01-02")));
        assertThat(order.getDocumentFileName(), is("file1"));


    }

}