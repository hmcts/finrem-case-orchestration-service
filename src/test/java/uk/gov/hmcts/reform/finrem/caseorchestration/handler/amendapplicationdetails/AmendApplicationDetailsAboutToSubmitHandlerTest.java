package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.assertj.core.api.ObjectEnumerableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPayment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BenefitPaymentChecklist;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FastTrackReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureOfApplicationSchedule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PropertyAdjustmentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PropertyAdjustmentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached.DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached.DECREE_NISI;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.StageReached.PETITION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsAboutToSubmitHandlerTest {

    @InjectMocks
    private AmendApplicationDetailsAboutToSubmitHandler handler;
    
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, AMEND_CONTESTED_APP_DETAILS)
        );
    }

    @Test
    void givenAnyCases_whenHandled_thenGenerateMiniFormAAndCaseFlagInformationSet() {
        final CaseDocument generatedMiniFormA = mock(CaseDocument.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(eq(AUTH_TOKEN), eq(finremCaseDetails)))
            .thenReturn(generatedMiniFormA);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(FinremCaseData::getMiniFormA).isEqualTo(generatedMiniFormA);
        verify(onlineFormDocumentService).generateDraftContestedMiniFormA(eq(AUTH_TOKEN), eq(finremCaseDetails));
        verify(caseFlagsService).setCaseFlagInformation(finremCaseDetails);
    }

    @Test
    void givenAnyCases_whenHandled_thenRefugeTabsUpdated() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {
            handler.handle(finremCallbackRequest, AUTH_TOKEN);
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(finremCaseDetails));
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(finremCaseDetails));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenExpressPilotEnabledToggled_whenHandled_thenExpressCaseEnrollmentStatusSet(boolean expressPilotEnabled) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(expressPilotEnabled);

        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(expressCaseService, times(expressPilotEnabled ? 1 : 0)).setExpressCaseEnrollmentStatus(finremCaseData);
    }

    @Test
    void givenDecreeNisiSelected_whenHandled_thenClearDecreeAbsoluteAndPetitionRelatedFields() {
        // Migrated from {@code UpdateContestedCaseControllerTest.shouldDeleteNoDecreeAbsoluteWhenDecreeNisiSelectedBySolicitor}.

        final CaseDocument divorceUploadEvidence1 = mock(CaseDocument.class);
        final LocalDate divorceDecreeNisiDate = mock(LocalDate.class);
        final LocalDate divorceDecreeAbsoluteDate = mock(LocalDate.class);
        final LocalDate divorcePetitionIssuedDate = mock(LocalDate.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(DECREE_NISI);
        finremCaseData.setDivorceUploadEvidence1(divorceUploadEvidence1);
        finremCaseData.setDivorceUploadEvidence2(caseDocument("DivorceUploadEvidence2.pdf"));
        finremCaseData.setDivorceDecreeNisiDate(divorceDecreeNisiDate);
        finremCaseData.setDivorceDecreeAbsoluteDate(divorceDecreeAbsoluteDate);
        finremCaseData.setDivorceUploadPetition(caseDocument("DivorceUploadPetition.pdf"));
        finremCaseData.setDivorcePetitionIssuedDate(divorcePetitionIssuedDate);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorceUploadEvidence2,
            FinremCaseData::getDivorceDecreeAbsoluteDate,
            FinremCaseData::getDivorceUploadPetition
        ).containsOnlyNulls();
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorcePetitionIssuedDate,
            FinremCaseData::getDivorceUploadEvidence1,
            FinremCaseData::getDivorceDecreeNisiDate
        ).containsExactly(
            divorcePetitionIssuedDate,
            divorceUploadEvidence1,
            divorceDecreeNisiDate
        );
    }

    @Test
    void givenDecreeAbsoluteSelected_whenHandled_thenClearDecreeNisiAndPetitionRelatedFields() {
        // Merged from {@code UpdateContestedCaseControllerTest.shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested}

        final CaseDocument divorceUploadEvidence1 = mock(CaseDocument.class);
        final LocalDate divorceDecreeNisiDate = mock(LocalDate.class);
        final LocalDate divorceDecreeAbsoluteDate = mock(LocalDate.class);
        final LocalDate divorcePetitionIssuedDate = mock(LocalDate.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(DECREE_ABSOLUTE);
        finremCaseData.setDivorceUploadEvidence1(divorceUploadEvidence1);
        finremCaseData.setDivorceUploadEvidence2(caseDocument("DivorceUploadEvidence2.pdf"));
        finremCaseData.setDivorceDecreeNisiDate(divorceDecreeNisiDate);
        finremCaseData.setDivorceDecreeAbsoluteDate(divorceDecreeAbsoluteDate);
        finremCaseData.setDivorceUploadPetition(caseDocument("DivorceUploadPetition.pdf"));
        finremCaseData.setDivorcePetitionIssuedDate(divorcePetitionIssuedDate);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorceUploadPetition,
            FinremCaseData::getDivorceUploadEvidence1,
            FinremCaseData::getDivorceDecreeNisiDate
        ).containsOnlyNulls();
        assertThat(response.getData()).extracting(FinremCaseData::getDivorcePetitionIssuedDate)
        .isEqualTo(divorcePetitionIssuedDate);
    }

    @Test
    void givenPetitionIssuedSelected_whenHandled_thenClearDecreeNisiAndDecreeAbsoluteRelatedFields() {
        // Merged from {@code UpdateContestedCaseControllerTest.shouldDeleteDecreeAbsoluteWhenSolicitorChooseToPetitionIssuedForContested}

        final CaseDocument divorceUploadEvidence1 = mock(CaseDocument.class);
        final CaseDocument divorceUploadPetition = mock(CaseDocument.class);
        final LocalDate divorceDecreeNisiDate = mock(LocalDate.class);
        final LocalDate divorceDecreeAbsoluteDate = mock(LocalDate.class);
        final LocalDate divorcePetitionIssuedDate = mock(LocalDate.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(PETITION_ISSUED);
        finremCaseData.setDivorceUploadEvidence1(divorceUploadEvidence1);
        finremCaseData.setDivorceUploadEvidence2(caseDocument("DivorceUploadEvidence2.pdf"));
        finremCaseData.setDivorceDecreeNisiDate(divorceDecreeNisiDate);
        finremCaseData.setDivorceDecreeAbsoluteDate(divorceDecreeAbsoluteDate);
        finremCaseData.setDivorceUploadPetition(divorceUploadPetition);
        finremCaseData.setDivorcePetitionIssuedDate(divorcePetitionIssuedDate);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorceUploadEvidence1,
            FinremCaseData::getDivorceUploadEvidence2,
            FinremCaseData::getDivorceDecreeNisiDate,
            FinremCaseData::getDivorceDecreeAbsoluteDate
        ).containsOnlyNulls();
        assertThat(response.getData()).extracting(
            FinremCaseData::getDivorcePetitionIssuedDate,
            FinremCaseData::getDivorceUploadPetition
        ).containsExactly(
            divorcePetitionIssuedDate,
            divorceUploadPetition
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, YES",
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, NO",
        "N/A, YES",
        "N/A, NO"
    }, nullValues = "N/A")
    void givenPropertyAdjustmentOrderNotSelected_whenHandled_thenClearPropertyRelatedFields(Schedule1OrMatrimonialAndCpList typeOfApplication,
                                                                                            YesOrNo additionalPropertyDecision) {
        // Merged from {@code UpdateContestedCaseControllerTest.shouldRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested}
        // Merged from {@code UpdateContestedCaseControllerTest.shouldUpdatePropertyAdjustmentOrderDecisionDetailForContested}
        // Merged from {@code UpdateContestedCaseControllerTest.shouldRemoveAdditionalPropertyDetailsForContested}

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(DECREE_NISI);
        finremCaseData.getScheduleOneWrapper().setTypeOfApplication(typeOfApplication);
        finremCaseData.setPropertyAddress("102");
        finremCaseData.setMortgageDetail("HSBC");
        finremCaseData.setAdditionalPropertyOrderDecision(additionalPropertyDecision);
        finremCaseData.setPropertyAdjustmentOrderDetail(List.of(
            PropertyAdjustmentOrderCollection.builder()
                .value(PropertyAdjustmentOrder.builder()
                    .nameForProperty("HSBC")
                    .propertyAddress("103")
                    .build())
                .build()
        ));
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplicationChecklist(List.of(
            NatureApplication.LUMP_SUM_ORDER,
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.PENSION_ATTACHMENT_ORDER,
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
            NatureApplication.PERIODICAL_PAYMENT_ORDER
        ));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getPropertyAddress,
            FinremCaseData::getMortgageDetail,
            FinremCaseData::getPropertyAdjustmentOrderDetail
        ).containsOnlyNulls();
    }

    @ParameterizedTest
    @CsvSource(value = {
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, YES, false",
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, NO, true",
        "N/A, YES, false",
        "N/A, NO, true"
    }, nullValues = "N/A")
    void givenPropertyAdjustmentOrderSelected_whenHandled_thenClearPropertyAdjustmentOrderDetail(
        Schedule1OrMatrimonialAndCpList typeOfApplication,
        YesOrNo additionalPropertyDecision,
        boolean shouldPropertyAdjustmentOrderDetail) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setDivorceStageReached(DECREE_NISI);
        finremCaseData.getScheduleOneWrapper().setTypeOfApplication(typeOfApplication);
        finremCaseData.setPropertyAddress("102");
        finremCaseData.setMortgageDetail("HSBC");
        finremCaseData.setAdditionalPropertyOrderDecision(additionalPropertyDecision);
        finremCaseData.setPropertyAdjustmentOrderDetail(List.of(
            PropertyAdjustmentOrderCollection.builder()
                .value(PropertyAdjustmentOrder.builder()
                    .nameForProperty("HSBC")
                    .propertyAddress("103")
                    .build())
                .build()
        ));
        finremCaseData.getNatureApplicationWrapper().setNatureOfApplicationChecklist(List.of(
            NatureApplication.LUMP_SUM_ORDER,
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.PENSION_ATTACHMENT_ORDER,
            NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER,
            NatureApplication.PERIODICAL_PAYMENT_ORDER,
            NatureApplication.PROPERTY_ADJUSTMENT_ORDER // selected
        ));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(
            FinremCaseData::getPropertyAddress,
            FinremCaseData::getMortgageDetail
        ).doesNotContainNull();
        if (shouldPropertyAdjustmentOrderDetail) {
            assertThat(response.getData()).extracting(
                FinremCaseData::getPropertyAdjustmentOrderDetail
            ).isNull();
        } else {
            assertThat(response.getData()).extracting(
                FinremCaseData::getPropertyAdjustmentOrderDetail
            ).isNotNull();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenRespondentRepresented_whenHandled_thenClearUnwantedRespondentFields(boolean respondentRepresented) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);

        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        when(finremCaseData.isRespondentRepresentedByASolicitor()).thenReturn(respondentRepresented);
        YesOrNo respSolNotificationsEmailConsent = mock(YesOrNo.class);
        finremCaseData.setRespSolNotificationsEmailConsent(respSolNotificationsEmailConsent);
        ContactDetailsWrapper contactDetailsWrapper = spy(ContactDetailsWrapper.class);
        contactDetailsWrapper.setRespondentSolicitorName("ABC");
        contactDetailsWrapper.setRespondentSolicitorFirm("ABC Solicitor");
        contactDetailsWrapper.setRespondentSolicitorReference("REFXXXXASDASDSA");
        Address respondentSolicitorAddress = mock(Address.class);
        contactDetailsWrapper.setRespondentSolicitorAddress(respondentSolicitorAddress);
        contactDetailsWrapper.setRespondentSolicitorPhone("02255666444");
        contactDetailsWrapper.setRespondentSolicitorEmail("respondentSolicitorEmail@abc.com");
        contactDetailsWrapper.setRespondentSolicitorDxNumber("XXXX1111");
        Address respondentAddress = mock(Address.class);
        contactDetailsWrapper.setRespondentAddress(respondentAddress);
        contactDetailsWrapper.setRespondentPhone("02255666333");
        contactDetailsWrapper.setRespondentEmail("respondent@email.com");
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        ObjectEnumerableAssert respondentSolicitorContactFields = assertThat(contactDetailsWrapper).extracting(
            ContactDetailsWrapper::getRespondentSolicitorName,
            ContactDetailsWrapper::getRespondentSolicitorFirm,
            ContactDetailsWrapper::getRespondentSolicitorReference,
            ContactDetailsWrapper::getRespondentSolicitorAddress,
            ContactDetailsWrapper::getRespondentSolicitorPhone,
            ContactDetailsWrapper::getRespondentSolicitorEmail,
            ContactDetailsWrapper::getRespondentSolicitorDxNumber
        );
        ObjectEnumerableAssert respondentContactFields = assertThat(contactDetailsWrapper).extracting(
            ContactDetailsWrapper::getRespondentAddress,
            ContactDetailsWrapper::getRespondentPhone,
            ContactDetailsWrapper::getRespondentEmail
        );

        if (respondentRepresented) {
            respondentContactFields.containsOnlyNulls();
            respondentSolicitorContactFields.containsExactly(
                "ABC",
                "ABC Solicitor",
                "REFXXXXASDASDSA",
                respondentSolicitorAddress,
                "02255666444",
                "respondentSolicitorEmail@abc.com",
                "XXXX1111"
            );
            assertThat(response.getData()).extracting(
                FinremCaseData::getRespSolNotificationsEmailConsent).isEqualTo(respSolNotificationsEmailConsent);
        } else {
            respondentContactFields.containsExactly(
                respondentAddress, "02255666333", "respondent@email.com"
            );
            respondentSolicitorContactFields.containsOnlyNulls();
            assertThat(response.getData()).extracting(
                FinremCaseData::getRespSolNotificationsEmailConsent
            ).isNull();
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS",
        "N/A"
    }, nullValues = "N/A")
    void givenPeriodicalPaymentOrderNotSelected_whenHandled_thenClearUnwantedFields(Schedule1OrMatrimonialAndCpList typeOfApplication) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        if (typeOfApplication != null) {
            finremCaseData.getScheduleOneWrapper().setTypeOfApplication(typeOfApplication);
            finremCaseData.getNatureApplicationWrapper().setNatureOfApplicationChecklist(List.of(
                NatureApplication.LUMP_SUM_ORDER,
                NatureApplication.PENSION_SHARING_ORDER,
                NatureApplication.PENSION_ATTACHMENT_ORDER,
                NatureApplication.PENSION_COMPENSATION_SHARING_ORDER,
                NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER
            ));
        } else {
            finremCaseData.getScheduleOneWrapper().setTypeOfApplication(mock(Schedule1OrMatrimonialAndCpList.class));
            finremCaseData.getScheduleOneWrapper().setNatureOfApplicationChecklistSchedule(List.of(
                NatureOfApplicationSchedule.LUMP_SUM_ORDER,
                NatureOfApplicationSchedule.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY
            ));
        }
        YesOrNo paymentForChildrenDecision = mock(YesOrNo.class);
        finremCaseData.setPaymentForChildrenDecision(paymentForChildrenDecision);

        YesOrNo benefitForChildrenDecision = mock(YesOrNo.class);
        finremCaseData.setBenefitForChildrenDecision(benefitForChildrenDecision);
        List<BenefitPayment> benefitPaymentChecklist = mock(List.class);
        finremCaseData.setBenefitPaymentChecklist(benefitPaymentChecklist);

        YesOrNo benefitForChildrenDecisionSchedule = mock(YesOrNo.class);
        finremCaseData.setBenefitForChildrenDecisionSchedule(benefitForChildrenDecisionSchedule);
        List<BenefitPaymentChecklist> benefitPaymentChecklistSchedule = mock(List.class);
        finremCaseData.setBenefitPaymentChecklistSchedule(benefitPaymentChecklistSchedule);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData()).extracting(FinremCaseData::getPaymentForChildrenDecision).isNull();
        if (typeOfApplication == MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS) {
            assertThat(response.getData())
                .extracting(FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getBenefitPaymentChecklist)
                .containsOnlyNulls();
            assertThat(response.getData())
                .extracting(FinremCaseData::getBenefitForChildrenDecisionSchedule,
                    FinremCaseData::getBenefitPaymentChecklistSchedule)
                .containsExactly(
                    benefitForChildrenDecisionSchedule,
                    benefitPaymentChecklistSchedule
                );
        } else {
            assertThat(response.getData())
                .extracting(FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getBenefitPaymentChecklist)
                .containsExactly(benefitForChildrenDecision, benefitPaymentChecklist);
            assertThat(response.getData())
                .extracting(FinremCaseData::getBenefitForChildrenDecisionSchedule,
                    FinremCaseData::getBenefitPaymentChecklistSchedule)
                .containsOnlyNulls();
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, YES, YES",
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, YES, NO",
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, NO, YES",
        "MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS, NO, NO",
        "N/A, YES, YES",
        "N/A, YES, NO",
        "N/A, NO, YES",
        "N/A, NO, NO"
    }, nullValues = "N/A")
    void givenPeriodicalPaymentOrderSelected_whenHandled_thenClearUnwantedFields(
        Schedule1OrMatrimonialAndCpList typeOfApplication, // null for other typeOfApplication
        YesOrNo paymentForChildrenDecision,
        YesOrNo benefitForChildrenDecision
    ) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));
        if (typeOfApplication != null) {
            finremCaseData.getScheduleOneWrapper().setTypeOfApplication(typeOfApplication);
            finremCaseData.getNatureApplicationWrapper().setNatureOfApplicationChecklist(List.of(
                NatureApplication.PERIODICAL_PAYMENT_ORDER
            ));
        } else {
            finremCaseData.getScheduleOneWrapper().setTypeOfApplication(mock(Schedule1OrMatrimonialAndCpList.class));
            finremCaseData.getScheduleOneWrapper().setNatureOfApplicationChecklistSchedule(List.of(
                NatureOfApplicationSchedule.PERIODICAL_PAYMENT_ORDER
            ));
        }

        finremCaseData.setPaymentForChildrenDecision(paymentForChildrenDecision);
        finremCaseData.setBenefitForChildrenDecision(benefitForChildrenDecision);
        finremCaseData.setBenefitPaymentChecklist(mock(List.class));
        finremCaseData.setBenefitForChildrenDecisionSchedule(mock(YesOrNo.class));
        finremCaseData.setBenefitPaymentChecklistSchedule(mock(List.class));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        List<Function<FinremCaseData, ?>> fieldsAreNullExtractors = List.of();
        List<Function<FinremCaseData, ?>> fieldsAreNotNullExtractors = List.of(
            FinremCaseData::getBenefitForChildrenDecision,
            FinremCaseData::getBenefitPaymentChecklist,
            FinremCaseData::getPaymentForChildrenDecision,
            FinremCaseData::getBenefitForChildrenDecisionSchedule,
            FinremCaseData::getBenefitPaymentChecklistSchedule
        );

        if (YesOrNo.NO.equals(paymentForChildrenDecision)) {
            if (typeOfApplication == MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS) {
                fieldsAreNullExtractors = List.of(
                    FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getBenefitPaymentChecklist
                );
                fieldsAreNotNullExtractors = List.of(
                    FinremCaseData::getPaymentForChildrenDecision,
                    FinremCaseData::getBenefitForChildrenDecisionSchedule,
                    FinremCaseData::getBenefitPaymentChecklistSchedule
                );
            } else {
                fieldsAreNullExtractors = List.of(
                    FinremCaseData::getBenefitForChildrenDecisionSchedule,
                    FinremCaseData::getBenefitPaymentChecklistSchedule
                );
                fieldsAreNotNullExtractors = List.of(
                    FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getBenefitPaymentChecklist,
                    FinremCaseData::getPaymentForChildrenDecision
                );
            }
        }
        if (YesOrNo.YES.equals(benefitForChildrenDecision) && YesOrNo.YES.equals(paymentForChildrenDecision)) {
            if (typeOfApplication == MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS) {
                fieldsAreNullExtractors = List.of(
                    FinremCaseData::getBenefitPaymentChecklist
                );
                fieldsAreNotNullExtractors = List.of(
                    FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getPaymentForChildrenDecision,
                    FinremCaseData::getBenefitForChildrenDecisionSchedule,
                    FinremCaseData::getBenefitPaymentChecklistSchedule
                );
            } else {
                fieldsAreNullExtractors = List.of(
                    FinremCaseData::getBenefitPaymentChecklistSchedule
                );
                fieldsAreNotNullExtractors = List.of(
                    FinremCaseData::getBenefitForChildrenDecision,
                    FinremCaseData::getBenefitPaymentChecklist,
                    FinremCaseData::getPaymentForChildrenDecision,
                    FinremCaseData::getBenefitForChildrenDecisionSchedule
                );
            }
        }
        // check all fields
        if (!fieldsAreNullExtractors.isEmpty()) {
            assertThat(response.getData()).extracting(fieldsAreNullExtractors.toArray(new Function[0]))
                .containsOnlyNulls();
        }
        if (!fieldsAreNotNullExtractors.isEmpty()) {
            assertThat(response.getData()).extracting(fieldsAreNotNullExtractors.toArray(new Function[0]))
                .doesNotContainNull();
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    @NullSource
    void givenFastTrackDecision_whenHandled_thenClearFastTrackDecisionReason(YesOrNo fastTrackDecision) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        List<FastTrackReason> fastTrackDecisionReason = mock(List.class);
        finremCaseData.setFastTrackDecision(fastTrackDecision);
        finremCaseData.setFastTrackDecisionReason(fastTrackDecisionReason);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (YesOrNo.NO.equals(fastTrackDecision)) {
            assertThat(response.getData()).extracting(FinremCaseData::getFastTrackDecisionReason)
                .isNull();
        } else {
            assertThat(response.getData()).extracting(FinremCaseData::getFastTrackDecisionReason)
                .isEqualTo(fastTrackDecisionReason);
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    @NullSource
    void givenOtherReasonForComplexity_whenHandled_thenClearOtherReasonForComplexityText(YesOrNo otherReasonForComplexity) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        String otherReasonForComplexityText = "otherReasonForComplexityText";
        finremCaseData.setOtherReasonForComplexity(otherReasonForComplexity);
        finremCaseData.setOtherReasonForComplexityText(otherReasonForComplexityText);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (YesOrNo.NO.equals(otherReasonForComplexity)) {
            assertThat(response.getData()).extracting(FinremCaseData::getOtherReasonForComplexityText)
                .isNull();
        } else {
            assertThat(response.getData()).extracting(FinremCaseData::getOtherReasonForComplexityText)
                .isEqualTo(otherReasonForComplexityText);
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    @NullSource
    void givenApplicantsHomeCourt_whenHandled_thenClearReasonForLocalCourt(YesOrNo isApplicantsHomeCourt) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        String reasonForLocalCourt = "reasonForLocalCourt";
        finremCaseData.setIsApplicantsHomeCourt(isApplicantsHomeCourt);
        finremCaseData.setReasonForLocalCourt(reasonForLocalCourt);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (YesOrNo.NO.equals(isApplicantsHomeCourt)) {
            assertThat(response.getData()).extracting(FinremCaseData::getReasonForLocalCourt)
                .isNull();
        } else {
            assertThat(response.getData()).extracting(FinremCaseData::getReasonForLocalCourt)
                .isEqualTo(reasonForLocalCourt);
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    @NullSource
    void givenAllocatedToBeHeardAtHighCourtJudgeLevel_whenHandled_thenClearAllocatedToBeHeardAtHighCourtJudgeLevelText(
            YesOrNo allocatedToBeHeardAtHighCourtJudgeLevel) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getDivorceStageReached()).thenReturn(mock(StageReached.class));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        String allocatedToBeHeardAtHighCourtJudgeLevelText = "allocatedToBeHeardAtHighCourtJudgeLevelText";
        finremCaseData.setAllocatedToBeHeardAtHighCourtJudgeLevel(allocatedToBeHeardAtHighCourtJudgeLevel);
        finremCaseData.setAllocatedToBeHeardAtHighCourtJudgeLevelText(allocatedToBeHeardAtHighCourtJudgeLevelText);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (YesOrNo.NO.equals(allocatedToBeHeardAtHighCourtJudgeLevel)) {
            assertThat(response.getData()).extracting(FinremCaseData::getAllocatedToBeHeardAtHighCourtJudgeLevelText)
                .isNull();
        } else {
            assertThat(response.getData()).extracting(FinremCaseData::getAllocatedToBeHeardAtHighCourtJudgeLevelText)
                .isEqualTo(allocatedToBeHeardAtHighCourtJudgeLevelText);
        }
    }
}
