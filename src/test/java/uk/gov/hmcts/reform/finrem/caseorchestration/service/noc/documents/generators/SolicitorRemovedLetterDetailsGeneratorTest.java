package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorRemovedLetterDetailsGeneratorTest extends AbstractLetterDetailsGeneratorTest {

    @InjectMocks
    private SolicitorRemovedLetterDetailsGenerator solicitorRemovedLetterDetailsGenerator;

    @Before
    public void setUpTest() throws IOException {
        super.setUpTest();
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorRemoved() {
        setCaseDataForApplicantLetter();
        setUpLitigantMockContext();

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorRemovedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, representationUpdate,
                DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.TRUE);
        assertConsentedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenRemoved() {
        setCaseDataForSolicitorLetter();
        setUpSolicitorMockContext();

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            solicitorRemovedLetterDetailsGenerator.generate(caseDetails, caseDetailsBefore, buildChangeOfRepresentation(),
                DocumentHelper.PaperNotificationRecipient.SOLICITOR);

        assertLetterDetails(noticeOfChangeLetterDetails, NoticeType.REMOVE, Boolean.FALSE);
        assertContestedCourtDetails(noticeOfChangeLetterDetails);
        assertAddresseeDetails(noticeOfChangeLetterDetails);
        assertThat(noticeOfChangeLetterDetails.getNoticeOfChangeText(),
            is("You've completed notice of acting on this, your access to this case has now been revoked."));
    }

    private void setCaseDataForApplicantLetter() {
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONSENTED);
        caseDetailsBefore.getCaseData().setCcdCaseType(CaseType.CONSENTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setAppRespondentFmName("respondent");
        caseDetails.getCaseData().getContactDetailsWrapper().setAppRespondentLName("FullNameConsented");
        caseDetailsBefore.getCaseData().getContactDetailsWrapper().setSolicitorFirm(ORGANISATION_REMOVED_NAME);
    }

    private void setUpLitigantMockContext() {
        when(addresseeGeneratorService.generateAddressee(caseDetails, changedRepresentativeRemoved,
            DocumentHelper.PaperNotificationRecipient.APPLICANT, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
    }

    private void setCaseDataForSolicitorLetter() {
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetailsBefore.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetailsBefore.getCaseData().getContactDetailsWrapper().setApplicantSolicitorFirm(ORGANISATION_REMOVED_NAME);
    }

    private void setUpSolicitorMockContext() {
        when(addresseeGeneratorService.generateAddressee(caseDetailsBefore, changedRepresentativeRemoved,
            DocumentHelper.PaperNotificationRecipient.SOLICITOR, "applicant"))
            .thenReturn(Addressee.builder().formattedAddress(
                FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build());
        when(mapper.convertValue(any(FrcCourtDetails.class),
            eq(TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class))))
            .thenReturn(getContestedFrcCourtDetailsAsMap());
        when(courtDetailsMapper.getCourtDetails(any())).thenReturn(getContestedFrcCourtDetails());
    }
}
