package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AmendedConsentOrderTest {
    protected AmendedConsentOrder order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/amended-consent-order.json").toURI()), AmendedConsentOrder.class);
    }

    @Test
    public void shouldCreateAmendedConsentOrderFromJson() {
        assertThat(order.getAmendedConsentOrder().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getAmendedConsentOrder().getDocumentFilename(), is("doc1"));
        assertThat(order.getAmendedConsentOrder().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getAmendedConsentOrderDate(), is(Date.valueOf("2010-01-02")));
    }
}