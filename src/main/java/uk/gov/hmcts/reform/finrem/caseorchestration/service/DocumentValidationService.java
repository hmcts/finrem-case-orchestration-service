package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.DocumentValidationResponseBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;


@Service
@Slf4j
public class DocumentValidationService {
    private static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    private static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";
    private static final String CONSENT_ORDER = "consentOrder";
    private static final String PENSION_COLLECTION = "pensionCollection";
    private static final String FR_SOLICITOR_CREATE = "FR_SolicitorCreate";
    private static final String FR_AMEND_APPLICATION_DETAILS = "FR_amendApplicationDetails";

    @Autowired
    private DocumentClient documentClient;
    @Autowired
    private DocumentHelper documentHelper;

    private static boolean hasErrors(DocumentValidationResponse documentValidationResponse) {
        return nonNull(documentValidationResponse.getErrors());
    }

    public DocumentValidationResponse validateDocument(CallbackRequest callbackRequest,
                                                       String field, String authToken) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (createOrAmendApplication(callbackRequest)) {
            if (consentOrder(field)) {
                return validateConsentOrderDocument(authToken, caseData);
            } else if (pensionDocuments(field)) {
                return validatePensionDocuments(authToken, caseData);
            }
        } else if (amendConsentOrder(callbackRequest)) {
            return validateLatestConsentOrderDocument(authToken, caseData);
        } else if (respondToOrderDocument(callbackRequest)) {
            return validateRespondToOrderDocument(authToken, caseData);
        }
        log.info("Invalid request with caseField = {} , event = {}", field, callbackRequest.getEventId());
        return DocumentValidationResponse.builder()
                .build();

    }

    private boolean respondToOrderDocument(CallbackRequest callbackRequest) {
        return FR_RESPOND_TO_ORDER.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private DocumentValidationResponse validatePensionDocuments(String authorizationToken,
                                                                Map<String, Object> caseData) {
        if (documentHelper.isInvalidPensionDocuments(caseData)) {
            return DocumentValidationResponse.builder()
                    .errors(ImmutableList.of("Please upload a document"))
                    .build();
        }

        List<CaseDocument> caseDocuments = documentHelper.getPensionDocumentsData(caseData);
        if (!caseDocuments.isEmpty()) {
            return validateDocuments(authorizationToken, caseDocuments);
        }
        return DocumentValidationResponse.builder().build();
    }

    private boolean pensionDocuments(String field) {
        return PENSION_COLLECTION.equalsIgnoreCase(field);
    }

    private boolean amendConsentOrder(CallbackRequest callbackRequest) {
        return FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private boolean createOrAmendApplication(CallbackRequest callbackRequest) {
        return FR_SOLICITOR_CREATE.equalsIgnoreCase(callbackRequest.getEventId())
                || FR_AMEND_APPLICATION_DETAILS.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private boolean consentOrder(String field) {
        return CONSENT_ORDER.equalsIgnoreCase(field);
    }

    private DocumentValidationResponse validateRespondToOrderDocument(String authToken, Map<String, Object> caseData) {
        Optional<CaseDocument> caseDocument = documentHelper.getLatestRespondToOrderDocuments(caseData);
        DocumentValidationResponse response = caseDocument
                .map(document -> documentClient.checkUploadedFileType(authToken, document.getDocumentBinaryUrl()))
                .orElseGet(() -> DocumentValidationResponse.builder().build());
        return response;
    }

    private DocumentValidationResponse validateConsentOrderDocument(String authToken,
                                                                    Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(caseData.get("consentOrder"));
        return documentClient.checkUploadedFileType(authToken, caseDocument.getDocumentBinaryUrl());
    }

    private DocumentValidationResponse validateLatestConsentOrderDocument(String authToken,
                                                                          Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.getLatestAmendedConsentOrder(caseData);
        return documentClient.checkUploadedFileType(authToken, caseDocument.getDocumentBinaryUrl());
    }

    private DocumentValidationResponse validateDocuments(String authToken, List<CaseDocument> caseDocuments) {
        DocumentValidationResponseBuilder builder = DocumentValidationResponse.builder();

        List<DocumentValidationResponse> responses = caseDocuments.stream()
                .map(caseDocument1 -> validate(authToken, caseDocument1))
                .map(CompletableFuture::join)
                .collect(toList());

        responses.stream()
                .filter(DocumentValidationService::hasErrors)
                .findAny()
                .ifPresent(documentValidationResponse -> getErrors(builder, documentValidationResponse));
        return builder.build();
    }

    private DocumentValidationResponseBuilder getErrors(DocumentValidationResponseBuilder builder,
                                                        DocumentValidationResponse documentValidationResponse) {
        return builder.errors(documentValidationResponse.getErrors());
    }

    private CompletableFuture<DocumentValidationResponse> validate(String authToken, CaseDocument caseDocument) {
        return CompletableFuture.supplyAsync(() ->
                documentClient.checkUploadedFileType(authToken, caseDocument.getDocumentBinaryUrl()));
    }
}
