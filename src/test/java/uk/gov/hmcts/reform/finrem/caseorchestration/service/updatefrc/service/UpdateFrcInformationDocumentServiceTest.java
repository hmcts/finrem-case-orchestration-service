package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildConsentedFrcCourtDetails;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcInformationDocumentServiceTest {

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @InjectMocks
    UpdateFrcInformationDocumentService updateFrcInformationDocumentService;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String SOL_DOC_TEMPLATE = "solDocTemplate";
    protected static final String SOL_DOC_FILENAME = "solDocFilename";
    protected static final String LIT_DOC_TEMPLATE = "litDocTemplate";
    protected static final String LIT_DOC_FILENAME = "litDocFilename";
    protected static final String LETTER_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String APPLICANT_NAME = "applicantName";
    protected static final String RESPONDENT_NAME = "respondentName";
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String ADDRESSEE_NAME = "addresseeName";
    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private static final String APP_LITIGANT_URL = "appLitigantUrl";
    private static final String APP_SOLICITOR_URL = "appSolicitorUrl";
    private static final String RESP_LITIGANT_URL = "respLitigantUrl";
    private static final String RESP_SOLICITOR_URL = "respSolicitorUrl";
    private static final String LITIGANT_URL = "litigantUrl";
    private static final String SOLICITOR_URL = "solicitorUrl";

    protected Map caseData = null;
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

    protected void assertAndVerifyDocumentsAreGenerated(List<CaseDocument> caseDocuments) {
        caseDocuments.forEach(Assertions::assertNotNull);
        verify(genericDocumentService, times(2))
            .generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            any(),
            any());
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

    private void setUpMockContextAppSolicitorRespLitigant() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new CaseDocument(RESP_LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpMockContextAppLitigantRespLitigant() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new CaseDocument(LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpMockContextAppSolicitorRespSolicitor() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new CaseDocument(SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpMockContextAppLitigantRespSolicitor() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new CaseDocument(RESP_SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpMockContextAppLitigant() {
        when(documentConfiguration.getUpdateFRCInformationLitigantTemplate()).thenReturn(LIT_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationLitigantFilename()).thenReturn(LIT_DOC_FILENAME);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(LIT_DOC_TEMPLATE),
            eq(LIT_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_LITIGANT_URL, LIT_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpMockContextAppSol() {
        when(documentConfiguration.getUpdateFRCInformationSolicitorTemplate()).thenReturn(SOL_DOC_TEMPLATE);
        when(documentConfiguration.getUpdateFRCInformationSolicitorFilename()).thenReturn(SOL_DOC_FILENAME);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(false);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            updateFrcInfoLetterDetailsCaptor.capture(),
            eq(SOL_DOC_TEMPLATE),
            eq(SOL_DOC_FILENAME)))
            .thenReturn(new CaseDocument(APP_SOLICITOR_URL, SOL_DOC_FILENAME, null));
        when(updateFrcInfoLetterDetailsGenerator.generate(any(), any())).thenReturn(updateFrcInfoLetterDetails);
    }

    private void setUpNoLetterMockContext() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(any())).thenReturn(true);
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForAppSolRespLitigant_thenGenerateLetters() {
        setUpMockContextAppSolicitorRespLitigant();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        assertAndVerifyDocumentsAreGenerated(letters);
        letters.forEach(letter -> {
            if (letter.getDocumentFilename().equalsIgnoreCase(LIT_DOC_FILENAME)) {
                assertEquals(letter.getDocumentUrl(), RESP_LITIGANT_URL);
            } else if (letter.getDocumentFilename().equalsIgnoreCase(SOL_DOC_FILENAME)) {
                assertEquals(letter.getDocumentUrl(), APP_SOLICITOR_URL);
            }
        });

        Predicate<String> solOrLitigantFilename = s -> s.equals(LIT_DOC_FILENAME) || s.equals(SOL_DOC_FILENAME);
        assertTrue(letters.stream().map(CaseDocument::getDocumentFilename).allMatch(solOrLitigantFilename));
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForLitigants_thenGenerateLetters() {
        setUpMockContextAppLitigantRespLitigant();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        assertAndVerifyDocumentsAreGenerated(letters);

        Predicate<String> litigantFilename = s -> s.equals(LIT_DOC_FILENAME);
        assertTrue(letters.stream().map(CaseDocument::getDocumentFilename).allMatch(litigantFilename));
        letters.forEach(letter -> assertEquals(letter.getDocumentUrl(), LITIGANT_URL));
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForSolicitors_thenGenerateLetters() {
        setUpMockContextAppSolicitorRespSolicitor();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        assertAndVerifyDocumentsAreGenerated(letters);

        Predicate<String> solFilename = s -> s.equals(SOL_DOC_FILENAME);
        assertTrue(letters.stream().map(CaseDocument::getDocumentFilename).allMatch(solFilename));
        letters.forEach(letter -> assertEquals(letter.getDocumentUrl(), SOLICITOR_URL));
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForAppLitigantRespSol_thenGenerateLetters() {
        setUpMockContextAppLitigantRespSolicitor();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        assertAndVerifyDocumentsAreGenerated(letters);
        letters.forEach(letter -> {
            if (letter.getDocumentFilename().equalsIgnoreCase(LIT_DOC_FILENAME)) {
                assertEquals(letter.getDocumentUrl(), APP_LITIGANT_URL);
            } else if (letter.getDocumentFilename().equalsIgnoreCase(SOL_DOC_FILENAME)) {
                assertEquals(letter.getDocumentUrl(), RESP_SOLICITOR_URL);
            }
        });

        Predicate<String> solOrLitigantFilename = s -> s.equals(LIT_DOC_FILENAME) || s.equals(SOL_DOC_FILENAME);
        assertTrue(letters.stream().map(CaseDocument::getDocumentFilename).allMatch(solOrLitigantFilename));
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForAppOnly_thenGenerateLetter() {
        setUpMockContextAppLitigant();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        assertEquals(letters.size(), 1);
        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        CaseDocument appLetter = letters.get(0);
        assertAndVerifyDocumentIsGenerated(appLetter);
        assertEquals(appLetter.getDocumentUrl(), APP_LITIGANT_URL);
        assertEquals(appLetter.getDocumentFilename(), LIT_DOC_FILENAME);
    }

    @Test
    public void givenUpdateFrcInfo_whenGetUpdateFrcInfoLettersForAppSolOnly_thenGenerateLetter() {
        setUpMockContextAppSol();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        assertEquals(letters.size(), 1);
        letters.forEach(letter -> assertPlaceHoldersMap(updateFrcInfoLetterDetailsCaptor.getValue()));
        CaseDocument appLetter = letters.get(0);
        assertAndVerifyDocumentIsGenerated(appLetter);
        assertEquals(appLetter.getDocumentUrl(), APP_SOLICITOR_URL);
        assertEquals(appLetter.getDocumentFilename(), SOL_DOC_FILENAME);
    }

    @Test
    public void givenUpdateFrcInfo_whenBothSolicitorsAreDigital_thenGenerateNoLetters() {
        setUpNoLetterMockContext();
        List<CaseDocument> letters = updateFrcInformationDocumentService.getUpdateFrcInfoLetters(caseDetails, AUTH_TOKEN);

        assertEquals(letters.size(), 0);
        verify(genericDocumentService, never())
            .generateDocumentFromPlaceholdersMap(any(), any(), any(), any());
    }

}

