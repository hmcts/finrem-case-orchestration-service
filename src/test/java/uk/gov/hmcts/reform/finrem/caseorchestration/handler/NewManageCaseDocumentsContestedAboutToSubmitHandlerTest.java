package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_MANAGE_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.ES1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.TRIAL_BUNDLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.WITHOUT_PREJUDICE_OFFERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class NewManageCaseDocumentsContestedAboutToSubmitHandlerTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private UploadedDocumentService uploadedDocumentService;
    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;
    @Mock
    private RespondentChronologiesStatementHandler documentHandlerOne = mock(RespondentChronologiesStatementHandler.class);
    @Mock
    private ApplicantOtherDocumentsHandler documentHandlerTwo = mock(ApplicantOtherDocumentsHandler.class);

    private List<DocumentHandler> documentHandlers;

    private NewManageCaseDocumentsContestedAboutToSubmitHandler underTest;

    @BeforeEach
    void setUp() {
        documentHandlers = List.of(documentHandlerOne, documentHandlerTwo);
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        underTest =
            new NewManageCaseDocumentsContestedAboutToSubmitHandler(finremCaseDetailsMapper,
                documentHandlers, uploadedDocumentService, evidenceManagementDeleteService,
                featureToggleService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, NEW_MANAGE_CASE_DOCUMENTS);
    }

    @Nested
    class WarningsPopulatedTests {

        @NullAndEmptySource
        @ParameterizedTest
        void givenNoInputDocuments_whenHandle_thenNoWarnings(
            List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection
        ) {
            FinremCaseData caseData = FinremCaseData.builder()
                .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                    .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                    .build())
                .build();

            // Act
            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
                .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

            // Verify
            assertThat(response.getWarnings()).isEmpty();
        }

        static Stream<Arguments> givenIntervenerSelectedAndNameMissing_whenHandle_thenWarningAdded() {
            return Stream.of(
                Arguments.of(INTERVENER_ONE, "Intervener 1"),
                Arguments.of(INTERVENER_TWO, "Intervener 2"),
                Arguments.of(INTERVENER_THREE, "Intervener 3"),
                Arguments.of(INTERVENER_FOUR, "Intervener 4")
            );
        }

        @ParameterizedTest
        @MethodSource
        void givenIntervenerSelectedAndNameMissing_whenHandle_thenWarningAdded(
            CaseDocumentParty caseDocumentParty,
            String intervenerIdentifier) {
            for (String nullOfEmptyIntervenerName : Arrays.asList("", null)) {
                FinremCaseData caseData = FinremCaseData.builder()
                    .intervenerOne(INTERVENER_ONE == caseDocumentParty
                        ? IntervenerOne.builder().intervenerName(nullOfEmptyIntervenerName).build() : null)
                    .intervenerTwo(INTERVENER_TWO == caseDocumentParty
                        ? IntervenerTwo.builder().intervenerName(nullOfEmptyIntervenerName).build() : null)
                    .intervenerThree(INTERVENER_THREE == caseDocumentParty
                        ? IntervenerThree.builder().intervenerName(nullOfEmptyIntervenerName).build() : null)
                    .intervenerFour(INTERVENER_FOUR == caseDocumentParty
                        ? IntervenerFour.builder().intervenerName(nullOfEmptyIntervenerName).build() : null)
                    .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                        .inputManageCaseDocumentCollection(List.of(
                            UploadCaseDocumentCollection.builder()
                                .uploadCaseDocument(UploadCaseDocument.builder()
                                    .caseDocumentParty(caseDocumentParty)
                                    .caseDocumentType(TRIAL_BUNDLE)
                                    .build())
                                .build()
                        ))
                        .build())
                    .build();

                GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
                    .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
                assertThat(response.getWarnings()).containsExactly("%s not present on the case, do you want to continue?"
                    .formatted(intervenerIdentifier));
            }
        }
    }

    @Test
    void shouldReplaceManagedDocumentsInCollectionType_whenHandled() {
        var inputFile1 = uploadDocument();
        var inputFile2 = uploadDocument();
        var inputs = List.of(inputFile1, inputFile2);
        var originalManageCaseDocumentCollection = new ArrayList<UploadCaseDocumentCollection>();

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .inputManageCaseDocumentCollection(inputs)
                .manageCaseDocumentCollection(originalManageCaseDocumentCollection)
                .build())
            .build();

        // Act
        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        // Verify
        verify(documentHandlerOne).replaceManagedDocumentsInCollectionType(eq(caseData),
            same(originalManageCaseDocumentCollection), eq(true));
        verify(documentHandlerTwo).replaceManagedDocumentsInCollectionType(eq(caseData),
            same(originalManageCaseDocumentCollection), eq(true));
        verifyNoMoreInteractions(documentHandlerOne, documentHandlerTwo);
        assertThat(originalManageCaseDocumentCollection).contains(inputFile1, inputFile2);
    }

    @Test
    void shouldAddUploadDateToNewDocuments_whenHandled() {
        FinremCaseData caseDataBefore = FinremCaseData.builder().build();
        FinremCaseData caseData = FinremCaseData.builder().build();

        underTest.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData),
            AUTH_TOKEN);

        verify(uploadedDocumentService).addUploadDateToNewDocuments(caseData, caseDataBefore);
        verifyNoMoreInteractions(uploadedDocumentService);
    }

    @Test
    void shouldClearLegacyCollections_whenHandled() {
        List<ConfidentialUploadedDocumentData> confidentialDocumentsUploaded = new ArrayList<>();

        FinremCaseData caseData = FinremCaseData.builder()
            .confidentialDocumentsUploaded(confidentialDocumentsUploaded)
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        verify(confidentialDocumentsUploaded).clear();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenDeleteToggleEnabled_whenHandle_thenDeletesOnlyRemovedDocuments(boolean featureToggleEnabled) {
        when(featureToggleService.isManageCaseDocsDeleteEnabled()).thenReturn(featureToggleEnabled);

        UploadCaseDocumentCollection removed = uploadDocument("removed.pdf");
        UploadCaseDocumentCollection retained = uploadDocument("retained.pdf");

        UploadCaseDocumentWrapper wrapperBefore = spy(UploadCaseDocumentWrapper.class);
        lenient().when(wrapperBefore.getAllManageableCollections()).thenReturn(new ArrayList<>(List.of(removed, retained)));

        UploadCaseDocumentWrapper wrapper = spy(UploadCaseDocumentWrapper.class);
        lenient().when(wrapper.getAllManageableCollections()).thenReturn(new ArrayList<>(List.of(retained)));

        FinremCaseData caseDataBefore = FinremCaseData.builder().uploadCaseDocumentWrapper(wrapperBefore).build();
        FinremCaseData caseData = FinremCaseData.builder().uploadCaseDocumentWrapper(wrapper).build();

        underTest.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData), AUTH_TOKEN);
        verify(evidenceManagementDeleteService, times(featureToggleEnabled ? 1 : 0))
            .delete(removed.getUploadCaseDocument().getCaseDocuments().getDocumentUrl(), AUTH_TOKEN);
    }

    @ParameterizedTest
    @EnumSource(value = CaseDocumentType.class, names = {"ATTENDANCE_SHEETS", "JUDICIAL_NOTES", "JUDGMENT",
        "WITNESS_SUMMONS", "TRANSCRIPT"})
    void shouldApplyDefault_whenHandle(CaseDocumentType caseDocumentType) {
        YesOrNo selectedConfidentiality = mock(YesOrNo.class);
        var input = uploadDocument(caseDocumentType, selectedConfidentiality);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .inputManageCaseDocumentCollection(List.of(input))
                .build())
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        UploadCaseDocument uploadCaseDocument = input.getUploadCaseDocument();
        assertThat(uploadCaseDocument)
            .extracting(
                UploadCaseDocument::getCaseDocumentParty,
                UploadCaseDocument::getCaseDocumentConfidentiality,
                UploadCaseDocument::getCaseDocumentFdr
            )
            .containsExactly(CASE, selectedConfidentiality, YesOrNo.NO);
    }

    // TODO

    @Test
    void givenWithoutPrejudiceOffersDocumentTypeSelected_whenHandleAddNewAction_thenDefaultsApplied() {
        UploadCaseDocumentCollection caseDocumentUploadedOne = null;
        UploadCaseDocumentCollection caseDocumentUploadedTwo = null;
        CaseDocumentParty caseDocumentPartyOne = mock(CaseDocumentParty.class);
        CaseDocumentParty caseDocumentPartyTwo = mock(CaseDocumentParty.class);

        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(
            caseDocumentUploadedOne = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(WITHOUT_PREJUDICE_OFFERS)
                    .caseDocumentParty(caseDocumentPartyOne)
                    .caseDocumentFdr(YesOrNo.NO)
                    .caseDocumentConfidentiality(YesOrNo.YES)
                    .build())
                .build(),
            caseDocumentUploadedTwo = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(WITHOUT_PREJUDICE_OFFERS)
                    .caseDocumentParty(caseDocumentPartyTwo)
                    .caseDocumentFdr(YesOrNo.NO)
                    .caseDocumentConfidentiality(YesOrNo.YES)
                    .build())
                .build()
        );

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.ADD_NEW)
                .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                .build())
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        UploadCaseDocument uploadCaseDocumentOne = caseDocumentUploadedOne.getUploadCaseDocument();
        assertThat(uploadCaseDocumentOne.getCaseDocumentParty()).isEqualTo(caseDocumentPartyOne);
        assertThat(uploadCaseDocumentOne.getCaseDocumentConfidentiality()).isEqualTo(YesOrNo.NO);
        assertThat(uploadCaseDocumentOne.getCaseDocumentFdr()).isEqualTo(YesOrNo.YES);

        UploadCaseDocument uploadCaseDocumentTwo = caseDocumentUploadedTwo.getUploadCaseDocument();
        assertThat(uploadCaseDocumentTwo.getCaseDocumentParty()).isEqualTo(caseDocumentPartyTwo);
        assertThat(uploadCaseDocumentTwo.getCaseDocumentConfidentiality()).isEqualTo(YesOrNo.NO);
        assertThat(uploadCaseDocumentTwo.getCaseDocumentFdr()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenOtherDocumentTypeSelected_whenHandleAddNewAction_thenDefaultsNotApplied() {
        UploadCaseDocumentCollection caseDocumentUploadedOne = null;
        UploadCaseDocumentCollection caseDocumentUploadedTwo = null;
        CaseDocumentParty caseDocumentPartyOne = mock(CaseDocumentParty.class);
        CaseDocumentParty caseDocumentPartyTwo = mock(CaseDocumentParty.class);

        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(
            caseDocumentUploadedOne = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(TRIAL_BUNDLE)
                    .caseDocumentParty(caseDocumentPartyOne)
                    .caseDocumentFdr(YesOrNo.NO)
                    .caseDocumentConfidentiality(YesOrNo.YES)
                    .build())
                .build(),
            caseDocumentUploadedTwo = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(ES1)
                    .caseDocumentParty(caseDocumentPartyTwo)
                    .caseDocumentFdr(YesOrNo.NO)
                    .caseDocumentConfidentiality(YesOrNo.YES)
                    .build())
                .build()
        );

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.ADD_NEW)
                .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                .build())
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        UploadCaseDocument uploadCaseDocumentOne = caseDocumentUploadedOne.getUploadCaseDocument();
        assertThat(uploadCaseDocumentOne.getCaseDocumentParty()).isEqualTo(caseDocumentPartyOne);
        assertThat(uploadCaseDocumentOne.getCaseDocumentConfidentiality()).isEqualTo(YesOrNo.YES);
        assertThat(uploadCaseDocumentOne.getCaseDocumentFdr()).isEqualTo(YesOrNo.NO);

        UploadCaseDocument uploadCaseDocumentTwo = caseDocumentUploadedTwo.getUploadCaseDocument();
        assertThat(uploadCaseDocumentTwo.getCaseDocumentParty()).isEqualTo(caseDocumentPartyTwo);
        assertThat(uploadCaseDocumentTwo.getCaseDocumentConfidentiality()).isEqualTo(YesOrNo.YES);
        assertThat(uploadCaseDocumentTwo.getCaseDocumentFdr()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void givenAmendAction_whenHandle_thenInputManageCaseDocumentCollectionReplacesManageCaseDocumentCollection() {

        UploadCaseDocumentCollection inputDocument = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentType(TRIAL_BUNDLE)
                .caseDocumentParty(CASE)
                .build())
            .build();

        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(inputDocument);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.AMEND)
                .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                .manageCaseDocumentCollection(new ArrayList<>())
                .build())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
        assertThat(response.getData().getManageCaseDocumentsWrapper().getManageCaseDocumentCollection())
            .isEqualTo(inputManageCaseDocumentCollection);
    }

    @Test
    void givenAmendAction_whenHandle_thenDocumentHandlersInvokedWithInputDocumentsAndAmendFlag() {
        UploadCaseDocumentCollection inputDocumentOne = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentType(TRIAL_BUNDLE)
                .caseDocumentParty(CASE)
                .build())
            .build();
        UploadCaseDocumentCollection inputDocumentTwo = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentType(ES1)
                .caseDocumentParty(CASE)
                .build())
            .build();

        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection =
            List.of(inputDocumentOne, inputDocumentTwo);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .manageCaseDocumentsActionSelection(ManageCaseDocumentsAction.AMEND)
                .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                .build())
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
        for (DocumentHandler documentHandler : documentHandlers) {
            verify(documentHandler).replaceManagedDocumentsInCollectionType(
                caseData,
                inputManageCaseDocumentCollection,
                true
            );
        }
    }

    private UploadCaseDocumentCollection uploadDocument() {
        return uploadDocument(null, null);
    }

    private UploadCaseDocumentCollection uploadDocument(CaseDocumentType documentType, YesOrNo confidentiality) {
        return UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentType(documentType)
                .caseDocumentConfidentiality(confidentiality)
                .build())
            .build();
    }

    private UploadCaseDocumentCollection uploadDocument(String filename) {
        return UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocuments(caseDocument(filename))
                .build())
            .build();
    }
}
