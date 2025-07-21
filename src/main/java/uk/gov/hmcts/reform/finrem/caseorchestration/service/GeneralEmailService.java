package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralEmailService {

    public void storeGeneralEmail(FinremCaseDetails caseDetails) {
        log.info("Storing general email for Case ID: {}", caseDetails.getId());
        addGeneralEmailToCollection(caseDetails);
    }

    private void addGeneralEmailToCollection(FinremCaseDetails caseDetails) {
        GeneralEmailWrapper generalEmailWrapper = caseDetails.getData().getGeneralEmailWrapper();
        List<GeneralEmailCollection> generalEmailCollection = Optional.ofNullable(generalEmailWrapper
                .getGeneralEmailCollection())
            .orElse(new ArrayList<>(1));
        GeneralEmailCollection collection = GeneralEmailCollection.builder().value(GeneralEmailHolder.builder()
            .generalEmailBody(generalEmailWrapper.getGeneralEmailBody())
            .generalEmailCreatedBy(generalEmailWrapper.getGeneralEmailCreatedBy())
            .generalEmailRecipient(generalEmailWrapper.getGeneralEmailRecipient())
            .generalEmailUploadedDocument(createNewCaseDocumentObject(generalEmailWrapper))
            .generalEmailDateSent(LocalDateTime.now())
            .build()).build();
        generalEmailCollection.add(collection);

        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailCollection(generalEmailCollection);
    }

    private CaseDocument createNewCaseDocumentObject(GeneralEmailWrapper wrapper) {
        CaseDocument latestUploadedDocument = wrapper.getGeneralEmailUploadedDocument();
        if (latestUploadedDocument != null) {
            CaseDocument documentToReturn = CaseDocument.builder().build();
            String binaryUrl = latestUploadedDocument.getDocumentBinaryUrl();
            if (binaryUrl != null) {
                documentToReturn.setDocumentBinaryUrl(binaryUrl);
            }
            String fileName = latestUploadedDocument.getDocumentFilename();
            if (fileName != null) {
                documentToReturn.setDocumentFilename(fileName);
            }
            String url = latestUploadedDocument.getDocumentUrl();
            if (url != null) {
                documentToReturn.setDocumentUrl(url);
            }
            return documentToReturn;
        }
        return null;
    }
}
