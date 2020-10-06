package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ActiveProfiles("test-mock-document-client")
public class AdditionalHearingDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintDocumentsCaptor;

    @MockBean
    GenericDocumentService genericDocumentService;

    @MockBean
    BulkPrintService bulkPrintService;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setAdditionalHearingTemplate("FL-FRM-HNO-ENG-00588.docx");
        config.setAdditionalHearingFileName("AdditionalHearingDocument.pdf");
        objectMapper = new ObjectMapper();

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void generateAdditionalHearingDocument() throws IOException {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource("/fixtures/bulkprint/bulk-print-additional-hearing.json", objectMapper);
        additionalHearingDocumentService.createAndSendAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        CaseDetails captorCaseDetails = documentGenerationRequestCaseDetailsCaptor.getValue();
        Map<String, Object> data = captorCaseDetails.getData();

        assertThat(data.get("CCDCaseNumber"), is(1234567890L));
        assertThat(data.get("DivorceCaseNumber"), is("AB01D23456"));
        assertThat(data.get("ApplicantName"), is("Test Applicant"));
        assertThat(data.get("RespondentName"), is("Name Respondent"));

        assertThat(data.get("HearingType"), is("Directions (DIR)"));
        assertThat(data.get("HearingVenue"), is("Nottingham County Court And Family Court"));
        assertThat(data.get("HearingDate"), is("2021-01-01"));
        assertThat(data.get("HearingTime"), is("12:00"));
        assertThat(data.get("HearingLength"), is("30 minutes"));
        assertThat(data.get("AnyOtherDirections"), is("N/A"));

        assertThat(data.get("CourtName"), is("Nottingham County Court And Family Court"));
        assertThat(data.get("CourtAddress"), is("60 Canal Street, Nottingham NG1 7EJ"));
        assertThat(data.get("CourtPhone"), is("0115 910 3504"));
        assertThat(data.get("CourtEmail"), is("FRCNottingham@justice.gov.uk"));

        verify(bulkPrintService, times(1)).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(1));
    }
}