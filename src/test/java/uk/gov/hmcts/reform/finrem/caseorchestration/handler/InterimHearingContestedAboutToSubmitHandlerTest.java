package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENTS_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PARTIES_ON_CASE;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private InterimHearingService interimHearingService;
    @Spy
    private PartyService partyService =
        new PartyService(new FinremCaseDetailsMapper(JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()));;
    @InjectMocks
    private InterimHearingContestedAboutToSubmitHandler interimHearingContestedAboutToSubmitHandler;

    private InterimHearingHelper interimHearingHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    @Before
    public void setup() {
        interimHearingHelper = new InterimHearingHelper(objectMapper);
    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEvent() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenContestedCase_WhenMultipleInterimHearing_ThenHearingsShouldBePresentInChronologicalOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_JSON);
        callbackRequest.getCaseDetails().getData().put(PARTIES_ON_CASE, getIntervenerParties());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> caseData = response.getData();
        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());
        assertEquals(getAllParties().getValue().size(),
            ((DynamicMultiSelectList) response.getData().get(PARTIES_ON_CASE)).getValue().size());

        verify(interimHearingService).submitInterimHearing(any(), any(), any());
        verifyNonCollectionData(caseData);
    }

    @Test
    public void shouldHandleErrorsReturnedFromService() {
        CallbackRequest callbackRequest = buildCallbackRequest(TEST_JSON);
        when(interimHearingService.submitInterimHearing(any(), any(), any()))
            .thenReturn(List.of("Error 1", "Error 2"));
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(2));

    }

    private void verifyNonCollectionData(Map<String, Object> data) {
        assertNull(data.get(INTERIM_HEARING_TYPE));
        assertNull(data.get(INTERIM_HEARING_DATE));
        assertNull(data.get(INTERIM_HEARING_TIME));
        assertNull(data.get(INTERIM_HEARING_TIME_ESTIMATE));
        assertNull(data.get(INTERIM_HEARING_REGION_LIST));
        assertNull(data.get(INTERIM_HEARING_CFC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_WALES_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LONDON_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DEVON_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_DORSET_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_HUMBER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BRISTOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NEWPORT_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHEAST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SOUTHWEST_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_SWANSEA_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LIVERPOOL_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BIRMINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_CLEAVELAND_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_KENT_SURREY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_LANCASHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_MANCHESTER_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NORTHWALES_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NOTTINGHAM_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_THAMESVALLEY_COURT_LIST));
        assertNull(data.get(INTERIM_HEARING_ADDITIONAL_INFO));
        assertNull(data.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        assertNull(data.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
    }

    private DynamicMultiSelectList getAllParties() {
        DynamicMultiSelectList allParties = getSolicitorParties();
        allParties.getValue().addAll(getIntervenerParties().getValue());
        allParties.getListItems().addAll(getIntervenerParties().getListItems());
        return allParties;
    }

    private DynamicMultiSelectList getSolicitorParties() {

        return DynamicMultiSelectList.builder()
            .value(new ArrayList<>(of(DynamicMultiSelectListElement.builder()
                    .code(CaseRole.APP_SOLICITOR.getCcdCode())
                    .label(CaseRole.APP_SOLICITOR.getCcdCode())
                    .build(),
                DynamicMultiSelectListElement.builder()
                    .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                    .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                    .build())))
            .listItems(new ArrayList<>(of(DynamicMultiSelectListElement.builder()
                    .code(CaseRole.APP_SOLICITOR.getCcdCode())
                    .label(CaseRole.APP_SOLICITOR.getCcdCode())
                    .build(),
                DynamicMultiSelectListElement.builder()
                    .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                    .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                    .build())))
            .build();
    }

    private DynamicMultiSelectList getIntervenerParties() {

        return DynamicMultiSelectList.builder()
            .value(new ArrayList<>(of(DynamicMultiSelectListElement.builder()
                    .code(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                    .label(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                    .build(),
                DynamicMultiSelectListElement.builder()
                    .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .build())))
            .listItems(new ArrayList<>(of(DynamicMultiSelectListElement.builder()
                    .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .build(),
                DynamicMultiSelectListElement.builder()
                    .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                    .build())))
            .build();
    }
}
