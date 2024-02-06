package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RemoveCaseDocumentNodeFromCaseData {

    ObjectMapper objectMapper = new ObjectMapper();

    public FinremCaseDetails removeCaseDocumentFromFinremCaseDetails(FinremCaseDetails finremCaseDetails, String documentUrlToRemove)
        throws JsonProcessingException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String finRemCaseDetailsAsString = objectMapper.writeValueAsString(finremCaseDetails);
        try {
            JsonNode rootNode = objectMapper.readTree(finRemCaseDetailsAsString);
            finremCaseDetails = removeCaseDocumentFromJson(rootNode, documentUrlToRemove, finremCaseDetails);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return finremCaseDetails;
    }

    public FinremCaseDetails removeCaseDocumentFromJson(JsonNode rootNode, String documentNameToFind, FinremCaseDetails originalFinremCaseDetails) throws JsonProcessingException {
        FinremCaseDetails finremCaseDetails = originalFinremCaseDetails;
        try {
            // Search for the target value and remove the parent object
            boolean removed = removeParentObject(rootNode, null, documentNameToFind, new ArrayList<>());

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

    private static boolean removeBobParentObject(JsonNode node, JsonNode parentNode, String targetValue, List<String> currentPath) {
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

                if (childNode.isTextual() && targetValue.equals(childNode.asText())) {
                    // Remove the parent object
                    if (parentNode instanceof ObjectNode) {
                        ((ObjectNode) parentNode).remove(nodeFieldName);
                        return true;
                    }
                } else if (childNode.isContainerNode()) {
                    // If the field value is a collection or another object, recursively search
                    if (removeParentObject(childNode, node, targetValue, newPath)) {
                        return true; // Parent object found and removed
                    }
                }
            }
        } else if (node.isArray()) {
            // If the current node is an array, iterate through its elements
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrayElement = node.get(i);
                if (arrayElement.isContainerNode() && removeParentObject(arrayElement, node, targetValue, currentPath)) {
                    // If the target value is found in the array element, remove the entire parent object
                    ((ArrayNode) node).remove(i);
                    return true;
                }
            }
        }

        return false; // Parent object not found
    }

    private static boolean removeParentObject(JsonNode node, JsonNode parentNode, String targetValue, List<String> currentPath) {
        boolean removed = false;
        List<JsonNode> nodesToRemove = new ArrayList<>();
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

                if (childNode.isTextual() && targetValue.equals(childNode.asText())) {
                    // Remove the parent object
                    if (parentNode instanceof ObjectNode) {
                        nodesToRemove.add(parentNode.get(nodeFieldName));
                        removed = true;
                    }
                } else if (childNode.isContainerNode()) {
                    // If the field value is a collection or another object, recursively search
                    if (removeParentObject(childNode, node, targetValue, newPath)) {
                        removed = true; // Parent object found and removed
                    }
                }
            }
        } else if (node.isArray()) {
            // If the current node is an array, iterate through its elements
            for (int i = node.size() - 1; i >= 0; i--) {
                JsonNode arrayElement = node.get(i);
                if (arrayElement.isContainerNode() && removeParentObject(arrayElement, node, targetValue, currentPath)) {
                    // If the target value is found in the array element, remove the entire parent object
                    nodesToRemove.add(node.get(i));
                    removed = true;
                }
            }
        }
        nodesToRemove.forEach(pNode -> {
            System.out.println("Removing node: " + pNode.toString());
            if (pNode instanceof ObjectNode) {
                ((ObjectNode) pNode).removeAll();
            } else if (pNode instanceof ArrayNode) {
                ((ArrayNode) pNode).removeAll();
            }
        });
        return removed; // Indicate whether at least one parent object was found and removed
    }


}
