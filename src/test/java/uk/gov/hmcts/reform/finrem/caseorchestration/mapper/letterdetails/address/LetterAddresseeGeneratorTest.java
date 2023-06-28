package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@RunWith(MockitoJUnitRunner.class)
public class LetterAddresseeGeneratorTest {
    LetterAddresseeGenerator letterAddresseeGenerator;
    @Mock
    ApplicantAddresseeGenerator applicantAddresseeGenerator;
    @Mock
    RespondentAddresseeGenerator respondentAddresseeGenerator;
    @Mock
    IntervenerOneAddresseeGenerator intervenerOneAddresseeGenerator;
    @Mock
    IntervenerTwoAddresseeGenerator intervenerTwoAddresseeGenerator;
    @Mock
    IntervenerThreeAddresseeGenerator intervenerThreeAddresseeGenerator;
    @Mock
    IntervenerFourAddresseeGenerator intervenerFourAddresseeGenerator;
    @Mock
    CaseDetails caseDetails;
    @Mock
    FinremCaseDetails finremCaseDetails;

    @Before
    public void setUp() throws Exception {
        letterAddresseeGenerator = new LetterAddresseeGenerator(applicantAddresseeGenerator, respondentAddresseeGenerator,
            intervenerOneAddresseeGenerator, intervenerTwoAddresseeGenerator, intervenerThreeAddresseeGenerator,
            intervenerFourAddresseeGenerator);
    }

    @Test
    public void shouldCallAppropriateLetterGenerator() {
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        Mockito.verify(applicantAddresseeGenerator).generate(caseDetails);
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        Mockito.verify(respondentAddresseeGenerator).generate(caseDetails);
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);
        Mockito.verify(intervenerOneAddresseeGenerator).generate(caseDetails);
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO);
        Mockito.verify(intervenerTwoAddresseeGenerator).generate(caseDetails);
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE);
        Mockito.verify(intervenerThreeAddresseeGenerator).generate(caseDetails);
        letterAddresseeGenerator.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR);
        Mockito.verify(intervenerFourAddresseeGenerator).generate(caseDetails);
    }

}
