package uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.generalapplication.GeneralApplicationRejectionLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REJECT_REASON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationRejectionLetterGeneratorTest {

    @Mock
    DocumentHelper documentHelper;

    @Mock
    CaseDataService caseDataService;

    @Mock
    LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    @InjectMocks
    GeneralApplicationRejectionLetterGenerator generalApplicationRejectionLetterGenerator;

    private CaseDetails caseDetails;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Poor");
        caseData.put(APPLICANT_LAST_NAME, "Guy");
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Contested");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Respondent");
        caseData.put(APPLICANT_ADDRESS, Address.builder()
            .addressLine1("50 Applicant Street")
            .postCode("SE12 9SA")
            .build());
        caseData.put(RESPONDENT_ADDRESS, Address.builder()
            .addressLine1("50 Respondent Street")
            .postCode("SE12 9SR")
            .build());
        caseData.put(CONTESTED_SOLICITOR_ADDRESS, Address.builder()
            .addressLine1("50 Applicant Solicitor Street")
            .postCode("SE12 9SAS")
            .build());
        caseData.put(RESP_SOLICITOR_ADDRESS, Address.builder()
            .addressLine1("50 Respondent Solicitor Street")
            .postCode("SE12 9SRS")
            .build());
        caseData.put(DIVORCE_CASE_NUMBER, "EZ17D80124");
        caseData.put(GENERAL_APPLICATION_REJECT_REASON, "Test rejection reason");
        caseData.put(REGION, "london");
        caseData.put(LONDON_FRC_LIST, "cfc");
        caseData.put(CFC_COURTLIST, "FR_s_CFCList_9");
        caseData.put(SOLICITOR_REFERENCE, "testSolReference");
        caseData.put(RESP_SOLICITOR_REFERENCE, "testRespSolReference");
        caseDetails = CaseDetails.builder().id(1234567890L).data(caseData).build();

        when(caseDataService.buildFullApplicantName(caseDetails)).thenReturn("Poor Guy");
        when(caseDataService.buildFullRespondentName(caseDetails)).thenReturn("Contested Respondent");


        generalApplicationRejectionLetterGenerator =
            new GeneralApplicationRejectionLetterGenerator(new ObjectMapper(), caseDataService, documentHelper, letterAddresseeGeneratorMapper);
    }

    @Test
    public void givenApplicantRecipient_whenGenerateGeneralApplicationRejectionLetterDetails_thenGenerateCorrectDetails() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, NO_VALUE);
        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn("50 Applicant Street");
        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            AddresseeDetails.builder().addresseeName("Poor Guy")
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(APPLICANT_ADDRESS), Map.class)).build());

        GeneralApplicationRejectionLetterDetails letterDetails = generalApplicationRejectionLetterGenerator
            .generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT, null);

        assertThat(letterDetails.getLetterDate(), is(String.valueOf(LocalDate.now())));
        assertThat(letterDetails.getAddressee().getName(), is("Poor Guy"));
        assertThat(letterDetails.getAddressee().getFormattedAddress(), containsString("50 Applicant Street"));
        assertThat(letterDetails.getReference(), is(""));
        assertThat(letterDetails.getCourtDetails().get(COURT_DETAILS_NAME_KEY), is("Central Family Court"));
        assertThat(letterDetails.getDivorceCaseNumber(), is("EZ17D80124"));
        assertThat(letterDetails.getApplicantName(), is("Poor Guy"));
        assertThat(letterDetails.getRespondentName(), is("Contested Respondent"));
        assertThat(letterDetails.getCaseNumber(), is("1234567890"));
        assertThat(letterDetails.getGeneralApplicationRejectionReason(), is("Test rejection reason"));
    }

    @Test
    public void givenApplicantSolicitorRecipient_whenGenerateGeneralApplicationRejectionLetterDetails_thenGenerateCorrectDetails() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn("50 Applicant Solicitor Street");
        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            AddresseeDetails.builder().addresseeName(TEST_SOLICITOR_NAME)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(CONTESTED_SOLICITOR_ADDRESS), Map.class)).build());

        GeneralApplicationRejectionLetterDetails letterDetails = generalApplicationRejectionLetterGenerator
            .generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT, null);

        assertThat(letterDetails.getAddressee().getName(), is(TEST_SOLICITOR_NAME));
        assertThat(letterDetails.getAddressee().getFormattedAddress(), containsString("50 Applicant Solicitor Street"));
        assertThat(letterDetails.getReference(), is("testSolReference"));
    }

    @Test
    public void givenRespondentRecipient_whenGenerateGeneralApplicationRejectionLetterDetails_thenGenerateCorrectDetails() {
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, NO_VALUE);

        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn("50 Respondent Street");
        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            AddresseeDetails.builder().addresseeName("Contested Respondent")
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(RESPONDENT_ADDRESS), Map.class)).build());

        GeneralApplicationRejectionLetterDetails letterDetails = generalApplicationRejectionLetterGenerator
            .generate(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT, null);

        assertThat(letterDetails.getAddressee().getName(), is("Contested Respondent"));
        assertThat(letterDetails.getAddressee().getFormattedAddress(), containsString("50 Respondent Street"));
        assertThat(letterDetails.getReference(), is(""));
    }

    @Test
    public void givenRespondentSolicitorRecipient_whenGenerateGeneralApplicationRejectionLetterDetails_thenGenerateCorrectDetails() {
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESP_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);

        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn("50 Respondent Solicitor Street");
        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(
            AddresseeDetails.builder().addresseeName(TEST_RESP_SOLICITOR_NAME)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(RESP_SOLICITOR_ADDRESS), Map.class)).build());

        GeneralApplicationRejectionLetterDetails letterDetails = generalApplicationRejectionLetterGenerator
            .generate(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT, null);

        assertThat(letterDetails.getAddressee().getName(), is(TEST_RESP_SOLICITOR_NAME));
        assertThat(letterDetails.getAddressee().getFormattedAddress(), containsString("50 Respondent Solicitor Street"));
        assertThat(letterDetails.getReference(), is("testRespSolReference"));
    }
}
