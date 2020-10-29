package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST_CT;

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
    public void generateAndAddAdditionalHearingDocument() throws IOException {
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
        assertThat(data.get("AdditionalHearingDated"), is(notNullValue()));

        assertThat(data.get("CourtName"), is("Nottingham County Court And Family Court"));
        assertThat(data.get("CourtAddress"), is("60 Canal Street, Nottingham NG1 7EJ"));
        assertThat(data.get("CourtPhone"), is("0115 910 3504"));
        assertThat(data.get("CourtEmail"), is("FRCNottingham@justice.gov.uk"));

        verify(bulkPrintService, times(1)).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(1));
        assertThat(caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION), is(notNullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments() throws JsonProcessingException {
        Map<String, Object> caseData = baseCaseData();
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, buildDirectionDetailsCollectionDataList(true));

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, times(1)).generateDocument(any(),
            documentGenerationRequestCaseDetailsCaptor.capture(), any(), any());

        CaseDetails captorCaseDetails = documentGenerationRequestCaseDetailsCaptor.getValue();
        Map<String, Object> data = captorCaseDetails.getData();

        assertThat(data.get("CCDCaseNumber"), is(1234567890L));
        assertThat(data.get("DivorceCaseNumber"), is("AB01D23456"));
        assertThat(data.get("ApplicantName"), is("Test Applicant"));
        assertThat(data.get("RespondentName"), is("Name Respondent"));

        assertThat(data.get("HearingType"), is("Final Hearing (FH)"));
        assertThat(data.get("HearingVenue"), is("Hastings County Court And Family Court Hearing Centre"));
        assertThat(data.get("HearingDate"), is("2020-01-01"));
        assertThat(data.get("HearingTime"), is("12"));
        assertThat(data.get("HearingLength"), is("12"));
        assertThat(data.get("AdditionalHearingDated"), is(notNullValue()));

        assertThat(data.get("CourtName"), is("Hastings County Court And Family Court Hearing Centre"));
        assertThat(data.get("CourtAddress"), is("The Law Courts, Bohemia Road, Hastings, TN34 1QX"));
        assertThat(data.get("CourtPhone"), is("01634 887900"));
        assertThat(data.get("CourtEmail"), is("FRCKSS@justice.gov.uk"));

        assertThat(caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION), is(notNullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_noNextHearing() throws JsonProcessingException {
        Map<String, Object> caseData = baseCaseData();
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, buildDirectionDetailsCollectionDataList(false));

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, never()).generateDocument(any(), any(), any(), any());
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_noHearingDetails() throws JsonProcessingException {
        Map<String, Object> caseData = baseCaseData();
        caseData.put(DIRECTION_DETAILS_COLLECTION_CT, Collections.EMPTY_LIST);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, never()).generateDocument(any(), any(), any(), any());
    }

    private Map<String, Object> baseCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(DIVORCE_CASE_NUMBER, "AB01D23456");
        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Test");
        caseData.put(APPLICANT_LAST_NAME, "Applicant");
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Name");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Respondent");

        return caseData;
    }

    private List<DirectionDetailsCollectionData> buildDirectionDetailsCollectionDataList(boolean isNextHearing) {
        Map<String, Object> localCourtMap = new HashMap<>();
        localCourtMap.put(REGION_CT, SOUTHEAST);
        localCourtMap.put(SOUTHEAST_FRC_LIST_CT, KENT);
        localCourtMap.put(KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_9");

        DirectionDetailsCollection directionDetailsCollection = DirectionDetailsCollection
            .builder()
            .isAnotherHearingYN(isNextHearing ? "Yes" : "No")
            .typeOfHearing("Final Hearing (FH)")
            .dateOfHearing("2020-01-01")
            .timeEstimate("12")
            .hearingTime("12")
            .localCourt(localCourtMap)
            .build();

        DirectionDetailsCollectionData directionDetailsCollectionData = DirectionDetailsCollectionData
            .builder()
            .id(UUID.randomUUID().toString())
            .directionDetailsCollection(directionDetailsCollection)
            .build();

        List<DirectionDetailsCollectionData> directionDetailsCollectionList = new ArrayList<>();
        directionDetailsCollectionList.add(directionDetailsCollectionData);

        return directionDetailsCollectionList;
    }

    @Test
    public void alreadyHadFirstHearing_containsForm() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FORM_C, "form c");

        CaseDetails caseDetails = CaseDetails
            .builder()
            .data(caseData)
            .build();

        assertTrue(additionalHearingDocumentService.alreadyHadFirstHearing(caseDetails));
    }

    @Test
    public void alreadyHadFirstHearing_containsNoForm() {
        Map<String, Object> caseData = new HashMap<>();

        CaseDetails caseDetails = CaseDetails
            .builder()
            .data(caseData)
            .build();

        assertFalse(additionalHearingDocumentService.alreadyHadFirstHearing(caseDetails));
    }
}