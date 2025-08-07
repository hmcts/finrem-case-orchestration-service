package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@ExtendWith(MockitoExtension.class)
class BulkPrintDocumentGeneratorServiceTest {

    @InjectMocks private BulkPrintDocumentGeneratorService service;
    @Mock private AuthTokenGenerator authTokenGenerator;
    @Mock private SendLetterApi sendLetterApi;
    @Captor private ArgumentCaptor<LetterWithPdfsRequest> letterWithPdfsRequestArgumentCaptor;

    @Test
    void downloadDocuments() {
        UUID randomId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("random-string");
        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(randomId));

        UUID letterId = service.send(getBulkPrintRequest(), singletonList("abc".getBytes()));
        assertThat(letterId, is(equalTo(randomId)));
    }

    @Test
    void testSend() {
        UUID randomId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("random-string");
        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(randomId));

        service.send(getBulkPrintRequest(), singletonList("abc".getBytes()));

        verify(sendLetterApi).sendLetter(anyString(), letterWithPdfsRequestArgumentCaptor.capture());

        LetterWithPdfsRequest request = letterWithPdfsRequestArgumentCaptor.getValue();
        assertThat(request.getAdditionalData().get("letterType"), is("any"));
        assertThat(request.getAdditionalData().get("caseReferenceNumber"), is("any"));
        assertThat(request.getAdditionalData().get("caseIdentifier"), is("any"));
        assertThat(request.getAdditionalData().get("fileNames"), is(Collections.singletonList("app_docs.pdf")));
        assertThat(request.getAdditionalData().get("recipients"), is(Collections.singletonList("123")));
        assertThat(request.getAdditionalData().get("recipientParty"), is(APPLICANT));
    }

    @Test
    void throwsException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());

        BulkPrintRequest bulkPrintRequest = getBulkPrintRequest();
        List<byte[]> documents = List.of("abc".getBytes());
        assertThatThrownBy(() -> service.send(bulkPrintRequest, documents))
            .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(sendLetterApi);
    }

    @Test
    void throwsExceptionOnSendLetter() {
        when(authTokenGenerator.generate()).thenReturn("random-string");
        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class))).thenThrow(new RuntimeException());

        BulkPrintRequest bulkPrintRequest = getBulkPrintRequest();
        List<byte[]> documents = List.of("abc".getBytes());
        assertThatThrownBy(() -> service.send(bulkPrintRequest, documents))
            .isInstanceOf(RuntimeException.class);
        verify(authTokenGenerator).generate();
    }

    private static BulkPrintRequest getBulkPrintRequest() {
        return BulkPrintRequest.builder()
            .letterType("any")
            .caseId("any")
            .recipientParty(APPLICANT)
            .isInternational(true)
            .requestId("123")
            .bulkPrintDocuments(Collections.singletonList(
                BulkPrintDocument.builder().binaryFileUrl(BINARY_URL).fileName(FILE_NAME).build())).build();
    }
}
