package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CaseDocumentCleaner {

    protected static final String DOCUMENT_URL = "document_url";
    protected static final String DOCUMENT_BINARY_URL = "document_binary_url";
    protected static final String DOCUMENT_FILENAME = "document_filename";
    ObjectMapper objectMapper = new ObjectMapper();

    public FinremCaseDetails removeOrReplaceCaseDocumentFromFinremCaseDetails(FinremCaseDetails finremCaseDetails, String documentUrlToRemove,
                                                                              CaseDocument replacementCaseDocument)
        throws JsonProcessingException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String finRemCaseDetailsAsString = objectMapper.writeValueAsString(finremCaseDetails);
        try {
            JsonNode rootNode = objectMapper.readTree(finRemCaseDetailsAsString);
            finremCaseDetails = removeOrReplaceCaseDocumentFromJson(rootNode, documentUrlToRemove, replacementCaseDocument, finremCaseDetails);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return finremCaseDetails;
    }

    public FinremCaseDetails removeOrReplaceCaseDocumentFromJson(JsonNode rootNode, String documentUrlToFind, CaseDocument replacementCaseDocument,
                                                                 FinremCaseDetails originalFinremCaseDetails) throws JsonProcessingException {
        FinremCaseDetails finremCaseDetails = originalFinremCaseDetails;
        try {
            // Search for the target value and remove the parent object
            boolean removed = removeOrReplaceParentObject(rootNode, null, documentUrlToFind, replacementCaseDocument, new ArrayList<>());

            if (removed) {
                // Convert the modified tree back to a JSON string
                String updatedJsonString = objectMapper.writeValueAsString(rootNode);
                finremCaseDetails = objectMapper.treeToValue(rootNode, FinremCaseDetails.class);
                System.out.println("Updated JSON: " + updatedJsonString);
            } else {
                System.out.println("Document URL not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finremCaseDetails;
    }


    private static boolean removeOrReplaceParentObject(JsonNode node, JsonNode parentNode, String targetValue, CaseDocument replacementCaseDoc,
                                                       List<String> currentPath) {
        boolean removedOrReplaced = false;
        List<JsonNode> nodesToRemove = new ArrayList<>();
        List<JsonNode> parentNodesToRemove = new ArrayList<>();
        if (node.isObject()) {
            String nodeFieldName = "";
            // If the current node is an object, iterate through its fields
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);
                if (!currentPath.isEmpty()) {
                    nodeFieldName = currentPath.get(currentPath.size() - 1);
                }
                List<String> newPath = new ArrayList<>(currentPath);
                newPath.add(fieldName);

                if (fieldName.equals(DOCUMENT_URL) && childNode.isTextual() && targetValue.equals(childNode.asText())) {
                    if (replacementCaseDoc != null) {
                        ObjectNode caseDocumentNode = (ObjectNode) node;
                        caseDocumentNode.put(DOCUMENT_URL, replacementCaseDoc.getDocumentUrl());
                        caseDocumentNode.put(DOCUMENT_BINARY_URL, replacementCaseDoc.getDocumentBinaryUrl());
                        caseDocumentNode.put(DOCUMENT_FILENAME, replacementCaseDoc.getDocumentFilename());
                    }
                    // Remove the parent object
                    else if (parentNode instanceof ObjectNode) {
                            ((ObjectNode) parentNode).remove(nodeFieldName);
                    }
                    removedOrReplaced = true;
                } else if (childNode.isContainerNode()) {
                    // If the field value is a collection or another object, recursively search
                    if (removeOrReplaceParentObject(childNode, node, targetValue, replacementCaseDoc, newPath)) {
                        removedOrReplaced = true; // Parent object found and removed
                    }
                }
            }
        } else if (node.isArray()) {
            // If the current node is an array, iterate through its elements
            for (int i = node.size() - 1; i >= 0; i--) {
                JsonNode arrayElement = node.get(i);
                if (arrayElement.isContainerNode() && removeOrReplaceParentObject(arrayElement, node, targetValue, replacementCaseDoc, currentPath)) {
                    // If the target value is found in the array element, remove the entire parent object
                    nodesToRemove.add(node.get(i));
                    removedOrReplaced = true;
                }
            }
        }
        if (replacementCaseDoc == null) {
            nodesToRemove.forEach(pNode -> {
                System.out.println("Removing node: " + pNode.toString());
                if (pNode instanceof ObjectNode) {
                    ((ObjectNode) pNode).removeAll();
                } else if (pNode instanceof ArrayNode) {
                    ((ArrayNode) pNode).removeAll();
                }
            });
        }
        return removedOrReplaced; // Indicate whether at least one parent object was found and removed
    }


}
