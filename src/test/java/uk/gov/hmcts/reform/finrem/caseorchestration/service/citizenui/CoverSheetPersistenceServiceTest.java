package uk.gov.hmcts.reform.finrem.caseorchestration.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class CoverSheetPersistenceServiceTest {

    private static final Long CASE_ID = 1234567890123456L;
    private static final String BIN_FIELD = "bin_fileUrlsCollection";

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private FeatureToggleService featureToggleService;

    private final FinremCaseDetailsMapper caseDetailsMapper =
        new FinremCaseDetailsMapper(createObjectMapper());

    private CoverSheetPersistenceService coverSheetPersistenceService;
    private FinremCaseDetails currentCaseDetails;
    private Map<String, Object> submittedUpdates;

    @BeforeEach
    void setUp() {
        coverSheetPersistenceService = new CoverSheetPersistenceService(
            coreCaseDataService, caseDetailsMapper, featureToggleService
        );
        currentCaseDetails = FinremCaseDetails.builder()
            .id(CASE_ID)
            .caseType(CONTESTED)
            .data(new FinremCaseData())
            .build();
    }

    @ParameterizedTest(name = "{0}: address confidential = {1}")
    @CsvSource({
        "APPLICANT, false",
        "APPLICANT, true",
        "RESPONDENT, false",
        "RESPONDENT, true"
    })
    void shouldSaveCoverSheetUsingLatestAddressConfidentiality(
        PaperNotificationRecipient recipient, boolean addressConfidential) {

        FinremCaseData latestCaseData = new FinremCaseData();
        setAddressConfidentiality(latestCaseData, recipient, addressConfidential);
        setExistingCoverSheets(
            latestCaseData, recipient, caseDocument("old-public"), caseDocument("old-confidential")
        );

        // The incoming case deliberately differs from the latest CCD case.
        setAddressConfidentiality(currentCaseDetails.getData(), recipient, !addressConfidential);
        setExistingCoverSheets(
            currentCaseDetails.getData(), recipient,
            caseDocument("stale-public"), caseDocument("stale-confidential")
        );

        PaperNotificationRecipient otherRecipient = recipient == APPLICANT ? RESPONDENT : APPLICANT;
        CaseDocument otherRecipientPublicCoverSheet = caseDocument("other-public");
        CaseDocument otherRecipientConfidentialCoverSheet = caseDocument("other-confidential");
        setExistingCoverSheets(
            currentCaseDetails.getData(), otherRecipient,
            otherRecipientPublicCoverSheet, otherRecipientConfidentialCoverSheet
        );

        CaseDocument newCoverSheet = caseDocument("new");
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, recipient, newCoverSheet);

        String publicFieldName = getPublicCoverSheetFieldName(recipient);
        String confidentialFieldName = getConfidentialCoverSheetFieldName(recipient);
        String populatedFieldName = addressConfidential ? confidentialFieldName : publicFieldName;
        String clearedFieldName = addressConfidential ? publicFieldName : confidentialFieldName;

        Object savedDocumentReference = caseDetailsMapper.finremCaseDataToMap(currentCaseDetails.getData())
            .get(populatedFieldName);

        assertThat(submittedUpdates).containsOnlyKeys(publicFieldName, confidentialFieldName);
        assertThat(savedDocumentReference).isNotNull();
        assertThat(submittedUpdates).containsEntry(populatedFieldName, savedDocumentReference);
        assertThat(submittedUpdates).containsEntry(clearedFieldName, null);

        assertThat(getPublicCoverSheet(currentCaseDetails.getData(), recipient))
            .isEqualTo(addressConfidential ? null : newCoverSheet);
        assertThat(getConfidentialCoverSheet(currentCaseDetails.getData(), recipient))
            .isEqualTo(addressConfidential ? newCoverSheet : null);

        assertThat(getPublicCoverSheet(currentCaseDetails.getData(), otherRecipient))
            .isSameAs(otherRecipientPublicCoverSheet);
        assertThat(getConfidentialCoverSheet(currentCaseDetails.getData(), otherRecipient))
            .isSameAs(otherRecipientConfidentialCoverSheet);
    }

    @ParameterizedTest(name = "{0}: address now confidential = {1}")
    @CsvSource({
        "APPLICANT, false",
        "APPLICANT, true",
        "RESPONDENT, false",
        "RESPONDENT, true"
    })
    void shouldMarkOldCoverSheetForDeletionWhenAddressConfidentialityChanges(
        PaperNotificationRecipient recipient, boolean addressConfidential) {

        FinremCaseData latestCaseData = new FinremCaseData();
        setAddressConfidentiality(latestCaseData, recipient, addressConfidential);

        CaseDocument oldCoverSheet = caseDocument("old");
        setExistingCoverSheets(
            latestCaseData, recipient,
            addressConfidential ? oldCoverSheet : null,
            addressConfidential ? null : oldCoverSheet
        );

        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, recipient, caseDocument("new"));

        assertThat(submittedUpdates).containsOnlyKeys(
            getPublicCoverSheetFieldName(recipient),
            getConfidentialCoverSheetFieldName(recipient),
            BIN_FIELD
        );
        assertThat(getDocumentUrlsMarkedForDeletion()).containsExactly(oldCoverSheet.getDocumentUrl());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = PaperNotificationRecipient.class, names = {"APPLICANT", "RESPONDENT"})
    void shouldPreserveExistingBinEntriesWhenAddingReplacedCoverSheets(PaperNotificationRecipient recipient) {
        FinremCaseData latestCaseData = new FinremCaseData();
        CaseDocument previouslyBinnedDocument = caseDocument("already-binned");
        CaseDocument oldPublicCoverSheet = caseDocument("old-public");
        CaseDocument oldConfidentialCoverSheet = caseDocument("old-confidential");

        latestCaseData.getBin().binCaseDocument(previouslyBinnedDocument);
        setExistingCoverSheets(latestCaseData, recipient, oldPublicCoverSheet, oldConfidentialCoverSheet);

        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, recipient, caseDocument("new"));

        assertThat(getDocumentUrlsMarkedForDeletion()).containsExactly(
            previouslyBinnedDocument.getDocumentUrl(),
            oldPublicCoverSheet.getDocumentUrl(),
            oldConfidentialCoverSheet.getDocumentUrl()
        );
    }

    @Test
    void shouldMarkOldCoverSheetForDeletionOnlyOnceWhenBothFieldsReferenceIt() {
        FinremCaseData latestCaseData = new FinremCaseData();
        CaseDocument oldCoverSheet = caseDocument("old");
        setExistingCoverSheets(latestCaseData, APPLICANT, oldCoverSheet, oldCoverSheet);

        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, caseDocument("new"));

        assertThat(getDocumentUrlsMarkedForDeletion()).containsExactly(oldCoverSheet.getDocumentUrl());
    }

    @Test
    void shouldNotMarkCoverSheetForDeletionWhenSavingTheSameDocument() {
        FinremCaseData latestCaseData = new FinremCaseData();
        CaseDocument existingCoverSheet = caseDocument("existing");
        setExistingCoverSheets(latestCaseData, APPLICANT, existingCoverSheet, null);

        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, existingCoverSheet);

        assertThat(submittedUpdates).doesNotContainKey(BIN_FIELD);
        assertThat(getPublicCoverSheet(currentCaseDetails.getData(), APPLICANT)).isEqualTo(existingCoverSheet);
    }

    @Test
    void shouldNotUpdateDeletionBinWhenDeletionToggleIsDisabled() {
        FinremCaseData latestCaseData = new FinremCaseData();
        setExistingCoverSheets(
            latestCaseData, APPLICANT, caseDocument("old-public"), caseDocument("old-confidential")
        );
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, caseDocument("new"));

        assertThat(submittedUpdates).doesNotContainKey(BIN_FIELD);
    }

    @Test
    void shouldNotUpdateDeletionBinWhenNoPreviousCoverSheetExists() {
        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(new FinremCaseData());

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, caseDocument("new"));

        assertThat(submittedUpdates).doesNotContainKey(BIN_FIELD);
    }

    @Test
    void shouldNotMarkOldCoverSheetsForDeletionWhenTheirUrlsAreMissingOrBlank() {
        FinremCaseData latestCaseData = new FinremCaseData();
        setExistingCoverSheets(
            latestCaseData, APPLICANT,
            CaseDocument.builder().build(),
            CaseDocument.builder().documentUrl(" ").build()
        );

        when(featureToggleService.isDeleteOldBpCoversheetEnabled()).thenReturn(true);
        stubSuccessfulCcdUpdate(latestCaseData);

        coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, caseDocument("new"));

        assertThat(submittedUpdates).doesNotContainKey(BIN_FIELD);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
        value = PaperNotificationRecipient.class,
        names = {"APPLICANT", "RESPONDENT"},
        mode = EnumSource.Mode.EXCLUDE
    )
    void shouldRejectRecipientsOtherThanApplicantOrRespondent(PaperNotificationRecipient recipient) {
        assertThrows(IllegalArgumentException.class,
            () -> coverSheetPersistenceService.saveCoverSheet(
                currentCaseDetails, recipient, caseDocument("new")
            ));

        verifyNoInteractions(coreCaseDataService, featureToggleService);
    }

    @Test
    void shouldRejectNullCoverSheet() {
        assertThrows(NullPointerException.class,
            () -> coverSheetPersistenceService.saveCoverSheet(currentCaseDetails, APPLICANT, null));

        verifyNoInteractions(coreCaseDataService, featureToggleService);
    }

    @Test
    void shouldKeepCurrentCoverSheetReferencesUnchangedWhenCcdUpdateFails() {
        CaseDocument existingCoverSheet = caseDocument("existing");
        setExistingCoverSheets(currentCaseDetails.getData(), APPLICANT, existingCoverSheet, null);
        IllegalStateException ccdFailure = new IllegalStateException("CCD update failed");

        when(coreCaseDataService.performPostSubmitCallback(
            eq(CONTESTED), eq(CASE_ID), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), any()
        )).thenThrow(ccdFailure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> coverSheetPersistenceService.saveCoverSheet(
                currentCaseDetails, APPLICANT, caseDocument("new")
            ));

        assertSame(ccdFailure, thrown);
        assertThat(getPublicCoverSheet(currentCaseDetails.getData(), APPLICANT)).isSameAs(existingCoverSheet);
        assertThat(getConfidentialCoverSheet(currentCaseDetails.getData(), APPLICANT)).isNull();
    }

    private void stubSuccessfulCcdUpdate(FinremCaseData latestCaseData) {
        CaseDetails latestCaseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .caseTypeId(CONTESTED.getCcdType())
            .data(new HashMap<>(caseDetailsMapper.finremCaseDataToMap(latestCaseData)))
            .build();

        when(coreCaseDataService.performPostSubmitCallback(
            eq(CONTESTED), eq(CASE_ID), eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), any()
        )).thenAnswer(invocation -> {
            Function<CaseDetails, Map<String, Object>> updateFunction = invocation.getArgument(3);
            submittedUpdates = updateFunction.apply(latestCaseDetails);

            // Mirror the CCD service's returned case data after applying the update.
            latestCaseDetails.getData().putAll(submittedUpdates);
            return latestCaseDetails;
        });
    }

    private List<String> getDocumentUrlsMarkedForDeletion() {
        FinremCaseData updatedCaseData = caseDetailsMapper.mapToFinremCaseData(
            submittedUpdates, CONTESTED.getCcdType()
        );
        return updatedCaseData.getBin().getFileUrlsToBeDeleted().stream()
            .map(item -> item.getValue().getBinFileUrl())
            .toList();
    }

    private void setAddressConfidentiality(FinremCaseData caseData,
                                           PaperNotificationRecipient recipient,
                                           boolean addressConfidential) {
        YesOrNo confidentialFlag = addressConfidential ? YesOrNo.YES : YesOrNo.NO;
        if (recipient == APPLICANT) {
            caseData.getContactDetailsWrapper().setApplicantAddressHiddenFromRespondent(confidentialFlag);
        } else {
            caseData.getContactDetailsWrapper().setRespondentAddressHiddenFromApplicant(confidentialFlag);
        }
    }

    private void setExistingCoverSheets(FinremCaseData caseData,
                                        PaperNotificationRecipient recipient,
                                        CaseDocument publicCoverSheet,
                                        CaseDocument confidentialCoverSheet) {
        if (recipient == APPLICANT) {
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(publicCoverSheet);
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(confidentialCoverSheet);
        } else {
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(publicCoverSheet);
            caseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(confidentialCoverSheet);
        }
    }

    private CaseDocument getPublicCoverSheet(FinremCaseData caseData, PaperNotificationRecipient recipient) {
        return recipient == APPLICANT
            ? caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp()
            : caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes();
    }

    private CaseDocument getConfidentialCoverSheet(FinremCaseData caseData, PaperNotificationRecipient recipient) {
        return recipient == APPLICANT
            ? caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential()
            : caseData.getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential();
    }

    private String getPublicCoverSheetFieldName(PaperNotificationRecipient recipient) {
        return recipient == APPLICANT ? "bulkPrintCoverSheetApp" : "bulkPrintCoverSheetRes";
    }

    private String getConfidentialCoverSheetFieldName(PaperNotificationRecipient recipient) {
        return recipient == APPLICANT
            ? "bulkPrintCoverSheetAppConfidential"
            : "bulkPrintCoverSheetResConfidential";
    }
}
