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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @InjectMocks
    private HearingService hearingService;

    static InterimHearingCollection createInterimHearing(String id, InterimTypeOfHearing type, LocalDate date, String time) {
        // Create the InterimHearingItem using the provided parameters
        InterimHearingItem hearingItem = InterimHearingItem.builder()
            .interimHearingType(type)
            .interimHearingDate(date)
            .interimHearingTime(time)
            .build();

        // Create and return the InterimHearingCollection
        return InterimHearingCollection.builder()
            .id(UUID.fromString(id)) // Convert the string ID to a UUID
            .value(hearingItem) // Set the value to the created InterimHearingItem
            .build();
    }

    static DynamicList createExpectedDynamicList(Map<String, String> itemsWithIds) {
        // Create a DynamicList using the builder pattern
        return DynamicList.builder()
            .listItems(itemsWithIds.entrySet().stream()
                .map(entry -> DynamicListElement.builder().code(entry.getKey()).label(entry.getValue()).build())
                .toList()
            )
            .build();
    }

    static Stream<Arguments> hearingCases() {
        return Stream.of(
            // Basic Case: No Interim Hearings
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                    }
                })
            ),

            // Case 1: One Interim Hearing
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // UUID for interim hearing
                    }
                })
            ),

            // Case 2: Multiple Interim Hearings on the Same Date
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 1), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-01 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
                    }
                })
            ),

            // Case 3: Multiple Interim Hearings on Different Dates
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-02 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
                    }
                })
            ),

            // Case 4: Invalid Time Format for Interim Hearing
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "Invalid Time")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)");
                        // UUID for interim hearing with invalid time
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 Invalid Time - Directions (DIR)");
                    }
                })
            ),

            // Case 5: Top-Level Hearing Date Later Than Interim Hearings
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 3, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-02 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
                        put("00000000-0000-0000-0000-000000000000", "2024-03-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                    }
                })
            ),

            // Case 6: Null Hearing Type, Null LocalDate, Null Hearing Time
            Arguments.of(
                null, // Null Hearing Type
                null, // Null LocalDate
                null, // Null hearingTime
                List.of(),
                createExpectedDynamicList(new LinkedHashMap<>()) // Empty LinkedHashMap for null case
            ),

            // Case 7: Null Interim Hearing with Null Hearing Type, Null LocalDate, and Null Hearing Time
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM",
                List.of(
                    // Interim hearing with null type
                    createInterimHearing("00000000-0000-0000-0000-000000000002", null, LocalDate.of(2024, 2, 1), "2:00 AM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - (unknown)"); // UUID for interim hearing with null type
                    }
                })
            ),
            // Case 8: Null Interim Hearing Date with Valid Hearing Time
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM",
                List.of(
                    // Interim hearing with null date
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, null, "2:00 AM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "(unknown) 2:00 AM - Directions (DIR)"); // Interim hearing with null date
                    }
                })
            ),
            // Case 9: Null Interim Hearing Time with Valid Hearing Date
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM",
                List.of(
                    // Interim hearing with null time
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), null)
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 (unknown) - Directions (DIR)"); // Interim hearing with null time
                    }
                })
            ),
            // Case 10: Null Top-Level Hearing with Interim Hearings
            Arguments.of(
                null, // Null Hearing Type
                null, // Null LocalDate
                null, // Null hearingTime
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-02 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
                    }
                })
            ),
            // Case 11: Multiple Interim Hearings with One Null Type
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1),
                "10:00 AM",
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", null, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-02 4:00 PM - (unknown)"); // Interim hearing 2 with null type
                    }
                })
            ),
            // Case 12: Null Top-Level Hearing Type with Valid Interim Hearings
            Arguments.of(
                null, // Null Hearing Type
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM", // Example time for top-level hearing
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "2024-01-01 10:00 AM - (unknown)"); // Top-level hearing with null type
                        put("00000000-0000-0000-0000-000000000002", "2024-02-01 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2024-02-02 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
                    }
                })
            )
        );
    }

    @ParameterizedTest
    @MethodSource("hearingCases")
    void generateSelectableHearingsAsDynamicList(HearingTypeDirection topLevelHearingType,
                                                 LocalDate topLevelHearingDate,
                                                 String topLevelHearingTime,
                                                 List<InterimHearingCollection> interimHearings,
                                                 DynamicList expectedDynamicList) {
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
        assertEquals(expectedDynamicList.getListItems().size(), dynamicList.getListItems().size());
        assertDynamicListEquals(expectedDynamicList, dynamicList);
    }

    // Helper method to assert that two DynamicLists are equal
    private void assertDynamicListEquals(DynamicList expected, DynamicList actual) {
        assertEquals(expected.getListItems().size(), actual.getListItems().size(), "Dynamic list sizes are not equal.");

        for (int i = 0; i < expected.getListItems().size(); i++) {
            assertEquals(expected.getListItems().get(i).getLabel(), actual.getListItems().get(i).getLabel(),
                "Label at index " + i + " does not match.");
            assertEquals(expected.getListItems().get(i).getCode(), actual.getListItems().get(i).getCode(),
                "Code at index " + i + " does not match.");
        }
    }

}
