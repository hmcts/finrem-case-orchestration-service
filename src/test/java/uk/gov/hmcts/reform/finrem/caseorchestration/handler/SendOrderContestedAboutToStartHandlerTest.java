package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToStartHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private GeneralOrderService generalOrderService;

    @InjectMocks
    private SendOrderContestedAboutToStartHandler handler;

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToStartEventSendOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToStartEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    @Test
    void givenACcdCallbackContestedCase_whenStartEventCalledAndAllPartiesAredigital_thenPrepareOrderList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.setApplicantOrganisationPolicy(getOrganisation("ORGAPP","applicant",
            CaseRole.APP_SOLICITOR.getValue()));
        data.setRespondentOrganisationPolicy(getOrganisation("ORGRESP","respondent",
            CaseRole.RESP_SOLICITOR.getValue()));
        data.getIntervenerOneWrapper().setIntervener1Organisation(getOrganisation("ORGINTV1","intervener1",
            CaseRole.INTVR_SOLICITOR_1.getValue()));
        data.getIntervenerTwoWrapper().setIntervener2Organisation(getOrganisation("ORGINTV2","intervener2",
            CaseRole.INTVR_SOLICITOR_2.getValue()));
        data.getIntervenerThreeWrapper().setIntervener3Organisation(getOrganisation("ORGINTV3","intervener3",
            CaseRole.INTVR_SOLICITOR_3.getValue()));
        data.getIntervenerFourWrapper().setIntervener4Organisation(getOrganisation("ORGINTV4","intervener4",
            CaseRole.INTVR_SOLICITOR_4.getValue()));


        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicMultiSelectList partiesInCase = resp.getData().getPartiesInCase();
        assertEquals(6, partiesInCase.getListItems().size(), "available parties");
        verify(generalOrderService).setOrderList(caseDetails);
    }

    @Test
    void givenACcdCallbackContestedCase_whenStartEventCalledAndAllPartiesAredigitalAndOneSelected_thenPrepareOrderList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.setApplicantOrganisationPolicy(getOrganisation("ORGAPP","applicant",
            CaseRole.APP_SOLICITOR.getValue()));
        data.setRespondentOrganisationPolicy(getOrganisation("ORGRESP","respondent",
            CaseRole.RESP_SOLICITOR.getValue()));
        data.getIntervenerOneWrapper().setIntervener1Organisation(getOrganisation("ORGINTV1","intervener1",
            CaseRole.INTVR_SOLICITOR_1.getValue()));
        data.getIntervenerTwoWrapper().setIntervener2Organisation(getOrganisation("ORGINTV2","intervener2",
            CaseRole.INTVR_SOLICITOR_2.getValue()));
        data.getIntervenerThreeWrapper().setIntervener3Organisation(getOrganisation("ORGINTV3","intervener3",
            CaseRole.INTVR_SOLICITOR_3.getValue()));
        data.getIntervenerFourWrapper().setIntervener4Organisation(getOrganisation("ORGINTV4","intervener4",
            CaseRole.INTVR_SOLICITOR_4.getValue()));


        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getValue()),
            getDynamicElementList(CaseRole.RESP_SOLICITOR.getValue()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getValue()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getValue()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getValue()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getValue()));

        DynamicMultiSelectList parties = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setPartiesInCase(parties);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicMultiSelectList partiesInCase = resp.getData().getPartiesInCase();
        assertEquals(6, partiesInCase.getListItems().size(), "available parties");
        assertEquals(6, partiesInCase.getValue().size(), "selected parties");
        verify(generalOrderService).setOrderList(caseDetails);
    }

    @Test
    void givenACcdCallbackContestedCase_whenStartEventCalledAndAllPartiesAreNotdigital_thenPrepareOrderList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setCcdCaseType(CONTESTED);
        data.getContactDetailsWrapper().setApplicantFmName("Tony");
        data.getContactDetailsWrapper().setApplicantLname("B");
        data.setApplicantOrganisationPolicy(getOrganisation(null,null,
            CaseRole.APP_SOLICITOR.getValue()));

        data.getContactDetailsWrapper().setRespondentFmName("Tony");
        data.getContactDetailsWrapper().setRespondentLname("C");
        data.setRespondentOrganisationPolicy(getOrganisation(null,null,
            CaseRole.RESP_SOLICITOR.getValue()));

        data.getIntervenerOneWrapper().setIntervener1Name("Intv1");
        data.getIntervenerOneWrapper().setIntervener1Organisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_1.getValue()));

        data.getIntervenerTwoWrapper().setIntervener2Organisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_2.getValue()));
        data.getIntervenerTwoWrapper().setIntervener2Name("Intv2");

        data.getIntervenerThreeWrapper().setIntervener3Organisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_3.getValue()));
        data.getIntervenerThreeWrapper().setIntervener3Name("Intv3");

        data.getIntervenerFourWrapper().setIntervener4Organisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_4.getValue()));
        data.getIntervenerFourWrapper().setIntervener4Name("Intv4");

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        DynamicMultiSelectList partiesInCase = resp.getData().getPartiesInCase();
        assertEquals(6, partiesInCase.getListItems().size(), "available parties");
        verify(generalOrderService).setOrderList(caseDetails);
    }


    private DynamicMultiSelectListElement getDynamicElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }


    private OrganisationPolicy getOrganisation(String orgId, String orgName, String role) {
        Organisation organisation = Organisation.builder()
            .organisationID(orgId)
            .organisationName(orgName)
            .build();
        return OrganisationPolicy.builder()
            .organisation(organisation)
            .orgPolicyCaseAssignedRole(role)
            .build();

    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}