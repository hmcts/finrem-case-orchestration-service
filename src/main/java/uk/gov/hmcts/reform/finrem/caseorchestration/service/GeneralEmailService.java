package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final ObjectMapper objectMapper;

    public void storeGeneralEmail(FinremCaseDetails caseDetails) {
        log.info("Storing general email for Case ID: {}", caseDetails.getId());

        addGeneralEmailToCollection(caseDetails);
    }

    private void addGeneralEmailToCollection(FinremCaseDetails caseDetails) {
        GeneralEmailWrapper generalEmailWrapper = caseDetails.getData().getGeneralEmailWrapper();
        List<GeneralEmailCollection> generalEmailCollection = Optional.ofNullable(generalEmailWrapper
                .getGeneralEmailCollection())
            .orElse(new ArrayList<>(1));

        generalEmailCollection.add(GeneralEmailCollection.builder().value(GeneralEmailHolder.builder()
                .generalEmailBody(generalEmailWrapper.getGeneralEmailBody())
                .generalEmailCreatedBy(generalEmailWrapper.getGeneralEmailCreatedBy())
                .generalEmailRecipient(generalEmailWrapper.getGeneralEmailRecipient())
                .generalEmailUploadedDocument(generalEmailWrapper.getGeneralEmailUploadedDocument())
            .build()).build());

        caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailCollection(generalEmailCollection);
    }
}
