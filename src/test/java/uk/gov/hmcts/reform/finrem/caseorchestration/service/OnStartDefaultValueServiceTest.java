package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.EstimatedAssetsChecklistWrapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion.V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion.V3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;

@ExtendWith(MockitoExtension.class)
class OnStartDefaultValueServiceTest {

    private static final LocalDate FIXED_DATE = LocalDate.now();

    @InjectMocks
    private OnStartDefaultValueService service;

    @Mock
    private IdamService idamService;

    @Mock
    private OrganisationPolicy applicantDefaultOrganisationPolicy;

    @Mock
    private OrganisationPolicy respondentDefaultOrganisationPolicy;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void setDefaultDate() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultIssueDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().getIssueDate());
    }

    @Test
    void defaultCivilPartnership_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(NO_VALUE, callbackRequest.getCaseDetails().getData().getCivilPartnership().getYesOrNo());
    }

    @Test
    void defaultCivilPartnership_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().setCivilPartnership(YesOrNo.YES);
        service.defaultCivilPartnershipField(callbackRequest);
        assertEquals(YES_VALUE, callbackRequest.getCaseDetails().getData().getCivilPartnership().getYesOrNo());
    }

    @Test
    void defaultTypeOfApplication_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getText(),
            callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().getTypeOfApplication().getText());
    }

    @Test
    void defaultTypeOfApplication_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().setTypeOfApplication(
            Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989);
        service.defaultTypeOfApplication(callbackRequest);
        assertEquals(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getText(),
            callbackRequest.getCaseDetails().getData().getScheduleOneWrapper().getTypeOfApplication().getText());
    }

    @Test
    void setDefaultContestedJudgeName() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");
        service.defaultContestedOrderJudgeName(callbackRequest, AUTH_TOKEN);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_JUDGE_NAME));
    }

    @Test
    void setDefaultContestedOrderDate() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        service.defaultContestedOrderDate(callbackRequest);
        assertNotNull(callbackRequest.getCaseDetails().getData().get(CONTESTED_ORDER_APPROVED_DATE));
    }

    @Test
    void defaultUrgencyQuestion_defaultValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        service.defaultUrgencyQuestion(callbackRequest);
        assertEquals(YesOrNo.NO, callbackRequest.getCaseDetails().getData().getPromptForUrgentCaseQuestion());
    }

    @Test
    void defaultUrgencyQuestion_userValue_finremRequest() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        callbackRequest.getCaseDetails().getData().setPromptForUrgentCaseQuestion(YesOrNo.YES);
        service.defaultUrgencyQuestion(callbackRequest);
        assertEquals(YesOrNo.YES, callbackRequest.getCaseDetails().getData().getPromptForUrgentCaseQuestion());
    }

    @Test
    void testDefaultConsentedOrderJudgeName() {
        when(idamService.getIdamSurname(AUTH_TOKEN)).thenReturn("test name");

        FinremCaseData caseData = mock(FinremCaseData.class);
        service.defaultConsentedOrderJudgeName(caseData, AUTH_TOKEN);

        assertAll(
            () -> verify(caseData).setOrderDirectionJudgeName("test name"),
            () -> verify(idamService).getIdamSurname(AUTH_TOKEN)
        );
    }

    @Test
    void testDefaultConsentedOrderDate() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(FIXED_DATE);

            service.defaultConsentedOrderDate(caseData);
            verify(caseData).setOrderDirectionDate(FIXED_DATE);
        }
    }

    @Test
    void testDefaultApplicantOrganisationPolicy() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        try (MockedStatic<OrganisationPolicy> mockedStatic = Mockito.mockStatic(OrganisationPolicy.class)) {
            service.defaultApplicantOrganisationPolicy(callbackRequest);
            mockedStatic.when(() -> OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR))
                .thenReturn(applicantDefaultOrganisationPolicy);
            mockedStatic.verify(() -> OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR));
        }
    }

    @Test
    void testDefaultRespondentOrganisationPolicy() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        try (MockedStatic<OrganisationPolicy> mockedStatic = Mockito.mockStatic(OrganisationPolicy.class)) {
            service.defaultRespondentOrganisationPolicy(callbackRequest);
            mockedStatic.when(() -> OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR))
                .thenReturn(respondentDefaultOrganisationPolicy);
            mockedStatic.verify(() -> OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSetEstimatedAssetsChecklistVersion(boolean toggle) {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);
        when(featureToggleService.isEstimatedAssetsChecklistV3Enabled()).thenReturn(toggle);

        service.setEstimatedAssetsChecklistVersion(callbackRequest);

        assertThat(callbackRequest.getFinremCaseData())
            .extracting(FinremCaseData::getEstimatedAssetsChecklistWrapper)
            .extracting(EstimatedAssetsChecklistWrapper::getEstimatedAssetsChecklistVersion)
            .isEqualTo(toggle ? V3 : V2);
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID_IN_LONG).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}
