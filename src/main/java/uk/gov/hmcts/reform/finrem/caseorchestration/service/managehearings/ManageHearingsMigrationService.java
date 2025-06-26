package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsMigrationService {

    public void populateListForHearingWrapper(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        if (shouldPopulateListForHearingWrapper(listForHearingWrapper)) {
            // Type of Hearing
            HearingTypeDirection hearingType = listForHearingWrapper.getHearingType();
            // Hearing Date
            LocalDate hearingDate = listForHearingWrapper.getHearingDate();
            // Hearing Time
            String hearingTime = listForHearingWrapper.getHearingTime();
            // Time Estimate
            String timeEstimate = listForHearingWrapper.getTimeEstimate();
            // Additional information about the hearing
            String additionalInformationAboutHearing = listForHearingWrapper.getAdditionalInformationAboutHearing();
            // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
            HearingRegionWrapper hearingRegionWrapper = listForHearingWrapper.getHearingRegionWrapper();

            Hearing newHearing = Hearing.builder()
                .hearingMode(null) // no Hearing attendance field
                .hearingDate(hearingDate)
                .hearingTime(hearingTime)
                .hearingType(hearingType.toHearingType())
                .additionalHearingInformation(additionalInformationAboutHearing)
                .hearingTimeEstimate(timeEstimate)
                .hearingCourtSelection(hearingRegionWrapper.toCourt())
                //.partiesOnCaseMultiSelectList(listForHearingWrapper)
                .build();

            appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(newHearing).build());
        }
    }

    private boolean shouldPopulateListForHearingWrapper(ListForHearingWrapper listForHearingWrapper) {
        return listForHearingWrapper.getHearingType() != null;
    }

    private void appendToHearings(FinremCaseData caseData, ManageHearingsCollectionItem newManageHearingsCollectionItem) {
        ManageHearingsWrapper manageHearingsWrapper = caseData.getManageHearingsWrapper();
        if (manageHearingsWrapper.getHearings() == null) {
            manageHearingsWrapper.setHearings(new ArrayList<>());
        }
        manageHearingsWrapper.getHearings().add(newManageHearingsCollectionItem);
    }

}
