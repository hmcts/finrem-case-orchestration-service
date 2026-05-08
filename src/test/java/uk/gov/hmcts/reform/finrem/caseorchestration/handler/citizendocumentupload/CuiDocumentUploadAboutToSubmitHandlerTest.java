package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizendocumentupload;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CuiDocumentUploadAboutToSubmitHandlerTest {

    @Mock
    private FinremCaseDetailsMapper mapper;

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldMergeAndSortDocumentsDescending_whenValidDocumentsPresent(HandlerCase handlerCase) {
        CitizenDocumentCollection oldDoc = doc(LocalDateTime.of(2024, 1, 1, 10, 0));
        CitizenDocumentCollection midDoc = doc(LocalDateTime.of(2024, 2, 1, 10, 0));
        CitizenDocumentCollection newDoc = doc(LocalDateTime.of(2024, 3, 1, 10, 0));

        FinremCaseData before = caseDataFor(handlerCase.party, List.of(oldDoc, midDoc));
        FinremCaseData current = caseDataFor(handlerCase.party, List.of(newDoc));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handlerCase.handler.handle(buildRequest(current, before, handlerCase.eventType), "auth");

        assertThat(documentsFor(response.getData(), handlerCase.party))
            .containsExactly(newDoc, midDoc, oldDoc);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldReturnOnlyCurrentDocuments_whenExistingDocumentsAreNull(HandlerCase handlerCase) {
        CitizenDocumentCollection newDoc = doc(LocalDateTime.of(2024, 3, 1, 10, 0));

        FinremCaseData before = new FinremCaseData();
        FinremCaseData current = caseDataFor(handlerCase.party, List.of(newDoc));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handlerCase.handler.handle(buildRequest(current, before, handlerCase.eventType), "auth");

        assertThat(documentsFor(response.getData(), handlerCase.party))
            .containsExactly(newDoc);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldReturnOnlyExistingDocuments_whenCurrentDocumentsAreNull(HandlerCase handlerCase) {
        CitizenDocumentCollection existing = doc(LocalDateTime.of(2024, 1, 1, 10, 0));

        FinremCaseData before = caseDataFor(handlerCase.party, List.of(existing));
        FinremCaseData current = new FinremCaseData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handlerCase.handler.handle(buildRequest(current, before, handlerCase.eventType), "auth");

        assertThat(documentsFor(response.getData(), handlerCase.party))
            .containsExactly(existing);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void canHandle_shouldReturnTrue_whenCorrectCallbackCaseAndEventType(HandlerCase handlerCase) {
        assertCanHandle(handlerCase.handler, CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            handlerCase.eventType);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handle_shouldUpdateOnlyRelevantCollection_whenApplicantOrRespondentEvent(HandlerCase handlerCase) {
        CitizenDocumentCollection existingApplicant = doc(LocalDateTime.of(2024, 1, 1, 10, 0));
        CitizenDocumentCollection existingRespondent = doc(LocalDateTime.of(2024, 2, 1, 10, 0));
        CitizenDocumentCollection newDoc = doc(LocalDateTime.of(2024, 3, 1, 10, 0));

        FinremCaseData before = new FinremCaseData();
        before.getCitizenDocumentWrapper().setCitizenApplicantDocument(List.of(existingApplicant));
        before.getCitizenDocumentWrapper().setCitizenRespondentDocument(List.of(existingRespondent));

        FinremCaseData current = caseDataFor(handlerCase.party, List.of(newDoc));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handlerCase.handler.handle(buildRequest(current, before, handlerCase.eventType), "auth");

        if (handlerCase.party == Party.APPLICANT) {
            assertThat(response.getData().getCitizenDocumentWrapper().getCitizenApplicantDocument())
                .containsExactly(newDoc, existingApplicant);
            assertThat(response.getData().getCitizenDocumentWrapper().getCitizenRespondentDocument()).isNull();
        } else {
            assertThat(response.getData().getCitizenDocumentWrapper().getCitizenRespondentDocument())
                .containsExactly(newDoc, existingRespondent);
            assertThat(response.getData().getCitizenDocumentWrapper().getCitizenApplicantDocument()).isNull();
        }
    }

    private static Stream<HandlerCase> handlers() {
        FinremCaseDetailsMapper mapper = mock(FinremCaseDetailsMapper.class);

        return Stream.of(
            new HandlerCase(
                new CuiApplicantDocumentUploadAboutToSubmitHandler(mapper),
                EventType.CUI_APPLICANT_DOCUMENT_UPLOAD,
                Party.APPLICANT
            ),
            new HandlerCase(
                new CuiRespondentDocumentUploadAboutToSubmitHandler(mapper),
                EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD,
                Party.RESPONDENT
            )
        );
    }

    private FinremCaseData caseDataFor(Party party, List<CitizenDocumentCollection> documents) {
        FinremCaseData caseData = new FinremCaseData();

        if (party == Party.APPLICANT) {
            caseData.getCitizenDocumentWrapper().setCitizenApplicantDocument(documents);
        } else {
            caseData.getCitizenDocumentWrapper().setCitizenRespondentDocument(documents);
        }

        return caseData;
    }

    private List<CitizenDocumentCollection> documentsFor(FinremCaseData caseData, Party party) {
        return party == Party.APPLICANT
            ? caseData.getCitizenDocumentWrapper().getCitizenApplicantDocument()
            : caseData.getCitizenDocumentWrapper().getCitizenRespondentDocument();
    }

    private FinremCallbackRequest buildRequest(
        FinremCaseData current,
        FinremCaseData before,
        EventType eventType
    ) {
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        caseDetails.setData(current);

        FinremCaseDetails caseDetailsBefore = new FinremCaseDetails();
        caseDetailsBefore.setData(before);

        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .eventType(eventType)
            .build();
    }

    private CitizenDocumentCollection doc(LocalDateTime dateTime) {
        CitizenUploadDocument document = new CitizenUploadDocument();
        document.setGeneralDocumentUploadDateTime(dateTime);

        CitizenDocumentCollection wrapper = new CitizenDocumentCollection();
        wrapper.setValue(document);

        return wrapper;
    }

    private enum Party {
        APPLICANT,
        RESPONDENT
    }

    private record HandlerCase(
        CuiDocumentUploadAboutToSubmitHandler handler,
        EventType eventType,
        Party party
    ) {
    }
}
