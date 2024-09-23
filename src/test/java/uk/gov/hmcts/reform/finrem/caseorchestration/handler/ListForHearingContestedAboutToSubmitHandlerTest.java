package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENTS_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PARTIES_ON_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ListForHearingContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private ValidateHearingService validateHearingService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GenerateCoverSheetService coverSheetService;
    @Mock
    private PartyService partyService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ListForHearingContestedAboutToSubmitHandler aboutToSubmitHandler;
    @InjectMocks
    private ListForHearingContestedAboutToStartHandler aboutToStartHandler;

    private static final String BULK_PRINT_ADDITIONAL_HEARING_JSON = "/fixtures/bulkprint/bulk-print-additional-hearing.json";
    private static final String NON_FAST_TRACK_HEARING_JSON = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";
    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY = "Issue Date, fast track decision or hearingDate is empty";
    private static final String FAST_TRACK_WARNING = "Date of the hearing must be between 12 and 14 weeks.";

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(aboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING);
    }

    @Test
    void givenContestedCase_whenNotFastTrackDecision_thenShouldThrowWarnings() {
        when(validateHearingService.validateHearingErrors(any())).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(any()))
                .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));
        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);
        FinremCallbackRequest finremCallbackRequest =
                buildFinremCallbackRequest(NON_FAST_TRACK_HEARING_JSON);
        CallbackRequest callbackRequest = buildCallbackRequest(NON_FAST_TRACK_HEARING_JSON);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class))).thenReturn(finremCallbackRequest.getCaseDetails());
        when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class))).thenReturn(callbackRequest.getCaseDetails());
        finremCallbackRequest.getCaseDetails().getData().setIssueDate(LocalDate.now().minusDays(1));
        finremCallbackRequest.getCaseDetails().getData().setHearingDate(LocalDate.now().plusDays(1));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings().get(0), equalTo(FAST_TRACK_WARNING));
    }

    @Test
    void givenContestedCase_whenHandledWithEmptyIssueAndHearingDates_thenShouldThrowError() {
        when(validateHearingService.validateHearingErrors(isA(FinremCaseDetails.class)))
            .thenReturn(ImmutableList.of(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest("/fixtures/pba-validate.json");
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors().get(0), equalTo(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    void givenContestedCase_whenHandled_thenSuccessfullyGeneratesAdditionalHearingDocument() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        CallbackRequest callbackRequest = buildCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        FinremCaseDetails finremCaseDetailsBefore =
            FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().build()).build();
        finremCallbackRequest.setCaseDetailsBefore(finremCaseDetailsBefore);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put(PARTIES_ON_CASE, getParties());
        caseDetails.getData().put(ADDITIONAL_HEARING_DOCUMENTS_OPTION, YesOrNo.NO);

        when(partyService.getAllActivePartyList(any(FinremCaseDetails.class))).thenReturn(getParties());

        aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class))).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class))).thenReturn(finremCallbackRequest.getCaseDetails());

        when(hearingDocumentService.alreadyHadFirstHearing(any(FinremCaseDetails.class))).thenReturn(true);

        when(validateHearingService.validateHearingErrors(any(FinremCaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(finremCallbackRequest.getCaseDetails())).thenReturn(ImmutableList.of());
        when(caseDataService.isApplicantAddressConfidential(any(FinremCaseData.class))).thenReturn(false);
        when(caseDataService.isRespondentAddressConfidential(any(FinremCaseData.class))).thenReturn(false);
        when(coverSheetService.generateApplicantCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument());
        when(coverSheetService.generateRespondentCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument());
        CaseDocument document = caseDocument("http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336",
                "api.docx", "http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336/binary");
        when(objectMapper.convertValue(any(), eq(CaseDocument.class))).thenReturn(document);
        when(additionalHearingDocumentService.convertToPdf(any(CaseDocument.class), anyString(), anyString())).thenReturn(document);
        when(caseDataService.isContestedApplication(any(FinremCaseDetails.class))).thenReturn(true);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential(), nullValue());
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential(), nullValue());

        verify(hearingDocumentService, times(0)).generateHearingDocuments(eq(AUTH_TOKEN), any());
        verify(additionalHearingDocumentService, times(1)).createAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(coverSheetService).generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    void givenContestedCase_whenHandledAndNotHadFirstHearing_thenSuccessfullyGeneratesFormCAndG() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        CallbackRequest callbackRequest = buildCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        FinremCaseDetails finremCaseDetailsBefore =
            FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().build()).build();
        finremCallbackRequest.setCaseDetailsBefore(finremCaseDetailsBefore);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put(PARTIES_ON_CASE, getParties());
        caseDetails.getData().put(ADDITIONAL_HEARING_DOCUMENTS_OPTION, YesOrNo.NO);

        when(partyService.getAllActivePartyList(any(FinremCaseDetails.class))).thenReturn(getParties());

        aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class))).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class))).thenReturn(finremCallbackRequest.getCaseDetails());

        when(hearingDocumentService.alreadyHadFirstHearing(any(FinremCaseDetails.class))).thenReturn(false);

        when(validateHearingService.validateHearingErrors(any(FinremCaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(finremCallbackRequest.getCaseDetails())).thenReturn(ImmutableList.of());
        when(caseDataService.isApplicantAddressConfidential(any(FinremCaseData.class))).thenReturn(false);
        when(caseDataService.isRespondentAddressConfidential(any(FinremCaseData.class))).thenReturn(false);
        when(coverSheetService.generateApplicantCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument());
        when(coverSheetService.generateRespondentCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseDocument());
        CaseDocument document = caseDocument("http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336",
            "api.docx", "http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336/binary");
        when(objectMapper.convertValue(any(), eq(CaseDocument.class))).thenReturn(document);
        when(additionalHearingDocumentService.convertToPdf(any(CaseDocument.class), anyString(), anyString())).thenReturn(document);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential(), nullValue());
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential(), nullValue());

        verify(hearingDocumentService, times(1)).generateHearingDocuments(eq(AUTH_TOKEN), any());
        verify(additionalHearingDocumentService, times(0)).createAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(coverSheetService).generateApplicantCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
        verify(coverSheetService).generateRespondentCoverSheet(any(FinremCaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    void givenContestedCase_whenHandledForConfidentialLitigants_thenSuccessfullyGeneratesAdditionalHearingDocument() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        CallbackRequest callbackRequest = buildCallbackRequest(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        FinremCaseDetails finremCaseDetailsBefore =
                FinremCaseDetails.builder().id(123L).data(FinremCaseData.builder().build()).build();
        finremCallbackRequest.setCaseDetailsBefore(finremCaseDetailsBefore);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put(PARTIES_ON_CASE, getParties());
        caseDetails.getData().put(ADDITIONAL_HEARING_DOCUMENTS_OPTION, YesOrNo.NO);

        when(partyService.getAllActivePartyList(any(FinremCaseDetails.class))).thenReturn(getParties());

        aboutToStartHandler.handle(finremCallbackRequest, AUTH_TOKEN);

        when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class))).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any(CaseDetails.class))).thenReturn(finremCallbackRequest.getCaseDetails());

        when(hearingDocumentService.alreadyHadFirstHearing(any())).thenReturn(true);

        when(validateHearingService.validateHearingErrors(any())).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(any())).thenReturn(ImmutableList.of());
        when(caseDataService.isApplicantAddressConfidential(any(FinremCaseData.class))).thenReturn(true);
        when(caseDataService.isRespondentAddressConfidential(any(FinremCaseData.class))).thenReturn(true);
        when(coverSheetService.generateApplicantCoverSheet(isA(FinremCaseDetails.class), anyString())).thenReturn(caseDocument());
        when(coverSheetService.generateRespondentCoverSheet(isA(FinremCaseDetails.class), anyString())).thenReturn(caseDocument());
        CaseDocument document = caseDocument("http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336",
                "api.docx", "http://document-management-store:8080/documents/0ee78bf4-4b0c-433f-a054-f21ce6f99336/binary");
        when(objectMapper.convertValue(any(), eq(CaseDocument.class))).thenReturn(document);
        when(additionalHearingDocumentService.convertToPdf(any(CaseDocument.class), anyString(), anyString())).thenReturn(document);
        when(caseDataService.isContestedApplication(any(FinremCaseDetails.class))).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = aboutToSubmitHandler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetResConfidential(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetAppConfidential(), equalTo(caseDocument()));
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetRes(), nullValue());
        assertThat(response.getData().getBulkPrintCoversheetWrapper().getBulkPrintCoverSheetApp(), nullValue());

        verify(hearingDocumentService, times(0)).generateHearingDocuments(eq(AUTH_TOKEN), any());
        verify(additionalHearingDocumentService, times(1)).createAdditionalHearingDocuments(eq(AUTH_TOKEN), any());
        verify(notificationService).isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(notificationService).isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class));
        verify(coverSheetService).generateApplicantCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
        verify(coverSheetService).generateRespondentCoverSheet(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list = new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build(),
                DynamicMultiSelectListElement.builder()
                .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_SOLICITOR_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }
}
