package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RefusalOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class DuplicateFilenameDocumentChecker implements DocumentChecker {

    private static final String WARNING = "A document with this filename already exists on the case";

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return true;
    }

    private boolean isDuplicateFilename(CaseDocument caseDocument, Supplier<CaseDocument> caseDocumentSupplier) {
        return ofNullable(caseDocumentSupplier.get()).orElse(CaseDocument.builder().documentFilename("").build())
            .getDocumentFilename().equals(caseDocument.getDocumentFilename());
    }

    private boolean isDuplicatedFilenameInFinremCaseData(CaseDocument caseDocument, FinremCaseData caseData) {
        return Arrays.stream(new BeanWrapperImpl(caseData.getClass()).getPropertyDescriptors())
            .filter(d -> CaseDocument.class.equals(d.getPropertyType()))
            .anyMatch(pd ->
                isDuplicateFilename(caseDocument, () -> {
                    try {
                        return (CaseDocument) pd.getReadMethod().invoke(caseData);
                    } catch (Exception e) {
                        log.error("Fail to invoke:" + pd.getReadMethod().getName());
                        return null;
                    }
                })
            );
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails beforeCaseDetails, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {
        final List<String> duplicatedWarning = List.of(WARNING);

        FinremCaseData caseData = beforeCaseDetails.getData();
        if (isDuplicatedFilenameInFinremCaseData(caseDocument, caseData)) {
            return duplicatedWarning;
        }

        // pensionCollection, consentPensionCollection
        if (Stream.of(ofNullable(caseData.getPensionCollection()).orElse(List.of()),
            ofNullable(caseData.getConsentPensionCollection()).orElse(List.of()))
            .flatMap(List::stream)
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getTypedCaseDocument()).orElse(PensionType.builder().build())
                .getPensionDocument()))) {
            return duplicatedWarning;
        }
        // copyOfPaperFormA
        if (ofNullable(caseData.getCopyOfPaperFormA()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(PaymentDocument.builder().build())
                .getUploadedDocument()))) {
            return duplicatedWarning;
        }
        // otherDocumentsCollection
        if (ofNullable(caseData.getOtherDocumentsCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(OtherDocument.builder().build())
                .getUploadedDocument()))) {
            return duplicatedWarning;
        }
        // additionalCicDocuments
        if (ofNullable(caseData.getAdditionalCicDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
            return duplicatedWarning;
        }
        // orderRefusalCollection, orderRefusalCollectionNew
        if (Stream.of(ofNullable(caseData.getOrderRefusalCollection()).orElse(List.of()),
                ofNullable(caseData.getOrderRefusalCollectionNew()).orElse(List.of()))
            .flatMap(List::stream)
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(OrderRefusalHolder.builder().build())
                .getOrderRefusalDocs()))) {
            return duplicatedWarning;
        }
        // uploadConsentOrderDocuments
        if (ofNullable(caseData.getUploadConsentOrderDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadConsentOrderDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // uploadOrder
        if (ofNullable(caseData.getUploadOrder()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadOrder.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // uploadDocuments
        if (ofNullable(caseData.getUploadDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // solUploadDocuments
        if (ofNullable(caseData.getSolUploadDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(SolUploadDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // respondToOrderDocuments
        if (ofNullable(caseData.getRespondToOrderDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(RespondToOrderDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // amendedConsentOrderCollection
        if (ofNullable(caseData.getAmendedConsentOrderCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(AmendedConsentOrder.builder().build())
                .getAmendedConsentOrder()))) {
            return duplicatedWarning;
        }
        // scannedDocuments
        if (ofNullable(caseData.getScannedDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(ScannedDocument.builder().build())
                .getUrl()))) {
            return duplicatedWarning;
        }
        // approvedOrderCollection
        if (ofNullable(caseData.getApprovedOrderCollection()).orElse(List.of())
            .stream()
            .anyMatch(d ->
                isDuplicateFilename(caseDocument, () -> ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build())
                    .getConsentOrder())
                || isDuplicateFilename(caseDocument, () -> ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build())
                    .getOrderLetter())
                || ofNullable(ofNullable(d.getApprovedOrder()).orElse(ApprovedOrder.builder().build()).getPensionDocuments()).orElse(List.of())
                    .stream()
                    .anyMatch(e -> isDuplicateFilename(caseDocument, () -> ofNullable(e.getTypedCaseDocument()).orElse(PensionType.builder().build())
                        .getPensionDocument()))
            )
        ) {
            return duplicatedWarning;
        }
        // scannedD81s
        if (ofNullable(caseData.getScannedD81s()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
            return duplicatedWarning;
        }
        // additionalHearingDocuments
        if (ofNullable(caseData.getAdditionalHearingDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(AdditionalHearingDocument.builder().build())
                .getDocument()))) {
            return duplicatedWarning;
        }
        // hearingNoticeDocumentPack
        if (ofNullable(caseData.getHearingNoticeDocumentPack()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
            return duplicatedWarning;
        }
        // hearingNoticesDocumentCollection
        if (ofNullable(caseData.getHearingNoticesDocumentCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
            return duplicatedWarning;
        }
        // uploadGeneralDocuments
        if (ofNullable(caseData.getUploadGeneralDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadGeneralDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // uploadHearingOrder
        if (ofNullable(caseData.getUploadHearingOrder()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(DirectionOrder.builder().build())
                .getUploadDraftDocument()))) {
            return duplicatedWarning;
        }
        // hearingOrderOtherDocuments
        if (ofNullable(caseData.getHearingOrderOtherDocuments()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(CaseDocument.builder().build())))) {
            return duplicatedWarning;
        }
        // finalOrderCollection
        if (ofNullable(caseData.getFinalOrderCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(DirectionOrder.builder().build())
                .getUploadDraftDocument()))) {
            return duplicatedWarning;
        }
        // intv1HearingNoticesCollection, intv2HearingNoticesCollection, intv3HearingNoticesCollection, intv4HearingNoticesCollection
        if (Stream.of(ofNullable(caseData.getIntv1HearingNoticesCollection()).orElse(List.of()),
                ofNullable(caseData.getIntv2HearingNoticesCollection()).orElse(List.of()),
                ofNullable(caseData.getIntv3HearingNoticesCollection()).orElse(List.of()),
                ofNullable(caseData.getIntv4HearingNoticesCollection()).orElse(List.of()))
            .flatMap(List::stream)
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(IntervenerHearingNotice.builder().build())
                .getCaseDocument()))) {
            return duplicatedWarning;
        }
        // refusalOrderCollection
        if (ofNullable(caseData.getRefusalOrderCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(RefusalOrder.builder().build())
                .getRefusalOrderAdditionalDocument()))) {
            return duplicatedWarning;
        }
        // uploadAdditionalDocument
        if (ofNullable(caseData.getUploadAdditionalDocument()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadAdditionalDocument.builder().build())
                .getAdditionalDocuments()))) {
            return duplicatedWarning;
        }
        // confidentialDocumentsUploaded
        if (ofNullable(caseData.getConfidentialDocumentsUploaded()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(UploadConfidentialDocument.builder().build())
                .getDocumentLink()))) {
            return duplicatedWarning;
        }
        // manageCaseDocumentCollection
        if (ofNullable(caseData.getManageCaseDocumentCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getUploadCaseDocument()).orElse(UploadCaseDocument.builder().build())
                .getCaseDocuments()))) {
            return duplicatedWarning;
        }
        // applicantScanDocuments, respondentScanDocuments
        if (Stream.of(ofNullable(caseData.getApplicantScanDocuments()).orElse(List.of()),
                ofNullable(caseData.getRespondentScanDocuments()).orElse(List.of()))
            .flatMap(List::stream)
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(ScannedDocument.builder().build())
                .getUrl()))) {
            return duplicatedWarning;
        }
        // manageScannedDocumentCollection
        if (ofNullable(caseData.getManageScannedDocumentCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getManageScannedDocument()).orElse(ManageScannedDocument.builder()
                    .uploadCaseDocument(UploadCaseDocument.builder().build())
                    .build()).getUploadCaseDocument().getCaseDocuments()))) {
            return duplicatedWarning;
        }
        // ordersSentToPartiesCollection
        if (ofNullable(caseData.getOrdersSentToPartiesCollection()).orElse(List.of())
            .stream()
            .anyMatch(d -> isDuplicateFilename(caseDocument, () -> ofNullable(d.getValue()).orElse(SendOrderDocuments.builder().build())
                .getCaseDocument()))) {
            return duplicatedWarning;
        }

        return Collections.emptyList();
    }
}
