package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.proxy.SendLetterApiProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintDocumentGeneratorService {

    private static final String XEROX_TYPE_PARAMETER = "FINREM001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private static final String FILE_NAMES = "fileNames";

    private final AuthTokenGenerator authTokenGenerator;
    private final SendLetterApiProxy sendLetterApiProxy;

    /**
     * Note: the order of documents you send to this service is the order in which they will print.
     */


    public UUID send(final BulkPrintRequest bulkPrintRequest, final List<byte[]> listOfDocumentsAsByteArray) {

        String letterType = bulkPrintRequest.getLetterType();
        String caseId = bulkPrintRequest.getCaseId();

        log.info("Sending {} for case {}", letterType, caseId);

        final List<String> documents = listOfDocumentsAsByteArray.stream()
            .map(getEncoder()::encodeToString)
            .collect(toList());

        SendLetterResponse sendLetterResponse = sendLetterApiProxy.sendLetter(authTokenGenerator.generate(), "true",
            new LetterWithPdfsRequest(documents, XEROX_TYPE_PARAMETER, getAdditionalData(caseId, letterType, bulkPrintRequest)));

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
        return sendLetterResponse.letterId;
    }


    private Map<String, Object> getAdditionalData(final String caseId, final String letterType, final BulkPrintRequest bulkPrintRequest) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        additionalData.put(FILE_NAMES, getFileNames(bulkPrintRequest));
        return additionalData;
    }

    private List<String> getFileNames(BulkPrintRequest bulkPrintRequest) {
        return bulkPrintRequest.getBulkPrintDocuments().stream().map(BulkPrintDocument::getFileName).collect(toList());
    }
}
