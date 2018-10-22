package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GeneralOrderDataTest {
    GeneralOrderData order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/general-order-data.json").toURI()), GeneralOrderData.class);
    }

    @Test
    public void shouldCreateGeneralOrderDataFromJson() {
        assertThat(order.getId(), is("1"));
        assertThat(order.getGeneralOrder().getGeneralOrder(), is("order1"));
        assertThat(order.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentFilename(), is("doc1"));
        assertThat(order.getGeneralOrder().getGeneralOrderDocumentUpload().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getGeneralOrder().getGeneralOrderJudgeType(), is("district judge"));
        assertThat(order.getGeneralOrder().getGeneralOrderJudgeName(), is("judge1"));
        assertThat(order.getGeneralOrder().getGeneralOrderDate(), is(Date.valueOf("2010-01-02")));
        assertThat(order.getGeneralOrder().getGeneralOrderComments(), is("comment1"));
    }

}