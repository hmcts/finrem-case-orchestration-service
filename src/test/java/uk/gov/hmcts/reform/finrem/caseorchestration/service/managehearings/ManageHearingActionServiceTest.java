package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ManageHearingActionServiceTest {

    private static final String HEARING_NOTICE_URL = "http://example.com/hearing-notice";
    private static final String HEARING_NOTICE_FILENAME = "HearingNotice.pdf";

    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;
    @Mock
    private ExpressCaseService expressCaseService;
    @Mock
    private HearingTabDataMapper hearingTabDataMapper;

    @InjectMocks
    private ManageHearingActionService manageHearingActionService;

    private FinremCaseDetails finremCaseDetails;
    private ManageHearingsWrapper hearingWrapper;
    private WorkingHearing workingHearing;

    @BeforeEach
    void setUp() {
        workingHearing = createWorkingHearing(LocalDate.now());
        hearingWrapper = ManageHearingsWrapper.builder().workingHearing(workingHearing).build();
        finremCaseDetails = FinremCaseDetails.builder()
            .data(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData.builder()
                .manageHearingsWrapper(hearingWrapper)
                .build())
            .build();
    }

    private WorkingHearing createWorkingHearing(LocalDate date) {
        return WorkingHearing.builder()
            .hearingTypeDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(HearingType.DIR.name())
                    .label(HearingType.DIR.getId())
                    .build())
                .build())
            .hearingDate(date)
            .hearingTime("10:00")
            .hearingTimeEstimate("30mins")
            .build();
    }

    @Test
    void performAddHearing_shouldAddHearingAndGenerateHearingNotice() {
        CaseDocument hearingNotice = createCaseDocument(HEARING_NOTICE_FILENAME, HEARING_NOTICE_URL);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(hearingNotice);

        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        assertThat(hearingWrapper.getHearings()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection().getFirst().getValue().getHearingDocument())
            .isEqualTo(hearingNotice);
    }

    @Test
    void performAddHearing_shouldGenerateAllDocumentsForStandardFdaHearingType() {
        workingHearing.setHearingTypeDynamicList(DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(HearingType.FDA.name())
                .label(HearingType.FDA.getId())
                .build())
            .build());

        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument formG = createCaseDocument("FormG.pdf", "http://example.com/form-g");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");

        Map<String, CaseDocument> pfdNcdrDocuments = Map.of(
            PFD_NCDR_COMPLIANCE_LETTER, createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"),
            PFD_NCDR_COVER_LETTER, createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(formC);
        when(manageHearingsDocumentService.generateFormG(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(formG);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(createCaseDocument(HEARING_NOTICE_FILENAME, HEARING_NOTICE_URL));

        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC, formG,
                pfdNcdrDocuments.get(PFD_NCDR_COMPLIANCE_LETTER),
                pfdNcdrDocuments.get(PFD_NCDR_COVER_LETTER),
                outOfCourtResolution);
    }

    @Test
    void performAddHearing_shouldGenerateDocumentsForFdrHearingTypeWithExpressCase() {
        workingHearing.setHearingTypeDynamicList(DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(HearingType.FDR.name())
                .label(HearingType.FDR.getId())
                .build())
            .build());
        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument formG = createCaseDocument("FormG.pdf", "http://example.com/form-g");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");

        Map<String, CaseDocument> pfdNcdrDocuments = Map.of(
            PFD_NCDR_COMPLIANCE_LETTER, createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"),
            PFD_NCDR_COVER_LETTER, createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(formC);
        when(manageHearingsDocumentService.generateFormG(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(formG);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(createCaseDocument("HearingNotice.pdf",
            "http://example.com/hearing-notice"));
        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);

        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC, formG,
                pfdNcdrDocuments.get(PFD_NCDR_COMPLIANCE_LETTER),
                pfdNcdrDocuments.get(PFD_NCDR_COVER_LETTER),
                outOfCourtResolution);
    }

    @Test
    void performAddHearing_shouldNotGenerateFormGForFastTrackApplication() {
        workingHearing.setHearingTypeDynamicList(DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(HearingType.FDA.name())
                .label(HearingType.FDA.getId())
                .build())
            .build());
        finremCaseDetails.getData().setFastTrackDecision(YesOrNo.YES);

        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");
        Map<String, CaseDocument> pfdNcdrDocuments = Map.of(
            PFD_NCDR_COMPLIANCE_LETTER, createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"),
            PFD_NCDR_COVER_LETTER, createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(formC);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            AUTH_TOKEN)).thenReturn(createCaseDocument("HearingNotice.pdf",
            "http://example.com/hearing-notice"));

        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(5);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC,
                pfdNcdrDocuments.get(PFD_NCDR_COMPLIANCE_LETTER),
                pfdNcdrDocuments.get(PFD_NCDR_COVER_LETTER),
                outOfCourtResolution);

        verify(manageHearingsDocumentService, never()).generateFormG(finremCaseDetails, AUTH_TOKEN);
    }

    @Test
    void updateTabData_shouldAddHearingToTabCollectionInCorrectOrder() {
        Hearing hearing1 = createHearing(HearingType.DIR, "10:00", "30mins", LocalDate.of(2025, 7, 20));
        hearing1.setPartiesOnCaseMultiSelectList(DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder()
                    .label("Applicant")
                    .code(APPLICANT)
                    .build()))
            .build()
        );

        Hearing hearing2 = createHearing(HearingType.FDA, "11:00", "1hr", LocalDate.of(2025, 7, 15));
        hearing2.setPartiesOnCaseMultiSelectList(DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder()
                    .label("Respondent")
                    .code(RESPONDENT)
                    .build()))
            .build()
        );

        hearingWrapper.setHearings(new ArrayList<>(List.of(
            createHearingCollectionItem(hearing2),
            createHearingCollectionItem(hearing1)
        )));

        when(hearingTabDataMapper.mapHearingToTabData(any(), any()))
            .thenReturn(createHearingTabItem("Respondent Hearing", "15 Jul 2025 11:00", APPLICANT),
                createHearingTabItem("Applicant Hearing", "20 Jul 2025 10:00", RESPONDENT));

        manageHearingActionService.updateTabData(finremCaseDetails.getData());

        List<HearingTabCollectionItem> hearingTabItems = hearingWrapper.getHearingTabItems();
        assertThat(hearingTabItems)
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("15 Jul 2025 11:00", "20 Jul 2025 10:00");
    }

    @Test
    void updateTabData_shouldAddHearingToTabCollectionInCorrectOrderWhenManageHearingsMigrationHadDone() {
        Hearing hearing1 = createHearing(HearingType.DIR, "10:00", "30mins", LocalDate.of(2025, 7, 20));
        hearing1.setPartiesOnCaseMultiSelectList(DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder()
                    .label("Applicant")
                    .code(APPLICANT)
                    .build()))
            .build()
        );

        Hearing hearing2 = createHearing(HearingType.FDA, "11:00", "1hr", LocalDate.of(2025, 7, 15));
        hearing2.setPartiesOnCaseMultiSelectList(DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder()
                    .label("Respondent")
                    .code(RESPONDENT)
                    .build()))
            .build()
        );

        Hearing hearing3 = createHearing(HearingType.FDA, "11:00", "1hr", LocalDate.of(2025, 7, 1));
        hearing3.setPartiesOnCaseMultiSelectList(DynamicMultiSelectList
            .builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder()
                    .label("INTERVENER")
                    .code(INTERVENER1)
                    .build()))
            .build()
        );

        Hearing migratedHearing1 = createHearing(HearingType.FDA, "11:00", "1hr", LocalDate.of(2025, 7, 10), true);

        hearingWrapper.setHearings(new ArrayList<>(List.of(
            createHearingCollectionItem(migratedHearing1),
            createHearingCollectionItem(hearing3),
            createHearingCollectionItem(hearing2),
            createHearingCollectionItem(hearing1)
        )));
        hearingWrapper.setHearingTabItems(List.of(HearingTabCollectionItem.builder()
            .value(HearingTabItem.builder()
                .tabDateTime("10 Jul 2025 10:00")
                .tabWasMigrated(YesOrNo.YES)
                .build()).build()));

        when(hearingTabDataMapper.mapHearingToTabData(argThat(hasHearing(hearing1)), any()))
            .thenReturn(
                createHearingTabItem("Applicant Hearing 1", "20 Jul 2025 10:00", APPLICANT)
            );
        when(hearingTabDataMapper.mapHearingToTabData(argThat(hasHearing(hearing2)), any()))
            .thenReturn(
                createHearingTabItem("Applicant Hearing 2", "15 Jul 2025 10:00", RESPONDENT)
            );
        when(hearingTabDataMapper.mapHearingToTabData(argThat(hasHearing(hearing3)), any()))
            .thenReturn(
                createHearingTabItem("Applicant Hearing 3", "1 Jul 2025 10:00", INTERVENER1)
            );
        // migrated hearing will not be populated to hearing tab item, therefore `lenient()` is needed.
        lenient()
            .when(hearingTabDataMapper.mapHearingToTabData(argThat(hasHearing(migratedHearing1)), any()))
            .thenReturn(
                createHearingTabItem("Applicant Hearing 3", "10 Jul 2025 10:00", null, true)
            );
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(hearingWrapper)
            .build();

        // Act
        manageHearingActionService.updateTabData(caseData);

        assertThat(hearingWrapper.getHearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("1 Jul 2025 10:00", "10 Jul 2025 10:00", "15 Jul 2025 10:00", "20 Jul 2025 10:00");
        assertThat(hearingWrapper.getApplicantHearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("10 Jul 2025 10:00", "20 Jul 2025 10:00");
        assertThat(hearingWrapper.getRespondentHearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("10 Jul 2025 10:00", "15 Jul 2025 10:00");
        assertThat(hearingWrapper.getInt1HearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("1 Jul 2025 10:00", "10 Jul 2025 10:00");
        assertThat(hearingWrapper.getInt2HearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("10 Jul 2025 10:00");
        assertThat(hearingWrapper.getInt3HearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("10 Jul 2025 10:00");
        assertThat(hearingWrapper.getInt4HearingTabItems())
            .extracting(item -> item.getValue().getTabDateTime())
            .containsExactly("10 Jul 2025 10:00");
    }

    @Test
    void updateTabData_shouldNotPopulateHearingTabItemsIfEmptyHearingsProvided() {
        hearingWrapper.setHearings(null);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(hearingWrapper)
            .build();

        // Act
        manageHearingActionService.updateTabData(caseData);

        assertThat(hearingWrapper.getHearingTabItems()).isNull();
        assertThat(hearingWrapper.getApplicantHearingTabItems()).isNull();
        assertThat(hearingWrapper.getRespondentHearingTabItems()).isNull();
        assertThat(hearingWrapper.getInt1HearingTabItems()).isNull();
        assertThat(hearingWrapper.getInt2HearingTabItems()).isNull();
        assertThat(hearingWrapper.getInt3HearingTabItems()).isNull();
        assertThat(hearingWrapper.getInt4HearingTabItems()).isNull();
    }

    private ArgumentMatcher<ManageHearingsCollectionItem> hasHearing(Hearing expected) {
        return entry -> entry != null && expected.equals(entry.getValue());
    }

    private Hearing createHearing(HearingType type, String time, String estimate, LocalDate date) {
        return createHearing(type, time, estimate, date, false);
    }

    private Hearing createHearing(HearingType type, String time, String estimate, LocalDate date, boolean migrated) {
        return Hearing.builder()
            .hearingType(type)
            .hearingDate(date)
            .hearingTime(time)
            .hearingTimeEstimate(estimate)
            .wasMigrated(migrated ? YesOrNo.YES : null)
            .build();
    }

    private CaseDocument createCaseDocument(String filename, String url) {
        return CaseDocument.builder()
            .documentFilename(filename)
            .documentUrl(url)
            .build();
    }

    private ManageHearingsCollectionItem createHearingCollectionItem(Hearing hearing) {
        return ManageHearingsCollectionItem.builder()
            .id(UUID.randomUUID())
            .value(hearing)
            .build();
    }

    private HearingTabItem createHearingTabItem(String type, String dateTime, String parties) {
        return createHearingTabItem(type, dateTime, parties, false);
    }

    private HearingTabItem createHearingTabItem(String type, String dateTime, String parties, boolean migrated) {
        return HearingTabItem.builder()
            .tabHearingType(type)
            .tabDateTime(dateTime)
            .tabConfidentialParties(parties)
            .tabWasMigrated(migrated ? YesOrNo.YES : null)
            .build();
    }
}
