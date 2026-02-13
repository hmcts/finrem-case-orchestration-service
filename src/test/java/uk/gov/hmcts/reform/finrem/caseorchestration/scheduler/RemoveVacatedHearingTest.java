package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.VacatedOrAdjournedHearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveVacatedHearingTest {

    @InjectMocks
    private RemoveVacatedHearing task;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Mock
    private FinremCaseData caseData;

    @Mock
    private ManageHearingsWrapper manageHearingsWrapper;

    @Test
    void shouldRemoveVacatedHearing() {
        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(finremCaseDetails.getId()).thenReturn(172930131L);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings = new ArrayList<>();
        vacatedHearings.add(VacatedOrAdjournedHearingsCollectionItem.builder().build());

        List<VacatedOrAdjournedHearingTabCollectionItem> tabItems = new ArrayList<>();
        tabItems.add(VacatedOrAdjournedHearingTabCollectionItem.builder().build());

        when(manageHearingsWrapper.getVacatedOrAdjournedHearings()).thenReturn(vacatedHearings);
        when(manageHearingsWrapper.getVacatedOrAdjournedHearingTabItems()).thenReturn(tabItems);

        task.executeTask(finremCaseDetails);

        assertTrue(vacatedHearings.isEmpty());
        assertTrue(tabItems.isEmpty());
    }
}
