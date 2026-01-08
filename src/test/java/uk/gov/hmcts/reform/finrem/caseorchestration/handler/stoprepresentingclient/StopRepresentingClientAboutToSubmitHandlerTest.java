package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.IntervenerRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.barristers;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;
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
    private ManageBarristerService manageBarristerService;

    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @Mock
    private IntervenerService intervenerService;

    @Mock
    private StopRepresentingClientService stopRepresentingClientService;

    private static final String APPLICANT_ORG_ID = "APPLICANT_ORG_ID";
    private static final String RESPONDENT_ORG_ID = "RESPONDENT_ORG_ID";
    private static final String INTERVENER1_ORG_ID = "INTERVENER1_ORG_ID";
    private static final String INTERVENER2_ORG_ID = "INTERVENER2_ORG_ID";
    private static final String INTERVENER3_ORG_ID = "INTERVENER3_ORG_ID";
    private static final String INTERVENER4_ORG_ID = "INTERVENER4_ORG_ID";


    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper, nocWorkflowService,
            manageBarristerService, barristerChangeCaseAccessUpdater, intervenerService, stopRepresentingClientService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @ParameterizedTest
    @MethodSource("provideAllLoggedInScenarios")
    void givenServiceAddressMissing_whenHandled_throwsException(boolean isApplicantRepresentative, Integer intervenerIndex,
                                                                IntervenerRole intervenerRole) {
        stubIsApplicantRepresentativeAndIntervenerIndex(isApplicantRepresentative, intervenerIndex, intervenerRole);

        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(null))
            .build();

        assertThatThrownBy(() -> underTest.handle(request(caseData), AUTH_TOKEN))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("serviceAddress is null");
    }

    @Nested
    class LoginAsAnyRepresentativeTests {

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenHavingClientConsent_whenHandled_thenWarningsPopulated(boolean isApplicantRepresentative, Integer intervenerIndex,
                                                                        IntervenerRole intervenerRole) {
            stubIsApplicantRepresentativeAndIntervenerIndex(isApplicantRepresentative, intervenerIndex, intervenerRole);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
                .build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN)
                .getWarnings())
                .containsExactly(
                    "Are you sure you wish to stop representing your client? If you continue your access to this access will be removed"
                );
            assertThat(logs.getInfos()).hasSize(2).contains(format(
                format("%s - %s representative stops representing a client with a client consent", CASE_ID,
                    isApplicantRepresentative ? "applicant" : "respondent")));

            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenHavingJudicialApproval_whenHandled_thenWarningsPopulated(boolean isApplicantRepresentative, Integer intervenerIndex,
                                                                           IntervenerRole intervenerRole) {
            stubIsApplicantRepresentativeAndIntervenerIndex(isApplicantRepresentative, intervenerIndex, intervenerRole);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.NO)
                    .stopRepJudicialApproval(YesOrNo.YES)
                    .clientAddressForService(mock(Address.class))
                    .build())
                .build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN)
                .getWarnings())
                .containsExactly(
                    "Are you sure you wish to stop representing your client? If you continue your access to this access will be removed"
                );
            assertThat(logs.getInfos()).hasSize(2).contains(format(
                format("%s - %s representative stops representing a client with a judicial approval", CASE_ID,
                    isApplicantRepresentative ? "applicant" : "respondent")));

            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideAllLoggedInScenarios")
        void givenNoJudicialApprovalOrClientConsent_whenHandled_thenThrowIllegalStateException(
            boolean isApplicantRepresentative, Integer intervenerIndex, IntervenerRole intervenerRole) {

            stubIsApplicantRepresentativeAndIntervenerIndex(isApplicantRepresentative, intervenerIndex, intervenerRole);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder().build())
                .build();

            assertThatThrownBy(() -> underTest.handle(request(caseData), AUTH_TOKEN))
                .hasMessage("Unreachable");

            verifyBuildRepresentationCalled(caseData);
        }

        private static Stream<Arguments> provideAllLoggedInScenarios() {
            return StopRepresentingClientAboutToSubmitHandlerTest.provideAllLoggedInScenarios();
        }
    }

    @Nested
    class LogInAsApplicantOrRespondentRepresentativeTests {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenCaseWithAppOrResBarrister_whenHandled_thenRemoveTheBarrister(
            boolean isApplicantRepresentative) {

            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            IntervenerOne intervenerOne = intervenerOne(INTERVENER1_ORG_ID);
            IntervenerTwo intervenerTwo = mock(IntervenerTwo.class);

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = FinremCaseData.builder()
                .intervenerOne(intervenerOne)
                .intervenerTwo(intervenerTwo)
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .applicantBarristers(barristers(APPLICANT_ORG_ID)) // it must be the same as applicant solicitor
                    .respondentBarristers(barristers(RESPONDENT_ORG_ID))
                    .intvr1Barristers(barristers(INTERVENER1_ORG_ID))
                    .intvr2Barristers(barristers(INTERVENER2_ORG_ID))
                    .intvr4Barristers(barristers(INTERVENER4_ORG_ID))
                    .build())
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)));
            if (isApplicantRepresentative) {
                caseDataBuilder.applicantOrganisationPolicy(organisationPolicy(APPLICANT_ORG_ID));
            } else {
                caseDataBuilder.respondentOrganisationPolicy(organisationPolicy(RESPONDENT_ORG_ID));
            }

            FinremCaseData caseData = caseDataBuilder.build();

            caseData = underTest.handle(request(caseData), AUTH_TOKEN).getData();

            assertThat(caseData)
                .describedAs("Intervener one unchanged")
                .extracting(FinremCaseData::getIntervenerOne)
                .isEqualTo(intervenerOne);
            assertThat(caseData)
                .describedAs("Intervener two unchanged")
                .extracting(FinremCaseData::getIntervenerTwo)
                .isEqualTo(intervenerTwo);
            assertThat(caseData)
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .satisfies(wrapper -> {
                    assertThat(wrapper.getApplicantBarristers())
                        .matches(res ->
                            isApplicantRepresentative ? res.isEmpty() : res.equals(barristers(APPLICANT_ORG_ID)));
                    assertThat(wrapper.getRespondentBarristers())
                        .matches(res ->
                            !isApplicantRepresentative ? res.isEmpty() : res.equals(barristers(RESPONDENT_ORG_ID)));
                    assertThat(wrapper.getIntvr1Barristers()).isEqualTo(barristers(INTERVENER1_ORG_ID)); // kept
                    assertThat(wrapper.getIntvr2Barristers()).isEqualTo(barristers(INTERVENER2_ORG_ID)); // kept
                    assertThat(wrapper.getIntvr4Barristers()).isEqualTo(barristers(INTERVENER4_ORG_ID)); // kept
                });

            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenAnyCase_whenHandled_thenSetUnrepresentedAndPopulateNocParty(boolean isApplicantRepresentative) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
                .build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(ContactDetailsWrapper::getNocParty)
                .isEqualTo(isApplicantRepresentative ? APPLICANT : RESPONDENT);

            verify(stopRepresentingClientService, times(isApplicantRepresentative ? 1 : 0)).setApplicantUnrepresented(caseData);
            verify(stopRepresentingClientService, times(isApplicantRepresentative ? 0 : 1)).setRespondentUnrepresented(caseData);
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource
        void givenAnyCase_whenHandled_thenPopulateServiceAddress(boolean isApplicantRepresentative, CaseType caseType,
                                                                 boolean addressConfidentiality) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            Address serviceAddress = mock(Address.class);
            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(serviceAddress).toBuilder()
                    .clientAddressForServiceConfidential(YesOrNo.forValue(addressConfidentiality))
                    .build())
                .build();

            assertThat(underTest.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, caseData), AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(
                    isApplicantRepresentative ? ContactDetailsWrapper::getApplicantAddress : ContactDetailsWrapper::getRespondentAddress,
                    isApplicantRepresentative ? ContactDetailsWrapper::getApplicantAddressHiddenFromRespondent
                        : ContactDetailsWrapper::getRespondentAddressHiddenFromApplicant)
                .contains(serviceAddress, YesOrNo.forValue(addressConfidentiality));

            verifyBuildRepresentationCalled(caseData);
        }

        static Stream<Arguments> givenAnyCase_whenHandled_thenPopulateServiceAddress() {
            return Stream.of(true, false)
                .flatMap(firstFlag ->
                    Stream.of(CONSENTED, CONTESTED)
                        .flatMap(caseType ->
                            Stream.of(true, false)
                                .map(secondFlag ->
                                    Arguments.of(firstFlag, caseType, secondFlag)
                                )
                        )
                );
        }

        @ParameterizedTest
        @MethodSource("provideIntervenerIndexAndIsApplicantRepresentative")
        void givenIntervenerSolicitorWithSameOrgId_whenHandled_thenRemoveIntervenerSolicitor(
            int index, boolean isApplicantRepresentative) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = appendIntervenerOrganisationPolicy(index, TEST_ORG_ID, FinremCaseData.builder()
                .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
            );
            if (isApplicantRepresentative) {
                caseDataBuilder.applicantOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            } else {
                caseDataBuilder.respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            }
            FinremCaseData caseData = caseDataBuilder.build();

            underTest.handle(request(caseData), AUTH_TOKEN);
            verify(stopRepresentingClientService).setIntervenerUnrepresented(same(caseData.getInterveners().get(index - 1)));
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenMultipleIntervenerSolicitorAndBarristersWithSameOrgId_whenHandled_thenRemoveBoth(
            boolean isApplicantRepresentative) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = appendIntervenerOrganisationPolicy(1, TEST_ORG_ID, FinremCaseData.builder()
                .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
                .barristerCollectionWrapper(
                    BarristerCollectionWrapper.builder()
                        .intvr1Barristers(barristers(TEST_ORG_ID))
                        .intvr2Barristers(barristers(TEST_ORG_ID))
                        .intvr3Barristers(barristers(TEST_ORG2_ID))
                        .build()
                )
            );
            appendIntervenerOrganisationPolicy(2, TEST_ORG_ID, caseDataBuilder);
            if (isApplicantRepresentative) {
                caseDataBuilder.applicantOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            } else {
                caseDataBuilder.respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            }
            FinremCaseData caseData = caseDataBuilder.build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .satisfies(wrapper -> {
                    assertThat(intervenerBarristerCollectionWrapperExtractor(1).apply(wrapper)).isEmpty();
                    assertThat(intervenerBarristerCollectionWrapperExtractor(2).apply(wrapper)).isEmpty();
                    assertThat(intervenerBarristerCollectionWrapperExtractor(3).apply(wrapper))
                        .isEqualTo(barristers(TEST_ORG2_ID));
                });
            verify(stopRepresentingClientService).setIntervenerUnrepresented(same(caseData.getInterveners().get(0)));
            verify(stopRepresentingClientService).setIntervenerUnrepresented(same(caseData.getInterveners().get(1)));
            verify(stopRepresentingClientService, never()).setIntervenerUnrepresented(same(caseData.getInterveners().get(2)));
            verify(stopRepresentingClientService, never()).setIntervenerUnrepresented(same(caseData.getInterveners().get(3)));
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideIntervenerIndexAndIsApplicantRepresentative")
        void givenIntervenerSolicitorWithDifferenceOrgId_whenHandled_thenDoesNotRemoveIntervenerSolicitor(
            int index, boolean isApplicantRepresentative) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = appendIntervenerOrganisationPolicy(index, TEST_ORG2_ID, FinremCaseData.builder()
                .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
            );
            if (isApplicantRepresentative) {
                caseDataBuilder.applicantOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            } else {
                caseDataBuilder.respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            }
            FinremCaseData caseData = caseDataBuilder.build();

            underTest.handle(request(caseData), AUTH_TOKEN);
            verify(stopRepresentingClientService, never()).setIntervenerUnrepresented(caseData.getInterveners().get(index - 1));
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideIntervenerIndexAndIsApplicantRepresentative")
        void givenIntervenerSolicitorWithDifferenceOrgId_whenHandled_thenDoesNotRemoveIntervenerSolicitorButBarrister(
            int index, boolean isApplicantRepresentative) {
            stubIsRepresentingApplicantOrRespondent(isApplicantRepresentative);

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = appendIntervenerOrganisationPolicy(index, TEST_ORG2_ID, FinremCaseData.builder()
                .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
                .barristerCollectionWrapper(intervenerBarristerCollectionWrapper(index, TEST_ORG_ID))
            );
            if (isApplicantRepresentative) {
                caseDataBuilder.applicantOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            } else {
                caseDataBuilder.respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID));
            }
            FinremCaseData caseData = caseDataBuilder.build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(intervenerBarristerCollectionWrapperExtractor(index),
                    InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .isEmpty();

            verify(stopRepresentingClientService, never()).setIntervenerUnrepresented(caseData.getInterveners().get(index - 1));
            verifyBuildRepresentationCalled(caseData);
        }

        static Stream<Arguments> provideIntervenerIndexAndIsApplicantRepresentative() {
            return IntStream.rangeClosed(1, 4)
                .boxed()
                .flatMap(index ->
                    Stream.of(true, false)
                        .map(isApplicantRepresentative ->
                            Arguments.of(index, isApplicantRepresentative)
                        )
                );
        }

        private void stubIsRepresentingApplicantOrRespondent(boolean isApplicantRepresentative) {
            when(stopRepresentingClientService.buildRepresentation(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(
                new Representation(TEST_USER_ID, isApplicantRepresentative, !isApplicantRepresentative, null, null)
            );
        }
    }

    @Nested
    class LogInAsIntervenerRepresentativeTests {

        @ParameterizedTest
        @MethodSource("provideAllIntervenerSolicitorRoles")
        void givenAsIntervenerSolicitor_whenAllPartiesOrgsDoNotMatch_thenDoesNotPopulateNocParty(
            int index, IntervenerRole intervenerRole) {

            stubIsRepresentingIntervener(index, intervenerRole);

            FinremCaseData caseData = appendIntervenerOrganisationPolicy(index, TEST_ORG_ID, FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(mock(NoticeOfChangeParty.class)).build())
                .applicantOrganisationPolicy(organisationPolicy("applicantOrg"))
                .respondentOrganisationPolicy(organisationPolicy("respondentOrg"))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
            ).build();

            FinremCallbackRequest request = request(caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(ContactDetailsWrapper::getNocParty)
                .isNull();

            verify(stopRepresentingClientService, never()).setApplicantUnrepresented(caseData);
            verify(stopRepresentingClientService, never()).setRespondentUnrepresented(caseData);
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideAllIntervenerSolicitorRolesWithApplicantOrRespondentOrg")
        void givenAsIntervenerSolicitor_whenApplicantOrRespondentOrgMatch_thenSetUnrepresentedAndNocPartyPopulated(
            int index, IntervenerRole intervenerRole, String intvOrgId, NoticeOfChangeParty noticeOfChangeParty) {

            stubIsRepresentingIntervener(index, intervenerRole);
            FinremCaseData caseData = appendIntervenerOrganisationPolicy(index, intvOrgId, FinremCaseData.builder()
                .applicantOrganisationPolicy(organisationPolicy("applicantOrg"))
                .respondentOrganisationPolicy(organisationPolicy("respondentOrg"))
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
            ).build();

            assertThat(underTest.handle(request(caseData), AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(ContactDetailsWrapper::getNocParty)
                .isEqualTo(noticeOfChangeParty);

            verify(stopRepresentingClientService, times(APPLICANT.equals(noticeOfChangeParty) ? 1 : 0))
                .setApplicantUnrepresented(caseData);
            verify(stopRepresentingClientService, times(RESPONDENT.equals(noticeOfChangeParty) ? 1 : 0))
                .setRespondentUnrepresented(caseData);
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideAllIntervenerSolicitorRoles")
        void givenAsIntervenerSolicitor_whenHandled_thenSetIntervenerUnrepresented(
            int index, IntervenerRole intervenerRole) {

            stubIsRepresentingIntervener(index, intervenerRole);
            FinremCaseData caseData = appendIntervenerOrganisationPolicy(index, TEST_ORG_ID, FinremCaseData.builder()
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
            ).build();

            underTest.handle(request(caseData), AUTH_TOKEN);

            ArgumentCaptor<IntervenerWrapper> captor = ArgumentCaptor.forClass(IntervenerWrapper.class);
            verify(stopRepresentingClientService).setIntervenerUnrepresented(captor.capture());
            assertThat(captor.getValue().getIntervenerType())
                .isEqualTo(
                    switch (index) {
                        case 1 -> IntervenerType.INTERVENER_ONE;
                        case 2 -> IntervenerType.INTERVENER_TWO;
                        case 3 -> IntervenerType.INTERVENER_THREE;
                        case 4 -> IntervenerType.INTERVENER_FOUR;
                        default -> throw new IllegalArgumentException("Unexpected index: " + index);
                    }
                );
            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @MethodSource("provideAllIntervenerSolicitorRolesWithCaseTypesAndConfidentiality")
        void givenAnyCase_whenHandled_thenPopulateServiceAddress(int index, IntervenerRole intervenerRole,
                                                                 CaseType caseType, boolean addressConfidentiality) {
            stubIsRepresentingIntervener(index, intervenerRole);

            Address serviceAddress = mock(Address.class);
            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(serviceAddress).toBuilder()
                    .clientAddressForServiceConfidential(YesOrNo.forValue(addressConfidentiality))
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseType, caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(intervenerWrapperExtractor(index))
                .extracting(
                    IntervenerWrapper::getIntervenerAddress,
                    IntervenerWrapper::getIntervenerAddressConfidential
                )
                .contains(serviceAddress, YesOrNo.forValue(addressConfidentiality));

            verifyBuildRepresentationCalled(caseData);
        }

        @ParameterizedTest
        @ValueSource(ints = {1})
        void givenCaseWithSameOrganisationBarrister_whenHandled_thenRemoveRespondentBarrister(int index) {
            stubIsRepresentingIntervener(1, IntervenerRole.SOLICITOR);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder()
                    .intervenerOrganisation(organisationPolicy("AAA"))
                    .build())
                .intervenerTwo(IntervenerTwo.builder()
                    .intervenerOrganisation(organisationPolicy("BBB"))
                    .build())
                .intervenerThree(IntervenerThree.builder()
                    .intervenerOrganisation(organisationPolicy("CCC"))
                    .build())
                .intervenerFour(IntervenerFour.builder()
                    .intervenerOrganisation(organisationPolicy("DDD"))
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr1Barristers(barristers("AAA"))
                    .intvr2Barristers(barristers("BBB"))
                    .intvr3Barristers(barristers("CCC"))
                    .intvr4Barristers(barristers("DDD"))
                    .build())
                .stopRepresentationWrapper(clientConsentedStopRepresentationWrapper(mock(Address.class)))
                .build();

            FinremCallbackRequest request = request(caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(intervenerBarristerCollectionWrapperExtractor(index),
                    InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .isEmpty();

            verifyBuildRepresentationCalled(caseData);
        }

        private void stubIsRepresentingIntervener(int index, IntervenerRole intervenerRole) {
            when(stopRepresentingClientService.buildRepresentation(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(
                new Representation(TEST_USER_ID, false, false, index, intervenerRole)
            );
        }

        static Stream<Arguments> provideAllIntervenerSolicitorRoles() {
            return IntStream.rangeClosed(1, 4)
                .boxed()
                .flatMap(index ->
                    Stream.of(IntervenerRole.SOLICITOR)
                        .map(intervenerRole -> Arguments.of(index, intervenerRole))
                );
        }

        static Stream<Arguments> provideAllIntervenerSolicitorRolesWithApplicantOrRespondentOrg() {
            return provideAllIntervenerSolicitorRoles()
                .flatMap(args ->
                    Map.of("applicantOrg", APPLICANT,
                            "respondentOrg", RESPONDENT)
                        .entrySet().stream()
                        .map(e -> Arguments.of(args.get()[0], args.get()[1],
                            e.getKey(), e.getValue()
                        ))
                );
        }

        static Stream<Arguments> provideAllIntervenerSolicitorRolesWithCaseTypesAndConfidentiality() {
            return provideAllIntervenerSolicitorRoles()
                .flatMap(args ->
                    Arrays.stream(CaseType.values())
                        .flatMap(caseType ->
                            Stream.of(true, false)
                                .map(addressConfidentiality -> Arguments.of(args.get()[0], args.get()[1],
                                    caseType, addressConfidentiality
                                ))
                        )
                );
        }
    }

    private void verifyBuildRepresentationCalled(FinremCaseData caseData) {
        verify(stopRepresentingClientService).buildRepresentation(caseData, AUTH_TOKEN);
    }

    private static Stream<Arguments> provideAllLoggedInScenarios() {
        return Stream.of(
            Arguments.of(true, null, null),
            Arguments.of(false, null, null),
            Arguments.of(false, 1, IntervenerRole.SOLICITOR),
            Arguments.of(false, 2, IntervenerRole.SOLICITOR),
            Arguments.of(false, 3, IntervenerRole.SOLICITOR),
            Arguments.of(false, 4, IntervenerRole.SOLICITOR),
            Arguments.of(false, 1, IntervenerRole.BARRISTER),
            Arguments.of(false, 2, IntervenerRole.BARRISTER),
            Arguments.of(false, 3, IntervenerRole.BARRISTER),
            Arguments.of(false, 4, IntervenerRole.BARRISTER)
        );
    }

    private void stubIsApplicantRepresentativeAndIntervenerIndex(boolean isApplicantRepresentative,
                                                                 Integer intervenerIndex,
                                                                 IntervenerRole intervenerRole) {
        boolean isRepresentingApplicant = intervenerIndex == null && isApplicantRepresentative;
        boolean isRepresentingRespondent = intervenerIndex == null && isApplicantRepresentative;

        when(stopRepresentingClientService.buildRepresentation(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(
            new Representation(TEST_USER_ID, isRepresentingApplicant, !isRepresentingRespondent, intervenerIndex, intervenerRole)
        );
    }

    private static StopRepresentationWrapper clientConsentedStopRepresentationWrapper(Address serviceAddress) {
        return StopRepresentationWrapper.builder()
            .stopRepClientConsent(YesOrNo.YES)
            .clientAddressForService(serviceAddress)
            .build();
    }

    private static FinremCaseData.FinremCaseDataBuilder appendIntervenerOrganisationPolicy(int index, String orgId,
        FinremCaseData.FinremCaseDataBuilder builder) {

        switch (index) {
            case 1 -> builder.intervenerOne(IntervenerOne.builder().intervenerOrganisation(organisationPolicy(orgId)).build());
            case 2 -> builder.intervenerTwo(IntervenerTwo.builder().intervenerOrganisation(organisationPolicy(orgId)).build());
            case 3 -> builder.intervenerThree(IntervenerThree.builder().intervenerOrganisation(organisationPolicy(orgId)).build());
            case 4 -> builder.intervenerFour(IntervenerFour.builder().intervenerOrganisation(organisationPolicy(orgId)).build());
            default -> throw new IllegalArgumentException("Unsupported intervener index: " + index);
        }
        return builder;
    }

    private static BarristerCollectionWrapper intervenerBarristerCollectionWrapper(int index, String orgId) {
        return switch (index) {
            case 1 -> BarristerCollectionWrapper.builder().intvr1Barristers(barristers(orgId)).build();
            case 2 -> BarristerCollectionWrapper.builder().intvr2Barristers(barristers(orgId)).build();
            case 3 -> BarristerCollectionWrapper.builder().intvr3Barristers(barristers(orgId)).build();
            case 4 -> BarristerCollectionWrapper.builder().intvr4Barristers(barristers(orgId)).build();
            default -> throw new IllegalArgumentException("Unsupported intervener index: " + index);
        };
    }

    private static Function<BarristerCollectionWrapper, List<BarristerCollectionItem>> intervenerBarristerCollectionWrapperExtractor(int index) {
        return switch (index) {
            case 1 -> BarristerCollectionWrapper::getIntvr1Barristers;
            case 2 -> BarristerCollectionWrapper::getIntvr2Barristers;
            case 3 -> BarristerCollectionWrapper::getIntvr3Barristers;
            case 4 -> BarristerCollectionWrapper::getIntvr4Barristers;
            default -> throw new IllegalArgumentException("Unsupported index: " + index);
        };
    }

    private static Function<FinremCaseData, IntervenerWrapper> intervenerWrapperExtractor(int index) {
        return data -> switch (index) {
            case 1 -> data.getIntervenerOne();
            case 2 -> data.getIntervenerTwo();
            case 3 -> data.getIntervenerThree();
            case 4 -> data.getIntervenerFour();
            default -> throw new IllegalArgumentException("Unsupported index: " + index);
        };
    }

    private static IntervenerOne intervenerOne(String orgId) {
        return IntervenerOne.builder().intervenerOrganisation(
            organisationPolicy(orgId)
        ).build();
    }

    private static FinremCallbackRequest request(FinremCaseData caseData) {
        return FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
    }
}
