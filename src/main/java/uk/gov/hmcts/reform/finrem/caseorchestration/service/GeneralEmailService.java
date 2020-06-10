package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_CREATED_BY_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_RECIPIENT_ADDRESS;


@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralEmailService {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private Function<CaseDetails, GeneralEmail> createGeneralEmailData = this::applyGeneralEmailData;

    public CaseDetails storeGeneralEmail(CaseDetails caseDetails) {

        log.info("Storing general email for Case ID: {}", caseDetails.getId());
        return createGeneralEmailData
            .andThen(data -> populateGeneralEmailData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, CaseDetails.class));
    }

    private GeneralEmail applyGeneralEmailData(CaseDetails caseDetails) {
        GeneralEmailData generalEmailData = new GeneralEmailData();
        generalEmailData.setGeneralEmailRecipientAddress(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_RECIPIENT_ADDRESS)));
        generalEmailData.setGeneralEmailCreatedByName(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_CREATED_BY_NAME)));
        generalEmailData.setGeneralEmailBody(Objects.toString(caseDetails.getData().get(GENERAL_EMAIL_BODY)));

        GeneralEmail generalEmail = new GeneralEmail();
        generalEmail.setId(UUID.randomUUID().toString());
        generalEmail.setGeneralEmailData(generalEmailData);
        return generalEmail;
    }

    private CaseDetails populateGeneralEmailData(GeneralEmail generalEmail, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralEmail> generalEmailList = Optional.ofNullable(caseData.get(GENERAL_EMAIL))
            .map(this::convertToUploadOrderList)
            .orElse(new ArrayList<>());

        generalEmailList.add(generalEmail);
        caseDetails.getData().put(GENERAL_EMAIL, generalEmailList);
        return caseDetails;
    }

    private List<GeneralEmail> convertToUploadOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralEmail>>() {
        });
    }
}
