package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService.TOP_LEVEL_HEARING_ID;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @InjectMocks
    private HearingService hearingService;

    @ParameterizedTest
    @MethodSource("hearingCases")
    void generateSelectableHearingsAsDynamicList(HearingTypeDirection topLevelHearingType,
                                                 LocalDate topLevelHearingDate,
                                                 String topLevelHearingTime,
                                                 List<InterimHearingCollection> interimHearings,
                                                 DynamicList expectedDynamicList) {
        testGenerateSelectableHearingsAsDynamicList(topLevelHearingType, topLevelHearingDate, topLevelHearingTime,
            interimHearings, null, null, expectedDynamicList);
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // UUID for interim hearing
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "1 Feb 2024 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)");
                        // UUID for interim hearing with invalid time
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 Invalid Time - Directions (DIR)");
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
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // UUID for interim hearing 2
                        put("00000000-0000-0000-0000-000000000000", "1 Mar 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
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
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 1), "2:00 AM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Final Hearing (FH)");
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
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
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 (unknown) - Directions (DIR)"); // Interim hearing with null time
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
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
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
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.MPS, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)"); // UUID for top-level hearing
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // UUID for interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Maintenance Pending Suit (MPS)");
                    }
                })
            ),
            // Case 12: Null Top-Level Hearing Type with Valid Interim Hearings
            Arguments.of(

                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM", // Example time for top-level hearing
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)");
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
                    }
                })
            )
        );
    }

    @ParameterizedTest
    @MethodSource("hearingCasesWithHearingsCreatedFromProcessOrderEvent")
    void generateSelectableHearingsAsDynamicListFromProcessOrder(HearingTypeDirection topLevelHearingType,
                                                 LocalDate topLevelHearingDate,
                                                 String topLevelHearingTime,
                                                 List<InterimHearingCollection> interimHearings,
                                                 List<DirectionDetailCollection> directionDetailCollection,
                                                 DynamicList expectedDynamicList) {
        testGenerateSelectableHearingsAsDynamicList(topLevelHearingType, topLevelHearingDate, topLevelHearingTime,
            interimHearings, directionDetailCollection, null, expectedDynamicList);
    }

    static Stream<Arguments> hearingCasesWithHearingsCreatedFromProcessOrderEvent() {
        return Stream.of(
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM", // Example time for top-level hearing
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                List.of(
                    createEmptyDirectionDetailCollection()
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)");
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
                    }
                })
            ),
            Arguments.of(
                HearingTypeDirection.FH,
                LocalDate.of(2024, 1, 1), // Example date for top-level hearing
                "10:00 AM", // Example time for top-level hearing
                List.of(
                    createInterimHearing("00000000-0000-0000-0000-000000000002", InterimTypeOfHearing.DIR, LocalDate.of(2024, 2, 1), "2:00 AM"),
                    createInterimHearing("00000000-0000-0000-0000-000000000003", InterimTypeOfHearing.FH, LocalDate.of(2024, 2, 2), "4:00 PM")
                ),
                List.of(
                    createDirectionDetailCollection("00000000-1111-0000-0000-000000000001", HearingTypeDirection.DIR,
                        LocalDate.of(2024, 5, 1), "23:00")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "1 Jan 2024 10:00 AM - Final Hearing (FH)");
                        put("00000000-0000-0000-0000-000000000002", "1 Feb 2024 2:00 AM - Directions (DIR)"); // Interim hearing 1
                        put("00000000-0000-0000-0000-000000000003", "2 Feb 2024 4:00 PM - Final Hearing (FH)"); // Interim hearing 2
                        put("00000000-1111-0000-0000-000000000001", "1 May 2024 23:00 - Directions (DIR)"); // Hearing created from Process Order
                    }
                })
            )
        );
    }

    private void testGenerateSelectableHearingsAsDynamicList(HearingTypeDirection topLevelHearingType,
                                                     LocalDate topLevelHearingDate,
                                                     String topLevelHearingTime,
                                                     List<InterimHearingCollection> interimHearings,
                                                     List<DirectionDetailCollection> directionDetailCollection,
                                                     List<HearingDirectionDetailsCollection> additionalHearings,
                                                     DynamicList expectedDynamicList) {
        // Arrange
        FinremCaseData.FinremCaseDataBuilder caseDataBuilder = FinremCaseData.builder()
            .interimWrapper(InterimWrapper.builder().interimHearings(interimHearings).build());

        caseDataBuilder.listForHearingWrapper(ListForHearingWrapper.builder()
            .hearingType(topLevelHearingType)
            .hearingDate(topLevelHearingDate)
            .hearingTime(topLevelHearingTime)
            .build());
        caseDataBuilder.directionDetailsCollection(directionDetailCollection);
        caseDataBuilder.hearingDirectionDetailsCollection(additionalHearings);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = caseDataBuilder.build();
        when(caseDetails.getData()).thenReturn(caseData);

        // Act
        DynamicList dynamicList = hearingService.generateSelectableHearingsAsDynamicList(caseDetails);

        // Assert
        assertEquals(expectedDynamicList.getListItems().size(), dynamicList.getListItems().size());
        assertDynamicListEquals(expectedDynamicList, dynamicList);
    }

    @ParameterizedTest
    @MethodSource("additionalHearings")
    void generateSelectableAdditionalHearingsAsDynamicList(HearingTypeDirection topLevelHearingType,
                                                           LocalDate topLevelHearingDate,
                                                           String topLevelHearingTime,
                                                           List<InterimHearingCollection> interimHearings,
                                                           List<DirectionDetailCollection> directionDetailCollection,
                                                           List<HearingDirectionDetailsCollection> additionalHearings,
                                                           DynamicList expectedDynamicList) {
        testGenerateSelectableHearingsAsDynamicList(topLevelHearingType, topLevelHearingDate, topLevelHearingTime,
            interimHearings, directionDetailCollection, additionalHearings, expectedDynamicList);
    }

    private static Stream<Arguments> additionalHearings() {
        return Stream.of(
            // Single additional hearing
            Arguments.of(
                null,
                null,
                null,
                null,
                null,
                List.of(createAdditionalHearing("00000000-1234-0000-0000-000000000003",
                    HearingTypeDirection.FH, LocalDate.of(2025, 3,27), "09:45")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-1234-0000-0000-000000000003", "27 Mar 2025 09:45 - Final Hearing (FH)");
                    }
                })
            ),
            // Multiple additional hearings
            Arguments.of(
                null,
                null,
                null,
                null,
                null,
                List.of(
                    createAdditionalHearing("00000000-1234-0000-0000-000000000003", HearingTypeDirection.FH, LocalDate.of(2025, 3,27), "09:45"),
                    createAdditionalHearing("00000000-0000-1000-0000-000000000005", HearingTypeDirection.DIR, LocalDate.of(2025, 1,21), "14:00"),
                    createAdditionalHearing("00000032-1234-0000-0100-000000000027", HearingTypeDirection.FDA, LocalDate.of(2024, 2,12), "10:00")

                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000032-1234-0000-0100-000000000027", "12 Feb 2024 10:00 - First Directions Appointment (FDA)");
                        put("00000000-0000-1000-0000-000000000005", "21 Jan 2025 14:00 - Directions (DIR)");
                        put("00000000-1234-0000-0000-000000000003", "27 Mar 2025 09:45 - Final Hearing (FH)");
                    }
                })
            ),
            // Single additional hearing with another type of hearing
            Arguments.of(
                HearingTypeDirection.FDA,
                LocalDate.of(2023, 10, 13),
                "10:00 AM",
                null,
                null,
                List.of(createAdditionalHearing("00000000-1234-0000-0000-000000000003",
                    HearingTypeDirection.FH, LocalDate.of(2025, 3,27), "09:45")
                ),
                createExpectedDynamicList(new LinkedHashMap<>() {
                    {
                        put("00000000-0000-0000-0000-000000000000", "13 Oct 2023 10:00 AM - First Directions Appointment (FDA)");
                        put("00000000-1234-0000-0000-000000000003", "27 Mar 2025 09:45 - Final Hearing (FH)");
                    }
                })
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideShouldThrowIllegalStateExceptionData")
    void shouldThrowIllegalStateException(HearingTypeDirection topLevelHearingType,
                                          List<InterimHearingCollection> interimHearings,
                                          List<DirectionDetailCollection> directionDetailCollection) {
        FinremCaseData.FinremCaseDataBuilder caseDataBuilder = FinremCaseData.builder()
            .interimWrapper(InterimWrapper.builder().interimHearings(interimHearings).build());

        caseDataBuilder.listForHearingWrapper(ListForHearingWrapper.builder()
            .hearingType(topLevelHearingType)
            .hearingDate(LocalDate.of(2025, 1, 1))
            .hearingTime("anyHearingTimeString")
            .build());
        caseDataBuilder.directionDetailsCollection(directionDetailCollection);

        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = caseDataBuilder.build();
        when(caseDetails.getData()).thenReturn(caseData);

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> hearingService.generateSelectableHearingsAsDynamicList(caseDetails));
        assertEquals("hearingType is unexpectedly null", e.getMessage());
    }

    static Stream<Arguments> provideShouldThrowIllegalStateExceptionData() {
        return Stream.of(
            Arguments.of(null, List.of(), List.of()),
            Arguments.of(HearingTypeDirection.FH,
                List.of(createInterimHearing("00000000-0000-0000-0000-000000000002", null, LocalDate.of(2024, 2, 1), "2:00 AM")),
                List.of()),
            Arguments.of(HearingTypeDirection.FH,
                List.of(),
                List.of(
                    createDirectionDetailCollection("00000000-1111-0000-0000-000000000001", null, LocalDate.of(2024, 5, 1), "23:00")
                ))
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
            caseData.getListForHearingWrapper().setHearingDate(LocalDate.of(2024, 10, 21));
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

    static Stream<Arguments> hearingDateCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, LocalDate.of(2024, 10, 21)), // Top-level hearing
            Arguments.of("11000000-0000-0000-0000-000000000000", LocalDate.of(2024, 10, 22)), // Valid interim hearing date
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
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
            caseData.getListForHearingWrapper().setHearingType(HearingTypeDirection.FH);
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

    static Stream<Arguments> hearingTypeCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, HearingTypeDirection.FH.getId()), // Top-level hearing type
            Arguments.of("11000000-0000-0000-0000-000000000000", InterimTypeOfHearing.DIR.getId()), // Valid interim hearing type
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
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
            caseData.getListForHearingWrapper().setHearingTime("09:00 AM");
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

    static Stream<Arguments> hearingTimeCases() {
        return Stream.of(
            Arguments.of("", null), // Edge case: empty selected code
            Arguments.of(TOP_LEVEL_HEARING_ID, "09:00 AM"), // Top-level hearing time
            Arguments.of("11000000-0000-0000-0000-000000000000", "10:30 AM"), // Valid interim hearing time
            Arguments.of("22000000-0000-0000-0000-000000000000", null), // Non-matching ID
            Arguments.of(null, null) // Case with null value in Interim Hearing
        );
    }

    @Test
    void testGetHearingInfosFromHearingsCreatedFromProcessOrder() {
        String selectedCode = "11000000-0000-0000-0000-000000000000";

        LocalDate expectedDate = LocalDate.of(2024, 10, 22);
        HearingTypeDirection expectedType = HearingTypeDirection.FDA;
        String expectedTime = "9999";

        Arrays.asList(true, false).forEach(isSingleEntry -> {
            // Arrange
            FinremCaseData caseData = spy(FinremCaseData.class);
            DynamicListElement selected = mock(DynamicListElement.class);
            when(selected.getCode()).thenReturn(selectedCode);

            caseData.setDirectionDetailsCollection(new ArrayList<>());
            caseData.getDirectionDetailsCollection().add(
                DirectionDetailCollection.builder()
                    .id(UUID.fromString(selectedCode))
                    .value(DirectionDetail.builder().dateOfHearing(expectedDate).typeOfHearing(expectedType).hearingTime(expectedTime).build())
                    .build())
            ;
            if (!isSingleEntry) {
                caseData.getDirectionDetailsCollection().add(DirectionDetailCollection.builder()
                    .id(UUID.randomUUID())
                    .value(DirectionDetail.builder().dateOfHearing(LocalDate.now()).typeOfHearing(HearingTypeDirection.DIR).hearingTime("XX").build())
                    .build());
            }

            assertEquals(expectedDate, hearingService.getHearingDate(caseData, selected));
            assertEquals(expectedType.getId(), hearingService.getHearingType(caseData, selected));
            assertEquals(expectedTime, hearingService.getHearingTime(caseData, selected));
        });
    }

    @Test
    void testGetHearingInfosFromAdditionalHearings() {
        FinremCaseData caseData = FinremCaseData.builder()
            .hearingDirectionDetailsCollection(List.of(
                HearingDirectionDetailsCollection.builder()
                    .id(UUID.fromString("11000000-0000-0000-0000-000000000000"))
                    .value(HearingDirectionDetail.builder()
                        .isAnotherHearingYN(YesOrNo.YES)
                        .typeOfHearing(HearingTypeDirection.FDA)
                        .dateOfHearing(LocalDate.of(2024, 10, 22))
                        .hearingTime("15:27")
                        .build())
                    .build()
            ))
            .build();

        DynamicListElement selected = mock(DynamicListElement.class);
        when(selected.getCode()).thenReturn("11000000-0000-0000-0000-000000000000");

        assertEquals(LocalDate.of(2024, 10, 22), hearingService.getHearingDate(caseData, selected));
        assertEquals(HearingTypeDirection.FDA.getId(), hearingService.getHearingType(caseData, selected));
        assertEquals("15:27", hearingService.getHearingTime(caseData, selected));
    }

    @ParameterizedTest
    @CsvSource({
        "'Civil', '2024-11-10', '10:30 AM', 'Civil on 10 November 2024 10:30 AM'",
        "NULL, '2024-11-10', '10:30 AM', 'N/A on 10 November 2024 10:30 AM'",
        "'Civil', NULL, '10:30 AM', 'Civil on N/A 10:30 AM'",
        "'Civil', '2024-11-10', NULL, 'Civil on 10 November 2024 N/A'",
        "'Civil', '2024-11-10', '10:30 AM', 'Civil on 10 November 2024 10:30 AM'",
        "NULL, NULL, NULL, 'N/A on N/A N/A'",
        "'', '2024-11-10', '', ' on 10 November 2024 '"
    })
    void formatHearingInfo_shouldReturnExpectedOutput(String hearingType, String hearingDate, String hearingTime, String expectedOutput) {
        LocalDate parsedHearingDate = "NULL".equals(hearingDate) ? null : LocalDate.parse(hearingDate);
        String parsedHearingType = "NULL".equals(hearingType) ? null : hearingType;
        String parsedHearingTime = "NULL".equals(hearingTime) ? null : hearingTime;

        String actualOutput = hearingService.formatHearingInfo(parsedHearingType, parsedHearingDate, parsedHearingTime);
        assertEquals(expectedOutput, actualOutput);
    }

    static DirectionDetailCollection createEmptyDirectionDetailCollection() {
        DirectionDetail directionDetail = DirectionDetail.builder()
            .isAnotherHearingYN(YesOrNo.NO)
            .build();
        return DirectionDetailCollection.builder()
            .id(UUID.fromString("00000000-1111-0000-0000-000000000001"))
            .value(directionDetail)
            .build();
    }

    static DirectionDetailCollection createDirectionDetailCollection(String id, HearingTypeDirection type, LocalDate date, String time) {
        DirectionDetail directionDetail = DirectionDetail.builder()
            .dateOfHearing(date)
            .hearingTime(time)
            .typeOfHearing(type)
            .isAnotherHearingYN(YesOrNo.YES)
            .build();
        return DirectionDetailCollection.builder()
            .id(UUID.fromString(id))
            .value(directionDetail)
            .build();
    }

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

    static HearingDirectionDetailsCollection createAdditionalHearing(String id, HearingTypeDirection typeOfHearing,
                                                                     LocalDate hearingDate, String hearingTime) {
        return HearingDirectionDetailsCollection.builder()
            .id(UUID.fromString(id))
            .value(HearingDirectionDetail.builder()
                .isAnotherHearingYN(YesOrNo.YES)
                .hearingTime(hearingTime)
                .dateOfHearing(hearingDate)
                .typeOfHearing(typeOfHearing)
                .build())
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
