package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
@Service
public class GeneralApplicationReferToJudgeAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    private GeneralApplicationReferToJudgeAboutToStartHandler startHandler;
    private GeneralApplicationReferToJudgeAboutToSubmitHandler submitHandler;
    @Mock
    private GenericDocumentService service;
    private GeneralApplicationService gaService;
    @Mock
    private GeneralApplicationHelper helper;
    @Mock
    private ObjectMapper objectMapper;
    private DocumentHelper documentHelper;
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    private IdamService idamService;
    private AssignCaseAccessService accessService;

    public static final String AUTH_TOKEN = "tokien:)";
    private static final String NO_GA_JSON = "/fixtures/contested/no-general-application-finrem.json";
    private static final String GA_JSON = "/fixtures/contested/general-application-details.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, service);
        gaService = new GeneralApplicationService(documentHelper, objectMapper, idamService, service, accessService, helper);
        startHandler = new GeneralApplicationReferToJudgeAboutToStartHandler(finremCaseDetailsMapper, helper, gaService);
        submitHandler = new GeneralApplicationReferToJudgeAboutToSubmitHandler(finremCaseDetailsMapper, helper, gaService);
    }

    @Test
    public void givenCase_whenCorrectConfigSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
            is(true));
    }

    @Test
    public void givenCase_whenInCorrectConfigCaseTypeSupplied_thenHandlerCanHandle() {
        assertThat(submitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
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
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.GENERAL_APPLICATION_REFER_TO_JUDGE),
            is(false));
    }

    @Test
    public void givenCase_whenRejectingAnApplication_thenRemoveElementFromCollection() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications().forEach(
            ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle =
            startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationReferList());
        assertEquals(2, dynamicList.getListItems().size());

        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications().forEach(x ->
            x.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle =
            submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(2, generalApplicationCollectionData.size());
        assertEquals(GeneralApplicationStatus.REFERRED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
    }

    @Test
    public void givenContestedCase_whenNonCollectionGeneralApplicationExistAndAlreadyReferred_thenReturnError() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(NO_GA_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferList());
        assertNull(dynamicList);
        assertNull(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferList());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(submitHandle.getErrors(), CoreMatchers.hasItem("There is no general application available to refer."));
    }

    @Test
    public void givenCase_whenRejectingAnExistinNonCollApplication_thenRemoveGeneralApplicationData() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_NON_COLL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> startHandle = startHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = startHandle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(caseData.getGeneralApplicationWrapper().getGeneralApplicationReferList());
        assertEquals(1, dynamicList.getListItems().size());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submitHandle = submitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData data = submitHandle.getData();
        assertExistingGeneralApplication(data);

        List<GeneralApplicationCollectionData> generalApplicationCollectionData
            = helper.covertToGeneralApplicationData(data.getGeneralApplicationWrapper().getGeneralApplications());
        assertEquals(1, generalApplicationCollectionData.size());

        assertEquals(GeneralApplicationStatus.REFERRED.getId(),
            generalApplicationCollectionData.get(0).getGeneralApplicationItems().getGeneralApplicationStatus());
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