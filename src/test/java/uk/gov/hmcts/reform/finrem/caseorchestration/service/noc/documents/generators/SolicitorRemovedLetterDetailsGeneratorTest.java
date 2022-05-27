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
public class SolicitorRemovedLetterDetailsGeneratorTest extends AbstractLetterDetailsGeneratorTest {

    @InjectMocks
    private SolicitorRemovedLetterDetailsGenerator solicitorRemovedLetterDetailsGenerator;

    @Before
    public void setUpTest() {
        super.setUpTest();
        when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        when(documentHelper.getRespondentFullNameConsented(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONSENTED);
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorRemoved() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);
        when(addresseeGeneratorService.generateAddressee(caseDetails, changedRepresentativeRemoved,
            DocumentHelper.PaperNotificationRecipient.APPLICANT, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorRemovedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, representationUpdate,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.TRUE);
        assertConsentedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);


    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenRemoved() {

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);
        when(addresseeGeneratorService.generateAddressee(caseDetailsBefore, changedRepresentativeRemoved,
            DocumentHelper.PaperNotificationRecipient.SOLICITOR, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorRemovedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.SOLICITOR);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
        assertThat(noticeOfChangeLetterDetails.getNoticeOfChangeText(),
            is("You've completed notice of acting on this, your access to this case has now been revoked."));

    }

}
