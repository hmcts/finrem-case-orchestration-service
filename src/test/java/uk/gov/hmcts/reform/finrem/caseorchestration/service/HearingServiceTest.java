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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService.TOP_LEVEL_HEARING_ID;

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

    static Stream<Arguments> hearingDateCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, LocalDate.of(2024, 10, 21)), // Top-level hearing
            Arguments.of("11000000-0000-0000-0000-000000000000", LocalDate.of(2024, 10, 22)), // Valid interim hearing date
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
    }

    static Stream<Arguments> hearingTypeCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, HearingTypeDirection.FH.getId()), // Top-level hearing type
            Arguments.of("11000000-0000-0000-0000-000000000000", InterimTypeOfHearing.DIR.getId()), // Valid interim hearing type
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
    }

    static Stream<Arguments> hearingTimeCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, "09:00 AM"), // Top-level hearing time
            Arguments.of("11000000-0000-0000-0000-000000000000", "10:30 AM"), // Valid interim hearing time
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
    }

    static Stream<Arguments> hearingTimeEstimateCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, "1 hour"), // Top-level hearing time
            Arguments.of("11000000-0000-0000-0000-000000000000", "1.5 hour"), // Valid interim hearing time
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
    }

    @ParameterizedTest
    @MethodSource("hearingDateCases")
    void testGetHearingDate(String selectedCode, LocalDate expectedDate) {
        Arrays.asList(true, false).forEach(singleInterimHearing -> {
            FinremCaseData caseData = spy(FinremCaseData.class);
            DynamicListElement selected = mock(DynamicListElement.class);
            when(selected.getCode()).thenReturn(selectedCode);

            // Mocking the data structure
            caseData.setHearingDate(LocalDate.of(2024, 10, 21));
            // Mocking the data structure based on the singleInterimHearing parameter
            if (singleInterimHearing) {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingDate(LocalDate.of(2024, 10, 22)).build())
                            .build()
                    ))
                    .build());
            } else {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingDate(LocalDate.of(2024, 10, 22)).build())
                            .build(),
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("12000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingDate(LocalDate.of(2024, 10, 23)).build())
                            .build()
                    ))
                    .build());
            }

            // Act
            LocalDate result = hearingService.getHearingDate(caseData, selected);

            // Assert
            assertEquals(expectedDate, result);
        });
    }

    @ParameterizedTest
    @MethodSource("hearingTypeCases")
    void testGetHearingType(String selectedCode, String expectedType) {
        Arrays.asList(true, false).forEach(singleInterimHearing -> {
            // Arrange
            FinremCaseData caseData = spy(FinremCaseData.class);
            DynamicListElement selected = mock(DynamicListElement.class);
            when(selected.getCode()).thenReturn(selectedCode);

            // Mocking the data structure
            caseData.setHearingType(HearingTypeDirection.FH);
            // Mocking the data structure
            if (singleInterimHearing) {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingType(InterimTypeOfHearing.DIR).build())
                            .build()
                    ))
                    .build());
            } else {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingType(InterimTypeOfHearing.DIR).build())
                            .build(),
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("12000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingType(InterimTypeOfHearing.FDA).build())
                            .build()
                    ))
                    .build());
            }

            // Act
            String result = hearingService.getHearingType(caseData, selected);

            // Assert
            assertEquals(expectedType, result);
        });
    }

    @ParameterizedTest
    @MethodSource("hearingTimeCases")
    void testGetHearingTime(String selectedCode, String expectedTime) {
        Arrays.asList(true, false).forEach(singleInterimHearing -> {
            // Arrange
            FinremCaseData caseData = spy(FinremCaseData.class);
            DynamicListElement selected = mock(DynamicListElement.class);
            when(selected.getCode()).thenReturn(selectedCode);

            // Mocking the data structure
            caseData.setHearingTime("09:00 AM");
            // Mocking the data structure
            if (singleInterimHearing) {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingTime("10:30 AM").build())
                            .build()
                    ))
                    .build());
            } else {
                caseData.setInterimWrapper(InterimWrapper.builder()
                    .interimHearings(List.of(
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingTime("10:30 AM").build())
                            .build(),
                        InterimHearingCollection.builder()
                            .id(UUID.fromString("12000000-0000-0000-0000-000000000000"))
                            .value(InterimHearingItem.builder().interimHearingTime("11:30 AM").build())
                            .build()
                    ))
                    .build());
            }

            // Act
            String result = hearingService.getHearingTime(caseData, selected);

            // Assert
            assertEquals(expectedTime, result);
        });
    }
}
