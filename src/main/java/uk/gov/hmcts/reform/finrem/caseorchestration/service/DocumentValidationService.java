package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.DocumentValidationResponseBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


@Service
public class DocumentValidationService {
    private static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    private static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";
    private static final String CONSENT_ORDER = "consentOrder";
    private static final String PENSION_COLLECTION = "pensionCollection";

    @Autowired
    private DocumentClient documentClient;
    @Autowired
    private DocumentHelper documentHelper;

    public DocumentValidationResponse validateDocument(CallbackRequest callbackRequest,
                                                       String field, String authorizationToken) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        if (CONSENT_ORDER.equalsIgnoreCase(field)) {
            return validateConsentOrderDocument(authorizationToken, caseData);
        } else if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
            return validateLatestRespondToOrderDocument(authorizationToken, caseData);
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
            return validateLatestConsentOrderDocument(authorizationToken, caseData);
        } else if (PENSION_COLLECTION.equalsIgnoreCase(field)) {
            DocumentValidationResponseBuilder builder = DocumentValidationResponse.builder();
            List<CaseDocument> caseDocuments = documentHelper.getPensionDocumentsData(caseData);
            if (!caseDocuments.isEmpty()) {
                validateDocuments(authorizationToken, builder, caseDocuments);
            }
            return builder.build();
        }
        return DocumentValidationResponse.builder()
                .errors(singletonList("Unsupported Event or field")).build();

    }

    private DocumentValidationResponse validateLatestRespondToOrderDocument(String authorizationToken,
                                                                            Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.getLatestRespondToOrderDocuments(caseData);
        return documentClient
                .checkUploadedFileType(authorizationToken, caseDocument.getDocumentBinaryUrl());
    }

    private DocumentValidationResponse validateConsentOrderDocument(String authorizationToken,
                                                                    Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(caseData.get("consentOrder"));
        return documentClient
                .checkUploadedFileType(authorizationToken, caseDocument.getDocumentBinaryUrl());
    }

    private DocumentValidationResponse validateLatestConsentOrderDocument(String authorizationToken,
                                                                          Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.getLatestAmendedConsentOrder(caseData);
        return documentClient
                .checkUploadedFileType(authorizationToken, caseDocument.getDocumentBinaryUrl());
    }

    private void validateDocuments(String authorizationToken, DocumentValidationResponseBuilder builder,
                                   List<CaseDocument> caseDocuments) {
        List<CompletableFuture<DocumentValidationResponse>> collect = caseDocuments.stream()
                .map(caseDocument1 -> validate(authorizationToken, caseDocument1))
                .collect(toList());
        List<DocumentValidationResponse> validationResponses = collect.stream()
                .map(CompletableFuture::join)
                .collect(toList());

        validationResponses.stream()
                .filter(documentValidationResponse -> Objects.nonNull(documentValidationResponse.getErrors()))
                .findFirst()
                .ifPresent(documentValidationResponse -> builder.errors(documentValidationResponse.getErrors()));
    }

    private CompletableFuture<DocumentValidationResponse> validate(String authToken, CaseDocument caseDocument) {
        return CompletableFuture.supplyAsync(() ->
                documentClient.checkUploadedFileType(authToken, caseDocument.getDocumentBinaryUrl()));
    }
}
