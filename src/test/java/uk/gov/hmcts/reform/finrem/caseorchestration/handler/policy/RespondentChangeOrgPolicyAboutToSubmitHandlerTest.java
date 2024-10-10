package uk.gov.hmcts.reform.finrem.caseorchestration.handler.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

class RespondentChangeOrgPolicyAboutToSubmitHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";

    private RespondentChangeOrgPolicyAboutToSubmitHandler handler;

    @BeforeEach
    void setup() {
        handler = new RespondentChangeOrgPolicyAboutToSubmitHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLEAR_RESPONDENT_POLICY),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLEAR_RESPONDENT_POLICY)
        );
    }

    @Test
    void canNotHandleWrongEvent() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED,
                EventType.CLEAR_RESPONDENT_POLICY));
    }

    @Test
    void handle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        FinremCaseData data = response.getData();

        assertNull(data.getRespondentOrganisationPolicy().getOrganisation().getOrganisationID());
        assertNull(data.getRespondentOrganisationPolicy().getOrganisation().getOrganisationName());
        assertNull(data.getRespondentOrganisationPolicy().getOrgPolicyReference());
        assertNull(data.getChangeOrganisationRequestField());
        assertEquals(CaseRole.RESP_SOLICITOR.getCcdCode(), data.getRespondentOrganisationPolicy()
            .getOrgPolicyCaseAssignedRole());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID("oldId").organisationName("oldName").build())
            .orgPolicyReference("oldReference")
            .orgPolicyCaseAssignedRole(CaseRole.RESP_SOLICITOR.getCcdCode())
            .build();

        ChangeOrganisationRequest defaultRequest = ChangeOrganisationRequest.builder()
            .requestTimestamp(null)
            .organisationToAdd(null)
            .organisationToRemove(null)
            .approvalRejectionTimestamp(null)
            .approvalStatus(null)
            .caseRoleId(null)
            .reason(null)
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        finremCaseData.setApplicantOrganisationPolicy(organisationPolicy);
        finremCaseData.setChangeOrganisationRequestField(defaultRequest);

        return FinremCallbackRequest
            .builder()
            .eventType(EventType.CLEAR_RESPONDENT_POLICY)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .build();
    }
}

