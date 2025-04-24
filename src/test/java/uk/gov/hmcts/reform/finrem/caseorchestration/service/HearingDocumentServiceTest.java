package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FinremDateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.paymentDocumentCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXPRESS_CASE_PARTICIPATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.CourtDetailsMigration.SWANSEA_COURT_LIST;

public class HearingDocumentServiceTest extends BaseServiceTest {

    private static final String DATE_OF_HEARING = "2019-01-01";

    @Autowired
    private HearingDocumentService hearingDocumentService;
    @Autowired
    private DocumentConfiguration documentConfiguration;

    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    BulkPrintService bulkPrintService;
    @MockitoBean
    private ExpressCaseService expressCaseService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private PfdNcdrDocumentService pfdNcdrDocumentService;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormCAndOutOfFamilyCourtResolution() {
        boolean respondentDigital = false;
        mockPfdNcdrDocuments(respondentDigital);

        Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyPfdNcdrDocuments(result, respondentDigital);
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormCAndOutOfFamilyCourtResolution() {
        boolean respondentDigital = false;
        mockPfdNcdrDocuments(respondentDigital);
        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN,
            makeItJudiciaryFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyPfdNcdrDocuments(result, respondentDigital);
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormGAndOutOfFamilyCourtResolution() {
        boolean respondentDigital = false;
        mockPfdNcdrDocuments(respondentDigital);
        CaseDetails caseDetails = makeItNonFastTrackDecisionCase();
        setExpressCaseParticipant(caseDetails, ExpressCaseParticipation.DOES_NOT_QUALIFY);
        when(expressCaseService.isExpressCase(any(CaseDetails.class))).thenReturn(false);

        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(FORM_G));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyPfdNcdrDocuments(result, respondentDigital);
        verifyCommonNonFastTrackAndExpressCaseFields(false);
    }

    @Test
    public void generateExpressCaseFormCTrackFormCAndFormGAndOutOfFamilyCourtResolution() {
        boolean respondentDigital = false;
        mockPfdNcdrDocuments(respondentDigital);

        CaseDetails caseDetails = makeItNonFastTrackDecisionCase();
        setExpressCaseParticipant(caseDetails, ExpressCaseParticipation.ENROLLED);
        when(expressCaseService.isExpressCase(any(CaseDetails.class))).thenReturn(true);
        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(FORM_G));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyPfdNcdrDocuments(result, respondentDigital);
        verifyCommonNonFastTrackAndExpressCaseFields(true);
    }

    @Test
    public void givenRespondentDigital_thenPfdNcdrCoverLetterNotGenerated() {
        boolean respondentDigital = true;
        mockPfdNcdrDocuments(respondentDigital);

        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(FORM_G));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyPfdNcdrDocuments(result, respondentDigital);
        verifyCommonNonFastTrackAndExpressCaseFields(false);
    }

    @Test
    public void sendToBulkPrint() {
        FinremCaseDetails caseDetails = finremCaseDetails(YesOrNo.NO);

        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(5));
        bulkPrintDocumentsCaptor.getValue().forEach(obj -> assertThat(obj.getBinaryFileUrl(), is(BINARY_URL)));
    }

    @Test
    public void sendToBulkPrint_multipleFormA() {
        FinremCaseDetails caseDetails = finremCaseDetails(YesOrNo.YES);
        caseDetails.getData().setCopyOfPaperFormA(List.of(
            paymentDocumentCollection(), paymentDocumentCollection(), paymentDocumentCollection()));

        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, AUTH_TOKEN);

        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(7));
        bulkPrintDocumentsCaptor.getValue().forEach(obj -> assertThat(obj.getBinaryFileUrl(), is(BINARY_URL)));
    }

    @Test
    public void verifySwanseaCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            WALES, HEARING_WALES_FRC_LIST, SWANSEA, HEARING_SWANSEA_COURT_LIST, "FR_swansea_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Swansea Civil & Family Justice Centre", "Carvella House, Quay West, Quay Parade, Swansea, SA1 1SD",
            "0300 123 5577", "FRCswansea@justice.gov.uk");
    }

    @Test
    public void verifyNewportCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            WALES, HEARING_WALES_FRC_LIST, NEWPORT, HEARING_NEWPORT_COURT_LIST, "FR_newport_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Newport Civil and Family Court", "Clarence House, Clarence Place, Newport, NP19 7AA",
            "0300 123 5577", "FRCNewport@justice.gov.uk");
    }

    @Test
    public void verifyNoWalesFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            WALES, HEARING_SOUTHEAST_FRC_LIST, SWANSEA, HEARING_SWANSEA_COURT_LIST, "FR_swansea_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyKentCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            SOUTHEAST, HEARING_SOUTHEAST_FRC_LIST, KENT, HEARING_KENT_SURREY_COURT_LIST, "FR_kent_surrey_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Canterbury Family Court Hearing Centre", "The Law Courts, Chaucer Road, Canterbury, CT1 1ZA",
            "0300 123 5577", "Family.canterbury.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNoSouthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            SOUTHEAST, HEARING_WALES_FRC_LIST, KENT, HEARING_KENT_SURREY_COURT_LIST, "FR_kent_surrey_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCleavelandCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHEAST, HEARING_NORTHEAST_FRC_LIST, CLEAVELAND, HEARING_CLEAVELAND_COURT_LIST, "FR_cleaveland_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Newcastle Civil and Family Courts and Tribunals Centre", "Barras Bridge, Newcastle upon Tyne, NE18QF",
            "0300 123 5577", "Family.newcastle.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNwYorkshireCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHEAST, HEARING_NORTHEAST_FRC_LIST, NWYORKSHIRE, HEARING_NWYORKSHIRE_COURT_LIST, "FR_nw_yorkshire_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Harrogate Justice Centre", "The Court House, Victoria Avenue, Harrogate, HG1 1EL",
            "0300 123 5577", "enquiries.harrogate.countycourt@Justice.gov.uk");
    }

    @Test
    public void verifyHumberCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHEAST, HEARING_NORTHEAST_FRC_LIST, HSYORKSHIRE, HEARING_HUMBER_COURT_LIST, "FR_humber_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Sheffield Family Hearing Centre", "The Law Courts, 50 West Bar, Sheffield, S3 8PH",
            "0300 123 5577", "FRCSheffield@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHEAST, HEARING_NORTHWEST_FRC_LIST, HSYORKSHIRE, HEARING_NWYORKSHIRE_COURT_LIST, "FR_humber_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyLiverpoolCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHWEST, HEARING_NORTHWEST_FRC_LIST, LIVERPOOL, HEARING_LIVERPOOL_COURT_LIST, "FR_liverpool_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Liverpool Civil And Family Court", "35 Vernon Street, Liverpool, L2 2BX",
            "0300 123 5577", "FRCLiverpool@Justice.gov.uk");
    }

    @Test
    public void verifyManchesterCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHWEST, HEARING_NORTHWEST_FRC_LIST, MANCHESTER, HEARING_MANCHESTER_COURT_LIST, "FR_manchester_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Manchester County And Family Court", "1 Bridge Street West, Manchester, M60 9DJ",
            "0300 123 5577", "manchesterdivorce@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthWestFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            NORTHWEST, HEARING_NORTHEAST_FRC_LIST, MANCHESTER, HEARING_MANCHESTER_COURT_LIST, "FR_manchester_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCfcCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            LONDON, HEARING_LONDON_FRC_LIST, CFC, HEARING_CFC_COURT_LIST, "FR_s_CFCList_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Bromley County Court And Family Court", "Bromley County Court, College Road, Bromley, BR1 3PX",
            "0300 123 5577", "FRCLondon@justice.gov.uk");
    }

    @Test
    public void verifyNoLondonFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            LONDON, HEARING_MIDLANDS_FRC_LIST, CFC, HEARING_CFC_COURT_LIST, "FR_s_CFCList_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNottinghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            MIDLANDS, HEARING_MIDLANDS_FRC_LIST, NOTTINGHAM, HEARING_NOTTINGHAM_COURT_LIST, "FR_s_NottinghamList_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Nottingham County Court And Family Court", "60 Canal Street, Nottingham NG1 7EJ",
            "0300 123 5577", "FRCNottingham@justice.gov.uk");
    }

    @Test
    public void verifyBirminghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            MIDLANDS, HEARING_MIDLANDS_FRC_LIST, BIRMINGHAM, HEARING_BIRMINGHAM_COURT_LIST, "FR_birmingham_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFields(
            "Birmingham Civil And Family Justice Centre", "Priory Courts, 33 Bull Street, Birmingham, B4 6DS",
            "0300 123 5577", "FRCBirmingham@justice.gov.uk");
    }

    @Test
    public void verifyNoMidlandsFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            MIDLANDS, HEARING_LONDON_FRC_LIST, BIRMINGHAM, HEARING_BIRMINGHAM_COURT_LIST, "FR_birmingham_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourt() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            MIDLANDS, HEARING_MIDLANDS_FRC_LIST, BIRMINGHAM, HEARING_BIRMINGHAM_COURT_LIST, "invalid_court"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourtList() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithHearingCourtDetails(
            MIDLANDS, HEARING_MIDLANDS_FRC_LIST, BIRMINGHAM, NEWPORT_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNoRegionProvided() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails(NO_VALUE));

        verifyCommonNonFastTrackAndExpressCaseFields(false);

        verifyCourtDetailsFieldsNotSet();
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails(NO_VALUE);
    }

    private void setExpressCaseParticipant(CaseDetails caseDetails, ExpressCaseParticipation expressCaseParticipation) {
        caseDetails.getData().put(EXPRESS_CASE_PARTICIPATION, expressCaseParticipation.getValue());
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails(YES_VALUE);
    }

    private CaseDetails makeItJudiciaryFastTrackDecisionCase() {
        Map<String, Object> caseData =
            Map.of(FAST_TRACK_DECISION, NO_VALUE,
                CASE_ALLOCATED_TO, YES_VALUE, HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
    }

    private CaseDetails caseDetailsWithHearingCourtDetails(String region, String frcList, String frc, String courtList, String court) {
        Map<String, Object> caseData =
            Map.of(FAST_TRACK_DECISION, NO_VALUE, HEARING_DATE, DATE_OF_HEARING, HEARING_REGION_LIST,
                region, frcList, frc, courtList, court, REGION, WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURT_LIST,
                "FR_swansea_hc_list_1");
        return CaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
    }

    private FinremCaseDetails finremCaseDetails(YesOrNo fastTrackDecision) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .fastTrackDecision(fastTrackDecision)
                .listForHearingWrapper(ListForHearingWrapper.builder()
                    .hearingDate(LocalDate.parse(DATE_OF_HEARING, FinremDateUtils.getDateFormatter()))
                    .additionalListOfHearingDocuments(caseDocument())
                    .formC(caseDocument())
                    .formG(caseDocument())
                    .build())
                .copyOfPaperFormA(singletonList(paymentDocumentCollection()))
                .outOfFamilyCourtResolution(caseDocument())
                .build())
            .build();
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(FAST_TRACK_DECISION, isFastTrackDecision);
        caseData.put(HEARING_DATE, DATE_OF_HEARING);
        caseData.put(FORM_A_COLLECTION, singletonList(paymentDocumentCollection()));
        caseData.put(FORM_C, caseDocument());
        caseData.put(FORM_G, caseDocument());
        caseData.put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseData.put(HEARING_ADDITIONAL_DOC, caseDocument());

        return CaseDetails.builder()
            .id(12345L)
            .caseTypeId(CaseType.CONTESTED.getCcdType())
            .state(State.PREPARE_FOR_HEARING.getStateId())
            .data(caseData).build();
    }

    private void verifyAdditionalFastTrackFields() {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getFormCFastTrackTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never()).generateDocument(any(), any(),
            eq(documentConfiguration.getFormCNonFastTrackTemplate(CaseDetails.builder().build())), any());
        verify(genericDocumentService, never()).generateDocument(any(), any(),
            eq(documentConfiguration.getFormGTemplate(CaseDetails.builder().build())), any());

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("eventDatePlus21Days"), is(notNullValue()));
    }

    private void verifyCourtDetailsFields(String courtName, String courtAddress, String phone, String email) {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        @SuppressWarnings("unchecked")
        Map<String, Object> courtDetails = (Map<String, Object>) data.get("courtDetails");
        assertThat(courtDetails.get(HEARING_COURT_DETAILS_NAME_KEY), is(courtName));
        assertThat(courtDetails.get(HEARING_COURT_DETAILS_ADDRESS_KEY), is(courtAddress));
        assertThat(courtDetails.get(HEARING_COURT_DETAILS_EMAIL_KEY), is(email));
        assertThat(courtDetails.get(HEARING_COURT_DETAILS_PHONE_KEY), is(phone));
    }

    private void verifyCourtDetailsFieldsNotSet() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertTrue(ObjectUtils.isEmpty(data.get("courtDetails")));
    }

    private void verifyCommonNonFastTrackAndExpressCaseFields(boolean isExpressCase) {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(isExpressCase ? documentConfiguration.getFormCExpressCaseTemplate() :
                documentConfiguration.getFormCNonFastTrackTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getFormCFileName()));

        verify(genericDocumentService, never())
            .generateDocument(any(), any(),
                eq(documentConfiguration.getFormCFastTrackTemplate(CaseDetails.builder().build())), any());

        verify(genericDocumentService)
            .generateDocument(eq(AUTH_TOKEN), any(), eq(documentConfiguration.getFormGTemplate(CaseDetails.builder().build())),
                eq(documentConfiguration.getFormGFileName()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();

        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("hearingDateLess35Days"), is(notNullValue()));
        assertThat(data.get("hearingDateLess14Days"), is(notNullValue()));
    }

    private void mockPfdNcdrDocuments(boolean respondentDigital) {
        when(pfdNcdrDocumentService.isPdfNcdrCoverSheetRequired(any(CaseDetails.class))).thenReturn(!respondentDigital);

        CaseDocument pfdNcdrComplianceLetter = createDocument("pfdNcdrComplianceLetter.pdf");
        when(pfdNcdrDocumentService.uploadPfdNcdrComplianceLetter(any(), any())).thenReturn(pfdNcdrComplianceLetter);
        CaseDocument pfdNcdrCoverLetter = createDocument("pfdNcdrCoverLetter.pdf");
        when(pfdNcdrDocumentService.uploadPfdNcdrCoverLetter(any(), any())).thenReturn(pfdNcdrCoverLetter);
    }

    private CaseDocument createDocument(String filename) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentFilename(filename);
        return caseDocument;
    }

    private void verifyPfdNcdrDocuments(Map<String, CaseDocument> result, boolean respondentDigital) {
        assertThat(result.get(PFD_NCDR_COMPLIANCE_LETTER).getDocumentFilename(), is("pfdNcdrComplianceLetter.pdf"));
        if (respondentDigital) {
            verify(pfdNcdrDocumentService, never()).uploadPfdNcdrCoverLetter(any(), any());
            assertThat(result.get(PFD_NCDR_COVER_LETTER), is(nullValue()));
        } else {
            assertThat(result.get(PFD_NCDR_COVER_LETTER).getDocumentFilename(), is("pfdNcdrCoverLetter.pdf"));
        }
    }
}
