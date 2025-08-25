package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationsRequestBuilderFactoryTest {

    @Test
    void testNewInstance() {
        @SuppressWarnings("unchecked")
        ObjectProvider<NotificationRequestBuilder> builderProvider = mock(ObjectProvider.class);
        when(builderProvider.getObject()).thenReturn(mock(NotificationRequestBuilder.class));
        NotificationRequestBuilderFactory factory = new NotificationRequestBuilderFactory(builderProvider);

        NotificationRequestBuilder builder = factory.newInstance();

        assertThat(builder).isInstanceOf(NotificationRequestBuilder.class);
    }
}
