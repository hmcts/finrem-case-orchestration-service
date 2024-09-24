package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentDeleteException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeep;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

@Service
@Slf4j
public class DocumentRemovalService {

    static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";
    private static final String DOCUMENT_UPLOAD_TIMESTAMP = "upload_timestamp";
    private static final String VALUE_KEY = "value";
    private final ObjectMapper objectMapper;

    private final GenericDocumentService genericDocumentService;

    private final FeatureToggleService featureToggleService;

    public DocumentRemovalService(ObjectMapper objectMapper,
                                  GenericDocumentService genericDocumentService,
                                  FeatureToggleService featureToggleService) {
        this.objectMapper = objectMapper;
        this.featureToggleService = featureToggleService;
        this.genericDocumentService = genericDocumentService;
    }

    public List<DocumentToKeepCollection> getCaseDocumentsList(FinremCaseData caseData) {
        JsonNode root = objectMapper.valueToTree(caseData);
        List<JsonNode> documentNodes = new ArrayList<>();

        log.info(format("Retrieving document JSON nodes for case id %s", caseData.getCcdCaseId()));
        retrieveDocumentNodes(root, documentNodes);
        log.info(format("Building case document list for case id %s", caseData.getCcdCaseId()));
        return buildCaseDocumentList(documentNodes);
    }

    public FinremCaseData removeDocuments(FinremCaseData caseData, Long caseId,  String userAuthorisation) {

        List<DocumentToKeepCollection> allExistingDocumentsList = getCaseDocumentsList(caseData);

        ArrayList<DocumentToKeepCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        Optional<List<DocumentToKeepCollection>> documentsUserWantsToKeepList = Optional.ofNullable(caseData.getDocumentToKeepCollection());
        documentsUserWantsToKeepList.ifPresent(documentsUserWantsDeletedList::removeAll);

        log.info(format("Beginning removal of %s document from Case ID %s", documentsUserWantsDeletedList.size(), caseId));

        //CDAM Needs to be enabled for this to work, only the user who created a document can delete it.
        // in our case, all documents are created by ExUI and therefor have no username, so auth fails on delete with 403.
        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            deleteDocument(
                documentToDeleteCollection.getValue(), userAuthorisation));

        JsonNode caseDataJson = objectMapper.valueToTree(caseData);

        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            removeDocumentFromJson(
                caseDataJson, documentToDeleteCollection.getValue()));

        log.info(format("Document removal complete, removing DocumentToKeep collection "
            + "from CaseData JSON for case ID: %s", caseId));

        ((ObjectNode) caseDataJson).remove("documentToKeepCollection");

        return buildAmendedCaseDataFromRootNode(caseDataJson, caseId);
    }

    /**
     * We expect the upload timestamp to be null or valid. However, if anything unexpected is provided,
     * catch and return null - this is only for sorting on the page.
     *
     * @param documentNode a JsonNode containing the upload timestamp
     * @return documentNodeUploadTimestamp a localDateTime version of the upload timestamp
     */
    private LocalDateTime getUploadTimestampFromDocumentNode(JsonNode documentNode) {
        LocalDateTime documentNodeUploadTimestamp;
        try {
            documentNodeUploadTimestamp =
                    Objects.isNull(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP)) ? null :
                            LocalDateTime.parse(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP).asText());
        } catch (Exception e) {
            log.error(format(
                    "Error getting upload timestamp for document url: %s.",
                    documentNode.get(DOCUMENT_URL).asText()));
            documentNodeUploadTimestamp = null;
        }
        return documentNodeUploadTimestamp;
    }

    private List<DocumentToKeepCollection> buildCaseDocumentList(List<JsonNode> documentNodes) {

        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId(docId)
                        .caseDocument(CaseDocument.builder()
                            .documentFilename(documentNode.get(DOCUMENT_FILENAME).asText())
                            .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                            .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                            .uploadTimestamp(getUploadTimestampFromDocumentNode(documentNode))
                            .build())
                        .build())
                    .build());
        }

        documentsCollection.sort(Comparator.comparing(
                DocumentToKeepCollection::getValue,
                    Comparator.comparing(DocumentToKeep::getCaseDocument,
                            Comparator.comparing(CaseDocument::getUploadTimestamp,
                                Comparator.nullsLast(
                                    Comparator.reverseOrder())))));

        return documentsCollection.stream().distinct().toList();
    }

    private void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)) {
                    documentNodes.add(fieldValue);
                } else {
                    retrieveDocumentNodes(fieldValue, documentNodes);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                retrieveDocumentNodes(arrayElement, documentNodes);
            }
        }
    }

    private void removeDocumentFromJson(JsonNode root, DocumentToKeep documentToDelete) {
        List<String> fieldsToRemove = new ArrayList<>();

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (shouldRemoveDocument(fieldValue,
                    documentToDelete.getCaseDocument().getDocumentUrl())) {
                    log.info(String.format("Deleting doc with url %s", documentToDelete.getCaseDocument().getDocumentUrl()));
                    fieldsToRemove.add(fieldName);
                } else {
                    removeDocumentFromJson(fieldValue, documentToDelete);
                }
            }
        } else if (root.isArray()) {
            processArrayNode(root, documentToDelete);
        }

        for (String fieldName : fieldsToRemove) {
            ((ObjectNode) root).remove(fieldName);
        }
    }

    private void processArrayNode(JsonNode root, DocumentToKeep documentToDelete) {
        ArrayNode arrayNode = (ArrayNode) root;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode arrayElement = arrayNode.get(i);
            if (arrayElement.has(VALUE_KEY)) {
                JsonNode valueObject = arrayElement.get(VALUE_KEY);
                Iterator<String> fieldNames = valueObject.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode fieldValue = valueObject.get(fieldName);

                    if (fieldValue.asText().equals(
                        documentToDelete.getCaseDocument().getDocumentUrl())
                        || shouldRemoveDocument(fieldValue,
                            documentToDelete.getCaseDocument().getDocumentUrl())) {
                        log.info(String.format("Deleting doc with url %s", documentToDelete.getCaseDocument().getDocumentUrl()));
                        ((ArrayNode) root).remove(i);
                    }
                }
            }
            removeDocumentFromJson(arrayElement, documentToDelete);
        }
    }

    private boolean shouldRemoveDocument(JsonNode fieldValue, String documentToKeepUrl) {
        return fieldValue.has(DOCUMENT_URL)
                && fieldValue.get(DOCUMENT_URL).asText().equals(documentToKeepUrl);
    }

    // Consider making async again.  See deleteOldMiniFormA
    private void deleteDocument(DocumentToKeep documentToRemove, String authorisationToken) {
        try {
            log.info(String.format("Deleting doc from DocStore with url %s",
                documentToRemove.getCaseDocument().getDocumentUrl()));

            if (featureToggleService.isSecureDocEnabled()) {
                genericDocumentService.deleteDocument(documentToRemove.getCaseDocument().getDocumentUrl(), authorisationToken);
            }
        } catch (Exception e) {
            log.error(format(
                    "Failed to delete document url %s",
                    documentToRemove.getCaseDocument().getDocumentUrl()), e);

            throw new DocumentDeleteException(e.getMessage(), e);
        }
    }

    private FinremCaseData buildAmendedCaseDataFromRootNode(JsonNode root, Long caseId) {
        FinremCaseData amendedCaseData;
        try {
            log.info(format("Building amendedCaseData for case id %s after deleting document", caseId));
            amendedCaseData = objectMapper.treeToValue(root, FinremCaseData.class);
        } catch (Exception e) {
            log.error(format("Error building amendedCaseData for case id %s after deleting document", caseId), e);
            throw new DocumentDeleteException(e.getMessage(), e);
        }
        return amendedCaseData;
    }
}
