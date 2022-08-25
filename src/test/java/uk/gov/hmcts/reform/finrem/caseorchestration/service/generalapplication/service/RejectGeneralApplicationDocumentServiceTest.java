package uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.generalapplication.GeneralApplicationRejectionLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.generators.GeneralApplicationRejectionLetterGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class RejectGeneralApplicationDocumentServiceTest extends BaseServiceTest {

    @Mock
    private GeneralApplicationRejectionLetterGenerator generalApplicationRejectionLetterGenerator;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Autowired
    private DocumentConfiguration documentConfiguration;

    @InjectMocks
    private RejectGeneralApplicationDocumentService rejectGeneralApplicationDocumentService;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        documentConfiguration = new DocumentConfiguration();

        rejectGeneralApplicationDocumentService = new RejectGeneralApplicationDocumentService(generalApplicationRejectionLetterGenerator,
                genericDocumentService,
                documentConfiguration);
    }

    @Test
    public void givenValidCaseData_whenGenerateGeneralApplicationRejectionLetter_thenGenerateLetter() {
        when(generalApplicationRejectionLetterGenerator.generate(any(), any())).thenReturn(letterDetails());
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(documentConfiguration.getGeneralApplicationRejectionTemplate()),
            eq(documentConfiguration.getGeneralApplicationRejectionFileName()))).thenReturn(expectedCaseDocument());

        CaseDocument actualDocument = rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(caseDetails,
            AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        assertEquals(expectedCaseDocument(), actualDocument);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationRejectionTemplate()),
            eq(documentConfiguration.getGeneralApplicationRejectionFileName()));

        Map<String, Object> templateMapData = getCaseDataFromCaptor();

        assertThat(templateMapData.get("reference"), is("testLetterDetailsReference"));
        assertThat(templateMapData.get("applicantName"), is("testAppName"));
        assertThat(templateMapData.get("caseNumber"), is("testCaseNumber"));
        assertThat(templateMapData.get("respondentName"), is("testRespName"));
    }

    private CaseDocument expectedCaseDocument() {
        return CaseDocument.builder()
            .documentFilename("rejectedGeneralApplicationLetter.pdf")
            .documentUrl("https://rejecteddoc")
            .documentBinaryUrl("https://rejecteddoc/binary")
            .build();
    }

    private GeneralApplicationRejectionLetterDetails letterDetails() {
        return GeneralApplicationRejectionLetterDetails.builder()
            .reference("testLetterDetailsReference")
            .caseNumber("testCaseNumber")
            .applicantName("testAppName")
            .respondentName("testRespName")
            .build();
    }

    private Map<String, Object> getCaseDataFromCaptor() {
        Map<String, Object> placeHoldersMap = (Map) placeholdersMapCaptor.getValue().get("caseDetails");
        return (Map) placeHoldersMap.get("case_data");
    }
}
