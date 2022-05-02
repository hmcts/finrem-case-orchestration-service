package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

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
    private AddresseeGeneratorService addresseeBuilderService;

    @Mock
    ApplicantAddresseeGenerator applicantAddresseeGenerator;
    @Mock
    RespondentAddresseeGenerator respondentAddresseeGenerator;
    @Mock
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    CaseDetails caseDetails = CaseDetails.builder().build();

    @Test
    public void givenRecipientIsSolicitorThenShouldCallSolicitorAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.SOLICITOR);
        verify(solicitorAddresseeGenerator).generate(caseDetails);

    }

    @Test
    public void givenRecipientIsApplicantThenShouldCallApplicantAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT);
        verify(applicantAddresseeGenerator).generate(caseDetails);

    }

    @Test
    public void givenRecipientIsRespondenthenShouldCallRespondentAddressGenerator() {

        addresseeBuilderService.generateAddressee(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
        verify(respondentAddresseeGenerator).generate(caseDetails);

    }


}