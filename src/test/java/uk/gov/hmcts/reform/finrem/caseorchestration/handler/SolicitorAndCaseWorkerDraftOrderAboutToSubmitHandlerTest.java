package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.POST_HEARING_DRAFT_ORDER;

@ExtendWith(MockitoExtension.class)
public class SolicitorAndCaseWorkerDraftOrderAboutToSubmitHandlerTest extends  BaseHandlerTestSetup {

    private SolicitorAndCaseWorkerDraftOrderAboutToSubmitHandler solicitorAndCaseWorkerDraftOrderAboutToSubmitHandler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUpTest() {
        UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser = new UploadedDraftOrderCategoriser(featureToggleService);
        solicitorAndCaseWorkerDraftOrderAboutToSubmitHandler = new SolicitorAndCaseWorkerDraftOrderAboutToSubmitHandler(
            finremCaseDetailsMapper,
            uploadedDraftOrderCategoriser
        );
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(solicitorAndCaseWorkerDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CW_DRAFT_ORDER),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(solicitorAndCaseWorkerDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_CW_DRAFT_ORDER),
            is(false));
    }

    @Test
    public void givenConsentInContestedCase_whenApprovedOrdersButNoRefusedOrderAvailableToShareWithParties_thenHandleRequest() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = solicitorAndCaseWorkerDraftOrderAboutToSubmitHandler.handle(
            callbackRequest, AUTH_TOKEN);
        FinremCaseData resultingData = response.getData();

        assertEquals(POST_HEARING_DRAFT_ORDER.getDocumentCategoryId(),
            resultingData.getDraftDirectionWrapper().getDraftDirectionOrderCollection().get(0).getValue().getUploadDraftDocument().getCategoryId());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        DraftDirectionOrder draftDirectionOrder = DraftDirectionOrder.builder().uploadDraftDocument(caseDocument()).build();
        List<DraftDirectionOrderCollection> draftDirectionOrderCollections =
            List.of(DraftDirectionOrderCollection.builder().value(draftDirectionOrder).build());
        DraftDirectionWrapper draftDirectionWrapper =
            DraftDirectionWrapper.builder().draftDirectionOrderCollection(draftDirectionOrderCollections).build();
        FinremCaseData caseData = FinremCaseData.builder().draftDirectionWrapper(draftDirectionWrapper).build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.SOLICITOR_CW_DRAFT_ORDER).caseDetails(caseDetails).build();
    }
}
