package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateContactDetailsSubmittedHandlerTest {
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private GenerateCoverSheetService generateCoverSheetService;
    @InjectMocks
    private UpdateContactDetailsSubmittedHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new UpdateContactDetailsSubmittedHandler(finremCaseDetailsMapper, generateCoverSheetService);
    }

    @Test
    void testCanHandle() {
        assertTrue(handler.canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.UPDATE_CONTACT_DETAILS));
        assertTrue(handler.canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPDATE_CONTACT_DETAILS));
    }

    @Test
    void shouldNotGenerateCoverSheetsWhenContactDetailsUnchanged() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("John")
                        .applicantLname("Smith")
                        .build())
                .build();
        FinremCaseData caseDataAfter = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("John")
                        .applicantLname("Smith")
                        .build())
                .build();
        FinremCaseDetails before = FinremCaseDetails.builder().data(caseDataBefore).build();
        FinremCaseDetails after = FinremCaseDetails.builder().data(caseDataAfter).build();
        FinremCallbackRequest request = mock(FinremCallbackRequest.class);
        when(request.getCaseDetails()).thenReturn(after);
        when(request.getCaseDetailsBefore()).thenReturn(before);

        handler.handle(request, "auth");

        verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(any(), any());
        verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(any(), any());
    }

    @Test
    void shouldGenerateOnlyApplicantCoverSheetWhenApplicantAddressChanged() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("John")
                        .applicantLname("Smith")
                        .applicantAddress(Address.builder().addressLine1("123 Main St").build())
                        .respondentAddress(Address.builder().addressLine1("789 Side St").build())
                        .build())
                .build();
        FinremCaseData caseDataAfter = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("John")
                        .applicantLname("Smith")
                        .applicantAddress(Address.builder().addressLine1("456 Main St").build()) // changed
                        .respondentAddress(Address.builder().addressLine1("789 Side St").build())
                        .build())
                .build();
        FinremCaseDetails before = FinremCaseDetails.builder().data(caseDataBefore).build();
        FinremCaseDetails after = FinremCaseDetails.builder().data(caseDataAfter).build();
        FinremCallbackRequest request = mock(FinremCallbackRequest.class);
        when(request.getCaseDetails()).thenReturn(after);
        when(request.getCaseDetailsBefore()).thenReturn(before);

        handler.handle(request, "auth");

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(after, "auth");
        verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(any(), any());
    }

    @Test
    void shouldGenerateOnlyRespondentCoverSheetWhenRespondentAddressChanged() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .respondentFmName("Jane")
                        .respondentLname("Doe")
                        .applicantAddress(Address.builder().addressLine1("123 Main St").build())
                        .respondentAddress(Address.builder().addressLine1("789 Side St").build())
                        .build())
                .build();
        FinremCaseData caseDataAfter = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .respondentFmName("Jane")
                        .respondentLname("Doe")
                        .applicantAddress(Address.builder().addressLine1("123 Main St").build())
                        .respondentAddress(Address.builder().addressLine1("1010 Side St").build()) // changed
                        .build())
                .build();
        FinremCaseDetails before = FinremCaseDetails.builder().data(caseDataBefore).build();
        FinremCaseDetails after = FinremCaseDetails.builder().data(caseDataAfter).build();
        FinremCallbackRequest request = mock(FinremCallbackRequest.class);
        when(request.getCaseDetails()).thenReturn(after);
        when(request.getCaseDetailsBefore()).thenReturn(before);

        handler.handle(request, "auth");

        verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(any(), any());
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(after, "auth");
    }

    @Test
    void shouldNotGenerateCoverSheetsWhenAddressesAreNullOrUnchanged() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantAddress(null)
                        .respondentAddress(null)
                        .build())
                .build();
        FinremCaseData caseDataAfter = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantAddress(null)
                        .respondentAddress(null)
                        .build())
                .build();
        FinremCaseDetails before = FinremCaseDetails.builder().data(caseDataBefore).build();
        FinremCaseDetails after = FinremCaseDetails.builder().data(caseDataAfter).build();
        FinremCallbackRequest request = mock(FinremCallbackRequest.class);
        when(request.getCaseDetails()).thenReturn(after);
        when(request.getCaseDetailsBefore()).thenReturn(before);

        handler.handle(request, "auth");

        verify(generateCoverSheetService, never()).generateAndSetApplicantCoverSheet(any(), any());
        verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(any(), any());
    }

    @Test
    void shouldGenerateOnlyApplicantCoverSheetWhenApplicantNameChanged() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("John")
                        .applicantLname("Smith")
                        .build())
                .build();
        FinremCaseData caseDataAfter = FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("Jane") // changed
                        .applicantLname("Smith")
                        .build())
                .build();
        FinremCaseDetails before = FinremCaseDetails.builder().data(caseDataBefore).build();
        FinremCaseDetails after = FinremCaseDetails.builder().data(caseDataAfter).build();
        FinremCallbackRequest request = mock(FinremCallbackRequest.class);
        when(request.getCaseDetails()).thenReturn(after);
        when(request.getCaseDetailsBefore()).thenReturn(before);

        handler.handle(request, "auth");

        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(after, "auth");
        verify(generateCoverSheetService, never()).generateAndSetRespondentCoverSheet(any(), any());
    }
}
