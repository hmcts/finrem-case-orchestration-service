package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToStartHandlerTest {

    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;

    private ObjectMapper objectMapper;
    private InterimHearingHelper interimHearingHelper;

    public static final String AUTH_TOKEN = "tokien:)";

    private static final String CONTESTED_INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing.json";
    private static final String TEST_NEW_JSON = "/fixtures/contested/interim-hearing-with-no-existing-hearing.json";
    private static final String TEST_NEW_JSON_NO_UPLOADED_DOC = "/fixtures/contested/interim-hearing-nouploaded-doc.json";



    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        interimHearingHelper = new InterimHearingHelper(objectMapper);
        InterimHearingItemMapper interimHearingItemMapper = new InterimHearingItemMapper(interimHearingHelper);
        interimHearingContestedAboutToStartHandler  =
            new InterimHearingContestedAboutToStartHandler(interimHearingHelper, interimHearingItemMapper);
    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenCase_WhenInterimHearingPresent_ThenMigrateToInterimHearingCollection() {
        CallbackRequest callbackRequest = buildCallbackRequest(CONTESTED_INTERIM_HEARING_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> caseData = handle.getData();

        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);
        assertNotNull(interimHearingList);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            interimHearingHelper.getInterimHearingBulkPrintDocumentList(caseData);

        assertEquals(1, bulkPrintDocumentsList.size());

        List<InterimHearingCollectionItemData> trackingList = interimHearingHelper.getInterimHearingTrackingList(caseData);

        assertEquals(interimHearingList.get(0).getId(), trackingList.get(0).getValue().getIhItemIds());
    }

    @Test
    public void givenCase_WhenMigrateToInterimHearingCollectionButNoUploadedDocAndNoBulkPrintDoc_ThenItShouldMigratedSuccessfully() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON_NO_UPLOADED_DOC);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> caseData = handle.getData();

        final List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);
        assertNotNull(interimHearingList);

        final List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            interimHearingHelper.getInterimHearingBulkPrintDocumentList(caseData);
        assertEquals(1, bulkPrintDocumentsList.size());

        final List<InterimHearingCollectionItemData> trackingList = interimHearingHelper.getInterimHearingTrackingList(caseData);
        assertEquals(interimHearingList.get(0).getId(), trackingList.get(0).getValue().getIhItemIds());
    }

    @Test
    public void givenCase_WhenInterimHearingPresent_ThenNothingToMigrateInInterimHearingCollection() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> caseData = handle.getData();

        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);
        assertNotNull(interimHearingList);

        List<InterimHearingCollectionItemData> trackingList = interimHearingHelper.getInterimHearingTrackingList(caseData);

        assertThat(interimHearingList, is(Collections.emptyList()));
        assertThat(trackingList, is(Collections.emptyList()));
    }

    private CallbackRequest buildCallbackRequest(final String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}