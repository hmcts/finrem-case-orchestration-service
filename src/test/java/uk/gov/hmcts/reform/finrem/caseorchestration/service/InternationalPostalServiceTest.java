package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class InternationalPostalServiceTest extends BaseServiceTest  {

    private InternationalPostalService postalService;

    @BeforeEach
    void setUp() {
        postalService = new InternationalPostalService();
    }

    @Test
    void givenContestedCase_whenBothPartiesAreDomesticOrInternational_thenValidate() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantAddress(Address.builder().addressLine1("123 London Road").build());
        wrapper.setRespondentAddress(Address.builder().addressLine1("456 London Road").build());

        List<String> validate = postalService.validate(finremCaseData);
        assertEquals(0, validate.size());

        wrapper.setApplicantResideOutsideUK(YesOrNo.YES);
        wrapper.setRespondentResideOutsideUK(YesOrNo.YES);

        validate = postalService.validate(finremCaseData);
        assertEquals(2, validate.size());
        assertTrue(validate.contains("If applicant resides outside of UK, please provide the country of residence."));
        assertTrue(validate.contains("If respondent resides outside of UK, please provide the country of residence."));

    }

    @Test
    void givenContestedCaseLegacy_whenBothPartiesAreDomesticOrInternational_thenValidate() {
        Map<String, Object> caseData = new HashMap<>();
        Address address = Address.builder().addressLine1("123 London Road").build();
        caseData.put("applicantAddress", address);
        caseData.put("respondentAddress", address);


        List<String> validate = postalService.validate(caseData);
        assertEquals(0, validate.size());

        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "Yes");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "Yes");

        validate = postalService.validate(caseData);
        assertEquals(2, validate.size());
        assertTrue(validate.contains("If applicant resides outside of UK, please provide the country of residence."));
        assertTrue(validate.contains("If respondent resides outside of UK, please provide the country of residence."));
    }

    @ParameterizedTest
    @MethodSource
    void givenContestedCase_whenRecipientResideOutsidesOfUK_thenReturnTrue(FinremCaseData caseData,
                                                                          String recipient,
                                                                          boolean expected) {
        assertThat(postalService.isRecipientResideOutsideOfUK(caseData, recipient)).isEqualTo(expected);
    }

    private Stream<Arguments> givenContestedCase_whenRecipientResideOutsidesOfUK_thenReturnTrue() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantResideOutsideUK(YesOrNo.YES);
        wrapper.setRespondentResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerOneWrapper().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerTwoWrapper().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerThreeWrapper().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerFourWrapper().setIntervenerResideOutsideUK(YesOrNo.YES);

        return Stream.of(
            Arguments.of(finremCaseData, APPLICANT, true),
            Arguments.of(finremCaseData, RESPONDENT, true),
            Arguments.of(finremCaseData, INTERVENER1, true),
            Arguments.of(finremCaseData, INTERVENER2, true),
            Arguments.of(finremCaseData, INTERVENER3, true),
            Arguments.of(finremCaseData, INTERVENER4, true),
            Arguments.of(finremCaseData, OTHER, false),
            Arguments.of(finremCaseData, null, false)
        );
    }


    @ParameterizedTest
    @MethodSource
    void givenContestedCase_whenRecipientResidesInUK_thenReturnFalse(FinremCaseData caseData,
                                                                          String recipient,
                                                                          boolean expected) {
        assertThat(postalService.isRecipientResideOutsideOfUK(caseData, recipient)).isEqualTo(expected);
    }

    private Stream<Arguments> givenContestedCase_whenRecipientResidesInUK_thenReturnFalse() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantResideOutsideUK(YesOrNo.NO);
        wrapper.setRespondentResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerOneWrapper().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerTwoWrapper().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerThreeWrapper().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerFourWrapper().setIntervenerResideOutsideUK(YesOrNo.NO);

        return Stream.of(
            Arguments.of(finremCaseData, APPLICANT, false),
            Arguments.of(finremCaseData, RESPONDENT, false),
            Arguments.of(finremCaseData, INTERVENER1, false),
            Arguments.of(finremCaseData, INTERVENER2, false),
            Arguments.of(finremCaseData, INTERVENER3, false),
            Arguments.of(finremCaseData, INTERVENER4, false),
            Arguments.of(finremCaseData, OTHER, false),
            Arguments.of(finremCaseData, null, false)
        );
    }


    @ParameterizedTest
    @MethodSource
    void givenContestedCaseLegacyStyle_whenRecipientResidesOutsideOfUK_thenReturnTrue(Map<String, Object> caseData,
                                                                                     String recipient,
                                                                                     boolean expected) {
        boolean recipientResideOutsideOfUK = postalService.isRecipientResideOutsideOfUK(caseData, recipient);
        assertThat(recipientResideOutsideOfUK).isEqualTo(expected);
    }

    private Stream<Arguments> givenContestedCaseLegacyStyle_whenRecipientResidesOutsideOfUK_thenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "Yes");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "Yes");

        IntervenerWrapper intervenerWrapper1 = IntervenerOneWrapper.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener1", intervenerWrapper1);
        IntervenerWrapper intervenerWrapper2 = IntervenerTwoWrapper.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener2", intervenerWrapper2);
        IntervenerWrapper intervenerWrapper3 = IntervenerThreeWrapper.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener3", intervenerWrapper3);
        IntervenerWrapper intervenerWrapper4 = IntervenerFourWrapper.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener4", intervenerWrapper4);


        return Stream.of(
            Arguments.of(caseData, APPLICANT, true),
            Arguments.of(caseData, RESPONDENT, true),
            Arguments.of(caseData, INTERVENER1, true),
            Arguments.of(caseData, INTERVENER2, true),
            Arguments.of(caseData, INTERVENER3, true),
            Arguments.of(caseData, INTERVENER4, true)
        );
    }


    @ParameterizedTest
    @MethodSource
    void givenContestedCaseLegacyStyle_whenRecipientNotResidesOutsideOfUK_thenReturnTrue(Map<String, Object> caseData,
                                                                                     String recipient,
                                                                                     boolean expected) {
        boolean recipientResideOutsideOfUK = postalService.isRecipientResideOutsideOfUK(caseData, recipient);
        assertThat(recipientResideOutsideOfUK).isEqualTo(expected);
    }

    private Stream<Arguments> givenContestedCaseLegacyStyle_whenRecipientNotResidesOutsideOfUK_thenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "No");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "No");

        IntervenerWrapper intervenerWrapper1 = IntervenerOneWrapper.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener1", intervenerWrapper1);
        IntervenerWrapper intervenerWrapper2 = IntervenerTwoWrapper.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener2", intervenerWrapper2);
        IntervenerWrapper intervenerWrapper3 = IntervenerThreeWrapper.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener3", intervenerWrapper3);
        IntervenerWrapper intervenerWrapper4 = IntervenerFourWrapper.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener4", intervenerWrapper4);


        return Stream.of(
            Arguments.of(caseData, APPLICANT, false),
            Arguments.of(caseData, RESPONDENT, false),
            Arguments.of(caseData, INTERVENER1, false),
            Arguments.of(caseData, INTERVENER2, false),
            Arguments.of(caseData, INTERVENER3, false),
            Arguments.of(caseData, INTERVENER4, false)
        );
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}