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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedLetterDetailsGeneratorTest extends AbstractLetterDetailsGeneratorTest {

    @InjectMocks
    private SolicitorAddedLetterDetailsGenerator solicitorAddedLetterDetailsGenerator;

    @Before
    public void setUpTest() throws IOException {
        super.setUpTest();

        when(mapper.convertValue(any(FrcCourtDetails.class),
            eq(TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class))))
            .thenReturn(getContestedFrcCourtDetailsAsMap());
        when(courtDetailsMapper.getCourtDetails(any())).thenReturn(getContestedFrcCourtDetails());

        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetailsBefore.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorFirm(ORGANISATION_ADDED_NAME);
    }

    @Test
    public void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorAdded() {
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
