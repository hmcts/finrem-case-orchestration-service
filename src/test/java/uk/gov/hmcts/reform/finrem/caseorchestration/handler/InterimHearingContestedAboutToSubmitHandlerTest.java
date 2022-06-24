package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private InterimHearingContestedAboutToSubmitHandler interimHearingContestedAboutToSubmitHandler;
    @Mock
    private InterimHearingService interimHearingService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleNonMatchEvent() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleNonMatchCallbackType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        AboutToStartOrSubmitCallbackResponse handle =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = handle.getData();
        List<InterimHearingData> interimHearingList = Optional.ofNullable(data.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());

        verify(interimHearingService).submitInterimHearing(any(), any());
        verifyNonCollectionData(data);
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
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
