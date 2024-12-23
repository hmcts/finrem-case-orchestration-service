package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
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

    @Mock
    private HearingService hearingService;

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
    @ValueSource(booleans = {true, false})
    void givenValidPsaAndOrderDetailsWithAttachments_whenHandle_thenMapCorrectly(boolean withUploadParty) {
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

        if (withUploadParty) {
            caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(createUploadParty());
        }

        CaseAssignedUserRolesResource caseAssignedUserRolesResource = CaseAssignedUserRolesResource.builder()
            .caseAssignedUserRoles(List.of(
                CaseAssignedUserRole.builder()
                    .caseRole(withUploadParty ? CaseRole.CASEWORKER.getCcdCode() : CaseRole.APP_SOLICITOR.getCcdCode())
                    .build()
            ))
            .build();

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN))
            .thenReturn(caseAssignedUserRolesResource);

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class));

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
        assertThat(draftOrderResult.getSubmittedByEmail()).isEqualTo(withUploadParty ? null : "Hamzah@hamzah.com");
        assertThat(draftOrderResult.getPensionSharingAnnex()).isNull();
        assertThat(draftOrderResult.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult.getAttachments()).isNotNull();
        assertThat(draftOrderResult.getUploadedOnBehalfOf()).isEqualTo(withUploadParty ? UPLOAD_PARTY_APPLICANT : null);

        SuggestedDraftOrder psaResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(psaResult.getSubmittedBy()).isNotNull();
        assertThat(psaResult.getSubmittedByEmail()).isEqualTo(withUploadParty ? null : "Hamzah@hamzah.com");
        assertThat(psaResult.getPensionSharingAnnex()).isNotNull();
        assertThat(psaResult.getDraftOrder()).isNull();
        assertThat(psaResult.getAttachments()).isNull();
        assertThat(psaResult.getUploadedOnBehalfOf()).isEqualTo(withUploadParty ? UPLOAD_PARTY_APPLICANT : null);
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

        DynamicRadioList uploadParty = createUploadParty();
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(uploadParty);

        when(caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseID), AUTH_TOKEN))
            .thenReturn(CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(Collections.emptyList()).build());

        doNothing().when(draftOrdersCategoriser).categoriseDocuments(any(FinremCaseData.class));

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

    @Test
    void testHandleClearsUploadedDraftOrders() {
        String caseReference = "1727874196328932";
        FinremCaseData caseData = FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .typeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION)
                .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                    .uploadParty(createUploadParty())
                    .build())
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder().build())
                .agreedDraftOrderCollection(agreedDraftOrdersCollection(List.of(LocalDateTime.now())))
                .build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.parseLong(caseReference),
            CaseType.CONTESTED, caseData);

        handler.handle(request, AUTH_TOKEN);

        assertThat(caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).isNull();
        assertThat(caseData.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).isNull();
    }

    private DynamicRadioList createUploadParty() {
        return DynamicRadioList.builder()
            .value(DynamicRadioListElement.builder().code(UPLOAD_PARTY_APPLICANT).build())
            .build();
    }

    private List<AgreedDraftOrderCollection> agreedDraftOrdersCollection(
        List<LocalDateTime> agreedDraftOrderSubmittedDates) {

        return agreedDraftOrderSubmittedDates.stream()
            .map(dateTime -> AgreedDraftOrder.builder().submittedDate(dateTime).build())
            .map(value -> AgreedDraftOrderCollection.builder().value(value).build())
            .toList();
    }
}
