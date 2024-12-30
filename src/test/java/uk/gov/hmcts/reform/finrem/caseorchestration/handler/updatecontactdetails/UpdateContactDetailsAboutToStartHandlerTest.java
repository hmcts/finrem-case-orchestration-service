package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsAboutToStartHandlerTest {

    @InjectMocks
    private UpdateContactDetailsAboutToStartHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.UPDATE_CONTACT_DETAILS),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS)
        );
    }

    @Test
    void testHandle() {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .updateIncludesRepresentativeChange(YesOrNo.YES)
                .build()).build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, AUTH_TOKEN);
        assertNull(response.getData().getContactDetailsWrapper().getNocParty());
        assertNull(response.getData().getContactDetailsWrapper().getUpdateIncludesRepresentativeChange());
    }

    @Test
    void testPopulateInRefugeQuestionsCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails), times(1));
            mockedStatic.verify(() -> RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails), times(1));
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
                .builder()
                .eventType(EventType.UPDATE_CONTACT_DETAILS)
                .caseDetails(FinremCaseDetails.builder().id(123L)
                        .data(new FinremCaseData()).build())
                .build();
    }
}
