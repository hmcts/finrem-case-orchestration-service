package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse.DocumentValidationResponseBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_PENSION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentValidationService {
    private static final String FR_SOLICITOR_CREATE = "FR_SolicitorCreate";
    private static final String FR_AMEND_APPLICATION_DETAILS = "FR_amendApplicationDetails";

    private final DocumentGeneratorValidationService documentGeneratorValidationService;
    private final DocumentHelper documentHelper;

    private static boolean hasErrors(DocumentValidationResponse documentValidationResponse) {
        return nonNull(documentValidationResponse.getErrors());
    }

    public DocumentValidationResponse validateDocument(CallbackRequest callbackRequest, String field, String authToken) {
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
        } else if (consentInContestedEvent(callbackRequest)) {
            if (consentOrder(field)) {
                return validateConsentOrderDocument(authToken, caseData);
            } else if (consentInContestedPensionDocuments(field)) {
                return validateConsentedInContestedPensionDocuments(authToken, caseData);
            }
        }
        log.info("Invalid request with caseField = {} , event = {}", field, callbackRequest.getEventId());
        return DocumentValidationResponse.builder()
            .build();
    }

    private boolean respondToOrderDocument(CallbackRequest callbackRequest) {
        return FR_RESPOND_TO_ORDER.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private DocumentValidationResponse validatePensionDocuments(String authorizationToken, Map<String, Object> caseData) {

        List<CaseDocument> caseDocuments = documentHelper.getPensionDocumentsData(caseData);
        if (!caseDocuments.isEmpty()) {
            return validateDocuments(authorizationToken, caseDocuments);
        }
        return DocumentValidationResponse.builder().build();
    }

    private DocumentValidationResponse validateConsentedInContestedPensionDocuments(String authorizationToken, Map<String, Object> caseData) {

        List<CaseDocument> caseDocuments = documentHelper.getConsentedInContestedPensionDocumentsData(caseData);
        if (!caseDocuments.isEmpty()) {
            return validateDocuments(authorizationToken, caseDocuments);
        }
        return DocumentValidationResponse.builder().build();
    }

    private boolean pensionDocuments(String field) {
        return PENSION_DOCS_COLLECTION.equalsIgnoreCase(field);
    }

    private boolean amendConsentOrder(CallbackRequest callbackRequest) {
        return FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private boolean consentInContestedEvent(CallbackRequest callbackRequest) {
        return FR_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId())
            || FR_RESPOND_TO_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private boolean createOrAmendApplication(CallbackRequest callbackRequest) {
        return FR_SOLICITOR_CREATE.equalsIgnoreCase(callbackRequest.getEventId())
            || FR_AMEND_APPLICATION_DETAILS.equalsIgnoreCase(callbackRequest.getEventId());
    }

    private boolean consentOrder(String field) {
        return CONSENT_ORDER.equalsIgnoreCase(field);
    }

    private boolean consentInContestedPensionDocuments(String field) {
        return CONTESTED_CONSENT_PENSION_COLLECTION.equalsIgnoreCase(field);
    }

    private DocumentValidationResponse validateRespondToOrderDocument(String authToken, Map<String, Object> caseData) {
        Optional<CaseDocument> caseDocument = documentHelper.getLatestRespondToOrderDocuments(caseData);
        return caseDocument
            .map(document -> documentGeneratorValidationService.validateFileType(document.getDocumentBinaryUrl()))
            .orElseGet(() -> DocumentValidationResponse.builder().build());
    }

    private DocumentValidationResponse validateConsentOrderDocument(String authToken, Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.convertToCaseDocument(caseData.get(CONSENT_ORDER));
        return documentGeneratorValidationService.validateFileType(caseDocument.getDocumentBinaryUrl());
    }

    private DocumentValidationResponse validateLatestConsentOrderDocument(String authToken, Map<String, Object> caseData) {
        CaseDocument caseDocument = documentHelper.getLatestAmendedConsentOrder(caseData);
        return documentGeneratorValidationService.validateFileType(caseDocument.getDocumentBinaryUrl());
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
            documentGeneratorValidationService.validateFileType(caseDocument.getDocumentBinaryUrl()));
    }
}
