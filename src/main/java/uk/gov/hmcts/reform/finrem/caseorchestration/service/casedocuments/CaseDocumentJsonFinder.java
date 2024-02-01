package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class CaseDocumentJsonFinder {

    public void findCaseDocumentJson(JsonNode rootNode, String documentNameToFind) {
        // Parse JSON string
        try {
            findTargetValue(rootNode, documentNameToFind, "caseData", new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findTargetValue(JsonNode node, String targetValue, String parentField, List<String> currentPath) {
        if (node.isObject()) {
            // If the current node is an object, iterate through its fields
            node.fields().forEachRemaining(entry -> {
                JsonNode childNode = entry.getValue();
                List<String> newPath = new ArrayList<>(currentPath);
                newPath.add(entry.getKey());

                if (childNode.isTextual() && targetValue.equals(childNode.asText())) {
                    System.out.println("Found target value at path: " + String.join(".", newPath));
                    removeParentObject(parentField, node, targetValue, currentPath);
                } else if (childNode.isContainerNode()) {
                    // If the field value is a collection or another object, recursively search
                    findTargetValue(childNode, targetValue, entry.getKey(), newPath);
                }
            });
        } else if (node.isArray()) {
            // If the current node is an array, iterate through its elements
            for (int i = 0; i < node.size(); i++) {
                List<String> newPath = new ArrayList<>(currentPath);
                newPath.add("[" + i + "]");
                findTargetValue(node.get(i), targetValue, currentPath.get(currentPath.size() - 1), newPath);
            }
        }
    }

    private static boolean removeParentObject(String fieldName, JsonNode parentNode, String targetValue, List<String> currentPath) {
        if (parentNode instanceof ObjectNode) {
            ((ObjectNode) parentNode).remove(fieldName);
            return true;
        }

        return false; // Parent object not found
    }


}
