package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_NOTICES;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearingTabDataMapper {

    private final CourtDetailsMapper courtDetailsMapper;

    private static final String DEFAULT_HEARING_MODE = "Hearing mode not specified";
    private static final String DEFAULT_DATE_TIME = "Date and time not provided";
    private static final String DEFAULT_CONFIDENTIAL_PARTIES = "Parties not specified";

    public HearingTabItem mapHearingToTabData(ManageHearingsCollectionItem hearingCollectionItem,
                                              List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection) {

        Hearing hearing = hearingCollectionItem.getValue();

        return HearingTabItem.builder()
            .tabHearingType(getHearingType(hearing))
            .tabCourtSelection(getCourtName(hearing))
            .tabAttendance(getHearingMode(hearing))
            .tabDateTime(getFormattedDateTime(hearing))
            .tabTimeEstimate(hearing.getHearingTimeEstimate())
            .tabConfidentialParties(getConfidentialParties(hearing))
            .tabAdditionalInformation(getAdditionalInformation(hearing))
            .tabHearingDocuments(tryToMapHearingDocumentsToTabData(
                hearingDocumentsCollection, hearingCollectionItem.getId(), hearing))
            .build();
    }

    /**
     * Retrieves the name of the court associated with the given {@link Court} object.
     * Resilient method; If the court details cannot be mapped, handles with a generic message.
     *
     * @param court the {@link Court} object to retrieve the court name for
     * @return the name of the court
     */
    public String getCourtName(Court court) {
        final String courtNameNotAvailable = "Court name not available";
        try {
            return courtDetailsMapper.convertToFrcCourtDetails(court).getCourtName();
        } catch (IllegalStateException | NullPointerException e) {
            log.error("Caught an exception when retrieving court name. '{}' provided instead.",
                courtNameNotAvailable, e);
            return courtNameNotAvailable;
        }
    }

    /**
     * Retrieves the name of the court associated with the given {@link Hearing}.
     *
     * @param hearing the {@link Hearing} object to retrieve the court name for
     * @return the name of the court
     */
    public String getCourtName(Hearing hearing) {
        return getCourtName(hearing.getHearingCourtSelection());
    }

    /**
     * Formats the given hearing date and time into a human-readable string.
     * If the date is {@code null}, returns a default value.
     *
     * @param hearingDate the hearing date to format
     * @param hearingTime the hearing time to append to the date
     * @return a formatted date-time string (e.g., "27 Jun 2025 10:00 AM"), or a default value if the date is null
     */
    public String getFormattedDateTime(LocalDate hearingDate, String hearingTime) {
        return hearingDate != null
            ? hearingDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + hearingTime
            : DEFAULT_DATE_TIME;
    }

    /**
     * Formats the date and time of the given {@link Hearing} into a human-readable string.
     *
     * @param hearing the {@link Hearing} to extract and format date and time from
     * @return a formatted date-time string, or a default value if the date is null
     */
    public String getFormattedDateTime(Hearing hearing) {
        return getFormattedDateTime(hearing.getHearingDate(), hearing.getHearingTime());
    }

    /**
     * Returns the additional hearing information if available, otherwise returns a blank space.
     *
     * @param additionalHearingInformation the string containing additional information
     * @return the additional information or a blank space if {@code null}
     */
    public String getAdditionalInformation(String additionalHearingInformation) {
        return additionalHearingInformation != null ? additionalHearingInformation : " ";
    }

    /**
     * Retrieves the additional hearing information from the given {@link Hearing} object.
     *
     * <p>
     * This method delegates to {@link #getAdditionalInformation(String)} using the
     * value from {@code hearing.getAdditionalHearingInformation()}.
     *
     * @param hearing the {@link Hearing} object containing the additional information
     * @return the formatted or processed additional hearing information, or {@code null} if none is available
     */
    public String getAdditionalInformation(Hearing hearing) {
        return getAdditionalInformation(hearing.getAdditionalHearingInformation());
    }

    private String getHearingType(Hearing hearing) {
        return hearing.getHearingType().getId();
    }

    private String getHearingMode(Hearing hearing) {
        return hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : DEFAULT_HEARING_MODE;
    }

    private String getConfidentialParties(Hearing hearing) {
        return hearing.getPartiesOnCase() != null
            ? hearing.getPartiesOnCase().stream()
            .map(PartyOnCaseCollectionItem::getValue).map(PartyOnCase::getLabel)
            .collect(Collectors.joining(", "))
            : DEFAULT_CONFIDENTIAL_PARTIES;
    }

    /**
     * Wraps the call to map hearing documents to tab data with error handling.
     * If an exception occurs during the mapping process, logs the error and returns an empty list
     * @param hearingDocumentsCollection the collection of hearing documents to filter and process
     * @param hearingId                  the unique identifier of the hearing to match documents against
     * @return a list of {@link DocumentCollectionItem} representing documents associated with the hearing,
     *         or an empty list if an error occurs
     */
    private List<DocumentCollectionItem> tryToMapHearingDocumentsToTabData(
        List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection,
        UUID hearingId,
        Hearing hearing) {
        try {
            return mapHearingDocumentsToTabData(hearingDocumentsCollection, hearingId, hearing);
        } catch (NullPointerException npe) {
            log.error("NullPointerException mapping hearing documents to tab data.", npe);
            return List.of();
        }
    }

    /**
     * Constructs a concatenated list of all documents associated with a hearing.
     * This method combines two sources of documents related to a hearing:
     * 1. Documents from the `hearingDocumentsCollection` that match the given `hearingId`.
     * 2. Additional documents directly associated with the `Hearing` object.
     * The method performs the following steps:
     * - Filters the `hearingDocumentsCollection` to include only documents whose `hearingId` matches the provided `hearingId`.
     * - Maps the filtered documents to `DocumentCollectionItem` objects.
     * - Retrieves additional documents from the `Hearing` object and maps them to `DocumentCollectionItem` objects.
     * - Concatenates the two streams of documents into a single list.
     *
     * @param hearingDocumentsCollection the collection of hearing documents to filter and process
     * @param hearingId                  the unique identifier of the hearing to match documents against
     * @param hearing                    the `Hearing` object containing additional documents
     * @return a list of `DocumentCollectionItem` objects representing all documents associated with the hearing
     */
    private List<DocumentCollectionItem> mapHearingDocumentsToTabData(
        List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection,
        UUID hearingId,
        Hearing hearing) {

        List<DocumentCollectionItem> hearingDocuments = hearingDocumentsCollection != null
            ? hearingDocumentsCollection.stream()
            .filter(doc -> hearingId.equals(doc.getValue().getHearingId()))
            .map(doc -> DocumentCollectionItem.builder()
                .value(CaseDocument
                    .builder()
                    .documentUrl(doc.getValue().getHearingDocument().getDocumentUrl())
                    .documentFilename(doc.getValue().getHearingDocument().getDocumentFilename())
                    .uploadTimestamp(doc.getValue().getHearingDocument().getUploadTimestamp())
                    .documentBinaryUrl(doc.getValue().getHearingDocument().getDocumentBinaryUrl())
                    .categoryId(HEARING_NOTICES.getDocumentCategoryId())
                    .build())
                .build())
            .toList()
            : List.of();

        List<DocumentCollectionItem> additionalDocs = hearing.getAdditionalHearingDocs() != null
            ? hearing.getAdditionalHearingDocs().stream()
            .map(doc -> DocumentCollectionItem.builder()
                    .value(CaseDocument
                            .builder()
                            .documentUrl(doc.getValue().getDocumentUrl())
                            .documentFilename(doc.getValue().getDocumentFilename())
                            .uploadTimestamp(doc.getValue().getUploadTimestamp())
                            .documentBinaryUrl(doc.getValue().getDocumentBinaryUrl())
                            .categoryId(HEARING_NOTICES.getDocumentCategoryId())
                            .build()
                    ).build())
            .toList()
            : List.of();

        return Stream.concat(
            hearingDocuments.stream(),
            additionalDocs.stream()
        ).toList();
    }
}
