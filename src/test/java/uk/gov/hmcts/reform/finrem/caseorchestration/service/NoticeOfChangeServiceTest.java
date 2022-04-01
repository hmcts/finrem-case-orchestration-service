package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NoticeOfChangeServiceTest extends BaseServiceTest {

    private static final String PATH = "/fixtures/";

    @Autowired private NoticeOfChangeService noticeOfChangeService;
    @Autowired private RestTemplate restTemplate;

    @MockBean private CaseDataService caseDataService;

    @MockBean private IdamService idamService;

    CallbackRequest callbackRequest;
    CaseDetails caseDetails;
    ChangeOfRepresentation newRepresentationChange;
    ChangeOfRepresentatives expected;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mapper.registerModule(new JavaTimeModule());

        ChangedRepresentative newRep = ChangedRepresentative.builder()
            .name("Sir Solicitor")
            .email("sirsolicitor1@gmail.com")
            .organisation(Organisation.builder().organisationID("123").organisationName("TestOrg").build())
            .build();

        newRepresentationChange = ChangeOfRepresentation.builder()
            .party("applicant")
            .clientName("John Smith")
            .date(LocalDate.of(2020, 06, 01))
            .by("Case Worker")
            .via("Notice of Change")
            .added(newRep)
            .build();

        expected = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(List.of(newRepresentationChange))
            .build();
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void updateChangeOfRepresentativesShouldReturnCorrectWithCorrectDataAndCurrentlyEmpty() throws Exception {
        CallbackRequest actualRequest;
        try (InputStream resourceAsStream = getClass().getResourceAsStream(PATH + "change-of-representatives-before.json")) {
            actualRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);

            ChangeOfRepresentatives actual = noticeOfChangeService.updateChangeOfRepresentatives(actualRequest.getCaseDetails(), newRepresentationChange);
            //actualRequest.getCaseDetails().getData().put("changeOfRepresentatives", mapper.writeValueAsString(actual));

            assertThat(actual).isEqualTo(expected);

//            System.out.println(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(actualRequest.getCaseDetails().getData()));
//            System.out.println();
//            setUpCaseDetails("change-of-representatives.json");
//            System.out.println(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(callbackRequest.getCaseDetails().getData()));
//            System.out.println(callbackRequest.getCaseDetails().getData());
//            assertThat(actualRequest.getCaseDetails().getData()).isEqualTo(callbackRequest.getCaseDetails().getData());
        }
    }

    @Test
    public void updateChangeOfRepresentativesShouldReturnCorrectObjectWhenCorrectDataAndAlreadyInitialised() throws Exception {
        setUpCaseDetails("change-of-representatives.json");

        ChangeOfRepresentatives actual = noticeOfChangeService.updateChangeOfRepresentatives(callbackRequest.getCaseDetails(), newRepresentationChange);

        expected = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(List.of(newRepresentationChange, newRepresentationChange))
            .build();

        assertThat(actual).isEqualTo(expected);
    }
}
