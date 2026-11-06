package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixHearingTimeTaskTest {

    private static final String LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
        + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.";

    private final FixHearingTimeTask task = new FixHearingTimeTask(null, null, null, null);

    @Test
    void shouldFixLongHearingTimeAndTab() {
        UUID id = UUID.randomUUID();
        FinremCaseDetails details = caseDetails(List.of(hearing(id, LONG_TEXT)), List.of(tab(id, LONG_TEXT)));

        task.executeTask(details);

        ManageHearingsWrapper wrapper = details.getData().getManageHearingsWrapper();
        assertThat(wrapper.getHearings().getFirst().getValue().getHearingTime()).isEqualTo("10:00 AM");
        assertThat(wrapper.getHearingTabItems().getFirst().getValue().getTabDateTime()).isEqualTo("02 Feb 2025 10:00 AM");
    }

    @Test
    void shouldNotChangeValidHearingTime() {
        UUID id = UUID.randomUUID();
        FinremCaseDetails details = caseDetails(List.of(hearing(id, "2:30 PM")), List.of(tab(id, "02 Feb 2025 2:30 PM")));

        task.executeTask(details);

        ManageHearingsWrapper wrapper = details.getData().getManageHearingsWrapper();
        assertThat(wrapper.getHearings().getFirst().getValue().getHearingTime()).isEqualTo("2:30 PM");
        assertThat(wrapper.getHearingTabItems().getFirst().getValue().getTabDateTime()).isEqualTo("02 Feb 2025 2:30 PM");
    }

    @Test
    void shouldThrowWhenMoreThanOneInvalidHearing() {
        FinremCaseDetails details = caseDetails(
            List.of(hearing(UUID.randomUUID(), LONG_TEXT), hearing(UUID.randomUUID(), LONG_TEXT)), List.of());

        assertThatThrownBy(() -> task.executeTask(details))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("More than one hearing with invalid time found");
    }

    private FinremCaseDetails caseDetails(List<ManageHearingsCollectionItem> hearings, List<HearingTabCollectionItem> tabs) {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder().hearings(hearings).hearingTabItems(tabs).build();
        return FinremCaseDetails.builder().id(123L)
            .data(FinremCaseData.builder().manageHearingsWrapper(wrapper).build()).build();
    }

    private ManageHearingsCollectionItem hearing(UUID id, String time) {
        return ManageHearingsCollectionItem.builder().id(id)
            .value(Hearing.builder().hearingDate(LocalDate.of(2025, 2, 2)).hearingTime(time).build()).build();
    }

    private HearingTabCollectionItem tab(UUID id, String tabDateTime) {
        return HearingTabCollectionItem.builder().id(id)
            .value(HearingTabItem.builder().tabDateTime(tabDateTime).build()).build();
    }
}
