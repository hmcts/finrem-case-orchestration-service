package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing.AdditionalHearingDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Court;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.ccd.domain.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class AdditionalHearingDocumentServiceTest extends BaseServiceTest {

    static final String CASE_DETAILS = "caseDetails";
    static final String CASE_DATA = "case_data";

    public static final String BULK_PRINT_ADDITIONAL_HEARING_JSON = "/fixtures/bulkprint/bulk-print-additional-hearing.json";
    @Autowired private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Autowired private ObjectMapper objectMapper;

    @Captor private ArgumentCaptor<FinremCaseDetails> finremCaseDetailsCaptor;

    @Captor private ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    @MockBean GenericDocumentService genericDocumentService;
    @MockBean BulkPrintService bulkPrintService;
    @MockBean AdditionalHearingDetailsMapper additionalHearingDetailsMapper;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(newDocument());
    }

    @Test
    public void generateAndAddAdditionalHearingDocument() throws IOException {
        FinremCaseDetails caseDetails = finremCaseDetailsFromResource(getResource(BULK_PRINT_ADDITIONAL_HEARING_JSON),
            objectMapper);
        when(additionalHearingDetailsMapper.getDocumentTemplateDetailsAsMap(any(), any())).thenReturn(expectedPlaceholdersMap());
        additionalHearingDocumentService.createAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        Map<String, Object> data = getCaseDataFromCaptor();

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

        assertThat(caseDetails.getCaseData().getAdditionalHearingDocuments(), is(notNullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(true));

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        when(additionalHearingDetailsMapper.getDocumentTemplateDetailsAsMap(any(), any()))
            .thenReturn(expectedPlaceholdersMapHastingsCourt());

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        Map<String, Object> data = getCaseDataFromCaptor();

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


        assertThat(caseDetails.getCaseData().getAdditionalHearingDocuments(), is(notNullValue()));
    }

    private void verifyInteractions() {
        verify(genericDocumentService, times(1)).generateDocumentFromPlaceholdersMap(any(),
            placeholdersMapCaptor.capture(), any(), any());
        verify(additionalHearingDetailsMapper, times(1))
            .getDocumentTemplateDetailsAsMap(finremCaseDetailsCaptor.capture(), any());
    }

    private Map<String, Object> getCaseDataFromCaptor() {
        Map<String, Object> caseDetailsMap = placeholdersMapCaptor.getValue();
        Map<String, Object> caseDataMap = (Map) caseDetailsMap.get(CASE_DETAILS);
        Map<String, Object> data = (Map) caseDataMap.get(CASE_DATA);
        return data;
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_withMultipleHearingsInList() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();

        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(true));
        addEntryToDirectionDetailsCollectionDataList(caseData.getDirectionDetailsCollection());

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData).build();

        when(additionalHearingDetailsMapper.getDocumentTemplateDetailsAsMap(any(), any()))
            .thenReturn(expectedPlaceholdersMapLongerHearing());
        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        Map<String, Object> data = getCaseDataFromCaptor();

        assertThat(data.get("CCDCaseNumber"), is(1234567890L));
        assertThat(data.get("DivorceCaseNumber"), is("AB01D23456"));
        assertThat(data.get("ApplicantName"), is("Test Applicant"));
        assertThat(data.get("RespondentName"), is("Name Respondent"));

        assertThat(data.get("HearingType"), is("Final Hearing (FH)"));
        assertThat(data.get("HearingVenue"), is("Nottingham County Court And Family Court"));
        assertThat(data.get("HearingDate"), is("2021-01-01"));
        assertThat(data.get("HearingLength"), is("1 hour"));
        assertThat(data.get("HearingTime"), is("15:00"));
        assertThat(data.get("AdditionalHearingDated"), is(notNullValue()));

        assertThat(data.get("CourtName"), is("Nottingham County Court And Family Court"));
        assertThat(data.get("CourtAddress"), is("60 Canal Street, Nottingham NG1 7EJ"));
        assertThat(data.get("CourtPhone"), is("0115 910 3504"));
        assertThat(data.get("CourtEmail"), is("FRCNottingham@justice.gov.uk"));

        assertThat(caseDetails.getCaseData().getAdditionalHearingDocuments(), is(notNullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_noNextHearing() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(false));

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, never()).generateDocumentFromPlaceholdersMap(any(), any(), any(), any());
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_noHearingDetails() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        caseData.setDirectionDetailsCollection(Collections.emptyList());

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verify(genericDocumentService, never()).generateDocumentFromPlaceholdersMap(any(), any(), any(), any());
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_caseworkerUploadsOrder() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        List<DirectionOrderCollection> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(true));
        caseData.setUploadHearingOrder(hearingOrderCollectionData);

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        FinremCaseDetails capturedDetails = finremCaseDetailsCaptor.getValue();

        assertThat(capturedDetails.getCaseData().getLatestDraftHearingOrder(), is(notNullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_caseworkerDoesntUploadsOrder() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(true));

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        FinremCaseDetails capturedDetails = finremCaseDetailsCaptor.getValue();

        assertThat(capturedDetails.getCaseData().getLatestDraftHearingOrder(), is(nullValue()));
    }

    @Test
    public void createAndStoreAdditionalHearingDocuments_noHearingOrderDocuments() throws JsonProcessingException {
        FinremCaseData caseData = baseCaseData();
        List<DirectionOrderCollection> hearingOrderCollectionData = buildHearingOrderCollectionData();
        hearingOrderCollectionData.get(0).setValue(null);
        caseData.setDirectionDetailsCollection(buildDirectionDetailsCollectionDataList(true));
        caseData.setUploadHearingOrder(hearingOrderCollectionData);

        FinremCaseDetails caseDetails = FinremCaseDetails
            .builder()
            .id(1234567890L)
            .caseData(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyInteractions();

        FinremCaseDetails capturedDetails = finremCaseDetailsCaptor.getValue();

        assertThat(capturedDetails.getCaseData().getLatestDraftHearingOrder(), is(nullValue()));
    }
    @Test
    public void givenAdditionalDocumentsToBeStored_whenCreateAndStoreAdditionalHearingDocumentsFromApprovedOrder_thenStore() {
        CaseDocument expectedDocument = CaseDocument.builder().documentBinaryUrl("docBin").documentFilename("docFilename")
            .documentUrl("docUrl").build();
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any())).thenReturn(expectedDocument);
        Map<String, Object> caseData = baseCaseData();
        List<HearingOrderCollectionData> hearingOrderCollectionData = buildHearingOrderCollectionData();
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(1234567890L)
            .data(caseData)
            .build();

        additionalHearingDocumentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(AUTH_TOKEN, caseDetails);
        assertTrue(caseDetails.getData().containsKey(LATEST_DRAFT_HEARING_ORDER));
        CaseDocument actualDocument = mapper.convertValue(caseDetails.getData().get(LATEST_DRAFT_HEARING_ORDER),
            CaseDocument.class);
        assertEquals(expectedDocument, actualDocument);
    }



    private FinremCaseData baseCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.setDivorceCaseNumber("AB01D23456");
        caseData.getContactDetailsWrapper().setApplicantFmName("Test");
        caseData.getContactDetailsWrapper().setApplicantLname("Applicant");
        caseData.getContactDetailsWrapper().setRespondentFmName("Name");
        caseData.getContactDetailsWrapper().setRespondentLname("Respondent");

        return caseData;
    }

    private List<DirectionDetailCollection> buildDirectionDetailsCollectionDataList(boolean isNextHearing) {
        Court localCourt = Court.builder()
            .region(Region.SOUTHEAST)
            .southEastList(RegionSouthEastFrc.KENT)
            .courtListWrapper(DefaultCourtListWrapper.builder()
                .kentSurreyCourtList(KentSurreyCourt.FR_kent_surreyList_9)
                .build())
            .build();

        DirectionDetailCollection directionDetailsCollection = DirectionDetailCollection
            .builder()
            .value(DirectionDetail.builder()
                .isAnotherHearingYN(isNextHearing ? YesOrNo.YES : YesOrNo.NO)
                .typeOfHearing(HearingTypeDirection.FH)
                .dateOfHearing(LocalDate.of(2020, 1, 1))
                .timeEstimate("12")
                .hearingTime("12")
                .localCourt(localCourt)
                .build())
            .build();

        return new ArrayList<>(List.of(directionDetailsCollection));
    }

    private void addEntryToDirectionDetailsCollectionDataList(
        List<DirectionDetailCollection> directionDetailsCollectionList) {

        Court localCourt = Court.builder()
            .region(Region.MIDLANDS)
            .midlandsList(RegionMidlandsFrc.NOTTINGHAM)
            .courtListWrapper(DefaultCourtListWrapper.builder()
                .nottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT)
                .build())
            .build();

        DirectionDetailCollection directionDetailsCollection = DirectionDetailCollection
            .builder()
            .value(DirectionDetail.builder()
                .isAnotherHearingYN(YesOrNo.YES)
                .typeOfHearing(HearingTypeDirection.FH)
                .dateOfHearing(LocalDate.of(2021, 1, 1))
                .timeEstimate("1 hour")
                .hearingTime("15:00")
                .localCourt(localCourt)
                .build())
            .build();

        directionDetailsCollectionList.add(directionDetailsCollection);
    }

    private List<DirectionOrderCollection> buildHearingOrderCollectionData() {
        Document caseDocument = Document
            .builder()
            .binaryUrl("docBin")
            .filename("docFilename")
            .url("docUrl")
            .build();

        DirectionOrderCollection directionOrder = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument)
                .build())
            .build();

        return List.of(directionOrder);
    }

    private Map<String, Object> expectedPlaceholdersMap() {
        Map<String, Object> placeholdersMap = new HashMap<>(Map.of(
            "CCDCaseNumber", 1234567890L,
            "DivorceCaseNumber", "AB01D23456",
            "ApplicantName", "Test Applicant",
            "RespondentName", "Name Respondent",
            "HearingType", "Directions (DIR)",
            "HearingVenue", "Nottingham County Court And Family Court",
            "HearingDate", "2021-01-01",
            "HearingLength", "30 minutes",
            "HearingTime", "12:00",
            "AdditionalHearingDated", "notNull"));

        placeholdersMap.put("AnyOtherDirections", "N/A");
        placeholdersMap.put("CourtName", "Nottingham County Court And Family Court");
        placeholdersMap.put("CourtAddress", "60 Canal Street, Nottingham NG1 7EJ");
        placeholdersMap.put("CourtPhone", "0115 910 3504");
        placeholdersMap.put("CourtEmail", "FRCNottingham@justice.gov.uk");

        Map<String, Object> caseDataMap = Map.of(
            CASE_DATA, placeholdersMap,
            "id", 1234567890L);

        return Map.of(CASE_DETAILS, caseDataMap);
    }

    private Map<String, Object> expectedPlaceholdersMapHastingsCourt() {
        Map<String, Object> placeholdersMap = new HashMap<>(Map.of(
            "CCDCaseNumber", 1234567890L,
            "DivorceCaseNumber", "AB01D23456",
            "ApplicantName", "Test Applicant",
            "RespondentName", "Name Respondent",
            "HearingType", "Final Hearing (FH)",
            "HearingVenue", "Hastings County Court And Family Court Hearing Centre",
            "HearingDate", "2020-01-01",
            "HearingLength", "12",
            "HearingTime", "12",
            "AdditionalHearingDated", "notNull"));

        placeholdersMap.put("AnyOtherDirections", "N/A");
        placeholdersMap.put("CourtName", "Hastings County Court And Family Court Hearing Centre");
        placeholdersMap.put("CourtAddress", "The Law Courts, Bohemia Road, Hastings, TN34 1QX");
        placeholdersMap.put("CourtPhone", "01634 887900");
        placeholdersMap.put("CourtEmail", "FRCKSS@justice.gov.uk");

        Map<String, Object> caseDataMap = Map.of(
            CASE_DATA, placeholdersMap,
            "id", 1234567890L);

        return Map.of(CASE_DETAILS, caseDataMap);
    }

    private Map<String, Object> expectedPlaceholdersMapLongerHearing() {
        Map<String, Object> placeholdersMap = new HashMap<>(Map.of(
            "CCDCaseNumber", 1234567890L,
            "DivorceCaseNumber", "AB01D23456",
            "ApplicantName", "Test Applicant",
            "RespondentName", "Name Respondent",
            "HearingType", "Final Hearing (FH)",
            "HearingVenue", "Nottingham County Court And Family Court",
            "HearingDate", "2021-01-01",
            "HearingLength", "1 hour",
            "HearingTime", "15:00",
            "AdditionalHearingDated", "notNull"));

        placeholdersMap.put("AnyOtherDirections", "N/A");
        placeholdersMap.put("CourtName", "Nottingham County Court And Family Court");
        placeholdersMap.put("CourtAddress", "60 Canal Street, Nottingham NG1 7EJ");
        placeholdersMap.put("CourtPhone", "0115 910 3504");
        placeholdersMap.put("CourtEmail", "FRCNottingham@justice.gov.uk");

        Map<String, Object> caseDataMap = Map.of(
            CASE_DATA, placeholdersMap,
            "id", 1234567890L);

        return Map.of(CASE_DETAILS, caseDataMap);
    }
}