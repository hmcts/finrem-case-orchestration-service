package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.COURT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;

public class GenerateCoverSheetServiceFinremTest extends BaseServiceTest {


    @Autowired
    private GenerateCoverSheetService generateCoverSheetService;
    @Autowired
    private ObjectMapper mapper;

    @Rule
    public ExpectedException expectedException = none();

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> generateDocumentCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void shouldGenerateApplicantCoverSheet() throws Exception {
        CaseDocument caseDocument = generateCoverSheetService.generateApplicantCoverSheet(
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print.json").getCaseDetails(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));

        assertCoversheetCalledWithRequiredData();
    }


    @Test
    public void shouldGenerateRespondentCoverSheet() throws Exception {
        CaseDocument caseDocument = generateCoverSheetService.generateRespondentCoverSheet(
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print.json").getCaseDetails(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));

        assertCoversheetCalledWithRequiredData();
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantAddressWhenApplicantSolicitorAddressIsEmpty() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")
                .getCaseDetails();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("50 Applicant Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantAddressWhenApplicantSolicitorAddressIsEmptyFinrem() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")
                .getCaseDetails();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("50 Applicant Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentAddressWhenRespondentSolicitorAddressIsEmpty() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")
                .getCaseDetails();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("51 Respondent Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespCoverSheetWithRespAddressWhenRespPostcodeIsEmptyAndRespSolAddressIsEmpty() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-empty-solicitor-address-and-empty-postcode.json")
                .getCaseDetails();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("51 Respondent Street\nLondon");
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantSolicitorAddress() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-with-solicitors.json").getCaseDetails();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("123 Applicant Solicitor Street\nSecond Address Line\nThird Address Line\nGreater London\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentSolicitorAddress() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-with-solicitors.json").getCaseDetails();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("321 Respondent Solicitor Street\nLondon\nSE1");
    }

    @Test
    public void whenPartyIsRepresented_thenSolicitorNameIsUsedOnCoverSheet() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-with-solicitors.json").getCaseDetails();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "Mr J Solicitor");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Ms J Solicitor");
    }

    @Test
    public void whenPartyIsNotRepresented_thenPartyNameIsUsedOnCoverSheet() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")
                .getCaseDetails();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "John Doe");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Jane Doe");
    }


    @Test
    public void shouldGenerateIntervenerCoverSheet() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithIntervener1Unrepresented();

        CaseDocument caseDocument =
            generateCoverSheetService.generateIntervenerCoverSheet(caseDetails, AUTH_TOKEN,
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE);

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));

        assertCoversheetAddressFromMap("Intervener 1 Address Line 1\nIntervener 1 Address Line 2"
            + "\nIntervener 1 Address Line 3\nIntervener 1 County\nIntervener 1 Post Town\nIntervener 1 Post Code");
    }

    private FinremCaseDetails caseDetailsConsented() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print.json")) {
            return getFinremCaseDetails(resourceAsStream);
        }
    }

    private FinremCaseDetails caseDetailsWithEmptySolAddress() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")) {
            return getFinremCaseDetails(resourceAsStream);
        }
    }

    private FinremCaseDetails caseDetailsWithEmptySolAddressAndEmptyPostcode() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-empty-solicitor-address-and-empty-postcode.json")) {
            return getFinremCaseDetails(resourceAsStream);
        }
    }

    private FinremCaseDetails caseDetailsWithSolicitors() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-with-solicitors.json")) {
            return getFinremCaseDetails(resourceAsStream);
        }
    }


    private FinremCaseDetails caseDetailsWithIntervener1Unrepresented() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-intervener1-notrepresented.json")) {
            return getFinremCaseDetails(resourceAsStream);
        }
    }

    private FinremCaseDetails getFinremCaseDetails(InputStream resourceAsStream) throws IOException {
        FinremCallbackRequest finremCallbackRequest = mapper.readValue(resourceAsStream, FinremCallbackRequest.class);
        finremCallbackRequest.getCaseDetails().getData().setCcdCaseType(finremCallbackRequest.getCaseDetails().getCaseType());
        return finremCallbackRequest.getCaseDetails();
    }

    private void assertCoversheetAddress(String formattedAddress) {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(), any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        MatcherAssert.assertThat(addressee.getFormattedAddress(), is(formattedAddress));
    }


    private void assertAddresseeName(int invocation, String name) {
        verify(genericDocumentService, times(invocation)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(), any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        MatcherAssert.assertThat(addressee.getName(), is(name));
    }

    private void assertCoversheetCalledWithRequiredData() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(),
            any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);

        String expectedCourtContactDetails =
            "HMCTS Financial Remedy\n"
                + "PO BOX 12746\n"
                + "HARLOW\n"
                + "CM20 9QZ";

        MatcherAssert.assertThat(data, hasKey(ADDRESSEE));
        MatcherAssert.assertThat(data, hasKey(COURT_CONTACT_DETAILS));
        assertEquals(expectedCourtContactDetails, data.get(COURT_CONTACT_DETAILS));
        MatcherAssert.assertThat(data, hasKey(CASE_NUMBER));
    }

    private void assertCoversheetAddressFromMap(String formattedAddress) {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        assertThat(addressee.getFormattedAddress(), is(formattedAddress));
    }
}