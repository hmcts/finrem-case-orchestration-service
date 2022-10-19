package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
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
    @Mock
    private GeneralApplicationService service;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application.json";
    private static final String GA_UNSORTED_JSON = "/fixtures/contested/general-application-unsorted.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper);
        handler = new GeneralApplicationAboutToSubmitHandler(service, helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CREATE_GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CREATE_GENERAL_APPLICATION),
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
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CREATE_GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetCreatedBy() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(buildCaseDetailsWtihPath(GA_JSON))
                .caseDetailsBefore(buildCaseDetailsWtihPath(GA_JSON)).build();

        List<GeneralApplicationCollectionData> existingGeneralApplication = new ArrayList<>();
        existingGeneralApplication.add(migrateExistingGeneralApplication(callbackRequest.getCaseDetails().getData()));
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION, existingGeneralApplication);

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(callbackRequest.getCaseDetails().getData());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertEquals(1, generalApplicationList.size());
        assertExistingGeneralApplication(caseData);
    }

    @Test
    public void sortGeneralApplicationListByLatestDate() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(buildCaseDetailsWtihPath(GA_UNSORTED_JSON))
                .caseDetailsBefore(buildCaseDetailsWtihPath(GA_UNSORTED_JSON)).build();

        CaseDetails caseDetailsCopy = deepCopy(callbackRequest.getCaseDetails(), CaseDetails.class);
        List<GeneralApplicationCollectionData> unsortedList =
            helper.getGeneralApplicationList(caseDetailsCopy.getData());

        List<GeneralApplicationCollectionData> applicationCollectionDataList = unsortedList.stream()
            .sorted(helper::getCompareTo)
            .collect(Collectors.toList());

        Map<String, Object> data = caseDetailsCopy.getData();
        data.put(GENERAL_APPLICATION_COLLECTION, applicationCollectionDataList);

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(data);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

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

    private CaseDetails buildCaseDetailsWtihPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            CaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
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

    private GeneralApplicationItems getApplicationItems(Map<String, Object> caseData) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(Objects.toString(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM), null));
        builder.generalApplicationCreatedBy(Objects.toString(caseData.get(GENERAL_APPLICATION_CREATED_BY), null));
        builder.generalApplicationHearingRequired(Objects.toString(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED), null));
        builder.generalApplicationTimeEstimate(Objects.toString(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES), null));
        builder.generalApplicationDocument(helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)));
        CaseDocument draftDocument = helper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
        builder.generalApplicationCreatedDate(helper.objectToDateTime(caseData.get(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE)));
        builder.generalApplicationDraftOrder(draftDocument);
        return builder.build();
    }

    private <T> T deepCopy(T object, Class<T> objectClass) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), objectClass);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}