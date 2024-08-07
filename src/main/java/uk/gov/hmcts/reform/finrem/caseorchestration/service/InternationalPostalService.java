package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Service
public class InternationalPostalService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public InternationalPostalService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Validates the given FinremCaseData for any errors related to contact details.
     *
     * @param caseData The FinremCaseData to validate.
     * @return A list of error messages. An empty list indicates no errors were found.
     */
    public List<String> validate(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        YesOrNo applicantResideOutsideUK = wrapper.getApplicantResideOutsideUK();
        List<String> errors = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(applicantResideOutsideUK)
            && applicantResideOutsideUK.equals(YesOrNo.YES)
            && wrapper.getApplicantAddress() != null
            && ObjectUtils.isEmpty(wrapper.getApplicantAddress().getCountry())) {
            errors.add("If applicant resides outside of UK, please provide the country of residence.");
        }
        YesOrNo respondentResideOutsideUK = wrapper.getRespondentResideOutsideUK();
        if (ObjectUtils.isNotEmpty(respondentResideOutsideUK)
            && respondentResideOutsideUK.equals(YesOrNo.YES)
            && wrapper.getRespondentAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentAddress().getCountry())) {
            errors.add("If respondent resides outside of UK, please provide the country of residence.");
        }
        return errors;
    }

    public List<String> validate(Map<String, Object> caseData) {
        String applicantOutsideUK = String.valueOf(caseData.get(APPLICANT_RESIDE_OUTSIDE_UK));
        Address applicantAddress = getAddress(caseData.get("applicantAddress"));

        List<String> errors = new ArrayList<>();
        if (applicantOutsideUK.equals("Yes")
            && applicantAddress != null
            && ObjectUtils.isEmpty(applicantAddress.getCountry())) {
            errors.add("If applicant resides outside of UK, please provide the country of residence.");
        }

        String respondentOutsideUK = String.valueOf(caseData.get(RESPONDENT_RESIDE_OUTSIDE_UK));
        Address respondentAddress = getAddress(caseData.get("respondentAddress"));

        if (respondentOutsideUK.equals("Yes")
            && respondentAddress != null
            && ObjectUtils.isEmpty(respondentAddress.getCountry())) {
            errors.add("If respondent resides outside of UK, please provide the country of residence.");
        }
        return errors;
    }

    /**
     * Determines whether the recipient of a letter resides outside of the UK based on the given case data and recipient.
     *
     * @param caseData The case data as a map of key-value pairs.
     * @param recipient The recipient of the letter.
     * @return True if the recipient resides outside of the UK, false otherwise.
     */
    public boolean isRecipientResideOutsideOfUK(Map<String, Object> caseData, String recipient) {
        String addressee = WordUtils.capitalizeFully(String.valueOf(recipient));
        boolean isInternational;
        switch (addressee) {
            case APPLICANT -> isInternational = isApplicantResideOutsideOfUK(caseData);
            case RESPONDENT -> isInternational = isRespondentResideOutsideOfUK(caseData);
            case INTERVENER1 -> {
                IntervenerWrapper intervenerWrapper = convertToIntervener1Wrapper(caseData.get(INTERVENER_ONE));
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER2 -> {
                IntervenerWrapper intervenerWrapper = convertToIntervener2Wrapper(caseData.get(INTERVENER_TWO));
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER3 -> {
                IntervenerWrapper intervenerWrapper = convertToIntervener3Wrapper(caseData.get(INTERVENER_THREE));
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER4 -> {
                IntervenerWrapper intervenerWrapper = convertToIntervener4Wrapper(caseData.get(INTERVENER_FOUR));
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            default -> isInternational = false;
        }
        return isInternational;
    }

    public boolean isRecipientResideOutsideOfUK(FinremCaseData caseData, String recipient) {
        String addressee = WordUtils.capitalizeFully(String.valueOf(recipient));
        boolean isInternational;
        switch (addressee) {
            case APPLICANT -> isInternational = isApplicantResideOutsideOfUK(caseData);
            case RESPONDENT -> isInternational = isRespondentResideOutsideOfUK(caseData);
            case INTERVENER1 -> {
                IntervenerWrapper intervenerWrapper = caseData.getIntervenerOne();
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER2 -> {
                IntervenerWrapper intervenerWrapper = caseData.getIntervenerTwo();
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER3 -> {
                IntervenerWrapper intervenerWrapper = caseData.getIntervenerThree();
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            case INTERVENER4 -> {
                IntervenerWrapper intervenerWrapper = caseData.getIntervenerFour();
                isInternational = isIntervenerResideOutsideOfUK(intervenerWrapper);
            }
            default -> isInternational = false;
        }
        return isInternational;
    }

    public boolean isApplicantResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        YesOrNo applicantResideOutsideUK = wrapper.getApplicantResideOutsideUK();
        return YesOrNo.YES.equals(applicantResideOutsideUK);
    }

    public boolean isApplicantResideOutsideOfUK(Map<String, Object> caseData) {
        String applicantResideOutsideUK = (String) caseData.get(APPLICANT_RESIDE_OUTSIDE_UK);
        return "Yes".equalsIgnoreCase(applicantResideOutsideUK);
    }


    public boolean isRespondentResideOutsideOfUK(FinremCaseData caseData) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        YesOrNo respondentResideOutsideUK = wrapper.getRespondentResideOutsideUK();
        return YesOrNo.YES.equals(respondentResideOutsideUK);
    }

    public boolean isRespondentResideOutsideOfUK(Map<String, Object> caseData) {
        String respondentResideOutsideUK = (String) caseData.get(RESPONDENT_RESIDE_OUTSIDE_UK);
        return "Yes".equalsIgnoreCase(respondentResideOutsideUK);
    }

    public boolean isIntervenerResideOutsideOfUK(IntervenerWrapper intervenerWrapper) {
        if (intervenerWrapper == null) {
            return false;
        }
        YesOrNo intervenerResideOutsideUK = intervenerWrapper.getIntervenerResideOutsideUK();
        return YesOrNo.YES.equals(intervenerResideOutsideUK);
    }

    private IntervenerWrapper convertToIntervener1Wrapper(Object object) {
        return objectMapper.convertValue(object, new TypeReference<IntervenerOne>() {
        });
    }

    private IntervenerWrapper convertToIntervener2Wrapper(Object object) {
        return objectMapper.convertValue(object, new TypeReference<IntervenerTwo>() {
        });
    }

    private IntervenerWrapper convertToIntervener3Wrapper(Object object) {
        return objectMapper.convertValue(object, new TypeReference<IntervenerThree>() {
        });
    }

    private IntervenerWrapper convertToIntervener4Wrapper(Object object) {
        return objectMapper.convertValue(object, new TypeReference<IntervenerFour>() {
        });
    }

    private Address getAddress(Object object) {
        if (object != null) {
            return objectMapper.convertValue(object, new TypeReference<>() {
            });
        }
        return null;
    }
}
