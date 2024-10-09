package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
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
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingService {

    private static final UUID TOP_LEVEL_HEARING_KEY = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
            .code(TOP_LEVEL_HEARING_KEY.toString())
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
        Map<DynamicListElement, LocalDate> elementToDateMap = new HashMap<>();

        FinremCaseData caseData = caseDetails.getData();
        HearingTypeDirection hearingType = caseData.getHearingType();
        LocalDate hearingDate = caseData.getHearingDate();
        String hearingTime = caseData.getHearingTime();

        DynamicListElement topLevelDynamicListElement = buildTopLevelHearingDynamicListElement(hearingType, hearingDate, hearingTime);
        if (topLevelDynamicListElement != null) {
            dynamicListElements.add(topLevelDynamicListElement);
            elementToDateMap.put(topLevelDynamicListElement, hearingDate);
        }

        ofNullable(caseData.getInterimWrapper().getInterimHearings()).orElse(List.of()).forEach(ihc -> {
            DynamicListElement interimDynamicListElement = buildInterimHearingDynamicListElement(ihc);
            if (interimDynamicListElement != null) {
                LocalDate interimHearingDate = ihc.getValue().getInterimHearingDate();
                dynamicListElements.add(interimDynamicListElement);
                elementToDateMap.put(interimDynamicListElement, interimHearingDate);
            }
        });

        // Sort the dynamicListElements using the dates from the map
        dynamicListElements.sort(Comparator.comparing(elementToDateMap::get));

        return generateSelectableHearingsAsDynamicList(dynamicListElements);
    }

    DynamicList generateSelectableHearingsAsDynamicList(List<DynamicListElement> dynamicListElement) {
        return DynamicList.builder()
            .listItems(dynamicListElement)
            .build();
    }
}
