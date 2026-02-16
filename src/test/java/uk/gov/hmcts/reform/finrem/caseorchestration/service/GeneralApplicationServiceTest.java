package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSuportingDocumentItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSupportingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationServiceTest {

    private static final String AUTH_TOKEN = "token";
    public static final String DOC_UPLOADED_URL = "http://dm-store/lhjbyuivu87y989hijbb";
    public static final String DOC_UPLOADED_BINARY_URL = "http://dm-store/lhjbyuivu87y989hijbb/binary";
    public static final String DOC_UPLOADED_NAME = "app_docs";
    public static final String WORD_FORMAT_EXTENSION = ".doc";
    public static final String PDF_FORMAT_EXTENSION = ".pdf";
    public static final String DOC_IN_EXISTING_COLLECTION_URL =
        "http://document-management-store:8080/documents/0abf044e-3d01-45eb-b792-c06d1e6344ee";
    public static final String USER_NAME = "Tony";

    @InjectMocks
    private GeneralApplicationService generalApplicationService;
    @Mock
    private IdamService idamService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private AssignCaseAccessService accessService;
    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;
    @Mock
    private GeneralApplicationsCategoriser generalApplicationsCategoriser;
    private GeneralApplicationHelper helper;
    private ObjectMapper objectMapper;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        caseDetails = CaseDetails.builder().data(new LinkedHashMap<>()).build();
        helper = new GeneralApplicationHelper(objectMapper, genericDocumentService);
        generalApplicationService = new GeneralApplicationService(documentHelper,
            idamService, genericDocumentService, accessService, helper, bulkPrintDocumentService,
            generalApplicationsCategoriser);
    }

    @Test
    void updateAndSortGeneralApplicationsForCaseworker() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Case");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection
            .builder().id(UUID.randomUUID()).build();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplications.setValue(generalApplicationItems);
        wrapper.setGeneralApplications(List.of(wrapperBefore.getGeneralApplications().get(0), generalApplications));
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(APPLICANT)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals(APPLICANT, caseData.getGeneralApplicationWrapper().getGeneralApplications().get(0)
            .getValue().getGeneralApplicationSender().getValue().getCode());
    }

    @Test
    void updateAndSortGeneralApplicationsForIntervener1() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener1");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener1GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener1GeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(INTERVENER1)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER1, caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications().get(0)
            .getValue().getGeneralApplicationSender().getValue().getCode());
    }

    @Test
    void updateAndSortGeneralApplicationsForIntervener2() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener2");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener2GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener2GeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(INTERVENER2)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER2, caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications().get(0)
            .getValue().getGeneralApplicationSender().getValue().getCode());
    }

    @Test
    void updateAndSortGeneralApplicationsForIntervener3() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener3");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener3GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener3GeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(INTERVENER3)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER3, caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications().get(0)
            .getValue().getGeneralApplicationSender().getValue().getCode());
    }

    @Test
    void updateAndSortGeneralApplicationsForIntervener4() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Intervener4");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.setIntervener4GeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.setGeneralApplications(wrapperBefore.getGeneralApplications());
        wrapper.getIntervener4GeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(INTERVENER4)));

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications());

        assertEquals(2, generalApplicationCollectionDataList.size());
        assertEquals(INTERVENER4, caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications().get(0)
            .getValue().getGeneralApplicationSender().getValue().getCode());
    }

    @Test
    void updateAndSortGeneralApplicationsForApplicant() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Applicant");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.setAppRespGeneralApplications(wrapper.getGeneralApplications());
        wrapper.setGeneralApplications(List.of(wrapper.getGeneralApplications().get(0)));
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();

        wrapper.getAppRespGeneralApplications().forEach(
            ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(APPLICANT));
        wrapperBefore.getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(APPLICANT)));
        wrapperBefore.setAppRespGeneralApplications(wrapperBefore.getGeneralApplications());

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals("applicant", caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
            .getValue().getAppRespGeneralApplicationReceivedFrom());
    }

    @Test
    void updateAndSortGeneralApplicationsForRespondent() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Respondent");
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.setAppRespGeneralApplications(wrapper.getGeneralApplications());
        wrapper.setGeneralApplications(List.of(wrapper.getGeneralApplications().get(0)));
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();

        wrapper.getAppRespGeneralApplications().forEach(
            ga -> ga.getValue().setAppRespGeneralApplicationReceivedFrom(RESPONDENT));
        wrapperBefore.getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(RESPONDENT)));
        wrapperBefore.setAppRespGeneralApplications(wrapperBefore.getGeneralApplications());

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        List<GeneralApplicationCollectionData> generalApplicationCollectionDataList
            = helper.covertToGeneralApplicationData(caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications());

        assertEquals(1, generalApplicationCollectionDataList.size());
        assertEquals("respondent", caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications().get(0)
            .getValue().getAppRespGeneralApplicationReceivedFrom());
    }

    @Test
    void givenGeneralApplicationWithPreviousData_whenUpdateCaseDataStart_thenPreviousDataDeleted() {

        String[] generalAppParameters = {GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT};
        Stream.of(generalAppParameters).forEach(ccdFieldName -> caseDetails.getData().put(ccdFieldName, "Something"));

        generalApplicationService.updateCaseDataStart(caseDetails.getData(), AUTH_TOKEN);

        Stream.of(generalAppParameters)
            .forEach(ccdFieldName -> assertThat(caseDetails.getData().get(ccdFieldName)).isNull());
    }

    @Test
    void givenGeneralApplication_whenUpdateCaseDataStart_thenCreatedByIsCurrentLoggedUser() {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn(USER_NAME);
        generalApplicationService.updateCaseDataStart(caseDetails.getData(), AUTH_TOKEN);

        assertThat(caseDetails.getData()).containsEntry(GENERAL_APPLICATION_CREATED_BY, USER_NAME);
    }

    @Test
    void givenGeneralApplication_shouldGetInterimGeneralApplicationList() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection
            .builder().id(UUID.randomUUID()).build();
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplications.setValue(generalApplicationItems);
        caseData.getGeneralApplicationWrapper().setGeneralApplications(List.of(
            caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications().get(0), generalApplications));
        caseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(ga -> ga.getValue()
            .setGeneralApplicationSender(buildDynamicList(INTERVENER1)));

        List<GeneralApplicationCollectionData> col =
            generalApplicationService.getInterimGeneralApplicationList(
                GENERAL_APPLICATION_COLLECTION, caseData, caseDataBefore);
        List<GeneralApplicationItems> itemsActual = new ArrayList<>();

        col.forEach(x -> itemsActual.add(x.getGeneralApplicationItems()));

        assertEquals("No", itemsActual.get(0).getGeneralApplicationHearingRequired());
        assertEquals("Special measure", itemsActual.get(0).getGeneralApplicationSpecialMeasures());
        assertEquals("48 hours", itemsActual.get(0).getGeneralApplicationTimeEstimate());
        assertEquals("Claire Mumford", itemsActual.get(0).getGeneralApplicationCreatedBy());
        assertEquals("Intervener1", itemsActual.get(0).getGeneralApplicationSender().getValue()
            .getCode());

    }

    @Test
    void givenGeneralApplicationAndNoExistingIntervenerDirectionsOrder_ShouldUpdateIntervenerDirectionsOrder() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours").generalApplicationDirectionsDocument(caseDocument)
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        generalApplicationService.updateIntervenerDirectionsOrders(generalApplicationItems, callbackRequest.getCaseDetails());
        assertEquals(caseDocument, wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationDirectionsDocument());
        assertEquals("No", wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationHearingRequired());
        assertEquals("48 hours", wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationTimeEstimate());
        assertEquals(1, wrapper.getGeneralApplicationIntvrOrders().size());
    }

    @Test
    void givenGeneralApplicationAndExistingIntervenerDirectionsDocument_ShouldUpdateIntervenerDirectionsDocuments() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        CaseDocument caseDocument = getCaseDocument(PDF_FORMAT_EXTENSION);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);
        GeneralApplicationItems generalApplicationItems1 =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours").generalApplicationDirectionsDocument(caseDocument)
                .generalApplicationSpecialMeasures("Special measure").build();
        GeneralApplicationItems generalApplicationItems2 =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Papadale")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("72 hours").generalApplicationDirectionsDocument(caseDocument)
                .generalApplicationSpecialMeasures("There will be special measures").build();
        wrapper.setGeneralApplicationIntvrOrders(List.of(
            GeneralApplicationsCollection.builder().id(UUID.randomUUID()).value(generalApplicationItems1).build()));
        generalApplicationService.updateIntervenerDirectionsOrders(generalApplicationItems2, callbackRequest.getCaseDetails());
        assertEquals(caseDocument, wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationDirectionsDocument());
        assertEquals(caseDocument, wrapper.getGeneralApplicationIntvrOrders().get(1).getValue().getGeneralApplicationDirectionsDocument());
        assertEquals("Claire Mumford", wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationCreatedBy());
        assertEquals("Claire Papadale", wrapper.getGeneralApplicationIntvrOrders().get(1).getValue().getGeneralApplicationCreatedBy());
        assertEquals("48 hours", wrapper.getGeneralApplicationIntvrOrders().get(0).getValue().getGeneralApplicationTimeEstimate());
        assertEquals("72 hours", wrapper.getGeneralApplicationIntvrOrders().get(1).getValue().getGeneralApplicationTimeEstimate());
        assertEquals(2, wrapper.getGeneralApplicationIntvrOrders().size());
    }

    @Test
    void givenNonWordOrPdfDocument_whenUpdateGeneralApplication_thenDoNotConvertSupportingDocToPdf() {
        String otherFilenameExtension = ".anyother";
        CaseDocument caseDocument = getCaseDocument(otherFilenameExtension);
        caseDocument.setDocumentUrl(DOC_IN_EXISTING_COLLECTION_URL);

        GeneralApplicationSupportingDocumentData generalApplicationSupportingDocumentData =
            GeneralApplicationSupportingDocumentData.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .value(GeneralApplicationSuportingDocumentItems.builder()
                    .supportDocument(caseDocument)
                    .build())
                .build();
        List<GeneralApplicationSupportingDocumentData> gaSupportDocuments = List.of(generalApplicationSupportingDocumentData);

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUser(any(), any())).thenReturn("Applicant");

        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().get(0).getValue().setGaSupportDocuments(gaSupportDocuments);
        wrapper.getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicList(APPLICANT)));
        callbackRequest.getCaseDetails().getData().setGeneralApplicationWrapper(wrapper);

        FinremCaseData caseData = generalApplicationService.updateGeneralApplications(callbackRequest, AUTH_TOKEN);

        CaseDocument gaSupportingDocument = caseData.getGeneralApplicationWrapper()
            .getGeneralApplications().get(1).getValue()
            .getGaSupportDocuments().get(0).getValue().getSupportDocument();
        assertThat(gaSupportingDocument.getDocumentFilename()).doesNotContain(PDF_FORMAT_EXTENSION);    }

    @Test
    void givenEmptyGeneralApplications_whenCheckIfApplicationCompleted_thenAddsError() {
        // Given
        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(
                Long.valueOf(CASE_ID), mock(CaseType.class))
            .build();
        List<String> errors = new ArrayList<>();

        // When
        generalApplicationService.checkIfApplicationCompleted(finremCaseDetails, errors, List.of(), List.of(), AUTH_TOKEN);

        // Then
        assertThat(errors).containsExactly(
            "Please complete the General Application. No information has been entered for this application."
        );
        verifyNoInteractions(bulkPrintDocumentService);
    }

    @Test
    void givenNoGeneralApplicationsBefore_whenCheckIfApplicationCompleted_thenValidatesAllCurrentGAs() {
        // Given
        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(
                Long.valueOf(CASE_ID), mock(CaseType.class))
            .build();
        List<String> errors = new ArrayList<>();

        List<GeneralApplicationsCollection> generalApplicationsCollection = List.of(GeneralApplicationsCollection
            .builder()
            .value(GeneralApplicationItems
                .builder()
                .generalApplicationDocument(caseDocument("generalApplicationDocument.pdf"))
                .generalApplicationDraftOrder(caseDocument("generalApplicationDraftOrder.docx"))
                .gaSupportDocuments(List.of(GeneralApplicationSupportingDocumentData
                    .builder()
                    .value(GeneralApplicationSuportingDocumentItems
                        .builder()
                        .supportDocument(caseDocument("generalApplicationSupportingDocument.docx"))
                        .build())
                    .build()))
                .build())
            .build());

        finremCaseDetails.getData().getGeneralApplicationWrapper()
            .setGeneralApplications(generalApplicationsCollection);

        // When
        generalApplicationService.checkIfApplicationCompleted(finremCaseDetails, errors, generalApplicationsCollection,
            null, AUTH_TOKEN);

        // Then
        verify(bulkPrintDocumentService, times(3))
            .validateEncryptionOnUploadedDocument(any(CaseDocument.class),
                eq(CASE_ID), eq(errors), eq(AUTH_TOKEN));
    }

    @Test
    void givenChangedExistingGaAndAdditionalSupportDoc_whenCheckIfApplicationCompleted_thenValidatesChangedGaDocsOnly() {
        // Given
        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(
                Long.valueOf(CASE_ID), mock(CaseType.class))
            .build();
        List<String> errors = new ArrayList<>();

        UUID gaId = UUID.randomUUID();

        // BEFORE: ga + draft + 1 support doc
        List<GeneralApplicationsCollection> generalApplicationsBefore = List.of(
            GeneralApplicationsCollection.builder()
                .id(gaId)
                .value(GeneralApplicationItems.builder()
                    .generalApplicationDocument(caseDocument("generalApplicationDocument.pdf")) // same
                    .generalApplicationDraftOrder(caseDocument("generalApplicationDraftOrder.docx")) // will change
                    .gaSupportDocuments(List.of(
                        GeneralApplicationSupportingDocumentData.builder()
                            .value(GeneralApplicationSuportingDocumentItems.builder()
                                .supportDocument(caseDocument("generalApplicationSupportingDocument-1.docx")) // existing
                                .build())
                            .build()
                    ))
                    .build())
                .build()
        );

        // CURRENT: draft order replaced + extra support doc added (so id changes)
        List<GeneralApplicationsCollection> generalApplicationsCurrent = List.of(
            GeneralApplicationsCollection.builder()
                .id(gaId)
                .value(GeneralApplicationItems.builder()
                    .generalApplicationDocument(caseDocument("generalApplicationDocument.pdf")) // unchanged
                    .generalApplicationDraftOrder(caseDocument("generalApplicationDraftOrder-UPDATED.docx")) // changed
                    .gaSupportDocuments(List.of(
                        GeneralApplicationSupportingDocumentData.builder()
                            .value(GeneralApplicationSuportingDocumentItems.builder()
                                .supportDocument(caseDocument("generalApplicationSupportingDocument-1.docx")) // existing
                                .build())
                            .build(),
                        GeneralApplicationSupportingDocumentData.builder()
                            .value(GeneralApplicationSuportingDocumentItems.builder()
                                .supportDocument(caseDocument("generalApplicationSupportingDocument-2-NEW.docx")) // new
                                .build())
                            .build()
                    ))
                    .build())
                .build()
        );

        finremCaseDetails.getData().getGeneralApplicationWrapper()
            .setGeneralApplications(generalApplicationsCurrent);

        // When
        generalApplicationService.checkIfApplicationCompleted(finremCaseDetails, errors,
            generalApplicationsCurrent, generalApplicationsBefore, AUTH_TOKEN);

        // Then
        ArgumentCaptor<CaseDocument> captor = ArgumentCaptor.forClass(CaseDocument.class);
        verify(bulkPrintDocumentService, times(4))
            .validateEncryptionOnUploadedDocument(captor.capture(), eq(CASE_ID), eq(errors), eq(AUTH_TOKEN));

        assertThat(captor.getAllValues())
            .extracting(CaseDocument::getDocumentFilename)
            .containsExactlyInAnyOrder(
                "generalApplicationDocument.pdf",
                "generalApplicationDraftOrder-UPDATED.docx",
                "generalApplicationSupportingDocument-1.docx",
                "generalApplicationSupportingDocument-2-NEW.docx"
            );
    }

    private DynamicRadioList buildDynamicList(String role) {

        List<DynamicRadioListElement> dynamicListElements = List.of(
            getDynamicListElement(role, role)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    private DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private CaseDocument getCaseDocument(String documentFormat) {
        return CaseDocument.builder()
            .documentUrl(DOC_UPLOADED_URL)
            .documentBinaryUrl(DOC_UPLOADED_BINARY_URL)
            .documentFilename(DOC_UPLOADED_NAME + documentFormat)
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2)).build();
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().build();
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder().build();
        generalApplications.setValue(generalApplicationItemsAdded);
        generalApplicationsBefore.setId(UUID.randomUUID());
        generalApplications.setId(UUID.randomUUID());
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("No").generalApplicationTimeEstimate("48 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplicationsBefore.setValue(generalApplicationItems);
        List<GeneralApplicationsCollection> generalApplicationsCollection = new ArrayList<>();
        List<GeneralApplicationsCollection> generalApplicationsCollectionBefore = new ArrayList<>();
        generalApplicationsCollectionBefore.add(generalApplications);
        generalApplicationsCollection.add(generalApplications);
        generalApplicationsCollection.add(generalApplicationsBefore);
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollection)
                .build()).build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(generalApplicationsCollectionBefore)
                .build()).build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(12345L)
            .state(State.CASE_ADDED)
            .data(caseData)
            .build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .id(12345L)
            .state(State.CASE_ADDED)
            .data(caseDataBefore)
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
    }
}
