package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentInContestedSendOrderCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendConsentOrderInContestedSubmittedHandlerTest {
    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();
    private static final String AUTH_TOKEN = "tokien:)";

    @InjectMocks
    private SendConsentOrderInContestedSubmittedHandler consentOrderInContestedSubmittedHandler;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private CcdService ccdService;
    @Mock
    private FinremConsentInContestedSendOrderCorresponder sendConsentOrderInContestedCorresponder;


    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(consentOrderInContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_CONSENT_IN_CONTESTED_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(consentOrderInContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_CONSENT_IN_CONTESTED_ORDER),
            is(false));
    }

    private void setupData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
    }

    @Test
    void givenContestedCase_whenSolicitorsAreDigitalAndBothAreSelectedParties_ThenSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(List.of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));
        consentOrderInContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(sendConsentOrderInContestedCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenRespIsDigital_WhenPartySelectedToShareOrderWithIsRespondent_ThenSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(List.of(CaseRole.RESP_SOLICITOR.getCcdCode()));
        consentOrderInContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(sendConsentOrderInContestedCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenAppSolIsDigital_WhenPartySelectedToShareOrderWithIsApplicant_ThenSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getConsentOrderWrapper().setContestedConsentedApprovedOrders(getApprovedConsentOrders());
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(List.of(CaseRole.APP_SOLICITOR.getCcdCode()));
        consentOrderInContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(sendConsentOrderInContestedCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenRespSolIsNotDigital_WhenPartySelectedToShareOrderWithIsApplicant_ThenSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setAdditionalDocument(caseDocument());
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder().respondentSolicitorEmail("res@sol.com").build());
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));
        consentOrderInContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(sendConsentOrderInContestedCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenAppSolIsNotDigital_WhenApplicantSelectedToShareOrderWith_ThenSendNotification() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder().solicitorEmail("app@sol.com").build());
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));
        consentOrderInContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(sendConsentOrderInContestedCorresponder).sendCorrespondence(any(), any());
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list =  new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_SOLICITOR_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_CONSENT_IN_CONTESTED_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    public List<ConsentOrderCollection> getApprovedConsentOrders() {
        CaseDocument caseDocument = caseDocument();
        ApprovedOrder approvedOrder = ApprovedOrder.builder().consentOrder(caseDocument).build();
        ConsentOrderCollection consentOrderCollection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        return List.of(consentOrderCollection);
    }

}