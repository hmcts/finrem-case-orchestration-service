package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGeneralApplicationStatusAboutToSubmitHandlerTest {

    private UpdateGeneralApplicationStatusAboutToSubmitHandler handler;
    @Mock
    private GenericDocumentService documentService;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    
    private GeneralApplicationHelper helper;
    @Mock
    private GeneralApplicationService service;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-details.json";
    private static final String GA_UNSORTED_JSON = "/fixtures/contested/general-application-details.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, documentService);
        handler = new UpdateGeneralApplicationStatusAboutToSubmitHandler(finremCaseDetailsMapper, service, helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
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
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
            is(false));
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetCreatedBy() {
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON))
                .caseDetailsBefore(buildCaseDetailsWithPath(GA_JSON)).build();

        List<GeneralApplicationCollectionData> existingGeneralApplication = new ArrayList<>();
        existingGeneralApplication.add(migrateExistingGeneralApplication(callbackRequest.getCaseDetails().getData()));
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(existingGeneralApplication));

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(callbackRequest.getCaseDetails().getData());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(
            caseData, GENERAL_APPLICATION_COLLECTION);
        assertEquals(1, generalApplicationList.size());
        assertExistingGeneralApplication(caseData);
    }

    @Test
    public void sortGeneralApplicationListByLatestDate() {
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_UNSORTED_JSON))
                .caseDetailsBefore(buildCaseDetailsWithPath(GA_UNSORTED_JSON)).build();

        FinremCaseDetails caseDetailsCopy = deepCopy(callbackRequest.getCaseDetails(), FinremCaseDetails.class);
        List<GeneralApplicationCollectionData> unsortedList =
            helper.getGeneralApplicationList(caseDetailsCopy.getData(), GENERAL_APPLICATION_COLLECTION);

        List<GeneralApplicationCollectionData> applicationCollectionDataList = unsortedList.stream()
            .sorted(helper::getCompareTo)
            .collect(Collectors.toList());

        FinremCaseData data = caseDetailsCopy.getData();
        data.getGeneralApplicationWrapper().setGeneralApplications(helper.convertToGeneralApplicationsCollection(applicationCollectionDataList));

        when(service.updateGeneralApplications(callbackRequest, AUTH_TOKEN)).thenReturn(data);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<GeneralApplicationCollectionData> generalApplicationList = helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        assertEquals(2, generalApplicationList.size());
    }

    private void assertExistingGeneralApplication(FinremCaseData caseData) {
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationHearingRequired());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationTimeEstimate());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationSpecialMeasures());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder());
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocumentDate());
    }

    private FinremCaseDetails buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GeneralApplicationCollectionData migrateExistingGeneralApplication(FinremCaseData caseData) {
        return GeneralApplicationCollectionData.builder()
            .id(UUID.randomUUID().toString())
            .generalApplicationItems(getApplicationItems(caseData))
            .build();
    }

    private GeneralApplicationItems getApplicationItems(FinremCaseData caseData) {
        GeneralApplicationItems.GeneralApplicationItemsBuilder builder =
            GeneralApplicationItems.builder();
        builder.generalApplicationReceivedFrom(Objects.toString(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom(), null));
        builder.generalApplicationCreatedBy(Objects.toString(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy(), null));
        builder.generalApplicationHearingRequired(Objects.toString(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationHearingRequired(), null));
        builder.generalApplicationTimeEstimate(Objects.toString(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationTimeEstimate(), null));
        builder.generalApplicationSpecialMeasures(Objects.toString(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationSpecialMeasures(), null));
        builder.generalApplicationDocument(helper.convertToCaseDocument(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument()));
        CaseDocument draftDocument = helper.convertToCaseDocument(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder());
        builder.generalApplicationCreatedDate(helper.objectToDateTime(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationLatestDocumentDate()));
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