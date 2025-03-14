package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.SolicitorCreateContestedMidHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedMidHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private SolicitorCreateContestedMidHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private InternationalPostalService postalService;

    @Mock
    private SelectedCourtService selectedCourtService;

    @Mock
    private ExpressCaseService expressCaseService;

    @Mock
    private FeatureToggleService featureToggleService;


    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    @BeforeEach
    public void init() {
        handler = new SolicitorCreateContestedMidHandler(
            finremCaseDetailsMapper,
            postalService,
            selectedCourtService,
            expressCaseService,
            featureToggleService
        );
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .build();
    }

    @Test
    void testPostalServiceValidationCalled() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(postalService, times(1))
            .validate(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void testSetSelectedCourtDetailsIfPresentCalled() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(selectedCourtService, times(1))
            .setSelectedCourtDetailsIfPresent(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void givenContestedCase_WhenNotEmptyPostCode_thenHandlerWillShowNoErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 1AA"
        ));

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
        ));

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            "SW1A 2AA"
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(0, handle.getErrors().size());
    }

    @Test
    void givenContestedCase_WhenEmptyApplicantSolicitorPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setApplicantSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(APPLICANT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyApplicantPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();


        data.getContactDetailsWrapper().setApplicantAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(APPLICANT_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyRespondentSolicitorPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setRespondentSolicitorAddress(new Address());

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactly(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
    }

    @Test
    void givenContestedCase_WhenEmptyRespondentPostCode_thenHandlerWillShowErrorMessage() {

        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();

        data.getContactDetailsWrapper().setRespondentAddress(new Address(
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "County",
            "Country",
            "Town",
            null
        ));

        data.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        data.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors())
            .containsExactlyInAnyOrder(RESPONDENT_POSTCODE_ERROR);
    }
}