package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;

@ExtendWith(MockitoExtension.class)
class InternationalPostalServiceTest {

    @InjectMocks
    private InternationalPostalService underTest;

    @Test
    void givenContestedCase_whenBothPartiesAreDomesticOrInternational_thenValidate() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantAddress(Address.builder().addressLine1("123 London Road").build());
        wrapper.setRespondentAddress(Address.builder().addressLine1("456 London Road").build());

        List<String> validate = underTest.validate(finremCaseData);
        assertThat(validate).isEmpty();

        wrapper.setApplicantResideOutsideUK(YesOrNo.YES);
        wrapper.setRespondentResideOutsideUK(YesOrNo.YES);

        validate = underTest.validate(finremCaseData);
        assertThat(validate).containsExactlyInAnyOrder(
            "If applicant resides outside of UK, please provide the country of residence.",
            "If respondent resides outside of UK, please provide the country of residence."
        );
    }

    @Test
    void givenContestedCaseLegacy_whenBothPartiesAreDomesticOrInternational_thenValidate() {
        Map<String, Object> caseData = new HashMap<>();
        Address address = Address.builder().addressLine1("123 London Road").build();
        caseData.put("applicantAddress", address);
        caseData.put("respondentAddress", address);

        List<String> validate = underTest.validate(caseData);
        assertEquals(0, validate.size());

        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "Yes");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "Yes");

        validate = underTest.validate(caseData);
        assertThat(validate).containsExactlyInAnyOrder(
            "If applicant resides outside of UK, please provide the country of residence.",
            "If respondent resides outside of UK, please provide the country of residence."
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenContestedCase_whenRecipientResideOutsidesOfUK_thenReturnTrue(FinremCaseData caseData,
                                                                          String recipient,
                                                                          boolean expected) {
        assertThat(underTest.isRecipientResideOutsideOfUK(caseData, recipient)).isEqualTo(expected);
    }

    private static Stream<Arguments> givenContestedCase_whenRecipientResideOutsidesOfUK_thenReturnTrue() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantResideOutsideUK(YesOrNo.YES);
        wrapper.setRespondentResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerOne().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerTwo().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerThree().setIntervenerResideOutsideUK(YesOrNo.YES);
        finremCaseData.getIntervenerFour().setIntervenerResideOutsideUK(YesOrNo.YES);

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
        assertThat(underTest.isRecipientResideOutsideOfUK(caseData, recipient)).isEqualTo(expected);
    }

    private static Stream<Arguments> givenContestedCase_whenRecipientResidesInUK_thenReturnFalse() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        ContactDetailsWrapper wrapper = finremCaseData.getContactDetailsWrapper();
        wrapper.setApplicantResideOutsideUK(YesOrNo.NO);
        wrapper.setRespondentResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerOne().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerTwo().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerThree().setIntervenerResideOutsideUK(YesOrNo.NO);
        finremCaseData.getIntervenerFour().setIntervenerResideOutsideUK(YesOrNo.NO);

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
        boolean recipientResideOutsideOfUK = underTest.isRecipientResideOutsideOfUK(caseData, recipient);
        assertThat(recipientResideOutsideOfUK).isEqualTo(expected);
    }

    private static Stream<Arguments> givenContestedCaseLegacyStyle_whenRecipientResidesOutsideOfUK_thenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "Yes");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "Yes");

        IntervenerWrapper intervenerWrapper1 = IntervenerOne.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener1", intervenerWrapper1);
        IntervenerWrapper intervenerWrapper2 = IntervenerTwo.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener2", intervenerWrapper2);
        IntervenerWrapper intervenerWrapper3 = IntervenerThree.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
        caseData.put("intervener3", intervenerWrapper3);
        IntervenerWrapper intervenerWrapper4 = IntervenerFour.builder().intervenerResideOutsideUK(YesOrNo.YES).build();
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
        boolean recipientResideOutsideOfUK = underTest.isRecipientResideOutsideOfUK(caseData, recipient);
        assertThat(recipientResideOutsideOfUK).isEqualTo(expected);
    }

    private static Stream<Arguments> givenContestedCaseLegacyStyle_whenRecipientNotResidesOutsideOfUK_thenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, "No");
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, "No");

        IntervenerWrapper intervenerWrapper1 = IntervenerOne.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener1", intervenerWrapper1);
        IntervenerWrapper intervenerWrapper2 = IntervenerTwo.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener2", intervenerWrapper2);
        IntervenerWrapper intervenerWrapper3 = IntervenerThree.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
        caseData.put("intervener3", intervenerWrapper3);
        IntervenerWrapper intervenerWrapper4 = IntervenerFour.builder().intervenerResideOutsideUK(YesOrNo.NO).build();
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
}
