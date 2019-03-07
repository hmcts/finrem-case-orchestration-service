package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CaseNotesTest {
    protected CaseNotes caseNotes;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        caseNotes = mapper.readValue(new File(getClass()
                .getResource("/fixtures/model/case-notes.json").toURI()), CaseNotes.class);
    }

    @Test
    public void shouldCreateCaseNotesFromJson() {
        assertThat(caseNotes.getCaseNote(), is("note1"));
        assertThat(caseNotes.getCaseNoteAuthor(), is("author1"));
        assertThat(caseNotes.getCaseNoteDate(), is(Date.valueOf("2010-01-02")));
    }

}