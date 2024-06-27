package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private boolean isDuplicateFilename(CaseDocument caseDocument, Supplier<List<CaseDocument>> caseDocumentSupplier) {
        return ofNullable(caseDocumentSupplier.get()).orElse(List.of(CaseDocument.builder().documentFilename("").build()))
            .stream().anyMatch(d -> d.getDocumentFilename().equals(caseDocument.getDocumentFilename()));
    }

    private boolean isDuplicatedFilenameInFinremCaseData(CaseDocument caseDocument, FinremCaseData caseData) {
        return Arrays.stream(new BeanWrapperImpl(caseData.getClass()).getPropertyDescriptors())
            .filter(d -> CaseDocument.class.isAssignableFrom(d.getPropertyType()))
            .anyMatch(pd ->
                isDuplicateFilename(caseDocument, () -> {
                    try {
                        return List.of((CaseDocument) pd.getReadMethod().invoke(caseData));
                    } catch (Exception e) {
                        log.error("Fail to invoke:" + pd.getReadMethod().getName());
                        return null;
                    }
                })
            ) || Arrays.stream(new BeanWrapperImpl(caseData.getClass()).getPropertyDescriptors())
                .filter(d -> CaseDocumentsDiscovery.class.isAssignableFrom(d.getPropertyType()))
                .anyMatch(pd ->
                    isDuplicateFilename(caseDocument, () -> {
                        try {
                            return ((CaseDocumentsDiscovery) pd.getReadMethod().invoke(caseData)).discover();
                        } catch (Exception e) {
                            log.error("Fail to invoke:" + pd.getReadMethod().getName());
                            return null;
                        }
                    })
                );
    }

    private static void processList(List<?> list, List<CaseDocument> allDocuments) {
        String methodName = "discover";
        if (list != null) {
            for (Object item : list) {
                try {
                    // Invoke the 'discover' method on each item in the list
                    Method discoverMethod = item.getClass().getMethod(methodName);
                    @SuppressWarnings("unchecked")
                    List<CaseDocument> documents = (List<CaseDocument>) discoverMethod.invoke(item);
                    allDocuments.addAll(documents);
                } catch (Exception e) {
                    log.error("Fail to invoke " + methodName + "()", e);
                }
            }
        }
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails beforeCaseDetails, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {

        FinremCaseData caseData = beforeCaseDetails.getData();
        if (isDuplicatedFilenameInFinremCaseData(caseDocument, caseData)) {
            return List.of(WARNING);
        }

        try {
            // Collect all fields from FinremCaseData class
            Field[] fields = FinremCaseData.class.getDeclaredFields();

            // List to collect all CaseDocument instances
            List<CaseDocument> allDocuments = new ArrayList<>();

            for (Field field : fields) {
                field.setAccessible(true);

                // Check if the field is a List with a parameterized type
                if (List.class.isAssignableFrom(field.getType())) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                    // Ensure the list has a single parameterized type argument
                    if (actualTypeArguments.length == 1 && CaseDocumentsDiscovery.class.isAssignableFrom((Class<?>) actualTypeArguments[0])) {

                        // Get the value of the field and process the list
                        List<?> list = (List<?>) field.get(caseData);
                        processList(list, allDocuments);
                    }
                }
            }

            log.info("Iterating all CaseDocuments with interface CaseDocumentsDiscovery.");

            // Check for duplicate filenames in the collected documents
            boolean hasDuplicates = allDocuments.stream()
                .anyMatch(d -> isDuplicateFilename(caseDocument, () -> List.of(d)));

            if (hasDuplicates) {
                return List.of(WARNING);
            }
        } catch (Exception e) {
            log.error("Failed to check for duplicate filenames and return warnings", e);
        }

        return Collections.emptyList();
    }
}
