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
        final String METHOD_NAME = "discover";
        if (list != null) {
            for (Object item : list) {
                try {
                    // Invoke the 'discover' method on each item in the list
                    Method discoverMethod = item.getClass().getMethod(METHOD_NAME);
                    @SuppressWarnings("unchecked")
                    List<CaseDocument> documents = (List<CaseDocument>) discoverMethod.invoke(item);
                    allDocuments.addAll(documents);
                } catch (Exception e) {
                    log.error("Fail to invoke " + METHOD_NAME + "()", e);
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
                    if (actualTypeArguments.length == 1 &&
                        CaseDocumentsDiscovery.class.isAssignableFrom((Class<?>) actualTypeArguments[0])) {

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



//        // otherDocumentsCollection
//        if (ofNullable(caseData.getOtherDocumentsCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(OtherDocument.builder().build())
//                .getUploadedDocument()))) {
//            return duplicatedWarning;
//        }
//        // additionalCicDocuments
//        if (ofNullable(caseData.getAdditionalCicDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
//            return duplicatedWarning;
//        }
//        // orderRefusalCollection, orderRefusalCollectionNew
//        if (Stream.of(ofNullable(caseData.getOrderRefusalCollection()).orElse(List.of()),
//                ofNullable(caseData.getOrderRefusalCollectionNew()).orElse(List.of()))
//            .flatMap(List::stream)
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(OrderRefusalHolder.builder().build())
//                .getOrderRefusalDocs()))) {
//            return duplicatedWarning;
//        }
//        // uploadConsentOrderDocuments
//        if (ofNullable(caseData.getUploadConsentOrderDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadConsentOrderDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // uploadOrder
//        if (ofNullable(caseData.getUploadOrder()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadOrder.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // uploadDocuments
//        if (ofNullable(caseData.getUploadDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // solUploadDocuments
//        if (ofNullable(caseData.getSolUploadDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(SolUploadDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // respondToOrderDocuments
//        if (ofNullable(caseData.getRespondToOrderDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(RespondToOrderDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // amendedConsentOrderCollection
//        if (ofNullable(caseData.getAmendedConsentOrderCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(AmendedConsentOrder.builder().build())
//                .getAmendedConsentOrder()))) {
//            return duplicatedWarning;
//        }
//        // scannedDocuments
//        if (ofNullable(caseData.getScannedDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(ScannedDocument.builder().build())
//                .getUrl()))) {
//            return duplicatedWarning;
//        }
//        // approvedOrderCollection
//        if (ofNullable(caseData.getApprovedOrderCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d ->
//                isDuplicateFilename(caseDocument, () -> ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build())
//                    .getConsentOrder())
//                || isDuplicateFilename(caseDocument, () -> ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build())
//                    .getOrderLetter())
//                || ofNullable(ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build()).getPensionDocuments()).orElse(List.of())
//                    .stream()
//                    .anyMatch(e -> isDuplicateFilename(caseDocument, () -> ofNullable(e.getTypedCaseDocument()).orElse(PensionType.builder().build())
//                        .getPensionDocument()))
//            )
//        ) {
//            return duplicatedWarning;
//        }
//        // scannedD81s
//        if (ofNullable(caseData.getScannedD81s()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
//            return duplicatedWarning;
//        }
//        // additionalHearingDocuments
//        if (ofNullable(caseData.getAdditionalHearingDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(AdditionalHearingDocument.builder().build())
//                .getDocument()))) {
//            return duplicatedWarning;
//        }
//        // hearingNoticeDocumentPack
//        if (ofNullable(caseData.getHearingNoticeDocumentPack()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
//            return duplicatedWarning;
//        }
//        // hearingNoticesDocumentCollection
//        if (ofNullable(caseData.getHearingNoticesDocumentCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
//            return duplicatedWarning;
//        }
//        // uploadGeneralDocuments
//        if (ofNullable(caseData.getUploadGeneralDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadGeneralDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // uploadHearingOrder
//        if (ofNullable(caseData.getUploadHearingOrder()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(DirectionOrder.builder().build())
//                .getUploadDraftDocument()))) {
//            return duplicatedWarning;
//        }
//        // hearingOrderOtherDocuments
//        if (ofNullable(caseData.getHearingOrderOtherDocuments()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
//            return duplicatedWarning;
//        }
//        // finalOrderCollection
//        if (ofNullable(caseData.getFinalOrderCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(DirectionOrder.builder().build())
//                .getUploadDraftDocument()))) {
//            return duplicatedWarning;
//        }
//        // intv1HearingNoticesCollection, intv2HearingNoticesCollection, intv3HearingNoticesCollection, intv4HearingNoticesCollection
//        if (Stream.of(ofNullable(caseData.getIntv1HearingNoticesCollection()).orElse(List.of()),
//                ofNullable(caseData.getIntv2HearingNoticesCollection()).orElse(List.of()),
//                ofNullable(caseData.getIntv3HearingNoticesCollection()).orElse(List.of()),
//                ofNullable(caseData.getIntv4HearingNoticesCollection()).orElse(List.of()))
//            .flatMap(List::stream)
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(IntervenerHearingNotice.builder().build())
//                .getCaseDocument()))) {
//            return duplicatedWarning;
//        }
//        // refusalOrderCollection
//        if (ofNullable(caseData.getRefusalOrderCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(RefusalOrder.builder().build())
//                .getRefusalOrderAdditionalDocument()))) {
//            return duplicatedWarning;
//        }
//        // uploadAdditionalDocument
//        if (ofNullable(caseData.getUploadAdditionalDocument()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadAdditionalDocument.builder().build())
//                .getAdditionalDocuments()))) {
//            return duplicatedWarning;
//        }
//        // confidentialDocumentsUploaded
//        if (ofNullable(caseData.getConfidentialDocumentsUploaded()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadConfidentialDocument.builder().build())
//                .getDocumentLink()))) {
//            return duplicatedWarning;
//        }
//        // manageCaseDocumentCollection
//        if (ofNullable(caseData.getManageCaseDocumentCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getUploadCaseDocument()).orElse(UploadCaseDocument.builder().build())
//                .getCaseDocuments()))) {
//            return duplicatedWarning;
//        }
//        // applicantScanDocuments, respondentScanDocuments
//        if (Stream.of(ofNullable(caseData.getApplicantScanDocuments()).orElse(List.of()),
//                ofNullable(caseData.getRespondentScanDocuments()).orElse(List.of()))
//            .flatMap(List::stream)
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(ScannedDocument.builder().build())
//                .getUrl()))) {
//            return duplicatedWarning;
//        }
//        // manageScannedDocumentCollection
//        if (ofNullable(caseData.getManageScannedDocumentCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getManageScannedDocument()).orElse(ManageScannedDocument.builder()
//                    .uploadCaseDocument(UploadCaseDocument.builder().build())
//                    .build()).getUploadCaseDocument().getCaseDocuments()))) {
//            return duplicatedWarning;
//        }
//        // ordersSentToPartiesCollection
//        if (ofNullable(caseData.getOrdersSentToPartiesCollection()).orElse(List.of())
//            .stream()
//            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(SendOrderDocuments.builder().build())
//                .getCaseDocument()))) {
//            return duplicatedWarning;
//        }

        return Collections.emptyList();
    }
}
