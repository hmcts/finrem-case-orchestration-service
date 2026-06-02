package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentType;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorUploadDocumentMidHandlerTest {

    private static final CaseDocument caseDocumentA = caseDocument("A.doc");
    private static final CaseDocument caseDocumentB = caseDocument("B.doc");
    private static final CaseDocument caseDocumentC = caseDocument("C.doc");

    @InjectMocks
    private SolicitorUploadDocumentMidHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.SOLICITOR_UPLOAD_DOCUMENT);
    }

    @ParameterizedTest
    @MethodSource
    void givenBothCollectionEmpty_whenHandled_thenPopulateError(
        List<PensionTypeCollection> inputPensionTypeCollection,
        List<SolUploadDocumentCollection> inputSolUploadDocuments
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .pensionCollection(inputPensionTypeCollection)
            .solUploadDocuments(inputSolUploadDocuments)
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);

        assertAll(
            () -> assertEquals(finremCaseData, response.getData()),
            () -> assertThat(response.getErrors())
                .containsOnly("No documents have been uploaded. Please upload at least one document to continue.")
        );
    }

    static Stream<Arguments> givenBothCollectionEmpty_whenHandled_thenPopulateError() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(List.of(), null),
            Arguments.of(null, List.of()),
            Arguments.of(List.of(), List.of())
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenNoChangeOnCollection_whenHandled_thenPopulateError(
        List<PensionTypeCollection> inputPensionTypeCollection,
        List<SolUploadDocumentCollection> inputSolUploadDocuments,
        List<PensionTypeCollection> existingPensionTypeCollection,
        List<SolUploadDocumentCollection> existingSolUploadDocuments
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .pensionCollection(inputPensionTypeCollection)
            .solUploadDocuments(inputSolUploadDocuments)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .pensionCollection(existingPensionTypeCollection)
            .solUploadDocuments(existingSolUploadDocuments)
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData), AUTH_TOKEN);

        assertAll(
            () -> assertEquals(finremCaseData, response.getData()),
            () -> assertThat(response.getErrors())
                .containsOnly("You must upload at least one new document to continue.")
        );
    }

    static Stream<Arguments> givenNoChangeOnCollection_whenHandled_thenPopulateError() {
        return Stream.of(
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), null,
                List.of(pensionTypeCollection(caseDocumentA)), null),
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), List.of(solUploadDocumentCollection(caseDocumentB)),
                List.of(pensionTypeCollection(caseDocumentA)), List.of(solUploadDocumentCollection(caseDocumentB))
            ),
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA), pensionTypeCollection(caseDocumentB)), null,
                List.of(pensionTypeCollection(caseDocumentA), pensionTypeCollection(caseDocumentB)), null
            ),
            Arguments.of(
                null, List.of(solUploadDocumentCollection(caseDocumentA)),
                null, List.of(solUploadDocumentCollection(caseDocumentA))
            ),
            Arguments.of(
                null, List.of(solUploadDocumentCollection()),
                null, List.of(solUploadDocumentCollection())
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenChangeOnCollection_whenHandled_thenNoErrors(
        List<PensionTypeCollection> inputPensionTypeCollection,
        List<SolUploadDocumentCollection> inputSolUploadDocuments,
        List<PensionTypeCollection> existingPensionTypeCollection,
        List<SolUploadDocumentCollection> existingSolUploadDocuments
    ) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .pensionCollection(inputPensionTypeCollection)
            .solUploadDocuments(inputSolUploadDocuments)
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .pensionCollection(existingPensionTypeCollection)
            .solUploadDocuments(existingSolUploadDocuments)
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseDataBefore,
            finremCaseData), AUTH_TOKEN);

        assertAll(
            () -> assertEquals(finremCaseData, response.getData()),
            () -> assertThat(response.getErrors()).isEmpty()
        );
    }

    static Stream<Arguments> givenChangeOnCollection_whenHandled_thenNoErrors() {
        return Stream.of(
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), null,
                null, null),
            Arguments.of(
                null, List.of(solUploadDocumentCollection(caseDocumentA)),
                null, null),
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), null,
                List.of(), List.of()),
            Arguments.of(
                null, List.of(solUploadDocumentCollection(caseDocumentA)),
                List.of(), List.of()),
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), null,
                List.of(pensionTypeCollection(caseDocumentB)), null),
            Arguments.of(
                null, List.of(solUploadDocumentCollection(caseDocumentA)),
                null, List.of(solUploadDocumentCollection(caseDocumentB))),
            Arguments.of(
                List.of(pensionTypeCollection(caseDocumentA)), List.of(solUploadDocumentCollection(caseDocumentB)),
                List.of(pensionTypeCollection(caseDocumentA)), List.of(solUploadDocumentCollection(caseDocumentC))
            ),
            Arguments.of(
                null, null,
                null, List.of(solUploadDocumentCollection(caseDocumentC))
            )
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

    private static SolUploadDocumentCollection solUploadDocumentCollection() {
        return SolUploadDocumentCollection.builder()
            .value(SolUploadDocument.builder().documentType(SolUploadDocumentType.OTHER).build())
            .build();
    }
}
