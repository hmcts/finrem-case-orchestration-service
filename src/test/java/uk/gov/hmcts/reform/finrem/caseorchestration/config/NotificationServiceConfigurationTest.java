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
        assertEquals("/consent-order-available-ctsc", underTest.getConsentOrderAvailableCtsc());
        assertEquals("/transfer-to-local-court", underTest.getTransferToLocalCourt());
        assertEquals("fr_applicant_sol@sharklasers.com", underTest.getCtscEmail());
        assertEquals("/prepare-for-hearing", underTest.getPrepareForHearing());
        assertEquals("/contested/draft-order", underTest.getContestedDraftOrder());
        assertEquals("/contested/order-not-approved", underTest.getContestedOrderNotApproved());
        assertEquals("/contested/consent-order-approved", underTest.getContestedConsentOrderApproved());
        assertEquals("/contested/consent-general-order", underTest.getContestedConsentGeneralOrder());
        assertEquals("/contested/general-order", underTest.getContestedGeneralOrder());
        assertEquals("/general-order", underTest.getConsentedGeneralOrder());
        assertEquals("/contested/consent-order-not-approved", underTest.getContestedConsentOrderNotApproved());
        assertEquals("/contested/general-application-refer-to-judge", underTest.getContestedGeneralApplicationReferToJudge());
        assertEquals("/contested/general-application-outcome", underTest.getContestedGeneralApplicationOutcome());
        assertEquals("/contested/prepare-for-interim-hearing-sent", underTest.getPrepareForInterimHearing());
        assertEquals("http://localhost:8086/", underTest.getUrl());
    }
}