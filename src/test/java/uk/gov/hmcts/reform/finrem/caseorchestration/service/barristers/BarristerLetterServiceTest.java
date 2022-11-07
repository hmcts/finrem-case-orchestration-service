package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.REMOVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;

@RunWith(MockitoJUnitRunner.class)
public class BarristerLetterServiceTest {

    private static final String APP_BARR_NAME = "app_Barr_name";
    private static final String APP_BARR_EMAIL = "app_Barr_email";
    private static final String APP_BARR_ORG = "app_BARR_Org";
    private static final String RESP_BARR_NAME = "resp_barr_name";
    private static final String RESP_BARR_EMAIL = "resp_BARR_EMAIL";
    private static final String RESP_BARR_ORG = "RESP_BARR_ORG";
    private static final String BARR_FIRM_NAME = "barr_firm_name";
    private static final String CASE_NUMBER = "1234567890";
    private static final String BARRISTER_ADDED_TEMPLATE = "barrister_added_template";
    private static final String BARRISTER_ADDED_FILENAME = "barrister_added_filename";
    private static final String BARRISTER_REMOVED_TEMPLATE = "barrister_removed_template";
    private static final String BARRISTER_REMOVED_FILENAME = "barrister_removed_filename";
    private static final String ADDED_BIN_URL = "added_bin_url";
    private static final String REMOVED_BIN_URL = "removed_bin_url";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private BarristerLetterDetailsGenerator barristerLetterDetailsGenerator;
    @Mock
    private BulkPrintService bulkPrintService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BarristerLetterService barristerLetterService;

    private CaseDetails caseDetails;
    private Barrister barrister;
    BarristerLetterTuple barristerLetterTuple;
    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void givenApplicantIsRepresentedBySolicitor_whenSendBarristerLetter_thenNoLetterSent() {
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        barristerLetterTuple = BarristerLetterTuple.of(APPLICANT, AUTH_TOKEN, ADDED);
        barrister = applicantBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(bulkPrintService, never()).sendDocumentForPrint(any(), any());
    }

    @Test
    public void givenRespondentIsRepresentedBySolicitor_whenSendBarristerLetter_thenNoLetterSent() {
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        barristerLetterTuple = BarristerLetterTuple.of(RESPONDENT, AUTH_TOKEN, ADDED);
        barrister = respondentBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(bulkPrintService, never()).sendDocumentForPrint(any(), any());
    }

    @Test
    public void givenApplicantUnrepresentedAndAddedBarrister_whenSendBarristerLetter_thenSendLetter() {
        BarristerLetterDetails letterDetails = barristerLetterDetails();
        CaseDocument addedCaseDocument = addedCaseDocument();
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(barristerLetterDetailsGenerator.generate(eq(caseDetails), eq(APPLICANT), any())).thenReturn(letterDetails);
        when(documentConfiguration.getBarristerAddedTemplate()).thenReturn(BARRISTER_ADDED_TEMPLATE);
        when(documentConfiguration.getBarristerAddedFilename()).thenReturn(BARRISTER_ADDED_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(BARRISTER_ADDED_TEMPLATE), eq(BARRISTER_ADDED_FILENAME))).thenReturn(addedCaseDocument);

        barristerLetterTuple = BarristerLetterTuple.of(APPLICANT, AUTH_TOKEN, ADDED);
        barrister = applicantBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(BARRISTER_ADDED_TEMPLATE), eq(BARRISTER_ADDED_FILENAME));
        verify(bulkPrintService).sendDocumentForPrint(addedCaseDocument, caseDetails);

        Map<String, Object> caseData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(caseData.get("barristerFirmName"), is(BARR_FIRM_NAME));
        assertThat(caseData.get("caseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void givenApplicantUnrepresentedAndRemovedBarrister_whenSendBarristerLetter_thenSendLetter() {
        BarristerLetterDetails letterDetails = barristerLetterDetails();
        CaseDocument removed = removedCaseDocument();
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(barristerLetterDetailsGenerator.generate(eq(caseDetails), eq(APPLICANT), any())).thenReturn(letterDetails);
        when(documentConfiguration.getBarristerRemovedTemplate()).thenReturn(BARRISTER_REMOVED_TEMPLATE);
        when(documentConfiguration.getBarristerRemovedFilename()).thenReturn(BARRISTER_REMOVED_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(BARRISTER_REMOVED_TEMPLATE), eq(BARRISTER_REMOVED_FILENAME))).thenReturn(removed);

        barristerLetterTuple = BarristerLetterTuple.of(APPLICANT, AUTH_TOKEN, REMOVED);
        barrister = applicantBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(BARRISTER_REMOVED_TEMPLATE), eq(BARRISTER_REMOVED_FILENAME));
        verify(bulkPrintService).sendDocumentForPrint(removed, caseDetails);

        Map<String, Object> caseData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(caseData.get("barristerFirmName"), is(BARR_FIRM_NAME));
        assertThat(caseData.get("caseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void givenRespondentUnrepresentedAndAddedBarrister_whenSendBarristerLetter_thenSendLetter() {
        BarristerLetterDetails letterDetails = barristerLetterDetails();
        CaseDocument addedCaseDocument = addedCaseDocument();
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);
        when(barristerLetterDetailsGenerator.generate(eq(caseDetails), eq(RESPONDENT), any())).thenReturn(letterDetails);
        when(documentConfiguration.getBarristerAddedTemplate()).thenReturn(BARRISTER_ADDED_TEMPLATE);
        when(documentConfiguration.getBarristerAddedFilename()).thenReturn(BARRISTER_ADDED_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(BARRISTER_ADDED_TEMPLATE), eq(BARRISTER_ADDED_FILENAME))).thenReturn(addedCaseDocument);

        barristerLetterTuple = BarristerLetterTuple.of(RESPONDENT, AUTH_TOKEN, ADDED);
        barrister = respondentBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(BARRISTER_ADDED_TEMPLATE), eq(BARRISTER_ADDED_FILENAME));
        verify(bulkPrintService).sendDocumentForPrint(addedCaseDocument, caseDetails);

        Map<String, Object> caseData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(caseData.get("barristerFirmName"), is(BARR_FIRM_NAME));
        assertThat(caseData.get("caseNumber"), is(CASE_NUMBER));
    }

    @Test
    public void givenRespondentUnrepresentedAndRemovedBarrister_whenSendBarristerLetter_thenSendLetter() {
        BarristerLetterDetails letterDetails = barristerLetterDetails();
        CaseDocument removed = removedCaseDocument();
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);
        when(barristerLetterDetailsGenerator.generate(eq(caseDetails), eq(RESPONDENT), any())).thenReturn(letterDetails);
        when(documentConfiguration.getBarristerRemovedTemplate()).thenReturn(BARRISTER_REMOVED_TEMPLATE);
        when(documentConfiguration.getBarristerRemovedFilename()).thenReturn(BARRISTER_REMOVED_FILENAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(),
            eq(BARRISTER_REMOVED_TEMPLATE), eq(BARRISTER_REMOVED_FILENAME))).thenReturn(removed);

        barristerLetterTuple = BarristerLetterTuple.of(RESPONDENT, AUTH_TOKEN, REMOVED);
        barrister = applicantBarrister();

        barristerLetterService.sendBarristerLetter(caseDetails, barrister, barristerLetterTuple);

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(), eq(BARRISTER_REMOVED_TEMPLATE), eq(BARRISTER_REMOVED_FILENAME));
        verify(bulkPrintService).sendDocumentForPrint(removed, caseDetails);

        Map<String, Object> caseData = getPlaceholdersMap(placeholdersMapCaptor);
        assertThat(caseData.get("barristerFirmName"), is(BARR_FIRM_NAME));
        assertThat(caseData.get("caseNumber"), is(CASE_NUMBER));
    }


    private Barrister applicantBarrister() {
        return Barrister.builder()
            .name(APP_BARR_NAME)
            .email(APP_BARR_EMAIL)
            .organisation(Organisation.builder()
                .organisationID(APP_BARR_ORG)
                .build())
            .build();
    }

    private Barrister respondentBarrister() {
        return Barrister.builder()
            .name(RESP_BARR_NAME)
            .email(RESP_BARR_EMAIL)
            .organisation(Organisation.builder()
                .organisationID(RESP_BARR_ORG)
                .build())
            .build();
    }

    private BarristerLetterDetails barristerLetterDetails() {
        return BarristerLetterDetails.builder()
            .barristerFirmName(BARR_FIRM_NAME)
            .caseNumber(CASE_NUMBER)
            .build();
    }

    private CaseDocument addedCaseDocument() {
        return CaseDocument.builder()
            .documentBinaryUrl(ADDED_BIN_URL)
            .build();
    }

    private CaseDocument removedCaseDocument() {
        return CaseDocument.builder()
            .documentBinaryUrl(REMOVED_BIN_URL)
            .build();
    }

    private Map<String, Object> getPlaceholdersMap(ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor) {
        Map<String, Object> map = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        return (Map<String, Object>) map.get(CASE_DATA);
    }
}