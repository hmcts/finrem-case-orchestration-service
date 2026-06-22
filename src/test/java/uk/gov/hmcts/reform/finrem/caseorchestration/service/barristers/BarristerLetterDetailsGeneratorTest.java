package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator.LETTER_DATE_FORMAT;

@ExtendWith(MockitoExtension.class)
class BarristerLetterDetailsGeneratorTest {

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
    private LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    @Mock
    private PrdOrganisationService prdOrganisationService;

    @Mock
    private InternationalPostalService internationalPostalService;

    @InjectMocks
    private BarristerLetterDetailsGenerator barristerLetterDetailsGenerator;

    private CaseDetails caseDetails;

    private final ObjectMapper objectMapper = createObjectMapper();

    @BeforeEach
    void setUp() {
        when(documentHelper.getApplicantFullName(any())).thenReturn(APPLICANT_FULL_NAME);
        when(documentHelper.getRespondentFullNameContested(any())).thenReturn(RESPONDENT_FULL_NAME_CONTESTED);
        caseDetails = caseDetailsFromResource(
            "/fixtures/contested/generate-frc-info-letter-details.json",
            objectMapper);
    }

    @Test
    void givenApplicant_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(APP_FORMATTED_ADDRESS);
        when(prdOrganisationService.findOrganisationByOrgId(APP_BARR_ORG_ID))
            .thenReturn(organisationsResponse(APP_BARR_ORG_NAME));

        when(letterAddresseeGeneratorMapper.generate(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            AddresseeDetails.builder().addresseeName(APPLICANT_FULL_NAME)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(APPLICANT_ADDRESS), Map.class)).build());

        BarristerLetterDetails letterDetails = barristerLetterDetailsGenerator.generate(caseDetails, APPLICANT, barrister(APP_BARR_ORG_ID));
        assertLetterDetails(letterDetails);
        Addressee addressee = letterDetails.getAddressee();
        assertEquals(APP_FORMATTED_ADDRESS, addressee.getFormattedAddress());
        assertEquals(APPLICANT_FULL_NAME, addressee.getName());
        assertEquals(APP_BARR_ORG_NAME, letterDetails.getBarristerFirmName());
        assertEquals(APP_BARR_ORG_ID, letterDetails.getReference());
        assertEquals(CaseType.CONTESTED, letterDetails.getCaseType());
    }

    @Test
    void givenRespondent_whenGenerateLetterDetails_thenReturnCorrectDetails() {
        when(documentHelper.formatAddressForLetterPrinting(any(), anyBoolean())).thenReturn(RESP_FORMATTED_ADDRESS);
        when(prdOrganisationService.findOrganisationByOrgId(RESP_BARR_ORG_ID))
            .thenReturn(organisationsResponse(RESP_BARR_ORG_NAME));

        when(letterAddresseeGeneratorMapper.generate(caseDetails, RESPONDENT)).thenReturn(
            AddresseeDetails.builder().addresseeName(RESPONDENT_FULL_NAME_CONTESTED)
                .addressToSendTo(objectMapper.convertValue(caseDetails.getData().get(RESPONDENT_ADDRESS), Map.class)).build());

        BarristerLetterDetails letterDetails = barristerLetterDetailsGenerator.generate(caseDetails, RESPONDENT, barrister(RESP_BARR_ORG_ID));
        assertLetterDetails(letterDetails);
        Addressee addressee = letterDetails.getAddressee();
        assertEquals(RESP_FORMATTED_ADDRESS, addressee.getFormattedAddress());
        assertEquals(RESPONDENT_FULL_NAME_CONTESTED, addressee.getName());
        assertEquals(RESP_BARR_ORG_NAME, letterDetails.getBarristerFirmName());
        assertEquals(RESP_BARR_ORG_ID, letterDetails.getReference());
        assertEquals(CaseType.CONTESTED, letterDetails.getCaseType());
    }

    private void assertLetterDetails(BarristerLetterDetails barristerLetterDetails) {
        assertEquals(caseDetails.getId().toString(), barristerLetterDetails.getCaseNumber());
        assertEquals(caseDetails.getData().get(DIVORCE_CASE_NUMBER).toString(), barristerLetterDetails.getDivorceCaseNumber());
        assertEquals(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()), barristerLetterDetails.getLetterDate());
        assertEquals(APPLICANT_FULL_NAME, barristerLetterDetails.getApplicantName());
        assertEquals(RESPONDENT_FULL_NAME_CONTESTED, barristerLetterDetails.getRespondentName());
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
