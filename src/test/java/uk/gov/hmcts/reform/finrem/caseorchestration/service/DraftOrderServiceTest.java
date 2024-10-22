package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.DisplayName;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class DraftOrderServiceTest {

    private static final String UPLOAD_PARTY_THE_APPLICANT = "theApplicant";

    private static final DynamicRadioList UPLOAD_PARTY = DynamicRadioList.builder()
        .value(DynamicRadioListElement.builder().code(UPLOAD_PARTY_THE_APPLICANT).build())
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
            .uploadedOnBehalfOf(UPLOAD_PARTY_THE_APPLICANT)
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
            .uploadedOnBehalfOf(UPLOAD_PARTY_THE_APPLICANT)
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
            .uploadedOnBehalfOf(UPLOAD_PARTY_THE_APPLICANT)
            .build();

        AgreedDraftOrder expectedOrder5 = AgreedDraftOrder.builder()
            .orderStatus(OrderStatus.TO_BE_REVIEWED)
            .submittedBy("<SUBMITTED_BY>")
            .submittedDate(LocalDateTime.of(2024, 10, 18, 10, 0))
            .uploadedOnBehalfOf(UPLOAD_PARTY_THE_APPLICANT)
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

        FinremCaseData finremCaseData = FinremCaseData.builder().ccdCaseId(TestConstants.CASE_ID).build();
        UploadAgreedDraftOrder uploadAgreedDraftOrder = new UploadAgreedDraftOrder();
        List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection = List.of(
            AgreedDraftOrderCollection.builder()
                .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build())
                .build(),
            AgreedDraftOrderCollection.builder()
                .value(AgreedDraftOrder.builder().pensionSharingAnnex(CaseDocument.builder().build()).build())
                .build()
        );

        // Mock hearingDetails and judge based on parameters
        DynamicList hearingDetails = hasHearingDetails ? DynamicList.builder().build() : null;
        uploadAgreedDraftOrder.setHearingDetails(hearingDetails);
        uploadAgreedDraftOrder.setJudge(hasJudge ? "Judge Name" : null);

        lenient().when(hearingService.getHearingType(any(), any())).thenReturn("Some Hearing Type");
        lenient().when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now());
        lenient().when(hearingService.getHearingTime(any(), any())).thenReturn("10:00 AM");

        // Call the method
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, uploadAgreedDraftOrder, newAgreedDraftOrderCollection);

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
        
        if (!hasHearingDetails) {
            assertThat(logs.getErrors()).containsExactly("Unexpected null hearing details for Case ID: " + TestConstants.CASE_ID);
        } else if (!hasJudge) {
            assertThat(logs.getErrors()).containsExactly("Unexpected null judge for Case ID: " + TestConstants.CASE_ID);
        }
    }

}
