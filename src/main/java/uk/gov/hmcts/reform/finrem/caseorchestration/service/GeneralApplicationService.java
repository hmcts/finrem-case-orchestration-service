package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_RECEIVED_FROM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_TIME_ESTIMATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationService {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final IdamService idamService;
    private final GenericDocumentService genericDocumentService;

    public void updateCaseDataSubmit(Map<String, Object> caseData, CaseDetails caseDetailsBefore, String authorisationToken) {
        caseData.put(GENERAL_APPLICATION_PRE_STATE, caseDetailsBefore.getState());
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST_DATE, LocalDate.now());

        CaseDocument applicationDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
            documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DOCUMENT)), authorisationToken);
        caseData.put(GENERAL_APPLICATION_DOCUMENT_LATEST, applicationDocument);

        if (caseData.get(GENERAL_APPLICATION_DRAFT_ORDER) != null) {
            CaseDocument draftOrderPdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(
                documentHelper.convertToCaseDocument(caseData.get(GENERAL_APPLICATION_DRAFT_ORDER)), authorisationToken);
            caseData.put(GENERAL_APPLICATION_DRAFT_ORDER, draftOrderPdfDocument);
        }
        updateGeneralApplicationDocumentCollection(caseData, applicationDocument);
    }

    private void updateGeneralApplicationDocumentCollection(Map<String, Object> caseData, CaseDocument applicationDocument) {
        GeneralApplication generalApplication = GeneralApplication.builder().generalApplicationDocument(applicationDocument).build();

        List<GeneralApplicationData> generalApplicationList = Optional.ofNullable(caseData.get(GENERAL_APPLICATION_DOCUMENT_COLLECTION))
            .map(this::convertToGeneralApplicationDataList)
            .orElse(new ArrayList<>());

        generalApplicationList.add(
            GeneralApplicationData.builder()
                .id(UUID.randomUUID().toString())
                .generalApplication(generalApplication)
                .build()
        );

        caseData.put(GENERAL_APPLICATION_DOCUMENT_COLLECTION, generalApplicationList);
    }

    private List<GeneralApplicationData> convertToGeneralApplicationDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralApplicationData>>() {
        });
    }

    public void updateCaseDataStart(Map<String, Object> caseData, String authorisationToken) {
        Stream.of(GENERAL_APPLICATION_RECEIVED_FROM,
            GENERAL_APPLICATION_HEARING_REQUIRED,
            GENERAL_APPLICATION_TIME_ESTIMATE,
            GENERAL_APPLICATION_SPECIAL_MEASURES,
            GENERAL_APPLICATION_DOCUMENT,
            GENERAL_APPLICATION_DRAFT_ORDER,
            GENERAL_APPLICATION_DIRECTIONS_DOCUMENT
        ).forEach(ccdFieldName -> caseData.remove(ccdFieldName));
        caseData.put(GENERAL_APPLICATION_CREATED_BY, idamService.getIdamFullName(authorisationToken));
    }
}
