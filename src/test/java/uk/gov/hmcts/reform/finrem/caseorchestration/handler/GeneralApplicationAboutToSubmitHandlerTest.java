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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToSubmitHandlerTest {

    private GeneralApplicationAboutToSubmitHandler handler;
    private ObjectMapper objectMapper;
    private GeneralApplicationHelper helper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application.json";
    private static final String GA_SORTED_JSON = "/fixtures/contested/general-application-sorted.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper);
        handler  = new GeneralApplicationAboutToSubmitHandler(helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetcreatedBy() {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(buildCaseDetails()).caseDetailsBefore(buildCaseDetails()).build();
        List<GeneralApplicationCollectionData> existingGeneralApplication = new ArrayList<>();
        existingGeneralApplication.add(migrateExistingGeneralApplication(callbackRequest.getCaseDetails().getData()));
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION,existingGeneralApplication);
        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertEquals(1, generalApplicationList.size());
        assertExistingGeneralApplication(caseData);
    }

    @Test
    public void sortGeneralApplicationListByLatestDate() {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(buildCaseDetailsWtihPath(GA_SORTED_JSON))
            .caseDetailsBefore(buildCaseDetailsWtihPath(GA_SORTED_JSON)).build();

        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertEquals(2, generalApplicationList.size());
    }

    private void assertExistingGeneralApplication(Map<String, Object> caseData) {
        assertNull(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM));
        assertNull(caseData.get(GENERAL_APPLICATION_CREATED_BY));
        assertNull(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED));
        assertNull(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE));
        assertNull(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES));
        assertNull(caseData.get(GENERAL_APPLICATION_DOCUMENT));
        assertNull(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        assertNull(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE));
    }

    private CaseDetails buildCaseDetails()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(GA_JSON)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CaseDetails buildCaseDetailsWtihPath(String path)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GeneralApplicationCollectionData migrateExistingGeneralApplication(Map<String, Object> caseData) {
        return GeneralApplicationCollectionData.builder()
            .id(UUID.randomUUID().toString())
            .generalApplicationItems(getApplicationItems(caseData))
            .build();
    }

    private GeneralApplicationItems getApplicationItems(Map<String,Object> caseData) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(helper.objectToString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM)));
        builder.generalApplicationCreatedBy("helper.objectToString(caseData.get(GENERAL_APPLICATION_CREATED_BY))");
        builder.generalApplicationHearingRequired(helper.objectToString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED)));
        builder.generalApplicationTimeEstimate(helper.objectToString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE)));
        builder.generalApplicationSpecialMeasures(helper.objectToString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES)));
        builder.generalApplicationDocument(helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)));
        CaseDocument draftDocument = helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        builder.generalApplicationCreatedDate(helper.objectToDateTime(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE)));
        builder.generalApplicationDraftOrder(draftDocument);
        return builder.build();
    }
}