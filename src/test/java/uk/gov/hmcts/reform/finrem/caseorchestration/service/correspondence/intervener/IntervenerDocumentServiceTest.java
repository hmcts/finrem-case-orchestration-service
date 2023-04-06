package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;

@RunWith(MockitoJUnitRunner.class)
public class IntervenerDocumentServiceTest {

    IntervenerDocumentService intervenerDocumentService;

    private static final String INTERVENER_ADDED_TEMPLATE = "intervener_added_template";
    private static final String INTERVENER_ADDED_FILENAME = "intervener_added_filename";

    private static final String INTERVENER_NAME = "intervenerName";

    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;

    private CaseDetails caseDetails;
    private FinremCaseDetails finremCaseDetails;
    private FinremCaseData finremCaseData;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;


    @Before
    public void setUp() {
        intervenerDocumentService = new IntervenerDocumentService(genericDocumentService,
            documentConfiguration, documentHelper, objectMapper);
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails(
            IntervenerChangeDetails.IntervenerType.INTERVENER_ONE,
            IntervenerChangeDetails.IntervenerAction.ADDED);
        IntervenerDetails intervenerDetails = IntervenerDetails.builder()
            .intervenerName(INTERVENER_NAME).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData = FinremCaseData.builder()
            .divorceCaseNumber(CASE_NUMBER)
            .currentIntervenerChangeDetails(intervenerChangeDetails)
            .build();
        finremCaseDetails = FinremCaseDetails.builder()
            .id(123L)
            .data(finremCaseData).build();
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseData).build();
    }

    @Test
    public void shouldGenerateIntervenerAddedLetter() {
        when(documentHelper.prepareLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerAddedTemplate()).thenReturn(INTERVENER_ADDED_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedFilename()).thenReturn(INTERVENER_ADDED_FILENAME);

        intervenerDocumentService.generateIntervenerAddedNotificationLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_ADDED_TEMPLATE), eq(INTERVENER_ADDED_FILENAME));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));

    }

    private Map<String, Object> getPlaceholdersMap(ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor) {
        Map<String, Object> map = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        return (Map<String, Object>) map.get(CASE_DATA);
    }
}
