package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantRepresentedPaper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicationNotApproved;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicationNotApprovedCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AuthorisationSignedBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPayment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotes;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildrenInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildrenInfoCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Complexity;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailInterim;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailInterimCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentPurpose;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAsset;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EvidenceParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FastTrackReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Gender;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTimeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Intention;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeAllocated;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeNotApprovedReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeNotApprovedReasonsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeTimeEstimate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamExemption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PeriodicalPaymentSubstitute;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PotentialAllegation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Provision;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RefusalReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolicitorToDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FinremCaseDetailMapperTest {


    FinremCaseDetailsMapper finremCaseDetailsMapper;

    private static final String REFUSAL_ORDER_CALLBACK_REQUEST = "/fixtures/refusal-order-contested.json";
    private static final String CONTESTED_INTERIM_CALLBACK_REQUEST = "/fixtures/contested-interim-hearing.json";
    public static final String BULK_PRINT_ADDITIONAL_HEARING_JSON = "/fixtures/bulkprint/bulk-print-additional-hearing.json";
    private static final String SOL_CONTEST_CALLBACK_REQUEST = "/fixtures/deserialisation/ccd-request-with-solicitor-contestApplicationIssued.json";
    private static final String BASIC_REQUEST = "/fixtures/deserialisation/basic-request.json";

    private static final String GA_REQUEST = "/fixtures/deserialisation/ccd-request-with-general-application.json";


    private CaseDetails caseDetails;
    private ObjectMapper objectMapper;

    @Before
    public void testSetUp() {
        objectMapper = new ObjectMapper();
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper.registerModule(new JavaTimeModule()));
    }

    @Test
    public void mapBasicCaseDetails() {
        caseDetails = buildCaseDetailsFromJson(BASIC_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
    }

    @Test
    public void mapFinremCaseDetailsToCaseDetails() {
        caseDetails = buildCaseDetailsFromJson(BASIC_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        CaseDetails caseDetailsFromPojo = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        assertEquals(caseDetails, caseDetailsFromPojo);
    }

    @Test
    public void mapBulkPrintDetails() throws JsonProcessingException {
        caseDetails = buildCaseDetailsFromJson(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
        assertEquals(finremCaseDetails.getData().getContactDetailsWrapper().getApplicantFmName(), "Test");
    }


    @Test
    @Ignore
    public void mapBulkPrintDetails_PojoCheck() throws JsonProcessingException {
        caseDetails = buildCaseDetailsFromJson(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
        assertEquals(finremCaseDetails.getData().getContactDetailsWrapper().getApplicantFmName(), "Test");
        CaseDetails caseDetailsFromPojo = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        String caseDetailsString = objectMapper.writeValueAsString(caseDetails);
        String caseDetailsPojoString = objectMapper.writeValueAsString(caseDetailsFromPojo);
        assertEquals(objectMapper.readTree(caseDetailsString), objectMapper.readTree(caseDetailsPojoString));
    }


    @Test
    public void givenValidCallbackRequest_thenSuccessfullyMapped() {
        caseDetails = buildCaseDetailsFromJson(REFUSAL_ORDER_CALLBACK_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
        assertNotNull(caseDetails);
        FinremCaseData caseData = finremCaseDetails.getData();
        assertEquals(caseData.getContactDetailsWrapper().getApplicantRepresented(), YesOrNo.YES);
        assertEquals(caseData.getContactDetailsWrapper().getApplicantFmName(), "Contested Applicant");
        assertEquals(caseData.getRegionWrapper().getDefaultCourtList().getNottinghamCourtList().getId(), "FR_s_NottinghamList_1");
    }

    @Test
    public void givenGeneralOrderFixture_whenDeserializeFromString_thenSuccessfullyDeserialize() {
        caseDetails = buildCaseDetailsFromJson(CONTESTED_INTERIM_CALLBACK_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
    }

    @Test
    public void givenGeneralApplicationFixture_whenDeserializeFromString_thenSuccessfullyDeserialize() {
        caseDetails = buildCaseDetailsFromJson(GA_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        assertNotNull(finremCaseDetails);
    }


    @Test
    public void givenCcdRequestAppIssued_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        caseDetails = buildCaseDetailsFromJson(SOL_CONTEST_CALLBACK_REQUEST);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        FinremCaseData caseData = finremCaseDetails.getData();
        assertMiam(caseData);
        assertAmendedConsentOrderCollection(caseData);
        assertCaseNotesCollection(caseData);
        assertScannedDocuments(caseData);
        assertCoverSheets(caseData);
        assertApprovedOrderCollection(caseData);
        assertEnums(caseData);
        assertMiam(caseData);
        assertChildrenInfo(caseData);
        assertGeneralEmail(caseData);
        assertRepresentationUpdateHistory(caseData);
        assertBenefitPayment(caseData);
        assertFastTrackReason(caseData);
        assertAdditionalHearingDocuments(caseData);
        assertUploadGeneralDocuments(caseData);
        assertApplicationNotApproved(caseData);
        assertDocumentCollections(caseData);
        assertJudgeNotApprovedReasons(caseData);
        assertUploadAdditionalDocument(caseData);
        assertHearingUploadBundle(caseData);
        assertConfidentialDocumentsUploaded(caseData);
        assertDirectionDetailsCollection(caseData);
        assertIndividualDocuments(caseData);
        assertOrderRefusalCollection(caseData);
        assertOtherCollection(caseData);
        assertCopyOfPaperFormA(caseData);
        assertPensionCollection(caseData);
        assertDraftDirectionOrders(caseData);
        assertGeneralApplicationsCollection(caseData);
        assertGeneralLetterCollection(caseData);
        assertRespondToOrderDocuments(caseData);
        assertDirectionDetailsInterim(caseData);
        assertSolUploadDocuments(caseData);
        assertUploadOrder(caseData);
        assertUploadDocuments(caseData);
        assertConsentOrderWrapper(caseData);

        assertNotNull(caseData.getContactDetailsWrapper().getRespondentAddress());
        assertNotNull(caseData.getContactDetailsWrapper().getApplicantSolicitorAddress());
        assertEquals(caseData.getContactDetailsWrapper().getApplicantSolicitorAddress().getAddressLine1(), "Line1");
    }


    private void assertAmendedConsentOrderCollection(FinremCaseData caseData) {
        List<AmendedConsentOrderCollection> expected = List.of(
            AmendedConsentOrderCollection.builder()
                .value(AmendedConsentOrder.builder()
                    .amendedConsentOrder(getTestDocument())
                    .amendedConsentOrderDate(LocalDate.of(2020, 1, 2))
                    .build())
                .build());
        assertNotNull(caseData.getAmendedConsentOrderCollection());
        assertTrue(caseData.getAmendedConsentOrderCollection().containsAll(expected));
    }

    private Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }

    private void assertCaseNotesCollection(FinremCaseData caseData) {
        List<CaseNotesCollection> expected = List.of(CaseNotesCollection.builder()
            .value(CaseNotes.builder()
                .caseNote("Note")
                .caseNoteAuthor("Cat")
                .build())
            .build());

        assertTrue(caseData.getCaseNotesCollection().containsAll(expected));
    }

    private void assertScannedDocuments(FinremCaseData caseData) {
        List<ScannedDocumentCollection> expected = List.of(
            ScannedDocumentCollection.builder()
                .value(ScannedDocument.builder()
                    .controlNumber("12345678")
                    .fileName("FormA.pdf")
                    .type(ScannedDocumentType.FORM)
                    .subtype("Form A")
                    .url(getTestDocument())
                    .exceptionRecordReference("none")
                    .build())
                .build()
        );

        assertTrue(caseData.getScannedDocuments().containsAll(expected));
    }

    private void assertCoverSheets(FinremCaseData caseData) {
        assertEquals(getTestDocument(), caseData.getBulkPrintCoverSheetApp());
        assertEquals(getTestDocument(), caseData.getBulkPrintCoverSheetRes());
        assertEquals("1234", caseData.getBulkPrintLetterIdApp());
        assertEquals("1234", caseData.getBulkPrintLetterIdRes());
        assertEquals(getTestDocument(), caseData.getApprovedConsentOrderLetter());
    }

    private void assertApprovedOrderCollection(FinremCaseData caseData) {
        List<ConsentOrderCollection> expected = List.of(
            ConsentOrderCollection.builder()
                .approvedOrder(ApprovedOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(
                        PensionTypeCollection.builder()
                            .typedCaseDocument(PensionType.builder()
                                .typeOfDocument(PensionDocumentType.FORM_PPF)
                                .pensionDocument(getTestDocument())
                                .build())
                            .build()
                    ))
                    .build())
                .build());

        assertTrue(caseData.getApprovedOrderCollection().containsAll(expected));
    }

    private void assertEnums(FinremCaseData caseData) {
        assertEquals(caseData.getDivRoleOfFrApplicant(), ApplicantRole.FR_ApplicantsRoleInDivorce_1);
        assertEquals(caseData.getApplicantRepresentedPaper(), ApplicantRepresentedPaper.FR_applicant_represented_1);
        assertEquals(caseData.getAuthorisationSignedBy(), AuthorisationSignedBy.LITIGATION_FRIEND);
        assertEquals(caseData.getHearingType(), HearingTypeDirection.DIR);
        assertTrue(caseData.getJudgeAllocated().containsAll(List.of(
            JudgeAllocated.FR_JUDGE_ALLOCATED_LIST_1,
            JudgeAllocated.FR_JUDGE_ALLOCATED_LIST_3)));
        assertEquals(caseData.getAssignedToJudgeReason(), AssignToJudgeReason.DRAFT_CONSENT_ORDER);
        assertEquals(caseData.getSolicitorResponsibleForDraftingOrder(), SolicitorToDraftOrder.APPLICANT_SOLICITOR);
        assertEquals(caseData.getRefusalOrderJudgeType(), JudgeType.HER_HONOUR_JUDGE);
        assertEquals(caseData.getSendOrderPostStateOption(), SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        assertEquals(caseData.getRegionWrapper().getDefaultRegionWrapper().getRegionList(), Region.SOUTHEAST);
        assertEquals(caseData.getRegionWrapper().getDefaultRegionWrapper().getSouthEastFrcList(), RegionSouthEastFrc.KENT);
        assertEquals(caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper().getKentSurreyCourtList(),
            KentSurreyCourt.FR_kent_surreyList_1);
        assertEquals(caseData.getOrderDirection(), OrderDirection.ORDER_ACCEPTED_AS_DRAFTED);
        assertEquals(caseData.getOrderDirectionJudge(), JudgeType.DISTRICT_JUDGE);
        assertEquals(caseData.getRegionWrapper().getInterimRegionWrapper().getInterimRegionList(), Region.SOUTHWEST);
        assertEquals(caseData.getRegionWrapper().getInterimRegionWrapper().getInterimSouthWestFrcList(), RegionSouthWestFrc.BRISTOL);
        assertEquals(caseData.getRegionWrapper().getInterimRegionWrapper().getCourtListWrapper().getInterimBristolCourtList(),
            BristolCourt.SALISBURY_LAW_COURTS);
        assertEquals(caseData.getAddToComplexityListOfCourts(), Complexity.TRUE_YES);
        assertTrue(caseData.getEstimatedAssetsChecklist().containsAll(List.of(
            EstimatedAsset.UNABLE_TO_QUANTIFY,
            EstimatedAsset.ONE_TO_FIVE_MILLION
        )));
        assertTrue(caseData.getPotentialAllegationChecklist().containsAll(List.of(
            PotentialAllegation.POTENTIAL_ALLEGATION_CHECKLIST_4,
            PotentialAllegation.POTENTIAL_ALLEGATION_CHECKLIST_13,
            PotentialAllegation.NOT_APPLICABLE
        )));
        assertEquals(caseData.getJudgeTimeEstimate(), JudgeTimeEstimate.ADDITIONAL_TIME);
        assertEquals(caseData.getSolicitorResponsibleForDraftingOrder(), SolicitorToDraftOrder.APPLICANT_SOLICITOR);
        assertEquals(caseData.getProvisionMadeFor(), Provision.CHILDREN_ACT_1989);
        assertEquals(caseData.getApplicantIntendsTo(), Intention.PROCEED_WITH_APPLICATION);
        assertTrue(caseData.getDischargePeriodicalPaymentSubstituteFor().containsAll(List.of(
            PeriodicalPaymentSubstitute.LUMP_SUM_ORDER,
            PeriodicalPaymentSubstitute.PENSION_SHARING_ORDER
        )));
        assertEquals(caseData.getServePensionProviderResponsibility(), PensionProvider.THE_COURT);
        assertEquals(caseData.getGeneralApplicationWrapper().getGeneralApplicationReceivedFrom(), EvidenceParty.CASE);
        assertEquals(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsJudgeType(),
            JudgeType.DEPUTY_DISTRICT_JUDGE);
        assertEquals(caseData.getGeneralLetterWrapper().getGeneralLetterAddressTo(), GeneralLetterAddressToType.APPLICANT_SOLICITOR);
        assertEquals(caseData.getGeneralApplicationWrapper().getGeneralApplicationOutcome(), GeneralApplicationOutcome.NOT_APPROVED);
        assertEquals(caseData.getDivorceStageReached(), StageReached.DECREE_NISI);
        assertEquals(YesOrNo.YES, caseData.getContactDetailsWrapper().getUpdateIncludesRepresentativeChange());
        assertEquals(YesOrNo.YES, caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent());
        assertEquals(YesOrNo.YES, caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant());
    }

    private void assertMiam(FinremCaseData caseData) {
        MiamWrapper miamValues = caseData.getMiamWrapper();

        assertTrue(miamValues.getMiamExemptionsChecklist().containsAll(
            List.of(
                MiamExemption.DOMESTIC_VIOLENCE,
                MiamExemption.URGENCY,
                MiamExemption.PREVIOUS_MIAM_ATTENDANCE,
                MiamExemption.OTHER
            )
        ));

        assertTrue(miamValues.getMiamDomesticViolenceChecklist().containsAll(
            List.of(
                MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1,
                MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_9,
                MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_18,
                MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_22
            )
        ));

        assertTrue(miamValues.getMiamUrgencyReasonChecklist().containsAll(
            List.of(
                MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1,
                MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_3,
                MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_5
            )
        ));

        assertEquals(miamValues.getMiamPreviousAttendanceChecklist(),
            MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2);

        assertEquals(miamValues.getMiamOtherGroundsChecklist(), MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1);


    }

    private void assertChildrenInfo(FinremCaseData caseData) {
        List<ChildrenInfoCollection> expected = List.of(
            ChildrenInfoCollection.builder()
                .value(ChildrenInfo.builder()
                    .countryOfResidence("UK")
                    .dateOfBirth(LocalDate.of(2008, 1, 1))
                    .gender(Gender.FEMALE)
                    .name("kid name")
                    .relationshipToApplicant("daughter")
                    .relationshipToRespondent("step-daughter")
                    .build())
                .build());

        assertTrue(caseData.getChildrenInfo().containsAll(expected));
    }

    private void assertGeneralEmail(FinremCaseData caseData) {
        assertEquals(caseData.getGeneralEmailRecipient(), "recipient");
        assertEquals(caseData.getGeneralEmailCreatedBy(), "sender");
        List<GeneralEmailCollection> expected = List.of(
            GeneralEmailCollection.builder()
                .value(GeneralEmailHolder.builder()
                    .generalEmailBody("test body")
                    .generalEmailRecipient("recipient")
                    .generalEmailCreatedBy("sender")
                    .build())
                .build()
        );
        assertTrue(caseData.getGeneralEmailCollection().containsAll(expected));
    }

    private void assertRepresentationUpdateHistory(FinremCaseData caseData) {
        List<RepresentationUpdateHistoryCollection> expected = List.of(
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party("applicant")
                    .clientName("John Applicant")
                    .by("claire")
                    .via("Notice of Change")
                    .added(ChangedRepresentative.builder()
                        .name("Added sol")
                        .email("solemail")
                        .organisation(Organisation.builder()
                            .organisationID("ashdiasd")
                            .build())
                        .build())
                    .build())
                .build());

        assertTrue(caseData.getRepresentationUpdateHistory().containsAll(expected));
    }

    private void assertBenefitPayment(FinremCaseData caseData) {
        List<BenefitPayment> expected = List.of(
            BenefitPayment.BENEFIT_CHECKLIST_VALUE_1,
            BenefitPayment.BENEFIT_CHECKLIST_VALUE_3
        );

        assertTrue(caseData.getBenefitPaymentChecklist().containsAll(expected));
    }

    private void assertFastTrackReason(FinremCaseData caseData) {
        List<FastTrackReason> expected = List.of(
            FastTrackReason.PERIODICAL_PAYMENTS_ORDER_NOT_SEEK_TO_DISMISS,
            FastTrackReason.ORDER_FOR_PERIODICAL_PAYMENTS
        );

        assertTrue(caseData.getFastTrackDecisionReason().containsAll(expected));
    }

    private void assertAdditionalHearingDocuments(FinremCaseData caseData) {
        List<AdditionalHearingDocumentCollection> expected = List.of(
            AdditionalHearingDocumentCollection.builder()
                .value(AdditionalHearingDocument.builder()
                    .document(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getAdditionalHearingDocuments().size() == 1);
    }

    private void assertUploadGeneralDocuments(FinremCaseData caseData) {
        List<UploadGeneralDocumentCollection> expected = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(getTestDocument())
                    .documentComment("none")
                    .documentDateAdded(LocalDate.of(2022, 6, 18))
                    .documentEmailContent("n/a")
                    .documentFileName("statement.pdf")
                    .documentType(UploadGeneralDocumentType.STATEMENT_REPORT)
                    .build())
                .build()
        );

        assertTrue(caseData.getUploadGeneralDocuments().containsAll(expected));
    }

    private void assertApplicationNotApproved(FinremCaseData caseData) {
        List<ApplicationNotApprovedCollection> expected = List.of(
            ApplicationNotApprovedCollection.builder()
                .value(ApplicationNotApproved.builder()
                    .additionalComments("none")
                    .andAfter("andAfter")
                    .dateOfOrder(LocalDate.of(2022, 2, 21))
                    .othersTextOrders("othersTextOrders")
                    .reasonForRefusal(List.of(
                        RefusalReason.FR_MS_REFUSAL_REASON_1,
                        RefusalReason.FR_MS_REFUSAL_REASON_5
                    ))
                    .selectJudge("selectJudge")
                    .build())
                .build()
        );

        assertTrue(caseData.getApplicationNotApproved().containsAll(expected));
    }

    private void assertDocumentCollections(FinremCaseData caseData) {
        assertTrue(caseData.getUploadHearingOrder().contains(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(getTestDocument()).build())
            .build()));
        assertTrue(caseData.getHearingOrderOtherDocuments().contains(DocumentCollection.builder()
            .value(getTestDocument())
            .build()));
        assertTrue(caseData.getFinalOrderCollection().contains(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(getTestDocument()).build())
            .build()));
    }

    private void assertDirectionDetailsCollection(FinremCaseData caseData) {
        List<DirectionDetailCollection> expected = List.of(
            DirectionDetailCollection.builder()
                .value(DirectionDetail.builder()
                    .isAnotherHearingYN(YesOrNo.YES)
                    .typeOfHearing(HearingTypeDirection.FH)
                    .timeEstimate("standardTime")
                    .dateOfHearing(LocalDate.of(2022, 6, 20))
                    .localCourt(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .build())
                .build()
        );

        assertTrue(caseData.getDirectionDetailsCollection().containsAll(expected));
    }

    private void assertJudgeNotApprovedReasons(FinremCaseData caseData) {
        assertTrue(caseData.getJudgeNotApprovedReasons().contains(JudgeNotApprovedReasonsCollection.builder()
            .value(JudgeNotApprovedReason.builder().judgeNotApprovedReasons("Reason").build())
            .build()));
    }

    private void assertUploadAdditionalDocument(FinremCaseData caseData) {
        List<UploadAdditionalDocumentCollection> expected = List.of(
            UploadAdditionalDocumentCollection.builder()
                .value(UploadAdditionalDocument.builder()
                    .additionalDocumentType(AdditionalDocumentType.STATEMENT_IN_SUPPORT_INCLUDING_MPS)
                    .additionalDocuments(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getUploadAdditionalDocument().containsAll(expected));
    }

    private void assertHearingUploadBundle(FinremCaseData caseData) {
        List<HearingUploadBundleCollection> expected = List.of(
            HearingUploadBundleCollection.builder()
                .value(HearingUploadBundleHolder.builder()
                    .hearingBundleDate(LocalDate.of(2022, 3, 28))
                    .hearingBundleDescription("Description")
                    .hearingBundleFdr(YesOrNo.YES)
                    .hearingBundleDocuments(List.of(
                        HearingBundleDocumentCollection.builder()
                            .value(HearingBundleDocument.builder()
                                .bundleDocuments(getTestDocument())
                                .bundleUploadDate(LocalDateTime.of(LocalDate.of(2022, 6, 20),
                                    LocalTime.of(15, 00)))
                                .build())
                            .build()
                    ))
                    .build())
                .build()
        );

        assertTrue(caseData.getHearingUploadBundle().containsAll(expected));
    }

    private void assertConfidentialDocumentsUploaded(FinremCaseData caseData) {
        List<UploadConfidentialDocumentCollection> expected = List.of(
            UploadConfidentialDocumentCollection.builder()
                .value(UploadConfidentialDocument.builder()
                    .documentLink(getTestDocument())
                    .documentComment("comment")
                    .documentDateAdded(LocalDate.of(2022, 5, 20))
                    .documentType(CaseDocumentType.VALUATION_REPORT)
                    .documentFileName("valuationReport.pdf")
                    .build())
                .build()
        );

        assertTrue(caseData.getConfidentialDocumentsUploaded().containsAll(expected));
    }

    private void assertIndividualDocuments(FinremCaseData caseData) {
        assertEquals(caseData.getConsentOrder(), getTestDocument());
        assertEquals(caseData.getConsentOrderText(), getTestDocument());
        assertEquals(caseData.getD81Applicant(), getTestDocument());
        assertEquals(caseData.getD81Joint(), getTestDocument());
        assertEquals(caseData.getD81Respondent(), getTestDocument());
        assertEquals(caseData.getDivorceUploadEvidence1(), getTestDocument());
        assertEquals(caseData.getDivorceUploadEvidence2(), getTestDocument());
        assertEquals(caseData.getMiniFormA(), getTestDocument());

    }

    private void assertGeneralOrderCollection(FinremCaseData caseData) {
        List<GeneralOrderCollectionItem> expected = List.of(
            GeneralOrderCollectionItem.builder()
                .value(GeneralOrder.builder()
                    .generalOrderJudgeType(JudgeType.DISTRICT_JUDGE)
                    .generalOrderDateOfOrder(LocalDate.of(2010, 1, 2))
                    .generalOrderOrder("order1")
                    .generalOrderDocumentUpload(getTestDocument())
                    .generalOrderComments("comment1")
                    .build())
                .build()
        );

        assertTrue(caseData.getGeneralOrderWrapper().getGeneralOrderCollection().containsAll(expected));
    }

    private void assertOrderRefusalCollection(FinremCaseData caseData) {
        List<OrderRefusalCollection> expected = List.of(
            OrderRefusalCollection.builder()
                .value(OrderRefusalHolder.builder()
                    .orderRefusal(List.of(
                        OrderRefusalOption.PENSION_ANNEX
                    ))
                    .orderRefusalAddComments("comment1")
                    .orderRefusalJudge(JudgeType.DISTRICT_JUDGE)
                    .orderRefusalDocs(getTestDocument())
                    .orderRefusalDate(LocalDate.of(2003, 2, 1))
                    .orderRefusalJudgeName("test3")
                    .orderRefusalOther("test1")
                    .build())
                .build()
        );

        assertTrue(caseData.getOrderRefusalCollection().containsAll(expected));
    }

    private void assertOtherCollection(FinremCaseData caseData) {
        List<OtherDocumentCollection> expected = List.of(
            OtherDocumentCollection.builder()
                .value(OtherDocument.builder()
                    .typeOfDocument(OtherDocumentType.NOTICE_OF_ACTING)
                    .uploadedDocument(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getOtherDocumentsCollection().containsAll(expected));
    }

    private void assertCopyOfPaperFormA(FinremCaseData caseData) {
        List<PaymentDocumentCollection> expected = List.of(
            PaymentDocumentCollection.builder()
                .value(PaymentDocument.builder()
                    .typeOfDocument(PaymentDocumentType.COPY_OF_PAPER_FORM_A)
                    .uploadedDocument(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getCopyOfPaperFormA().containsAll(expected));
    }

    private void assertPensionCollection(FinremCaseData caseData) {
        List<PensionTypeCollection> expected = List.of(
            PensionTypeCollection.builder()
                .id("1")
                .typedCaseDocument(PensionType.builder()
                    .pensionDocument(getTestDocument())
                    .typeOfDocument(PensionDocumentType.FORM_P1)
                    .build())
                .build()
        );

        assertTrue(caseData.getPensionCollection().containsAll(expected));
    }

    private void assertDraftDirectionOrders(FinremCaseData caseData) {
        DraftDirectionOrder expectedOrder = DraftDirectionOrder.builder()
            .purposeOfDocument(DocumentPurpose.RESUBMITTED_DRAFT_ORDER.getValue())
            .uploadDraftDocument(getTestDocument())
            .build();
        List<DraftDirectionOrderCollection> expected = List.of(DraftDirectionOrderCollection.builder()
            .value(expectedOrder)
            .build());

        assertTrue(caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection().containsAll(expected));
        assertTrue(caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection().containsAll(expected));
        assertEquals(caseData.getDraftDirectionWrapper().getLatestDraftDirectionOrder(), expectedOrder);
        assertDraftDirectionDetailsCollection(caseData);
    }

    private void assertDraftDirectionDetailsCollection(FinremCaseData caseData) {
        List<DraftDirectionDetailsCollection> expected = List.of(
            DraftDirectionDetailsCollection.builder()
                .value(DraftDirectionDetailsHolder.builder()
                    .isThisFinalYN(YesOrNo.YES)
                    .isAnotherHearingYN(YesOrNo.YES)
                    .typeOfHearing(HearingTypeDirection.FH)
                    .timeEstimate(HearingTimeDirection.STANDARD_TIME)
                    .additionalTime("additional time")
                    .listingInstructor("listing Instructor")
                    .localCourt(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .build())
                .build()
        );

        assertTrue(caseData.getDraftDirectionWrapper().getDraftDirectionDetailsCollection().containsAll(expected));
    }

    private void assertConsentOtherCollection(FinremCaseData caseData) {
        List<OtherDocumentCollection> expected = List.of(
            OtherDocumentCollection.builder()
                .value(OtherDocument.builder()
                    .uploadedDocument(getTestDocument())
                    .typeOfDocument(OtherDocumentType.SCHEDULE_OF_ASSETS)
                    .build())
                .build()
        );

        assertTrue(caseData.getConsentOrderWrapper().getConsentOtherCollection().containsAll(expected));
    }

    private void assertGeneralApplicationsCollection(FinremCaseData caseData) {
        List<GeneralApplicationCollection> expected = List.of(
            GeneralApplicationCollection.builder()
                .value(GeneralApplication.builder()
                    .generalApplicationDocument(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocumentCollection().size() == 1);
    }

    private void assertGeneralLetterCollection(FinremCaseData caseData) {
        List<GeneralLetterCollection> expected = List.of(
            GeneralLetterCollection.builder()
                .value(GeneralLetter.builder()
                    .generatedLetter(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getGeneralLetterWrapper().getGeneralLetterCollection().size() == 1);
    }

    private void assertRespondToOrderDocuments(FinremCaseData caseData) {
        List<RespondToOrderDocumentCollection> expected = List.of(
            RespondToOrderDocumentCollection.builder()
                .value(RespondToOrderDocument.builder()
                    .documentDateAdded(LocalDate.of(2010, 1, 2))
                    .documentType(RespondToOrderDocumentType.APPLICANT_LETTER_EMAIL)
                    .documentFileName("file1")
                    .documentLink(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getRespondToOrderDocuments().containsAll(expected));
    }

    private void assertDirectionDetailsInterim(FinremCaseData caseData) {
        List<DirectionDetailInterimCollection> expected = List.of(
            DirectionDetailInterimCollection.builder()
                .value(DirectionDetailInterim.builder()
                    .interimTypeOfHearing(InterimTypeOfHearing.FH)
                    .interimTimeEstimate("1 hour")
                    .interimHearingTime("1pm")
                    .localCourt(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .build())
                .build()
        );

        assertTrue(caseData.getInterimWrapper().getDirectionDetailsCollectionInterim().containsAll(expected));
    }

    private void assertSolUploadDocuments(FinremCaseData caseData) {
        List<SolUploadDocumentCollection> expected = List.of(
            SolUploadDocumentCollection.builder()
                .value(SolUploadDocument.builder()
                    .documentComment("doc-comment")
                    .documentDateAdded(LocalDate.of(2010, 1, 2))
                    .documentEmailContent("email-content")
                    .documentLink(getTestDocument())
                    .documentFileName("file1")
                    .documentType(SolUploadDocumentType.NOTICE_OF_ACTING)
                    .build())
                .build()
        );

        assertTrue(caseData.getSolUploadDocuments().containsAll(expected));
    }

    private void assertUploadDocuments(FinremCaseData caseData) {
        List<UploadDocumentCollection> expected = List.of(
            UploadDocumentCollection.builder()
                .value(UploadDocument.builder()
                    .documentComment("doc-comment")
                    .documentDateAdded(LocalDate.of(2010, 1, 2))
                    .documentEmailContent("email-content")
                    .documentLink(getTestDocument())
                    .documentFileName("file1")
                    .documentType(UploadDocumentType.FINAL_ORDER)
                    .build())
                .build()
        );

        assertTrue(caseData.getUploadDocuments().containsAll(expected));
    }

    private void assertUploadOrder(FinremCaseData caseData) {
        List<UploadOrderCollection> expected = List.of(
            UploadOrderCollection.builder()
                .id("1")
                .value(UploadOrder.builder()
                    .documentComment("doc-comment")
                    .documentDateAdded(LocalDate.of(2010, 1, 2))
                    .documentEmailContent("email-content")
                    .documentLink(getTestDocument())
                    .documentFileName("file1")
                    .documentType(UploadOrderDocumentType.GENERAL_ORDER)
                    .build())
                .build()
        );

        assertTrue(caseData.getUploadOrder().containsAll(expected));
    }

    private void assertConsentOrderWrapper(FinremCaseData caseData) {
        ConsentOrderWrapper consentOrderWrapper = caseData.getConsentOrderWrapper();
        assertTrue(consentOrderWrapper.getConsentNatureOfApplicationChecklist()
            .contains(NatureApplication.PENSION_COMPENSATION_SHARING_ORDER));
        assertEquals(consentOrderWrapper.getConsentNatureOfApplicationAddress(), "Address");
        assertEquals(consentOrderWrapper.getConsentNatureOfApplicationMortgage(), "Mortgage");
        assertEquals(consentOrderWrapper.getConsentNatureOfApplication5(), YesOrNo.YES);
        assertEquals(consentOrderWrapper.getConsentOrderForChildrenQuestion1(), YesOrNo.YES);
        assertTrue(consentOrderWrapper.getConsentNatureOfApplication6().containsAll(List.of(
            ConsentNatureOfApplication.DISABILITY_EXPENSES,
            ConsentNatureOfApplication.TRAINING
        )));
        assertEquals(consentOrderWrapper.getConsentNatureOfApplication7(), "String");
        assertEquals(consentOrderWrapper.getConsentOrderFrcName(), "Bromley");
        assertEquals(consentOrderWrapper.getConsentOrderFrcAddress(), "The Law Courts, North Parade Road, Bath, BA1 5AF");
        assertEquals(consentOrderWrapper.getConsentOrderFrcEmail(), "email");
        assertEquals(consentOrderWrapper.getConsentOrderFrcPhone(), "123456789");
        assertEquals(consentOrderWrapper.getConsentSubjectToDecreeAbsoluteValue(), YesOrNo.YES);
        assertEquals(consentOrderWrapper.getConsentServePensionProvider(), YesOrNo.YES);
        assertEquals(consentOrderWrapper.getConsentServePensionProviderResponsibility(), PensionProvider.APPLICANT_SOLICITOR);
        assertEquals(consentOrderWrapper.getConsentServePensionProviderOther(), "Other");
        assertEquals(consentOrderWrapper.getConsentSelectJudge(), "Judge");
        assertEquals(consentOrderWrapper.getConsentJudgeName(), "Name");
        assertEquals(consentOrderWrapper.getConsentDateOfOrder(), LocalDate.of(2022, 2, 2));
        assertEquals(consentOrderWrapper.getConsentAdditionalComments(), "additional");
        assertEquals(consentOrderWrapper.getConsentMiniFormA(), getTestDocument());
        assertConsentedNotApprovedOrders(caseData);
        assertConsentedNotApprovedOrders(caseData);
        assertContestedConsentedApprovedOrders(caseData);
        assertUploadConsentOrder(caseData);
        assertConsentOtherCollection(caseData);
    }

    private void assertConsentedNotApprovedOrders(FinremCaseData caseData) {
        List<ConsentOrderCollection> expected = List.of(
            ConsentOrderCollection.builder()
                .approvedOrder(ApprovedOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(PensionTypeCollection.builder()
                        .typedCaseDocument(PensionType.builder()
                            .typeOfDocument(PensionDocumentType.FORM_P1)
                            .pensionDocument(getTestDocument())
                            .build())
                        .build()))
                    .build())
                .build());

        assertTrue(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders().containsAll(expected));
    }

    private void assertContestedConsentedApprovedOrders(FinremCaseData caseData) {
        List<ConsentOrderCollection> expected = List.of(
            ConsentOrderCollection.builder()
                .approvedOrder(ApprovedOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(PensionTypeCollection.builder()
                        .typedCaseDocument(PensionType.builder()
                            .typeOfDocument(PensionDocumentType.FORM_P1)
                            .pensionDocument(getTestDocument())
                            .build())
                        .build()))
                    .build())
                .build());

        assertTrue(caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders().containsAll(expected));
    }

    private void assertUploadConsentOrder(FinremCaseData caseData) {
        List<UploadConsentOrderCollection> expected = List.of(
            UploadConsentOrderCollection.builder()
                .value(UploadConsentOrder.builder()
                    .documentLink(getTestDocument())
                    .documentComment("Something")
                    .documentFileName("generalOrder.pdf")
                    .documentType(ConsentOrderType.GENERAL_ORDER)
                    .documentEmailContent("Something")
                    .build())
                .build());

        assertTrue(caseData.getConsentOrderWrapper().getUploadConsentOrder().containsAll(expected));
    }

    private CaseDocument getTestDocument() {
        return CaseDocument.builder()
            .documentBinaryUrl("http://doc1.binary")
            .documentFilename("doc1")
            .documentUrl("http://doc1")
            .build();
    }

    private CaseDetails buildCaseDetailsFromJson(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return caseDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
