package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.addItemToList;

/**
 * Spring component responsible for appending hearing items to the hearings collection
 * in {@link FinremCaseData} and converting hearing-related wrapper data
 * into {@link Hearing} domain objects.
 */
@Component
@Slf4j
public class HearingsAppender {

    /**
     * Appends a {@link ManageHearingsCollectionItem} to the hearings list
     * contained within the {@code ManageHearingsWrapper} of the provided {@link FinremCaseData}.
     *
     * @param caseData the case data containing the hearings wrapper
     * @param item     the hearing collection item to append
     */
    public void appendToHearings(FinremCaseData caseData, ManageHearingsCollectionItem item) {
        addItemToList(caseData.getManageHearingsWrapper()::getHearings,
            caseData.getManageHearingsWrapper()::setHearings, item);
    }

    /**
     * Converts hearing data from the {@link ListForHearingWrapper} inside the provided {@link FinremCaseData}
     * into a {@link Hearing} object.
     *
     * <p>
     * The hearing information extracted includes hearing type, date, time, time estimate,
     * additional information, and court location. The migration flag is set to indicate
     * this hearing was migrated.
     * </p>
     *
     * <p>
     * Note: Hearing mode and parties on case are currently ignored because
     * the existing wrapper doesn't capture these or they are handled elsewhere.
     * </p>
     *
     * @param caseData the case data containing hearing wrapper details
     * @return the constructed hearing domain object
     */
    public Hearing toHearing(FinremCaseData caseData) {
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();

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
            .hearingType(hearingType == null ? null : HearingType.valueOf(hearingType.name()))
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingRegionWrapper == null ? null : hearingRegionWrapper.toCourt())
            //.hearingMode(null) // Ignore it because existing List for Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toAdditionalHearingDocs(listForHearingWrapper))
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    /**
     * Converts any additional hearing documents present in the {@link ListForHearingWrapper}
     * to a list of {@link DocumentCollectionItem}.
     *
     * <p>
     * Returns {@code null} if no documents exist.
     * </p>
     *
     * @param listForHearingWrapper the wrapper containing additional hearing documents
     * @return singleton list with the document collection item or {@code null} if none found
     */
    private List<DocumentCollectionItem> toAdditionalHearingDocs(ListForHearingWrapper listForHearingWrapper) {
        CaseDocument doc = listForHearingWrapper.getAdditionalListOfHearingDocuments();
        return doc == null ? null : List.of(DocumentCollectionItem.builder().value(doc).build());
    }
}
