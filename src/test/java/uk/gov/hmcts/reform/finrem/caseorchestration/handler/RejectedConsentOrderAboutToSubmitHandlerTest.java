package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;

@ExtendWith(MockitoExtension.class)
public class RejectedConsentOrderAboutToSubmitHandlerTest {

    private RejectedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentHelper documentHelper = new DocumentHelper(objectMapper, caseDataService, genericDocumentService,
        finremCaseDetailsMapper, letterAddresseeGeneratorMapper);
    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String REJECT_ORDER_VALID_JSON = "/fixtures/fee-lookup.json";

    @BeforeEach
    public void setUp() {
        RefusedConsentOrderDocumentCategoriser refusalConsentOrderDocumentCategoriser =
            new RefusedConsentOrderDocumentCategoriser(featureToggleService);
        handler = new RejectedConsentOrderAboutToSubmitHandler(refusalOrderDocumentService,
            refusalConsentOrderDocumentCategoriser, finremCaseDetailsMapper);
    }

    @Test
    public void given_case_whenEventRejectedOrder_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(true));
    }

    @Test
    public void given_contested_case_whenEventConsentOrderNotApproved_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CONSENT_ORDER_NOT_APPROVED),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REJECT_ORDER),
            is(false));
    }

    @Test
    public void given_case_when_wrong_event_type_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void given_case_when_order_not_approved_then_reject_order() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CallbackRequest callbackRequest = doValidCaseDataSetUp();
        FinremCallbackRequest finremCallbackRequest = doValidFinremCaseDataSetUp();
        CaseDetails mappedCaseDetails = callbackRequest.getCaseDetails();
        List<ContestedConsentOrderData> notApprovedConsentOrderData = getNotApprovedConsentOrderData(mappedCaseDetails.getData());
        notApprovedConsentOrderData.forEach(data -> data.getConsentOrder().getConsentOrder().setCategoryId(
            DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId()));
        mappedCaseDetails.getData().put("consentedNotApprovedOrders", notApprovedConsentOrderData);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails())).thenReturn(finremCallbackRequest.getCaseDetails());
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCallbackRequest.getCaseDetails())).thenReturn(callbackRequest.getCaseDetails());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        getNotApprovedConsentOrderData(response.getData()).forEach(data -> Assertions.assertEquals(
            DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId(), data.getConsentOrder().getConsentOrder().getCategoryId()));
        verify(refusalOrderDocumentService).generateConsentOrderNotApproved(any(), any());
    }

    private List<ContestedConsentOrderData> getNotApprovedConsentOrderData(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION))
            .map(documentHelper::convertToContestedConsentOrderData)
            .orElse(new ArrayList<>(1));
    }

    private CallbackRequest doValidCaseDataSetUp() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(REJECT_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FinremCallbackRequest doValidFinremCaseDataSetUp() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(REJECT_ORDER_VALID_JSON)) {
            return objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}