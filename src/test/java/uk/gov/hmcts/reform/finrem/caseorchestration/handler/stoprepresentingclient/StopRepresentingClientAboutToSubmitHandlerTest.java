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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
    private CaseRoleService caseRoleService;

    @Mock
    private ManageBarristerService manageBarristerService;

    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper, nocWorkflowService,
            caseRoleService, manageBarristerService, barristerChangeCaseAccessUpdater);
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
            assertThatThrownBy(() -> underTest.handle(request, AUTH_TOKEN))
                .hasMessage("Unreachable");

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
    class LogInAsApplicantRepresentativeTests {

        @Test
        void givenCaseWithOtherOrganisationApplicantBarrister_whenHandled_thenDoesNotRemoveBarrister() {
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

        @Test
        void givenCaseWithSameOrganisationBarrister_whenHandled_thenRemoveApplicantBarrister() {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(true);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);

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
                .hasSize(0);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }
    }

    @Nested
    class LogInAsRespondentRepresentativeTests {

        @Test
        void givenCaseWithSameOrganisationBarrister_whenHandled_thenRemoveRespondentBarrister() {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(true);

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
                .hasSize(0);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }
    }

    @Nested
    class LogInAsApplicantOrRespondentRepresentativeTests {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenAnyCase_whenHandled_thenPopulateNocParty(boolean isApplicantRepresentative) {
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
        void givenAnyCase_whenHandled_thenClearOrganisationPolicy(boolean isApplicantRepresentative) {
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

        @ParameterizedTest
        @MethodSource
        void givenAnyCase_whenHandled_thenPopulateServiceAddress(boolean isApplicantRepresentative,
                                                                 CaseType caseType,
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
                .contains(serviceAddress, YesOrNo.forValue(addressConfidentiality), YesOrNo.NO);


            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
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

        private void stubApplicantOrRespondentRep(boolean isApplicantRepresentative) {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(isApplicantRepresentative);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(!isApplicantRepresentative);
        }
    }

    @Nested
    class LogInAsIntervenerRepresentativeTests {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void givenAnyCase_whenHandled_thenDoesNotPopulateNocParty(int index) {
            stubIntervenerRep(index);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getContactDetailsWrapper)
                .extracting(ContactDetailsWrapper::getNocParty)
                .isNull();

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void givenAnyCase_whenHandled_thenClearOrganisationPolicy(int index) {
            stubIntervenerRep(index);

            FinremCaseData caseData = FinremCaseData.builder()
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();

            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(data -> switch (index) {
                    case 1 -> data.getIntervenerOne();
                    case 2 -> data.getIntervenerTwo();
                    case 3 -> data.getIntervenerThree();
                    case 4 -> data.getIntervenerFour();
                    default -> throw new IllegalArgumentException("Unsupported index: " + index);
                })
                .extracting(IntervenerWrapper::getIntervenerOrganisation)
                .is(expectedIntervenerOrganisationPolicy(index));

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        @ParameterizedTest
        @MethodSource
        void givenAnyCase_whenHandled_thenPopulateServiceAddress(int index, CaseType caseType, boolean addressConfidentiality) {
            stubIntervenerRep(index);

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
                .extracting(data -> switch (index) {
                    case 1 -> data.getIntervenerOne();
                    case 2 -> data.getIntervenerTwo();
                    case 3 -> data.getIntervenerThree();
                    case 4 -> data.getIntervenerFour();
                    default -> throw new IllegalArgumentException("Unsupported index: " + index);
                })
                .extracting(
                    IntervenerWrapper::getIntervenerAddress,
                    IntervenerWrapper::getIntervenerAddressConfidential,
                    IntervenerWrapper::getIntervenerRepresented
                )
                .contains(serviceAddress, YesOrNo.forValue(addressConfidentiality), YesOrNo.NO);

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

        static Stream<Arguments> givenAnyCase_whenHandled_thenPopulateServiceAddress() {
            return IntStream.rangeClosed(1, 4)
                .boxed()
                .flatMap(index ->
                    Stream.of(CONSENTED, CONTESTED)
                        .flatMap(caseType ->
                            Stream.of(true, false)
                                .map(flag -> Arguments.of(index, caseType, flag))
                        )
                );
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void givenCaseWithSameOrganisationBarrister_whenHandled_thenRemoveRespondentBarrister(int index) {
            when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(false);
            when(caseRoleService.isIntervenerRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(true);
            when(caseRoleService.getIntervenerIndex(any(FinremCaseData.class), eq(AUTH_TOKEN)))
                .thenReturn(Optional.of(index));

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
                .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                    .stopRepClientConsent(YesOrNo.YES)
                    .build())
                .build();


            FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
            assertThat(underTest.handle(request, AUTH_TOKEN).getData())
                .extracting(FinremCaseData::getBarristerCollectionWrapper)
                .extracting(data -> switch (index) {
                    case 1 -> data.getIntvr1Barristers();
                    case 2 -> data.getIntvr2Barristers();
                    case 3 -> data.getIntvr3Barristers();
                    case 4 -> data.getIntvr4Barristers();
                    default -> throw new IllegalArgumentException("Unsupported index: " + index);
                }, InstanceOfAssertFactories.list(BarristerCollectionItem.class))
                .isEmpty();

            verifyCaseRoleServiceCalled(request.getCaseDetails().getData());
        }

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

    private static Condition<OrganisationPolicy> expectedIntervenerOrganisationPolicy(int index) {
        return new Condition<>(orgPolicy -> {
            if (orgPolicy == null) {
                return false;
            }

            return CaseRole.getIntervenerSolicitorByIndex(index).getCcdCode().equals(orgPolicy.getOrgPolicyCaseAssignedRole());
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
