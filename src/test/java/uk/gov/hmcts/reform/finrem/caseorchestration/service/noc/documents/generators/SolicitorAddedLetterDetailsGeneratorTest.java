package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedLetterDetailsGeneratorTest extends AbstractLetterDetailsGeneratorTest {

    @InjectMocks
    private SolicitorAddedLetterDetailsGenerator solicitorAddedLetterDetailsGenerator;

    @Before
    public void setUpTest() {
        super.setUpTest();
        when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorAdded() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeGeneratorService.generateAddressee(caseDetailsBefore, changedRepresentativeAdded,
            DocumentHelper.PaperNotificationRecipient.APPLICANT, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorAddedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.ADD, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);

    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenAdded() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeGeneratorService.generateAddressee(caseDetails, changedRepresentativeAdded,
            DocumentHelper.PaperNotificationRecipient.SOLICITOR, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorAddedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.SOLICITOR);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.ADD, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
        assertThat(noticeOfChangeLetterDetails.getNoticeOfChangeText(),
            is("Your notice of change has been completed successfully. You can now view your client's case."));

    }


}
