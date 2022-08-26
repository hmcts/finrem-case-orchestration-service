package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ClientDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenericDocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";

    private final DocumentClient documentClient;

    public Document generateDocument(String authorisationToken, CaseDetails caseDetails,
                                         String template, String fileName) {
        Map<String, Object> caseDetailsMap = Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails);
        return generateDocumentFromPlaceholdersMap(authorisationToken, caseDetailsMap, template, fileName);

    }

    public Document generateDocumentFromPlaceholdersMap(String authorisationToken, Map placeholders,
                                                            String template, String fileName) {
        return toDocumentFromClientResponse(
            documentClient.generatePdf(
                DocumentGenerationRequest.builder()
                    .template(template)
                    .fileName(fileName)
                    .values(placeholders).build(),
                authorisationToken)
        );
    }

    public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
        return documentClient.bulkPrint(bulkPrintRequest);
    }

    public void deleteDocument(String documentUrl, String authorisationToken) {
        documentClient.deleteDocument(documentUrl, authorisationToken);
    }

    @Deprecated
    public CaseDocument annexStampDocument(CaseDocument document, String authorisationToken) {
        ClientDocument stampedDocument = documentClient.annexStampDocument(toDocumentClientDocument(document), authorisationToken);
        return toCaseDocumentFromClientResponse(stampedDocument);
    }

    public Document annexStampDocument(Document document, String authorisationToken) {
        return toDocumentFromClientResponse(
            documentClient.annexStampDocument(toDocumentClientDocument(document), authorisationToken));
    }

    @Deprecated
    public CaseDocument convertDocumentIfNotPdfAlready(CaseDocument document, String authorisationToken) {
        return !Files.getFileExtension(document.getDocumentFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken) : document;
    }

    public Document convertDocumentIfNotPdfAlready(Document document, String authorisationToken) {
        return !Files.getFileExtension(document.getFilename()).equalsIgnoreCase("pdf")
            ? convertDocumentToPdf(document, authorisationToken) : document;
    }

    @Deprecated
    public CaseDocument convertDocumentToPdf(CaseDocument document, String authorisationToken) {
        return toCaseDocumentFromClientResponse(
            documentClient.convertDocumentToPdf(authorisationToken, toDocumentClientDocument(document)));
    }

    public Document convertDocumentToPdf(Document document, String authorisationToken) {
        return toDocumentFromClientResponse(
            documentClient.convertDocumentToPdf(authorisationToken, toDocumentClientDocument(document)));
    }


    public CaseDocument stampDocument(CaseDocument document, String authorisationToken) {
        ClientDocument stampedDocument = documentClient.stampDocument(toDocumentClientDocument(document), authorisationToken);
        return toCaseDocumentFromClientResponse(stampedDocument);
    }

    public Document stampDocument(Document document, String authorisationToken) {
        return toDocumentFromClientResponse(
            documentClient.stampDocument(toDocumentClientDocument(document), authorisationToken));
    }

    public CaseDocument toCaseDocument(Document document) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(document.getBinaryUrl());
        caseDocument.setDocumentFilename(document.getFilename());
        caseDocument.setDocumentUrl(document.getUrl());
        return caseDocument;
    }

    public static Document toDocumentFromClientResponse(ClientDocument clientDocument) {
        return Document.builder()
            .url(clientDocument.getUrl())
            .filename(clientDocument.getFileName())
            .binaryUrl(clientDocument.getBinaryUrl())
            .build();
    }

    public static CaseDocument toCaseDocumentFromClientResponse(ClientDocument document) {
        return CaseDocument.builder()
            .documentUrl(document.getUrl())
            .documentBinaryUrl(document.getBinaryUrl())
            .documentFilename(document.getFileName())
            .build();
    }

    public static ClientDocument toDocumentClientDocument(Document document) {
        return ClientDocument.builder()
            .binaryUrl(document.getBinaryUrl())
            .url(document.getUrl())
            .fileName(document.getFilename())
            .build();
    }

    public static ClientDocument toDocumentClientDocument(CaseDocument document) {
        return ClientDocument.builder()
            .binaryUrl(document.getDocumentBinaryUrl())
            .fileName(document.getDocumentFilename())
            .url(document.getDocumentUrl())
            .build();
    }
}
