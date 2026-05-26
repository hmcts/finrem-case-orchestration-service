package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RemoveCaseNoteTaskTest {

    private static final String NOTE_ID = "bf87157f-6e72-4af6-81ae-8f74df93d0f0";

    @Mock
    private CaseReferenceCsvLoader csvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private RemoveCaseNoteTask task;

    @BeforeEach
    void setUp() {
        task = new RemoveCaseNoteTask(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Test
    void givenTargetNotePresent_whenExecuteTask_thenRemovesOnlyThatNote() {
        CaseNotesCollection keeper1 = noteItem("id-1", "first", "Author A", LocalDate.of(2026, 2, 1));
        CaseNotesCollection target = noteItem(NOTE_ID, "bad note", "Author B", LocalDate.of(2026, 5, 14));
        CaseNotesCollection keeper2 = noteItem("id-3", "third", "Author C", LocalDate.of(2026, 4, 1));

        FinremCaseDetails details = caseWithNotes(keeper1, target, keeper2);

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).containsExactly(keeper1, keeper2);
    }

    @Test
    void givenTargetNoteAbsent_whenExecuteTask_thenLeavesNotesUntouched() {
        CaseNotesCollection a = noteItem("id-1", "first", "Author A", LocalDate.of(2026, 2, 1));
        CaseNotesCollection b = noteItem("id-2", "second", "Author B", LocalDate.of(2026, 3, 1));

        FinremCaseDetails details = caseWithNotes(a, b);

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).containsExactly(a, b);
    }

    @Test
    void givenOnlyTargetNote_whenExecuteTask_thenLeavesEmptyList() {
        CaseNotesCollection only = noteItem(NOTE_ID, "bad note", "Author", LocalDate.of(2026, 5, 14));

        FinremCaseDetails details = caseWithNotes(only);

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).isEmpty();
    }

    @Test
    void givenNullCaseNotesCollection_whenExecuteTask_thenDoesNothing() {
        FinremCaseDetails details = FinremCaseDetails.builder()
            .id(1234567890123456L)
            .data(FinremCaseData.builder().caseNotesCollection(null).build())
            .build();

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).isNull();
    }

    @Test
    void givenEmptyCaseNotesCollection_whenExecuteTask_thenLeavesItEmpty() {
        FinremCaseDetails details = caseWithNotes();

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).isEmpty();
    }

    private CaseNotesCollection noteItem(String id, String text, String author, LocalDate date) {
        return CaseNotesCollection.builder()
            .id(id)
            .value(CaseNotes.builder()
                .caseNote(text)
                .caseNoteAuthor(author)
                .caseNoteDate(date)
                .build())
            .build();
    }

    private FinremCaseDetails caseWithNotes(CaseNotesCollection... notes) {
        List<CaseNotesCollection> list = new ArrayList<>(List.of(notes));
        return FinremCaseDetails.builder()
            .id(1234567890123456L)
            .data(FinremCaseData.builder().caseNotesCollection(list).build())
            .build();
    }
}