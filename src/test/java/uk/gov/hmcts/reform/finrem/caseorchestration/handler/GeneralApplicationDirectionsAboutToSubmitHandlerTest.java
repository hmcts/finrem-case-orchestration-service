package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationDirectionsAboutToSubmitHandlerTest {

    private GeneralApplicationDirectionsAboutToStartHandler startHandler;
    private GeneralApplicationDirectionsAboutToSubmitHandler submitHandler;
    private GeneralApplicationHelper helper;
    private final CaseDocument caseDocument = TestSetUpUtils.caseDocument();
    @Mock
    private GeneralApplicationDirectionsService service;
    @Mock
    private GenericDocumentService documentService;
    private ObjectMapper objectMapper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-direction.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, documentService);
        startHandler  = new GeneralApplicationDirectionsAboutToStartHandler(helper, service);
        submitHandler  = new GeneralApplicationDirectionsAboutToSubmitHandler(helper, service);

        when(documentService.convertDocumentIfNotPdfAlready(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            CaseDocument.builder().documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary")
                .documentFilename("InterimHearingNotice.pdf")
                .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e").build()
        );
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigEventTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCase_whenInCorrectConfigCallbackTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_DIRECTIONS),
            is(false));
    }

    @Test
    public void givenCase_whenExistingApplication_thenMigratedAndUpdateStatusApprovedCompleted() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_NON_COLL_JSON);
        when(service.getBulkPrintDocument(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument);
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_OUTCOME_DECISION, APPROVED.getId());

        AboutToStartOrSubmitCallbackResponse startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        AboutToStartOrSubmitCallbackResponse submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(1, list.size());

        assertEquals(DIRECTION_APPROVED.getId(),
            list.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
        assertNull(data.get(GENERAL_APPLICATION_OUTCOME_DECISION));
    }

    @Test
    public void givenCase_whenApproveAnApplication_thenUpdateStatusApprovedCompleted() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        when(service.getBulkPrintDocument(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument);
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData());
        List<GeneralApplicationCollectionData> updatedList
            = existingList.stream().map(obj -> updateStatus(obj, APPROVED)).toList();
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION, updatedList);

        AboutToStartOrSubmitCallbackResponse startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        AboutToStartOrSubmitCallbackResponse submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, list.size());

        assertEquals(DIRECTION_APPROVED.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));

        verify(service).submitCollectionGeneralApplicationDirections(any(), any(), any());
    }


    @Test
    public void givenCase_whenNotApproveAnApplication_thenUpdateStatusNotApproved() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        when(service.getBulkPrintDocument(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument);
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData());
        List<GeneralApplicationCollectionData> updatedList
            = existingList.stream().map(obj -> updateStatus(obj, NOT_APPROVED)).toList();
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION, updatedList);

        AboutToStartOrSubmitCallbackResponse startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        AboutToStartOrSubmitCallbackResponse submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, list.size());

        assertEquals(DIRECTION_NOT_APPROVED.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
    }

    @Test
    public void givenCase_whenOtherAnApplication_thenUpdateStatusOther() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        when(service.getBulkPrintDocument(callbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument);
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(callbackRequest.getCaseDetails().getData());
        List<GeneralApplicationCollectionData> updatedList
            = existingList.stream().map(obj -> updateStatus(obj, OTHER)).toList();
        callbackRequest.getCaseDetails().getData().put(GENERAL_APPLICATION_COLLECTION, updatedList);

        AboutToStartOrSubmitCallbackResponse startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertEquals(1, dynamicList.getListItems().size());

        AboutToStartOrSubmitCallbackResponse submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        Map<String, Object> data = submitHandle.getData();

        List<GeneralApplicationCollectionData> list
            = helper.covertToGeneralApplicationData(data.get(GENERAL_APPLICATION_COLLECTION));
        assertEquals(2, list.size());

        assertEquals(DIRECTION_OTHER.getId(),
            list.get(1).getGeneralApplicationItems().getGeneralApplicationStatus());
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_LIST));
        assertNull(data.get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));
    }

    private GeneralApplicationCollectionData updateStatus(GeneralApplicationCollectionData obj, GeneralApplicationStatus status) {
        if (obj.getId().equals("b0bfb0af-4f07-4628-a677-1de904b6ea1c")) {
            obj.getGeneralApplicationItems().setGeneralApplicationStatus(status.getId());
        }
        return obj;
    }

    private CallbackRequest buildCallbackRequest(String testJson)  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}