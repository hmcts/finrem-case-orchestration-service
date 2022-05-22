package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class AddresseeBuilderServiceTest {

    @InjectMocks
    private AddresseeGeneratorService addresseeBuilderService;

    @Mock
    ApplicantAddresseeGenerator applicantAddresseeGenerator;
    @Mock
    RespondentAddresseeGenerator respondentAddresseeGenerator;
    @Mock
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails = CaseDetails.builder().build();
    ChangedRepresentative changedRepresentative = ChangedRepresentative.builder().build();

    @Test
    public void givenRecipientIsSolicitorThenShouldCallSolicitorAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, changedRepresentative,
            DocumentHelper.PaperNotificationRecipient.SOLICITOR, "applicant");
        verify(solicitorAddresseeGenerator).generate(caseDetails, changedRepresentative, "applicant");
    }

    @Test
    public void givenRecipientIsApplicantThenShouldCallApplicantAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, changedRepresentative,
            DocumentHelper.PaperNotificationRecipient.APPLICANT, "applicant");
        verify(applicantAddresseeGenerator).generate(caseDetails, changedRepresentative, "applicant");
    }

    @Test
    public void givenRecipientIsRespondenthenShouldCallRespondentAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, changedRepresentative,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT, "respondent");
        verify(respondentAddresseeGenerator).generate(caseDetails, changedRepresentative, "respondent");
    }

}