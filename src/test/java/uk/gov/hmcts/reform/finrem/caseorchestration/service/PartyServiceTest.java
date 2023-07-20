package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class PartyServiceTest {

    private PartyService partyService;

    @BeforeEach
    void setup() {
        partyService =  new PartyService();
    }

    @Test
    void givenACcdCallbackContestedCase_whenStartEventCalledAndAllPartiesAreNotdigital_thenPartyListDefaultSelectedAppAndResp() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setCcdCaseType(CONTESTED);
        data.getContactDetailsWrapper().setApplicantFmName("Tony");
        data.getContactDetailsWrapper().setApplicantLname("B");
        data.setApplicantOrganisationPolicy(getOrganisation(null,null,
            CaseRole.APP_SOLICITOR.getCcdCode()));

        data.getContactDetailsWrapper().setRespondentFmName("Tony");
        data.getContactDetailsWrapper().setRespondentLname("C");
        data.setRespondentOrganisationPolicy(getOrganisation(null,null,
            CaseRole.RESP_SOLICITOR.getCcdCode()));

        data.getIntervenerOneWrapper().setIntervenerName("Intv1");
        data.getIntervenerOneWrapper().setIntervenerOrganisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_1.getCcdCode()));

        data.getIntervenerTwoWrapper().setIntervenerOrganisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_2.getCcdCode()));
        data.getIntervenerTwoWrapper().setIntervenerName("Intv2");

        data.getIntervenerThreeWrapper().setIntervenerOrganisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_3.getCcdCode()));
        data.getIntervenerThreeWrapper().setIntervenerName("Intv3");

        data.getIntervenerFourWrapper().setIntervenerOrganisation(getOrganisation(null,null,
            CaseRole.INTVR_SOLICITOR_4.getCcdCode()));
        data.getIntervenerFourWrapper().setIntervenerName("Intv4");


        DynamicMultiSelectList partiesOnCase = partyService.getAllActivePartyList(caseDetails);


        Assertions.assertEquals(6, partiesOnCase.getListItems().size(), "available parties");
        Assertions.assertEquals(2, partiesOnCase.getValue().size(), "pre-selected parties");
    }

    @Test
    void givenACcdCallbackContestedCase_whenStartEventCalledAndAllPartiesAredigitalAndAllSelected_thenPartyList() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.setApplicantOrganisationPolicy(getOrganisation("ORGAPP","applicant",
            CaseRole.APP_SOLICITOR.getCcdCode()));
        data.setRespondentOrganisationPolicy(getOrganisation("ORGRESP","respondent",
            CaseRole.RESP_SOLICITOR.getCcdCode()));
        data.getIntervenerOneWrapper().setIntervenerOrganisation(getOrganisation("ORGINTV1","intervener1",
            CaseRole.INTVR_SOLICITOR_1.getCcdCode()));
        data.getIntervenerTwoWrapper().setIntervenerOrganisation(getOrganisation("ORGINTV2","intervener2",
            CaseRole.INTVR_SOLICITOR_2.getCcdCode()));
        data.getIntervenerThreeWrapper().setIntervenerOrganisation(getOrganisation("ORGINTV3","intervener3",
            CaseRole.INTVR_SOLICITOR_3.getCcdCode()));
        data.getIntervenerFourWrapper().setIntervenerOrganisation(getOrganisation("ORGINTV4","intervener4",
            CaseRole.INTVR_SOLICITOR_4.getCcdCode()));


        List<DynamicMultiSelectListElement> dynamicElementList = List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()),
            getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()),
            getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode()));

        DynamicMultiSelectList parties = DynamicMultiSelectList.builder()
            .value(dynamicElementList)
            .listItems(dynamicElementList)
            .build();

        data.setPartiesOnCase(parties);

        DynamicMultiSelectList partiesOnCase = partyService.getAllActivePartyList(caseDetails);

        Assertions.assertEquals(6, partiesOnCase.getListItems().size(), "available parties");
        Assertions.assertEquals(6, partiesOnCase.getValue().size(), "selected parties");
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
