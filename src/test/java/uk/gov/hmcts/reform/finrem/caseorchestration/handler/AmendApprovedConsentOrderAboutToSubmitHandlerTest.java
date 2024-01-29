package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION;

@ExtendWith(MockitoExtension.class)
class AmendApprovedConsentOrderAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApprovedConsentOrderAboutToSubmitHandler aboutToSubmitHandler;
    private FinremCallbackRequest callbackRequest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void init() {
        ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser = new ApprovedConsentOrderDocumentCategoriser(featureToggleService);
        aboutToSubmitHandler = new AmendApprovedConsentOrderAboutToSubmitHandler(finremCaseDetailsMapper, approvedConsentOrderCategoriser);
        callbackRequest = buildCallbackRequest(EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER);
    }

    @Test
    void givenACcdCallbackConsentedCase_whenAboutToSubmitEvent_thenHandlerCannotHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(false));
    }

    @Test
    void givenACcdCallbackContestedCase_whenAboutToSubmitEvent_thenHandlerCanHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_whenAboutToStartEvent_thenHandlerCannotHandle() {
        assertThat(aboutToSubmitHandler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED,
            EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER), equalTo(false));
    }

    @Test
    void givenContestedCase_whenNoApprovedOrdersOnCase_thenDoesNotCategorise() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        assertNull(response.getData().getConsentOrderWrapper().getContestedConsentedApprovedOrders());
    }

    @Test
    void givenContestedCase_whenSingleApprovedOrderOnCase_thenCategoriseOrderLetterAndPensionDocuments() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        callbackRequest.getCaseDetails().getData().getConsentOrderWrapper().setContestedConsentedApprovedOrders(List.of(getApprovedOrder("letter.pdf",
            "letterurl", "letterbinary")));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId(),
            response.getData().getConsentOrderWrapper().getContestedConsentedApprovedOrders()
            .get(0).getApprovedOrder().getOrderLetter().getCategoryId());
    }

    @Test
    void givenContestedCase_whenMultipleApprovedOrdersOnCase_thenCategoriseOrderLetterAndPensionDocuments() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        callbackRequest.getCaseDetails().getData().getConsentOrderWrapper().setContestedConsentedApprovedOrders(List.of(getApprovedOrder("letter.pdf",
            "letterurl", "letterbinary"), getApprovedOrder("letter2.pdf",
            "letter2url", "letter2binary")));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);
        response.getData().getConsentOrderWrapper().getContestedConsentedApprovedOrders().forEach(order ->
            assertEquals(APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId(),
                order.getApprovedOrder().getOrderLetter().getCategoryId()));
    }

    private ConsentOrderCollection getApprovedOrder(String file, String url, String binary) {
        ApprovedOrder approvedOrder = ApprovedOrder.builder().orderLetter(getCaseDocument(file, url, binary)).build();
        return ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
    }

    private CaseDocument getCaseDocument(String file, String url, String binary) {
        return TestSetUpUtils.caseDocument(url, file, binary);
    }
}