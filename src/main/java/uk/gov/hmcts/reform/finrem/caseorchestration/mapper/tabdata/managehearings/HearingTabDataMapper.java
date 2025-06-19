package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class HearingTabDataMapper {

    private final CourtDetailsMapper courtDetailsMapper;

    private static final String DEFAULT_HEARING_MODE = "Hearing mode not specified";
    private static final String DEFAULT_DATE_TIME = "Date and time not provided";
    private static final String DEFAULT_CONFIDENTIAL_PARTIES = "Confidential parties not specified";

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
            .tabHearingDocuments(mapHearingDocumentsToTabData(
                hearingDocumentsCollection, hearingCollectionItem.getId(), hearing))
            .build();
    }

    private String getHearingType(Hearing hearing) {
        return hearing.getHearingType().getId();
    }

    private String getCourtName(Hearing hearing) {
        return courtDetailsMapper.convertToFrcCourtDetails(hearing.getHearingCourtSelection()).getCourtName();
    }

    private String getHearingMode(Hearing hearing) {
        return hearing.getHearingMode() != null ? hearing.getHearingMode().getDisplayValue() : DEFAULT_HEARING_MODE;
    }

    private String getFormattedDateTime(Hearing hearing) {
        return hearing.getHearingDate() != null
            ? hearing.getHearingDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + hearing.getHearingTime()
            : DEFAULT_DATE_TIME;
    }

    private String getConfidentialParties(Hearing hearing) {
        return hearing.getPartiesOnCaseMultiSelectList() != null
            ? hearing.getPartiesOnCaseMultiSelectList().getValue().stream()
            .map(DynamicMultiSelectListElement::getLabel)
            .collect(Collectors.joining(", "))
            : DEFAULT_CONFIDENTIAL_PARTIES;
    }

    private String getAdditionalInformation(Hearing hearing) {
        return hearing.getAdditionalHearingInformation() != null ? hearing.getAdditionalHearingInformation() : " ";
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
            .map(doc -> DocumentCollectionItem.builder().value(doc.getValue().getHearingDocument()).build())
            .toList()
            : List.of();

        List<DocumentCollectionItem> additionalDocs = hearing.getAdditionalHearingDocs() != null
            ? hearing.getAdditionalHearingDocs().stream()
            .map(doc -> DocumentCollectionItem.builder().value(doc.getValue()).build())
            .toList()
            : List.of();
        
        return Stream.concat(
            hearingDocuments.stream(),
            additionalDocs.stream()
        ).toList();
    }
}
