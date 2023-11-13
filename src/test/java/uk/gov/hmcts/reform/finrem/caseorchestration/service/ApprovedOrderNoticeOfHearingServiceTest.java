package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

public class ApprovedOrderNoticeOfHearingServiceTest extends BaseServiceTest {

    private static final String LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL = "http://dm-store/1frea-ldo-doc/binary";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL = "http://dm-store/1f3a-gads-doc/binary";
    static final String CASE_DATA = "case_data";
    static final String CASE_DETAILS = "caseDetails";


    @Autowired
    private ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DocumentConfiguration documentConfiguration;
    @Autowired
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private DocumentHelper documentHelper;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;
    @Captor
    ArgumentCaptor<List<BulkPrintDocument>> printDocumentsRequestDocumentListCaptor;

    private CaseDetails caseDetails;

    @Before
    public void setup() {
        caseDetails = caseDetailsFromResource("/fixtures/general-application-directions.json", objectMapper);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any(), any()))
            .thenReturn(caseDocument(DOC_URL, FILE_NAME, GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL));
    }

    @Test
    public void givenHearingRequired_whenSubmitNoticeOfHearing_thenHearingNoticeIsPrintedForContestedCase() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any(), any()))
            .thenReturn(caseDocument(DOC_URL, FILE_NAME, BINARY_URL));
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();


        data.getContactDetailsWrapper().setApplicantFmName("Poor");
        data.getContactDetailsWrapper().setApplicantLname("Poor");
        data.getContactDetailsWrapper().setAppRespondentFmName("Poor2");
        data.getContactDetailsWrapper().setAppRespondentFmName("Poor2");

        HearingDirectionDetail detail = HearingDirectionDetail.builder()
            .dateOfHearing(LocalDate.of(2023, 1, 1))
            .hearingTime("1200")
            .isAnotherHearingYN(YesOrNo.YES)
            .typeOfHearing(HearingTypeDirection.FH)
            .timeEstimate("24hours")
            .localCourt(Court.builder()
                .region(Region.LONDON)
                .londonList(RegionLondonFrc.LONDON)
                .courtListWrapper(DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                    .build()).build())
            .build();

        HearingDirectionDetailsCollection directionDetailsCollection = HearingDirectionDetailsCollection.builder().value(detail).build();
        List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollection = new ArrayList<>();
        hearingDirectionDetailsCollection.add(directionDetailsCollection);
        data.setHearingDirectionDetailsCollection(hearingDirectionDetailsCollection);

        approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, AUTH_TOKEN);

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = data.getAdditionalHearingDocuments();
        assertEquals("Not null", caseDocument(), additionalHearingDocuments.get(0).getValue().getDocument());

        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(
            eq(AUTH_TOKEN),
            placeholdersMapCaptor.capture(),
            eq(documentConfiguration.getAdditionalHearingTemplate()),
            eq(documentConfiguration.getAdditionalHearingFileName()), eq("123"));

        Map<String, Object> caseDetailsMap = convertToMap(placeholdersMapCaptor.getValue().get(CASE_DETAILS));
        Map<String, Object> data2 = convertToMap(caseDetailsMap.get(CASE_DATA));
        assertThat(data2, allOf(
            Matchers.<String, Object>hasEntry("CCDCaseNumber", 123L),
            Matchers.hasEntry("CourtAddress", "Bromley County Court, College Road, Bromley, BR1 3PX"),
            Matchers.hasEntry("CourtPhone", "0208 290 9620"),
            Matchers.hasEntry("CourtEmail", "family.bromley.countycourt@justice.gov.uk"),
            Matchers.hasEntry("ApplicantName", "Poor Poor"),
            Matchers.hasEntry("AdditionalHearingDated", formattedNowDate),
            Matchers.hasEntry("HearingTime", "1200"),
            Matchers.hasEntry("RespondentName", "Poor2"),
            Matchers.hasEntry("HearingVenue",
                "Bromley County Court And Family Court, Bromley County Court, College Road, Bromley, BR1 3PX")
            ));
    }

    private Map<String, Object> convertToMap(Object object) {
        return objectMapper.convertValue(object,  new TypeReference<>() {});
    }

    @Test
    public void givenSubmittedCallbackReceived_whenSubmitNotice_thenSendNoticeOfHearingToAppAndResp() {
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, buildHearingNoticePack());
        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString())).thenReturn(false);
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)).thenReturn(true);
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(any())).thenReturn(List.of(BulkPrintDocument
                .builder()
                .binaryFileUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build(),
            BulkPrintDocument
                .builder()
                .binaryFileUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()));
        approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, AUTH_TOKEN);

        assertBulkPrintServiceInteraction();

        assertThat(printDocumentsRequestDocumentListCaptor.getValue(), containsInAnyOrder(BulkPrintDocument
                .builder()
                .binaryFileUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build(),
            BulkPrintDocument
                .builder()
                .binaryFileUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build())
        );
    }

    @Test
    public void givenSubmittedCallbackReceived_whenSubmitNotice_thenSendNoticeOfHearingEmailToAppAndResp() {
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, buildHearingNoticePack());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        approvedOrderNoticeOfHearingService.printHearingNoticePackAndSendToApplicantAndRespondent(caseDetails, AUTH_TOKEN);

        assertNotificationServiceInteraction();
    }

    private void assertBulkPrintServiceInteraction() {
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(CaseDetails.class), eq(AUTH_TOKEN), any());
        verify(bulkPrintService, times(1)).printRespondentDocuments(any(CaseDetails.class), eq(AUTH_TOKEN),
            printDocumentsRequestDocumentListCaptor.capture());
    }

    private void assertNotificationServiceInteraction() {
        verify(notificationService, times(1)).sendPrepareForHearingEmailApplicant(caseDetails);
        verify(notificationService, times(1)).sendPrepareForHearingEmailRespondent(caseDetails);
    }

    private List<Element<CaseDocument>> buildHearingNoticePack() {
        return List.of(element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT_BIN_URL)
                .build()),
            element(UUID.randomUUID(), CaseDocument.builder()
                .documentBinaryUrl(LATEST_DRAFT_ORDER_DOCUMENT_BIN_URL)
                .build()));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.DIRECTION_UPLOAD_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).state(State.APPLICATION_ISSUED).build())
            .build();
    }
}
