package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetterAddresseeGeneratorMapper {
    private final ApplicantLetterAddresseeGenerator applicantAddresseeGenerator;
    private final RespondentLetterAddresseeGenerator respondentAddresseeGenerator;
    private final IntervenerOneLetterAddresseeGenerator intervenerOneAddresseeGenerator;
    private final IntervenerTwoLetterAddresseeGenerator intervenerTwoAddresseeGenerator;
    private final IntervenerThreeLetterAddresseeGenerator intervenerThreeAddresseeGenerator;
    private final IntervenerFourLetterAddresseeGenerator intervenerFourAddresseeGenerator;

    public AddresseeDetails generate(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        return addresseeGeneratorMap().get(recipient).generate(caseDetails);
    }

    public AddresseeDetails generate(FinremCaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient) {
        return addresseeGeneratorMap().get(recipient).generate(caseDetails);
    }


    private Map<DocumentHelper.PaperNotificationRecipient, LetterAddresseeGenerator> addresseeGeneratorMap() {
        ImmutableMap<DocumentHelper.PaperNotificationRecipient, LetterAddresseeGenerator>
            map =
            ImmutableMap.of(DocumentHelper.PaperNotificationRecipient.APPLICANT, applicantAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.RESPONDENT, respondentAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE, intervenerOneAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO, intervenerTwoAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE, intervenerThreeAddresseeGenerator,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR, intervenerFourAddresseeGenerator
            );
        return map;
    }
}
