package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AbstractNotificationRequestMapper {

    private final NotificationRequestBuilderFactory builderFactory;

    /**
     * Creates a new instance of {@link NotificationRequestBuilder} to build
     * {@link uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest}.
     * @return a new instance of {@link NotificationRequestBuilder}
     */
    protected NotificationRequestBuilder notificationRequestBuilder() {
        return builderFactory.newInstance();
    }
}


