package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_FDR_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.INTERVENER_TWO;

@Component
@Slf4j
@Order(2)
public class FdrDocumentsHandler extends CaseDocumentHandler<ContestedUploadedDocumentData> {
    @Autowired
    public FdrDocumentsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> {
                ContestedUploadedDocument uploadedCaseDocument = d.getUploadedCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && uploadedCaseDocument.getCaseDocumentFdr().equalsIgnoreCase("Yes");
            })
            .toList();

        List<ContestedUploadedDocumentData> fdrDocsCollection = getDocumentCollection(caseData, FDR_DOCS_COLLECTION);
        addAndSortCollection(fdrFiltered, fdrDocsCollection);

        copyFdrCollIntoIntervenerDocIfActiveCaseRoleIsIntervener(fdrFiltered, caseData);

        uploadedDocuments.removeAll(fdrFiltered);

        if (!fdrDocsCollection.isEmpty()) {
            caseData.put(FDR_DOCS_COLLECTION, fdrDocsCollection);
        }

    }

    private void copyFdrCollIntoIntervenerDocIfActiveCaseRoleIsIntervener(List<ContestedUploadedDocumentData> fdrFiltered,
                                                                          Map<String, Object> caseData) {
        String logMessage = "Logged in user role {}";
        fdrFiltered.stream().map(obj -> obj.getUploadedCaseDocument().getCaseDocumentParty()).filter(Objects::nonNull).forEach(role -> {
            switch (role) {
                case INTERVENER_ONE -> setIntervenerFdrDocuments(fdrFiltered, caseData,
                    INTERVENER_ONE, INTV_ONE_FDR_DOCS_COLLECTION, logMessage);
                case INTERVENER_TWO -> setIntervenerFdrDocuments(fdrFiltered, caseData,
                    INTERVENER_TWO, INTV_TWO_FDR_DOCS_COLLECTION, logMessage);
                case INTERVENER_THREE -> setIntervenerFdrDocuments(fdrFiltered, caseData,
                    INTERVENER_THREE, INTV_THREE_FDR_DOCS_COLLECTION, logMessage);
                case INTERVENER_FOUR -> setIntervenerFdrDocuments(fdrFiltered, caseData,
                    INTERVENER_FOUR, INTV_FOUR_FDR_DOCS_COLLECTION, logMessage);
                default -> log.info(logMessage, role);
            }
        });
    }

    private void setIntervenerFdrDocuments(List<ContestedUploadedDocumentData> fdrFiltered,
                                           Map<String, Object> caseData,
                                           String role,
                                           String collectionName,
                                           String logMessage) {
        log.info(logMessage, role);
        List<ContestedUploadedDocumentData> fdrDocsCollection = getDocumentCollection(caseData, collectionName);
        addAndSortCollection(fdrFiltered, fdrDocsCollection);
        if (!fdrDocsCollection.isEmpty()) {
            caseData.put(collectionName, fdrDocsCollection);
        }
    }

    private void addAndSortCollection(List<ContestedUploadedDocumentData> fdrFiltered,
                                      List<ContestedUploadedDocumentData> fdrDocsCollection) {
        fdrDocsCollection.addAll(fdrFiltered);
        fdrDocsCollection.sort(Comparator.comparing(
            ContestedUploadedDocumentData::getUploadedCaseDocument, Comparator.comparing(
                ContestedUploadedDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to FDR Documents Collection", fdrFiltered);
    }
}