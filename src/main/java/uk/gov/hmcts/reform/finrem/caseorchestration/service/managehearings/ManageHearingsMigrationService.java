package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.addItemToList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsMigrationService {

    private final HearingTabDataMapper hearingTabDataMapper;

    /**
     * Marks the case data as migrated to a specified Manage Hearings migration version.
     *
     * @param caseData           the case data to update
     * @param mhMigrationVersion the version string representing the applied migration
     */
    public void markCaseDataMigrated(FinremCaseData caseData, String mhMigrationVersion) {
        caseData.getMhMigrationWrapper().setMhMigrationVersion(mhMigrationVersion);
    }

    /**
     * Populates a new {@link HearingTabItem} in the hearing tab of the case if the hearing
     * details have not already been migrated.
     *
     * <p>
     * This method checks if the {@code ListForHearingWrapper} needs to be processed by evaluating
     * both the {@code MhMigrationWrapper} and the current state of the wrapper itself.
     * If migration is needed, it extracts hearing-related fields such as type, date, time,
     * time estimate, court information, and additional information.
     * These are used to build a new {@code HearingTabItem}, which is then added to the case data.
     * Once added, the migration flag is set to {@code YES}.
     *
     * @param caseData the {@link FinremCaseData} containing the hearing details and wrappers
     */
    public void populateListForHearingWrapper(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();

        if (!shouldPopulateListForHearingWrapper(mhMigrationWrapper, listForHearingWrapper)) {
            log.warn("{} - List for Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }

        appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(toHearingTabItem(listForHearingWrapper)).build());
        appendToHearings(caseData, ManageHearingsCollectionItem.builder().value(toHearing(listForHearingWrapper)).build());

        caseData.getMhMigrationWrapper().setIsListForHearingsMigrated(YesOrNo.YES);
    }

    /**
     * Determines whether the case has already undergone Manage Hearings migration.
     *
     * @param caseData the case data to evaluate
     * @return {@code true} if migration has been marked; {@code false} otherwise
     */
    public boolean wasMigrated(FinremCaseData caseData) {
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();
        return mhMigrationWrapper.getMhMigrationVersion()  != null;
    }

    /**
     * Determines whether hearing details should be migrated from the legacy fields to the modern structure.
     *
     * @param mhMigrationWrapper      the migration wrapper with migration flags
     * @param listForHearingWrapper   the wrapper containing legacy hearing data
     * @return {@code true} if hearing details should be populated; {@code false} otherwise
     */
    private boolean shouldPopulateListForHearingWrapper(MhMigrationWrapper mhMigrationWrapper,
                                                        ListForHearingWrapper listForHearingWrapper) {
        if (YesOrNo.isYes(mhMigrationWrapper.getIsListForHearingsMigrated())) {
            return false;
        }
        return listForHearingWrapper.getHearingType() != null;
    }

    private void appendToHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearingTabItems,
            caseData.getManageHearingsWrapper()::setHearingTabItems, item);
        /*
        addItemToList(caseData.getManageHearingsWrapper()::getApplicantHHearingTabItems,
            caseData.getManageHearingsWrapper()::setApplicantHHearingTabItems, item);
        addItemToList(caseData.getManageHearingsWrapper()::getRespondentHHearingTabItems,
                caseData.getManageHearingsWrapper()::setRespondentHHearingTabItems, item);
         */
    }

    private void appendToHearings(FinremCaseData caseData, ManageHearingsCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearings,
            caseData.getManageHearingsWrapper()::setHearings, item);
    }

    private Hearing toHearing(ListForHearingWrapper listForHearingWrapper) {
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

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(HearingType.valueOf(hearingType.name()))
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingRegionWrapper.toCourt())
            //.hearingMode(null) // Ignore it because existing List for Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toAdditionalHearingDocs(listForHearingWrapper))
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    private HearingTabItem toHearingTabItem(ListForHearingWrapper listForHearingWrapper) {
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

        return HearingTabItem.builder()
            .tabHearingType(hearingType.getId())
            .tabCourtSelection(hearingTabDataMapper.getCourtName(hearingRegionWrapper.toCourt()))
            .tabDateTime(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime))
            .tabTimeEstimate(timeEstimate)
            .tabConfidentialParties("Unknown")
            .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(additionalInformationAboutHearing))
            .tabHearingMigratedDate(LocalDateTime.now())
            .tabHearingDocuments(toAdditionalHearingDocs(listForHearingWrapper))
            .build();
    }

    private List<DocumentCollectionItem> toAdditionalHearingDocs(ListForHearingWrapper listForHearingWrapper) {
        CaseDocument doc = listForHearingWrapper.getAdditionalListOfHearingDocuments();
        return doc == null ? null : List.of(DocumentCollectionItem.builder().value(doc).build());
    }
}
