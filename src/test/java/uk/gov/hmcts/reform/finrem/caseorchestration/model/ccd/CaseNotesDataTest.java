package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class CaseNotesDataTest extends  CaseNotesTest {
    private CaseNotesData caseNotesData;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        caseNotesData = mapper.readValue(new File(getClass()
                        .getResource("/fixtures/model/case-notes-data.json").toURI()),
                CaseNotesData.class);
        caseNotes = caseNotesData.getCaseNotes();
    }

    @Test
    public void shouldCreateCaseNotesDataFromJson() {
        assertThat(caseNotesData.getId(), is("1"));
        assertThat(caseNotesData.getCaseNotes(), is(notNullValue()));
    }

}