package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService.CASE_DETAILS;

public class NocDocumentServiceBaseTest {

    @Mock
    protected DocumentConfiguration documentConfiguration;
    @Mock
    protected GenericDocumentService genericDocumentService;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String DOC_TEMPLATE = "docTemplate";
    protected static final String DOC_FILENAME = "docFilename";
    protected static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String APPLICANT_NAME = "applicantName";
    protected static final String RESPONDENT_NAME = "respondentName";
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String ADDRESSEE_NAME = "addresseeName";

    protected Map caseData = null;
    protected CaseDetails caseDetails = null;

    protected NoticeOfChangeLetterDetails noticeOfChangeLetterDetails;

    @Captor
    ArgumentCaptor<Map> notiicationLettersDetailsMapCaptor;

    @Before
    public void setUpTest() {

        caseData = Map.of(DIVORCE_CASE_NUMBER, "divCaseReference", SOLICITOR_REFERENCE,
            "solicitorReference");
        caseDetails = CaseDetails.builder().id(1234L).data(caseData).build();
        LocalDate date = LocalDate.of(2022, Month.MAY, 6);

        noticeOfChangeLetterDetails = NoticeOfChangeLetterDetails.builder()
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(date))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .caseNumber(caseDetails.getId().toString())
            .reference(Objects.toString(caseDetails.getData().get(SOLICITOR_REFERENCE)))
            .applicantName(APPLICANT_NAME)
            .respondentName(RESPONDENT_NAME)
            .courtDetails(buildConsentedFrcCourtDetails())
            .addressee(Addressee.builder().formattedAddress(FORMATTED_ADDRESS).name(ADDRESSEE_NAME).build())
            .build();


    }

    protected void assertAndVerifyDocumentsAreGenerated(CaseDocument caseDocument) {
        assertNotNull(caseDocument);
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), notiicationLettersDetailsMapCaptor.capture(),
            eq(DOC_TEMPLATE), eq(DOC_FILENAME));
    }

    protected void assertPlaceHoldersMap(Map placeholdersMap) {
        Map caseDetailsMap = (Map) placeholdersMap.get(CASE_DETAILS);
        Map caseDataMap = (Map) caseDetailsMap.get(CASE_DATA);
        assertThat(caseDataMap.get("caseNumber"), is("1234"));
        assertThat(caseDataMap.get("reference"), is("solicitorReference"));
        assertThat(caseDataMap.get("divorceCaseNumber"), is("divCaseReference"));
        assertThat(caseDataMap.get("letterDate"), is("2022-05-06"));
        assertThat(caseDataMap.get("applicantName"), is("applicantName"));
        assertThat(caseDataMap.get("respondentName"), is("respondentName"));
        assertThat(caseDataMap.get("letterDate"), is("2022-05-06"));

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


