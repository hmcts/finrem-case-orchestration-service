package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_RECIPIENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralEmailService {

    private final ObjectMapper objectMapper;

    public void storeGeneralEmail(CaseDetails caseDetails) {
        log.info("Storing general email for Case ID: {}", caseDetails.getId());

        GeneralEmail generalEmail = makeGeneralEmail(caseDetails);
        addGeneralEmailToCollection(caseDetails, generalEmail);
    }

    private GeneralEmail makeGeneralEmail(CaseDetails caseDetails) {
        GeneralEmailData generalEmailData = new GeneralEmailData();
        generalEmailData.setGeneralEmailRecipient(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT)));
        generalEmailData.setGeneralEmailCreatedBy(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_CREATED_BY)));
        generalEmailData.setGeneralEmailBody(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_BODY)));

        GeneralEmail generalEmail = new GeneralEmail();
        generalEmail.setId(UUID.randomUUID().toString());
        generalEmail.setGeneralEmailData(generalEmailData);
        return generalEmail;
    }

    private void addGeneralEmailToCollection(CaseDetails caseDetails, GeneralEmail generalEmail) {
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralEmail> generalEmailList = Optional.ofNullable(caseData.get(GENERAL_EMAIL_COLLECTION))
            .map(this::convertToGeneralEmailList)
            .orElse(new ArrayList<>());

        generalEmailList.add(generalEmail);
        caseDetails.getData().put(GENERAL_EMAIL_COLLECTION, generalEmailList);
    }

    private List<GeneralEmail> convertToGeneralEmailList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
