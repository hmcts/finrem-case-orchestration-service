package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGeneralApplicationStatusAboutToStartHandlerTest extends BaseHandlerTestSetup {

    private UpdateGeneralApplicationStatusAboutToStartHandler handler;
    @Mock
    private GenericDocumentService service;
    @Mock
    private GeneralApplicationService generalApplicationService;
    private ObjectMapper objectMapper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    private AssignCaseAccessService assignCaseAccessService;
    private IdamService idamService;
    private GeneralApplicationHelper helper;
    private DocumentHelper documentHelper;
    public static final String AUTH_TOKEN = "tokien:)";
    private static final String GA_JSON = "/fixtures/contested/general-application-finrem.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        handler = new UpdateGeneralApplicationStatusAboutToStartHandler(
            finremCaseDetailsMapper, helper, generalApplicationService);
        generalApplicationService = new GeneralApplicationService(
            documentHelper, objectMapper, idamService, service, assignCaseAccessService, helper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
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
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.UPDATE_CONTESTED_GENERAL_APPLICATION),
            is(false));
    }

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    public DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    @Test
    public void givenCase_whenExistingGeneApp_thenSetcreatedBy() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON)).build();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().getGeneralApplications().forEach(ga -> ga.getValue()
            .setGeneralApplicationSender(buildDynamicIntervenerList()));
        data.getGeneralApplicationWrapper().setGeneralApplicationReceivedFrom(APPLICANT);
        data.getGeneralApplicationWrapper().getGeneralApplications().forEach(ga -> ga.getValue()
            .setGeneralApplicationSender(buildDynamicIntervenerList()));
        List<GeneralApplicationCollectionData> collection = helper.getGeneralApplicationList(data, GENERAL_APPLICATION_COLLECTION);
        generalApplicationService.updateGeneralApplicationCollectionData(collection, data);
        CaseDocument document = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        when(service.convertDocumentIfNotPdfAlready(any(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(document);
        GeneralApplicationCollectionData migratedData =
            helper.migrateExistingGeneralApplication(data, AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString());
        migratedData.getGeneralApplicationItems().setGeneralApplicationStatus(GeneralApplicationStatus.REFERRED.getId());
        collection.add(migratedData);
        generalApplicationService.updateGeneralApplicationCollectionData(collection, data);

        assertData(data.getGeneralApplicationWrapper().getGeneralApplications().get(1).getValue());
    }

    @Test
    public void givenContestedCase_whenExistingGeneAppAndDirectionGiven_thenMigrateToCollection() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(GA_JSON)).build();
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getGeneralApplicationWrapper().getGeneralApplications().forEach(ga -> ga.getValue()
            .setGeneralApplicationSender(buildDynamicIntervenerList()));
        data.getGeneralApplicationWrapper().setGeneralApplicationReceivedFrom(APPLICANT);
        data.getGeneralApplicationWrapper().setGeneralApplicationOutcome(GeneralApplicationOutcome.APPROVED);
        data.getGeneralApplicationWrapper().setGeneralApplicationDirectionsHearingRequired(YesOrNo.YES);
        List<GeneralApplicationCollectionData> collection = helper.getGeneralApplicationList(data, GENERAL_APPLICATION_COLLECTION);
        generalApplicationService.updateGeneralApplicationCollectionData(collection, data);
        CaseDocument document = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        when(service.convertDocumentIfNotPdfAlready(any(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(document);
        GeneralApplicationCollectionData migratedData =
            helper.migrateExistingGeneralApplication(data, AUTH_TOKEN, callbackRequest.getCaseDetails().getId().toString());
        migratedData.getGeneralApplicationItems().setGeneralApplicationStatus(GeneralApplicationStatus.REFERRED.getId());
        collection.add(migratedData);
        generalApplicationService.updateGeneralApplicationCollectionData(collection, data);

        assertData(data.getGeneralApplicationWrapper().getGeneralApplications().get(1).getValue());
    }

    private void assertData(GeneralApplicationItems generalApplicationItems) {
        assertEquals(APPLICANT,
            generalApplicationItems.getGeneralApplicationSender().getValue().getCode());
        assertEquals(APPLICANT,
            generalApplicationItems.getGeneralApplicationSender().getValue().getLabel());
        assertEquals("Claire Mumford", generalApplicationItems.getGeneralApplicationCreatedBy());
        assertEquals("NO", generalApplicationItems.getGeneralApplicationHearingRequired());
        assertEquals(GeneralApplicationStatus.REFERRED.getId(), generalApplicationItems.getGeneralApplicationStatus());

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

    private FinremCaseDetails buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
