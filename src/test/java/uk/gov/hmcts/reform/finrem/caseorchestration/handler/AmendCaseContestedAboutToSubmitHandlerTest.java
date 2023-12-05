package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetailsCollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AmendCaseService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AmendCaseContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private AmendCaseService amendCaseService;

    @InjectMocks
    private AmendCaseContestedAboutToSubmitHandler handler;

    @Test
    void givenContestedCase_whenEventIsAmendCase_thenHandlerCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenEventIsClose_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenCallBackIsStart_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.AMEND_CASE));
    }

    @Test
    void givenContestedCase_whenNoneMatches_thenHandlerCanNotHandle() {
        assertFalse(handler.canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldMissing_thenDefaultsToMatrimoninalType() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(amendCaseService).addApplicationType(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldIsNotMissing_thenDoNotUpdate() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();
        data.getScheduleOneWrapper()
            .setTypeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(amendCaseService).addApplicationType(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenContestedCase_whenTypeOfApplicationFieldIsMissingForSchedule1App_thenDoNotUpdate() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseData data = finremCallbackRequest.getCaseDetails().getData();
        List<ChildDetailsCollectionElement> childrenCollection = new ArrayList<>();
        childrenCollection.add(ChildDetailsCollectionElement.builder()
            .childDetails(ChildDetails.builder().childrenLiveInEnglandOrWales(YesOrNo.YES).build()).build());
        data.getScheduleOneWrapper().setChildrenCollection(childrenCollection);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);


        verify(amendCaseService).addApplicationType(finremCallbackRequest.getCaseDetails().getData());
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.AMEND_CASE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}