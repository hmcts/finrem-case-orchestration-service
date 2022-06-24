package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;

public class InterimHearingServiceTest extends BaseServiceTest  {

    @Autowired
    private InterimHearingService interimHearingService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private DocumentConfiguration documentConfiguration;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @Autowired
    private DocumentHelper documentHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";
    private static final String TEST_NEW_JSON = "/fixtures/contested/interim-hearing-three-collection-no-track.json";


    @Test
    public void submitInterimHearingOneNewOneExisting() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_JSON);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        Map<String, Object> data = caseDetails.getData();
        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseDetails.getData().get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        verifyNonCollectionData(data);
    }

    private void verifyNonCollectionData(Map<String, Object> data) {
        assertNull(data.get(INTERIM_HEARING_TYPE));
        assertNull(data.get(INTERIM_HEARING_DATE));
        assertNull(data.get(INTERIM_HEARING_TIME));
        assertNull(data.get(INTERIM_HEARING_TIME_ESTIMATE));
        assertNull(data.get(INTERIM_HEARING_REGION_LIST));
        assertNull(data.get(INTERIM_HEARING_CFC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_WALES_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LONDON_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DEVON_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DORSET_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_HUMBER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BRISTOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NEWPORT_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SWANSEA_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LIVERPOOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BIRMINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_CLEAVELAND_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_KENT_SURREY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LANCASHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MANCHESTER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWALES_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NOTTINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_THAMESVALLEY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_ADDITIONAL_INFO));
        assertNull(data.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        assertNull(data.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
    }

    @Test
    public void submitInterimHearingThreeNewOne() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseDetails.getData().get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());
    }

    @Test
    public void submitInterimHearingThreeNewOneNonPaper() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put("paperApplication", "No");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("respondentRepresented", "No");

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(caseDocument());

        interimHearingService.submitInterimHearing(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());

        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseDetails.getData().get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        assertEquals("2010-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2020-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals("2030-10-10", interimHearingList.get(2).getValue().getInterimHearingDate());
    }

    private CallbackRequest buildCallbackRequest(String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}