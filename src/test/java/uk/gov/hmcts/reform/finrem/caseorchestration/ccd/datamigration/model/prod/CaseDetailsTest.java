package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CaseDetailsTest  extends CaseDataTest {
    protected CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        caseDetails = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/prod/case-details.json").toURI()), CaseDetails.class);
        data = caseDetails.getCaseData();
    }


    @Test
    public void shouldCreateCaseDetailsFromJson() {
        assertCaseDetails();
    }

    protected void assertCaseDetails() {
        assertThat(caseDetails.getCaseId(), is("12345678"));
        assertThat(caseDetails.getJurisdiction(), is("divorce"));
        assertThat(caseDetails.getState(), is("created"));
    }
}