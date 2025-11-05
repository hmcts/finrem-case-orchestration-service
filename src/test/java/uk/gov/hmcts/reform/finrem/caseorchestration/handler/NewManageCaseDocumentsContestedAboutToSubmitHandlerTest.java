package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private List<DocumentHandler> allDocumentHandlers;
    private NewManageCaseDocumentsContestedAboutToSubmitHandler underTest;

    @BeforeEach
    void setUp() {
        allDocumentHandlers = List.of(documentHandlerOne, documentHandlerTwo);
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        underTest =
            new NewManageCaseDocumentsContestedAboutToSubmitHandler(finremCaseDetailsMapper,
                allDocumentHandlers, uploadedDocumentService, evidenceManagementDeleteService,
                featureToggleService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, NEW_MANAGE_CASE_DOCUMENTS);
    }

    @NullAndEmptySource
    @ParameterizedTest
    void givenInputManageCaseDocumentCollectionMissing_whenHandle_thenNoWarningsPopulated(
        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection
    ) {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .inputManageCaseDocumentCollection(inputManageCaseDocumentCollection)
                .build())
            .build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
            .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource
    void givenIntervenerPartySelectedAndIntervenerNameMissing_whenHandle_thenWarningPopulated(
        CaseDocumentParty caseDocumentParty,
        String intervenerIdentifier) {
        for (String intervenerName : Arrays.asList("", null)) {
            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(INTERVENER_ONE == caseDocumentParty
                    ? IntervenerOne.builder().intervenerName(intervenerName).build() : null)
                .intervenerTwo(INTERVENER_TWO == caseDocumentParty
                    ? IntervenerTwo.builder().intervenerName(intervenerName).build() : null)
                .intervenerThree(INTERVENER_THREE == caseDocumentParty
                    ? IntervenerThree.builder().intervenerName(intervenerName).build() : null)
                .intervenerFour(INTERVENER_FOUR == caseDocumentParty
                    ? IntervenerFour.builder().intervenerName(intervenerName).build() : null)
                .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                    .inputManageCaseDocumentCollection(List.of(
                        UploadCaseDocumentCollection.builder()
                            .uploadCaseDocument(UploadCaseDocument.builder()
                                .caseDocumentParty(caseDocumentParty).build())
                            .build()
                    ))
                    .build())
                .build();

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
                .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
            assertThat(response.getWarnings()).containsExactly(format("%s not present on the case, do you want to continue?",
                intervenerIdentifier));
        }
    }

    static Stream<Arguments> givenIntervenerPartySelectedAndIntervenerNameMissing_whenHandle_thenWarningPopulated() {
        return Stream.of(
            Arguments.of(INTERVENER_ONE, "Intervener 1"),
            Arguments.of(INTERVENER_TWO, "Intervener 2"),
            Arguments.of(INTERVENER_THREE, "Intervener 3"),
            Arguments.of(INTERVENER_FOUR, "Intervener 4")
        );
    }

    @Test
    void givenIntervenerPartiesSelectedAndIntervenerNameMissing_whenHandle_thenWarningsPopulated() {
        for (String intervenerName : Arrays.asList("", null)) {
            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder().intervenerName(intervenerName).build())
                .intervenerTwo(IntervenerTwo.builder().intervenerName(intervenerName).build())
                .intervenerThree(IntervenerThree.builder().intervenerName(intervenerName).build())
                .intervenerFour(IntervenerFour.builder().intervenerName(intervenerName).build())
                .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                    .inputManageCaseDocumentCollection(List.of(
                        UploadCaseDocumentCollection.builder()
                            .uploadCaseDocument(UploadCaseDocument.builder()
                                .caseDocumentParty(INTERVENER_ONE).build())
                            .build(),
                        UploadCaseDocumentCollection.builder()
                            .uploadCaseDocument(UploadCaseDocument.builder()
                                .caseDocumentParty(INTERVENER_THREE).build())
                            .build()
                    ))
                    .build())
                .build();

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
                .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
            assertThat(response.getWarnings()).containsExactly(
                "Intervener 1 not present on the case, do you want to continue?",
                "Intervener 3 not present on the case, do you want to continue?");
        }
    }

    @ParameterizedTest
    @EnumSource(value = CaseDocumentParty.class, names = {"INTERVENER_ONE", "INTERVENER_TWO",
        "INTERVENER_THREE", "INTERVENER_FOUR"})
    void givenIntervenerPartySelectedAndIntervenerNameIsNotEmpty_whenHandle_thenNoWarningsPopulated(
        CaseDocumentParty caseDocumentParty) {
        String intervenerName = "John James";
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(INTERVENER_ONE == caseDocumentParty
                ? IntervenerOne.builder().intervenerName(intervenerName).build() : null)
            .intervenerTwo(INTERVENER_TWO == caseDocumentParty
                ? IntervenerTwo.builder().intervenerName(intervenerName).build() : null)
            .intervenerThree(INTERVENER_THREE == caseDocumentParty
                ? IntervenerThree.builder().intervenerName(intervenerName).build() : null)
            .intervenerFour(INTERVENER_FOUR == caseDocumentParty
                ? IntervenerFour.builder().intervenerName(intervenerName).build() : null)
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .inputManageCaseDocumentCollection(List.of(
                    UploadCaseDocumentCollection.builder()
                        .uploadCaseDocument(UploadCaseDocument.builder()
                            .caseDocumentParty(caseDocumentParty).build())
                        .build()
                ))
                .build())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest
            .handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void givenSingleCaseDocumentUploaded_whenHandleAddNewAction_thenDocumentHandlersInvoked() {
        UploadCaseDocumentCollection singleCaseDocumentUploaded = null;
        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(
            singleCaseDocumentUploaded = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(mock(CaseDocumentType.class))
                    .caseDocumentParty(mock(CaseDocumentParty.class))
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

        for (DocumentHandler documentHandler : allDocumentHandlers) {
            verify(documentHandler).replaceManagedDocumentsInCollectionType(caseData, List.of(singleCaseDocumentUploaded),
                false);
        }
    }

    @Test
    void givenMultipleCaseDocumentsUploaded_whenHandleAddNewAction_thenDocumentHandlersInvoked() {
        UploadCaseDocumentCollection caseDocumentUploadedOne = null;
        UploadCaseDocumentCollection caseDocumentUploadedTwo = null;

        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(
            caseDocumentUploadedOne = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(mock(CaseDocumentType.class))
                    .caseDocumentParty(mock(CaseDocumentParty.class))
                    .build())
                .build(),
            caseDocumentUploadedTwo = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(mock(CaseDocumentType.class))
                    .caseDocumentParty(mock(CaseDocumentParty.class))
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

        for (DocumentHandler documentHandler : allDocumentHandlers) {
            verify(documentHandler).replaceManagedDocumentsInCollectionType(caseData, List.of(caseDocumentUploadedOne,
                caseDocumentUploadedTwo),false);
        }
    }

    @Test
    void givenAnyCase_whenHandle_thenShouldAddUploadDateToNewDocuments() {
        FinremCaseData caseDataBefore = FinremCaseData.builder().build();
        FinremCaseData caseData = FinremCaseData.builder().build();

        underTest.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData),
            AUTH_TOKEN);

        verify(uploadedDocumentService).addUploadDateToNewDocuments(caseData, caseDataBefore);
    }

    @Test
    void givenAnyCase_whenHandle_thenShouldClearLegacyCollections() {
        List<ConfidentialUploadedDocumentData> confidentialDocumentsUploaded = mock(List.class);

        FinremCaseData caseData = FinremCaseData.builder()
            .confidentialDocumentsUploaded(confidentialDocumentsUploaded)
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        verify(confidentialDocumentsUploaded).clear();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenSecuredDocToggle_whenHandle_thenDeleteRemovedDocumentsOnlyWhenSecureDocFeatureEnabled(boolean featureToggleEnabled) {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(featureToggleEnabled);

        CaseDocument removedDocument = caseDocument("removed.pdf");
        UploadCaseDocumentCollection removed = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocuments(removedDocument)
                .build())
            .build();
        UploadCaseDocumentCollection retained = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocuments(caseDocument("retained.pdf"))
                .build())
            .build();

        UploadCaseDocumentWrapper wrapperBefore = spy(UploadCaseDocumentWrapper.class);
        lenient().when(wrapperBefore.getAllManageableCollections()).thenReturn(new ArrayList<>(List.of(removed, retained)));

        UploadCaseDocumentWrapper wrapper = spy(UploadCaseDocumentWrapper.class);
        lenient().when(wrapper.getAllManageableCollections()).thenReturn(new ArrayList<>(List.of(retained)));

        FinremCaseData caseDataBefore = FinremCaseData.builder().uploadCaseDocumentWrapper(wrapperBefore).build();
        FinremCaseData caseData = FinremCaseData.builder().uploadCaseDocumentWrapper(wrapper).build();

        underTest.handle(FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseDataBefore, caseData), AUTH_TOKEN);
        verify(evidenceManagementDeleteService, times(featureToggleEnabled ? 1 : 0))
            .delete(removedDocument.getDocumentUrl(), AUTH_TOKEN);
    }

    @Test
    void givenAnyCase_whenHandle_thenClearTemporaryField() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageCaseDocumentsWrapper(ManageCaseDocumentsWrapper.builder()
                .inputManageCaseDocumentCollection(List.of())
                .build())
            .build();

        assertThat(underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN)
            .getData().getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection()
        ).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = CaseDocumentType.class, names = {"ATTENDANCE_SHEETS", "JUDICIAL_NOTES", "JUDGMENT",
        "WITNESS_SUMMONS", "TRANSCRIPT"})
    void givenAdministrativeCaseDocumentTypes_whenHandleAddNewAction_thenDefaultsApplied(CaseDocumentType caseDocumentType) {
        UploadCaseDocumentCollection singleCaseDocumentUploaded = null;
        List<UploadCaseDocumentCollection> inputManageCaseDocumentCollection = List.of(
            singleCaseDocumentUploaded = UploadCaseDocumentCollection.builder()
                .uploadCaseDocument(UploadCaseDocument.builder()
                    .caseDocumentType(caseDocumentType)
                    .caseDocumentParty(mock(CaseDocumentParty.class))
                    .caseDocumentFdr(YesOrNo.YES)
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

        UploadCaseDocument uploadCaseDocument = singleCaseDocumentUploaded.getUploadCaseDocument();
        assertThat(uploadCaseDocument.getCaseDocumentParty()).isEqualTo(CASE);
        assertThat(uploadCaseDocument.getCaseDocumentConfidentiality()).isEqualTo(YesOrNo.NO);
        assertThat(uploadCaseDocument.getCaseDocumentFdr()).isEqualTo(YesOrNo.NO);
    }

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
}
