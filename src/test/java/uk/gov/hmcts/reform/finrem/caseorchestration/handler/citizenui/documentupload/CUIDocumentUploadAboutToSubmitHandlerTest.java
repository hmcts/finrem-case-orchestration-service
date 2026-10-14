package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CitizenPartyType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter.CitizenDocumentSorter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

class CUIDocumentUploadAboutToSubmitHandlerTest {

    @ParameterizedTest
    @MethodSource("handlers")
    void canHandle_shouldReturnTrue_whenCorrectCallbackCaseAndEventType(HandlerCase handlerCase) {
        assertCanHandle(handlerCase.handler, CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            handlerCase.eventType);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldCategoriseSortCreateAuditAndReturnCurrentCaseData(HandlerCase handlerCase) {
        CitizenDocumentCollectionItem newDoc = doc(LocalDateTime.of(2026, 3, 1, 10, 0));
        FinremCaseData current = caseDataFor(handlerCase.party, List.of(newDoc));
        FinremCallbackRequest request = buildRequest(current, handlerCase.eventType);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handlerCase.handler.handle(request, AUTH_TOKEN);

        assertThat(response.getData()).isSameAs(current);
        assertThat(documentsFor(response.getData(), handlerCase.party))
            .containsExactly(newDoc);

        verify(handlerCase.documentsCategoriser).categorise(current);
        verify(handlerCase.citizenDocumentSorter)
            .sortDocuments(current.getCitizenDocumentWrapper(), handlerCase.party);
        verify(handlerCase.citizenDocumentsUploadedCorresponder).buildCorrespondenceEvent(
            request.getCaseDetails(),
            AUTH_TOKEN,
            handlerCase.notificationParty
        );
        verify(handlerCase.correspondenceEventAuditOrchestrationService)
            .createPendingAudits(handlerCase.sendCorrespondenceEvent, handlerCase.eventType);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldCreateNotificationAuditRowsWhenCorrespondenceEventExists(HandlerCase handlerCase) {
        FinremCaseData current = caseDataFor(handlerCase.party, List.of(doc(LocalDateTime.of(2026, 3, 1, 10, 0))));
        FinremCallbackRequest request = buildRequest(current, handlerCase.eventType);

        handlerCase.handler.handle(request, AUTH_TOKEN);

        verify(handlerCase.correspondenceEventAuditOrchestrationService).createPendingAudits(any(), eq(handlerCase.eventType));
        verify(handlerCase.citizenDocumentsUploadedCorresponder).buildCorrespondenceEvent(
            any(),
            anyString(),
            eq(handlerCase.notificationParty)
        );
    }

    private static Stream<HandlerCase> handlers() {
        return Stream.of(
            handlerCase(CitizenPartyType.APPLICANT),
            handlerCase(CitizenPartyType.RESPONDENT)
        );
    }

    private static HandlerCase handlerCase(CitizenPartyType party) {
        FinremCaseDetailsMapper mapper = mock(FinremCaseDetailsMapper.class);
        CUIDocumentsCategoriser documentsCategoriser = mock(CUIDocumentsCategoriser.class);
        CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService =
            mock(CorrespondenceEventAuditOrchestrationService.class);
        CitizenDocumentSorter citizenDocumentSorter = mock(CitizenDocumentSorter.class);
        CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder = mock(CitizenDocumentsUploadedCorresponder.class);
        SendCorrespondenceEvent sendCorrespondenceEvent = SendCorrespondenceEvent.builder().build();
        when(citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(any(), anyString(), any()))
            .thenReturn(sendCorrespondenceEvent);

        EventType eventType = CitizenPartyType.APPLICANT.equals(party)
            ? EventType.CUI_APPLICANT_DOCUMENT_UPLOAD
            : EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD;
        NotificationParty notificationParty = CitizenPartyType.APPLICANT.equals(party)
            ? NotificationParty.CITIZEN_APPLICANT
            : NotificationParty.CITIZEN_RESPONDENT;
        CUIDocumentUploadAboutToSubmitHandler handler = CitizenPartyType.APPLICANT.equals(party)
            ? new CUIApplicantDocumentUploadAboutToSubmitHandler(
                mapper,
                correspondenceEventAuditOrchestrationService,
                citizenDocumentsUploadedCorresponder,
                documentsCategoriser,
                citizenDocumentSorter
            ) :
            new CUIRespondentDocumentUploadAboutToSubmitHandler(
                mapper,
                correspondenceEventAuditOrchestrationService,
                citizenDocumentsUploadedCorresponder,
                documentsCategoriser,
                citizenDocumentSorter
            );

        return new HandlerCase(
            handler,
            eventType,
            party,
            notificationParty,
            correspondenceEventAuditOrchestrationService,
            citizenDocumentsUploadedCorresponder,
            documentsCategoriser,
            citizenDocumentSorter,
            sendCorrespondenceEvent
        );
    }

    private FinremCaseData caseDataFor(CitizenPartyType party, List<CitizenDocumentCollectionItem> documents) {
        FinremCaseData caseData = new FinremCaseData();

        if (CitizenPartyType.APPLICANT.equals(party)) {
            caseData.getCitizenDocumentWrapper().setUploadedCuiApplicantDocuments(documents);
        } else {
            caseData.getCitizenDocumentWrapper().setUploadedCuiRespondentDocuments(documents);
        }

        return caseData;
    }

    private List<CitizenDocumentCollectionItem> documentsFor(FinremCaseData caseData, CitizenPartyType party) {
        return CitizenPartyType.APPLICANT.equals(party)
            ? caseData.getCitizenDocumentWrapper().getUploadedCuiApplicantDocuments()
            : caseData.getCitizenDocumentWrapper().getUploadedCuiRespondentDocuments();
    }

    private FinremCallbackRequest buildRequest(
        FinremCaseData current,
        EventType eventType
    ) {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        caseDetails.setData(current);

        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(null)
            .eventType(eventType)
            .build();
    }

    private CitizenDocumentCollectionItem doc(LocalDateTime dateTime) {
        CitizenUploadDocument document = new CitizenUploadDocument();
        document.setGeneralDocumentUploadDateTime(dateTime);

        CitizenDocumentCollectionItem wrapper = new CitizenDocumentCollectionItem();
        wrapper.setValue(document);

        return wrapper;
    }

    private record HandlerCase(
        CUIDocumentUploadAboutToSubmitHandler handler,
        EventType eventType,
        CitizenPartyType party,
        NotificationParty notificationParty,
        CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService,
        CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
        CUIDocumentsCategoriser documentsCategoriser,
        CitizenDocumentSorter citizenDocumentSorter,
        SendCorrespondenceEvent sendCorrespondenceEvent
    ) {
    }
}
