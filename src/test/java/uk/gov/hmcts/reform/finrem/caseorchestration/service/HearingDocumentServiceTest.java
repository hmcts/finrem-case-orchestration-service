package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.CfcCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HumberCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.NewportCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.paymentDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;

public class HearingDocumentServiceTest extends BaseServiceTest {

    private static final LocalDate DATE_OF_HEARING = LocalDate.of(2019, 01, 01);

    @Autowired private HearingDocumentService hearingDocumentService;
    @Autowired private DocumentConfiguration documentConfiguration;

    @MockBean private GenericDocumentService genericDocumentService;
    @MockBean BulkPrintService bulkPrintService;

    @Captor private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintDocumentsCaptor;
    @Captor private ArgumentCaptor<Map<String,Object>> placeholdersCaptor;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(), any(), any()))
            .thenReturn(newDocument());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseData(new FinremCaseData()).build();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());
        assertCaseDocument((Document) result.get(FORM_C));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN,
            makeItJudiciaryFastTrackDecisionCase());
        assertCaseDocument((Document) result.get(FORM_C));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormG() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
        assertCaseDocument((Document) result.get(FORM_C));
        assertCaseDocument((Document) result.get(FORM_G));
        verifyAdditionalNonFastTrackFields();
    }

    @Test
    public void sendToBulkPrint() {
        FinremCaseDetails caseDetails = caseDetails(NO_VALUE);

        hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(3));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(2).getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void sendToBulkPrint_multipleFormA() {
        FinremCaseDetails caseDetails = caseDetails(YES_VALUE);
        caseDetails.getCaseData().setCopyOfPaperFormA(asList(paymentDocumentData(), paymentDocumentData(), paymentDocumentData()));

        hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(5));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(2).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(3).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(4).getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void verifySwanseaCourtDetails()  {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.WALES);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setWalesFrcList(RegionWalesFrc.SWANSEA);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setSwanseaCourtList(SwanseaCourt.FR_swanseaList_1);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Swansea Civil & Family Justice Centre", "Carvella House, Quay West, Quay Parade, Swansea, SA1 1SD",
            "01792 485 800", "FRCswansea@justice.gov.uk");
    }

    @Test
    public void verifyNewportCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.WALES);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setWalesFrcList(RegionWalesFrc.NEWPORT);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setNewportCourtList(NewportCourt.FR_newportList_1);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newport Civil and Family Court", "Clarence House, Clarence Place, Newport, NP19 7AA",
            "01633 245 040", "FRCNewport@justice.gov.uk");
    }

    @Test
    public void verifyKentCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.SOUTHEAST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setSouthEastFrcList(RegionSouthEastFrc.KENT);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setKentSurreyCourtList(KentSurreyCourt.FR_kent_surreyList_1);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Canterbury Family Court Hearing Centre", "The Law Courts, Chaucer Road, Canterbury, CT1 1ZA",
            "01634 887900", "FRCKSS@justice.gov.uk");
    }

    @Test
    public void verifyCleavelandCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHEAST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setNorthEastFrcList(RegionNorthEastFrc.CLEVELAND);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setCleavelandCourtList(ClevelandCourt.FR_CLEVELAND_LIST_1);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newcastle Upon Tyne Justice Centre", "Barras Bridge, Newcastle upon Tyne, NE18QF",
            "0191 2012000", "Family.newcastle.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNwYorkshireCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHEAST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper()
            .setNorthEastFrcList(RegionNorthEastFrc.NW_YORKSHIRE);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setNwYorkshireCourtList(NwYorkshireCourt.HARROGATE_COURT);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Harrogate Justice Centre", "The Court House, Victoria Avenue, Harrogate, HG1 1EL",
            "0113 306 2501", "leedsfamily@justice.gov.uk");
    }

    @Test
    public void verifyHumberCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHEAST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper()
            .setNorthEastFrcList(RegionNorthEastFrc.HS_YORKSHIRE);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setHumberCourtList(HumberCourt.FR_humberList_1);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Sheffield Family Hearing Centre", "The Law Courts, 50 West Bar, Sheffield, S3 8PH",
            "0114 2812522", "FRCSheffield@justice.gov.uk");
    }

    @Test
    public void verifyLiverpoolCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHWEST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setNorthWestFrcList(RegionNorthWestFrc.LIVERPOOL);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setLiverpoolCourtList(LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Liverpool Civil And Family Court", "35 Vernon Street, Liverpool, L2 2BX",
            "0151 296 2225", "FRCLiverpool@Justice.gov.uk");
    }

    @Test
    public void verifyManchesterCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHWEST);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setNorthWestFrcList(RegionNorthWestFrc.MANCHESTER);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setManchesterCourtList(ManchesterCourt.MANCHESTER_COURT);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Manchester County And Family Court", "1 Bridge Street West, Manchester, M60 9DJ",
            "0161 240 5430", "manchesterdivorce@justice.gov.uk");
    }

    @Test
    public void verifyCfcCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.LONDON);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setLondonFrcList(RegionLondonFrc.LONDON);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setCfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Bromley County Court And Family Court", "Bromley County Court, College Road, Bromley, BR1 3PX",
            "0208 290 9620", "family.bromley.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNottinghamCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.MIDLANDS);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Nottingham County Court And Family Court", "60 Canal Street, Nottingham NG1 7EJ",
            "0115 910 3504", "FRCNottingham@justice.gov.uk");
    }

    @Test
    public void verifyBirminghamCourtDetails() {
        FinremCaseDetails caseDetails = caseDetailsWithCourtDetails();
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.MIDLANDS);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.BIRMINGHAM);
        caseDetails.getCaseData().getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper()
            .setBirminghamCourtList(BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE);

        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Birmingham Civil And Family Justice Centre", "Priory Courts, 33 Bull Street, Birmingham, B4 6DS",
            "0300 123 5577", "FRCBirmingham@justice.gov.uk");
    }

    private FinremCaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails(NO_VALUE);
    }

    private FinremCaseDetails makeItFastTrackDecisionCase() {
        return caseDetails(YES_VALUE);
    }

    private FinremCaseDetails makeItJudiciaryFastTrackDecisionCase() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setFastTrackDecision(YesOrNo.forValue(NO_VALUE));
        caseData.setCaseAllocatedTo(YesOrNo.forValue(YES_VALUE));
        caseData.setHearingDate(DATE_OF_HEARING);
        caseData.getRegionWrapper().getDefaultCourtList().setBristolCourtList(BristolCourt.FR_bristolList_1);
        return FinremCaseDetails.builder().caseData(caseData).build();
    }

    private FinremCaseDetails caseDetails(String isFastTrackDecision) {

        FinremCaseData caseData = new FinremCaseData();
        caseData.setFastTrackDecision(YesOrNo.forValue(isFastTrackDecision));
        caseData.setHearingDate(DATE_OF_HEARING);
        caseData.setCopyOfPaperFormA(singletonList(paymentDocumentData()));
        caseData.getRegionWrapper().getDefaultCourtList().setBristolCourtList(BristolCourt.FR_bristolList_1);
        caseData.setFormC(newDocument());
        caseData.setFormG(newDocument());

        return FinremCaseDetails.builder().caseData(caseData).build();
    }

    private FinremCaseDetails caseDetailsWithCourtDetails() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setFastTrackDecision(YesOrNo.forValue(NO_VALUE));
        caseData.setHearingDate(DATE_OF_HEARING);
        return FinremCaseDetails.builder().caseData(caseData).build();
    }

    void verifyAdditionalFastTrackFields() {
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersCaptor.capture(),
            eq(documentConfiguration.getFormCFastTrackTemplate()), eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never()).generateDocument(any(), any(), eq(documentConfiguration.getFormCNonFastTrackTemplate()), any());
        verify(genericDocumentService, never()).generateDocument(any(), any(), eq(documentConfiguration.getFormGTemplate()), any());

        Map<String, Object> data = getDataFromCaptor(placeholdersCaptor);
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("eventDatePlus21Days"), is(notNullValue()));
    }

    void verifyCourtDetailsFields(String courtName, String courtAddress, String phone, String email) {
        Map<String, Object> data = getDataFromCaptor(placeholdersCaptor);
        @SuppressWarnings("unchecked")
        Map<String, Object> courtDetails = (Map<String, Object>) data.get("courtDetails");
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is(courtName));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is(courtAddress));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is(email));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is(phone));
    }

    void verifyAdditionalNonFastTrackFields() {
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), placeholdersCaptor.capture(),
            eq(documentConfiguration.getFormCNonFastTrackTemplate()), eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never())
            .generateDocumentFromPlaceholdersMap(any(), any(),
                eq(documentConfiguration.getFormCFastTrackTemplate()), any());
        verify(genericDocumentService)
            .generateDocumentFromPlaceholdersMap(eq(AUTH_TOKEN), any(), eq(documentConfiguration.getFormGTemplate()),
                eq(documentConfiguration.getFormGFileName()));

        Map<String, Object> data = getDataFromCaptor(placeholdersCaptor);
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("hearingDateLess35Days"), is(notNullValue()));
        assertThat(data.get("hearingDateLess14Days"), is(notNullValue()));
    }
}