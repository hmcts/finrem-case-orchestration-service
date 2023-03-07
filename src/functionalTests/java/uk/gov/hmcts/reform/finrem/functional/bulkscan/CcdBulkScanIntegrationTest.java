package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;
import uk.gov.hmcts.reform.finrem.functional.model.UserDetails;
import uk.gov.hmcts.reform.finrem.functional.util.ServiceUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration.class)
public class CcdBulkScanIntegrationTest {

    private static final String FORM_A_JSON = "json/bulkscan/formA.json";
    private static final String DIVORCE_JURISDICTION_ID = "DIVORCE";
    private static final String FR_NEW_PAPER_CASE_EVENT_ID = "FR_newPaperCase";
    private static final String DIVORCE_SERVICE_AUTHORISED_WITH_CCD = "divorce_ccd_submission";

    @Autowired
    private IdamUtils idamUtils;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ServiceUtils serviceUtils;

    @Value("${case.orchestration.api-bsp}")
    private String cosBaseUrl;

    @Value("${auth.provider.bulkscan.update.microservice}")
    private String bulkScanTransformationAndUpdateMicroservice;

    @Test
    @Ignore
    public void givenOcrPayload_whenTransformedPayloadUploadedToCcd_thenCaseIsCreated() throws Exception {
        var formA = ResourceLoader.loadJsonToObject(FORM_A_JSON, Map.class);
        setScannedDocumentsUrls(formA);

        var transformedOcrData = transformOcrData(new JSONObject(formA).toString());
        var caseCreationDetails = ResourceLoader.jsonToObject(transformedOcrData.getBytes(StandardCharsets.UTF_8), Map.class);
        var caseData = ((Map) caseCreationDetails.get("case_creation_details")).get("case_data");
        var userDetails = idamUtils.createCaseworkerUser();

        log.info("case data = {}, user details = {}", caseData, userDetails);
        CaseDetails caseDetails = submitCase(caseData, userDetails);

        //Scanned documents
        Map<String, Object> persistedCaseData = caseDetails.getData();
        assertThat(persistedCaseData, hasEntry("paperApplication", "Yes"));
        assertThat(persistedCaseData, hasKey("formA"));
        assertThat(persistedCaseData, hasKey("scannedD81s"));
        assertThat((List<?>) persistedCaseData.get("scannedD81s"), hasSize(2));
        assertThat(persistedCaseData, hasKey("pensionCollection"));
        assertThat((List<?>) persistedCaseData.get("pensionCollection"), hasSize(5));//P1, PPF1, P2, PPF2, PPF
        assertThat(persistedCaseData, hasKey("otherCollection"));
        assertThat((List<?>) persistedCaseData.get("otherCollection"), hasSize(3));//FormE, CoverLetter, OtherSupportDocuments
        assertThat(persistedCaseData, hasKey("consentOrder"));//Draft consent order
        assertThat(persistedCaseData, hasKey("latestConsentOrder"));//Draft consent order
        assertThat(persistedCaseData, hasKey("divorceUploadEvidence1"));//DecreeNisi
        assertThat(persistedCaseData, hasKey("divorceUploadEvidence2"));//DecreeAbsolute
    }

    @After
    public void cleanUp() {
        idamUtils.deleteTestUsers();
    }

    private String transformOcrData(String body) {
        String token = idamUtils.generateServiceTokenWithValidMicroservice(bulkScanTransformationAndUpdateMicroservice);
        Response response = SerenityRest.given()
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(body)
            .post(cosBaseUrl + "/transform-exception-record");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        return response.body().asString();
    }

    private void setScannedDocumentsUrls(Map<String, Object> formA) throws Exception {
        var uploadedDoc = serviceUtils.uploadFileToEmStore("fileTypes/sample.pdf", "application/pdf");
        var scannedDocuments = (List)formA.get("scanned_documents");
        for (var document : scannedDocuments) {
            var url = (Map)((Map)document).get("url");
            url.put("document_url", uploadedDoc.get("document_url"));
            url.put("document_binary_url", uploadedDoc.get("document_binary_url"));
        }
    }

    private CaseDetails submitCase(Object caseData, UserDetails userDetails) {
        String serviceToken = idamUtils.generateServiceTokenWithValidMicroservice(DIVORCE_SERVICE_AUTHORISED_WITH_CCD);

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            bearer.apply(userDetails.getAuthToken()),
            serviceToken,
            userDetails.getId(),
            DIVORCE_JURISDICTION_ID,
            CaseType.CONSENTED.getCcdType(),
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
            bearer.apply(userDetails.getAuthToken()),
            serviceToken,
            userDetails.getId(),
            DIVORCE_JURISDICTION_ID,
            CaseType.CONSENTED.getCcdType(),
            true,
            caseDataContent);

        log.info("Created case ID {}", caseDetails.getId());
        return caseDetails;
    }

    private Function<String, String> bearer = token -> String.format("Bearer %s", token);

}