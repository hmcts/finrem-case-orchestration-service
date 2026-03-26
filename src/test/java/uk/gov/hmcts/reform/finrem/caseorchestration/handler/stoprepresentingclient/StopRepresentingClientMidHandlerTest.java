package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.RepresentativeInContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientMidHandlerTest {

    private StopRepresentingClientMidHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private StopRepresentingClientService stopRepresentingClientService;

    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientMidHandler(finremCaseDetailsMapper,
            stopRepresentingClientService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(MID_EVENT, CONSENTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(MID_EVENT, CONTESTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenIsHavingClientConsent_whenHandled_thenNoErrorPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).isEmpty();
    }

    @Test
    void givenIsHavingJudicialApproval_whenHandled_thenNoErrorPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepJudicialApproval(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).isEmpty();
    }

    @Test
    void givenIsNotHavingJudicialApproval_whenHandled_thenErrorPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(mock(YesOrNo.class))
                .stopRepJudicialApproval(YesOrNo.NO)
                .clientAddressForService(createValidAddress())
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "You cannot stop representing your client without either client consent or judicial approval. "
                + "You will need to make a general application to apply to come off record using the next step event 'general application"
        );
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO"})
    @NullSource
    void givenIsNotHavingClientConsent_whenHandled_thenErrorPopulated(YesOrNo stopRepJudicialApproval) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.NO)
                .stopRepJudicialApproval(stopRepJudicialApproval)
                .clientAddressForService(createValidAddress())
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "You cannot stop representing your client without either client consent or judicial approval. "
                + "You will need to make a general application to apply to come off record using the next step event 'general application"
        );
    }

    static Stream<Arguments> givenPostcodeMissingInMasterServiceAddress_whenHandled_thenErrorPopulated() {
        return Stream.of(
            Arguments.of(createEmptyPostcodeAddress(), createApplicantRepresentativeInContext(), "Applicant"),
            Arguments.of(createNullPostcodeAddress(), createApplicantRepresentativeInContext(), "Applicant"),
            Arguments.of(createEmptyPostcodeAddress(), createRespondentRepresentativeInContext(), "Respondent"),
            Arguments.of(createNullPostcodeAddress(), createRespondentRepresentativeInContext(), "Respondent")
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenPostcodeMissingInMasterServiceAddress_whenHandled_thenErrorPopulated(Address clientServiceAddress,
                                                                                   RepresentativeInContext representativeInContext,
                                                                                   String expectedPartyInError) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(clientServiceAddress)
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            representativeInContext);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(expectedPartyInError)
        );
    }

    static Stream<Arguments> extraClientScenarios() {
        return Stream.of(
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.INTERVENER1),
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.INTERVENER2),
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.INTERVENER3),
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.INTERVENER4),
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.APPLICANT),
            Arguments.of(createEmptyPostcodeAddress(), ExtraAddrType.RESPONDENT)
        );
    }

    @ParameterizedTest
    @MethodSource("extraClientScenarios")
    void givenPostcodeMissingInExtraClientAddress1_whenHandled_thenErrorPopulated(Address extraClientAddress,
                                                                                  ExtraAddrType extraAddrType) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .extraClientAddr1(extraClientAddress)
                .extraClientAddr1Id(extraAddrType.getId())
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            mock(RepresentativeInContext.class));

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(ExtraAddrType.describe(extraAddrType.getId()).orElseThrow())
        );
    }

    @ParameterizedTest
    @MethodSource("extraClientScenarios")
    void givenPostcodeMissingInExtraClientAddress2_whenHandled_thenErrorPopulated(Address extraClientAddress,
                                                                                  ExtraAddrType extraAddrType) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .extraClientAddr2(extraClientAddress)
                .extraClientAddr2Id(extraAddrType.getId())
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            mock(RepresentativeInContext.class));

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(ExtraAddrType.describe(extraAddrType.getId()).orElseThrow())
        );
    }

    @ParameterizedTest
    @MethodSource("extraClientScenarios")
    void givenPostcodeMissingInExtraClientAddress3_whenHandled_thenErrorPopulated(Address extraClientAddress,
                                                                                  ExtraAddrType extraAddrType) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .extraClientAddr3(extraClientAddress)
                .extraClientAddr3Id(extraAddrType.getId())
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            mock(RepresentativeInContext.class));

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(ExtraAddrType.describe(extraAddrType.getId()).orElseThrow())
        );
    }

    @ParameterizedTest
    @MethodSource("extraClientScenarios")
    void givenPostcodeMissingInExtraClientAddress4_whenHandled_thenErrorPopulated(Address extraClientAddress,
                                                                                  ExtraAddrType extraAddrType) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createValidAddress())
                .extraClientAddr4(extraClientAddress)
                .extraClientAddr4Id(extraAddrType.getId())
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            mock(RepresentativeInContext.class));

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(ExtraAddrType.describe(extraAddrType.getId()).orElseThrow())
        );
    }

    @Test
    void givenMultiplePostcodesAreMissing_whenHandled_thenErrorsPopulated() {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .clientAddressForService(createNullPostcodeAddress())
                .extraClientAddr1(createEmptyPostcodeAddress())
                .extraClientAddr1Id(ExtraAddrType.INTERVENER1.getId())
                .extraClientAddr2(createEmptyPostcodeAddress())
                .extraClientAddr2Id(ExtraAddrType.INTERVENER2.getId())
                .build())
            .build();

        when(stopRepresentingClientService.buildRepresentation(caseData, AUTH_TOKEN)).thenReturn(
            createApplicantRepresentativeInContext());

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "%s's postcode field is required".formatted(ExtraAddrType.describe(ExtraAddrType.APPLICANT.getId()).orElseThrow()),
            "%s's postcode field is required".formatted(ExtraAddrType.describe(ExtraAddrType.INTERVENER1.getId()).orElseThrow()),
            "%s's postcode field is required".formatted(ExtraAddrType.describe(ExtraAddrType.INTERVENER2.getId()).orElseThrow())
        );
    }

    private static Address createValidAddress() {
        Address address = mock(Address.class);
        when(address.getPostCode()).thenReturn("PO53C 0D3");
        return address;
    }

    private static Address createEmptyPostcodeAddress() {
        Address address = mock(Address.class);
        when(address.getPostCode()).thenReturn("");
        return address;
    }

    private static Address createNullPostcodeAddress() {
        Address address = mock(Address.class);
        when(address.getPostCode()).thenReturn(null);
        return address;
    }

    private static RepresentativeInContext createApplicantRepresentativeInContext() {
        return new RepresentativeInContext(TEST_USER_ID, true, false, null, null);
    }

    private static RepresentativeInContext createRespondentRepresentativeInContext() {
        return new RepresentativeInContext(TEST_USER_ID, false, true, null, null);
    }
}
