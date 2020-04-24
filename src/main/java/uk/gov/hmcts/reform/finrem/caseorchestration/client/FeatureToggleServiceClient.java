package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ff4j.FeatureToggle;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "feature-toggle-service-client", url = "${feature-toggle.service.api.baseurl}")
public interface FeatureToggleServiceClient {

    @GetMapping(value = "/api/ff4j/store/features/{feature_name}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    FeatureToggle getToggle(@PathVariable("feature_name") String featureName);

}
