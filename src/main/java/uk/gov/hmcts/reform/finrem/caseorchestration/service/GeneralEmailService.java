package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralEmailService {

    /**
     * Stores the current general email details in the general email history collection.
     *
     * <p>If the general email collection does not already exist, a new collection is created.
     * The stored history includes the email body, recipient, creator, uploaded documents,
     * and the date and time the email was sent.</p>
     *
     * @param finremCaseData the case data containing the general email information to be stored
     */
    public void storeGeneralEmail(FinremCaseData finremCaseData) {
        GeneralEmailWrapper generalEmailWrapper = finremCaseData.getGeneralEmailWrapper();
        List<GeneralEmailCollection> generalEmailCollection = generalEmailWrapper
            .getGeneralEmailCollection();
        if (isNull(generalEmailCollection)) {
            generalEmailCollection = new ArrayList<>();
            generalEmailWrapper.setGeneralEmailCollection(generalEmailCollection);
        }

        GeneralEmailCollection collection = GeneralEmailCollection.builder().value(GeneralEmailHolder.builder()
            .generalEmailBody(generalEmailWrapper.getGeneralEmailBody())
            .generalEmailCreatedBy(generalEmailWrapper.getGeneralEmailCreatedBy())
            .generalEmailRecipient(generalEmailWrapper.getGeneralEmailRecipient())
            .generalEmailUploadedDocument(generalEmailWrapper.getGeneralEmailUploadedDocument())
            .generalEmailDateSent(LocalDateTime.now())
            .build()).build();
        generalEmailCollection.add(collection);
    }
}
