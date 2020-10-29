package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.File;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class OrderRefusalTest extends BaseServiceTest {

    @Test
    public void shouldCreateOrderRefusalFromJson() throws Exception {
        OrderRefusal order = mapper.readValue(new File(getClass()
            .getResource("/fixtures/model/order-refusal.json").toURI()), OrderRefusal.class);

        assertThat(order.getOrderRefusalAfterText(), is("afterText"));
        assertThat(order.getOrderRefusal(), hasItems("Other"));
        assertThat(order.getOrderRefusalDate(), is(LocalDate.of(2003, 2, 1)));
        assertThat(order.getOrderRefusalDocs().getDocumentUrl(), is("http://doc1"));
        assertThat(order.getOrderRefusalDocs().getDocumentFilename(), is("doc1"));
        assertThat(order.getOrderRefusalDocs().getDocumentBinaryUrl(), is("http://doc1.binary"));
        assertThat(order.getOrderRefusalJudge(), is("District Judge"));
        assertThat(order.getOrderRefusalOther(), is("test1"));
        assertThat(order.getOrderRefusalJudgeName(), is("test3"));
        assertThat(order.getOrderRefusalAddComments(), is("comment1"));
    }
}
