package uk.gov.hmcts.reform.finrem.caseorchestration.handler.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class ApplicantChangeOrgPolicyAboutToSubmitHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";

    private ApplicantChangeOrgPolicyAboutToSubmitHandler handler;

    @BeforeEach
    void setup() {
        handler = new ApplicantChangeOrgPolicyAboutToSubmitHandler(new FinremCaseDetailsMapper(new ObjectMapper()));
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLEAR_APPLICANT_POLICY),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLEAR_APPLICANT_POLICY)
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
                EventType.CLEAR_APPLICANT_POLICY));
    }

    @Test
    void canNotHandleAtAll() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED,
            EventType.LIST_FOR_HEARING));
    }

    @Test
    void handle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        FinremCaseData data = response.getData();

        assertNull(data.getApplicantOrganisationPolicy().getOrganisation().getOrganisationID());
        assertNull(data.getApplicantOrganisationPolicy().getOrganisation().getOrganisationName());
        assertNull(data.getApplicantOrganisationPolicy().getOrgPolicyReference());
        assertEquals(CaseRole.APP_SOLICITOR.getCcdCode(), data.getApplicantOrganisationPolicy()
            .getOrgPolicyCaseAssignedRole());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID("oldId").organisationName("oldName").build())
            .orgPolicyReference("oldReference")
            .orgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getCcdCode())
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        finremCaseData.setApplicantOrganisationPolicy(organisationPolicy);

        return FinremCallbackRequest
            .builder()
            .eventType(EventType.CLEAR_APPLICANT_POLICY)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .build();
    }

    private static void assertCanHandle(CallbackHandler handler, Arguments... combination) {
        for (CallbackType callbackType : CallbackType.values()) {
            for (CaseType caseType : CaseType.values()) {
                for (EventType eventType : EventType.values()) {
                    boolean expectedOutcome = Arrays.stream(combination).anyMatch(c ->
                        callbackType == c.get()[0]
                            && caseType == c.get()[1]
                            && eventType == c.get()[2] // This condition will always be true
                    );
                    assertThat(handler.canHandle(callbackType, caseType, eventType), equalTo(expectedOutcome));
                }
            }
        }
    }
}
