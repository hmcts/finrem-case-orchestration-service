package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageHearingsCorresponderTest {

    @Mock
    private HearingNotificationHelper hearingNotificationHelper;

    @InjectMocks
    private ManageHearingsCorresponder corresponder;

    @Test
    void shouldSendNotificationsWhenShouldSendNotificationTrue() {
        // Arrange
        DynamicMultiSelectListElement firstParty = new DynamicMultiSelectListElement();
        DynamicMultiSelectListElement secondParty = new DynamicMultiSelectListElement();

        firstParty.setCode("[APP_SOLICITOR]");
        secondParty.setCode("[RESP_SOLICITOR]");

        DynamicMultiSelectList list = new DynamicMultiSelectList();
        list.setValue(List.of(firstParty, secondParty));

        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(list);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Assert that sendHearingNotificationsByParty called for each party
        verify(hearingNotificationHelper).sendHearingCorrespondenceByParty(
                firstParty, callbackRequest.getCaseDetails(), hearing, AUTH_TOKEN);
        verify(hearingNotificationHelper).sendHearingCorrespondenceByParty(
                secondParty, callbackRequest.getCaseDetails(), hearing, AUTH_TOKEN);
    }

    @Test
    void shouldNotSendNotificationsWhenShouldNotSendNotificationTrue() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        Hearing hearing = new Hearing();

        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(true);

        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingNotificationHelper, never())
                .sendHearingCorrespondenceByParty(any(), any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationsNoPartiesSelected() {
        // Arrange
        FinremCallbackRequest callbackRequest = callbackRequest();

        DynamicMultiSelectList list = new DynamicMultiSelectList();
        list.setValue(List.of());

        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(list);

        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingNotificationHelper, never())
                .sendHearingCorrespondenceByParty(any(), any(), any(), any());
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
                .builder()
                .caseDetails(FinremCaseDetails.builder().id(123L)
                        .data(new FinremCaseData()).build())
                .build();
    }
}
