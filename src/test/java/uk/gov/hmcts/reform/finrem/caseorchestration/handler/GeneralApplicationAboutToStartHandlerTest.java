package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_OUTCOME_DECISION;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToStartHandlerTest extends BaseHandlerTest {

    private GeneralApplicationAboutToStartHandler handler;
    @Mock
    private GenericDocumentService service;
    private ObjectMapper objectMapper;
    private GeneralApplicationHelper helper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new GeneralApplicationAboutToStartHandler(helper);
        when(service.convertDocumentIfNotPdfAlready(ArgumentMatchers.any(), ArgumentMatchers.any(), anyString()))
            .thenReturn(
            CaseDocument.builder()
                .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary")
                .documentFilename("InterimHearingNotice.pdf")
                .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e").build());
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetcreatedBy() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertData(caseData, generalApplicationList.get(0).getGeneralApplicationItems());
    }

    @Test
    public void givenContestedCase_whenExistingGeneAppAndDirectionGiven_thenMigrateToCollection() {
        CallbackRequest callbackRequest = buildCallbackRequest(GA_JSON);
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(GENERAL_APPLICATION_OUTCOME_DECISION, GeneralApplicationStatus.APPROVED.getId());
        data.put(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED, YES_VALUE);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertData(data, generalApplicationList.get(0).getGeneralApplicationItems());
    }

    private void assertData(Map<String, Object> caseData, GeneralApplicationItems generalApplicationItems) {
        assertEquals("applicant", generalApplicationItems.getGeneralApplicationReceivedFrom());
        assertEquals("Claire Mumford", generalApplicationItems.getGeneralApplicationCreatedBy());
        assertEquals("No", generalApplicationItems.getGeneralApplicationHearingRequired());
        String directionGiven = Objects.toString(caseData.get(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED), null);
        assertEquals(directionGiven == null
                ? GeneralApplicationStatus.CREATED.getId() : GeneralApplicationStatus.DIRECTION_APPROVED.getId(),
            generalApplicationItems.getGeneralApplicationStatus());
        System.out.println(generalApplicationItems.getGeneralApplicationStatus());
        assertNull(generalApplicationItems.getGeneralApplicationTimeEstimate());
        assertNull(generalApplicationItems.getGeneralApplicationSpecialMeasures());
        CaseDocument generalApplicationDocument = generalApplicationItems.getGeneralApplicationDocument();
        assertNotNull(generalApplicationDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDocument.getDocumentBinaryUrl());
        CaseDocument generalApplicationDraftOrderDocument = generalApplicationItems.getGeneralApplicationDocument();
        assertNotNull(generalApplicationDraftOrderDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDraftOrderDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDraftOrderDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDraftOrderDocument.getDocumentBinaryUrl());
        CaseDocument generalApplicationDirectionOrderDocument = generalApplicationItems.getGeneralApplicationDirectionsDocument();
        assertNotNull(generalApplicationDirectionOrderDocument);
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e",
            generalApplicationDirectionOrderDocument.getDocumentUrl());
        assertEquals("InterimHearingNotice.pdf",
            generalApplicationDirectionOrderDocument.getDocumentFilename());
        assertEquals("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary",
            generalApplicationDirectionOrderDocument.getDocumentBinaryUrl());
    }

}