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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContestedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
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

    private static final String INTERVENER_ADDED_SOLICITOR_TEMPLATE = "intervener_added_solicitor_template";
    private static final String INTERVENER_ADDED_SOLICITOR_FILENAME = "intervener_added_solicitor_filename";
    private static final String INTERVENER_REMOVED_SOLICITOR_TEMPLATE = "intervener_removed_solicitor_template";
    private static final String INTERVENER_REMOVED_SOLICITOR_FILENAME = "intervener_removed_solicitor_filename";
    private static final String INTERVENER_REMOVED_TEMPLATE = "intervener_removed_template";
    private static final String INTERVENER_REMOVED_FILENAME = "intervener_removed_filename";

    private static final String INTERVENER_NAME = "intervenerName";
    private static final String APPLICANT_NAME = "appName";
    private static final String RESPONDENT_NAME = "respName";
    private static final String REFERENCE = "Y0HO47OP8";

    private static final String INTERVENER_SOLICITOR_FIRM = "intervenerSolicitorFirm";

    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private CourtDetailsMapper courtDetailsMapper;
    private CaseDetails caseDetails;
    private FinremCaseDetails<FinremCaseDataContested> finremCaseDetails;
    private FinremCaseDataContested finremCaseData;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;


    @Before
    public void setUp() {
        intervenerDocumentService = new IntervenerDocumentService(genericDocumentService,
            documentConfiguration, documentHelper, objectMapper, courtDetailsMapper);
        IntervenerChangeDetails intervenerChangeDetails = new IntervenerChangeDetails();
        intervenerChangeDetails.setIntervenerType(IntervenerType.INTERVENER_ONE);
        intervenerChangeDetails.setIntervenerAction(IntervenerAction.ADDED);
        Organisation organisation = Organisation.builder()
            .organisationName(INTERVENER_SOLICITOR_FIRM).build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation).build();
        ContestedContactDetailsWrapper contactDetailsWrapper = ContestedContactDetailsWrapper.builder().build();
        contactDetailsWrapper.setSolicitorReference(REFERENCE);
        contactDetailsWrapper.setApplicantFmName(APPLICANT_NAME);
        contactDetailsWrapper.setRespondentFmName(RESPONDENT_NAME);
        IntervenerWrapper intervenerDetails = IntervenerOneWrapper.builder()
            .intervenerName(INTERVENER_NAME)
            .intervenerOrganisation(organisationPolicy).build();
        intervenerChangeDetails.setIntervenerDetails(intervenerDetails);
        finremCaseData = FinremCaseDataContested.builder()
            .divorceCaseNumber(CASE_NUMBER)
            .currentIntervenerChangeDetails(intervenerChangeDetails)
            .contactDetailsWrapper(contactDetailsWrapper)
            .build();
        finremCaseDetails = FinremCaseDetails.<FinremCaseDataContested>builder()
            .id(123L)
            .caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseData).build();
    }

    @Test
    public void shouldGenerateIntervenerAddedLetter() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerAddedTemplate()).thenReturn(INTERVENER_ADDED_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedFilename()).thenReturn(INTERVENER_ADDED_FILENAME);

        intervenerDocumentService.generateIntervenerAddedNotificationLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_ADDED_TEMPLATE), eq(INTERVENER_ADDED_FILENAME), eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertNull(letterData.get("reference"));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("caseNumber"), is("123"));
    }

    @Test
    public void shouldGenerateIntervenerRemovedLetter() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerRemovedTemplate()).thenReturn(INTERVENER_REMOVED_TEMPLATE);
        when(documentConfiguration.getIntervenerRemovedFilename()).thenReturn(INTERVENER_REMOVED_FILENAME);

        intervenerDocumentService.generateIntervenerRemovedNotificationLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_REMOVED_TEMPLATE), eq(INTERVENER_REMOVED_FILENAME), eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertNull(letterData.get("reference"));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("caseNumber"), is("123"));
    }

    @Test
    public void shouldGenerateIntervenerAddedSolicitorLetterWhenApplicantAndRespondentRepresented() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerAddedSolicitorTemplate()).thenReturn(INTERVENER_ADDED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedSolicitorFilename()).thenReturn(INTERVENER_ADDED_SOLICITOR_FILENAME);
        ContestedContactDetailsWrapper contactDetailsWrapper = finremCaseDetails.getData().getContactDetailsWrapper();
        contactDetailsWrapper.setApplicantRepresented(YesOrNo.YES);
        contactDetailsWrapper.setContestedRespondentRepresented(YesOrNo.YES);

        intervenerDocumentService.generateIntervenerSolicitorAddedLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_ADDED_SOLICITOR_TEMPLATE), eq(INTERVENER_ADDED_SOLICITOR_FILENAME),
            eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);

        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertThat(letterData.get("reference"), is(REFERENCE));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
    }

    @Test
    public void shouldGenerateIntervenerAddedSolicitorLetterWhenApplicantAndRespondentNotRepresented() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerAddedSolicitorTemplate()).thenReturn(INTERVENER_ADDED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerAddedSolicitorFilename()).thenReturn(INTERVENER_ADDED_SOLICITOR_FILENAME);

        intervenerDocumentService.generateIntervenerSolicitorAddedLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_ADDED_SOLICITOR_TEMPLATE), eq(INTERVENER_ADDED_SOLICITOR_FILENAME),
            eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertNull(letterData.get("reference"));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("caseNumber"), is("123"));
    }

    @Test
    public void shouldGenerateIntervenerRemovedSolicitorLetterWhenApplicantAndRespondentRepresented() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerRemovedSolicitorTemplate()).thenReturn(INTERVENER_REMOVED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerRemovedSolicitorFilename()).thenReturn(INTERVENER_REMOVED_SOLICITOR_FILENAME);
        ContestedContactDetailsWrapper contactDetailsWrapper = finremCaseDetails.getData().getContactDetailsWrapper();
        contactDetailsWrapper.setApplicantRepresented(YesOrNo.YES);
        contactDetailsWrapper.setContestedRespondentRepresented(YesOrNo.YES);

        intervenerDocumentService.generateIntervenerSolicitorRemovedLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_REMOVED_SOLICITOR_TEMPLATE), eq(INTERVENER_REMOVED_SOLICITOR_FILENAME),
            eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);

        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertThat(letterData.get("reference"), is(REFERENCE));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
    }

    @Test
    public void shouldGenerateIntervenerRemovedSolicitorLetterWhenApplicantAndRespondentNotRepresented() {
        when(documentHelper.prepareIntervenerLetterTemplateData(finremCaseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT))
            .thenReturn(caseDetails);

        when(documentConfiguration.getIntervenerRemovedSolicitorTemplate()).thenReturn(INTERVENER_REMOVED_SOLICITOR_TEMPLATE);
        when(documentConfiguration.getIntervenerRemovedSolicitorFilename()).thenReturn(INTERVENER_REMOVED_SOLICITOR_FILENAME);

        intervenerDocumentService.generateIntervenerSolicitorRemovedLetter(finremCaseDetails,
            AUTH_TOKEN, DocumentHelper.PaperNotificationRecipient.APPLICANT);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(INTERVENER_REMOVED_SOLICITOR_TEMPLATE), eq(INTERVENER_REMOVED_SOLICITOR_FILENAME),
            eq("123"));

        Map<String, Object> letterData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(letterData.get("intervenerFullName"), is(INTERVENER_NAME));
        assertThat(letterData.get("intervenerSolicitorFirm"), is(INTERVENER_SOLICITOR_FIRM));
        assertThat(letterData.get("divorceCaseNumber"), is(CASE_NUMBER));
        assertNull(letterData.get("reference"));
        assertThat(letterData.get("applicantName"), is(APPLICANT_NAME));
        assertThat(letterData.get("respondentName"), is(RESPONDENT_NAME));
        assertThat(letterData.get("caseNumber"), is("123"));
    }

    private Map<String, Object> getPlaceholdersMap(ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor) {
        Map<String, Object> map = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        return (Map<String, Object>) map.get(CASE_DATA);
    }
}
