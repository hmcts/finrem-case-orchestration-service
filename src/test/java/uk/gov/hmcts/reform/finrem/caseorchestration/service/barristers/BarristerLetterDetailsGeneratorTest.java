package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator.LETTER_DATE_FORMAT;

@RunWith(MockitoJUnitRunner.class)
public class BarristerLetterDetailsGeneratorTest {

    protected static final String APPLICANT_FULL_NAME = "applicantFullName";
    protected static final String RESPONDENT_FULL_NAME_CONTESTED = "respondentFullNameContested";
    protected static final String APP_FORMATTED_ADDRESS = "appFormattedAddress";
    protected static final String RESP_FORMATTED_ADDRESS = "respFormattedAddress";
    public static final String APP_BARR_ORG_ID = "app_Barr_orgID";
    public static final String RESP_BARR_ORG_ID = "RESP_BARR_ORG_ID";
    public static final String APP_BARR_ORG_NAME = "APP_BARR_ORG_NAME";
    public static final String RESP_BARR_ORG_NAME = "RESP_BARR_ORG_NAME";

    @Mock
    private DocumentHelper documentHelper;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private PrdOrganisationService prdOrganisationService;

    @InjectMocks
    private BarristerLetterDetailsGenerator barristerLetterDetailsGenerator;

    private CaseDetails caseDetails;

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
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(false);
        when(caseDataService.buildFullApplicantName(caseDetails)).thenReturn(APPLICANT_FULL_NAME);
        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn(APP_FORMATTED_ADDRESS);
        when(prdOrganisationService.findOrganisationByOrgId(APP_BARR_ORG_ID))
            .thenReturn(organisationsResponse(APP_BARR_ORG_NAME));

        BarristerLetterDetails letterDetails = barristerLetterDetailsGenerator.generate(caseDetails, APPLICANT, barrister(APP_BARR_ORG_ID));
        assertLetterDetails(letterDetails);
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(APP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(APPLICANT_FULL_NAME));
        assertThat(letterDetails.getBarristerFirmName(), is(APP_BARR_ORG_NAME));
        assertThat(letterDetails.getReference(), is(APP_BARR_ORG_ID));
    }

    @Test
    public void givenRespondent_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(false);
        when(caseDataService.buildFullRespondentName(caseDetails)).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        when(documentHelper.formatAddressForLetterPrinting(any())).thenReturn(RESP_FORMATTED_ADDRESS);
        when(prdOrganisationService.findOrganisationByOrgId(RESP_BARR_ORG_ID))
            .thenReturn(organisationsResponse(RESP_BARR_ORG_NAME));

        BarristerLetterDetails letterDetails = barristerLetterDetailsGenerator.generate(caseDetails, RESPONDENT, barrister(RESP_BARR_ORG_ID));
        assertLetterDetails(letterDetails);
        Addressee addressee = letterDetails.getAddressee();
        assertThat(addressee.getFormattedAddress(), is(RESP_FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(RESPONDENT_FULL_NAME_CONTESTED));
        assertThat(letterDetails.getBarristerFirmName(), is(RESP_BARR_ORG_NAME));
        assertThat(letterDetails.getReference(), is(RESP_BARR_ORG_ID));
    }

    private void assertLetterDetails(BarristerLetterDetails barristerLetterDetails) {
        assertThat(barristerLetterDetails.getCaseNumber(), is(caseDetails.getId().toString()));
        assertThat(barristerLetterDetails.getDivorceCaseNumber(), is(caseDetails.getData().get(DIVORCE_CASE_NUMBER).toString()));
        assertThat(barristerLetterDetails.getLetterDate(), is(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now())));
        assertThat(barristerLetterDetails.getApplicantName(), is(APPLICANT_FULL_NAME));
        assertThat(barristerLetterDetails.getRespondentName(), is(RESPONDENT_FULL_NAME_CONTESTED));
    }

    private Barrister barrister(String orgId) {
        return Barrister.builder()
            .organisation(Organisation.builder()
                .organisationID(orgId)
                .build())
            .build();
    }

    private OrganisationsResponse organisationsResponse(String orgName) {
        return OrganisationsResponse.builder()
            .name(orgName)
            .build();
    }
}