package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.address.address;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class AddresseeBuilderServiceTest {

    @InjectMocks
    private AddresseeBuilderService addresseeBuilderService;

    @Mock
    ApplicantAddresseeGenerator applicantAddresseeGenerator;
    @Mock
    RespondentAddresseeGenerator respondentAddresseeGenerator;
    @Mock
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails = CaseDetails.builder().build();

    @Test
    public void givenRecipientIsSolicitorThenShouldCallSolicitorAddressGenerator() {

        addresseeBuilderService.buildAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.SOLICITOR);
        verify(solicitorAddresseeGenerator).generate(caseDetails);

    }

    @Test
    public void givenRecipientIsApplicantThenShouldCallApplicantAddressGenerator() {

        addresseeBuilderService.buildAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(applicantAddresseeGenerator).generate(caseDetails);

    }

    @Test
    public void givenRecipientIsRespondenthenShouldCallRespondentAddressGenerator() {

        addresseeBuilderService.buildAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(respondentAddresseeGenerator).generate(caseDetails);

    }


}