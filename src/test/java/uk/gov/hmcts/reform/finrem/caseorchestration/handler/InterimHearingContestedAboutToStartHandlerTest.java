package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ALL_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TRACKING;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToStartHandlerTest {

    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String AUTH_TOKEN = "tokien:)";

    private static final String CONTESTED_INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing.json";
    private static final String TEST_NEW_JSON = "/fixtures/contested/interim-hearing-with-no-existing-hearing.json";


    @Before
    public void setup() {
        interimHearingContestedAboutToStartHandler  = new InterimHearingContestedAboutToStartHandler(objectMapper);
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
    public void handleWhenExistingInterimHearingPresent() {
        CallbackRequest callbackRequest = buildCallbackRequest(CONTESTED_INTERIM_HEARING_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        List<InterimHearingData> interimHearingList = Optional.ofNullable(handle.getData().get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(new ArrayList<>());
        assertNotNull(interimHearingList);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList = Optional.ofNullable(handle.getData().get(INTERIM_HEARING_ALL_DOCUMENT))
            .map(this::convertToBulkPrintDocumentDataList).orElse(new ArrayList<>());

        assertEquals(1, bulkPrintDocumentsList.size());

        List<InterimHearingCollectionItemData> interimHearingCollectionItemDataList = Optional.ofNullable(handle.getData()
                .get(INTERIM_HEARING_TRACKING))
            .map(this::convertToInterimHearingCollectionItemDataList).orElse(new ArrayList<>());

        assertEquals(interimHearingList.get(0).getId(), interimHearingCollectionItemDataList.get(0).getValue().getIhItemIds());
    }

    @Test
    public void handleWhenThereIsNoExistingInterimHearingPresent() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        List<InterimHearingData> interimHearingList = Optional.ofNullable(handle.getData().get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(new ArrayList<>());
        assertNotNull(interimHearingList);

        List<InterimHearingCollectionItemData> interimHearingCollectionItemDataList = Optional.ofNullable(handle.getData()
                .get(INTERIM_HEARING_TRACKING))
            .map(this::convertToInterimHearingCollectionItemDataList).orElse(new ArrayList<>());

        assertThat(interimHearingList, is(Collections.emptyList()));
        assertThat(interimHearingCollectionItemDataList, is(Collections.emptyList()));
    }

    private CallbackRequest buildCallbackRequest(final String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<InterimHearingBulkPrintDocumentsData> convertToBulkPrintDocumentDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<InterimHearingCollectionItemData> convertToInterimHearingCollectionItemDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}