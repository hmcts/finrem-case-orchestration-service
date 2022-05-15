package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;

public class BaseUpdateFrcInfoDocumentServiceTest {

    @Mock
    protected GenericDocumentService genericDocumentService;

    @Mock
    protected CaseDataService caseDataService;

    @Mock
    protected UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    @Mock
    protected DocumentConfiguration documentConfiguration;

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String SOL_DOC_TEMPLATE = "solDocTemplate";
    protected static final String SOL_DOC_FILENAME = "solDocFilename";
    protected static final String LIT_DOC_TEMPLATE = "litDocTemplate";
    protected static final String LIT_DOC_FILENAME = "litDocFilename";
    protected static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    private static final String APPLICANT_NAME = "applicantName";
    private static final String RESPONDENT_NAME = "respondentName";
    private static final String FORMATTED_ADDRESS = "formattedAddress";
    private static final String ADDRESSEE_NAME = "addresseeName";
    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private Map caseData = null;
    protected CaseDetails caseDetails = null;

    protected UpdateFrcInfoLetterDetails updateFrcInfoLetterDetails;

    @Captor
    ArgumentCaptor<Map> updateFrcInfoLetterDetailsCaptor;

    private String letterDate;

    @Before
    public void setUp() {
        caseData = Map.of(DIVORCE_CASE_NUMBER, "divCaseReference", SOLICITOR_REFERENCE,
            "solicitorReference");
        caseDetails = CaseDetails.builder().id(1234L).data(caseData).build();

        letterDate = DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now());
        updateFrcInfoLetterDetails = UpdateFrcInfoLetterDetails.builder()
            .letterDate(letterDate)
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)))
            .applicantName(APPLICANT_NAME)
            .respondentName(RESPONDENT_NAME)
            .courtDetails(buildConsentedFrcCourtDetails())
            .addressee(Addressee.builder().formattedAddress(FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build())
            .build();
    }

    protected void assertAndVerifyDocumentIsGenerated(CaseDocument caseDocument) {
        assertNotNull(caseDocument);
        verify(genericDocumentService, times(1))
            .generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
                updateFrcInfoLetterDetailsCaptor.capture(),
                any(),
                any());
    }

    protected void assertPlaceHoldersMap(Map placeholdersMap) {
        Map caseDetailsMap = (Map) placeholdersMap.get(CASE_DETAILS);
        Map caseDataMap = (Map) caseDetailsMap.get(CASE_DATA);
        assertThat(caseDataMap.get("caseNumber"), is("1234"));
        assertThat(caseDataMap.get("reference"), is("solicitorReference"));
        assertThat(caseDataMap.get("divorceCaseNumber"), is("divCaseReference"));
        assertThat(caseDataMap.get("letterDate"), is(letterDate));
        assertThat(caseDataMap.get("applicantName"), is("applicantName"));
        assertThat(caseDataMap.get("respondentName"), is("respondentName"));

        Map courtDetailsMap = (Map) caseDataMap.get("courtDetails");
        assertThat(courtDetailsMap.get("courtName"), is("Family Court at the Courts and Tribunal Service Centre"));
        assertThat(courtDetailsMap.get("courtAddress"), is("PO Box 12746, Harlow, CM20 9QZ"));
        assertThat(courtDetailsMap.get("phoneNumber"), is("0300 303 0642"));
        assertThat(courtDetailsMap.get("email"), is("contactFinancialRemedy@justice.gov.uk"));

        Map addresseeMap = (Map) caseDataMap.get("addressee");
        assertThat(addresseeMap.get("name"), is("addresseeName"));
        assertThat(addresseeMap.get("formattedAddress"), is("formattedAddress"));
    }
}
