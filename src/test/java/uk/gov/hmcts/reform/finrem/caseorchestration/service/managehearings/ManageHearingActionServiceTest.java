package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
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
    private Hearing hearing;

    @BeforeEach
    void setUp() {
        hearing = createHearing(HearingType.DIR, "10:00", "30mins", LocalDate.now());
        hearingWrapper = ManageHearingsWrapper.builder().workingHearing(hearing).build();
        finremCaseDetails = FinremCaseDetails.builder()
            .data(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData.builder()
                .manageHearingsWrapper(hearingWrapper)
                .build())
            .build();
    }

    @Test
    void performAddHearing_shouldAddHearingAndGenerateHearingNotice() {
        CaseDocument hearingNotice = createCaseDocument(HEARING_NOTICE_FILENAME, HEARING_NOTICE_URL);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, "AUTH_TOKEN"))
            .thenReturn(hearingNotice);

        manageHearingActionService.performAddHearing(finremCaseDetails, "AUTH_TOKEN");

        assertThat(hearingWrapper.getHearings()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection().get(0).getValue().getHearingDocument())
            .isEqualTo(hearingNotice);
    }

    @Test
    void performAddHearing_shouldGenerateAllDocumentsForStandardFdaHearingType() {
        hearing.setHearingType(HearingType.FDA);

        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument formG = createCaseDocument("FormG.pdf", "http://example.com/form-g");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");

        Map<String, Pair<CaseDocument, CaseDocumentType>>  pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", Pair.of(createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"), CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER),
            "PFD_NCDR_COVER_LETTER",  Pair.of(createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"), CaseDocumentType.PFD_NCDR_COVER_LETTER));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(formC);
        when(manageHearingsDocumentService.generateFormG(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(formG);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(createCaseDocument(HEARING_NOTICE_FILENAME, HEARING_NOTICE_URL));

        manageHearingActionService.performAddHearing(finremCaseDetails, "AUTH_TOKEN");

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC, formG,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER").getLeft(),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER").getLeft(),
                outOfCourtResolution);
    }

    @Test
    void performAddHearing_shouldGenerateDocumentsForFdrHearingTypeWithExpressCase() {
        hearing.setHearingType(HearingType.FDR);

        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument formG = createCaseDocument("FormG.pdf", "http://example.com/form-g");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");

        Map<String, Pair<CaseDocument, CaseDocumentType>>  pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", Pair.of(createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"), CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER),
            "PFD_NCDR_COVER_LETTER",  Pair.of(createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"), CaseDocumentType.PFD_NCDR_COVER_LETTER));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(formC);
        when(manageHearingsDocumentService.generateFormG(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(formG);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(createCaseDocument("HearingNotice.pdf",
            "http://example.com/hearing-notice"));
        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);

        manageHearingActionService.performAddHearing(finremCaseDetails, "AUTH_TOKEN");

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC, formG,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER").getLeft(),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER").getLeft(),
                outOfCourtResolution);
    }

    @Test
    void performAddHearing_shouldNotGenerateFormGForFastTrackApplication() {
        hearing.setHearingType(HearingType.FDA);
        finremCaseDetails.getData().setFastTrackDecision(YesOrNo.YES);

        CaseDocument formC = createCaseDocument("FormC.pdf", "http://example.com/form-c");
        CaseDocument outOfCourtResolution = createCaseDocument("OutOfCourtResolution.pdf",
            "http://example.com/OutOfCourtResolution");
        Map<String, Pair<CaseDocument, CaseDocumentType>>  pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", Pair.of(createCaseDocument("ComplianceLetter.pdf",
                "http://example.com/compliance-letter"), CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER),
            "PFD_NCDR_COVER_LETTER",  Pair.of(createCaseDocument("CoverLetter.pdf",
                "http://example.com/cover-letter"), CaseDocumentType.PFD_NCDR_COVER_LETTER));

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(formC);
        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(pfdNcdrDocuments);
        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(outOfCourtResolution);
        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails,
            "AUTH_TOKEN")).thenReturn(createCaseDocument("HearingNotice.pdf",
            "http://example.com/hearing-notice"));

        manageHearingActionService.performAddHearing(finremCaseDetails, "AUTH_TOKEN");

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(5);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item -> item.getValue().getHearingDocument())
            .contains(formC,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER").getLeft(),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER").getLeft(),
                outOfCourtResolution);

        verify(manageHearingsDocumentService, never()).generateFormG(finremCaseDetails, "AUTH_TOKEN");
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
        assertThat(hearingTabItems).hasSize(2);
        assertThat(hearingTabItems.get(0).getValue().getTabDateTime()).isEqualTo("15 Jul 2025 11:00");
        assertThat(hearingTabItems.get(1).getValue().getTabDateTime()).isEqualTo("20 Jul 2025 10:00");
    }

    private Hearing createHearing(HearingType type, String time, String estimate, LocalDate date) {
        return Hearing.builder()
            .hearingType(type)
            .hearingDate(date)
            .hearingTime(time)
            .hearingTimeEstimate(estimate)
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
        return HearingTabItem.builder()
            .tabHearingType(type)
            .tabDateTime(dateTime)
            .tabConfidentialParties(parties)
            .build();
    }
}
