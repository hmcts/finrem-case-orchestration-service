package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitorAddedLetterDetailsGeneratorTest extends AbstractLetterDetailsGeneratorTestSetup {

    @InjectMocks
    private SolicitorAddedLetterDetailsGenerator solicitorAddedLetterDetailsGenerator;

    @BeforeEach
    @Override
    void setUpTest() {
        super.setUpTest();
        when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
    }

    @Test
    void shouldGenerateNoticeOfChangeLetterDetailsForApplicantWhenSolicitorAdded() {
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
    void shouldGenerateNoticeOfChangeLetterDetailsForSolicitorWhenAdded() {
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
        assertEquals(
            "Your notice of change has been completed successfully. You can now view your client's case.",
            noticeOfChangeLetterDetails.getNoticeOfChangeText());
    }
}
