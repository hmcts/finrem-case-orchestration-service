package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;

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
            .generalEmailUploadedDocument(createNewCaseDocumentObject(generalEmailWrapper.getGeneralEmailUploadedDocument()))
            .build()).build();
        generalEmailCollection.add(collection);

        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailCollection(generalEmailCollection);
    }

    private CaseDocument createNewCaseDocumentObject(CaseDocument document) {
        return CaseDocument.builder()
            .documentBinaryUrl(document.getDocumentBinaryUrl())
            .documentFilename(document.getDocumentFilename())
            .documentUrl(document.getDocumentUrl())
            .categoryId(document.getCategoryId())
            .build();
    }
}
