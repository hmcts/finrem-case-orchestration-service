package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.manahehearings;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HearingTabDataMapper {

    private final CourtDetailsMapper courtDetailsMapper;

    public HearingTabItem mapHearingToTabData(ManageHearingsCollectionItem hearingCollectionItem, List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection) {

        Hearing hearing = hearingCollectionItem.getValue();

        return HearingTabItem.builder()
            .tabHearingType(hearing.getHearingType().getId())
            //TODO: Pull out court value
            .tabCourtSelection(courtDetailsMapper.convertToFrcCourtDetails(hearing.getHearingCourtSelection()).getCourtName())
            .tabAttendance(hearing.getHearingMode().getDisplayValue())
            .tabDateTime(hearing.getHearingDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " " + hearing.getHearingTime())
            .tabTimeEstimate(hearing.getHearingTimeEstimate())
            .tabConfidentialParties(hearing.getPartiesOnCaseMultiSelectList().getValue().stream().map(
                    DynamicMultiSelectListElement::getLabel).collect(Collectors.joining(", ")))
            .tabAdditionalInformation(hearing.getAdditionalHearingInformation())
            .tabHearingDocuments(mapHearingDocumentsToTabData(hearingDocumentsCollection, hearingCollectionItem.getId(), hearing))
            .build();
    }

    private List<DocumentCollectionItem> mapHearingDocumentsToTabData(
            List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection,
            UUID hearingId,
            Hearing hearing) {


        List<DocumentCollectionItem> documents = new ArrayList<>(hearingDocumentsCollection.stream()
                .filter(doc -> doc.getValue().getHearingId().equals(hearingId))
                .map(doc ->
                        DocumentCollectionItem.builder().value(doc.getValue().getHearingDocument()).build())
                .toList());

        documents.addAll(hearing.getAdditionalHearingDocs().stream()
                .map(doc -> DocumentCollectionItem.builder().value(doc.getValue()).build())
                .toList());

        return documents;
    }
}
