package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class OrderRefusalDataTest {
    OrderRefusalData order;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/order-refusal-data.json").toURI()), OrderRefusalData.class);
    }

    @Test
    public void shouldCreateOrderRefusalDataFromJSON() {
        assertThat(order.getId(), is("1"));
        assertThat(order.getOrderRefusal().getOrderRefusal(), hasItems("Other"));
        assertThat(order.getOrderRefusal().getOrderRefusalDate(), is(Date.valueOf("2003-02-01")));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentFilename(), is("doc1"));
        assertThat(order.getOrderRefusal().getOrderRefusalDocs().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getOrderRefusal().getOrderRefusalJudge(), is("District Judge"));
        assertThat(order.getOrderRefusal().getOrderRefusalOther(), is("test1"));
        assertThat(order.getOrderRefusal().getOtherHearingDetails(), is("test2"));
        assertThat(order.getOrderRefusal().getOrderRefusalJudgeName(), is("test3"));
        assertThat(order.getOrderRefusal().getOrderRefusalNotEnough(), hasItems("reason1"));
        assertThat(order.getOrderRefusal().getEstimateLengthOfHearing(), is("10"));
        assertThat(order.getOrderRefusal().getOrderRefusalAddComments(), is("comment1"));
        assertThat(order.getOrderRefusal().getWhenShouldHearingTakePlace(), is("today"));
        assertThat(order.getOrderRefusal().getWhereShouldHearingTakePlace(), is("EZ801"));

    }

}