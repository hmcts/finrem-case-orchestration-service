package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingServiceTest {

    @InjectMocks
    private InterimHearingService interimHearingService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private InterimHearingHelper interimHearingHelper;
    @Mock
    private SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    @Spy
    private FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(JsonMapper
        .builder()
        .addModule(new JavaTimeModule())
        .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build());

    @Spy
    private final ObjectMapper objectMapper = JsonMapper
        .builder()
        .addModule(new JavaTimeModule())
        .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TWO_OLD_TWO_NEW_INTERIM_HEARING =
        "/fixtures/contested/interim-hearing-two-old-two-new-collections.json";
    private static final String ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON =
        "/fixtures/contested/interim-hearing-two-collection-modified.json";
    private static final String LEGACY_INTERIM_HEARING = "/fixtures/contested/interim-hearing-three-collection-no-track.json";

    @BeforeAll
    public void setUp() {
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenCallRealMethod();
    }

    @Test
    public void given2Old2NewInterimHearing1UploadedDoc_whenAddHearingNotices_then3BulkprintDocs() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(TWO_OLD_TWO_NEW_INTERIM_HEARING);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();

        caseDetails.getData().setIntervenerOne(intervenerOne);

        interimHearingService.addHearingNoticesToCase(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(FinremCaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class), any(), any());

        List<InterimHearingBulkPrintDocumentsData> interimHearingBulkPrintDocumentList =
            caseDetails.getData().getInterimWrapper().getInterimHearingDocuments();

        assertEquals(3, interimHearingBulkPrintDocumentList.size());
    }

    @Test
    public void given3NewInterimHearing1UploadedDoc_whenAddHearingNotices_then6BulkprintDocs() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(LEGACY_INTERIM_HEARING);


        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();

        caseDetails.getData().setIntervenerOne(intervenerOne);

        interimHearingService.addHearingNoticesToCase(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(FinremCaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class), any(), any());

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            caseDetails.getData().getInterimWrapper().getInterimHearingDocuments();

        assertEquals(6, bulkPrintDocumentsList.size());
    }


    @Test
    public void givenCoreLitigantNotSelectedForCorrespondenceShouldReturnWithErrorOnSubmitInterimHearing() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(TWO_OLD_TWO_NEW_INTERIM_HEARING);

        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();
        caseDetails.getData().setIntervenerOne(intervenerOne);

        when(selectablePartiesCorrespondenceService.validateApplicantAndRespondentCorrespondenceAreSelected(
            any(), anyString())).thenReturn(
            List.of(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));

        List<String> errors = interimHearingService.getValidationErrors(caseDetails.getData());

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));
    }

    @Test
    public void givenContestedMultipleHearing_WhenNoConsentToEmail_ThenItShouldSendAllToBulkPrint() {

        FinremCaseDetails caseDetails = buildFinremCaseDetails(LEGACY_INTERIM_HEARING);

        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();
        FinremCaseData data = caseDetails.getData();
        data.setIntervenerOne(intervenerOne);
        data.setPaperApplication(YesOrNo.NO);
        data.getContactDetailsWrapper().setApplicantSolicitorConsentForEmails(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerOne.class),
            any(FinremCaseDetails.class))).thenReturn(false);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());

        interimHearingService.addHearingNoticesToCase(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(any(FinremCaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(FinremCaseDetails.class), any(), any());
        verify(bulkPrintService).printIntervenerDocuments(any(IntervenerOne.class), any(FinremCaseDetails.class),
            any(), any());
    }

    @Test
    public void givenContestedCase_ThenItShouldSendNotification() {
        FinremCaseDetails caseDetails = buildFinremCaseDetails(LEGACY_INTERIM_HEARING);

        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();
        FinremCaseData data = caseDetails.getData();
        data.setIntervenerOne(intervenerOne);
        data.setPaperApplication(YesOrNo.NO);

        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(true);

        interimHearingService.sendNotification(caseDetails);

        verify(notificationService, never()).isIntervenerSolicitorDigitalAndEmailPopulated(
            any(), any(FinremCaseDetails.class));
        verify(notificationService, times(3))
            .isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(notificationService, times(3))
            .sendInterimHearingNotificationEmailToApplicantSolicitor(any(), anyMap());
        verify(notificationService, times(3))
            .sendInterimHearingNotificationEmailToRespondentSolicitor(any(), anyMap());
        verify(notificationService, never()).sendInterimHearingNotificationEmailToIntervenerSolicitor(any(), anyMap(),
            any(SolicitorCaseDataKeysWrapper.class));
    }

    @Test
    public void givenCaseWithIntervener_whenAddHearingNoticesToCase_theIntervenerCollectionPopulated() {

        FinremCaseDetails caseDetails = buildFinremCaseDetails(TWO_OLD_TWO_NEW_INTERIM_HEARING);

        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(), any(), any())).thenReturn(caseDocument());
        IntervenerOne intervenerOne = IntervenerOne.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();
        IntervenerTwo intervenerTwo = IntervenerTwo.builder()
            .intervenerCorrespondenceEnabled(Boolean.TRUE).build();

        FinremCaseData data = caseDetails.getData();
        data.setIntervenerOne(intervenerOne);
        data.setIntervenerTwo(intervenerTwo);

        interimHearingService.addHearingNoticesToCase(caseDetails, AUTH_TOKEN);

        assertThat(data.getIntv1HearingNoticesCollection().size(), is(3));
        assertThat(data.getIntv2HearingNoticesCollection().size(), is(3));
        assertThat(data.getIntv3HearingNoticesCollection(), is(nullValue()));
        assertThat(data.getIntv4HearingNoticesCollection(), is(nullValue()));
    }

    @Test
    public void givenCaseWithLegacyInterim_whenGetLegacyInterimAsInterimCollection_thenNewInterimCollection() {

        FinremCaseDetails caseDetails = buildFinremCaseDetails(LEGACY_INTERIM_HEARING);

        List<InterimHearingCollection> legacyCollection =
            interimHearingService.getLegacyInterimHearingAsInterimHearingCollection(caseDetails.getData());

        assertThat(legacyCollection.size(), is(1));
        assertThat(legacyCollection.get(0).getValue().getInterimHearingTime(), is("5 hour"));
        assertThat(legacyCollection.get(0).getValue().getInterimHearingTimeEstimate(), is("Test"));
    }

    protected FinremCaseDetails buildFinremCaseDetails(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return caseDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
