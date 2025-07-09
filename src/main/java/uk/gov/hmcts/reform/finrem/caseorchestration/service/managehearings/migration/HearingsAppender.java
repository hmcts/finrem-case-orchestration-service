package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.addItemToList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.toSingletonListOrNull;

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
     * Converts a {@link ListForHearingWrapper} instance into a {@link Hearing} domain object.
     *
     * <p>
     * Extracts and maps hearing details such as type, date, time, time estimate,
     * court region, additional information, and associated hearing documents.
     * Marks the hearing as migrated.
     * </p>
     *
     * @param listForHearingWrapper the hearing data wrapper to convert
     * @return the constructed {@code Hearing} object
     */
    public Hearing toHearing(ListForHearingWrapper listForHearingWrapper) {
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

    /**
     * Converts an {@link InterimHearingItem} instance into a {@link Hearing} domain object.
     *
     * <p>
     * Extracts and maps interim hearing details including type, date, time, time estimate,
     * court region, additional information, and associated hearing documents.
     * Marks the hearing as migrated.
     * </p>
     *
     * @param interimHearingItem the interim hearing item to convert
     * @return the constructed {@code Hearing} object
     */
    public Hearing toHearing(InterimHearingItem interimHearingItem) {
        // Type of Hearing
        InterimTypeOfHearing hearingType = interimHearingItem.getInterimHearingType();
        // Hearing Date
        LocalDate hearingDate = interimHearingItem.getInterimHearingDate();
        // Hearing Time
        String hearingTime = interimHearingItem.getInterimHearingTime();
        // Time Estimate
        String timeEstimate = interimHearingItem.getInterimHearingTimeEstimate();
        // Additional information about the hearing
        String additionalInformationAboutHearing = interimHearingItem.getInterimAdditionalInformationAboutHearing();
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = interimHearingItem.toCourt();
        DocumentCollectionItem additionalDocument = interimHearingItem.getInterimUploadAdditionalDocument() == null ? null :
            DocumentCollectionItem.builder().value(interimHearingItem.getInterimUploadAdditionalDocument()).build();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(HearingType.valueOf(hearingType.name()))
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing List for Interim Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toSingletonListOrNull(additionalDocument))
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    public Hearing toHearing(GeneralApplicationWrapper generalApplicationWrapper,
                             GeneralApplicationRegionWrapper generalApplicationRegionWrapper) {
        // Type of Hearing is not captured
        // Hearing Date
        LocalDate hearingDate = generalApplicationWrapper.getGeneralApplicationDirectionsHearingDate();
        // Hearing Time
        String hearingTime = generalApplicationWrapper.getGeneralApplicationDirectionsHearingTime();
        // Time Estimate
        String timeEstimate = generalApplicationWrapper.getGeneralApplicationDirectionsHearingTimeEstimate();
        // Additional information about the hearing
        String additionalInformationAboutHearing = generalApplicationWrapper.getGeneralApplicationDirectionsAdditionalInformation();
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = generalApplicationRegionWrapper.toCourt();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(HearingType.APPLICATION_HEARING)
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing List for Interim Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            //.partiesOnCaseMultiSelectList() // Unknown as partiesOnCase is updated by multiple events.
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    public Hearing toHearing(DirectionDetail directionDetail) {
        // Type of Hearing
        HearingTypeDirection hearingType = directionDetail.getTypeOfHearing();
        // Hearing Date
        LocalDate hearingDate = directionDetail.getDateOfHearing();
        // Hearing Time
        String hearingTime = directionDetail.getHearingTime();
        // Time Estimate
        String timeEstimate = directionDetail.getTimeEstimate();
        // Additional information about the hearing is not captured
        String additionalInformationAboutHearing = null;
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = null; //TODO directionDetail.toCourt();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(hearingType != null ? HearingType.valueOf(hearingType.name()) : null)
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing List for Interim Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
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
