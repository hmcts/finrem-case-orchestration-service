package uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo.UpdateFrcInfoLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.generators.UpdateFrcInfoLetterDetailsGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public abstract class BaseUpdateFrcInfoDocumentService {

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    protected final CaseDataService caseDataService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    @Autowired
    public BaseUpdateFrcInfoDocumentService(GenericDocumentService genericDocumentService,
                                            DocumentConfiguration documentConfiguration,
                                            CaseDataService caseDataService,
                                            UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        this.genericDocumentService = genericDocumentService;
        this.documentConfiguration = documentConfiguration;
        this.caseDataService = caseDataService;
        this.updateFrcInfoLetterDetailsGenerator = updateFrcInfoLetterDetailsGenerator;
    }

    public abstract Optional<CaseDocument> getUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken);

    protected CaseDocument generateSolicitorUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken,
                                                                DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Update FRC Info Letter for {} SOLICITOR for caseId {}", recipient, caseDetails.getId());
        String template = documentConfiguration.getUpdateFRCInformationSolicitorTemplate();
        String fileName = documentConfiguration.getUpdateFRCInformationSolicitorFilename();

        return generateUpdateFrcInfoLetter(caseDetails, authToken, recipient, template, fileName);
    }

    protected CaseDocument generateLitigantUpdateFrcInfoLetter(CaseDetails caseDetails,
                                                               String authToken,
                                                               DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Update FRC Info Letter for {} for caseId {}", recipient, caseDetails.getId());
        String template = documentConfiguration.getUpdateFRCInformationLitigantTemplate();
        String filename = documentConfiguration.getUpdateFRCInformationLitigantFilename();

        return generateUpdateFrcInfoLetter(caseDetails, authToken, recipient, template, filename);
    }

    private CaseDocument generateUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken,
                                                     DocumentHelper.PaperNotificationRecipient recipient,
                                                     String template, String filename) {
        UpdateFrcInfoLetterDetails letterDetails = updateFrcInfoLetterDetailsGenerator.generate(caseDetails, recipient);
        Map letterDetailsMap = convertUpdateFrcInfoLetterDetailsToMap(letterDetails);
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, letterDetailsMap, template, filename);
    }

    private Map convertUpdateFrcInfoLetterDetailsToMap(UpdateFrcInfoLetterDetails letterDetails) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap caseDetailsMap = new HashMap<String, Object>();
        HashMap caseDataMap = new HashMap<String, Object>();
        caseDataMap.put(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }
}
