package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;

@ExtendWith(MockitoExtension.class)
class DraftOrderServiceTest {

    private static final DynamicRadioList UPLOAD_PARTY = DynamicRadioList.builder()
        .value(DynamicRadioListElement.builder().code(UPLOAD_PARTY_APPLICANT).build())
        .build();

    @InjectMocks
    private DraftOrderService draftOrderService;

    @Mock
    private IdamAuthService idamAuthService;

    @Mock
    private HearingService hearingService;

    @TestLogs
    private final TestLogger logs = new TestLogger(DraftOrderService.class);

    @ParameterizedTest
    @CsvSource({
        "'orders,pensionSharingAnnexes', true",
        "'pensionSharingAnnexes,UNKNOWN', false",
        "'orders', true",
        "'', false",
        "null, false"
    })
    void testIsOrdersSelected(String input, boolean expectedResult) {
        List<String> uploadOrdersOrPsas = input == null ? null : List.of(input.split(","));
        boolean result = draftOrderService.isOrdersSelected(uploadOrdersOrPsas);
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
        "'pensionSharingAnnexes,orders', true",
        "'orders,UNKNOWN', false",
        "'pensionSharingAnnexes', true",
        "'', false",
        "null, false"
    })
    void testIsPsaSelected(String input, boolean expectedResult) {
        List<String> uploadOrdersOrPsas = input == null ? null : List.of(input.split(","));
        boolean result = draftOrderService.isPsaSelected(uploadOrdersOrPsas);
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("provideAgreedDraftOrderTestCases")
    void testProcessAgreedDraftOrders(UploadAgreedDraftOrder uploadAgreedDraftOrder,
                                      List<AgreedDraftOrder> expectedOrders,
                                      boolean havingMissingUploadPartyLog) {
        // Mock dependencies
        lenient().when(idamAuthService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().name("<SUBMITTED_BY>").build());

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 10, 18, 10, 0);
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // Call the method
            List<AgreedDraftOrderCollection> result = draftOrderService.processAgreedDraftOrders(uploadAgreedDraftOrder, AUTH_TOKEN);

            assertEquals(expectedOrders.size(), result.size());

            // Additional assertions for expected properties
            for (AgreedDraftOrderCollection actualOrderCollection : result) {
                AgreedDraftOrder actualOrder = actualOrderCollection.getValue();

                // Check if there's any expected order that matches the actual order, accounting for potential nulls
                boolean matches = expectedOrders.stream().anyMatch(expectedOrder ->
                    Objects.equals(expectedOrder.getOrderStatus(), actualOrder.getOrderStatus())
                        && Objects.equals(expectedOrder.getDraftOrder(), actualOrder.getDraftOrder())
                        && Objects.equals(expectedOrder.getResubmission(), actualOrder.getResubmission())
                        && Objects.equals(expectedOrder.getSubmittedBy(), actualOrder.getSubmittedBy())
                        && Objects.equals(expectedOrder.getSubmittedDate(), actualOrder.getSubmittedDate())
                        && Objects.equals(expectedOrder.getUploadedOnBehalfOf(), actualOrder.getUploadedOnBehalfOf())
                        && Objects.equals(expectedOrder.getAttachments(), actualOrder.getAttachments())
                        && Objects.equals(expectedOrder.getPensionSharingAnnex(), actualOrder.getPensionSharingAnnex())
                );

                assertTrue(matches, "No matching expected order found for: " + actualOrder);
            }

            if (havingMissingUploadPartyLog) {
                assertThat(logs.getErrors()).containsExactly("Unexpected null 'uploadedParty' on upload agreed order journey.");
            }
        }
    }

    private static Stream<Arguments> provideAgreedDraftOrderTestCases() {
        // Case 1: One draft order with one attachment
        CaseDocument draftOrder1 = CaseDocument.builder().build();
        CaseDocument attachment1 = CaseDocument.builder().build();

        AgreedDraftOrder expectedOrder1 = AgreedDraftOrder.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .draftOrder(draftOrder1)
            .resubmission(YesOrNo.YES)
            .submittedBy("<SUBMITTED_BY>")
            .submittedDate(LocalDateTime.of(2024, 10, 18, 10, 0))
            .uploadedOnBehalfOf(UPLOAD_PARTY_APPLICANT)
            .attachments(List.of(CaseDocumentCollection.builder().value(attachment1).build()))
            .build();

        UploadAgreedDraftOrder uploadOrder1 = UploadAgreedDraftOrder.builder()
            .uploadAgreedDraftOrderCollection(List.of(
                UploadAgreedDraftOrderCollection.builder()
                    .value(UploadedDraftOrder.builder()
                        .resubmission(List.of(YesOrNo.YES.getYesOrNo()))
                        .agreedDraftOrderDocument(draftOrder1)
                        .agreedDraftOrderAdditionalDocumentsCollection(List.of(
                            AgreedDraftOrderAdditionalDocumentsCollection.builder()
                                .value(attachment1)
                                .build()
                        ))
                        .build())
                    .build()))
            .agreedPsaCollection(List.of())
            .build();

        // Case 2: No documents provided
        UploadAgreedDraftOrder uploadOrder2 = UploadAgreedDraftOrder.builder()
            .uploadAgreedDraftOrderCollection(List.of())
            .agreedPsaCollection(List.of())
            .build();

        // Case 3: One draft order with multiple attachments
        CaseDocument attachment2 = CaseDocument.builder().build();
        AgreedDraftOrder expectedOrder3 = AgreedDraftOrder.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .draftOrder(draftOrder1)
            .resubmission(YesOrNo.YES)
            .submittedBy("<SUBMITTED_BY>")
            .submittedDate(LocalDateTime.of(2024, 10, 18, 10, 0))
            .uploadedOnBehalfOf(UPLOAD_PARTY_APPLICANT)
            .attachments(List.of(
                CaseDocumentCollection.builder().value(attachment1).build(),
                CaseDocumentCollection.builder().value(attachment2).build()
            ))
            .build();

        UploadAgreedDraftOrder uploadOrder3 = UploadAgreedDraftOrder.builder()
            .uploadAgreedDraftOrderCollection(List.of(
                UploadAgreedDraftOrderCollection.builder()
                    .value(UploadedDraftOrder.builder()
                        .resubmission(List.of(YesOrNo.YES.getYesOrNo()))
                        .agreedDraftOrderDocument(draftOrder1)
                        .agreedDraftOrderAdditionalDocumentsCollection(List.of(
                            AgreedDraftOrderAdditionalDocumentsCollection.builder()
                                .value(attachment1)
                                .build(),
                            AgreedDraftOrderAdditionalDocumentsCollection.builder()
                                .value(attachment2)
                                .build()
                        ))
                        .build())
                    .build()))
            .agreedPsaCollection(List.of())
            .build();

        // Case 4: One draft order with a pension sharing annex
        CaseDocument psa1 = CaseDocument.builder().build();
        AgreedDraftOrder expectedOrder4 = AgreedDraftOrder.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .draftOrder(draftOrder1)
            .resubmission(YesOrNo.NO)
            .submittedBy("<SUBMITTED_BY>")
            .submittedDate(LocalDateTime.of(2024, 10, 18, 10, 0))
            .uploadedOnBehalfOf(UPLOAD_PARTY_APPLICANT)
            .build();

        AgreedDraftOrder expectedOrder5 = AgreedDraftOrder.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedBy("<SUBMITTED_BY>")
            .submittedDate(LocalDateTime.of(2024, 10, 18, 10, 0))
            .uploadedOnBehalfOf(UPLOAD_PARTY_APPLICANT)
            .pensionSharingAnnex(psa1)
            .build();

        UploadAgreedDraftOrder uploadOrder4 = UploadAgreedDraftOrder.builder()
            .uploadAgreedDraftOrderCollection(List.of(
                UploadAgreedDraftOrderCollection.builder()
                    .value(UploadedDraftOrder.builder()
                        .resubmission(List.of(YesOrNo.NO.getYesOrNo()))
                        .agreedDraftOrderDocument(draftOrder1)
                        .build())
                    .build()))
            .agreedPsaCollection(List.of(
                AgreedPensionSharingAnnexCollection.builder()
                    .value(AgreedPensionSharingAnnex.builder()
                        .agreedPensionSharingAnnexes(psa1)
                        .build())
                    .build()))
            .build();

        // Case 5
        UploadAgreedDraftOrder uploadOrder5 = UploadAgreedDraftOrder.builder()
            .uploadAgreedDraftOrderCollection(List.of(
                UploadAgreedDraftOrderCollection.builder()
                    .value(UploadedDraftOrder.builder()
                        .resubmission(List.of(YesOrNo.YES.getYesOrNo()))
                        .agreedDraftOrderDocument(draftOrder1)
                        .build())
                    .build()))
            .agreedPsaCollection(List.of(
                AgreedPensionSharingAnnexCollection.builder()
                    .value(AgreedPensionSharingAnnex.builder()
                        .agreedPensionSharingAnnexes(psa1)
                        .build())
                    .build()))
            .build();

        return Stream.of(
            // Case 1: One draft order with one attachment
            Arguments.of(uploadOrder1.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(expectedOrder1),
                false),
            Arguments.of(uploadOrder1.toBuilder().uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            Arguments.of(uploadOrder1.toBuilder().uploadOrdersOrPsas(List.of(PSA_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            // Case 2: No documents provided
            Arguments.of(uploadOrder2.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            Arguments.of(uploadOrder2.toBuilder().uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            Arguments.of(uploadOrder2.toBuilder().uploadOrdersOrPsas(List.of(PSA_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            // Case 3: One draft order with multiple attachments
            Arguments.of(uploadOrder3.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(expectedOrder3),
                false),
            Arguments.of(uploadOrder3.toBuilder().uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            Arguments.of(uploadOrder3.toBuilder().uploadOrdersOrPsas(List.of(PSA_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            // Case 4: One draft order with a pension sharing annex
            Arguments.of(uploadOrder4.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE)).uploadParty(UPLOAD_PARTY).build(), List.of(expectedOrder4),
                false),
            Arguments.of(uploadOrder4.toBuilder().uploadParty(UPLOAD_PARTY).build(), List.of(), false),
            Arguments.of(uploadOrder4.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE, PSA_TYPE)).uploadParty(UPLOAD_PARTY).build(),
                List.of(expectedOrder4, expectedOrder5), false),
            // Case 5: resubmission = YES
            Arguments.of(uploadOrder5.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE, PSA_TYPE)).uploadParty(UPLOAD_PARTY).build(),
                List.of(expectedOrder4.toBuilder().resubmission(YesOrNo.YES).build(), expectedOrder5), false),
            // Case 6:
            Arguments.of(uploadOrder1.toBuilder().uploadOrdersOrPsas(List.of(ORDER_TYPE)).uploadParty(null).build(),
                List.of(expectedOrder1.toBuilder().uploadedOnBehalfOf(null).build()), true)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true", // Valid input with non-null hearing details and judge
        "false, true, false", // Null hearing details
        "true, false, false" // Null judge
    })
    @DisplayName("Should populate draft orders review collection based on hearingDetails and judge presence")
    void shouldPopulateDraftOrdersReviewCollectionBasedOnHearingDetailsAndJudgePresence(
        boolean hasHearingDetails,
        boolean hasJudge,
        boolean shouldPopulate) {

        UploadAgreedDraftOrder uploadAgreedDraftOrder = new UploadAgreedDraftOrder();

        // Mock hearingDetails and judge based on parameters
        DynamicList hearingDetails = hasHearingDetails ? DynamicList.builder().build() : null;
        uploadAgreedDraftOrder.setHearingDetails(hearingDetails);
        uploadAgreedDraftOrder.setJudge(hasJudge ? "Judge Name" : null);

        lenient().when(hearingService.getHearingType(any(), any())).thenReturn("Some Hearing Type");
        lenient().when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now());
        lenient().when(hearingService.getHearingTime(any(), any())).thenReturn("10:00 AM");

        FinremCaseData finremCaseData = FinremCaseData.builder().ccdCaseId(TestConstants.CASE_ID).build();
        List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection = List.of(
            AgreedDraftOrderCollection.builder()
                .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build())
                .build(),
            AgreedDraftOrderCollection.builder()
                .value(AgreedDraftOrder.builder().pensionSharingAnnex(CaseDocument.builder().build()).build())
                .build()
        );

        // Call the method
        if (!hasHearingDetails || !hasJudge) {
            Exception exception = assertThrows(InvalidCaseDataException.class, () ->
                draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, newAgreedDraftOrderCollection));
            String expectedMessage = !hasHearingDetails
                ? ("Unexpected null hearing details for Case ID: " + TestConstants.CASE_ID)
                : ("Unexpected null judge for Case ID: " + TestConstants.CASE_ID);
            String actualMessage = exception.getMessage();
            assertEquals(expectedMessage, actualMessage);
        } else {
            draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, newAgreedDraftOrderCollection);
        }

        // Assert based on expected behavior
        if (shouldPopulate) {
            assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection())
                .isNotEmpty();

            DraftOrdersReview populatedReview = finremCaseData.getDraftOrdersWrapper()
                .getDraftOrdersReviewCollection().get(0).getValue();

            assertThat(populatedReview.getHearingType()).isEqualTo("Some Hearing Type");
            assertThat(populatedReview.getHearingDate()).isEqualTo(LocalDate.now());
            assertThat(populatedReview.getHearingTime()).isEqualTo("10:00 AM");
            assertThat(populatedReview.getHearingJudge()).isEqualTo("Judge Name");

            // Further assertions for the draft order and PSA doc collections if needed
            assertThat(populatedReview.getDraftOrderDocReviewCollection()).hasSize(1);
            assertThat(populatedReview.getPsaDocReviewCollection()).hasSize(1);
        } else {
            assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection())
                .isNullOrEmpty();
        }

        // Verify interactions
        if (shouldPopulate) {
            verify(hearingService).getHearingType(any(), any());
            verify(hearingService).getHearingDate(any(), any());
            verify(hearingService).getHearingTime(any(), any());
        } else {
            verify(hearingService, never()).getHearingType(any(), any());
            verify(hearingService, never()).getHearingDate(any(), any());
            verify(hearingService, never()).getHearingTime(any(), any());
        }
    }

    @ParameterizedTest
    @MethodSource("provideAgreedDraftOrderCollections")
    @DisplayName("Should correctly populate DraftOrdersReview properties in FinremCaseData with varying numbers of AgreedDraftOrderCollection")
    void shouldPopulateDraftOrdersReviewPropertiesWithVaryingNumbers(
        List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection,
        DraftOrdersReview expectedDraftOrdersReview) {

        // Arrange
        UploadAgreedDraftOrder uploadAgreedDraftOrder = new UploadAgreedDraftOrder();

        DynamicListElement selected = DynamicListElement.builder().build();
        DynamicList hearingDetails = DynamicList.builder().value(selected).build();
        uploadAgreedDraftOrder.setHearingDetails(hearingDetails);
        uploadAgreedDraftOrder.setJudge(expectedDraftOrdersReview.getHearingJudge());

        // Mocking the service methods to return specific values
        when(hearingService.getHearingType(any(), any())).thenReturn(expectedDraftOrdersReview.getHearingType());
        when(hearingService.getHearingDate(any(), any())).thenReturn(expectedDraftOrdersReview.getHearingDate());
        when(hearingService.getHearingTime(any(), any())).thenReturn(expectedDraftOrdersReview.getHearingTime());

        FinremCaseData finremCaseData = new FinremCaseData();

        // Act
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, newAgreedDraftOrderCollection);

        // Assert
        assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).isNotEmpty();

        DraftOrdersReview populatedReview = finremCaseData.getDraftOrdersWrapper()
            .getDraftOrdersReviewCollection().get(0).getValue();

        // Verifying the populated properties using recursive comparison
        assertThat(populatedReview)
            .usingRecursiveComparison()
            .isEqualTo(expectedDraftOrdersReview);

        // Verify interaction with mocked service
        verify(hearingService).getHearingType(finremCaseData, selected);
        verify(hearingService).getHearingDate(finremCaseData, selected);
        verify(hearingService).getHearingTime(finremCaseData, selected);
    }

    static Stream<Arguments> provideAgreedDraftOrderCollections() {
        return Stream.of(
            // Case 1: Empty list, no draft orders or PSA orders
            Arguments.of(List.of(),
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of())  // No draft orders
                    .psaDocReviewCollection(List.of())         // No PSA orders
                    .build()),

            // Case 2: Single draft order
            Arguments.of(List.of(
                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .draftOrder(CaseDocument.builder().build())
                            .build())
                        .build()),
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build()))
                    .psaDocReviewCollection(List.of())  // No PSA orders
                    .build()),

            // Case 3: Single PSA order
            Arguments.of(List.of(
                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .pensionSharingAnnex(CaseDocument.builder().build())
                            .build())
                        .build()),
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of())  // No draft orders
                    .psaDocReviewCollection(List.of(
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .psaDocument(CaseDocument.builder().build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build()))
                    .build()),

            // Case 4: Both draft and PSA orders
            Arguments.of(List.of(
                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .draftOrder(CaseDocument.builder().build())
                            .build())
                        .build(),
                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .pensionSharingAnnex(CaseDocument.builder().build())
                            .build())
                        .build()),
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build()))
                    .psaDocReviewCollection(List.of(
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .psaDocument(CaseDocument.builder().build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build()))
                    .build()),

            // Case 5: null newAgreedDraftOrderCollection entry
            Arguments.of(
                Collections.singletonList((AgreedDraftOrderCollection) null), // Null entry in list
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of())  // No draft orders due to null entry
                    .psaDocReviewCollection(List.of())         // No PSA orders due to null entry
                    .build()),

            // Case 6: multiple draft orders should be grouped together
            Arguments.of(List.of(
                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .draftOrder(CaseDocument.builder().documentUrl("NEW_DOC1.doc").build())
                            .build())
                        .build(),

                    AgreedDraftOrderCollection.builder()
                        .value(AgreedDraftOrder.builder()
                            .orderStatus(OrderStatus.TO_BE_REVIEWED)
                            .submittedBy("Mr ABC")
                            .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                            .resubmission(YesOrNo.NO)
                            .uploadedOnBehalfOf("theApplicant")
                            .draftOrder(CaseDocument.builder().documentUrl("NEW_DOC2.doc").build())
                            .build())
                        .build()),
                DraftOrdersReview.builder()
                    .hearingType("First Directions Appointment (FDA)")
                    .hearingDate(LocalDate.of(2024, 10, 21))
                    .hearingTime("2:00 PM")
                    .hearingJudge("Judge Name")
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().documentUrl("NEW_DOC1.doc").build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build(),
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().documentUrl("NEW_DOC2.doc").build())
                                .hearingType("First Directions Appointment (FDA)")
                                .build())
                            .build()
                    ))
                    .psaDocReviewCollection(List.of())  // No PSA orders
                    .build())
        );
    }

    @Test
    void shouldAppendNewDraftOrderToExistingDraftOrdersReview() {
        // Arrange
        UploadAgreedDraftOrder uploadAgreedDraftOrder = new UploadAgreedDraftOrder();

        DynamicListElement selected = DynamicListElement.builder().build();
        DynamicList hearingDetails = DynamicList.builder().value(selected).build();
        uploadAgreedDraftOrder.setHearingDetails(hearingDetails);
        uploadAgreedDraftOrder.setJudge("Mr Justice");

        // Mocking the service methods to return specific values
        when(hearingService.getHearingType(any(), any())).thenReturn("Existing Hearing Type");
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.of(2024, 10, 20));
        when(hearingService.getHearingTime(any(), any())).thenReturn("1:00 PM");

        // Existing DraftOrdersReview to be kept
        DraftOrderDocReviewCollection existingDraftOrderDocReviewCollection =
            DraftOrderDocReviewCollection.builder()
                .value(DraftOrderDocumentReview.builder()
                    .orderStatus(OrderStatus.APPROVED_BY_JUDGE)
                    .submittedBy("Existing User")
                    .submittedDate(LocalDateTime.of(2024, 10, 10, 12, 0))
                    .approvalJudge("Approved Judge")
                    .approvalDate(LocalDateTime.of(2024, 10, 10, 12, 0))
                    .build())
                .build();
        DraftOrdersReview existingDraftOrderReview = DraftOrdersReview.builder()
            .hearingType("Existing Hearing Type")
            .hearingDate(LocalDate.of(2024, 10, 20))
            .hearingTime("1:00 PM")
            .hearingJudge("Mr Justice")
            .draftOrderDocReviewCollection(new ArrayList<>(List.of(existingDraftOrderDocReviewCollection)))
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(new ArrayList<>(List.of(DraftOrdersReviewCollection.builder().value(existingDraftOrderReview).build())))
                .build())
            .build();

        // Act
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, List.of(
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder()
                        .orderStatus(OrderStatus.TO_BE_REVIEWED)
                        .submittedBy("Mr ABC")
                        .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                        .resubmission(YesOrNo.NO)
                        .uploadedOnBehalfOf("theApplicant")
                        .draftOrder(CaseDocument.builder().documentUrl("NEW_DO_1.doc").build())
                        .build())
                    .build(),
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder()
                        .orderStatus(OrderStatus.TO_BE_REVIEWED)
                        .submittedBy("Mr ABC")
                        .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                        .resubmission(YesOrNo.NO)
                        .uploadedOnBehalfOf("theApplicant")
                        .pensionSharingAnnex(CaseDocument.builder().documentUrl("NEW_PSA_1.doc").build())
                        .build())
                    .build()
            )
        );

        // Assert
        assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).isNotEmpty();
        assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).hasSize(1);

        // Check if new review is added as expected
        DraftOrdersReview populatedReview = finremCaseData.getDraftOrdersWrapper()
            .getDraftOrdersReviewCollection().get(0).getValue(); // This will depend on how your collection is ordered

        // Verifying the populated properties using recursive comparison
        assertThat(populatedReview)
            .usingRecursiveComparison()
            .isEqualTo(
                DraftOrdersReview.builder()
                    .hearingType("Existing Hearing Type")
                    .hearingDate(LocalDate.of(2024, 10, 20))
                    .hearingTime("1:00 PM")
                    .hearingJudge("Mr Justice")
                    .draftOrderDocReviewCollection(List.of(
                        existingDraftOrderDocReviewCollection,
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().documentUrl("NEW_DO_1.doc").build())
                                .hearingType("Existing Hearing Type")
                                .build())
                            .build()
                    ))
                    .psaDocReviewCollection(List.of(
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .psaDocument(CaseDocument.builder().documentUrl("NEW_PSA_1.doc").build())
                                .hearingType("Existing Hearing Type")
                                .build())
                            .build()))
                    .build());

        // Verify interaction with mocked service
        verify(hearingService).getHearingType(finremCaseData, selected);
        verify(hearingService).getHearingDate(finremCaseData, selected);
        verify(hearingService).getHearingTime(finremCaseData, selected);
    }

    @Test
    void shouldAppendNewPsaToExistingDraftOrdersReview() {
        // Arrange
        UploadAgreedDraftOrder uploadAgreedDraftOrder = new UploadAgreedDraftOrder();

        DynamicListElement selected = DynamicListElement.builder().build();
        DynamicList hearingDetails = DynamicList.builder().value(selected).build();
        uploadAgreedDraftOrder.setHearingDetails(hearingDetails);
        uploadAgreedDraftOrder.setJudge("Mr Justice");

        // Mocking the service methods to return specific values
        when(hearingService.getHearingType(any(), any())).thenReturn("Existing Hearing Type");
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.of(2024, 10, 20));
        when(hearingService.getHearingTime(any(), any())).thenReturn("1:00 PM");

        // Existing DraftOrdersReview to be kept
        PsaDocReviewCollection existingPsaDocReviewCollection =
            PsaDocReviewCollection.builder()
                .value(PsaDocumentReview.builder()
                    .orderStatus(OrderStatus.APPROVED_BY_JUDGE)
                    .submittedBy("Existing User")
                    .submittedDate(LocalDateTime.of(2024, 10, 10, 12, 0))
                    .approvalJudge("Approved Judge")
                    .approvalDate(LocalDateTime.of(2024, 10, 10, 12, 0))
                    .build())
                .build();
        DraftOrdersReview existingDraftOrderReview = DraftOrdersReview.builder()
            .hearingType("Existing Hearing Type")
            .hearingDate(LocalDate.of(2024, 10, 20))
            .hearingTime("1:00 PM")
            .hearingJudge("Mr Justice")
            .psaDocReviewCollection(new ArrayList<>(List.of(existingPsaDocReviewCollection)))
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(new ArrayList<>(List.of(DraftOrdersReviewCollection.builder().value(existingDraftOrderReview).build())))
                .build())
            .build();

        // Act
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, List.of(
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder()
                        .orderStatus(OrderStatus.TO_BE_REVIEWED)
                        .submittedBy("Mr ABC")
                        .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                        .resubmission(YesOrNo.NO)
                        .uploadedOnBehalfOf("theApplicant")
                        .draftOrder(CaseDocument.builder().documentUrl("NEW_DO_1.doc").build())
                        .build())
                    .build(),
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder()
                        .orderStatus(OrderStatus.TO_BE_REVIEWED)
                        .submittedBy("Mr ABC")
                        .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                        .resubmission(YesOrNo.NO)
                        .uploadedOnBehalfOf("theApplicant")
                        .pensionSharingAnnex(CaseDocument.builder().documentUrl("NEW_PSA_1.doc").build())
                        .build())
                    .build()
            )
        );

        // Assert
        assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).isNotEmpty();
        assertThat(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).hasSize(1);

        // Check if new review is added as expected
        DraftOrdersReview populatedReview = finremCaseData.getDraftOrdersWrapper()
            .getDraftOrdersReviewCollection().get(0).getValue(); // This will depend on how your collection is ordered

        // Verifying the populated properties using recursive comparison
        assertThat(populatedReview)
            .usingRecursiveComparison()
            .isEqualTo(
                DraftOrdersReview.builder()
                    .hearingType("Existing Hearing Type")
                    .hearingDate(LocalDate.of(2024, 10, 20))
                    .hearingTime("1:00 PM")
                    .hearingJudge("Mr Justice")
                    .draftOrderDocReviewCollection(List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 23, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .draftOrderDocument(CaseDocument.builder().documentUrl("NEW_DO_1.doc").build())
                                .hearingType("Existing Hearing Type")
                                .build())
                            .build()
                    ))
                    .psaDocReviewCollection(List.of(
                        existingPsaDocReviewCollection,
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder()
                                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                                .submittedBy("Mr ABC")
                                .submittedDate(LocalDateTime.of(2024, 10, 10, 22, 59))
                                .resubmission(YesOrNo.NO)
                                .uploadedOnBehalfOf("theApplicant")
                                .psaDocument(CaseDocument.builder().documentUrl("NEW_PSA_1.doc").build())
                                .hearingType("Existing Hearing Type")
                                .build())
                            .build()))
                    .build());

        // Verify interaction with mocked service
        verify(hearingService).getHearingType(finremCaseData, selected);
        verify(hearingService).getHearingDate(finremCaseData, selected);
        verify(hearingService).getHearingTime(finremCaseData, selected);
    }

    @ParameterizedTest
    @MethodSource("provideMultipleReviewsWithStatusTestCases")
    void testGetEarliestToBeReviewedOrderDate_withMultipleReviewsAndStatus(List<Optional<LocalDateTime>> draftDates,
                                                                           List<Optional<OrderStatus>> draftStatuses,
                                                                           List<Optional<LocalDateTime>> psaDates,
                                                                           List<Optional<OrderStatus>> psaStatuses,
                                                                           List<Optional<LocalDateTime>> draftNotificationDates,
                                                                           List<Optional<LocalDateTime>> psaNotificationDates,
                                                                           int expectedSituationForCase1, int expectedSituationForCase2) {
        List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection = new ArrayList<>();
        List<PsaDocReviewCollection> psaDocReviewCollection = new ArrayList<>();

        // Populate draft reviews
        for (int i = 0; i < draftDates.size(); i++) {
            DraftOrderDocumentReview reviewable = DraftOrderDocumentReview.builder()
                .submittedDate(draftDates.get(i).orElse(null))
                .orderStatus(draftStatuses.get(i).orElse(null))
                .notificationSentDate(draftNotificationDates.get(i).orElse(null))
                .build();

            DraftOrderDocReviewCollection draftCollection = new DraftOrderDocReviewCollection();
            draftCollection.setValue(reviewable);
            draftOrderDocReviewCollection.add(draftCollection);
        }

        // Populate PSA reviews
        for (int i = 0; i < psaDates.size(); i++) {
            PsaDocumentReview reviewable = PsaDocumentReview.builder()
                .submittedDate(psaDates.get(i).orElse(null))
                .orderStatus(psaStatuses.get(i).orElse(null))
                .notificationSentDate(psaNotificationDates.get(i).orElse(null))
                .build();

            PsaDocReviewCollection psaCollection = new PsaDocReviewCollection();
            psaCollection.setValue(reviewable);
            psaDocReviewCollection.add(psaCollection);
        }

        LocalDate fixedDate = LocalDate.of(2024, 10, 18);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(fixedDate);

            // Case 1: Single draftOrdersReviewCollection
            FinremCaseDetails caseDetails1 = FinremCaseDetailsBuilderFactory
                .from(Long.valueOf(TestConstants.CASE_ID), FinremCaseData.builder().draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        // may need a multiple
                        DraftOrdersReviewCollection.builder()
                            .value(DraftOrdersReview.builder()
                                .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
                                .psaDocReviewCollection(psaDocReviewCollection)
                                .build()
                            ).build()
                    )).build())).build();
            List<DraftOrdersReview> actual = draftOrderService.getDraftOrderReviewOverdue(caseDetails1, 14);
            assertExpectedSituationForCase1(draftOrderDocReviewCollection, psaDocReviewCollection, actual, expectedSituationForCase1);

            // Case 2: Multiple draftOrdersReviewCollection
            FinremCaseDetails caseDetails2 = FinremCaseDetailsBuilderFactory
                .from(Long.valueOf(TestConstants.CASE_ID), FinremCaseData.builder().draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        // may need a multiple
                        DraftOrdersReviewCollection.builder()
                            .value(DraftOrdersReview.builder()
                                .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
                                .build()
                            ).build(),
                        DraftOrdersReviewCollection.builder()
                            .value(DraftOrdersReview.builder()
                                .psaDocReviewCollection(psaDocReviewCollection)
                                .build()
                            ).build()
                    )).build())).build();
            actual = draftOrderService.getDraftOrderReviewOverdue(caseDetails2, 14);
            assertExpectedSituationForCase2(draftOrderDocReviewCollection, psaDocReviewCollection, actual, expectedSituationForCase2);
        }
    }

    private static void assertExpectedSituationForCase1(List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection,
                                                        List<PsaDocReviewCollection> psaDocReviewCollection,
                                                        List<DraftOrdersReview> actual, int expectedSituationForCase1) {
        switch (expectedSituationForCase1) {
            case 0:
                assertArrayEquals(new DraftOrdersReview[]{}, actual.toArray());
                break;
            case 1:
                assertEquals(List.of(DraftOrdersReview.builder()
                    .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
                    .psaDocReviewCollection(psaDocReviewCollection)
                    .build()), actual);
                break;
            default:
                fail(String.format("Unexpected expectedSituationForCase1: %s", expectedSituationForCase1));
                break;
        }
    }

    private static void assertExpectedSituationForCase2(List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection,
                                                        List<PsaDocReviewCollection> psaDocReviewCollection,
                                                        List<DraftOrdersReview> actual, int expectedSituationForCase2) {
        switch (expectedSituationForCase2) {
            case 0:
                assertArrayEquals(new DraftOrdersReview[]{}, actual.toArray());
                break;
            case 1:
                assertEquals(List.of(DraftOrdersReview.builder()
                    .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
                    .build()), actual);
                break;
            case 2:
                assertEquals(List.of(DraftOrdersReview.builder()
                    .psaDocReviewCollection(psaDocReviewCollection)
                    .build()), actual);
                break;
            case 3:
                assertEquals(List.of(
                    DraftOrdersReview.builder()
                        .draftOrderDocReviewCollection(draftOrderDocReviewCollection)
                        .build(),
                    DraftOrdersReview.builder()
                        .psaDocReviewCollection(psaDocReviewCollection)
                        .build()
                ), actual);
                break;
            default:
                fail(String.format("Unexpected expectedSituationForCase2: %s", expectedSituationForCase2));
                break;
        }
    }

    static Stream<Arguments> provideMultipleReviewsWithStatusTestCases() {
        return Stream.of(
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 5, 10, 10, 0)), Optional.of(LocalDateTime.of(2024, 6, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2024, 3, 15, 10, 0)), Optional.of(LocalDateTime.of(2024, 4, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 4, 19, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 4, 21, 10, 0))), // Notification dates
                1, 3
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 10, 18, 10, 0)), Optional.of(LocalDateTime.of(2024, 6, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2024, 10, 18, 10, 0)), Optional.of(LocalDateTime.of(2024, 4, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 4, 19, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 4, 21, 10, 0))), // Notification dates
                0, 0
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 1, 10, 10, 0)), Optional.of(LocalDateTime.of(2024, 1, 5, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2024, 1, 15, 10, 0)), Optional.of(LocalDateTime.of(2024, 1, 12, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 1, 4, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.empty()), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 2, 20, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2024, 2, 22, 10, 0)), Optional.of(LocalDateTime.of(2024, 2, 21, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty()), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2024, 2, 22, 10, 0)), Optional.of(LocalDateTime.of(2024, 2, 21, 10, 0))), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 3, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(Optional.of(LocalDateTime.of(2024, 3, 5, 10, 0)), Optional.of(LocalDateTime.of(2024, 3, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.APPROVED_BY_JUDGE)),
                List.of(Optional.empty()), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2024, 3, 4, 10, 0)), Optional.of(LocalDateTime.of(2024, 3, 2, 10, 0))), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 4, 1, 10, 0)), Optional.of(LocalDateTime.of(2024, 4, 3, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.of(LocalDateTime.of(2024, 4, 2, 10, 0)), Optional.of(LocalDateTime.of(2024, 4, 4, 10, 0))),
                List.of(Optional.of(OrderStatus.APPROVED_BY_JUDGE), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 4, 3, 10, 0))), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2024, 4, 2, 10, 0)), Optional.empty()), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2024, 5, 5, 10, 0)), Optional.of(LocalDateTime.of(2024, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 5, 1, 10, 0))), // Notification dates
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 5, 2, 10, 0))), // Notification dates
                1, 2
            ),
            Arguments.of(
                List.of(Optional.empty()), // Empty DraftOrderDocumentReview
                List.of(Optional.empty()), // No statuses
                List.of(Optional.of(LocalDateTime.of(2024, 5, 5, 10, 0)), Optional.of(LocalDateTime.of(2024, 5, 1, 10, 0))),
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED), Optional.of(OrderStatus.PROCESSED_BY_ADMIN)),
                List.of(Optional.empty(), Optional.of(LocalDateTime.of(2024, 5, 1, 10, 0))), // Notification dates
                List.of(Optional.of(LocalDateTime.of(2024, 5, 2, 10, 0)), Optional.of(LocalDateTime.of(2024, 5, 2, 10, 0))), // Notification dates
                0, 0
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 6, 1, 10, 0))), // Single DraftOrderDocumentReview
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(Optional.empty()), // Notification dates
                List.of(), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 10, 4, 0, 0))), // Single DraftOrderDocumentReview
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(Optional.empty()), // Notification dates
                List.of(), // Notification dates
                0, 0
            ),
            Arguments.of(
                List.of(Optional.of(LocalDateTime.of(2024, 10, 3, 23, 59))), // Single DraftOrderDocumentReview
                List.of(Optional.of(OrderStatus.TO_BE_REVIEWED)),
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(Optional.empty()), // Notification dates
                List.of(), // Notification dates
                1, 1
            ),
            Arguments.of(
                List.of(), // Empty DraftOrderDocumentReview
                List.of(), // No statuses
                List.of(), // Empty PsaDocumentReview
                List.of(), // No statuses
                List.of(), // No notification dates
                List.of(), // No notification dates
                0, 0
            )
        );
    }

    @ParameterizedTest
    @MethodSource("testIsDraftOrderReviewOverdue")
    void testIsDraftOrderReviewOverdue(LocalDate submissionDate, boolean expected) {
        DraftOrdersReview draftOrdersReview = DraftOrdersReview.builder()
            .draftOrderDocReviewCollection(List.of(
                createDraftOrder(OrderStatus.TO_BE_REVIEWED, submissionDate, null)
            ))
            .build();
        List<DraftOrdersReviewCollection> draftOrdersReviewCollection = List.of(
            DraftOrdersReviewCollection.builder()
              .value(draftOrdersReview)
              .build()
        );
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(draftOrdersReviewCollection)
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();
        LocalDate fixedDate = LocalDate.of(2024, 11, 4);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDate::now).thenReturn(fixedDate);
            boolean actual = draftOrderService.isDraftOrderReviewOverdue(caseDetails, 14);
            assertThat(actual).isEqualTo(expected);
        }
    }

    static Stream<Arguments> testIsDraftOrderReviewOverdue() {
        return Stream.of(
            Arguments.of(LocalDate.of(2024, 10, 20), true),
            Arguments.of(LocalDate.of(2024, 11, 4), false)
        );
    }

    @Test
    void testUpdateOverdueDocuments() {
        DraftOrdersReview draftOrdersReview = createDraftOrdersReview();
        LocalDate fixedDate = LocalDate.of(2024, 11, 4);
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 11, 4, 10, 23, 4);
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class,
                 Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);
            draftOrderService.updateOverdueDocuments(draftOrdersReview, 14);
        }

        List<DraftOrderDocReviewCollection> draftOrderCollection = draftOrdersReview.getDraftOrderDocReviewCollection();
        // Notification already sent
        verifyDraftOrderOverdueNotificationSent(draftOrderCollection, 0, LocalDateTime.of(2024, 10, 4, 12, 0, 5));
        // Notification sent
        verifyDraftOrderOverdueNotificationSent(draftOrderCollection, 1, LocalDateTime.of(2024, 11, 4, 10, 23, 4));
        // Not overdue
        verifyDraftOrderOverdueNotificationSent(draftOrderCollection, 2, null);
        verifyDraftOrderOverdueNotificationSent(draftOrderCollection, 3, null);
        verifyDraftOrderOverdueNotificationSent(draftOrderCollection, 4, null);

        List<PsaDocReviewCollection> psaCollection = draftOrdersReview.getPsaDocReviewCollection();
        // Notification already sent
        verifyPsaOverdueNotificationSent(psaCollection, 0, LocalDateTime.of(2024, 10, 4, 12, 0, 5));
        // Notification sent
        verifyPsaOverdueNotificationSent(psaCollection, 1, LocalDateTime.of(2024, 11, 4, 10, 23, 4));
        // Not overdue
        verifyPsaOverdueNotificationSent(psaCollection, 2, null);
        verifyPsaOverdueNotificationSent(psaCollection, 3, null);
        verifyPsaOverdueNotificationSent(psaCollection, 4, null);
    }

    private void verifyDraftOrderOverdueNotificationSent(List<DraftOrderDocReviewCollection> collection, int index,
                                                         LocalDateTime expectedNotificationSentDate) {
        DraftOrderDocumentReview draftOrderDocumentReview = collection.get(index).getValue();
        if (expectedNotificationSentDate != null) {
            LocalDateTime actualNotificationSentDate = draftOrderDocumentReview.getNotificationSentDate();
            assertThat(actualNotificationSentDate).isEqualTo(expectedNotificationSentDate);
        } else {
            assertThat(draftOrderDocumentReview.getNotificationSentDate()).isNull();
        }
    }

    private void verifyPsaOverdueNotificationSent(List<PsaDocReviewCollection> collection, int index,
                                                  LocalDateTime expectedNotificationSentDate) {
        PsaDocumentReview psaDocumentReview = collection.get(index).getValue();
        if (expectedNotificationSentDate != null) {
            LocalDateTime actualNotificationSentDate = psaDocumentReview.getNotificationSentDate();
            assertThat(actualNotificationSentDate).isEqualTo(expectedNotificationSentDate);
        } else {
            assertThat(psaDocumentReview.getNotificationSentDate()).isNull();
        }
    }

    private DraftOrdersReview createDraftOrdersReview() {
        return DraftOrdersReview.builder()
            .draftOrderDocReviewCollection(List.of(
                createDraftOrder(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 9, 1),
                    LocalDateTime.of(2024, 10, 4, 12, 0, 5)),
                createDraftOrder(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 9, 1), null),
                createDraftOrder(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 11, 1), null),
                createDraftOrder(OrderStatus.APPROVED_BY_JUDGE, LocalDate.of(2024, 9, 1), null),
                createDraftOrder(OrderStatus.PROCESSED_BY_ADMIN, LocalDate.of(2024, 9, 1), null)
            ))
            .psaDocReviewCollection(List.of(
                createPsa(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 9, 1),
                    LocalDateTime.of(2024, 10, 4, 12, 0, 5)),
                createPsa(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 9, 1), null),
                createPsa(OrderStatus.TO_BE_REVIEWED, LocalDate.of(2024, 11, 1), null),
                createPsa(OrderStatus.APPROVED_BY_JUDGE, LocalDate.of(2024, 9, 1), null),
                createPsa(OrderStatus.PROCESSED_BY_ADMIN, LocalDate.of(2024, 9, 1), null)
            ))
            .build();
    }

    private DraftOrderDocReviewCollection createDraftOrder(OrderStatus orderStatus, LocalDate submittedDate,
                                                           LocalDateTime notificationSentDate) {
        return DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .orderStatus(orderStatus)
                .submittedDate(submittedDate.atStartOfDay())
                .notificationSentDate(notificationSentDate)
                .build())
            .build();
    }

    private PsaDocReviewCollection createPsa(OrderStatus orderStatus, LocalDate submittedDate,
                                             LocalDateTime notificationSentDate) {
        return PsaDocReviewCollection.builder()
            .value(PsaDocumentReview.builder()
                .orderStatus(orderStatus)
                .submittedDate(submittedDate.atStartOfDay())
                .notificationSentDate(notificationSentDate)
                .build())
            .build();
    }
}
