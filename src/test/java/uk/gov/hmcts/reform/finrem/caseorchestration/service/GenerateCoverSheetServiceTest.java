package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.InputStream;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;

@ActiveProfiles("test-mock-document-client")
public class GenerateCoverSheetServiceTest extends BaseServiceTest {

    @Autowired
    private GenerateCoverSheetService generateCoverSheetService;
    @Autowired
    private ObjectMapper mapper;

    @Rule
    public ExpectedException expectedException = none();

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> generateDocumentCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void shouldGenerateApplicantCoverSheet() throws Exception {
        CaseDocument caseDocument = generateCoverSheetService.generateApplicantCoverSheet(caseDetailsConsented(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));

        assertCoversheetCalledWithRequiredData();
    }

    @Test
    public void shouldGenerateRespondentCoverSheet() throws Exception {
        CaseDocument caseDocument = generateCoverSheetService.generateRespondentCoverSheet(caseDetailsConsented(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));

        assertCoversheetCalledWithRequiredData();
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantAddressWhenApplicantSolicitorAddressIsEmpty() throws Exception {
        CaseDetails caseDetails = caseDetailsWithEmptySolAddress();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("50 Applicant Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentAddressWhenRespondentSolicitorAddressIsEmpty() throws Exception {
        CaseDetails caseDetails = caseDetailsWithEmptySolAddress();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("51 Respondent Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantSolicitorAddress() throws Exception {
        CaseDetails caseDetails = caseDetailsWithSolicitors();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("123 Applicant Solicitor Street\nSecond Address Line\nGreater London\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentSolicitorAddress() throws Exception {
        CaseDetails caseDetails = caseDetailsWithSolicitors();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("321 Respondent Solicitor Street\nLondon\nSE1");
    }

    @Test
    public void whenPartyIsRepresented_thenSolicitorNameIsUsedOnCoverSheet() throws Exception {
        CaseDetails caseDetails = caseDetailsWithSolicitors();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "Mr J Solicitor");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Ms J Solicitor");
    }

    @Test
    public void whenPartyIsNotRepresented_thenPartyNameIsUsedOnCoverSheet() throws Exception {
        CaseDetails caseDetails = caseDetailsWithEmptySolAddress();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "John Doe");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Jane Doe");
    }

    private CaseDetails caseDetailsConsented() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsWithEmptySolAddress() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsWithSolicitors() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulkprint/bulk-print-with-solicitors.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private void assertCoversheetAddress(String formattedAddress) {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Addressee addressee = (Addressee) generateDocumentCaseDetailsCaptor.getValue().getData().get(ADDRESSEE);
        assertThat(addressee.getFormattedAddress(), is(formattedAddress));
    }

    private void assertAddresseeName(int invocation, String name) {
        verify(genericDocumentService, times(invocation)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Addressee addressee = (Addressee) generateDocumentCaseDetailsCaptor.getValue().getData().get(ADDRESSEE);
        assertThat(addressee.getName(), is(name));
    }

    private void assertCoversheetCalledWithRequiredData() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> data = generateDocumentCaseDetailsCaptor.getValue().getData();

        String expectedCourtContactDetails = "HMCTS Financial Remedy" + "\n"
            + "PO BOX 12746" + "\n"
            + "HARLOW" + "\n"
            + "CM20 9QZ";

        assertThat(data, hasKey(ADDRESSEE));
        assertThat(data, hasKey(COURT_CONTACT_DETAILS));
        assertEquals(expectedCourtContactDetails, data.get(COURT_CONTACT_DETAILS));
        assertThat(data, hasKey(CASE_NUMBER));
    }

    private void assertContestedCoversheetCalledWithRequiredData() {
        verify(genericDocumentService, times(1)).generateDocument(any(), generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> data = generateDocumentCaseDetailsCaptor.getValue().getData();

        String expectedCourtContactDetails = "Hastings County Court And Family Court Hearing Centre" + "\n"
            + "The Law Courts, Bohemia Road, Hastings, TN34 1QX" + "\n"
            + "01634 887900" + "\n"
            + "fr_applicant_sol@sharklasers.com";

        assertThat(data, hasKey(ADDRESSEE));
        assertThat(data, hasKey(COURT_CONTACT_DETAILS));
        assertEquals(expectedCourtContactDetails, data.get(COURT_CONTACT_DETAILS));
        assertThat(data, hasKey(CASE_NUMBER));
    }
}
