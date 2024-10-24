package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;

@RunWith(MockitoJUnitRunner.class)
public class RejectGeneralApplicationSubmittedHandlerTest {

    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT = "Respondent";
    public static final String TEST_ID = "1fa411d2-3da3-468d-ad8d-3bfb2514203d";

    @InjectMocks
    private RejectGeneralApplicationSubmittedHandler submittedHandler;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaperNotificationService paperNotificationService;

    @Mock
    private GeneralApplicationHelper generalApplicationHelper;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;


    @Before
    public void setup() {
        callbackRequest = CallbackRequest.builder().build();
        caseDetails = CaseDetails.builder().build();
        caseDetails.setData(new HashMap<>());
        callbackRequest.setCaseDetails(caseDetails);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        submittedHandler = new RejectGeneralApplicationSubmittedHandler(finremCaseDetailsMapper,
            notificationService,
            paperNotificationService,
            objectMapper, generalApplicationHelper);
    }

    @Test
    public void givenValidCallBack_whenCanHandle_thenReturnTrue() {
        assertTrue(submittedHandler.canHandle(CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenInvalidCallBack_whenCanHandle_thenReturnFalse() {
        assertFalse(submittedHandler.canHandle(CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.REJECT_GENERAL_APPLICATION));
    }

    @Test
    public void givenApplicantSolicitorDigital_whenHandle_thenSendEmailToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(callbackRequest.getCaseDetails()))
            .thenReturn(true);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForApplicant()));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForApplicant()));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void givenRespondentSolicitorDigital_whenHandle_thenSendEmailToResSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(callbackRequest.getCaseDetails()))
            .thenReturn(true);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForRespondent()));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForRespondent()));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToResSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void givenIntervenerSolicitorDigital_whenHandle_thenSendEmailToIntervenerSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().getIntervenerOne().setIntervenerCorrespondenceEnabled(true);
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(true);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER1)));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER1)));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendGeneralApplicationRejectionEmailToIntervenerSolicitor(callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetails().getData().getIntervenerOne());
    }

    @Test
    public void givenApplicantSolicitorNotDigital_whenHandle_thenSendLetterToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(
            caseDetailsBefore(buildDynamicIntervenerListForApplicant()));
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForApplicant()));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForApplicant()));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printApplicantRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForApplicant()), AUTH_TOKEN);
    }

    @Test
    public void givenRespondentSolicitorNotDigital_whenHandle_thenSendLetterToResSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(
            caseDetailsBefore(buildDynamicIntervenerListForRespondent()));
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForRespondent()));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            buildDynamicIntervenerListForRespondent()));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printRespondentRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForRespondent()), AUTH_TOKEN);
    }

    @Test
    public void givenIntervenerSolicitorNotDigital_whenHandle_thenSendLetterToIntervenerSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        DynamicRadioList dynamicRadioList = buildDynamicListForIntervener(INTERVENER1);
        callbackRequest.getCaseDetails().getData().getIntervenerOne().setIntervenerCorrespondenceEnabled(true);
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(
            caseDetailsBefore(dynamicRadioList));
        when(notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(any(IntervenerWrapper.class),
            any(FinremCaseDetails.class))).thenReturn(false);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService).printIntervenerRejectionGeneralApplication(
            caseDetailsBefore(dynamicRadioList), callbackRequest.getCaseDetails().getData().getIntervenerOne(), AUTH_TOKEN);
    }

    @Test
    public void givenReceivedFromCase_whenHandle_thenDoNotSendNotifications() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());

        submittedHandler.handle(callbackRequest, AUTH_TOKEN);
        verify(paperNotificationService, never()).printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(paperNotificationService, never()).printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);

    }


    @Test
    public void givenApplicantSolicitorDigital_whenHandle_RejectApplication_thenSendEmailToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        DynamicRadioList dynamicRadioList = buildDynamicIntervenerListForApplicant();

        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(true);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendGeneralApplicationRejectionEmailToAppSolicitor(callbackRequest.getCaseDetails());
        verify(paperNotificationService, never()).printApplicantRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForApplicant()), AUTH_TOKEN);
    }

    @Test
    public void givenApplicantSolicitorNonDigital_whenHandle_RejectApplication_thenSendLetterToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        DynamicRadioList dynamicRadioList = buildDynamicIntervenerListForApplicant();

        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(
            caseDetailsBefore(dynamicRadioList));
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(paperNotificationService).printApplicantRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForApplicant()), AUTH_TOKEN);
        verify(notificationService, never()).sendGeneralApplicationRejectionEmailToAppSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    public void givenRespondentSolicitorDigital_whenHandle_RejectApplication_thenSendEmailToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        DynamicRadioList dynamicRadioList = buildDynamicIntervenerListForRespondent();

        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(true);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(notificationService).sendGeneralApplicationRejectionEmailToResSolicitor(callbackRequest.getCaseDetails());
        verify(paperNotificationService, never()).printApplicantRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForRespondent()), AUTH_TOKEN);
    }

    @Test
    public void givenRespondentSolicitorNonDigital_whenHandle_RejectApplication_thenSendLetterToAppSolicitor() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        DynamicRadioList dynamicRadioList = buildDynamicIntervenerListForRespondent();

        when(generalApplicationHelper.objectToDynamicList(any())).thenReturn(generalApplicationDynamicList());
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(
            caseDetailsBefore(dynamicRadioList));
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class))).thenReturn(false);
        GeneralApplicationWrapper wrapper = callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = callbackRequest.getCaseDetailsBefore().getData().getGeneralApplicationWrapper();
        wrapper.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        wrapperBefore.getGeneralApplications().forEach(ga -> ga.getValue().setGeneralApplicationSender(
            dynamicRadioList));
        submittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(paperNotificationService).printRespondentRejectionGeneralApplication(
            caseDetailsBefore(buildDynamicIntervenerListForRespondent()), AUTH_TOKEN);
        verify(notificationService, never()).sendGeneralApplicationRejectionEmailToResSolicitor(callbackRequest.getCaseDetails());
    }

    private DynamicList generalApplicationDynamicList() {
        return DynamicList.builder()
            .value(DynamicListElement.builder().code(TEST_ID).build())
            .build();
    }

    public DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    public DynamicRadioList buildDynamicIntervenerListForApplicant() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    public DynamicRadioList buildDynamicIntervenerListForRespondent() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    public DynamicRadioList buildDynamicListForIntervener(String role) {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(role, role));
        return DynamicRadioList.builder()
            .value(dynamicListElements.get(0))
            .listItems(dynamicListElements)
            .build();
    }

    private CaseDetails caseDetailsBefore(DynamicRadioList receivedFrom) {
        Map<String, Object> caseData = new HashMap<>();
        List<GeneralApplicationCollectionData> rejectedGeneralApplicationData = List.of(
            GeneralApplicationCollectionData.builder()
                .id(TEST_ID)
                .generalApplicationItems(GeneralApplicationItems.builder()
                    .generalApplicationSender(receivedFrom)
                    .build())
                .build()
        );
        caseData.put(GENERAL_APPLICATION_COLLECTION, rejectedGeneralApplicationData);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED.getCcdType())
            .id(12345L)
            .build();
        return caseDetails;
    }

    protected FinremCallbackRequest buildCallbackRequest() {
        GeneralApplicationItems generalApplicationItems =
            GeneralApplicationItems.builder().generalApplicationSender(
                buildDynamicIntervenerListForApplicant()).generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationHearingRequired("Yes").generalApplicationTimeEstimate("24 hours")
                .generalApplicationSpecialMeasures("Special measure").generalApplicationCreatedDate(
                    LocalDate.of(2022, 8, 2)).build();
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();
        GeneralApplicationsCollection intervener1GeneralApplications = GeneralApplicationsCollection.builder()
            .id(UUID.randomUUID()).value(generalApplicationItems).build();
        GeneralApplicationsCollection intervener2GeneralApplications = GeneralApplicationsCollection.builder()
            .id(UUID.randomUUID()).value(generalApplicationItems).build();
        GeneralApplicationsCollection intervener3GeneralApplications = GeneralApplicationsCollection.builder()
            .id(UUID.randomUUID()).value(generalApplicationItems).build();
        GeneralApplicationsCollection intervener4GeneralApplications = GeneralApplicationsCollection.builder()
            .id(UUID.randomUUID()).value(generalApplicationItems).build();
        intervener1GeneralApplications.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER1));
        intervener2GeneralApplications.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER2));
        intervener3GeneralApplications.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER3));
        intervener4GeneralApplications.getValue().setGeneralApplicationSender(
            buildDynamicListForIntervener(INTERVENER4));
        GeneralApplicationsCollection generalApplicationsBefore = GeneralApplicationsCollection.builder()
            .id(UUID.fromString("1fa411d2-3da3-468d-ad8d-3bfb2514203d")).build();
        GeneralApplicationItems generalApplicationItemsAdded =
            GeneralApplicationItems.builder().generalApplicationDocument(caseDocument).generalApplicationDraftOrder(caseDocument)
                .generalApplicationDirectionsDocument(caseDocument).generalApplicationSender(buildDynamicIntervenerListForApplicant())
                .generalApplicationCreatedBy("Claire Mumford")
                .generalApplicationStatus(String.valueOf(DIRECTION_APPROVED)).generalApplicationHearingRequired("No")
                .generalApplicationCreatedDate(LocalDate.now()).build();
        generalApplicationsBefore.setValue(generalApplicationItemsAdded);
        GeneralApplicationsCollection generalApplications = GeneralApplicationsCollection.builder().id(
            UUID.fromString("1fa411d2-3da3-468d-ad8d-3bfb2514203d")).value(generalApplicationItems).build();
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationDirectionsHearingRequired(YesOrNo.YES)
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationPreState("applicationIssued")
                .generalApplications(List.of(generalApplications, generalApplicationsBefore))
                .intervener1GeneralApplications(List.of(intervener1GeneralApplications))
                .intervener2GeneralApplications(List.of(intervener2GeneralApplications))
                .intervener3GeneralApplications(List.of(intervener3GeneralApplications))
                .intervener4GeneralApplications(List.of(intervener4GeneralApplications))
                .generalApplicationOutcome(GeneralApplicationOutcome.APPROVED)
                .build()).build();
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                .generalApplicationCreatedBy("Claire Mumford").generalApplicationOutcome(GeneralApplicationOutcome.APPROVED)
                .generalApplicationPreState("applicationIssued")
                .generalApplications(List.of(generalApplications))
                .intervener1GeneralApplications(List.of(intervener1GeneralApplications))
                .intervener2GeneralApplications(List.of(intervener2GeneralApplications))
                .intervener3GeneralApplications(List.of(intervener3GeneralApplications))
                .intervener4GeneralApplications(List.of(intervener4GeneralApplications))
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
