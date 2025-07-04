package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.addItemToList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

/**
 * Component responsible for appending hearing tab items to a case's hearing data wrapper
 * and converting hearing wrapper data to {@link HearingTabItem} instances.
 */
@Component
@Slf4j
public class HearingTabItemsAppender {

    private final HearingTabDataMapper hearingTabDataMapper;

    public HearingTabItemsAppender(HearingTabDataMapper hearingTabDataMapper) {
        this.hearingTabDataMapper = hearingTabDataMapper;
    }

    /**
     * Appends a {@link HearingTabCollectionItem} to the list of hearing tab items
     * within the {@code ManageHearingsWrapper} inside the provided {@link FinremCaseData}.
     *
     * <p>
     * Note: Additional appending to applicant and respondent hearing tab lists
     * is planned but currently commented out, pending tasks DFR-3831 and DFR-3587.
     * </p>
     *
     * @param caseData the case data to update
     * @param item     the hearing tab collection item to append
     */
    public void appendToHearingTabItems(FinremCaseData caseData, HearingTabCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearingTabItems,
            caseData.getManageHearingsWrapper()::setHearingTabItems, item);
        /* TODO Requires DFR-3831 and DFR-3587
        addItemToList(caseData.getManageHearingsWrapper()::getApplicantHHearingTabItems,
            caseData.getManageHearingsWrapper()::setApplicantHHearingTabItems, item);
        addItemToList(caseData.getManageHearingsWrapper()::getRespondentHHearingTabItems,
                caseData.getManageHearingsWrapper()::setRespondentHHearingTabItems, item);
         */
    }

    /**
     * Converts a {@link ListForHearingWrapper} instance into a {@link HearingTabItem}.
     * This involves extracting hearing type, date, time, time estimate, additional information,
     * court region, and associated hearing documents, formatting these details suitably
     * for display in the hearing tab.
     *
     * @param listForHearingWrapper the hearing data wrapper to convert
     * @return the constructed hearing tab item
     */
    public HearingTabItem toHearingTabItem(ListForHearingWrapper listForHearingWrapper) {
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

    /**
     * Converts an {@link InterimHearingItem} instance into a {@link HearingTabItem}.
     *
     * <p>
     * Extracts and formats interim hearing details including type, date, time, time estimate,
     * court region, additional information, and associated hearing documents for display.
     * Confidential parties data cannot be migrated and is set as "Unknown".
     * </p>
     *
     * @param interimHearingItem the interim hearing item to convert
     * @return the constructed hearing tab item
     */
    public HearingTabItem toHearingTabItem(InterimHearingItem interimHearingItem) {
        return HearingTabItem.builder()
            .tabHearingType(interimHearingItem.getInterimHearingType().getId())
            .tabCourtSelection(hearingTabDataMapper.getCourtName(interimHearingItem.toCourt()))
            .tabDateTime(hearingTabDataMapper.getFormattedDateTime(
                interimHearingItem.getInterimHearingDate(), interimHearingItem.getInterimHearingTime()))
            .tabTimeEstimate(interimHearingItem.getInterimHearingTimeEstimate())
            .tabConfidentialParties("Unknown")  // Cannot migrate "Who has received this notice"
            .tabAdditionalInformation(hearingTabDataMapper.getAdditionalInformation(
                interimHearingItem.getInterimAdditionalInformationAboutHearing()))
            .tabHearingMigratedDate(LocalDateTime.now())
            .tabHearingDocuments(toAdditionalHearingDocs(interimHearingItem))
            .build();
    }

    /**
     * Converts the additional list of hearing documents from the {@link ListForHearingWrapper}
     * into a singleton list of {@link DocumentCollectionItem}, or returns {@code null} if none exist.
     *
     * @param listForHearingWrapper the hearing data wrapper containing documents
     * @return a singleton list containing the document collection item, or {@code null} if no document is present
     */
    private List<DocumentCollectionItem> toAdditionalHearingDocs(ListForHearingWrapper listForHearingWrapper) {
        CaseDocument caseDocument = listForHearingWrapper.getAdditionalListOfHearingDocuments();
        return toSingletonListOrNull(DocumentCollectionItem.fromCaseDocument(caseDocument));
    }
    /**
     * Converts the additional hearing documents from an {@link InterimHearingItem}
     * into a singleton list of {@link DocumentCollectionItem}, or returns {@code null}
     * if no document is present.
     *
     * @param interimHearingItem the interim hearing item containing documents
     * @return a singleton list containing the document collection item, or {@code null} if no document is present
     */
    private List<DocumentCollectionItem> toAdditionalHearingDocs(InterimHearingItem interimHearingItem) {
        CaseDocument caseDocument = interimHearingItem.getInterimUploadAdditionalDocument();
        return toSingletonListOrNull(DocumentCollectionItem.fromCaseDocument(caseDocument));
    }

}
