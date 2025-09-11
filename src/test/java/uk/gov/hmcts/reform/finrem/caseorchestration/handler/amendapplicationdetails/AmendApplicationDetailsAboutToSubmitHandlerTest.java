package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import org.assertj.core.api.ObjectEnumerableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
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
        contactDetailsWrapper.setRespondentSolicitorName("respondentSolicitorName");
        contactDetailsWrapper.setRespondentSolicitorFirm("respondentSolicitorFirm");
        contactDetailsWrapper.setRespondentSolicitorReference("respondentSolicitorReference");
        Address respondentSolicitorAddress = mock(Address.class);
        contactDetailsWrapper.setRespondentSolicitorAddress(respondentSolicitorAddress);
        contactDetailsWrapper.setRespondentSolicitorPhone("respondentSolicitorPhone");
        contactDetailsWrapper.setRespondentSolicitorEmail("respondentSolicitorEmail");
        contactDetailsWrapper.setRespondentSolicitorDxNumber("respondentSolicitorDxNumber");
        Address respondentAddress = mock(Address.class);
        contactDetailsWrapper.setRespondentAddress(respondentAddress);
        contactDetailsWrapper.setRespondentPhone("respondentPhone");
        contactDetailsWrapper.setRespondentEmail("respondentEmail");
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
                "respondentSolicitorName",
                "respondentSolicitorFirm",
                "respondentSolicitorReference",
                respondentSolicitorAddress,
                "respondentSolicitorPhone",
                "respondentSolicitorEmail",
                "respondentSolicitorDxNumber"
            );
            assertThat(response.getData()).extracting(
                FinremCaseData::getRespSolNotificationsEmailConsent).isEqualTo(respSolNotificationsEmailConsent);
        } else {
            respondentContactFields.containsExactly(
                respondentAddress, "respondentPhone", "respondentEmail"
            );
            respondentSolicitorContactFields.containsOnlyNulls();
            assertThat(response.getData()).extracting(
                FinremCaseData::getRespSolNotificationsEmailConsent
            ).isNull();
        }
    }
}
