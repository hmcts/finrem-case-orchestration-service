package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingService {

    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");

    static final String TOP_LEVEL_HEARING_ID = "00000000-0000-0000-0000-000000000000";

    // Helper class to handle sorting by date, time, and type.
    private record HearingSortingKey(LocalDate hearingDate, String hearingTime,
                                     String hearingType) implements Comparable<HearingSortingKey> {

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
            hearingDate == null ? toUnknownDisplayText() : dateFormatter.format(hearingDate),
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

    DynamicListElement buildDirectionDetailDynamicListElement(DirectionDetailCollection directionDetailCollection) {
        String code = directionDetailCollection.getId().toString();
        LocalDate hearingDate = directionDetailCollection.getValue().getDateOfHearing();
        String hearingTime = directionDetailCollection.getValue().getHearingTime();
        HearingTypeDirection hearingType = directionDetailCollection.getValue().getTypeOfHearing();

        String label = formatDynamicListElementLabel(hearingType == null ? "" : hearingType.getId(), hearingDate, hearingTime);
        return DynamicListElement.builder().code(code).label(label).build();
    }

    /**
     * Generates a {@link DynamicList} containing selectable hearings for a given case.
     *
     * <p>
     * This method retrieves the hearing type, date, and time from the case details and constructs
     * a list of {@link DynamicListElement} objects representing the available hearings.
     * It includes both the top-level hearing, any interim hearings and hearings created from
     * Process Order event if present.
     *
     * <p>
     * The list is sorted based on a {@link HearingSortingKey}, which orders elements
     * by hearing date, time, and type.
     *
     * @param caseDetails the {@link FinremCaseDetails} containing case data with hearing details
     * @return a {@link DynamicList} of selectable hearings sorted by hearing attributes
     */
    public DynamicList generateSelectableHearingsAsDynamicList(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        Map<DynamicListElement, HearingSortingKey> elementToSortingKeyMap = new HashMap<>();

        populateTopLevelHearings(caseData, dynamicListElements, elementToSortingKeyMap);
        populateInterimHearings(caseData, dynamicListElements, elementToSortingKeyMap);
        populateHearingsCreatedFromProcessOrder(caseData, dynamicListElements, elementToSortingKeyMap);

        // Sort the dynamicListElements using the sorting keys from the map
        dynamicListElements.sort(Comparator.comparing(elementToSortingKeyMap::get));

        return generateSelectableHearingsAsDynamicList(dynamicListElements);
    }

    DynamicList generateSelectableHearingsAsDynamicList(List<DynamicListElement> dynamicListElement) {
        return DynamicList.builder()
            .listItems(dynamicListElement)
            .build();
    }

    public LocalDate getHearingDate(FinremCaseData caseData, DynamicListElement selected) {
        return getHearingInfo(caseData, selected, ListForHearingWrapper::getHearingDate,
            e -> e.getValue().getInterimHearingDate(),
            e -> e.getValue().getDateOfHearing());
    }

    public String getHearingType(FinremCaseData caseData, DynamicListElement selected) {
        return getHearingInfo(caseData, selected,
            d -> d.getHearingType() != null ? d.getHearingType().getId() : "",
            e -> e.getValue().getInterimHearingType().getId(),
            e -> e.getValue().getTypeOfHearing().getId());
    }

    public String getHearingTime(FinremCaseData caseData, DynamicListElement selected) {
        return getHearingInfo(caseData, selected, ListForHearingWrapper::getHearingTime,
            e -> e.getValue().getInterimHearingTime(),
            e -> e.getValue().getHearingTime());
    }

    private <T> T getHearingInfo(FinremCaseData caseData, DynamicListElement selected,
                                 Function<ListForHearingWrapper, T> hearingExtractor,
                                 Function<InterimHearingCollection, T> interimHearingExtractor,
                                 Function<DirectionDetailCollection, T> hearingCreatedFromProcessOrderExtractor) {
        if (StringUtils.isEmpty(selected.getCode())) {
            return null;
        }

        if (TOP_LEVEL_HEARING_ID.equals(selected.getCode())) {
            return hearingExtractor.apply(caseData.getListForHearingWrapper());
        }

        // Search for the matching InterimHearingCollection; otherwise, search for the matching directionDetailCollection
        return getHearingInfoFromInterimHearing(caseData, selected, interimHearingExtractor)
            .orElse(
                getHearingInfoFromHearingCreatedFromProcessOrder(caseData, selected, hearingCreatedFromProcessOrderExtractor).orElse(null)
            );
    }

    private <T> Optional<T> getHearingInfoFromInterimHearing(FinremCaseData caseData, DynamicListElement selected,
                                                             Function<InterimHearingCollection, T> interimHearingExtractor) {
        return ofNullable(caseData.getInterimWrapper().getInterimHearings()).orElse(List.of()).stream()
            .filter(i -> i.getId().toString().equals(selected.getCode()) && i.getValue() != null)
            .map(interimHearingExtractor)
            .findFirst();
    }

    private <T> Optional<T> getHearingInfoFromHearingCreatedFromProcessOrder(FinremCaseData caseData, DynamicListElement selected,
                                                                             Function<DirectionDetailCollection, T>
                                                                                 hearingCreatedFromProcessOrderExtractor) {
        return ofNullable(caseData.getDirectionDetailsCollection()).orElse(List.of()).stream()
            .filter(e -> e.getId().toString().equals(selected.getCode()) && e.getValue() != null)
            .map(hearingCreatedFromProcessOrderExtractor)
            .findFirst();
    }

    public String formatHearingInfo(String hearingType, LocalDate hearingDate, String hearingTime) {
        return format(
            "%s on %s %s",
            Optional.ofNullable(hearingType).orElse("N/A"),
            Optional.ofNullable(hearingDate)
                .map(date -> hearingDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .orElse("N/A"),
            Optional.ofNullable(hearingTime).orElse("N/A")
        );
    }

    private void populateTopLevelHearings(FinremCaseData caseData, List<DynamicListElement> dynamicListElements,
                                          Map<DynamicListElement, HearingSortingKey> elementToSortingKeyMap) {
        HearingTypeDirection hearingType = caseData.getListForHearingWrapper().getHearingType();
        LocalDate hearingDate = caseData.getListForHearingWrapper().getHearingDate();
        String hearingTime = caseData.getListForHearingWrapper().getHearingTime();

        DynamicListElement topLevelDynamicListElement = buildTopLevelHearingDynamicListElement(hearingType, hearingDate, hearingTime);
        if (topLevelDynamicListElement != null) {
            dynamicListElements.add(topLevelDynamicListElement);
            elementToSortingKeyMap.put(topLevelDynamicListElement, new HearingSortingKey(hearingDate, hearingTime, hearingType == null
                ? null : hearingType.getId()));
        }
    }

    private void populateInterimHearings(FinremCaseData caseData, List<DynamicListElement> dynamicListElements,
                                         Map<DynamicListElement, HearingSortingKey> elementToSortingKeyMap) {
        ofNullable(caseData.getInterimWrapper().getInterimHearings()).orElse(List.of()).forEach(interimHearingCollection -> {
            DynamicListElement dynamicListElement = buildInterimHearingDynamicListElement(interimHearingCollection);
            if (dynamicListElement != null) {
                LocalDate hearingDate = interimHearingCollection.getValue().getInterimHearingDate();
                String hearingTime = interimHearingCollection.getValue().getInterimHearingTime();
                String hearingType = interimHearingCollection.getValue().getInterimHearingType() != null
                    ? interimHearingCollection.getValue().getInterimHearingType().getId() : null;

                dynamicListElements.add(dynamicListElement);
                elementToSortingKeyMap.put(dynamicListElement, new HearingSortingKey(hearingDate, hearingTime, hearingType));
            }
        });
    }

    private void populateHearingsCreatedFromProcessOrder(FinremCaseData caseData, List<DynamicListElement> dynamicListElements,
                                                         Map<DynamicListElement, HearingSortingKey> elementToSortingKeyMap) {
        ofNullable(caseData.getDirectionDetailsCollection()).orElse(List.of()).forEach(directionDetailCollection -> {
            DynamicListElement dynamicListElement = buildDirectionDetailDynamicListElement(directionDetailCollection);
            if (dynamicListElement != null) {
                LocalDate hearingDate = directionDetailCollection.getValue().getDateOfHearing();
                String hearingTime = directionDetailCollection.getValue().getHearingTime();
                String hearingType = directionDetailCollection.getValue().getTypeOfHearing() != null
                    ? directionDetailCollection.getValue().getTypeOfHearing().getId() : null;

                dynamicListElements.add(dynamicListElement);
                elementToSortingKeyMap.put(dynamicListElement, new HearingSortingKey(hearingDate, hearingTime, hearingType));
            }
        });
    }

}
