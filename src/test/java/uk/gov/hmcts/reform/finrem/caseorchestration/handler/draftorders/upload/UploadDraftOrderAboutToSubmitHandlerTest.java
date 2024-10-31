package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrderAboutToSubmitHandlerTest {

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

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void givenValidPsaAndOrderDetailsWithAttachments_whenHandle_thenMapCorrectly() {
        // Given
        final Long caseID = 1727874196328932L;
        FinremCaseData caseData = spy(new FinremCaseData());

        // Setting up Order, PSA and attachment details
        SuggestedPensionSharingAnnexCollection psaCollection = SuggestedPensionSharingAnnexCollection.builder()
            .value(SuggestedPensionSharingAnnex.builder()
                .suggestedPensionSharingAnnexes(mock(CaseDocument.class))
                .build())
            .build();
        SuggestedDraftOrderAdditionalDocumentsCollection additionalDocumentCollection = SuggestedDraftOrderAdditionalDocumentsCollection.builder()
            .value(mock(CaseDocument.class))
            .build();
        UploadSuggestedDraftOrderCollection orderCollection = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .suggestedDraftOrderAdditionalDocumentsCollection(List.of(additionalDocumentCollection))
                .build())
            .build();


        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder().build());
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadOrdersOrPsas(Arrays.asList(ORDER_TYPE, PSA_TYPE));
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setSuggestedPsaCollection((List.of(psaCollection)));
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadSuggestedDraftOrderCollection((List.of(orderCollection)));

        DynamicRadioList uploadParty = DynamicRadioList.builder().value(
            DynamicRadioListElement.builder().code(UPLOAD_PARTY_APPLICANT).build()
        ).build();

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(uploadParty);

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(Collections.emptyList()).build());

        UserInfo mockUserInfo = mock(UserInfo.class);
        when(idamAuthService.getUserInfo(AUTH_TOKEN)).thenReturn(mockUserInfo);
        when(mockUserInfo.getName()).thenReturn("Hamzah");

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class), anyString());

        when(draftOrderService.isOrdersSelected(List.of(ORDER_TYPE, PSA_TYPE))).thenReturn(true);
        when(draftOrderService.isPsaSelected(List.of(ORDER_TYPE, PSA_TYPE))).thenReturn(true);

        // When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        // Then
        List<SuggestedDraftOrderCollection> collectionResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection();
        Assertions.assertEquals(2, collectionResult.size());

        SuggestedDraftOrder draftOrderResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(0).getValue();
        assertThat(draftOrderResult.getSubmittedBy()).isNotNull();
        assertThat(draftOrderResult.getPensionSharingAnnex()).isNull();
        assertThat(draftOrderResult.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult.getAttachments()).isNotNull();
        assertThat(draftOrderResult.getUploadedOnBehalfOf()).isEqualTo(UPLOAD_PARTY_APPLICANT);

        SuggestedDraftOrder psaResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(psaResult.getSubmittedBy()).isNotNull();
        assertThat(psaResult.getPensionSharingAnnex()).isNotNull();
        assertThat(psaResult.getDraftOrder()).isNull();
        assertThat(psaResult.getAttachments()).isNull();
        assertThat(psaResult.getUploadedOnBehalfOf()).isEqualTo(UPLOAD_PARTY_APPLICANT);
    }

    @Test
    void givenMultipleOrderDetailsWithAttachments_whenHandle_thenMapCorrectly() {
        // Given
        final Long caseID = 1727874196328932L;
        FinremCaseData caseData = spy(new FinremCaseData());

        SuggestedDraftOrderAdditionalDocumentsCollection additionalDocument1 = SuggestedDraftOrderAdditionalDocumentsCollection.builder()
            .value(mock(CaseDocument.class))
            .build();
        SuggestedDraftOrderAdditionalDocumentsCollection additionalDocument2 = SuggestedDraftOrderAdditionalDocumentsCollection.builder()
            .value(mock(CaseDocument.class))
            .build();
        SuggestedDraftOrderAdditionalDocumentsCollection additionalDocument3 = SuggestedDraftOrderAdditionalDocumentsCollection.builder()
            .value(mock(CaseDocument.class))
            .build();
        UploadSuggestedDraftOrderCollection orderCollection1 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .suggestedDraftOrderAdditionalDocumentsCollection(List.of(additionalDocument1, additionalDocument2))
                .build())
            .build();

        UploadSuggestedDraftOrderCollection orderCollection2 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .suggestedDraftOrderAdditionalDocumentsCollection(List.of(additionalDocument3))
                .build())
            .build();

        UploadSuggestedDraftOrderCollection orderCollection3 = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .build())
            .build();

        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder().build());
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadOrdersOrPsas(List.of(ORDER_TYPE));
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadSuggestedDraftOrderCollection((List.of(
            orderCollection1, orderCollection2, orderCollection3)));

        DynamicRadioList uploadParty = DynamicRadioList.builder().value(
            DynamicRadioListElement.builder().code(UPLOAD_PARTY_APPLICANT).build()
        ).build();

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(uploadParty);

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(Collections.emptyList()).build());

        UserInfo mockUserInfo = mock(UserInfo.class);
        when(idamAuthService.getUserInfo(AUTH_TOKEN)).thenReturn(mockUserInfo);
        when(mockUserInfo.getName()).thenReturn("Hamzah");

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class), anyString());

        when(draftOrderService.isOrdersSelected(List.of(ORDER_TYPE))).thenReturn(true);
        when(draftOrderService.isPsaSelected(List.of(ORDER_TYPE))).thenReturn(false);

        // When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        // Then
        List<SuggestedDraftOrderCollection> collectionResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection();
        Assertions.assertEquals(3, collectionResult.size());

        SuggestedDraftOrder draftOrderResult1 = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(0).getValue();
        assertThat(draftOrderResult1.getDraftOrder()).isNotNull();
        Assertions.assertEquals(2, draftOrderResult1.getAttachments().size());

        SuggestedDraftOrder draftOrderResult2 = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(draftOrderResult2.getDraftOrder()).isNotNull();
        Assertions.assertEquals(1, draftOrderResult2.getAttachments().size());

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
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, caseData), AUTH_TOKEN);

        verify(draftOrderService).populateDraftOrdersReviewCollection(caseData, uado, expectedAgreedDraftOrderCollection);
        assertThat(response.getData().getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .containsAll(expectedAgreedDraftOrderCollection);
    }

    private static Stream<Arguments> provideAgreedDraftOrders() {
        UploadAgreedDraftOrder uado1 = UploadAgreedDraftOrder.builder().build();
        AgreedDraftOrderCollection draftOrder1 = AgreedDraftOrderCollection.builder()
            .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build()).build();

        UploadAgreedDraftOrder uado2 = UploadAgreedDraftOrder.builder().build();
        AgreedDraftOrderCollection draftOrder2 = AgreedDraftOrderCollection.builder()
            .value(AgreedDraftOrder.builder().draftOrder(CaseDocument.builder().build()).build()).build();

        UploadAgreedDraftOrder uado3 = UploadAgreedDraftOrder.builder().build();
        UploadAgreedDraftOrder uadoWithNull = null;

        return Stream.of(
            // Single draft order added to an empty collection
            Arguments.of(uado1, List.of(), List.of(draftOrder1)),

            // Adding a draft order to a non-empty collection
            Arguments.of(uado2, List.of(draftOrder1), List.of(draftOrder1, draftOrder2)),

            // Empty input and empty expected output
            Arguments.of(uado3, List.of(), List.of()),

            // Null input with existing collection
            Arguments.of(uadoWithNull, List.of(draftOrder1), List.of(draftOrder1)),

            // Null input with empty collection
            Arguments.of(uadoWithNull, List.of(), List.of())
        );
    }
}
