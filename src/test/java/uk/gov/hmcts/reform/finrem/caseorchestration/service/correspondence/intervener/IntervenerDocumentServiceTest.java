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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
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
    private static final String INTERVENER_REMOVED_TEMPLATE = "intervener_removed_template";
    private static final String INTERVENER_REMOVED_FILENAME = "intervener_removed_filename";

    private static final String INTERVENER_ADDED_SOLICITOR_TEMPLATE = "intervener_added_solicitor_template";
    private static final String INTERVENER_ADDED_SOLICITOR_FILENAME = "intervener_added_solicitor_filename";
    private static final String INTERVENER_REMOVED_SOLICITOR_TEMPLATE = "intervener_removed_solicitor_template";
    private static final String INTERVENER_REMOVED_SOLICITOR_FILENAME = "intervener_removed_solicitor_filename";

    private static final String INTERVENER_NAME = "intervenerName";
    private static final String INTERVENER_SOLICITOR_FIRM = "intervenerSolicitorFirm";

    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;

    private CaseDetails caseDetails;
    private FinremCaseDetails finremCaseDetails;
    private FinremCaseData finremCaseData;
    IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;


    @Before
    public void setUp() {
        intervenerDocumentService = new IntervenerDocumentService(genericDocumentService, documentConfiguration, documentHelper, objectMapper);
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        Organisation organisation = Organisation.builder().organisationName(INTERVENER_SOLICITOR_FIRM).build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(organisation).build();
        IntervenerDetails intervenerDetails =
            IntervenerDetails.builder().intervenerName(INTERVENER_NAME).intervenerOrganisation(organisationPolicy).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData = FinremCaseData.builder().divorceCaseNumber(CASE_NUMBER).currentIntervenerChangeDetails(intervenerChangeDetails).build();
        finremCaseDetails = FinremCaseDetails.builder().id(123L).data(finremCaseData).build();
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void shouldGenerateIntervenerAddedLetter() {
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDetails);

        when(documentConfiguration.getIntervenerAddedTemplate()).thenReturn(INTERVENER_ADDED_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedFilename()).thenReturn(INTERVENER_ADDED_FILENAME);

        intervenerDocumentService.generateIntervenerAddedNotificationLetter(finremCaseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(INTERVENER_ADDED_TEMPLATE), eq(INTERVENER_ADDED_FILENAME));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void shouldGenerateIntervenerRemovedLetter() {
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDetails);

        when(documentConfiguration.getIntervenerRemovedTemplate()).thenReturn(INTERVENER_REMOVED_TEMPLATE);
        when(documentConfiguration.getIntervenerRemovedFilename()).thenReturn(INTERVENER_REMOVED_FILENAME);

        intervenerDocumentService.generateIntervenerRemovedNotificationLetter(finremCaseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(INTERVENER_REMOVED_TEMPLATE), eq(INTERVENER_REMOVED_FILENAME));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void shouldGenerateIntervenerAddedSolicitorLetter() {
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDetails);

        when(documentConfiguration.getIntervenerAddedSolicitorTemplate()).thenReturn(INTERVENER_ADDED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedSolicitorFilename()).thenReturn(INTERVENER_ADDED_SOLICITOR_FILENAME);

        intervenerDocumentService.generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(INTERVENER_ADDED_SOLICITOR_TEMPLATE), eq(INTERVENER_ADDED_SOLICITOR_FILENAME));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void shouldGenerateIntervenerRemovedSolicitorLetter() {
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.REMOVED);
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(
            caseDetails);

        when(documentConfiguration.getIntervenerAddedSolicitorTemplate()).thenReturn(INTERVENER_REMOVED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedSolicitorFilename()).thenReturn(INTERVENER_REMOVED_SOLICITOR_FILENAME);

        intervenerDocumentService.generateIntervenerSolicitorAddedLetter(finremCaseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersMapCaptor.capture(),
            eq(INTERVENER_REMOVED_SOLICITOR_TEMPLATE), eq(INTERVENER_REMOVED_SOLICITOR_FILENAME));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
    }

    private Map<String, Object> getPlaceholdersMap(ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor) {
        Map<String, Object> map = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        return (Map<String, Object>) map.get(CASE_DATA);
    }
}
