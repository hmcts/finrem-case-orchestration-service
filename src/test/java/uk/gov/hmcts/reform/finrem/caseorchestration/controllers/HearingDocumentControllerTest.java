package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrderDocumentCategoriser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(HearingDocumentController.class)
public class HearingDocumentControllerTest extends BaseControllerTest {

    private static final String DIRECTION_ORDER_URL = "/case-orchestration/contested-upload-direction-order";

    @MockBean
    private AdditionalHearingDocumentService additionalHearingDocumentService;
    @MockBean
    private ValidateHearingService validateHearingService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private GenerateCoverSheetService coverSheetService;
    @MockBean
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @MockBean
    private DraftOrderDocumentCategoriser draftOrderDocumentCategoriser;
    @InjectMocks
    private ObjectMapper mapper;


    @Before
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(validateHearingService.validateHearingErrors(isA(FinremCaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(FinremCaseDetails.class))).thenReturn(ImmutableList.of());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")).toURI()));
    }

    @Test
    public void generateHearingDocumentDirectionOrder_isAnotherHearingTrue() throws Exception {
        FinremCaseDetails finremCaseDetails = buildFinremCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();
        CaseDetails caseDetails = buildCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);

        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(additionalHearingDocumentService, times(1)).createAndStoreAdditionalHearingDocuments(any(), any());
        verify(draftOrderDocumentCategoriser).categorise(any(FinremCaseData.class));
    }

    @Test
    public void generateHearingDocumentDirectionOrder_CourtDetailsParseException() throws Exception {
        FinremCaseDetails finremCaseDetails = buildFinremCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();
        CaseDetails caseDetails = buildCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();
        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);

        doThrow(new CourtDetailsParseException()).when(additionalHearingDocumentService).createAndStoreAdditionalHearingDocuments(any(), any());

        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]", is(new CourtDetailsParseException().getMessage())));

        verify(draftOrderDocumentCategoriser).categorise(any(FinremCaseData.class));
    }

    @Test
    public void generateHearingDocumentDirectionOrderMostRecentEnteredAtTheTop() throws Exception {
        FinremCaseDetails finremCaseDetails = buildFinremCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();
        CaseDetails caseDetails = buildCallbackRequest(
            "/fixtures/contested/validate-hearing-successfully.json").getCaseDetails();

        when(finremCaseDetailsMapper.mapToCaseDetails(any())).thenReturn(caseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(any())).thenReturn(finremCaseDetails);

        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/validate-hearing-successfully.json")).toURI()));
        mvc.perform(post(DIRECTION_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.directionDetailsCollection[0].value.dateOfHearing", is("2023-10-01")))
            .andExpect(jsonPath("$.data.directionDetailsCollection[1].value.dateOfHearing", is("2022-12-01")))
            .andExpect(jsonPath("$.data.directionDetailsCollection[2].value.dateOfHearing", is("2020-07-01")));

        verify(additionalHearingDocumentService, times(1)).createAndStoreAdditionalHearingDocuments(any(), any());
        verify(draftOrderDocumentCategoriser).categorise(any(FinremCaseData.class));
    }

    protected CallbackRequest buildCallbackRequest(String testJson) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testJson)) {
            CaseDetails caseDetails =
                objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
            return CallbackRequest.builder().caseDetails(caseDetails).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}