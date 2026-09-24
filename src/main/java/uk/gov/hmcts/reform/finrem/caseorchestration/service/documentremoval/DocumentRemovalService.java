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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;

@Service
@Slf4j
public class DocumentRemovalService {

    static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";
    private static final String DOCUMENT_UPLOAD_TIMESTAMP = "upload_timestamp";
    private static final String GENERAL_APPLICATION_DRAFT_ORDER = "generalApplicationDraftOrder";
    private static final String GENERAL_APPLICATION_DIRECTIONS_DOCUMENT = "generalApplicationDirectionsDocument";
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

    public FinremCaseData removeDocuments(FinremCaseData caseData, Long caseId, String userAuthorisation) {

        List<DocumentToKeepCollection> allExistingDocumentsList = getCaseDocumentsList(caseData);

        ArrayList<DocumentToKeepCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        Optional<List<DocumentToKeepCollection>> documentsUserWantsToKeepList = Optional.ofNullable(caseData.getDocumentToKeepCollection());
        documentsUserWantsToKeepList.ifPresent(documentsUserWantsDeletedList::removeAll);

        log.info(format("Beginning removal of %s document from Case ID %s", documentsUserWantsDeletedList.size(), caseId));

        //CDAM Needs to be enabled for this to work, only the user who created a document can delete it.
        // in our case, all documents are created by ExUI and therefore have no username, so auth fails on delete with 403.
        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            deleteDocument(
                documentToDeleteCollection.getValue(), userAuthorisation, caseId));

        JsonNode caseDataJson = objectMapper.valueToTree(caseData);

        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            removeDocumentFromJson(
                caseDataJson, documentToDeleteCollection.getValue().getCaseDocument().getDocumentUrl()));

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

            LocalDateTime uploadTimestamp = getUploadTimestampFromDocumentNode(documentNode);

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId(docId)
                        .caseDocument(CaseDocument.builder()
                            .documentFilename(documentNode.get(DOCUMENT_FILENAME).asText())
                            .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                            .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                            .uploadTimestamp(uploadTimestamp)
                            .build())
                        .caseDocumentUploadedDate(uploadTimestamp)
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

    private void removeDocumentFromJson(JsonNode node, String documentUrl) {
        if (node == null) {
            return;
        }

        if (node.isArray()) {
            removeDocumentFromArray((ArrayNode) node, documentUrl);

        } else if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldsToRemove = new ArrayList<>();

            for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();


                if (isDocumentEntry(fieldValue, documentUrl)) {
                    fieldsToRemove.add(fieldName);
                } else {
                    removeDocumentFromJson(fieldValue, documentUrl);
                }
            }
            fieldsToRemove.forEach(objectNode::remove);
        }
    }

    private void removeDocumentFromArray(ArrayNode arrayNode, String documentUrl) {
        for (int i = arrayNode.size() - 1; i >= 0; i--) {
            JsonNode element = arrayNode.get(i);

            if (isDocumentArrayElement(element, documentUrl)) {
                log.info("Removing document entry with URL: {}", documentUrl);
                arrayNode.remove(i);
            } else {
                removeDocumentFromJson(element, documentUrl);
            }
        }
    }

    private boolean isDocumentArrayElement(JsonNode element, String documentUrl) {
        if (isDocumentEntry(element, documentUrl)) {
            return true;
        }

        if (element == null || !element.isObject()) {
            return false;
        }

        JsonNode valueNode = element.get("value");

        if (valueNode == null || !valueNode.isObject()) {
            return false;
        }

        for (Map.Entry<String, JsonNode> field : valueNode.properties()) {

            if (isGeneralApplicationDocumentField(field.getKey())) {
                continue;
            }

            JsonNode fieldValue = field.getValue();

            if (fieldValue.isTextual() && documentUrl.equals(fieldValue.asText())) {
                return true;
            }

            if (isDocumentEntry(fieldValue, documentUrl)) {
                return true;
            }
        }

        return false;
    }

    private boolean isGeneralApplicationDocumentField(String fieldName) {
        return GENERAL_APPLICATION_DOCUMENT.equals(fieldName)
            || GENERAL_APPLICATION_DRAFT_ORDER.equals(fieldName)
            || GENERAL_APPLICATION_DIRECTIONS_DOCUMENT.equals(fieldName);
    }

    private void deleteDocument(DocumentToKeep documentToRemove, String authorisationToken, Long caseId) {
        log.info(String.format("Deleting doc from DocStore with url %s",
            documentToRemove.getCaseDocument().getDocumentUrl()));

        if (featureToggleService.isSecureDocEnabled()) {
            CompletableFuture.runAsync(() -> {
                try {
                    genericDocumentService.deleteDocument(documentToRemove.getCaseDocument().getDocumentUrl(), authorisationToken);
                } catch (Exception e) {
                    log.error(format(
                        "Document Removal Service failed to delete document url %s for case ID %s",
                        documentToRemove.getCaseDocument().getDocumentUrl(), caseId), e);
                }
            });
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

    private boolean isDocumentEntry(JsonNode node, String documentUrl) {
        if (node == null || !node.isObject()) {
            return false;
        }

        JsonNode directDocumentUrl = node.get(DOCUMENT_URL);

        if (directDocumentUrl != null && directDocumentUrl.isTextual() && documentUrl.equals(directDocumentUrl.asText())) {
            return true;
        }

        JsonNode valueNode = node.get("value");

        if (valueNode == null || !valueNode.isObject()) {
            return false;
        }

        JsonNode valueDocumentUrl = valueNode.get(DOCUMENT_URL);

        if (valueDocumentUrl != null && valueDocumentUrl.isTextual() && documentUrl.equals(valueDocumentUrl.asText())) {
            return true;
        }

        JsonNode documentLinkNode = valueNode.get("documentLink");

        if (documentLinkNode == null) {
            documentLinkNode = valueNode.get("document_link");
        }

        if (documentLinkNode != null && documentLinkNode.isObject()) {
            JsonNode documentLinkUrl = documentLinkNode.get(DOCUMENT_URL);

            return documentLinkUrl != null && documentLinkUrl.isTextual() && documentUrl.equals(documentLinkUrl.asText());
        }

        return false;
    }
}
