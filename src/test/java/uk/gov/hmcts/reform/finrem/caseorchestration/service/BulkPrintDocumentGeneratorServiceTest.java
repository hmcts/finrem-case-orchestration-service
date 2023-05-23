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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.sendletter.SendLetterApiResponse;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintDocumentGeneratorServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks private BulkPrintDocumentGeneratorService service;

    @Mock private AuthTokenGenerator authTokenGenerator;
    @Mock private SendLetterApi sendLetterApi;

    @Mock private FeatureToggleService featureToggleService;

    @Test
    public void downloadDocuments() {

        UUID randomId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("random-string");
        when(featureToggleService.isSendLetterDuplicateCheckEnabled()).thenReturn(false);

        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(randomId));

        SendLetterApiResponse apiResponse
            = service.send(getBulkPrintRequest(), APPLICANT, singletonList("abc".getBytes()));
        assertThat(apiResponse.getLetterId(), is(equalTo(randomId)));
    }



    @Test
    public void throwsException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());
        thrown.expect(RuntimeException.class);
        service.send(getBulkPrintRequest(), APPLICANT, singletonList("abc".getBytes()));
        verifyNoInteractions(sendLetterApi);
    }

    @Test
    public void throwsExceptionOnSendLetter() {
        when(authTokenGenerator.generate())
            .thenReturn("random-string");

        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .thenThrow(new RuntimeException());

        thrown.expect(RuntimeException.class);
        service.send(getBulkPrintRequest(), APPLICANT, singletonList("abc".getBytes()));
        verify(authTokenGenerator).generate();
    }

    private static BulkPrintRequest getBulkPrintRequest() {
        return BulkPrintRequest.builder().letterType("any").caseId("any")
            .bulkPrintDocuments(Collections.singletonList(
                BulkPrintDocument.builder().binaryFileUrl(BINARY_URL).fileName(FILE_NAME).build())).build();
    }
}
