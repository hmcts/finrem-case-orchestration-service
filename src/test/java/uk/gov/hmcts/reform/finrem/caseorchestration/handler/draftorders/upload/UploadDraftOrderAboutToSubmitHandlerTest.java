package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
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

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void givenValidPsaAndOrderDetails_whenHandle_thenMapCorrectly() {
        // Given
        long caseID = 123;
        FinremCaseData caseData = spy(new FinremCaseData());

        // Setting up PSA and Order details
        SuggestedPensionSharingAnnexCollection psaCollection = SuggestedPensionSharingAnnexCollection.builder()
            .value(SuggestedPensionSharingAnnex.builder()
                .suggestedPensionSharingAnnexes(mock(CaseDocument.class))
                .build())
            .build();
        UploadSuggestedDraftOrderCollection orderCollection = UploadSuggestedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(mock(CaseDocument.class))
                .build())
            .build();

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


        // When
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseID, caseData), AUTH_TOKEN);

        // Then
        List<SuggestedDraftOrderCollection> collectionResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection();
        Assertions.assertEquals(2, collectionResult.size());

        SuggestedDraftOrder draftOrderResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(0).getValue();
        assertThat(draftOrderResult.getSubmittedBy()).isNotNull();
        assertThat(draftOrderResult.getPensionSharingAnnex()).isNull();
        assertThat(draftOrderResult.getDraftOrder()).isNotNull();
        assertThat(draftOrderResult.getUploadedOnBehalfOf()).isEqualTo(UPLOAD_PARTY_APPLICANT);

        SuggestedDraftOrder psaResult = response.getData().getDraftOrdersWrapper().getSuggestedDraftOrderCollection().get(1).getValue();
        assertThat(psaResult.getSubmittedBy()).isNotNull();
        assertThat(psaResult.getPensionSharingAnnex()).isNotNull();
        assertThat(psaResult.getDraftOrder()).isNull();
        assertThat(psaResult.getUploadedOnBehalfOf()).isEqualTo(UPLOAD_PARTY_APPLICANT);
    }

}
