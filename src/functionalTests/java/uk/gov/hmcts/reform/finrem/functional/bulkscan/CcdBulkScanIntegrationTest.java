package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;
import uk.gov.hmcts.reform.finrem.functional.model.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration.class)
public class CcdBulkScanIntegrationTest {

    private static final String FORM_A_JSON = "json/bulkscan/formA.json";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String DIVORCE_JURISDICTION_ID = "DIVORCE";
    public static final String FR_CONSENTED_CASE_TYPE = "FinancialRemedyMVP2";
    public static final String FR_NEW_PAPER_CASE_EVENT_ID = "FR_newPaperCase";

    @Autowired
    private IdamUtils idamUtils;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Value("${case.orchestration.api-bsp}")
    private String cosBaseUrl;

    @Value("${auth.provider.bulkscan.update.microservice}")
    private String bulkScanTransformationAndUpdateMicroservice;

    @Test
    public void givenOcrPayload_whenTransformedPayloadUploadedToCcd_thenCaseIsCreated() throws Exception {
        String transformedOcrData = transformOcrData(FORM_A_JSON);
        Map caseCreationDetails = ResourceLoader.jsonToObject(transformedOcrData.getBytes(StandardCharsets.UTF_8), Map.class);
        Object caseData = ((Map)caseCreationDetails.get("case_creation_details")).get("case_data");

        try {
            UserDetails userDetails = idamUtils.createCaseworkerUser();
            CaseDetails caseDetails = submitCase(caseData, userDetails);
//            CaseDetails retrievedCase = retrieveCase(userDetails);
            System.out.println("created case: " + caseDetails);
        } finally {
            idamUtils.deleteTestUsers();
        }
    }

    private String transformOcrData(String path) throws Exception {
        String token = idamUtils.generateServiceTokenWithValidMicroservice(bulkScanTransformationAndUpdateMicroservice);
        String body = ResourceLoader.loadResourceAsString(path);
        Response response = SerenityRest.given()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORISATION_HEADER, token)
                .relaxedHTTPSValidation()
                .body(body)
                .post(cosBaseUrl + "/transform-exception-record");

        assertThat(HttpStatus.valueOf(response.getStatusCode()), is(HttpStatus.OK));

        return response.body().asString();
    }

    private CaseDetails submitCase(Object caseData, UserDetails userDetails) throws JsonProcessingException {
        String serviceToken = idamUtils.generateServiceTokenWithValidMicroservice("FR_integration_test");

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
                DIVORCE_JURISDICTION_ID,
                FR_CONSENTED_CASE_TYPE,
                FR_NEW_PAPER_CASE_EVENT_ID
        );

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .caseReference("FinRem-" + UUID.randomUUID().toString())
            .data(caseData)
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("Case created")
                .description("Case created by FinRem integration test from " + cosBaseUrl)
                .build())
            .eventToken(startEventResponse.getToken())
            .build();

        CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
                userDetails.getAuthToken(),
                serviceToken,
                userDetails.getId(),
                DIVORCE_JURISDICTION_ID,
                FR_CONSENTED_CASE_TYPE,
                true,
                caseDataContent);

        log.info("Created case ID {}", caseDetails.getId());

        return caseDetails;
    }

    private CaseDetails retrieveCase(UserDetails userDetails) {
        String serviceToken = idamUtils.generateServiceTokenWithValidMicroservice("divorce_ccd_submission");

        List<CaseDetails> caseDetailsList = Optional.ofNullable(coreCaseDataApi.searchForCaseworker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            DIVORCE_JURISDICTION_ID,
            FR_CONSENTED_CASE_TYPE,
            Collections.emptyMap()
        )).orElse(Collections.EMPTY_LIST);

//        assertThat(caseDetailsList.size(), is(1));

        return caseDetailsList.get(0);
    }
}
