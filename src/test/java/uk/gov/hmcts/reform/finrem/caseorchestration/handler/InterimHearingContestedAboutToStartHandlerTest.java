package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollectionItemData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToStartHandlerTest extends BaseHandlerTest {

    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;

    public static final String AUTH_TOKEN = "tokien:)";

    private static final String CONTESTED_INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing.json";
    private static final String TEST_NEW_JSON = "/fixtures/contested/interim-hearing-with-no-existing-hearing.json";
    private static final String TEST_NEW_JSON_NO_UPLOADED_DOC = "/fixtures/contested/interim-hearing-nouploaded-doc.json";



    @Before
    public void setup() {
        InterimHearingItemMapper interimHearingItemMapper = new InterimHearingItemMapper(new DocumentHelper(objectMapper,
            new CaseDataService()));
        interimHearingContestedAboutToStartHandler  =
            new InterimHearingContestedAboutToStartHandler(interimHearingItemMapper);
    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenCase_WhenInterimHearingPresent_ThenMigrateToInterimHearingCollection() {
        CallbackRequest callbackRequest = getCallbackRequestFromResource(CONTESTED_INTERIM_HEARING_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());
        assertNotNull(interimHearingList);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList = caseData.getInterimWrapper().getInterimHearingDocuments();

        assertEquals(1, bulkPrintDocumentsList.size());

        List<InterimHearingCollectionItemData> trackingList = caseData.getInterimWrapper().getInterimHearingCollectionItemIds();

        assertEquals(interimHearingList.get(0).getId().toString(), trackingList.get(0).getValue().getIhItemIds());
    }

    @Test
    public void givenCase_WhenMigrateToInterimHearingCollectionButNoUploadedDocAndNoBulkPrintDoc_ThenItShouldMigratedSuccessfully() {
        CallbackRequest callbackRequest = getCallbackRequestFromResource(TEST_NEW_JSON_NO_UPLOADED_DOC);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        final List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());
        assertNotNull(interimHearingList);

        final List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            caseData.getInterimWrapper().getInterimHearingDocuments();
        assertEquals(1, bulkPrintDocumentsList.size());

        final List<InterimHearingCollectionItemData> trackingList = caseData.getInterimWrapper().getInterimHearingCollectionItemIds();
        assertEquals(interimHearingList.get(0).getId().toString(), trackingList.get(0).getValue().getIhItemIds());
    }

    @Test
    public void givenCase_WhenInterimHearingPresent_ThenNothingToMigrateInInterimHearingCollection() {
        CallbackRequest callbackRequest = getCallbackRequestFromResource(TEST_NEW_JSON);
        AboutToStartOrSubmitCallbackResponse handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());
        assertNotNull(interimHearingList);

        List<InterimHearingCollectionItemData> trackingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearingCollectionItemIds())
            .orElse(new ArrayList<>());

        assertThat(interimHearingList, is(Collections.emptyList()));
        assertThat(trackingList, is(Collections.emptyList()));
    }
}