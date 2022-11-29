package uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.generalapplication.GeneralApplicationRejectionLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.generators.GeneralApplicationRejectionLetterGenerator;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RejectGeneralApplicationDocumentService  {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private final GeneralApplicationRejectionLetterGenerator generalApplicationRejectionLetterGenerator;

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    public CaseDocument generateGeneralApplicationRejectionLetter(CaseDetails caseDetails, String authToken,
                                                     DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating General Application Rejection Letter for {} for caseId {}", recipient, caseDetails.getId());
        GeneralApplicationRejectionLetterDetails letterDetails = generalApplicationRejectionLetterGenerator
            .generate(caseDetails, recipient, null);
        Map letterDetailsMap = convertGeneralApplicationRejectionLetterDetailsToMap(letterDetails);
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, letterDetailsMap,
            documentConfiguration.getGeneralApplicationRejectionTemplate(),
            documentConfiguration.getGeneralApplicationRejectionFileName());
    }

    private Map convertGeneralApplicationRejectionLetterDetailsToMap(GeneralApplicationRejectionLetterDetails letterDetails) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap caseDetailsMap = new HashMap<String, Object>();
        HashMap caseDataMap = new HashMap<String, Object>();
        caseDataMap.put(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }
}
