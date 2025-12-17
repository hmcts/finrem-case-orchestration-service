package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.ArrayList;
import java.util.List;
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingClientConsent_whenHandled_thenWarningsPopulated(boolean isApplicantSolicitor) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(isApplicantSolicitor);
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
                isApplicantSolicitor ? "applicant" : "respondent")));
        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingJudicialApproval_whenHandled_thenWarningsPopulated(boolean isApplicantSolicitor) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(isApplicantSolicitor);
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
                isApplicantSolicitor ? "applicant" : "respondent")));
        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNoJudicialApprovalOrClientConsent_whenHandled_thenThrowIllegalStateException(boolean isApplicantSolicitor) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(isApplicantSolicitor);
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
        verify(caseRoleService).isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenLoginAsApplicantFlag_whenHandled_thenPopulatePartyToChangeRepresented(boolean isApplicantRepresentative) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(isApplicantRepresentative);

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
        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @Test
    void givenNoSameOrganisationBarrister_whenHandled_thenDoesNotRemoveBarrister() {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(true);

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

        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenLoginAsApplicantFlag_whenHandled_thenRemoveApplicantBarristerAccordingly(boolean loginAsApplicantFlag) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(loginAsApplicantFlag);

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
            .hasSize(loginAsApplicantFlag ? 0 : 1);

        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenLoginAsApplicantFlag_whenHandled_thenRemoveRespondentBarristerAccordingly(boolean loginAsApplicantFlag) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(loginAsApplicantFlag);

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
            .hasSize(loginAsApplicantFlag ? 1 : 0);

        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource
    void givenLoginAsApplicantFlag_whenHandled_thenPopulateServiceAddress(CaseType caseType,
        boolean loginAsApplicantFlag, boolean addressConfidentiality) {
        when(caseRoleService.isApplicantRepresentative(any(FinremCaseData.class), eq(AUTH_TOKEN)))
            .thenReturn(loginAsApplicantFlag);

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
                loginAsApplicantFlag ? ContactDetailsWrapper::getApplicantAddress : ContactDetailsWrapper::getRespondentAddress,
                loginAsApplicantFlag ? ContactDetailsWrapper::getApplicantAddressHiddenFromRespondent
                    : ContactDetailsWrapper::getRespondentAddressHiddenFromApplicant,
                loginAsApplicantFlag ? ContactDetailsWrapper::getApplicantRepresented : (CONTESTED.equals(caseType)
                    ? ContactDetailsWrapper::getContestedRespondentRepresented
                    : ContactDetailsWrapper::getConsentedRespondentRepresented))
            .contains(serviceAddress,  YesOrNo.forValue(addressConfidentiality), YesOrNo.NO);
        verify(caseRoleService).isApplicantRepresentative(request.getCaseDetails().getData(), AUTH_TOKEN);
    }

    static Stream<Arguments> givenLoginAsApplicantFlag_whenHandled_thenPopulateServiceAddress() {
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

    private static Condition<NoticeOfChangeParty> expectedParty(boolean isApplicantSolicitor) {
        return new Condition<>(party ->
            isApplicantSolicitor ? APPLICANT.equals(party) : RESPONDENT.equals(party),
            "expected APPLICANT if applicant solicitor, otherwise RESPONDENT");
    }
}
