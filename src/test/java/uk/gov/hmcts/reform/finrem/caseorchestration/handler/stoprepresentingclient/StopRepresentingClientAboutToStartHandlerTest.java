package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.RepresentativeInContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.IntervenerRole.BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.IntervenerRole.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToStartHandlerTest {

    @Mock
    private StopRepresentingClientService stopRepresentingClientService;

    @InjectMocks
    private StopRepresentingClientAboutToStartHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_START, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_START, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenAsApplicantSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new RepresentativeInContext(TEST_USER_ID, true, false,null, null)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Applicant)",
                "Keep the Applicant's contact details private from the Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsRespondentSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new RepresentativeInContext(TEST_USER_ID, false, true, null, null)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Respondent)",
                "Keep the Respondent's contact details private from the Applicant?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsIntervenerSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new RepresentativeInContext(TEST_USER_ID, false, false, 1, SOLICITOR)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 1)",
                "Keep the Intervener 1's contact details private from the Applicant & Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerBarristerWithCorrespondingIntvSol_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(true);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Intervener 2's contact details private from the Applicant & Respondent?");

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 2)",
                "Keep the Intervener 2's contact details private from the Applicant & Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerBarristerWithoutCorrespondingIntvSol_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(false);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.NO,
                null,
                null
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerBarristerWithoutCorrespondingIntvSolButRespondentSol_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr2Barristers(List.of(BarristerCollectionItem.builder()
                    .value(Barrister.builder().userId(TEST_USER_ID).organisation(organisation(TEST_ORG_ID)).build())
                    .build()))
                .build())
            .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
            .build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(false);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel,
                StopRepresentationWrapper::getExtraClientAddr1Label
            )
            .containsExactly(
                YesOrNo.NO,
                null,
                null,
                "Client's address for service (Respondent)"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerBarristerWithSameOrgRespondentSolAndIntv1Sol_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr1Barristers(List.of(BarristerCollectionItem.builder()
                    .value(Barrister.builder().userId(TEST_USER_ID).organisation(organisation(TEST_ORG_ID)).build())
                    .build()))
                .build())
            .intervenerTwo(IntervenerTwo.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG_ID))
                .build())
            .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
            .build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 1, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(false);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel,
                StopRepresentationWrapper::getExtraClientAddr1Label,
                StopRepresentationWrapper::getExtraClientAddr2Label
            )
            .containsExactly(
                YesOrNo.NO,
                null,
                null,
                "Client's address for service (Respondent)",
                "Client's address for service (Intervener 2)"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerBarristerWithSameOrgIntervenerSolicitors_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr2Barristers(List.of(BarristerCollectionItem.builder()
                    .value(Barrister.builder().userId(TEST_USER_ID).organisation(organisation(TEST_ORG_ID)).build())
                    .build()))
                .build())
            .respondentOrganisationPolicy(organisationPolicy(TEST_ORG_ID))
            .intervenerOne(IntervenerOne.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG_ID))
                .build())
            .intervenerThree(IntervenerThree.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG_ID))
                .build())
            .intervenerFour(IntervenerFour.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG_ID))
                .build())
            .build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(false);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel,
                StopRepresentationWrapper::getExtraClientAddr1Label,
                StopRepresentationWrapper::getExtraClientAddr2Label,
                StopRepresentationWrapper::getExtraClientAddr3Label,
                StopRepresentationWrapper::getExtraClientAddr4Label
            )
            .containsExactly(
                YesOrNo.NO,
                null,
                null,
                "Client's address for service (Respondent)",
                "Client's address for service (Intervener 1)",
                "Client's address for service (Intervener 3)",
                "Client's address for service (Intervener 4)"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerTwoBarristerAndApplicantHavingSameOrgId_whenHandled_thenPopulateCorrectLabels() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr2Barristers(List.of(BarristerCollectionItem.builder()
                    .value(Barrister.builder().userId(TEST_USER_ID).organisation(organisation(TEST_ORG_ID)).build())
                    .build()))
                .build())
            .applicantOrganisationPolicy(OrganisationPolicy.builder()
                .organisation(organisation(TEST_ORG_ID))
                .build())
            .build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(true);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Intervener 2's contact details private from the Applicant & Respondent?");

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel,
                StopRepresentationWrapper::getExtraClientAddr1Label
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 2)",
                "Keep the Intervener 2's contact details private from the Applicant & Respondent?",
                "Client's address for service (Applicant)"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenIntervenerTwoBarristerAndRespondentHavingSameOrgId_whenHandled_thenPopulateCorrectLabels() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder()
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .intvr2Barristers(List.of(BarristerCollectionItem.builder()
                    .value(Barrister.builder().userId(TEST_USER_ID).organisation(organisation(TEST_ORG_ID)).build())
                    .build()))
                .build())
            .respondentOrganisationPolicy(OrganisationPolicy.builder()
                .organisation(organisation(TEST_ORG_ID))
                .build())
            .build();
        RepresentativeInContext representativeInContext = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representativeInContext = new RepresentativeInContext(TEST_USER_ID, false, false, 2, BARRISTER)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representativeInContext))
            .thenReturn(true);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Intervener 2's contact details private from the Applicant & Respondent?");

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel,
                StopRepresentationWrapper::getExtraClientAddr1Label
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 2)",
                "Keep the Intervener 2's contact details private from the Applicant & Respondent?",
                "Client's address for service (Respondent)"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsCaseworker_whenHandled_thenExceptionIsThrown() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new RepresentativeInContext(TEST_USER_ID, false, false, null, null)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        assertThatThrownBy(() -> underTest.handle(callbackRequest, AUTH_TOKEN))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage(CASE_ID + " - It supports applicant/respondent representatives only");

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }
}
