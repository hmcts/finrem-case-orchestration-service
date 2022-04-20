package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.idam.client.CoreFeignConfiguration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

@FeignClient(name = "idam-api", url = "${idam.api.url}", configuration = CoreFeignConfiguration.class)
public interface IdamAuthApi {
    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse generateOpenIdToken(@RequestBody TokenRequest tokenRequest);
}
