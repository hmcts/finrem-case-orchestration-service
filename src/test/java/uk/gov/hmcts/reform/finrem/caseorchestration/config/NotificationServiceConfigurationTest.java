package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.junit.Assert.assertEquals;

public class NotificationServiceConfigurationTest extends BaseServiceTest {

    @Autowired
    private NotificationServiceConfiguration underTest;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("/notify", underTest.getApi());
        assertEquals("/hwf-successful", underTest.getHwfSuccessful());
        assertEquals("/contested/hwf-successful", underTest.getContestedHwfSuccessful());
        assertEquals("/contested/application-issued", underTest.getContestedApplicationIssued());
        assertEquals("/contested/order-approved", underTest.getContestOrderApproved());
        assertEquals("/assign-to-judge", underTest.getAssignToJudge());
        assertEquals("/consent-order-made", underTest.getConsentOrderMade());
        assertEquals("/consent-order-not-approved", underTest.getConsentOrderNotApproved());
        assertEquals("/consent-order-available", underTest.getConsentOrderAvailable());
        assertEquals("/prepare-for-hearing", underTest.getPrepareForHearing());
        assertEquals("/contested/draft-order", underTest.getContestedDraftOrder());
        assertEquals("/contested/order-not-approved", underTest.getContestedOrderNotApproved());
        assertEquals("/contested/consent-order-approved", underTest.getContestedConsentOrderApproved());
        assertEquals("http://localhost:8086/", underTest.getUrl());
    }
}