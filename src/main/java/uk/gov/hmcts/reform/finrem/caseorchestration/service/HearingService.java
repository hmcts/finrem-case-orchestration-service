package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FinremDateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.TOP_LEVEL_HEARING_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingService {

    // Helper class to handle sorting by date, time, and type.
    private record HearingSortingKey(LocalDate hearingDate, String hearingTime, String hearingType) implements Comparable<HearingSortingKey> {

        @Override
        public int compareTo(HearingSortingKey other) {
            return Comparator
                .comparing(HearingSortingKey::hearingDate, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(HearingSortingKey::hearingTime, Comparator.nullsLast(String::compareTo))
                .thenComparing(HearingSortingKey::hearingType, Comparator.nullsLast(String::compareTo))
                .compare(this, other);
        }
    }

    private static final String UNKNOWN_TEXT = "unknown";

    String toUnknownDisplayText() {
        return (format("(%s)", UNKNOWN_TEXT));
    }

    String formatDynamicListElementLabel(String hearingTypeInString, LocalDate hearingDate, String hearingTime) {
        return format("%s %s - %s",
            hearingDate == null ? toUnknownDisplayText() : FinremDateUtils.getDateFormatter().format(hearingDate),
            StringUtils.isEmpty(hearingTime) ? toUnknownDisplayText() : hearingTime,
            StringUtils.isEmpty(hearingTypeInString) ? toUnknownDisplayText() : hearingTypeInString);
    }

    DynamicListElement buildTopLevelHearingDynamicListElement(HearingTypeDirection hearingType, LocalDate hearingDate, String hearingTime) {
        if (hearingType == null && hearingDate == null && StringUtils.isEmpty(hearingTime)) {
            return null;
        }
        return DynamicListElement.builder()
            .code(TOP_LEVEL_HEARING_ID)
            .label(formatDynamicListElementLabel(hearingType == null ? "" : hearingType.getId(), hearingDate, hearingTime))
            .build();
    }

    DynamicListElement buildInterimHearingDynamicListElement(InterimHearingCollection ihc) {
        String code = ihc.getId().toString();
        LocalDate hearingDate = ihc.getValue().getInterimHearingDate();
        String hearingTime = ihc.getValue().getInterimHearingTime();
        InterimTypeOfHearing hearingType = ihc.getValue().getInterimHearingType();

        String label = formatDynamicListElementLabel(hearingType == null ? "" : hearingType.getId(), hearingDate, hearingTime);
        return DynamicListElement.builder().code(code).label(label).build();
    }

    public DynamicList generateSelectableHearingsAsDynamicList(FinremCaseDetails caseDetails) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        Map<DynamicListElement, HearingSortingKey> elementToSortingKeyMap = new HashMap<>();

        FinremCaseData caseData = caseDetails.getData();
        HearingTypeDirection hearingType = caseData.getHearingType();
        LocalDate hearingDate = caseData.getHearingDate();
        String hearingTime = caseData.getHearingTime();

        DynamicListElement topLevelDynamicListElement = buildTopLevelHearingDynamicListElement(hearingType, hearingDate, hearingTime);
        if (topLevelDynamicListElement != null) {
            dynamicListElements.add(topLevelDynamicListElement);
            elementToSortingKeyMap.put(topLevelDynamicListElement, new HearingSortingKey(hearingDate, hearingTime, hearingType == null
                ? null : hearingType.getId()));
        }

        ofNullable(caseData.getInterimWrapper().getInterimHearings()).orElse(List.of()).forEach(ihc -> {
            DynamicListElement interimDynamicListElement = buildInterimHearingDynamicListElement(ihc);
            if (interimDynamicListElement != null) {
                LocalDate interimHearingDate = ihc.getValue().getInterimHearingDate();
                String interimHearingTime = ihc.getValue().getInterimHearingTime();
                String interimHearingType = ihc.getValue().getInterimHearingType() != null ? ihc.getValue().getInterimHearingType().getId() : null;

                dynamicListElements.add(interimDynamicListElement);
                elementToSortingKeyMap.put(interimDynamicListElement, new HearingSortingKey(interimHearingDate, interimHearingTime,
                    interimHearingType));
            }
        });

        // Sort the dynamicListElements using the sorting keys from the map
        dynamicListElements.sort(Comparator.comparing(elementToSortingKeyMap::get));

        return generateSelectableHearingsAsDynamicList(dynamicListElements);
    }

    DynamicList generateSelectableHearingsAsDynamicList(List<DynamicListElement> dynamicListElement) {
        return DynamicList.builder()
            .listItems(dynamicListElement)
            .build();
    }
}
