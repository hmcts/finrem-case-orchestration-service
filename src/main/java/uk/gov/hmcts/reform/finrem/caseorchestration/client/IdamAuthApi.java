package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.idam.client.CoreFeignConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}", configuration = CoreFeignConfiguration.class)
public interface IdamAuthApi {

    @GetMapping("/api/v1/users/{userId}")
    UserDetails getUserByUserId(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("userId") String userId
    );
}
