package uk.gov.hmcts.reform.finrem.caseorchestration.insights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
@ConditionalOnProperty("azure.app_insights_key")
public class AppInsights extends AbstractAppInsights {
    @Autowired
    public AppInsights(TelemetryClient client) {
        super(client);
    }
}

