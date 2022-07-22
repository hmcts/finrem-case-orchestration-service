package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@Service
@RequiredArgsConstructor
public class FinremCallbackRequestDeserializer implements Deserializer<CallbackRequest> {

    private static final String APP_ADDRESS_CONFIDENTIAL = "applicantAddressConfidential";
    private static final String RESP_ADDRESS_CONFIDENTIAL = "respondentAddressConfidential";
    private static final int APPLICANT_KEY_LENGTH = APP_ADDRESS_CONFIDENTIAL.length();
    private static final int RESPONDENT_KEY_LENGTH = RESP_ADDRESS_CONFIDENTIAL.length();
    private static final int TRIMMABLE = 4;
    private final char YES_FIRST_LETTER = 'Y';


    private final ObjectMapper mapper;

    @Override
    public CallbackRequest deserialize(String source) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            CallbackRequest callbackRequest = mapper.readValue(source, new TypeReference<>() {});
            callbackRequest.getCaseDetails().getCaseData().setCcdCaseType(callbackRequest.getCaseDetails().getCaseType());
            callbackRequest.getCaseDetails().getCaseData().setCcdCaseId(String.valueOf(callbackRequest.getCaseDetails().getId()));
            callbackRequest.getCaseDetails().getCaseData().getContactDetailsWrapper()
                .setApplicantAddressHiddenFromRespondent(getAddressConfidential(source, APP_ADDRESS_CONFIDENTIAL, APPLICANT_KEY_LENGTH));
            callbackRequest.getCaseDetails().getCaseData().getContactDetailsWrapper()
                    .setRespondentAddressHiddenFromApplicant(getAddressConfidential(source, RESP_ADDRESS_CONFIDENTIAL, RESPONDENT_KEY_LENGTH));

            Optional.ofNullable(callbackRequest.getCaseDetailsBefore())
                .flatMap(caseDetails -> Optional.ofNullable(caseDetails.getCaseData()))
                .ifPresent(caseData -> caseData.setCcdCaseType(callbackRequest.getCaseDetails().getCaseType()));

            return callbackRequest;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not deserialize callback %s", e.getMessage()), e);
        }
    }

    public YesOrNo getAddressConfidential(String source,
                                          String field,
                                          int fieldLength) {
        if (!source.contains(field)) {
            return null;
        }

        int startIndex = source.indexOf(field) + fieldLength;
        startIndex += getNextAlphanumericIndex(source, startIndex);
        int sizeOfValue = source.charAt(startIndex) == YES_FIRST_LETTER ? YES_VALUE.length() : NO_VALUE.length();
        int endIndex = startIndex + sizeOfValue;
        return YesOrNo.forValue(source.substring(startIndex, endIndex));
    }

    private int getNextAlphanumericIndex(String source, int index) {
        int i = index;
        while (!Character.isAlphabetic(source.charAt(i))) {
            i ++;
        }
        return i - index;
    }
}
