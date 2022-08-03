package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.COURT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;

public class GenerateCoverSheetServiceTest extends BaseServiceTest {

    @Autowired private GenerateCoverSheetService generateCoverSheetService;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> generateDocumentCaseDetailsCaptor;

    @Before
    public void setup() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void shouldGenerateApplicantCoverSheet() throws Exception {
        Document caseDocument = generateCoverSheetService.generateApplicantCoverSheet(caseDetailsConsented(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getFilename()));
        assertThat(document().getUrl(), is(caseDocument.getUrl()));

        assertCoversheetCalledWithRequiredData();
    }

    @Test
    public void shouldGenerateRespondentCoverSheet() throws Exception {
        Document caseDocument = generateCoverSheetService.generateRespondentCoverSheet(caseDetailsConsented(), AUTH_TOKEN);

        assertThat(document().getBinaryUrl(), is(caseDocument.getBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getFilename()));
        assertThat(document().getUrl(), is(caseDocument.getUrl()));

        assertCoversheetCalledWithRequiredData();
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantAddressWhenApplicantSolicitorAddressIsEmpty() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithEmptySolAddress();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("50 Applicant Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentAddressWhenRespondentSolicitorAddressIsEmpty() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithEmptySolAddress();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("51 Respondent Street\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateApplicantCoverSheetUsingApplicantSolicitorAddress() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithSolicitors();
        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("123 Applicant Solicitor Street\nSecond Address Line\nGreater London\nLondon\nSE1");
    }

    @Test
    public void shouldGenerateRespondentCoverSheetUsingRespondentSolicitorAddress() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithSolicitors();
        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);

        assertCoversheetAddress("321 Respondent Solicitor Street\nLondon\nSE1");
    }

    @Test
    public void whenPartyIsRepresented_thenSolicitorNameIsUsedOnCoverSheet() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithSolicitors();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "Mr J Solicitor");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Ms J Solicitor");
    }

    @Test
    public void whenPartyIsNotRepresented_thenPartyNameIsUsedOnCoverSheet() throws Exception {
        FinremCaseDetails caseDetails = caseDetailsWithEmptySolAddress();

        generateCoverSheetService.generateApplicantCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(1, "John Doe");

        generateCoverSheetService.generateRespondentCoverSheet(caseDetails, AUTH_TOKEN);
        assertAddresseeName(2, "Jane Doe");
    }

    private void assertCoversheetAddress(String formattedAddress) {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        assertThat(addressee.getFormattedAddress(), is(formattedAddress));
    }

    private void assertAddresseeName(int invocation, String name) {
        verify(genericDocumentService, times(invocation)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(), any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);
        Addressee addressee = mapper.convertValue(data.get(ADDRESSEE), Addressee.class);
        assertThat(addressee.getName(), is(name));
    }

    private void assertCoversheetCalledWithRequiredData() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            generateDocumentCaseDetailsCaptor.capture(),
            any(),
            any());
        Map<String, Object> data = getDataFromCaptor(generateDocumentCaseDetailsCaptor);

        String expectedCourtContactDetails =
            "HMCTS Financial Remedy\n"
            + "PO BOX 12746\n"
            + "HARLOW\n"
            + "CM20 9QZ";

        assertThat(data, hasKey(ADDRESSEE));
        assertThat(data, hasKey(COURT_CONTACT_DETAILS));
        assertEquals(expectedCourtContactDetails, data.get(COURT_CONTACT_DETAILS));
        assertThat(data, hasKey(CASE_NUMBER));
    }

    private FinremCaseDetails caseDetailsConsented() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/bulkprint/bulk-print.json"), mapper);
    }

    private FinremCaseDetails caseDetailsWithEmptySolAddress() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/bulkprint/bulk-print-empty-solicitor-address.json"), mapper);
    }

    private FinremCaseDetails caseDetailsWithSolicitors() throws Exception {
        return finremCaseDetailsFromResource(getResource("/fixtures/bulkprint/bulk-print-with-solicitors.json"), mapper);
    }
}
