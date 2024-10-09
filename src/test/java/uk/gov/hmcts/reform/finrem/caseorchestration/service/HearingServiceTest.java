package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @InjectMocks
    private HearingService hearingService;

    static Stream<Arguments> hearingCases() {
        return Stream.of(
            // Edge Case 1: No Interim Hearings
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 1, 1), "10:00 AM",
                List.of(), 1), // Only top-level hearing

            // Edge Case 2: Multiple Interim Hearings on the Same Date
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 1, 1), "10:00 AM",
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 1))
                            .interimHearingTime("10:00 AM")
                            .build())
                        .build(),
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2024, 2, 1))
                            .interimHearingTime("2:00 PM")
                            .build())
                        .build()
                ), 3), // 1 top-level + 2 interim

            // Edge Case 3: Multiple Interim Hearings on Different Dates
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 1, 1), "10:00 AM",
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 1))
                            .interimHearingTime("10:00 AM")
                            .build())
                        .build(),
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2024, 2, 2))
                            .interimHearingTime("2:00 PM")
                            .build())
                        .build()
                ), 3), // 1 top-level + 2 interim

            // Edge Case 4: Mixed Hearing Types
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 1, 1), "10:00 AM",
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 1))
                            .interimHearingTime("10:00 AM")
                            .build())
                        .build(),
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2024, 2, 2))
                            .interimHearingTime("11:00 AM")
                            .build())
                        .build(),
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FDA)
                            .interimHearingDate(LocalDate.of(2024, 2, 3))
                            .interimHearingTime("12:00 PM")
                            .build())
                        .build()
                ), 4), // 1 top-level + 3 interim

            // Edge Case 5: Invalid Dates or Times
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 1, 1), "10:00 AM",
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 29)) // Invalid date (2024 is a leap year)
                            .interimHearingTime("10:00 AM")
                            .build())
                        .build()
                ), 2), // 1 top-level + 1 invalid interim

            // Edge Case 6: Top Level Hearing Date Later Than Interim Hearings
            Arguments.of(HearingTypeDirection.FH, LocalDate.of(2024, 3, 1), "10:00 AM",
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 1)) // Earlier date
                            .interimHearingTime("10:00 AM")
                            .build())
                        .build(),
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2024, 2, 15)) // Earlier date
                            .interimHearingTime("2:00 PM")
                            .build())
                        .build()
                ), 3), // 1 top-level + 2 interim, // 1 top-level + 1 invalid interim

            // Edge Case 7: Test case for null top-level hearing fields
            Arguments.of(null, null, null,
                List.of(
                    InterimHearingCollection.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.DIR)
                            .interimHearingDate(LocalDate.of(2024, 2, 1))
                            .interimHearingTime("11:00 AM")
                            .build())
                        .build()
                ), 1)
        );
    }

    @ParameterizedTest
    @MethodSource("hearingCases")
    void generateSelectableHearingsAsDynamicList(HearingTypeDirection topLevelHearingType,
                                                 LocalDate topLevelHearingDate,
                                                 String topLevelHearingTime,
                                                 List<InterimHearingCollection> interimHearings,
                                                 int expectedFinalSize) {
        // Arrange
        FinremCaseData.FinremCaseDataBuilder caseDataBuilder = FinremCaseData.builder()
            .interimWrapper(InterimWrapper.builder().interimHearings(interimHearings).build());

        // Set top-level fields only if they are not null
        if (topLevelHearingType != null) {
            caseDataBuilder.hearingType(topLevelHearingType);
        }
        if (topLevelHearingDate != null) {
            caseDataBuilder.hearingDate(topLevelHearingDate);
        }
        if (topLevelHearingTime != null) {
            caseDataBuilder.hearingTime(topLevelHearingTime);
        }

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = caseDataBuilder.build();
        when(caseDetails.getData()).thenReturn(caseData);

        // Act
        DynamicList dynamicList = hearingService.generateSelectableHearingsAsDynamicList(caseDetails);

        // Assert
        List<DynamicListElement> elements = dynamicList.getListItems();
        assertEquals(expectedFinalSize, elements.size());

        // Verify that the elements are sorted by date.
        for (int i = 1; i < elements.size(); i++) {
            LocalDate previousDate = extractHearingDateFromLabel(elements.get(i - 1).getLabel());
            LocalDate currentDate = extractHearingDateFromLabel(elements.get(i).getLabel());
            assertTrue(previousDate.isBefore(currentDate) || previousDate.isEqual(currentDate),
                "The list is not sorted by hearing date.");
        }

        // Verify the HearingType for each element in the list.
        for (DynamicListElement element : elements) {
            String codeFromElement = element.getCode();
            String label = element.getLabel();
            String hearingTypeFromLabel = extractHearingTypeFromLabel(label);
            LocalDate hearingDateFromLabel = extractHearingDateFromLabel(label);
            String hearingTimeFromLabel = extractHearingTimeFromLabel(label);

            // Check if the extracted hearing type matches either the top-level or one of the interim hearing types.
            boolean isMatchingType = (topLevelHearingType != null && hearingTypeFromLabel.equals(topLevelHearingType.getId()))
                || interimHearings.stream()
                .anyMatch(interim -> interim.getValue().getInterimHearingType().getId().equals(hearingTypeFromLabel));

            assertTrue(isMatchingType, "The hearing type in the label does not match the expected value: " + label);

            // Verify the code for top-level hearing
            if (topLevelHearingType != null && hearingTypeFromLabel.equals(topLevelHearingType.getId())
                && hearingDateFromLabel.equals(topLevelHearingDate)
                && hearingTimeFromLabel.equals(topLevelHearingTime)) {
                assertEquals("00000000-0000-0000-0000-000000000000", codeFromElement,
                    "The code for the top-level hearing does not match the expected static ID.");
            } else {
                // For interim hearings, check the code against the collection ID
                UUID interimId = interimHearings.stream()
                    .filter(interim -> interim.getValue().getInterimHearingType().getId().equals(hearingTypeFromLabel))
                    .map(InterimHearingCollection::getId)
                    .findFirst()
                    .orElse(null);

                assertNotNull(interimId, "No matching interim hearing found for hearing type: " + hearingTypeFromLabel);
                assertEquals(interimId.toString(), codeFromElement,
                    "The code for the interim hearing does not match the expected ID: " + codeFromElement);
            }
        }
    }

    private LocalDate extractHearingDateFromLabel(String label) {
        // Assuming the label format is like "2024-02-01 10:00 AM - FH",
        // where the date is at the beginning.
        String dateString = label.substring(0, label.indexOf(" ")).trim();
        return LocalDate.parse(dateString);
    }

    private String extractHearingTypeFromLabel(String label) {
        // Assuming the label format is like "2024-02-01 10:00 AM - FH",
        // extract the hearing type after the last " - ".
        return label.substring(label.lastIndexOf("-") + 1).trim();
    }

    private String extractHearingTimeFromLabel(String label) {
        // Assuming the label format is "2024-02-01 10:00 AM - FH"
        // The time is located between the date and the hyphen.
        int timeStartIndex = label.indexOf(" ") + 1; // Start after the first space
        int timeEndIndex = label.lastIndexOf(" -"); // End before the last " -"
        return label.substring(timeStartIndex, timeEndIndex).trim();
    }

}
