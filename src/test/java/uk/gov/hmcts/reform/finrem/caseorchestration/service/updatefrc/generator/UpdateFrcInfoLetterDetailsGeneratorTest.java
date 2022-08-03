package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator.LETTER_DATE_FORMAT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcInfoLetterDetailsGeneratorTest extends BaseServiceTest {
    @Mock
    private CourtDetailsMapper courtDetailsMapper;
    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    private FinremCaseDetails caseDetails;

    protected static final String APPLICANT_FULL_NAME = "Applicant FullName";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "Respondent FullNameContested";
    protected static final String APP_SOL_FORMATTED_ADDRESS = "appSolFormattedAddress";
    protected static final String RESP_SOL_FORMATTED_ADDRESS = "respSolFormattedAddress";
    protected static final String APP_FORMATTED_ADDRESS = "appFormattedAddress";
    protected static final String RESP_FORMATTED_ADDRESS = "respFormattedAddress";

    @Before
    public void setUp() throws IOException {
        when(courtDetailsMapper.getCourtDetails(any())).thenReturn(FrcCourtDetails.builder().build());
        caseDetails = finremCaseDetailsFromResource(
            getResource("/fixtures/contested/generate-frc-info-letter-details.json"), new ObjectMapper());
    }

    @Test
    public void givenApplicant_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        setCaseDataUnrepresented();

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, APPLICANT);
        assertLetterDetails(letterDetails, caseDetails.getCaseData().getContactDetailsWrapper().getSolicitorReference());
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(APP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(APPLICANT_FULL_NAME));
    }

    @Test
    public void givenRespondent_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        setCaseDataUnrepresented();

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, RESPONDENT);
        assertLetterDetails(letterDetails, caseDetails.getCaseData().getContactDetailsWrapper().getRespondentSolicitorReference());
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(RESP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENT_FULL_NAME_CONTESTED));
    }

    @Test
    public void givenAppSolicitor_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        setCaseDataRepresented();

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, APPLICANT);
        assertLetterDetails(letterDetails, caseDetails.getCaseData().getContactDetailsWrapper().getSolicitorReference());
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(APP_SOL_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is("Solicitor"));
    }

    @Test
    public void givenRespSolicitor_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        setCaseDataRepresented();

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, RESPONDENT);
        assertLetterDetails(letterDetails, caseDetails.getCaseData().getContactDetailsWrapper().getRespondentSolicitorReference());
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(RESP_SOL_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is("respSolicitor"));
    }

    private void assertLetterDetails(UpdateFrcInfoLetterDetails updateFrcInfoLetterDetails, String reference) {
        assertThat(updateFrcInfoLetterDetails.getCaseNumber(), is(String.valueOf(caseDetails.getId())));
        assertThat(updateFrcInfoLetterDetails.getReference(), is(reference));
        assertThat(updateFrcInfoLetterDetails.getDivorceCaseNumber(), is(caseDetails.getCaseData().getDivorceCaseNumber()));
        assertThat(updateFrcInfoLetterDetails.getLetterDate(), is(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now())));
        assertThat(updateFrcInfoLetterDetails.getApplicantName(), is(APPLICANT_FULL_NAME));
        assertThat(updateFrcInfoLetterDetails.getRespondentName(), is(RESPONDENT_FULL_NAME_CONTESTED));
    }

    private void setCaseDataUnrepresented() {
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantFmName("Applicant");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantLname("FullName");
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentFmName("Respondent");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentLname("FullNameContested");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantAddress(Address.builder().addressLine1(APP_FORMATTED_ADDRESS).build());
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentAddress(Address.builder()
            .addressLine1(RESP_FORMATTED_ADDRESS)
            .build());
    }

    private void setCaseDataRepresented() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantFmName("Applicant");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantLname("FullName");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentFmName("Respondent");
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentLname("FullNameContested");
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorName("Solicitor");
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorAddress(Address.builder()
            .addressLine1(APP_SOL_FORMATTED_ADDRESS)
            .build());
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        caseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentSolicitorAddress(Address.builder()
            .addressLine1(RESP_SOL_FORMATTED_ADDRESS)
            .build());
    }
}
