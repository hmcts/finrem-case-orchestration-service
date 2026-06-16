package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GenericInputFields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorUploadDocumentAboutToSubmitHandlerTest {

    private static final CaseDocument deleteCaseDocumentA = caseDocument("del-A.doc");
    private static final CaseDocument deleteCaseDocumentB = caseDocument("del-B.doc");
    private static final CaseDocument caseDocumentB = caseDocument("B.doc");

    @InjectMocks
    private SolicitorUploadDocumentAboutToSubmitHandler underTest;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_UPLOAD_DOCUMENT);
    }

    @Test
    void shouldRemoveReadyToSubmitDocumentWhenHandled() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.YES).build())
            .build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();

        verifyTemporaryFieldsWereSanitised(underTest,
            finremCaseDetails, finremCaseDetailsMapper, new HashMap<>(Map.of(
                "readyToSubmitDocument", YesOrNo.YES
            ))
        );
    }

    @Test
    void givenReadyToSubmit_whenHandled_thenSetInfoReceivedCaseStateAndWarningPopulated() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.YES).build())
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getState()).isEqualTo("infoReceived"),
            () -> assertThat(response.getWarnings()).containsOnly(
                "Please note your documents will be submitted and you won't be able to upload any additional documents."
            )
        );
    }

    @Test
    void givenNotReadyToSubmit_whenHandled_thenCaseStateShouldBeNullAndWarningPopulated() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.NO).build())
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getState()).isNull(),
            () -> assertThat(response.getWarnings()).containsOnly(
                "Please note your documents will not be submitted, to allow you to upload additional documents."
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenAnyDocumentsRemoved_whenHandled_thenDocumentUrlsShouldBeBinned(
        List<SolUploadDocumentCollection> previousSolUploadDocuments,
        List<PensionTypeCollection> previousPensionCollection,
        List<SolUploadDocumentCollection> solUploadDocuments,
        List<PensionTypeCollection> pensionCollection,
        List<CaseDocument> expectedDeletedCaseDocuments

    ) {
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .solUploadDocuments(previousSolUploadDocuments)
            .pensionCollection(previousPensionCollection)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .solUploadDocuments(solUploadDocuments)
            .pensionCollection(pensionCollection)
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            finremCaseDataBefore, finremCaseData), AUTH_TOKEN);

        var data = response.getData();
        var bin = data != null ? data.getBin() : null;
        var fileUrls = bin != null ? bin.getFileUrlsToBeDeleted() : null;

        assertAll(
            () -> assertThat(data).isNotNull(),
            () -> assertThat(bin).isNotNull(),
            () -> assertThat(fileUrls).isNotNull()
        );
        assertThat(fileUrls.getListItems())
            .isNotNull()
            .extracting(DynamicListElement::getCode)
            .containsExactly(
                expectedDeletedCaseDocuments.stream()
                    .map(CaseDocument::getDocumentUrl)
                    .toArray(String[]::new)
            );
    }

    static Stream<Arguments> givenAnyDocumentsRemoved_whenHandled_thenDocumentUrlsShouldBeBinned() {
        return Stream.of(
            Arguments.of(
                List.of(solUploadDocumentCollection(deleteCaseDocumentA)), null,
                null, null,
                List.of(deleteCaseDocumentA)),
            Arguments.of(
                List.of(solUploadDocumentCollection(deleteCaseDocumentA)), List.of(pensionTypeCollection(deleteCaseDocumentB)),
                null, null,
                List.of(deleteCaseDocumentA, deleteCaseDocumentB)),
            Arguments.of(
                List.of(solUploadDocumentCollection(caseDocumentB)), List.of(pensionTypeCollection(deleteCaseDocumentB)),
                List.of(solUploadDocumentCollection(caseDocumentB)), null,
                List.of(deleteCaseDocumentB)),
            Arguments.of(
                List.of(solUploadDocumentCollection(deleteCaseDocumentA)), List.of(pensionTypeCollection(deleteCaseDocumentB)),
                List.of(solUploadDocumentCollection(caseDocumentB)), null,
                List.of(deleteCaseDocumentA, deleteCaseDocumentB))
        );
    }

    private static PensionTypeCollection pensionTypeCollection(CaseDocument file) {
        return PensionTypeCollection.builder()
            .typedCaseDocument(PensionType.builder().pensionDocument(file).build())
            .build();
    }

    private static SolUploadDocumentCollection solUploadDocumentCollection(CaseDocument file) {
        return SolUploadDocumentCollection.builder()
            .value(SolUploadDocument.builder().documentLink(file).build())
            .build();
    }

}
