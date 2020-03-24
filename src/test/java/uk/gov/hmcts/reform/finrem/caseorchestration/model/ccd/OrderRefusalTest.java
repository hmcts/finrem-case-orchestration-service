package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class OrderRefusalTest {
    protected OrderRefusal order;

    @Before
    public void setUp() throws Exception {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        ObjectMapper mapper = new ObjectMapper();
        order = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/order-refusal.json").toURI()), OrderRefusal.class);
    }

    @Test
    public void shouldCreateOrderRefusalFromJson() {
        assertThat(order.getOrderRefusalAfterText(), is("afterText"));
        assertThat(order.getOrderRefusal(), hasItems("Other"));
        assertThat(order.getOrderRefusalDate(), is(parseDate("2003-02-01")));
        assertThat(order.getOrderRefusalDocs().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getOrderRefusalDocs().getDocumentFilename(), is("doc1"));
        assertThat(order.getOrderRefusalDocs().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getOrderRefusalJudge(), is("District Judge"));
        assertThat(order.getOrderRefusalOther(), is("test1"));
        assertThat(order.getOrderRefusalJudgeName(), is("test3"));
        assertThat(order.getOrderRefusalAddComments(), is("comment1"));
    }

    private static Date parseDate(String stringDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            return dateFormat.parse(stringDate);
        } catch (ParseException exeption) {
            throw new IllegalArgumentException("Invalid date or format of date!", exeption);
        }
    }
}
