package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class CCDMigrationCallbackResponseTest extends CaseDataTest {
    protected CCDMigrationCallbackResponse ccdMigrationCallbackResponse;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ccdMigrationCallbackResponse = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/prod/ccd-response.json").toURI()), CCDMigrationCallbackResponse.class);
        data = ccdMigrationCallbackResponse.getData();
    }


    @Test
    public void shouldCreateCCDRequestFromJson() {
        assertThat(ccdMigrationCallbackResponse.getErrors(), hasItems("error1", "error2"));
        assertThat(ccdMigrationCallbackResponse.getWarnings(), hasItems("warning1", "warning2"));
    }
}