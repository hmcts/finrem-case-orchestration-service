package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.proxy.SendLetterApiProxy;

import java.util.Collections;
import java.util.UUID;

import static java.util.Collections.singletonList;
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

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintDocumentGeneratorServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks private BulkPrintDocumentGeneratorService service;

    @Mock private AuthTokenGenerator authTokenGenerator;
    @Mock private SendLetterApiProxy sendLetterApiProxy;

    @Test
    public void downloadDocuments() {

        UUID randomId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("random-string");

        when(sendLetterApiProxy.sendLetter(anyString(), anyString(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(randomId));

        UUID letterId = service.send(getBulkPrintRequest(), singletonList("abc".getBytes()));
        assertThat(letterId, is(equalTo(randomId)));
    }



    @Test
    public void throwsException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());
        thrown.expect(RuntimeException.class);
        service.send(getBulkPrintRequest(), singletonList("abc".getBytes()));
        verifyNoInteractions(sendLetterApiProxy);
    }

    @Test
    public void throwsExceptionOnSendLetter() {
        when(authTokenGenerator.generate())
            .thenReturn("random-string");

        when(sendLetterApiProxy.sendLetter(anyString(), anyString(), any(LetterWithPdfsRequest.class)))
            .thenThrow(new RuntimeException());

        thrown.expect(RuntimeException.class);
        service.send(getBulkPrintRequest(), singletonList("abc".getBytes()));
        verify(authTokenGenerator).generate();
    }

    private static BulkPrintRequest getBulkPrintRequest() {
        return BulkPrintRequest.builder().letterType("any").caseId("any")
            .bulkPrintDocuments(Collections.singletonList(
                BulkPrintDocument.builder().binaryFileUrl(BINARY_URL).fileName(FILE_NAME).build())).build();
    }
}
