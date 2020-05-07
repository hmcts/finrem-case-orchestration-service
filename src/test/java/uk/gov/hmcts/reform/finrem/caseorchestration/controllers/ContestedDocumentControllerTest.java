package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ContestedDocumentController.class)
public class ContestedDocumentControllerTest extends MiniFormAControllerTest {

    @MockBean
    private NotificationService notificationService;

    @Test
    public void shouldSendSolicitorEmailWhenAgreed() throws Exception {
        mvc.perform(post(endpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).sendContestedApplicationIssuedEmail(any());
    }

    @Test
    public void shouldNotSendSolicitorEmailWhenNotAgreed() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision-without-email-consent.json").toURI()));

        mvc.perform(post(endpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Override
    public String endpoint() {
        return "/case-orchestration/documents/generate-contested-mini-form-a";
    }

    @Override
    public OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(documentService.generateContestedMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    @Override
    String jsonFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }
}