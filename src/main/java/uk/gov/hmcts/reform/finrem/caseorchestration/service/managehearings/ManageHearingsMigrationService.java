package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

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

        // We cannot migrate the "Who has received this notice" field from the List for Hearing event,
        // as the partiesOnCase field changes depending on the event.
        // Therefore, we default to "Unknown" for tabConfidentialParties in the Hearing tab.
        HearingTabItem newHearingTabItem = HearingTabItem.builder()
            .tabHearingType(hearingType.getId())
            .tabCourtSelection(hearingTabDataMapper.getCourtName(hearingRegionWrapper.toCourt()))
            .tabDateTime(hearingTabDataMapper.getFormattedDateTime(hearingDate, hearingTime))
            .tabTimeEstimate(timeEstimate)
            .tabConfidentialParties("Unknown")
            .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(additionalInformationAboutHearing))
            .tabHearingMigratedDate(LocalDateTime.now())
            .build();

        appendToHearingTabItems(caseData, HearingTabCollectionItem.builder().value(newHearingTabItem).build());

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
     * Populates the hearing tab lists in the given {@link FinremCaseData} object based on the interim hearings data.
     *
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Checks if the list for interim hearing wrapper should be populated using migration criteria.</li>
     *   <li>Validates if the interim hearing documents count matches the expected number.</li>
     *   <li>Maps interim hearing items and their associated hearing documents into hearing tab items.</li>
     *   <li>Appends the created hearing tab items to the applicant, respondent, and general hearing tab collections.</li>
     *   <li>Sets the migration flag {@code isListForInterimHearingsMigrated} in {@link MhMigrationWrapper} to YES if successful,
     *       or NO if migration is skipped or fails due to document count mismatch.</li>
     * </ul>
     *
     * <p>
     * Logs warnings when migration is skipped or fails due to insufficient documents.
     *
     * @param caseData the {@link FinremCaseData} containing interim hearing and migration wrappers; must not be {@code null}
     */
    public void populateListForInterimHearingWrapper(FinremCaseData caseData) {
        InterimWrapper interimWrapper = caseData.getInterimWrapper();
        MhMigrationWrapper mhMigrationWrapper = caseData.getMhMigrationWrapper();

        if (!shouldPopulateListForInterimHearingWrapper(mhMigrationWrapper, interimWrapper)) {
            log.warn("{} - List for Interim Hearing migration skipped.", caseData.getCcdCaseId());
            return;
        }

        if (!doesInterimHearingDocumentCountMatch(interimWrapper)) {
            log.warn("{} - List for Interim Hearing migration fails. Insufficient interim hearing documents.",
                caseData.getCcdCaseId());
            mhMigrationWrapper.setIsListForInterimHearingsMigrated(YesOrNo.NO);
            return;
        }

        List<InterimHearingItem> interimHearingItems = interimWrapper.getInterimHearings().stream()
            .map(InterimHearingCollection::getValue)
            .toList();

        List<CaseDocument> interimHearingNotices = interimWrapper.getInterimHearingDocuments().stream()
            .map(InterimHearingBulkPrintDocumentsData::getValue)
            .map(InterimHearingBulkPrintDocument::getCaseDocument)
            .toList();

        IntStream.range(0, interimHearingItems.size()).forEach(i -> {
            InterimHearingItem hearingItem = interimHearingItems.get(i);
            CaseDocument hearingNotice = interimHearingNotices.get(i);

            HearingTabItem hearingTabItem = HearingTabItem.builder()
                .tabHearingType(hearingItem.getInterimHearingType().getId())
                .tabCourtSelection(hearingTabDataMapper.getCourtName(hearingItem.toCourt()))
                .tabDateTime(hearingTabDataMapper.getFormattedDateTime(
                    hearingItem.getInterimHearingDate(), hearingItem.getInterimHearingTime()))
                .tabTimeEstimate(hearingItem.getInterimHearingTimeEstimate())
                .tabConfidentialParties("Unknown")  // Cannot migrate "Who has received this notice"
                .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(
                    hearingItem.getInterimAdditionalInformationAboutHearing()))
                .tabHearingMigratedDate(LocalDateTime.now())
                .tabHearingDocuments(prepareInterimHearingDocuments(hearingNotice, hearingItem))
                .build();

            HearingTabCollectionItem collectionItem = HearingTabCollectionItem.builder()
                .value(hearingTabItem)
                .build();

            appendToHearingTabItems(caseData, collectionItem);
            appendToApplicantHearingTabItems(caseData, collectionItem);
            appendToRespondentHearingTabItems(caseData, collectionItem);
        });

        mhMigrationWrapper.setIsListForInterimHearingsMigrated(YesOrNo.YES);
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

    private boolean shouldPopulateListForInterimHearingWrapper(MhMigrationWrapper mhMigrationWrapper,
                                                               InterimWrapper interimWrapper) {
        if (YesOrNo.isYes(mhMigrationWrapper.getIsListForInterimHearingsMigrated())) {
            return false;
        }
        return !emptyIfNull(interimWrapper.getInterimHearings()).isEmpty();
    }

    private void appendToHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem item) {
        appendToList(caseData.getManageHearingsWrapper()::getHearingTabItems,
                caseData.getManageHearingsWrapper()::setHearingTabItems, item);
        /*
        appendToList(caseData.getManageHearingsWrapper()::getApplicantHHearingTabItems,
            caseData.getManageHearingsWrapper()::setApplicantHHearingTabItems, item);
        appendToList(caseData.getManageHearingsWrapper()::getRespondentHHearingTabItems,
                caseData.getManageHearingsWrapper()::setRespondentHHearingTabItems, item);
         */
    }

    private boolean doesInterimHearingDocumentCountMatch(InterimWrapper interimWrapper) {
        return emptyIfNull(interimWrapper.getInterimHearings()).size() == emptyIfNull(interimWrapper.getInterimHearingDocuments()).size();
    }

    private List<DocumentCollectionItem> prepareInterimHearingDocuments(CaseDocument interimHearingNotice,
                                                                        InterimHearingItem interimHearingItem) {
        List<DocumentCollectionItem> hearingDocuments = new ArrayList<>();
        if (interimHearingItem.getInterimUploadAdditionalDocument() != null) {
            hearingDocuments.add(DocumentCollectionItem.builder()
                .value(interimHearingItem.getInterimUploadAdditionalDocument())
                .build());
        }
        hearingDocuments.add(DocumentCollectionItem.builder()
            .value(interimHearingNotice)
            .build());
        return hearingDocuments;
    }

    private void appendToList(
            Supplier<List<HearingTabCollectionItem>> getter,
            Consumer<List<HearingTabCollectionItem>> setter,
            HearingTabCollectionItem item
    ) {
        List<HearingTabCollectionItem> list = getter.get();
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(list); // in case it is Immutable
        }
        list.add(item);
        setter.accept(list);
    }

}
