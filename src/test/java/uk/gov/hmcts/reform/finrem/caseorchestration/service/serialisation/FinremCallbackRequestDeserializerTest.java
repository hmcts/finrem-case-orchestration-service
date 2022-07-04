package uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApplicantRepresentedPaper;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApplicantRole;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApplicationNotApproved;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApplicationNotApprovedCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.AssignToJudgeReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.AuthorisationSignedBy;
import uk.gov.hmcts.reform.finrem.ccd.domain.BenefitPayment;
import uk.gov.hmcts.reform.finrem.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseNotes;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseNotesCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.CfcCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenInfo;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenInfoCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Complexity;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentNatureOfApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.ConsentOrderType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Court;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailInterim;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailInterimCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DocumentPurpose;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.EstimatedAsset;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EvidenceParty;
import uk.gov.hmcts.reform.finrem.ccd.domain.FastTrackReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.Gender;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralApplicationCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralApplicationOutcome;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralEmail;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetter;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.GeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingTimeDirection;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingTypeDirection;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingUploadBundle;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Intention;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeAllocated;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeNotApprovedReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeNotApprovedReasonsCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeTimeEstimate;
import uk.gov.hmcts.reform.finrem.ccd.domain.JudgeType;
import uk.gov.hmcts.reform.finrem.ccd.domain.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.LondonCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamDomesticViolence;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamOtherGrounds;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamPreviousAttendance;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamUrgencyReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderDirection;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusal;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.ccd.domain.Organisation;
import uk.gov.hmcts.reform.finrem.ccd.domain.OtherDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.OtherDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionProvider;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionType;
import uk.gov.hmcts.reform.finrem.ccd.domain.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.PeriodicalPaymentSubstitute;
import uk.gov.hmcts.reform.finrem.ccd.domain.PotentialAllegation;
import uk.gov.hmcts.reform.finrem.ccd.domain.Provision;
import uk.gov.hmcts.reform.finrem.ccd.domain.RefusalReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.Region;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.ScannedDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.ccd.domain.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.SolUploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.SolUploadDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.SolicitorToDraftOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConsentOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadOrderDocumentType;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.MiamWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FinremCallbackRequestDeserializerTest {

    private static final String REFUSAL_ORDER_CALLBACK_REQUEST = "fixtures/refusal-order-contested.json";
    private static final String CONTESTED_INTERIM_CALLBACK_REQUEST = "fixtures/contested-interim-hearing.json";
    private static final String SOL_CONTEST_CALLBACK_REQUEST = "fixtures/deserialisation/ccd-request-with-solicitor-contestApplicationIssued.json";
    private static final String BASIC_REQUEST = "fixtures/deserialisation/basic-request.json";

    private FinremCallbackRequestDeserializer callbackRequestDeserializer;

    private ObjectMapper objectMapper;

    private String callback;

    @Before
    public void testSetUp() {
        objectMapper = new ObjectMapper();
        callbackRequestDeserializer = new FinremCallbackRequestDeserializer(objectMapper);
    }

    @Test
    public void deserializeBasicCallbackRequest() throws IOException {
        setCallbackString(BASIC_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        System.out.println(callbackRequest.getCaseDetails().getCaseData());
        assertNotNull(callbackRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void givenValidCallbackRequest_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(REFUSAL_ORDER_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
        System.out.println(callbackRequest.getCaseDetails().getCaseData());
        EventType eventType = callbackRequest.getEventType();
        assertEquals(eventType, EventType.GIVE_ALLOCATION_DIRECTIONS);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        assertEquals(caseData.getContactDetailsWrapper().getApplicantRepresented(), YesOrNo.YES);
        assertEquals(caseData.getRegionWrapper().getDefaultCourtList().getNottinghamCourtList().getId(), "FR_s_NottinghamList_1");
    }


    @Test
    public void givenGeneralOrderFixture_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(CONTESTED_INTERIM_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);

        assertNotNull(callbackRequest);
    }

    @Test
    public void givenCcdRequestAppIssued_whenDeserializeFromString_thenSuccessfullyDeserialize() throws IOException {
        setCallbackString(SOL_CONTEST_CALLBACK_REQUEST);
        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(callback);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        assertMiam(caseData);
        assertNotNull(callbackRequest);
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
    }

    private void setCallbackString(String fileName) throws IOException {
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile();
        callback = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
    }

    private Document getTestDocument() {
        return Document.builder()
            .binaryUrl("http://doc1.binary")
            .filename("doc1")
            .url("http://doc1")
            .build();
    }

    private void assertAmendedConsentOrderCollection(FinremCaseData caseData) {
        List<AmendedConsentOrderCollection> expected = List.of(
            AmendedConsentOrderCollection.builder()
            .value(AmendedConsentOrder.builder()
                .amendedConsentOrder(getTestDocument()).
                amendedConsentOrderDate(LocalDate.of(2020, 1, 2))
                .build())
                .build());
        assertNotNull(caseData.getAmendedConsentOrderCollection());
        assertTrue(caseData.getAmendedConsentOrderCollection().containsAll(expected));
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
        List<ApprovedOrderCollection> expected = List.of(
            ApprovedOrderCollection.builder()
                .value(ApprovedOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(
                        PensionTypeCollection.builder()
                            .value(PensionType.builder()
                                .typeOfDocument(PensionDocumentType.FORM_PPF)
                                .uploadedDocument(getTestDocument())
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
            BristolCourt.FR_bristolList_4);
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
                .value(GeneralEmail.builder()
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
                    .name("John Applicant")
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
                    .additionalHearingDocument(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getAdditionalHearingDocuments().containsAll(expected));
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
                .value(HearingUploadBundle.builder()
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
                    .documentDateAdded(LocalDate.of(2022,5,20))
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
        List<GeneralOrderCollection> expected = List.of(
            GeneralOrderCollection.builder()
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
                .value(OrderRefusal.builder()
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
                .value(PensionType.builder()
                    .uploadedDocument(getTestDocument())
                    .typeOfDocument(PensionDocumentType.FORM_P1)
                    .build())
                .build()
        );

        assertTrue(caseData.getPensionCollection().containsAll(expected));
    }

    private void assertDraftDirectionOrders(FinremCaseData caseData) {
        DraftDirectionOrder expectedOrder = DraftDirectionOrder.builder()
            .purposeOfDocument(DocumentPurpose.RESUBMITTED_DRAFT_ORDER)
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
                .value(DraftDirectionDetails.builder()
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

        assertTrue(caseData.getGeneralApplicationWrapper().getGeneralApplicationCollection().containsAll(expected));
    }

    private void assertGeneralLetterCollection(FinremCaseData caseData) {
        List<GeneralLetterCollection> expected = List.of(
            GeneralLetterCollection.builder()
                .value(GeneralLetter.builder()
                    .generatedLetter(getTestDocument())
                    .build())
                .build()
        );

        assertTrue(caseData.getGeneralLetterWrapper().getGeneralLetterCollection().containsAll(expected));
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
        assertEquals(consentOrderWrapper.getConsentOrderFrcAddress(), Address.builder()
            .addressLine1("Address").build());
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
                .value(ConsentOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(PensionTypeCollection.builder()
                            .value(PensionType.builder()
                                .typeOfDocument(PensionDocumentType.FORM_P1)
                                .uploadedDocument(getTestDocument())
                                .build())
                        .build()))
                    .build())
                .build());

        assertTrue(caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders().containsAll(expected));
    }

    private void assertContestedConsentedApprovedOrders(FinremCaseData caseData) {
        List<ConsentOrderCollection> expected = List.of(
            ConsentOrderCollection.builder()
                .value(ConsentOrder.builder()
                    .consentOrder(getTestDocument())
                    .orderLetter(getTestDocument())
                    .pensionDocuments(List.of(PensionTypeCollection.builder()
                        .value(PensionType.builder()
                            .typeOfDocument(PensionDocumentType.FORM_P1)
                            .uploadedDocument(getTestDocument())
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
}
