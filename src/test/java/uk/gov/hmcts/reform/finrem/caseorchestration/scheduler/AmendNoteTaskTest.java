package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotesCollection;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class AmendNoteTaskTest {

    @InjectMocks
    private AmendNoteTask task;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Mock
    private FinremCaseData caseData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(finremCaseDetails.getData()).thenReturn(caseData);
    }

    @Test
    void removesCaseNoteWithMatchingDate() {
        List<CaseNotesCollection> notes = new ArrayList<>();
        CaseNotesCollection matchingNote = mock(CaseNotesCollection.class);
        CaseNotes noteValue = mock(CaseNotes.class);
        when(matchingNote.getValue()).thenReturn(noteValue);
        when(noteValue.getCaseNoteDate()).thenReturn(LocalDate.of(2026, 2, 2));
        notes.add(matchingNote);
        when(caseData.getCaseNotesCollection()).thenReturn(notes);

        task.executeTask(finremCaseDetails);

        assertTrue(notes.isEmpty());
    }

    @Test
    void doesNothingWhenNoMatchingDate() {
        List<CaseNotesCollection> notes = new ArrayList<>();
        CaseNotesCollection otherNote = mock(CaseNotesCollection.class);
        CaseNotes noteValue = mock(CaseNotes.class);
        when(otherNote.getValue()).thenReturn(noteValue);
        when(noteValue.getCaseNoteDate()).thenReturn(LocalDate.of(2026, 2, 1));
        notes.add(otherNote);
        when(caseData.getCaseNotesCollection()).thenReturn(notes);

        task.executeTask(finremCaseDetails);

        assertEquals(1, notes.size());
    }

    @Test
    void handlesEmptyCaseNotesCollection() {
        when(caseData.getCaseNotesCollection()).thenReturn(new ArrayList<>());
        task.executeTask(finremCaseDetails);
        assertTrue(caseData.getCaseNotesCollection().isEmpty());
    }

    @Test
    void handlesNullCaseNotesCollection() {
        when(caseData.getCaseNotesCollection()).thenReturn(null);
        task.executeTask(finremCaseDetails);
        verify(caseData, times(1)).getCaseNotesCollection();
    }
}
