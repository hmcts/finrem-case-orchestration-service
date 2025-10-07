package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.HearingRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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

    public void appendToHearings(FinremCaseData caseData,
                                 Supplier<ManageHearingsCollectionItem> hearingSupplier) {
        ManageHearingsCollectionItem item = hearingSupplier.get();
        item.setId(UUID.randomUUID());
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
    public Hearing toHearing(ListForHearingWrapper listForHearingWrapper, AllocatedRegionWrapper allocatedRegionWrapper) {
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
            .hearingCourtSelection(hearingRegionWrapper.isEmpty()
                ? allocatedRegionWrapper.toCourt() : hearingRegionWrapper.toCourt())
            //.hearingMode(null) // Ignore it because existing List for Hearing doesn't capture hearing mode
            .additionalHearingInformation(additionalInformationAboutHearing)
            .additionalHearingDocs(toAdditionalHearingDocs(listForHearingWrapper))
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
        AdditionalHearingDocumentCollection additionalDocument = interimHearingItem.getInterimUploadAdditionalDocument() == null ? null :
            AdditionalHearingDocumentCollection.builder().value(
                AdditionalHearingDocument.builder()
                .additionalDocument(interimHearingItem.getInterimUploadAdditionalDocument()
                    .toBuilder().build())
                    .build())
                .build();

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
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = directionDetail.getLocalCourt();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(hearingType != null ? HearingType.valueOf(hearingType.name()) : null)
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing List for Interim Hearing doesn't capture hearing mode
            .wasMigrated(YesOrNo.YES)
            .build();
    }

    public Hearing toHearing(HearingDirectionDetail hearingDirectionDetail) {
        // Type of Hearing
        HearingTypeDirection hearingType = hearingDirectionDetail.getTypeOfHearing();
        // Hearing Date
        LocalDate hearingDate = hearingDirectionDetail.getDateOfHearing();
        // Hearing Time
        String hearingTime = hearingDirectionDetail.getHearingTime();
        // Time Estimate
        String timeEstimate = hearingDirectionDetail.getTimeEstimate();
        // Additional information about the hearing is not captured
        // Hearing Court - Please state in which Financial Remedies Court Zone the applicant resides
        Court hearingCourtSelection = hearingDirectionDetail.getLocalCourt();

        return Hearing.builder()
            .hearingDate(hearingDate)
            .hearingType(hearingType != null ? HearingType.valueOf(hearingType.name()) : null)
            .hearingTimeEstimate(timeEstimate)
            .hearingTime(hearingTime)
            .hearingCourtSelection(hearingCourtSelection)
            //.hearingMode(null) // Ignore it because existing logic doesn't capture hearing mode
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
    private List<AdditionalHearingDocumentCollection> toAdditionalHearingDocs(ListForHearingWrapper listForHearingWrapper) {
        CaseDocument doc = listForHearingWrapper.getAdditionalListOfHearingDocuments();
        return doc == null ? null : List.of(
            AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .additionalDocument(doc.toBuilder().build())
                .build())
            .build());
    }
}
