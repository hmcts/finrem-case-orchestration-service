package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.Bin;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.MOCKED_EVENT_CCD_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;

@ExtendWith(MockitoExtension.class)
class FinremCallbackHandlerTest {

    private static final String PROPERTY_TO_BE_RETAINED = "d81Question";
    private static final String PROPERTY_TO_BE_RETAINED_IN_CASE_DOCUMENT = "divorceUploadPetition";
    private static final String TEMP_PROPERTY_TO_BE_CLEARED_1 = "stopRepJudicialApproval";
    private static final String TEMP_PROPERTY_TO_BE_CLEARED_2 = "clientAddressForService";
    private static final String TEMP_PROPERTY_TO_BE_BINNED = "generalApplicationDirectionsPreview";

    static class GenericFinremCallbackHandler extends FinremCallbackHandler {

        public GenericFinremCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData());
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    static class ValidateCaseDataTestHandler extends GenericFinremCallbackHandler {
        public ValidateCaseDataTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            validateCaseData(callbackRequest);
            return response(callbackRequest.getCaseDetails().getData());
        }
    }

    static class GenericAboutToSubmitCallbackHandler extends FinremAboutToSubmitCallbackHandler {

        public GenericAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData());
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    static class ResponseWithoutWarningsTestHandler extends GenericAboutToSubmitCallbackHandler {

        public ResponseWithoutWarningsTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return responseWithoutWarnings(callbackRequest.getCaseDetails().getData(),
                List.of("ERROR12345"));
        }
    }

    static class ResponseTestHandler extends GenericAboutToSubmitCallbackHandler {

        public ResponseTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData(),
                List.of("WARNING_1111"),
                List.of("ERROR_1111"),
                "POST_STATE");
        }
    }

    static class GenericSubmittedCallbackHandler extends FinremSubmittedCallbackHandler {

        public GenericSubmittedCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               EvidenceManagementDeleteService evidenceManagementDeleteService,
                                               RetryExecutor retryExecutor) {
            super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return submittedResponse(toConfirmationHeader("TEST"),
                toConfirmationBody("Message One", "Message Two"));
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    private CaseDocument documentToBeBinned = caseDocument();

    private GenericFinremCallbackHandler finremCallbackHandler;
    private GenericAboutToSubmitCallbackHandler aboutToSubmitCallbackHandler;
    private GenericSubmittedCallbackHandler submittedCallbackHandler;
    private ResponseWithoutWarningsTestHandler responseWithoutWarningsTestHandler;
    private ResponseTestHandler responseTestHandler;
    private ValidateCaseDataTestHandler validateCaseDataTestHandler;

    @BeforeEach
    void setUp() {
        finremCallbackHandler = spy(new GenericFinremCallbackHandler(finremCaseDetailsMapper));
        aboutToSubmitCallbackHandler = spy(new GenericAboutToSubmitCallbackHandler(finremCaseDetailsMapper));
        submittedCallbackHandler = spy(new GenericSubmittedCallbackHandler(finremCaseDetailsMapper,
            evidenceManagementDeleteService, retryExecutor));
        responseWithoutWarningsTestHandler = new ResponseWithoutWarningsTestHandler(finremCaseDetailsMapper);
        responseTestHandler = new ResponseTestHandler(finremCaseDetailsMapper);
        validateCaseDataTestHandler = new ValidateCaseDataTestHandler(finremCaseDetailsMapper);
    }

    @Test
    void shouldPopulateCaseIdToBothFinremCaseDetails() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(finremCaseDetails.getCaseIdAsString()).thenReturn(CASE_ID);

        FinremCaseData caseDataBefore = spy(FinremCaseData.builder().build());
        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(finremCaseDetailsBefore.getData()).thenReturn(caseDataBefore);
        CallbackRequest callbackRequest = mock(CallbackRequest.class);
        when(callbackRequest.getEventId()).thenReturn("FR_sendOrder");

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        CaseDetails caseDetailsBefore = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetailsBefore)).thenReturn(finremCaseDetailsBefore);

        finremCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(caseData).setCcdCaseId(CASE_ID),
            () -> verify(caseDataBefore).setCcdCaseId(CASE_ID)
        );
    }

    @Test
    void givenSubmittedHandler_whenHandled_thenShouldPopulateConfirmationHeaderAndConfirmationBody() {
        assertThat(submittedCallbackHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN))
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody)
            .containsExactly("# TEST", "<ul><li><h2>Message One</h2></li><li><h2>Message Two</h2></li></ul>");
    }

    @Test
    void givenHandlerWithResponseWithoutWarnings_whenHandled_thenShouldPopulateError() {
        var response = responseWithoutWarningsTestHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN);
        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getErrors,
                GenericAboutToStartOrSubmitCallbackResponse::getWarnings
            ).containsExactly(
                List.of("ERROR12345"), List.of()
            )
        ;
    }

    @Test
    void givenHandlerWithResponse_whenHandled_thenShouldPopulateError() {
        var response = responseTestHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN);
        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getErrors,
                GenericAboutToStartOrSubmitCallbackResponse::getWarnings,
                GenericAboutToStartOrSubmitCallbackResponse::getState
            ).containsExactly(
                List.of("ERROR_1111"), List.of("WARNING_1111"), "POST_STATE"
            )
        ;
    }

    @Test
    void givenHandler_whenValidateCaseData_thenThrowException() {
        final FinremCallbackRequest nullCaseDetailsRequest =
            FinremCallbackRequestFactory.from((FinremCaseDetails) null);

        final FinremCallbackRequest emptyCaseDataRequest =
            FinremCallbackRequestFactory.from(
                FinremCaseDetails.builder().build()
            );

        final FinremCallbackRequest validRequest =
            FinremCallbackRequestFactory.from(
                FinremCaseDetails.builder()
                    .data(FinremCaseData.builder().build())
                    .build()
            );

        assertThrows(
            InvalidCaseDataException.class,
            () -> validateCaseDataTestHandler.handle((FinremCallbackRequest) null, AUTH_TOKEN)
        );

        assertThrows(
            InvalidCaseDataException.class,
            () -> validateCaseDataTestHandler.handle(nullCaseDetailsRequest, AUTH_TOKEN)
        );

        assertThrows(
            InvalidCaseDataException.class,
            () -> validateCaseDataTestHandler.handle(emptyCaseDataRequest, AUTH_TOKEN)
        );

        assertDoesNotThrow(
            () -> validateCaseDataTestHandler.handle(validRequest, AUTH_TOKEN)
        );
    }

    @Nested
    class ClearTemporaryFieldsTests {

        Bin mockedBin = mock(Bin.class);

        CallbackRequest callbackRequest;

        Map<String, Object> toBeSanitisedMap;

        FinremCaseData sanitisedFinremCaseData;

        FinremCaseData nonSanitisedFinremCaseData;

        @BeforeEach
        void setUp() {
            nonSanitisedFinremCaseData = FinremCaseData.builder().bin(mockedBin).build();
            FinremCaseDetails finremCaseDetails = spy(
                FinremCaseDetails.builder().data(nonSanitisedFinremCaseData).build()
            );

            CaseDetails callbackRequestCaseDetails = mock(CaseDetails.class);
            when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequestCaseDetails))
                .thenReturn(finremCaseDetails);

            callbackRequest = mock(CallbackRequest.class);
            when(callbackRequest.getCaseDetails()).thenReturn(callbackRequestCaseDetails);
            when(callbackRequest.getEventId()).thenReturn(MOCKED_EVENT_CCD_TYPE);
        }

        @Test
        void aboutToSubmitHandlerShouldClearTemporaryFields() {
            mockForClearTemporaryFields(Map.of(
                PROPERTY_TO_BE_RETAINED, YesOrNo.YES,
                TEMP_PROPERTY_TO_BE_CLEARED_1, YesOrNo.YES,
                TEMP_PROPERTY_TO_BE_CLEARED_2, Address.builder().addressLine1("Test Address").build()
            ));
            when(finremCaseDetailsMapper.mapToFinremCaseData(argThat(
                map -> map.size() == 1 && map.containsKey(PROPERTY_TO_BE_RETAINED)
            ))).thenReturn(sanitisedFinremCaseData);
            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                var response = aboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    // only return sanitisedFinremCaseData if TESTING_DATA_IN_MAP is sanitised
                    () -> assertEquals(sanitisedFinremCaseData, response.getData())
                );
            }
        }

        @Test
        void aboutToSubmitHandlerShouldBinCaseDocumentsFromTemporaryFields() {
            mockForClearTemporaryFields(Map.of(
                TEMP_PROPERTY_TO_BE_BINNED, documentToBeBinned
            ));
            when(finremCaseDetailsMapper.mapToFinremCaseData(argThat(
                Map::isEmpty
            ))).thenReturn(sanitisedFinremCaseData);

            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                var response = aboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    // only return sanitisedFinremCaseData if TESTING_DATA_IN_MAP is sanitised
                    () -> assertEquals(sanitisedFinremCaseData, response.getData()),
                    () -> verify(mockedBin).binCaseDocument(documentToBeBinned)
                );
            }
        }

        @Test
        void whenCaseDocumentExistsInOtherField_whenHandle_shouldNotBinCaseDocumentFromTemporaryFields() {
            mockForClearTemporaryFields(Map.of(
                TEMP_PROPERTY_TO_BE_BINNED, documentToBeBinned,
                PROPERTY_TO_BE_RETAINED_IN_CASE_DOCUMENT, documentToBeBinned
            ));
            when(finremCaseDetailsMapper.mapToFinremCaseData(argThat(
                map -> map.size() == 1 && map.containsKey(PROPERTY_TO_BE_RETAINED_IN_CASE_DOCUMENT)
            ))).thenReturn(sanitisedFinremCaseData);

            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                var response = aboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    // only return sanitisedFinremCaseData if TESTING_DATA_IN_MAP is sanitised
                    () -> assertEquals(sanitisedFinremCaseData, response.getData()),
                    () -> verify(mockedBin, never()).binCaseDocument(documentToBeBinned)
                );
            }
        }

        @Test
        void submittedHandlerShouldNotClearTemporaryFields() {
            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                submittedCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    () -> verify(submittedCallbackHandler, never()).shouldClearTemporaryFieldsAfterHandle(),
                    () -> verify(submittedCallbackHandler, never()).removeTemporaryFieldsAfterHandled(any())
                );
            }
        }

        private void mockForClearTemporaryFields(Map<String, Object> dataMap) {
            toBeSanitisedMap = new HashMap<>(dataMap);
            when(finremCaseDetailsMapper.finremCaseDataToMap(nonSanitisedFinremCaseData)
            ).thenReturn(toBeSanitisedMap);
            sanitisedFinremCaseData = spy(FinremCaseData.builder().build());
        }
    }

    @Nested
    class ClearBinBeforeHandleTests {

        Bin spiedBin;

        CallbackRequest callbackRequest;

        Map<String, Object> toBeSanitisedMap;

        FinremCaseData sanitisedFinremCaseData;

        FinremCaseData nonSanitisedFinremCaseData;

        @BeforeEach
        void setup() {
            spiedBin = spy(Bin.builder().build());

            nonSanitisedFinremCaseData = FinremCaseData.builder().bin(spiedBin).build();
            FinremCaseDetails finremCaseDetails = spy(
                FinremCaseDetails.builder().data(nonSanitisedFinremCaseData).build()
            );

            CaseDetails callbackRequestCaseDetails = mock(CaseDetails.class);
            when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequestCaseDetails))
                .thenReturn(finremCaseDetails);

            callbackRequest = mock(CallbackRequest.class);
            when(callbackRequest.getCaseDetails()).thenReturn(callbackRequestCaseDetails);
            when(callbackRequest.getEventId()).thenReturn(MOCKED_EVENT_CCD_TYPE);

            // mocking finremCaseDetailsMapper in method removeTemporaryFieldsAfterHandled
            toBeSanitisedMap = new HashMap<>();
            lenient().when(finremCaseDetailsMapper.finremCaseDataToMap(nonSanitisedFinremCaseData)
            ).thenReturn(toBeSanitisedMap);
            sanitisedFinremCaseData = mock(FinremCaseData.class);
            lenient().when(finremCaseDetailsMapper.mapToFinremCaseData(toBeSanitisedMap))
                .thenReturn(sanitisedFinremCaseData);
        }

        @Test
        void submittedHandlerShouldNotClearBin() {
            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                submittedCallbackHandler.handle(callbackRequest, AUTH_TOKEN);
                verify(spiedBin, never()).clearBin();
            }
        }

        @Test
        void aboutToSubmitHandlerShouldClearBin() {
            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                var response = aboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    () -> verify(spiedBin).clearBin(),
                    () -> assertEquals(sanitisedFinremCaseData, response.getData())
                );
            }
        }

        @Test
        void finremCallbackHandlerShouldClearBin() {
            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                var response = finremCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    () -> verify(spiedBin).clearBin(),
                    () -> assertEquals(nonSanitisedFinremCaseData, response.getData())
                );
            }
        }
    }

    @Nested
    class PurgeBinAfterHandleTests {

        @Mock
        Bin mockedBin;

        CallbackRequest callbackRequest;

        FinremCaseData nonSanitisedFinremCaseData;

        @BeforeEach
        void setUp() {
            nonSanitisedFinremCaseData = FinremCaseData.builder()
                .bin(mockedBin)
                .build();
            FinremCaseDetails finremCaseDetails = spy(
                FinremCaseDetails.builder().id(CASE_ID_IN_LONG).data(nonSanitisedFinremCaseData).build()
            );

            CaseDetails callbackRequestCaseDetails = mock(CaseDetails.class);
            when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequestCaseDetails))
                .thenReturn(finremCaseDetails);

            callbackRequest = mock(CallbackRequest.class);
            when(callbackRequest.getCaseDetails()).thenReturn(callbackRequestCaseDetails);
            when(callbackRequest.getEventId()).thenReturn(MOCKED_EVENT_CCD_TYPE);
        }

        @Test
        void submittedHandlerShouldPurgeFileUrlsInBin() {
            String documentAUrl = caseDocument("fileA").getDocumentUrl();
            String documentBUrl = caseDocument("fileB").getDocumentUrl();
            when(mockedBin.getFileUrlsToBeDeleted())
                .thenReturn(DynamicList.builder()
                    .listItems(List.of(
                        DynamicListElement.builder().code(documentAUrl).build(),
                        DynamicListElement.builder().code(documentBUrl).build()
                    ))
                    .build());

            try (MockedStatic<EventType> mockedStatic = Mockito.mockStatic(EventType.class)) {
                EventType eventType = mock(EventType.class);
                mockedStatic.when(() -> EventType.getEventType(MOCKED_EVENT_CCD_TYPE))
                    .thenReturn(eventType);

                ArgumentCaptor<ThrowingRunnable> captor = getThrowingRunnableCaptor();

                submittedCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

                assertAll(
                    () -> verify(retryExecutor)
                        .runWithRetrySuppressException(captor.capture(), eq("Physical File Deletion - %s".formatted(documentAUrl)), eq(CASE_ID)),
                    () -> verify(retryExecutor)
                        .runWithRetrySuppressException(captor.capture(), eq("Physical File Deletion - %s".formatted(documentBUrl)), eq(CASE_ID)),
                    () -> captor.getAllValues().forEach(TestSetUpUtils::runSafely),
                    () -> verify(evidenceManagementDeleteService).delete(documentAUrl, AUTH_TOKEN),
                    () -> verify(evidenceManagementDeleteService).delete(documentBUrl, AUTH_TOKEN),
                    () -> verifyNoMoreInteractions(evidenceManagementDeleteService)
                );
            }
        }
    }
}
