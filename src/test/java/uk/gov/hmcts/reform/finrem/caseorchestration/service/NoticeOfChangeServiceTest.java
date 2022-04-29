package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class NoticeOfChangeServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/noticeOfChange/caseworkerNoc/";
    private static final String CHANGE_OF_REPRESENTATIVES = "ChangeOfRepresentatives";

    @Autowired private NoticeOfChangeService noticeOfChangeService;

    @MockBean private CaseDataService mockCaseDataService;

    @MockBean private IdamService mockIdamService;

    private CallbackRequest callbackRequest;

    private final Function<Map<String, Object>, List<Element<RepresentationUpdate>>> getFirstChangeElement =
        this::convertToChangeOfRepresentation;


    @Before
    public void setUp() {
        mapper.registerModule(new JavaTimeModule());
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void shouldUpdateRepresentationAndGenerateChangeOfRepresentatives_whenCaseDataIsValid() throws Exception {
        setUpCaseDetails("change-of-representatives.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream(PATH + "change-of-representatives-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-representatives-original-data.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);

            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateRepresentationAndUpdateChangeOfRepresentatives_whenChangeAlreadyPopulated() throws Exception  {
        setUpCaseDetails("change-of-representatives.json");
        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-populated-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            List<Element<RepresentationUpdate>> actual = getFirstChangeElement.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyUnpopulated() throws Exception {
        setUpCaseDetails("consented-change-of-reps.json");

        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "consented-change-of-reps-before.json")) {

            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void inConsented_shouldUpdateChangeOfRepresentatives_whenChangeCurrentlyPopulated() throws Exception  {
        setUpCaseDetails("consented-change-of-reps.json");
        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "consented-change-of-reps.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "consented-change-of-reps-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(), originalDetails);
            List<Element<RepresentationUpdate>> actual = getFirstChangeElement.apply(caseData);
            RepresentationUpdate actualChange = actual.get(1).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actual).hasSize(2);
            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expectedChange.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateChangeOfRepresentatives_whenNatureIsRemoving() throws Exception {
        setUpCaseDetails("change-of-reps-removing.json");
        setUpHelper();
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(true);

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-reps-removing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-removing-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expectedChange = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expectedChange.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expectedChange.getParty());
            assertThat(actualChange.getRemoved()).isEqualTo(expectedChange.getRemoved());
            assertThat(actualChange.getBy()).isEqualTo(expectedChange.getBy());
        }
    }

    @Test
    public void shouldUpdateChangeOfRepresentatives_whenNatureIsReplacing() throws Exception {
        setUpCaseDetails("change-of-reps-replacing.json");

        setUpHelper();

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-reps-replacing-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH + "change-of-reps-replacing-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();
            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);

            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expected = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expected.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded()); //added = old sol
            assertThat(actualChange.getRemoved().getOrganisation()).isEqualTo(expected.getRemoved().getOrganisation());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    @Test
    public void shouldUpdateChangeOfRepresentativesRespondent() throws Exception {
        setUpCaseDetails("change-of-representatives-respondent.json");
        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");

        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH
            + "change-of-representatives-respondent-before.json")) {
            CallbackRequest actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            InputStream is = getClass().getResourceAsStream(PATH
                + "change-of-representatives-respondent-original.json");
            CaseDetails originalDetails = mapper.readValue(is, CallbackRequest.class).getCaseDetails();

            Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(actualRequest.getCaseDetails(),
                authTokenGenerator.generate(),
                originalDetails);
            RepresentationUpdate actualChange = getFirstChangeElement.apply(caseData).get(0).getValue();
            RepresentationUpdate expected = getFirstChangeElement.apply(callbackRequest.getCaseDetails()
                .getData()).get(0).getValue();

            assertThat(actualChange.getClientName()).isEqualTo(expected.getClientName());
            assertThat(actualChange.getParty()).isEqualTo(expected.getParty());
            assertThat(actualChange.getAdded()).isEqualTo(expected.getAdded());
            assertThat(actualChange.getBy()).isEqualTo(expected.getBy());
        }
    }

    private List<Element<RepresentationUpdate>> convertToChangeOfRepresentation(Map<String, Object> data) {
        System.out.println(data);
        return mapper.convertValue(data.get(CHANGE_OF_REPRESENTATIVES),
            new TypeReference<>() {});
    }

    private void setUpHelper() {

        when(mockIdamService.getIdamFullName(any())).thenReturn("Claire Mumford");
        when(mockCaseDataService.buildFullApplicantName(any())).thenReturn("John Smith");
        when(mockCaseDataService.buildFullRespondentName(any())).thenReturn("Jane Smith");
        when(mockCaseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(mockCaseDataService.isConsentedApplication(any())).thenReturn(false);
    }

}

