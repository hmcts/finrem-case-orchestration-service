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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.AssignedToJudgeLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentServiceTest.buildCaseDetails;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class AssignedToJudgeBulkPrintServiceTest {

    public static final String FILE_NAME = "ApplicationHasBeenAssignedToJudge.pdf";

    @Mock
    private DocumentClient documentClient;

    private ArgumentCaptor<DocumentGenerationRequest> bulkPrintRequestArgumentCaptor;
    private AssignedToJudgeBulkPrintService assignedToJudgeBulkPrintService;

    @Before
    public void setup() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setApplicationAssignedToJudgeTemplate("FL-FRM-LET-ENG-00318.docx");
        config.setApplicationAssignedToJudgeFileName(FILE_NAME);
        bulkPrintRequestArgumentCaptor = ArgumentCaptor.forClass(DocumentGenerationRequest.class);

        when(documentClient.generatePdf(bulkPrintRequestArgumentCaptor.capture(), anyString()))
                .thenReturn(buildDocumentModel());

        assignedToJudgeBulkPrintService = new AssignedToJudgeBulkPrintService(
                documentClient, config, new ObjectMapper()
        );
    }

    @Test
    public void sendLetterShouldBeOkWhenNotRepresented() {
        CaseDetails caseDetails = assignedToJudgeBulkPrintService.sendLetter(AUTH_TOKEN, buildCaseDetails());

        CaseDocument caseDocument = (CaseDocument) (caseDetails.getData().get(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER));

        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(getLetterAddressee().getName(), isApplicant());

        verify(documentClient).generatePdf(any(DocumentGenerationRequest.class), eq(AUTH_TOKEN));
    }

    @Test
    public void sendLetterShouldBeOkWhenRepresented() {
        CaseDetails caseDetails = assignedToJudgeBulkPrintService
                .sendLetter(AUTH_TOKEN, buildCaseDetailsWithRepresentedApplicant());

        CaseDocument caseDocument = (CaseDocument) (caseDetails.getData().get(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER));

        assertThat(caseDocument.getDocumentFilename(), is(FILE_NAME));
        assertThat(getLetterAddressee().getName(), isSolicitor());

        verify(documentClient).generatePdf(any(DocumentGenerationRequest.class), eq(AUTH_TOKEN));
    }

    private Addressee getLetterAddressee() {
        Map caseDetails = (Map) (bulkPrintRequestArgumentCaptor.getValue().getValues().get("caseDetails"));
        AssignedToJudgeLetter assignedToJudgeLetter = (AssignedToJudgeLetter) (caseDetails.get("caseData"));

        return assignedToJudgeLetter.getAddressee();
    }

    private static Matcher<String> isApplicant() {
        return is("James Joyce");
    }

    private static Matcher<String> isSolicitor() {
        return is(TEST_SOLICITOR_NAME);
    }

    private static CaseDetails buildCaseDetailsWithRepresentedApplicant() {
        CaseDetails caseDetails = buildCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
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
        document.setBinaryUrl("sadasd");
        document.setFileName(FILE_NAME);
        document.setUrl("sdsdsd");

        return document;
    }

    /*
    add test for feature toggle
     */
}
