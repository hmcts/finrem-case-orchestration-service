package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(StopRepresentingClientAboutToSubmitHandler.class);

    private StopRepresentingClientAboutToSubmitHandler underTest;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private UpdateRepresentationWorkflowService nocWorkflowService;

    @Mock
    private CaseRoleService caseRoleService;

    @Mock
    private ManageBarristerService manageBarristerService;

    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper, nocWorkflowService,
            caseRoleService, manageBarristerService, barristerChangeCaseAccessUpdater, onlineFormDocumentService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Nested
    class LoginAsAnyRepresentativeTests {

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenAnyCase_whenHandled_thenMiniFormAIsRefreshed(boolean isApplicantRepresentative, Integer intervenerIndex) {
            stubIsApplicantSolicitorAndIntervenerIndex(isApplicantRepresentative, intervenerIndex);

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), FinremCaseData.builder().build(),
                FinremCaseData.builder()
                    .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                        .stopRepClientConsent(YesOrNo.YES)
                        .build())
                    .build());

            // Act
            underTest.handle(request, AUTH_TOKEN);

            // Verify
            verify(onlineFormDocumentService).refreshContestedMiniFormA(request.getCaseDetails(), request.getCaseDetailsBefore(),
                AUTH_TOKEN);
            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenHavingClientConsent_whenHandled_thenWarningsPopulated(boolean isApplicantRepresentative, Integer intervenerIndex) {
            stubIsApplicantSolicitorAndIntervenerIndex(isApplicantRepresentative, intervenerIndex);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
                "Are you sure you wish to stop representing your client? "
                    + "If you continue your access to this access will be removed"
            );
            assertThat(logs.getInfos()).hasSize(2).contains(format(
                format("%s - %s representative stops representing a client with a client consent", CASE_ID,
                    isApplicantRepresentative ? "applicant" : "respondent")));

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenHavingJudicialApproval_whenHandled_thenWarningsPopulated(boolean isApplicantRepresentative, Integer intervenerIndex) {
            stubIsApplicantSolicitorAndIntervenerIndex(isApplicantRepresentative, intervenerIndex);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepJudicialApproval(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
                "Are you sure you wish to stop representing your client? "
                    + "If you continue your access to this access will be removed"
            );
            assertThat(logs.getInfos()).hasSize(2).contains(format(
                format("%s - %s representative stops representing a client with a judicial approval", CASE_ID,
                    isApplicantRepresentative ? "applicant" : "respondent")));

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenNoJudicialApprovalOrClientConsent_whenHandled_thenThrowIllegalStateException(boolean isApplicantRepresentative,
                                                                                               Integer intervenerIndex) {
            stubIsApplicantSolicitorAndIntervenerIndex(isApplicantRepresentative, intervenerIndex);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder().build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThatThrownBy(() -> underTest.handle(request, AUTH_TOKEN).getWarnings())
                .hasMessage("Client consent or judicial approval is required but missing.");
            assertThat(logs.getInfos()).doesNotContain(
                format("%s - applicant representative stops representing a client with a judicial approval", CASE_ID),
                format("%s - respondent representative stops representing a client with a judicial approval", CASE_ID),
                format("%s - applicant representative stops representing a client with a client consent", CASE_ID),
                format("%s - respondent representative stops representing a client with a client consent", CASE_ID)
            );

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        private static Stream<Arguments> provideAllLoggedInScenarios() {
            return Stream.of(
                Arguments.of(true, null),
                Arguments.of(false, null),
                Arguments.of(false, 1),
                Arguments.of(false, 2),
                Arguments.of(false, 3),
                Arguments.of(false, 4)
            );
        }

        private void stubIsApplicantSolicitorAndIntervenerIndex(boolean isApplicantSolicitor, Integer intervenerIndex) {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(isApplicantSolicitor);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(!isApplicantSolicitor);
            when(caseRoleService.isIntervenerRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(!isApplicantSolicitor);
            when(caseRoleService.getIntervenerIndex(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(Optional.ofNullable(intervenerIndex));
        }
    }

    @Nested
    class LoginAsApplicantOrRespondentRepresentativeTests {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenApplicantOrRespondentRepLoggedIn_whenHandled_thenPopulateNocParty(boolean isApplicantRepresentative) {
            stubApplicantOrRespondentRep(isApplicantRepresentative);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(ContactDetailsWrapper::getNocParty)
                .is(expectedParty(isApplicantRepresentative));

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenApplicantOrRespondentRepLoggedIn_whenHandled_thenClearOrganisationPolicy(boolean isApplicantRepresentative) {
            stubApplicantOrRespondentRep(isApplicantRepresentative);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(isApplicantRepresentative ? FinremCaseData::getApplicantOrganisationPolicy
                    : FinremCaseData::getRespondentOrganisationPolicy)
                .is(expectedOrganisationPolicy(isApplicantRepresentative));

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @Test
        void givenApplicantRepLoggedIn_whenNoSameOrganisationBarrister_thenDoesNotRemoveBarrister() {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(true);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);

            FinremCaseData caseData = FinremCaseData.builder()
                .applicantOrganisationPolicy(OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("BBB").build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .applicantBarristers(new ArrayList<>(List.of(
                        BarristerCollectionItem.builder()
                            .value(Barrister.builder()
                                .organisation(Organisation.builder().organisationID("AAA").build())
                                .build())
                            .build()
                    )))
                    .build())
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(BarristerCollectionWrapper::getApplicantBarristers,
                    InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .hasSize(1);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenApplicantOrRespondentRepLoggedIn_whenHandled_thenRemoveApplicantBarrister(boolean isApplicantRepresentative) {
            stubApplicantOrRespondentRep(isApplicantRepresentative);

            FinremCaseData caseData = FinremCaseData.builder()
                .applicantOrganisationPolicy(OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("AAA").build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .applicantBarristers(new ArrayList<>(List.of(
                        BarristerCollectionItem.builder()
                            .value(Barrister.builder()
                                .organisation(Organisation.builder().organisationID("AAA").build())
                                .build())
                            .build()
                    )))
                    .build())
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(BarristerCollectionWrapper::getApplicantBarristers,
                    InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .hasSize(isApplicantRepresentative ? 0 : 1);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenApplicantOrRespondentRepLoggedIn_whenHandled_thenRemoveRespondentBarristerAccordingly(boolean isApplicantRepresentative) {
            stubApplicantOrRespondentRep(!isApplicantRepresentative);

            FinremCaseData caseData = FinremCaseData.builder()
                .respondentOrganisationPolicy(OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("AAA").build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .respondentBarristers(new ArrayList<>(List.of(
                        BarristerCollectionItem.builder()
                            .value(Barrister.builder()
                                .organisation(Organisation.builder().organisationID("AAA").build())
                                .build())
                            .build()
                    )))
                    .build())
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(BarristerCollectionWrapper::getRespondentBarristers,
                    InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .hasSize(!isApplicantRepresentative ? 1 : 0);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @MethodSource
        void givenApplicantOrRespondentRepLoggedIn_whenHandled_thenPopulateServiceAddress(CaseType caseType,
                                                                                          boolean isApplicantRepresentative,
                                                                                          boolean addressConfidentiality) {
            stubApplicantOrRespondentRep(isApplicantRepresentative);

            Address serviceAddress = mock(Address.class);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .clientAddressForService(serviceAddress)
                    .clientAddressForServiceConfidential(YesOrNo.forValue(addressConfidentiality))
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(
                    isApplicantRepresentative ? ContactDetailsWrapper::getApplicantAddress : ContactDetailsWrapper::getRespondentAddress,
                    isApplicantRepresentative ? ContactDetailsWrapper::getApplicantAddressHiddenFromRespondent
                        : ContactDetailsWrapper::getRespondentAddressHiddenFromApplicant,
                    isApplicantRepresentative ? ContactDetailsWrapper::getApplicantRepresented : (CONTESTED.equals(caseType)
                        ? ContactDetailsWrapper::getContestedRespondentRepresented
                        : ContactDetailsWrapper::getConsentedRespondentRepresented))
                .contains(serviceAddress,  YesOrNo.forValue(addressConfidentiality), YesOrNo.NO);


            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        static Stream<Arguments> givenApplicantOrRespondentRepLoggedIn_whenHandled_thenPopulateServiceAddress() {
            return Stream.of(
                Arguments.of(CONSENTED, true, true),
                Arguments.of(CONSENTED, false, true),
                Arguments.of(CONSENTED, true, false),
                Arguments.of(CONSENTED, false, false),
                Arguments.of(CONTESTED, true, true),
                Arguments.of(CONTESTED, false, true),
                Arguments.of(CONTESTED, true, false),
                Arguments.of(CONTESTED, false, false)
            );
        }

        private void stubApplicantOrRespondentRep(boolean isApplicantRepresentative) {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(isApplicantRepresentative);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(!isApplicantRepresentative);
        }
    }

    @Nested
    class LoginAsIntervenerRepresentativeTests {

        private void stubIntervenerRep(int index) {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);
            when(caseRoleService.isIntervenerRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(true);
            when(caseRoleService.getIntervenerIndex(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(Optional.of(index));
        }
    }

    private static Condition<NoticeOfChangeParty> expectedParty(boolean isApplicantSolicitor) {
        return new Condition<>(party ->
            isApplicantSolicitor ? APPLICANT.equals(party) : RESPONDENT.equals(party),
            "expected APPLICANT if applicant solicitor, otherwise RESPONDENT");
    }

    private static Condition<OrganisationPolicy> expectedOrganisationPolicy(boolean isApplicantSolicitor) {
        return new Condition<>(orgPolicy -> {
            if (orgPolicy == null) {
                return false;
            }
            return isApplicantSolicitor ? CaseRole.APP_SOLICITOR.getCcdCode().equals(orgPolicy.getOrgPolicyCaseAssignedRole())
                : CaseRole.RESP_SOLICITOR.getCcdCode().equals(orgPolicy.getOrgPolicyCaseAssignedRole());
        },
            "expected APPLICANT if applicant solicitor, otherwise RESPONDENT");
    }

    private void verifyCaseRoleServiceCalled(FinremCaseData caseData) {
        verify(caseRoleService).isApplicantRepresentative(caseData, AUTH_TOKEN);
        verify(caseRoleService).isRespondentRepresentative(caseData, AUTH_TOKEN);
        verify(caseRoleService).isIntervenerRepresentative(caseData, AUTH_TOKEN);
        verify(caseRoleService).getIntervenerIndex(caseData, AUTH_TOKEN);
    }
}
