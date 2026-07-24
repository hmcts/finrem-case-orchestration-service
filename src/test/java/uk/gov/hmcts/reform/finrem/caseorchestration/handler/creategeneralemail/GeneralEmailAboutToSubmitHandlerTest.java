package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentConversionException;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

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
        assertCanHandleAnyCaseType(handler, CallbackType.ABOUT_TO_SUBMIT, EventType.CREATE_GENERAL_EMAIL);
    }

    @Test
    void givenEmailWithAttachment_whenHandled_thenAttachmentsShouldBeConvertedToPdfIfRequired() {
        CaseDocument caseDocument1 = caseDocument("a.doc");

        FinremCaseData finremCaseData = spy(FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(List.of(DocumentCollectionItem.fromCaseDocument(caseDocument1)))
                .build())
            .build());
        CaseType caseType = mock(CaseType.class);
        when(finremCaseData.getCcdCaseType()).thenReturn(caseType);

        CaseDocument pdf1 = caseDocument("a.pdf");
        when(genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, caseType))
            .thenReturn(pdf1);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(genericDocumentService).convertDocumentIfNotPdfAlready(caseDocument1, AUTH_TOKEN, caseType),
            () -> assertThat(finremCaseData.getGeneralEmailWrapper()
                .getGeneralEmailUploadedDocuments()
                .getFirst()
                .getValue())
                .isEqualTo(pdf1)
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
    void shouldReturnWhenDocumentValidationFails() {
        FinremCallbackRequest request = mockConsentedCallbackRequest();
        FinremCaseData caseData = request.getFinremCaseData();

        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(2);
            errors.add("Validation error");
            return null;
        }).when(generalEmailService)
            .validateUploadedDocuments(same(caseData), eq(AUTH_TOKEN), anyList());

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getErrors()).containsOnly("Validation error");
        verify(generalEmailService, never()).storeGeneralEmail(any());
    }

    @Test
    void shouldRemoveTemporaryFieldsWhenHandled() {
        verifyTemporaryFieldsWereSanitised(handler,
            finremCaseDetailsMapper, new HashMap<>(Map.of(
                "generalEmailRecipient", "generalEmailRecipient",
                "generalEmailCreatedBy", "generalEmailCreatedBy",
                "generalEmailBody", "generalEmailBody",
                "generalEmailUploadedDocuments", List.of()
            )));
    }

    @Test
    void givenDocumentConversionException_whenHandled_thenReturnError() {
        CaseDocument caseDocument = caseDocument("a.doc");

        FinremCaseData finremCaseData = spy(FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(List.of(DocumentCollectionItem.fromCaseDocument(caseDocument)))
                .build())
            .build());

        CaseType caseType = mock(CaseType.class);
        when(finremCaseData.getCcdCaseType()).thenReturn(caseType);

        doThrow(new DocumentConversionException("Conversion failed", new RuntimeException()))
            .when(genericDocumentService)
            .convertDocumentIfNotPdfAlready(caseDocument, AUTH_TOKEN, caseType);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getErrors())
            .containsOnly("Unable to convert a provided attachment to PDF");
    }

    private FinremCallbackRequest mockCallbackRequest(boolean isConsented) {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        lenient().when(finremCaseDetails.isConsentedApplication()).thenReturn(isConsented);
        lenient().when(finremCaseDetails.isContestedApplication()).thenReturn(!isConsented);
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
