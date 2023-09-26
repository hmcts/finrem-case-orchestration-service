package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@Component
@Slf4j
@SuppressWarnings({"java:S110", "java:S2387"})
public class FormCandGCorresponder extends HearingCorresponder {

    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormCandGCorresponder(BulkPrintService bulkPrintService,
                                 NotificationService notificationService,
                                 FinremCaseDetailsMapper finremCaseDetailsMapper,
                                 DocumentHelper documentHelper, ObjectMapper objectMapper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper, documentHelper);
        this.documentHelper = documentHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CaseDocument> getCaseDocuments(CaseDetails caseDetails) {
        String caseId = caseDetails.getId() == null ? "noId" : caseDetails.getId().toString();
        return getHearingCaseDocuments(caseDetails.getData(), caseId);
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private List<CaseDocument> getHearingCaseDocuments(Map<String, Object> caseData, String caseId) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        try {
            caseData = objectMapper.readValue(objectMapper.writeValueAsString(caseData), HashMap.class);
        } catch (JsonProcessingException e) {
            return caseDocuments;
        }

        log.info("Fetching Contested Paper Case bulk print document for caseId {}", caseId);

        Optional.ofNullable(documentHelper.nullCheckAndConvertToCaseDocument(caseData.get(FORM_C))).ifPresent(caseDocuments::add);
        Optional.ofNullable(documentHelper.nullCheckAndConvertToCaseDocument(caseData.get(FORM_G))).ifPresent(caseDocuments::add);
        Optional.ofNullable(documentHelper.nullCheckAndConvertToCaseDocument(caseData.get(MINI_FORM_A))).ifPresent(caseDocuments::add);
        Optional.ofNullable(documentHelper.nullCheckAndConvertToCaseDocument(caseData.get(OUT_OF_FAMILY_COURT_RESOLUTION)))
            .ifPresent(caseDocuments::add);
        Optional.ofNullable(documentHelper.nullCheckAndConvertToCaseDocument(caseData.get(HEARING_ADDITIONAL_DOC))).ifPresent(caseDocuments::add);
        caseDocuments.addAll(documentHelper.getFormADocumentsData(caseData));

        return caseDocuments;
    }


}
