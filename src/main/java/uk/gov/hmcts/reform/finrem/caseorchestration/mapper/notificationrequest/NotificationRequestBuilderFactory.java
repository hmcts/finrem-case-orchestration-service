package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRequestBuilderFactory {

    private final ObjectProvider<NotificationRequestBuilder> builderProvider;

    /**
     * Creates a new instance of {@link NotificationRequestBuilder} to build
     * {@link uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest}.
     * This method is used to ensure that each NotificationRequest can be built independently without side effects.
     * @return a new instance of {@link NotificationRequestBuilder}
     */
    public NotificationRequestBuilder newInstance() {
        return builderProvider.getObject();
    }
}
