package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing.mapHearingToWorkingHearing;

@Service
@RequiredArgsConstructor
public class ManageHearingsEditHearingService {

    public void setWorkingHearingFromWorkingHearingId(FinremCaseData caseData) {
        ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();

        UUID workingHearingId = manageHearingsWrapper
            .getWorkingHearingId();

        Hearing hearingToBeWorkedOn = manageHearingsWrapper
            .getHearings().stream().filter(h -> h.getId().equals(workingHearingId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No hearing found with ID: " + workingHearingId))
            .getValue();

        manageHearingsWrapper.setWorkingHearing(mapHearingToWorkingHearing(
            hearingToBeWorkedOn, List.of(HearingType.values())));
    }
}
