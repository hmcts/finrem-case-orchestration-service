package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;


@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDownloadService {
    private final IdamAuthService idamAuthService;
    private final CaseDocumentClient caseDocumentClient;

    public byte[] download(String binaryFileUrl, String auth) throws HttpClientErrorException {
        ResponseEntity<Resource> responseEntity =
            caseDocumentClient.getDocumentBinary(
                idamAuthService.getIdamToken(auth).getIdamOauth2Token(),
                idamAuthService.getIdamToken(auth).getServiceAuthorization(),
                binaryFileUrl);

        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }
}
