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
    void givenMultipleNotes_whenExecuteTask_thenRemovesLastOnly() {
        CaseNotesCollection first = noteItem("first", "Author A", LocalDate.of(2026, 2, 1));
        CaseNotesCollection second = noteItem("second", "Author B", LocalDate.of(2026, 3, 1));
        CaseNotesCollection third = noteItem("third", "Author C", LocalDate.of(2026, 4, 1));

        FinremCaseDetails details = caseWithNotes(first, second, third);

        task.executeTask(details);

        assertThat(details.getData().getCaseNotesCollection()).containsExactly(first, second);
    }

    @Test
    void givenSingleNote_whenExecuteTask_thenLeavesEmptyList() {
        CaseNotesCollection only = noteItem("only", "Author", LocalDate.of(2026, 2, 1));

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

    private CaseNotesCollection noteItem(String text, String author, LocalDate date) {
        return CaseNotesCollection.builder()
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