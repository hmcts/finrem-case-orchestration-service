package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.manahehearings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HearingTabDataMapper {

    private final CourtDetailsConfiguration courtDetailsConfiguration;

    public HearingTabItem mapHearingToTabData(ManageHearingsCollectionItem hearingCollectionItem, List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection) {

        Hearing hearing = hearingCollectionItem.getValue();

        return HearingTabItem.builder()
            .hearingType(hearing.getHearingType().name())
            //TODO: Pull out court value
            //.courtSelection(courtDetailsConfiguration.getCourts().get(courtSelection).getCourtName())
            .courtSelection("Cheese")
            .hearingAttendance(hearing.getHearingMode().name())
            .hearingDateTime(hearing.getHearingDate() + " " + hearing.getHearingTime())
            .timeEstimate(hearing.getHearingTimeEstimate())
            .hearingConfidentialParties(hearing.getPartiesOnCaseMultiSelectList().toString())
            .additionalHearingInformation(hearing.getAdditionalHearingInformation())
            .hearingDocuments(
                hearingDocumentsCollection.stream()
                    .filter(doc -> doc.getValue().getHearingId().equals(hearingCollectionItem.getId()))
                    .map(doc -> doc.getValue().getHearingDocument())
                    .toList()
            )
            .build();
    }
}
