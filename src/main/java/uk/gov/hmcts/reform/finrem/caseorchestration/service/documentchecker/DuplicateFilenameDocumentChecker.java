package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentFileNameProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class DuplicateFilenameDocumentChecker implements DocumentChecker {

    private static final String WARNING = "A document with this filename already exists on the case";

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return true;
    }

    private boolean isDuplicateFilename(CaseDocument caseDocument, Supplier<List<DocumentFileNameProvider>> caseDocumentSupplier) {
        return ofNullable(caseDocumentSupplier.get()).orElse(List.of(CaseDocument.builder().documentFilename("").build()))
            .stream().anyMatch(d -> d.getDocumentFilename().equals(caseDocument.getDocumentFilename()));
    }

    private static void processList(List<?> list, List<DocumentFileNameProvider> allDocuments) {
        if (list != null) {
            for (Object item : list) {
                if (item instanceof HasCaseDocument hasCaseDocument) {
                    processHasCaseDocument(hasCaseDocument, allDocuments);
                } else {
                    log.warn("Ignored " + item.getClass().getName());
                }
            }
        }
    }

    private static void processHasCaseDocument(HasCaseDocument hcd, List<DocumentFileNameProvider> allDocuments) {
        if (hcd != null) {
            try {
                // Collect all fields from HasCaseDocument class
                Field[] fields = hcd.getClass().getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);

                    // Check if the field is a List with a parameterized type
                    if (List.class.isAssignableFrom(field.getType())) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                        // Ensure the list has a single parameterized type argument
                        if (actualTypeArguments.length == 1 && HasCaseDocument.class.isAssignableFrom((Class<?>) actualTypeArguments[0])) {
                            // Get the value of the field and process the list
                            processList((List<?>) field.get(hcd), allDocuments);
                        }
                    } else if (DocumentFileNameProvider.class.isAssignableFrom(field.getType())) {
                        allDocuments.add((DocumentFileNameProvider) field.get(hcd));
                    } else if (HasCaseDocument.class.isAssignableFrom(field.getType())) {
                        processHasCaseDocument((HasCaseDocument)  field.get(hcd), allDocuments);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to check for duplicate filenames and return warnings", e);
            }
        }
    }

    private List<DocumentFileNameProvider> collectCaseDocumentsFromFinremCaseData(FinremCaseData caseData) {
        List<DocumentFileNameProvider> allDocuments = new ArrayList<>();
        processHasCaseDocument(caseData, allDocuments);
        return allDocuments.stream().filter(Objects::nonNull).toList();
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails beforeCaseDetails, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {

        FinremCaseData caseData = beforeCaseDetails.getData();
        List<DocumentFileNameProvider> allDocuments = collectCaseDocumentsFromFinremCaseData(caseData);

        log.info("Iterating all CaseDocuments with interface HasCaseDocument.");

        // Check for duplicate filenames in the collected documents
        boolean hasDuplicates = allDocuments.stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> List.of(d)));

        if (hasDuplicates) {
            return List.of(WARNING);
        }

        return Collections.emptyList();
    }
}
