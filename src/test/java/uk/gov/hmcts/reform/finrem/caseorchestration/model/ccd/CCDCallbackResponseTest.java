package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class CCDCallbackResponseTest extends CaseDataTest {
    protected CCDCallbackResponse ccdResponse;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ccdResponse = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/ccd-response.json").toURI()), CCDCallbackResponse.class);
        data = ccdResponse.getData();
    }


    @Test
    public void shouldCreateCCDRequestFromJson() {
        assertThat(ccdResponse.getErrors(), hasItems("error1", "error2"));
        assertThat(ccdResponse.getWarnings(), hasItems("warning1", "warning2"));
    }
}