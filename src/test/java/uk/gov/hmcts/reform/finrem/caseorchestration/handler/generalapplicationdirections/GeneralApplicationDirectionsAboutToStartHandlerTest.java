package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_APPLICATION_DIRECTIONS_MH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.GeneralApplicationStatus.DIRECTION_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@RunWith(MockitoJUnitRunner.class)
class GeneralApplicationDirectionsAboutToStartHandlerTest {

    private GeneralApplicationDirectionsAboutToStartHandler handler;
    private GeneralApplicationHelper helper;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;
    @Mock
    private GenericDocumentService documentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private PartyService partyService;

    private ObjectMapper objectMapper;

    private static final String GA_JSON = "/fixtures/contested/general-application-direction-finrem.json";
    private static final String GA_NON_COLL_JSON = "/fixtures/contested/general-application.json";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        helper = new GeneralApplicationHelper(objectMapper, documentService);
        handler = new GeneralApplicationDirectionsAboutToStartHandler(assignCaseAccessService,
            finremCaseDetailsMapper, helper, generalApplicationDirectionsService, partyService);

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(
            List.of()
        ).build();
        when(partyService.getAllActivePartyList(any(FinremCaseDetails.class)))
            .thenReturn(dynamicMultiSelectList);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, ABOUT_TO_START, CONTESTED, GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    @Test
    void givenCase_whenExistingGeneAppNonCollection_thenCreateSelectionList() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_NON_COLL_JSON);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList()
        );

        assertEquals(1, dynamicList.getListItems().size());
        verify(generalApplicationDirectionsService).resetGeneralApplicationDirectionsFields(any());
    }

    @Test
    void givenCase_whenExistingGeneAppAsACollection_thenCreateSelectionList() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle =
            handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        DynamicList dynamicList = helper.objectToDynamicList(
            caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsList()
        );

        assertEquals(1, dynamicList.getListItems().size());
        verify(generalApplicationDirectionsService).resetGeneralApplicationDirectionsFields(any());
    }

    @Test
    void givenCase_whenNoApplicationAvailable_thenShowErrorMessage() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<GeneralApplicationCollectionData> existingList = helper.getGeneralApplicationList(
            caseData, GENERAL_APPLICATION_COLLECTION);
        List<GeneralApplicationCollectionData> updatedList
            = existingList.stream().map(this::updateStatus).toList();

        caseData.getGeneralApplicationWrapper().setGeneralApplications(
            helper.convertToGeneralApplicationsCollection(updatedList));
        caseData.getGeneralApplicationWrapper().setGeneralApplicationCreatedBy(null);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(handle.getErrors())
            .contains("There are no general application available for issue direction.");
        verify(generalApplicationDirectionsService).resetGeneralApplicationDirectionsFields(any());
    }

    @Test
    void givenCase_whenHandle_thenPrepopulateFields() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID, FinremCaseData.builder().build());

        DynamicMultiSelectList mockedDynamicMultiSelectList = mock(DynamicMultiSelectList.class);
        when(partyService.getAllActivePartyList(callbackRequest.getCaseDetails()))
            .thenReturn(mockedDynamicMultiSelectList);
        when(mockedDynamicMultiSelectList.setValueByCodes(eq(List.of(APP_SOLICITOR.getCcdCode(),
            RESP_SOLICITOR.getCcdCode())))).thenReturn(mockedDynamicMultiSelectList);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(handle.getData().getManageHearingsWrapper().getWorkingHearing()).isNotNull();
        assertThat(handle.getData().getManageHearingsWrapper().getWorkingHearing())
            .extracting(WorkingHearing::getHearingNoticePrompt)
                .isEqualTo(YesOrNo.YES);
        assertThat(handle.getData().getManageHearingsWrapper().getWorkingHearing().getPartiesOnCaseMultiSelectList())
            .isEqualTo(mockedDynamicMultiSelectList);

        verify(mockedDynamicMultiSelectList).setValueByCodes(eq(List.of(APP_SOLICITOR.getCcdCode(),
            RESP_SOLICITOR.getCcdCode())));
    }

    @Test
    void givenCase_whenHandlerInvoked_thenInitializeWorkingHearing() {
        //Arrange
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(GA_JSON);
        callbackRequest.setEventType(GENERAL_APPLICATION_DIRECTIONS_MH);
        callbackRequest.getCaseDetails().getData().getGeneralApplicationWrapper().getGeneralApplications()
            .forEach(ga -> ga.getValue().setGeneralApplicationSender(buildDynamicIntervenerList()));

        //Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        //Assert
        FinremCaseData caseData = response.getData();
        WorkingHearing workingHearing = caseData.getManageHearingsWrapper().getWorkingHearing();

        List<DynamicListElement> listItems = workingHearing.getHearingTypeDynamicList().getListItems();
        assertEquals(1, listItems.size());
        assertEquals(HearingType.APPLICATION_HEARING.name(), listItems.getFirst().getCode());
        assertEquals(HearingType.APPLICATION_HEARING.getId(), listItems.getFirst().getLabel());
        verify(generalApplicationDirectionsService).resetGeneralApplicationDirectionsFields(caseData);
    }

    private DynamicRadioListElement getDynamicListElement(String code, String label) {
        return DynamicRadioListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private DynamicRadioList buildDynamicIntervenerList() {

        List<DynamicRadioListElement> dynamicListElements = List.of(getDynamicListElement(APPLICANT, APPLICANT),
            getDynamicListElement(RESPONDENT, RESPONDENT),
            getDynamicListElement(CASE_LEVEL_ROLE, CASE_LEVEL_ROLE)
        );
        return DynamicRadioList.builder()
            .value(dynamicListElements.getFirst())
            .listItems(dynamicListElements)
            .build();
    }

    private GeneralApplicationCollectionData updateStatus(GeneralApplicationCollectionData obj) {
        if (obj.getId().equals("b0bfb0af-4f07-4628-a677-1de904b6ea1c")) {
            obj.getGeneralApplicationItems().setGeneralApplicationStatus(DIRECTION_APPROVED.getId());
        }
        return obj;
    }

    private FinremCallbackRequest buildFinremCallbackRequest(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
