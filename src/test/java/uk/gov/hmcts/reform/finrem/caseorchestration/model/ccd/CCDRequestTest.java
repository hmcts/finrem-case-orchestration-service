package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CCDRequestTest extends CaseDetailsTest {
    private CCDRequest ccdRequest;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ccdRequest = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/ccd-request.json").toURI()), CCDRequest.class);
        caseDetails = ccdRequest.getCaseDetails();
        data = caseDetails.getCaseData();
    }


    @Test
    public void shouldCreateCCDRequestFromJson() {
        assertThat(ccdRequest.getToken(), is("test_token"));
        assertThat(ccdRequest.getEventId(), is("event1"));
    }
}