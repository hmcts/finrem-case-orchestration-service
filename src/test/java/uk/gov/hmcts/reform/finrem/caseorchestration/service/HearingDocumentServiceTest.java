package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class HearingDocumentServiceTest extends BaseServiceTest {
    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintDocumentsCaptor;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @MockBean
    private GenericDocumentService genericDocumentService;

    @Autowired
    private HearingDocumentService hearingDocumentService;

    @MockBean
    BulkPrintService bulkPrintService;

    @Autowired
    private DocumentConfiguration documentConfiguration;


    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN,
                makeItJudiciaryFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormG() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        assertCaseDocument((CaseDocument) result.get(FORM_G));
        verifyAdditionalNonFastTrackFields();
    }

    @Test
    public void sendToBulkPrint() {
        CaseDetails caseDetails = caseDetails(NO_VALUE);

        hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService, times(1)).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(3));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(2).getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void sendToBulkPrint_multipleFormA() {
        CaseDetails caseDetails = caseDetails(YES_VALUE);

        caseDetails.getData().put(FORM_A_COLLECTION, asList(pensionDocumentData(), pensionDocumentData(), pensionDocumentData()));

        hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService, times(1)).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(5));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(0).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(1).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(2).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(3).getBinaryFileUrl(), is(BINARY_URL));
        assertThat(bulkPrintDocumentsCaptor.getValue().get(4).getBinaryFileUrl(), is(BINARY_URL));
    }

    @Test
    public void verifySwanseaCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Swansea Civil & Family Justice Centre", "Carvella House, Quay West, Quay Parade, Swansea, SA1 1SD",
            "01792 485 800", "FRCswansea@justice.gov.uk");
    }

    @Test
    public void verifyNewportCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST, "FR_newport_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newport Civil and Family Court", "Clarence House, Clarence Place, Newport, NP19 7AA",
            "01633 245 040", "FRCNewport@justice.gov.uk");
    }

    @Test
    public void verifyNoWalesFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, SOUTHEAST_FRC_LIST, SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyKentCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Canterbury Family Court Hearing Centre", "The Law Courts, Chaucer Road, Canterbury, CT1 1ZA",
            "01634 887900", "FRCKSS@justice.gov.uk");
    }

    @Test
    public void verifyNoSouthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            SOUTHEAST, WALES_FRC_LIST, KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCleavelandCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newcastle Upon Tyne Justice Centre", "The Law Courts, The Quayside, Newcastle-upon-Tyne, NE1 3LA",
            "0191 2012000", "Family.newcastle.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNwYorkshireCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST, "FR_nw_yorkshire_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Harrogate Justice Centre", "The Court House, Victoria Avenue, Harrogate, HG1 1EL",
            "0113 306 2501", "leedsfamily@justice.gov.uk");
    }

    @Test
    public void verifyHumberCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST, "FR_humber_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Sheffield Family Hearing Centre", "The Law Courts, 50 West Bar, Sheffield, S3 8PH",
            "0114 2812522", "FRCSheffield@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHWEST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST, "FR_humber_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyLiverpoolCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST, "FR_liverpool_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Liverpool Civil And Family Court", "35 Vernon Street, Liverpool, L2 2BX",
            "0151 296 2225", "FRCLiverpool@Justice.gov.uk");
    }

    @Test
    public void verifyManchesterCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST, "FR_manchester_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Manchester County And Family Court", "1 Bridge Street West, Manchester, M60 9DJ",
            "0161 240 5430", "manchesterdivorce@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthWestFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHEAST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST, "FR_manchester_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCfcCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            LONDON, LONDON_FRC_LIST, CFC, CFC_COURTLIST, "FR_s_CFCList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Bromley County Court And Family Court", "Bromley County Court, College Road, Bromley, BR1 3PX",
            "0208 290 9620", "family.bromley.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNoLondonFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            LONDON, MIDLANDS_FRC_LIST, CFC, CFC_COURTLIST, "FR_s_CFCList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNottinghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Nottingham County Court And Family Court", "60 Canal Street, Nottingham NG1 7EJ",
            "0115 910 3504", "FRCNottingham@justice.gov.uk");
    }

    @Test
    public void verifyBirminghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Birmingham Civil And Family Justice Centre", "Pipers Row, Wolverhampton, WV1 3LQ",
            "0121 250 6794", "FRCBirmingham@justice.gov.uk");
    }

    @Test
    public void verifyNoMidlandsFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, LONDON_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourt() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "invalid_court"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourtList() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, NEWPORT_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNoRegionProvided() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails(NO_VALUE));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails(NO_VALUE);
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails(YES_VALUE);
    }

    private CaseDetails makeItJudiciaryFastTrackDecisionCase() {
        Map<String, Object> caseData =
                ImmutableMap.of(FAST_TRACK_DECISION, NO_VALUE,
                        CASE_ALLOCATED_TO, YES_VALUE, HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(FAST_TRACK_DECISION, isFastTrackDecision);
        caseData.put(HEARING_DATE, DATE_OF_HEARING);
        caseData.put(FORM_A_COLLECTION, asList(pensionDocumentData()));
        caseData.put(FORM_C, caseDocument());
        caseData.put(FORM_G, caseDocument());

        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetailsWithCourtDetails(String region, String frcList, String frc, String courtList, String court) {
        Map<String, Object> caseData =
            ImmutableMap.of(FAST_TRACK_DECISION, NO_VALUE, HEARING_DATE, DATE_OF_HEARING, REGION, region, frcList, frc, courtList, court);
        return CaseDetails.builder().data(caseData).build();
    }

    void verifyAdditionalFastTrackFields() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getFormCFastTrackTemplate()), eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never()).generateDocument(any(), any(), eq(documentConfiguration.getFormCNonFastTrackTemplate()), any());
        verify(genericDocumentService, never()).generateDocument(any(), any(), eq(documentConfiguration.getFormGTemplate()), any());

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("eventDatePlus21Days"), is(notNullValue()));
    }

    void verifyCourtDetailsFields(String courtName, String courtAddress, String phone, String email) {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        Map<String, Object> courtDetails = (Map<String, Object>) data.get("courtDetails");
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is(courtName));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is(courtAddress));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is(email));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is(phone));
    }

    void verifyCourtDetailsFieldsNotSet() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("courtDetails"), is(nullValue()));
    }

    void verifyAdditionalNonFastTrackFields() {
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq(documentConfiguration.getFormCNonFastTrackTemplate()), eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never())
            .generateDocument(any(), any(), eq(documentConfiguration.getFormCFastTrackTemplate()), any());
        verify(genericDocumentService, times(1))
            .generateDocument(eq(AUTH_TOKEN), any(), eq(documentConfiguration.getFormGTemplate()), eq(documentConfiguration.getFormGFileName()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("hearingDateLess35Days"), is(notNullValue()));
        assertThat(data.get("hearingDateLess14Days"), is(notNullValue()));
    }
}