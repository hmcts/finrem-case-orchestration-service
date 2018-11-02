package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GeneralOrderTest {
    protected GeneralOrder order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/general-order.json").toURI()), GeneralOrder.class);
    }

    @Test
    public void shouldCreateGeneralOrderFromJson() {
        assertThat(order.getGeneralOrder(), is("order1"));
        assertThat(order.getGeneralOrderDocumentUpload().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getGeneralOrderDocumentUpload().getDocumentFilename(), is("doc1"));
        assertThat(order.getGeneralOrderDocumentUpload().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getGeneralOrderJudgeType(), is("district judge"));
        assertThat(order.getGeneralOrderJudgeName(), is("judge1"));
        assertThat(order.getGeneralOrderDate(), is(Date.valueOf("2010-01-02")));
        assertThat(order.getGeneralOrderComments(), is("comment1"));
    }
}