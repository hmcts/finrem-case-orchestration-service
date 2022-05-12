package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocSolicitorAddedLettersProcessor;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocSolicitorRemovedLettersProcessor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;

@RunWith(MockitoJUnitRunner.class)
public class NocLetterNotificationServiceTest {

    protected static final String AUTH_TOKEN = "authToken";
    NocLetterNotificationService noticeOfChangeLetterNotificationService;

    @Mock
    private NocSolicitorAddedLettersProcessor nocSolicitorAddedLettersProcessor;
    @Mock
    private NocSolicitorRemovedLettersProcessor nocSolicitorRemovedLettersProcessor;
    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;

    @Captor
    private ArgumentCaptor<RepresentationUpdate> changeOfRepresentationArgumentCaptor;

    @Before
    public void setUpTest() {
        noticeOfChangeLetterNotificationService =
            new NocLetterNotificationService(nocSolicitorAddedLettersProcessor, nocSolicitorRemovedLettersProcessor);
    }

    @Test
    public void shouldSendNoticeOfChangeLettersForAddedAndRemoved() {

        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke.json",
            new ObjectMapper());
        caseDetailsBefore =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-and-revoke-before.json",
                new ObjectMapper());

        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(nocSolicitorAddedLettersProcessor).processSolicitorAndLitigantLetters(any(CaseDetails.class), any(CaseDetails.class), anyString(),
            changeOfRepresentationArgumentCaptor.capture());

        RepresentationUpdate representationUpdate = changeOfRepresentationArgumentCaptor.getValue();
        assertThat(representationUpdate.getAdded().getOrganisation().getOrganisationName(), is("FRApplicantSolicitorFirmAdded"));

        verify(nocSolicitorRemovedLettersProcessor).processSolicitorAndLitigantLetters(any(CaseDetails.class), any(CaseDetails.class), anyString(),
            changeOfRepresentationArgumentCaptor.capture());

        RepresentationUpdate changeOfRepresentationRemoved = changeOfRepresentationArgumentCaptor.getValue();
        assertThat(changeOfRepresentationRemoved.getRemoved().getOrganisation().getOrganisationName(), is("FRApplicantSolicitorFirmRemoved"));
    }

    @Test
    public void shouldSendNoticeOfChangeLettersForAdded() {

        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add.json",
            new ObjectMapper());
        caseDetailsBefore = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-add-before.json",
            new ObjectMapper());

        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(nocSolicitorAddedLettersProcessor).processSolicitorAndLitigantLetters(any(CaseDetails.class), any(CaseDetails.class), anyString(),
            changeOfRepresentationArgumentCaptor.capture());

        RepresentationUpdate representationUpdate = changeOfRepresentationArgumentCaptor.getValue();
        assertThat(representationUpdate.getAdded().getOrganisation().getOrganisationName(), is("FRApplicantSolicitorFirm1"));

        verifyNoMoreInteractions(nocSolicitorAddedLettersProcessor);
    }

    @Test
    public void shouldSendNoticeOfChangeLettersForRevoked() {

        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-revoke.json",
            new ObjectMapper());


        caseDetailsBefore = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-revoke-before.json",
            new ObjectMapper());

        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(nocSolicitorRemovedLettersProcessor).processSolicitorAndLitigantLetters(any(CaseDetails.class), any(CaseDetails.class), anyString(),
            changeOfRepresentationArgumentCaptor.capture());

        RepresentationUpdate representationUpdate = changeOfRepresentationArgumentCaptor.getValue();
        assertThat(representationUpdate.getRemoved().getOrganisation().getOrganisationName(), is("FRApplicantSolicitorFirmR1"));

        verifyNoMoreInteractions(nocSolicitorRemovedLettersProcessor);
    }

    @Test
    public void shouldSendNoticeOfChangeLettersForLatestChangeOfRepresentationOnly() {

        caseDetails = caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-max-cor.json",
            new ObjectMapper());

        caseDetailsBefore =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-max-cor-before.json",
                new ObjectMapper());
        noticeOfChangeLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        verify(nocSolicitorRemovedLettersProcessor).processSolicitorAndLitigantLetters(any(CaseDetails.class), any(CaseDetails.class), anyString(),
            changeOfRepresentationArgumentCaptor.capture());

        RepresentationUpdate representationUpdate = changeOfRepresentationArgumentCaptor.getValue();
        assertThat(representationUpdate.getRemoved().getOrganisation().getOrganisationName(), is("FRApplicantSolicitorFirmMax"));

        verifyNoMoreInteractions(nocSolicitorRemovedLettersProcessor);
    }

}