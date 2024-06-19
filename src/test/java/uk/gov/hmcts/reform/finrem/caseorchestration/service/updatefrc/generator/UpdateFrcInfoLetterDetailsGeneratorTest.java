package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator.LETTER_DATE_FORMAT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcInfoLetterDetailsGeneratorTest {
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private InternationalPostalService postalService;

    @Mock
    LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    @InjectMocks
    UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    private CaseDetails caseDetails;

    private ObjectMapper objectMapper = new ObjectMapper();

    protected static final String APPLICANT_FULL_NAME = "applicantFullName";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "respondentFullNameContested";
    protected static final String APP_SOL_FORMATTED_ADDRESS = "appSolFormattedAddress";
    protected static final String RESP_SOL_FORMATTED_ADDRESS = "respSolFormattedAddress";
    protected static final String APP_FORMATTED_ADDRESS = "appFormattedAddress";
    protected static final String RESP_FORMATTED_ADDRESS = "respFormattedAddress";

    @Before
    public void setUp() {
        when(documentHelper.getApplicantFullName(any())).thenReturn(APPLICANT_FULL_NAME);
        when(documentHelper.getRespondentFullNameContested(any())).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        caseDetails = caseDetailsFromResource(
            "/fixtures/contested/generate-frc-info-letter-details.json",
            new ObjectMapper());
    }

    @Test
    public void givenApplicant_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(APP_FORMATTED_ADDRESS);

        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            AddresseeDetails.builder().addresseeName(APPLICANT_FULL_NAME)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(APPLICANT_ADDRESS), Map.class)).build());

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, APPLICANT, null);
        assertLetterDetails(letterDetails, (String) caseDetails.getData().get(SOLICITOR_REFERENCE));
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(APP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(APPLICANT_FULL_NAME));
        verify(postalService).isRecipientResideOutsideOfUK(caseDetails.getData(), "APPLICANT");
    }

    @Test
    public void givenRespondent_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(RESP_FORMATTED_ADDRESS);

        when(letterAddresseeGeneratorMapper.generate(caseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder().addresseeName(RESPONDENT_FULL_NAME_CONTESTED)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(RESPONDENT_ADDRESS), Map.class)).build());


        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, RESPONDENT, null);
        assertLetterDetails(letterDetails, (String) caseDetails.getData().get(RESP_SOLICITOR_REFERENCE));
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(RESP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENT_FULL_NAME_CONTESTED));
        verify(postalService).isRecipientResideOutsideOfUK(caseDetails.getData(), "RESPONDENT");
    }

    @Test
    public void givenAppSolicitor_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(APP_SOL_FORMATTED_ADDRESS);

        when(letterAddresseeGeneratorMapper.generate(caseDetails, APPLICANT)).thenReturn(
            AddresseeDetails.builder().addresseeName("Solicitor")
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(CONTESTED_SOLICITOR_ADDRESS), Map.class)).build());

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, APPLICANT, null);
        assertLetterDetails(letterDetails, (String) caseDetails.getData().get(SOLICITOR_REFERENCE));
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(APP_SOL_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is("Solicitor"));
        verify(postalService).isRecipientResideOutsideOfUK(caseDetails.getData(), "APPLICANT");
    }

    @Test
    public void givenRespSolicitor_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(RESP_SOL_FORMATTED_ADDRESS);
        when(letterAddresseeGeneratorMapper.generate(caseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder().addresseeName("respSolicitor")
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(RESP_SOLICITOR_ADDRESS), Map.class)).build());

        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, RESPONDENT, null);
        assertLetterDetails(letterDetails, (String) caseDetails.getData().get(RESP_SOLICITOR_REFERENCE));
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(RESP_SOL_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is("respSolicitor"));
        verify(postalService).isRecipientResideOutsideOfUK(caseDetails.getData(), "RESPONDENT");
    }

    private void assertLetterDetails(UpdateFrcInfoLetterDetails updateFrcInfoLetterDetails, String reference) {
        assertThat(updateFrcInfoLetterDetails.getCaseNumber(), is(caseDetails.getId().toString()));
        assertThat(updateFrcInfoLetterDetails.getReference(), is(reference));
        assertThat(updateFrcInfoLetterDetails.getDivorceCaseNumber(), is(caseDetails.getData().get(DIVORCE_CASE_NUMBER).toString()));
        assertThat(updateFrcInfoLetterDetails.getLetterDate(), is(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now())));
        assertThat(updateFrcInfoLetterDetails.getApplicantName(), is(APPLICANT_FULL_NAME));
        assertThat(updateFrcInfoLetterDetails.getRespondentName(), is(RESPONDENT_FULL_NAME_CONTESTED));
    }
}
