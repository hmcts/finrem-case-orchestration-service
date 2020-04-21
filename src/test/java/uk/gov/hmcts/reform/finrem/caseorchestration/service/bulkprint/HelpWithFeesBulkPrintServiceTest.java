package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.HelpWithFeesSuccessLetter;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentServiceTest.buildCaseDetails;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class HelpWithFeesBulkPrintServiceTest {

    public static final String FILE_NAME = "HelpWithFeesSuccessfulLetter.pdf";
    public static final String SOLICITOR_NAME_VALUE = "Mr solicitor";
    @Mock
    private DocumentClient documentClient;

    private ArgumentCaptor<DocumentGenerationRequest> bulkPrintRequestGeneratePdfCaptor;
    private ArgumentCaptor<BulkPrintRequest> bulkPrintRequestBulkPrintCaptor;

    private HelpWithFeesBulkPrintService helpWithFeesBulkPrintService;

    @Before
    public void setup() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulTemplate("FL-FRM-DEC-ENG-00096.docx");
        config.setHelpWithFeesSuccessfulFileName(FILE_NAME);
        bulkPrintRequestGeneratePdfCaptor = ArgumentCaptor.forClass(DocumentGenerationRequest.class);

        when(documentClient.generatePdf(bulkPrintRequestGeneratePdfCaptor.capture(), anyString()))
                .thenReturn(buildDocumentModel());

        helpWithFeesBulkPrintService = new HelpWithFeesBulkPrintService(
                documentClient, config, new ObjectMapper()
        );
    }

    @Test
    public void sendLetterShouldBeOkWhenNotRepresented() {
        CaseDetails caseDetails = buildCaseDetails();

        helpWithFeesBulkPrintService.sendLetter(AUTH_TOKEN, caseDetails);

        CaseDocument caseDocument = (CaseDocument) (caseDetails.getData().get("hwfSuccessNotificationLetter"));

        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(getLetterAddressee().getName(), isApplicant());

        verify(documentClient).generatePdf(any(DocumentGenerationRequest.class), eq(AUTH_TOKEN));
    }

    @Test
    public void sendLetterShouldBeOkWhenRepresented() {
        CaseDetails caseDetails = buildCaseDetailsWithRepresentedApplicant();

        helpWithFeesBulkPrintService.sendLetter(AUTH_TOKEN, caseDetails);

        CaseDocument caseDocument = (CaseDocument) (caseDetails.getData().get("hwfSuccessNotificationLetter"));

        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(getLetterAddressee().getName(), isSolicitor());

        verify(documentClient).generatePdf(any(DocumentGenerationRequest.class), eq(AUTH_TOKEN));
    }

    private Addressee getLetterAddressee() {
        Map<String, Object> caseDetails = (Map) (bulkPrintRequestGeneratePdfCaptor.getValue().getValues().get("caseDetails"));
        HelpWithFeesSuccessLetter helpWithFeesSuccessLetter = (HelpWithFeesSuccessLetter) (caseDetails.get("caseData"));

        return helpWithFeesSuccessLetter.getAddressee();
    }

    private static Matcher<String> isApplicant() {
        return is("James Joyce");
    }

    private static Matcher<String> isSolicitor() {
        return is(SOLICITOR_NAME_VALUE);
    }

    private static CaseDetails buildCaseDetailsWithRepresentedApplicant() {
        CaseDetails caseDetails = buildCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(SOLICITOR_NAME, SOLICITOR_NAME_VALUE);
        caseData.put(APP_SOLICITOR_ADDRESS_CCD_FIELD, ImmutableMap.of(
                "AddressLine1", "102 Petty France",
                "AddressLine2", "Floor 6",
                "AddressLine3", "My desk",
                "PostTown", "London",
                "PostCode", "4YU 0IO"
        ));

        return caseDetails;
    }

    private static Document buildDocumentModel() {
        Document document = new Document();
        document.setBinaryUrl("binaryUrl");
        document.setFileName(FILE_NAME);
        document.setUrl("url");

        return document;
    }
}
