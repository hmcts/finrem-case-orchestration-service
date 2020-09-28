package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CFC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;

public class GeneralApplicationDirectionsServiceTest extends BaseServiceTest {

    @Autowired private GeneralApplicationDirectionsService generalApplicationDirectionsService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DocumentConfiguration documentConfiguration;

    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private GenericDocumentService genericDocumentService;

    @Captor ArgumentCaptor<CaseDetails> documentGenerationRequestCaseDetailsCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test
    public void whenGeneralApplicationDirectionsStarted_thenStateSpecificFieldAreRemoved() {
        List<String> generalApplicationDirectionsSpecificFields = asList(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC,
            GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CFC_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT,
            GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT,
            GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION,
            GENERAL_APPLICATION_DIRECTIONS_RECITALS,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE,
            GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE);

        generalApplicationDirectionsService.startGeneralApplicationDirections(caseDetails);

        assertThat(caseDetails.getData(), not(hasKey(in(generalApplicationDirectionsSpecificFields))));
    }

    @Test
    public void whenGeneralApplicationDirectionsSubmitted_thenGeneralApplicationLatestDocumentIsUpdated() {
        List<String> generalApplicationLatestDocumentFields = asList(GENERAL_APPLICATION_DOCUMENT_LATEST, GENERAL_APPLICATION_DOCUMENT_LATEST_DATE);

        assertThat(caseDetails.getData(), not(hasKey(in(generalApplicationLatestDocumentFields))));

        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        assertThat(caseDetails.getData(), allOf(generalApplicationLatestDocumentFields.stream().map(Matchers::hasKey).collect(Collectors.toList())));
    }

    @Test
    public void givenHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenHearingNoticeIsPrinted() {
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeTemplate()),
            eq(documentConfiguration.getGeneralApplicationHearingNoticeFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();

        assertThat(data, allOf(
            Matchers.<String, Object>hasEntry("ccdCaseNumber", 1234567890L),
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            Matchers.<String, Object>hasEntry("hearingVenue", "Croydon County Court And Family Court, Croydon County Court, Altyre Road, Croydon, CR9 5AB"),
            hasKey("letterDate")));
    }

    @Test
    public void givenNoHearingRequired_whenGeneralApplicationDirectionsSubmitted_thenGeneralOrderIsPrinted() {
        caseDetails.getData().put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, NO_VALUE);
        generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, AUTH_TOKEN);

        verify(genericDocumentService, times(1)).generateDocument(
            eq(AUTH_TOKEN),
            documentGenerationRequestCaseDetailsCaptor.capture(),
            eq(documentConfiguration.getGeneralApplicationOrderTemplate()),
            eq(documentConfiguration.getGeneralApplicationOrderFileName()));
        verify(bulkPrintService, times(1)).printApplicantDocuments(any(), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(), eq(AUTH_TOKEN), any());

        Map<String, Object> data = documentGenerationRequestCaseDetailsCaptor.getValue().getData();

        assertThat(data, allOf(
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", "Kingston-Upon-Thames County Court And Family Court",
                "courtAddress", "Kingston upon Thames County Court, St James Road, Kingston-upon-Thames, KT1 2AD",
                "phoneNumber", "0208 972 8700",
                "email", "enquiries.kingston.countycourt@justice.gov.uk")),
            Matchers.<String, Object>hasEntry("applicantName", "Poor Guy"),
            Matchers.<String, Object>hasEntry("respondentName", "test Korivi"),
            hasKey("letterDate")));
    }
}
