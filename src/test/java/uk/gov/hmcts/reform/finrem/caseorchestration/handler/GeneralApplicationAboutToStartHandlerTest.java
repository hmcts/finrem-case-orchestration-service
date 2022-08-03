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
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAboutToStartHandlerTest {

    private GeneralApplicationAboutToStartHandler handler;
    private ObjectMapper objectMapper;
    private GeneralApplicationHelper helper;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application.json";


    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper);
        handler  = new GeneralApplicationAboutToStartHandler(helper);
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
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse handle = handler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData);
        assertData(generalApplicationList.get(0).getGeneralApplicationItems());
        assertExistingGeneralApplication(caseData);
    }

    private void assertExistingGeneralApplication(Map<String, Object> caseData) {
        assertNull(caseData.get(GENERAL_APPLICATION_RECEIVED_FROM));
        assertNull(caseData.get(GENERAL_APPLICATION_CREATED_BY));
        assertNull(caseData.get(GENERAL_APPLICATION_HEARING_REQUIRED));
        assertNull(caseData.get(GENERAL_APPLICATION_TIME_ESTIMATE));
        assertNull(caseData.get(GENERAL_APPLICATION_SPECIAL_MEASURES));
        assertNull(caseData.get(GENERAL_APPLICATION_DOCUMENT));
        assertNull(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER));
    }

    private void assertData(GeneralApplicationItems generalApplicationItems) {
        assertEquals("applicant", generalApplicationItems.getGeneralApplicationReceivedFrom());
        assertEquals("Claire Mumford", generalApplicationItems.getGeneralApplicationCreatedBy());
        assertEquals("No", generalApplicationItems.getGeneralApplicationHearingRequired());
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
        assertNull(generalApplicationItems.getGeneralApplicationDraftOrder());
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(GA_JSON)) {
            CaseDetails caseDetails = objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}