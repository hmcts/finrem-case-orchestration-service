package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import feign.FeignException;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class Feign500RetryTest extends BaseServiceTest {

    @Autowired
    DocumentValidationService documentValidationService;

    @ClassRule
    public static WireMockClassRule documentService = new WireMockClassRule(4009);

    @Test
    public void whenDocumentServiceReturns500_thenFeignWillRetry3Times() {
        CaseDetails caseDetails = Mockito.mock(CaseDetails.class);
        when(caseDetails.getData()).thenReturn(new HashMap<String, Object>() {
            {
                put("consentOrder", new HashMap<String, String>() {
                    {
                        put("document_binary_url", "test");
                    }
                });
            }
        });
        CallbackRequest callbackRequest = Mockito.mock(CallbackRequest.class);
        when(callbackRequest.getEventId()).thenReturn("FR_SolicitorCreate");
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        documentService.stubFor(get(urlPathMatching("/file-upload-check.*"))
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        );
        try {
            documentValidationService.validateDocument(callbackRequest, "consentOrder", null);
        } catch (FeignException feignException) {
            /* ignore the exception */
        }
        Assert.assertThat(documentService.countRequestsMatching(RequestPatternBuilder.newRequestPattern(
            RequestMethod.GET, urlPathMatching("/file-upload-check.*")).build()).getCount(),
            is(3));
    }
}
