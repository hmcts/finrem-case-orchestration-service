package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.AdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.UploadDraftOrderAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.REVIEW_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.SCHEDULING_AND_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrdersAboutToSubmitHandlerTest {

    @InjectMocks
    private UploadDraftOrdersAboutToSubmitHandler handler;

    @Mock
    private CaseAssignedRoleService caseAssignedRoleService;

    @Mock
    private DraftOrdersCategoriser draftOrdersCategoriser;

    @Mock
    private IdamAuthService idamAuthService;

    @Mock
    private DraftOrderService draftOrderService;

    @Mock
    private HearingService hearingService;

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @BeforeEach
    void setup() {
        UserDetails mockedUserDetails = mock(UserDetails.class);
        lenient().when(idamAuthService.getUserDetails(AUTH_TOKEN)).thenReturn(mockedUserDetails);
        lenient().when(mockedUserDetails.getFullName()).thenReturn("Hamzah");
        lenient().when(mockedUserDetails.getEmail()).thenReturn("Hamzah@hamzah.com");

        lenient().doAnswer(invocation -> {
            String input = invocation.getArgument(0);
            HasSubmittedInfo secondArg = invocation.getArgument(1);
            return new DraftOrderService(idamAuthService, hearingService).applySubmittedInfo(input, secondArg);
        }).when(draftOrderService).applySubmittedInfo(anyString(), any());
    }

    @ParameterizedTest
    @MethodSource("provideSuggestedDraftOrders")
    void givenValidPsaAndOrderDetailsWithAttachments_whenHandle_thenMapCorrectly(CaseRole userCaseRole,
                                                                                 String uploadOnBehalfOf,
                                                                                 String submittedByEmail) {
        // Given
        final Long caseID = 1727874196328932L;
        FinremCaseData caseData = spy(new FinremCaseData());

        // Setting up Order, PSA and attachment details
        SuggestedPensionSharingAnnexCollection psaCollection = SuggestedPensionSharingAnnexCollection.builder()
            .value(SuggestedPensionSharingAnnex.builder()
                .suggestedPensionSharingAnnexes(mock(CaseDocument.class))
                .build())
            .build();
        AdditionalDocumentsCollection additionalDocumentCollection = AdditionalDocumentsCollection.builder()
            .value(UploadDraftOrderAdditionalDocument.builder()
                .orderAttachment(mock(CaseDocument.class))
                .build())
            .build();
        UploadSuggestedDraftOrderCollection orderCollection = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .additionalDocuments(List.of(additionalDocumentCollection))
                .build())
            .build();

        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
            .uploadOrdersOrPsas(List.of(ORDER_TYPE, PSA_TYPE))
            .uploadParty(buildUploadParty(uploadOnBehalfOf))
            .uploadSuggestedDraftOrderCollection(List.of(orderCollection))
            .suggestedPsaCollection(List.of(psaCollection))
            .build());
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);

        mockCaseRole(String.valueOf(caseID), userCaseRole);

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class));

        when(draftOrderService.isOrdersSelected(List.of(ORDER_TYPE, PSA_TYPE))).thenReturn(true);
        when(draftOrderService.isPsaSelected(List.of(ORDER_TYPE, PSA_TYPE))).thenReturn(true);
        when(documentWarningsHelper.getDocumentWarnings(any(FinremCallbackRequest.class), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of());

        // When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData, SCHEDULING_AND_HEARING), AUTH_TOKEN);

        // Then
        List<SuggestedDraftOrderCollection> collectionResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection();
        assertEquals(2, collectionResult.size());

        SuggestedDraftOrder draftOrderResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(0).getValue();
        assertThat(draftOrderResult.getSubmittedBy()).isNotNull();
        assertThat(draftOrderResult.getSubmittedByEmail()).isEqualTo(submittedByEmail);
        assertThat(draftOrderResult.getPensionSharingAnnex()).isNull();
        assertThat(draftOrderResult.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult.getAttachments()).isNotNull();
        assertThat(draftOrderResult.getUploadedOnBehalfOf()).isEqualTo(uploadOnBehalfOf);

        SuggestedDraftOrder psaResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(psaResult.getSubmittedBy()).isNotNull();
        assertThat(psaResult.getSubmittedByEmail()).isEqualTo(submittedByEmail);
        assertThat(psaResult.getPensionSharingAnnex()).isNotNull();
        assertThat(psaResult.getDraftOrder()).isNull();
        assertThat(psaResult.getAttachments()).isNull();
        assertThat(psaResult.getUploadedOnBehalfOf()).isEqualTo(uploadOnBehalfOf);
    }

    private static Stream<Arguments> provideSuggestedDraftOrders() {
        return Stream.of(
            // Applicant/respondent solicitors and barristers
            Arguments.of(CaseRole.APP_SOLICITOR, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.APP_BARRISTER, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.RESP_SOLICITOR, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.RESP_BARRISTER, null, "Hamzah@hamzah.com"),

            // Interveners
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, null, "Hamzah@hamzah.com"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, null, "Hamzah@hamzah.com"),

            // Caseworkers
            Arguments.of(CaseRole.CASEWORKER, UPLOAD_PARTY_APPLICANT, null),
            Arguments.of(CaseRole.CASEWORKER, UPLOAD_PARTY_RESPONDENT, null)
        );
    }

    @Test
    void givenMultipleOrderDetailsWithAttachments_whenHandle_thenMapCorrectly() {
        // Given
        final Long caseID = 1727874196328932L;
        FinremCaseData caseData = spy(new FinremCaseData());

        AdditionalDocumentsCollection additionalDocument1 = AdditionalDocumentsCollection.builder()
            .value(UploadDraftOrderAdditionalDocument.builder()
                .orderAttachment(mock(CaseDocument.class))
                .build())
            .build();
        AdditionalDocumentsCollection additionalDocument2 = AdditionalDocumentsCollection.builder()
            .value(UploadDraftOrderAdditionalDocument.builder()
                .orderAttachment(mock(CaseDocument.class))
                .build())
            .build();
        AdditionalDocumentsCollection additionalDocument3 = AdditionalDocumentsCollection.builder()
            .value(UploadDraftOrderAdditionalDocument.builder()
                .orderAttachment(mock(CaseDocument.class))
                .build())
            .build();
        UploadSuggestedDraftOrderCollection orderCollection1 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .additionalDocuments(List.of(additionalDocument1, additionalDocument2))
                .build())
            .build();

        UploadSuggestedDraftOrderCollection orderCollection2 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .additionalDocuments(List.of(additionalDocument3))
                .build())
            .build();

        UploadSuggestedDraftOrderCollection orderCollection3 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .build())
            .build();

        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
            .uploadOrdersOrPsas(List.of(ORDER_TYPE))
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .uploadSuggestedDraftOrderCollection(List.of(orderCollection1, orderCollection2, orderCollection3))
            .build());
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(Collections.emptyList()).build());

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class));

        when(draftOrderService.isOrdersSelected(List.of(ORDER_TYPE))).thenReturn(true);
        when(draftOrderService.isPsaSelected(List.of(ORDER_TYPE))).thenReturn(false);

        // When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData, SCHEDULING_AND_HEARING), AUTH_TOKEN);

        // Then
        List<SuggestedDraftOrderCollection> collectionResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection();
        assertEquals(3, collectionResult.size());

        SuggestedDraftOrder draftOrderResult1 = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(0).getValue();
        assertThat(draftOrderResult1.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult1.getAttachments()).isNotEmpty().hasSize(2);

        SuggestedDraftOrder draftOrderResult2 = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(draftOrderResult2.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult2.getAttachments()).isNotEmpty().hasSize(1);

        SuggestedDraftOrder draftOrderResult3 = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(2).getValue();
        assertThat(draftOrderResult3.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult3.getAttachments()).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideAgreedDraftOrders")
    void shouldHandleAgreedDraftOrder(UploadAgreedDraftOrder uado,
                                      List<AgreedDraftOrderCollection> existingAgreedDraftOrderCollection,
                                      List<AgreedDraftOrderCollection> expectedAgreedDraftOrderCollection) {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.uploadAgreedDraftOrder(uado);
        builder.typeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION);
        builder.agreedDraftOrderCollection(new ArrayList<>(existingAgreedDraftOrderCollection));
        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();

        when(draftOrderService.processAgreedDraftOrders(uado, AUTH_TOKEN)).thenReturn(expectedAgreedDraftOrderCollection);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData, SCHEDULING_AND_HEARING), AUTH_TOKEN);

        verify(draftOrderService).populateDraftOrdersReviewCollection(caseData, uado, expectedAgreedDraftOrderCollection);
        assertThat(response.getData().getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .containsAll(expectedAgreedDraftOrderCollection);
    }

    private static Stream<Arguments> provideAgreedDraftOrders() {
        UploadAgreedDraftOrder uado1 = UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build();
        AgreedDraftOrderCollection draftOrder1 = AgreedDraftOrderCollection.builder()
            .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build()).build();

        UploadAgreedDraftOrder uado2 = UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build();
        AgreedDraftOrderCollection draftOrder2 = AgreedDraftOrderCollection.builder()
            .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build()).build();

        UploadAgreedDraftOrder uado3 = UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build();

        UploadAgreedDraftOrder uado4 = UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_RESPONDENT))
            .build();

        return Stream.of(
            // Single draft order on behalf of applicant added to an empty collection
            Arguments.of(uado1, List.of(), List.of(draftOrder1)),

            // Adding a draft order to a non-empty collection
            Arguments.of(uado2, List.of(draftOrder1), List.of(draftOrder1, draftOrder2)),

            // Empty input and empty expected output
            Arguments.of(uado3, List.of(), List.of()),

            // Single draft order on behalf of respondent added to an empty collection
            Arguments.of(uado4, List.of(), List.of(draftOrder1))
        );
    }

    @Test
    void testHandleClearsUploadedDraftOrdersAndSetsIsUnreviewedDocumentPresent() {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .typeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION)
                .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                    .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
                    .build())
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder().build())
                .agreedDraftOrderCollection(agreedDraftOrdersCollection(List.of(LocalDateTime.now())))
                .isUnreviewedDocumentPresent(YesOrNo.NO)
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData, SCHEDULING_AND_HEARING);

        handler.handle(request, AUTH_TOKEN);

        assertThat(caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).isNull();
        assertThat(caseData.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).isNull();
        assertThat(caseData.getDraftOrdersWrapper().getIsUnreviewedDocumentPresent()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenUserDoesNotHaveAValidCaseRole_whenHandle_thenThrowsException() {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .typeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION)
                .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                    .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
                    .build())
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        mockCaseRole(caseReference, CaseRole.CREATOR);

        assertThatThrownBy(() -> handler.handle(request, AUTH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unexpected case role CREATOR");

        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(request, AUTH_TOKEN));
    }

    @Test
    void givenCaseDataForUploadingAgreedDraftOrder_whenWarningDetected_thenReturnWarnings() {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.typeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION);
        uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder uploadAgreedDraftOrder;
        AgreedPensionSharingAnnex agreedPensionSharingAnnex;

        builder.uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
            .uploadParty(DynamicRadioList.builder().value(DynamicRadioListElement.builder().code("theRespondent").build()).build())
            .uploadAgreedDraftOrderCollection(List.of(
                UploadAgreedDraftOrderCollection.builder().value(uploadAgreedDraftOrder
                        = uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder.builder().build())
                    .build()
            ))
            .agreedPsaCollection(List.of(
                AgreedPensionSharingAnnexCollection.builder()
                    .value(agreedPensionSharingAnnex = AgreedPensionSharingAnnex.builder().build())
                    .build()
            ))
            .build());
        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();

        ArgumentCaptor<Function> lambdaCaptor = ArgumentCaptor.forClass(Function.class);
        doAnswer(invocation -> {
            // Capture the function passed into the method
            Function<FinremCaseData, List<?>> capturedFunction = invocation.getArgument(1);

            // Execute the function with mock data
            List<?> extractedDocuments = capturedFunction.apply(caseData);

            // Validate that the function correctly processes the collections
            assertEquals(2, extractedDocuments.size());
            assertTrue(extractedDocuments.contains(uploadAgreedDraftOrder));
            assertTrue(extractedDocuments.contains(agreedPensionSharingAnnex));

            // Return a sample warning message
            return List.of("WARNING1");
        }).when(documentWarningsHelper).getDocumentWarnings(any(FinremCallbackRequest.class), lambdaCaptor.capture(), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData, SCHEDULING_AND_HEARING), AUTH_TOKEN);
        assertThat(response.getWarnings()).containsExactly("WARNING1");
        // Verify that the function was captured and executed
        verify(documentWarningsHelper).getDocumentWarnings(any(FinremCallbackRequest.class), any(), eq(AUTH_TOKEN));
    }

    @Test
    void givenCaseDataForUploadingSuggestedDraftOrder_whenWarningDetected_thenReturnWarnings() {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.typeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);
        UploadedDraftOrder uploadAgreedDraftOrder;
        SuggestedPensionSharingAnnex suggestedPensionSharingAnnex;

        builder.uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
            .uploadParty(DynamicRadioList.builder().value(DynamicRadioListElement.builder().code("theRespondent").build()).build())
            .uploadSuggestedDraftOrderCollection(List.of(
                UploadSuggestedDraftOrderCollection.builder().value(uploadAgreedDraftOrder
                        = UploadedDraftOrder.builder().build())
                    .build()
            ))
            .suggestedPsaCollection(List.of(
                SuggestedPensionSharingAnnexCollection.builder()
                    .value(suggestedPensionSharingAnnex = SuggestedPensionSharingAnnex.builder().build())
                    .build()
            ))
            .build());
        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();

        ArgumentCaptor<Function> lambdaCaptor = ArgumentCaptor.forClass(Function.class);
        doAnswer(invocation -> {
            // Capture the function passed into the method
            Function<FinremCaseData, List<?>> capturedFunction = invocation.getArgument(1);

            // Execute the function with mock data
            List<?> extractedDocuments = capturedFunction.apply(caseData);

            // Validate that the function correctly processes the collections
            assertEquals(2, extractedDocuments.size());
            assertTrue(extractedDocuments.contains(uploadAgreedDraftOrder));
            assertTrue(extractedDocuments.contains(suggestedPensionSharingAnnex));

            // Return a sample warning message
            return List.of("WARNING1");
        }).when(documentWarningsHelper).getDocumentWarnings(any(FinremCallbackRequest.class), lambdaCaptor.capture(), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData, SCHEDULING_AND_HEARING), AUTH_TOKEN);
        assertThat(response.getWarnings()).containsExactly("WARNING1");
        // Verify that the function was captured and executed
        verify(documentWarningsHelper).getDocumentWarnings(any(FinremCallbackRequest.class), any(), eq(AUTH_TOKEN));
    }

    @Test
    void givenCaseData_whenUploadingSuggestedDraftOrder_thenNoCaseStateChange() {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.typeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);
        builder.uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build());

        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(
                FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).state(APPLICATION_ISSUED),
                FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).state(REVIEW_ORDER).data(caseData)
            ), AUTH_TOKEN);
        assertThat(response.getState()).isEqualTo(APPLICATION_ISSUED.getStateId());
    }

    @Test
    void givenCaseData_whenUploadingAgreedDraftOrder_thenCaseStateChanges() {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.typeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION);
        builder.uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build());

        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(
                FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).state(APPLICATION_ISSUED),
                FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).state(REVIEW_ORDER).data(caseData)
            ), AUTH_TOKEN);
        assertThat(response.getState()).isEqualTo(REVIEW_ORDER.getStateId());
    }

    @Test
    void givenCaseData_whenStateIsMissing_thenThrowIllegalStateException() {
        DraftOrdersWrapper.DraftOrdersWrapperBuilder builder = DraftOrdersWrapper.builder();
        builder.typeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION);
        builder.uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
            .uploadParty(buildUploadParty(UPLOAD_PARTY_APPLICANT))
            .build());

        FinremCaseData caseData = FinremCaseData.builder().draftOrdersWrapper(builder.build()).build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(
            FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).state(null).data(caseData)
        );

        assertThat(assertThrows(IllegalStateException.class, () -> handler.handle(request, AUTH_TOKEN)).getMessage())
            .isEqualTo("Unexpected null in state");
    }

    private void mockCaseRole(String caseId, CaseRole userCaseRole) {
        CaseAssignedUserRolesResource caseAssignedUserRolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(
                CaseAssignedUserRole.builder()
                    .caseRole(userCaseRole.getCcdCode())
                    .build()
            ))
            .build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(caseId, AUTH_TOKEN))
            .thenReturn(caseAssignedUserRolesResource);
    }

    private static List<AgreedDraftOrderCollection> agreedDraftOrdersCollection(
        List<LocalDateTime> agreedDraftOrderSubmittedDates) {

        return agreedDraftOrderSubmittedDates.stream()
            .map(dateTime -> AgreedDraftOrder.builder().submittedDate(dateTime).build())
            .map(value -> AgreedDraftOrderCollection.builder().value(value).build())
            .toList();
    }

    private static DynamicRadioList buildUploadParty(String code) {
        return DynamicRadioList.builder()
            .value(DynamicRadioListElement.builder().code(code).build())
            .build();
    }
}
