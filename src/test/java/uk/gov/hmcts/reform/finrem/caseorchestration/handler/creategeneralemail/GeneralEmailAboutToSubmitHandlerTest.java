package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.InvalidEmailAddressException;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.exceptions.SendEmailException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralEmailDocumentCategoriser;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralEmailAboutToSubmitHandlerTest {

    @InjectMocks
    private GeneralEmailAboutToSubmitHandler handler;
    @Mock
    private GeneralEmailService generalEmailService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GeneralEmailDocumentCategoriser generalEmailCategoriser;

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CREATE_GENERAL_EMAIL),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CREATE_GENERAL_EMAIL)
        );
    }

    @Test
    void givenEmailWithAttachments_whenHandled_thenAttachmentsShouldBeConvertedToPdfIfRequired() {
        CaseDocument caseDocument1 = caseDocument("a.doc");
        CaseDocument caseDocument2 = caseDocument("b.doc");

        FinremCaseData finremCaseData = spy(FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(List.of(
                    DocumentCollectionItem.fromCaseDocument(caseDocument1),
                    DocumentCollectionItem.fromCaseDocument(caseDocument2)
                ))
                .build())
            .build());
        CaseType caseType = mock(CaseType.class);
        when(finremCaseData.getCcdCaseType()).thenReturn(caseType);

        CaseDocument pdf1 = caseDocument("a.pdf");
        CaseDocument pdf2 = caseDocument("b.pdf");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, caseType))
            .thenReturn(pdf1);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument2, AUTH_TOKEN, caseType))
            .thenReturn(pdf2);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, caseType),
            () -> verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument2, AUTH_TOKEN, caseType),
            () -> assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocuments())
                .extracting(DocumentCollectionItem::getValue).containsExactly(pdf1, pdf2)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenCaseType_whenHandledWithoutErrors_thenDocumentIsCategorised(boolean isConsented) {
        FinremCallbackRequest request = isConsented ? mockConsentedCallbackRequest() : mockContestedCallbackRequest();

        if (isConsented) {
            doNothing().when(notificationService).sendConsentGeneralEmail(request.getCaseDetails(), AUTH_TOKEN);
        } else {
            doNothing().when(notificationService).sendContestedGeneralEmail(request.getCaseDetails(), AUTH_TOKEN);
        }

        handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(notificationService, times(isConsented ? 0 : 1)).sendContestedGeneralEmail(request.getCaseDetails(), AUTH_TOKEN),
            () -> verify(notificationService, times(isConsented ? 1 : 0)).sendConsentGeneralEmail(request.getCaseDetails(), AUTH_TOKEN),
            () -> verify(generalEmailCategoriser, times(isConsented ? 0 : 1)).categorise(request.getFinremCaseData())
        );
    }

    @Test
    void givenInvalidEmailAddressException_whenHandled_thenReturnError() {
        FinremCallbackRequest request = mockConsentedCallbackRequest();

        doThrow(new InvalidEmailAddressException(mock(NotificationClientException.class)))
            .when(notificationService).sendConsentGeneralEmail(request.getCaseDetails(), AUTH_TOKEN);

        var response = handler.handle(request, AUTH_TOKEN);
        assertThat(response.getErrors()).containsOnly("Not a valid email address");
    }

    @Test
    void givenSendEmailException_whenHandled_thenReturnError() {
        FinremCallbackRequest request = mockConsentedCallbackRequest();

        doThrow(new SendEmailException(mock(NotificationClientException.class)))
            .when(notificationService).sendConsentGeneralEmail(request.getCaseDetails(), AUTH_TOKEN);

        var response = handler.handle(request, AUTH_TOKEN);
        assertThat(response.getErrors()).containsOnly("An error occurred when sending the email");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldStoreGeneralEmailWhenHandled(boolean isConsented) {
        FinremCallbackRequest request = isConsented ? mockConsentedCallbackRequest() : mockContestedCallbackRequest();
        handler.handle(request, AUTH_TOKEN);
        verify(generalEmailService).storeGeneralEmail(request.getFinremCaseData());
    }

    @Test
    void shouldRemoveTemporaryFieldsWhenHandled() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(finremCaseData).build();

        verifyTemporaryFieldsWereSanitised(EventType.CREATE_GENERAL_EMAIL, handler,
            finremCaseDetails, finremCaseDetailsMapper, Map.of(
                "generalEmailRecipient", "generalEmailRecipient",
                "generalEmailCreatedBy", "generalEmailCreatedBy",
                "generalEmailBody", "generalEmailBody",
                "generalEmailUploadedDocument", "generalEmailUploadedDocument"
            ));
    }

    private FinremCallbackRequest mockCallbackRequest(boolean isConsented) {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        when(finremCaseDetails.isConsentedApplication()).thenReturn(isConsented);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        return FinremCallbackRequestFactory.from(finremCaseDetails);
    }

    private FinremCallbackRequest mockConsentedCallbackRequest() {
        return mockCallbackRequest(true);
    }

    private FinremCallbackRequest mockContestedCallbackRequest() {
        return mockCallbackRequest(false);
    }
}
