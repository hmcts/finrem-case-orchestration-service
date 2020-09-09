package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationService {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public void updateCaseDataSubmit(Map<String, Object> caseData) {
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE, LocalDate.now());
        updateGeneralApplicationDocumentCollection(caseData);
        updateGeneralApplicationDocumentLatest(caseData);
    }

    private void updateGeneralApplicationDocumentLatest(Map<String, Object> caseData) {
        log.info("updateGeneralApplicationDocumentLatest caseData: {}", caseData);
        GeneralApplication generalApplication =
            new GeneralApplication(documentHelper.convertToCaseDocument(
                caseData.get(GENERAL_APPLICATION_DOCUMENT)));

        GeneralApplicationData generalApplicationData = new GeneralApplicationData(UUID.randomUUID().toString(), generalApplication);

        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST, generalApplicationData);
    }

    private void updateGeneralApplicationDocumentCollection(Map<String, Object> caseData) {
        log.info("updateGeneralApplicationDocumentCollection caseData: {}", caseData);
        GeneralApplication generalApplication =
            new GeneralApplication(documentHelper.convertToCaseDocument(
                caseData.get(GENERAL_APPLICATION_DOCUMENT)));

        GeneralApplicationData generalApplicationData = new GeneralApplicationData(UUID.randomUUID().toString(), generalApplication);

        List<GeneralApplicationData> generalApplicationDataList = Optional.ofNullable(caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
            .map(this::convertToGeneralApplicationDataList)
            .orElse(new ArrayList<>());

        generalApplicationDataList.add(generalApplicationData);
        caseData.put(GENERAL_APPLICATION_DOCUMENT_COLLECTION, generalApplicationDataList);
    }

    private List<GeneralApplicationData> convertToGeneralApplicationDataList(Object object) {
        log.info("convertToGeneralApplicationDataList object: {}", object);
        return objectMapper.convertValue(object, new TypeReference<List<GeneralApplicationData>>() {});
    }

    public void updateCaseDataStart(Map<String, Object> caseData) {
        caseData.remove(GENERAL_APPLICATION_RECEIVED_FROM);
        caseData.remove(GENERAL_APPLICATION_CREATED_BY);
        caseData.remove(GENERAL_APPLICATION_HEARING_REQUIRED);
        caseData.remove(GENERAL_APPLICATION_TIME_ESTIMATE);
        caseData.remove(GENERAL_APPLICATION_SPECIAL_MEASURES);
        caseData.remove(GENERAL_APPLICATION_DOCUMENT);
        caseData.remove(GENERAL_APPLICATION_DRAFT_ORDER);
    }
}
