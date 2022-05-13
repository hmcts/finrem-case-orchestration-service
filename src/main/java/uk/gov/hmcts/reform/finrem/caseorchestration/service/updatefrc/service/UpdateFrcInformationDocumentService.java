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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@Slf4j
public class UpdateFrcInformationDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;
    private final UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator;

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    @Autowired
    public UpdateFrcInformationDocumentService(GenericDocumentService genericDocumentService,
                                               DocumentConfiguration documentConfiguration,
                                               CaseDataService caseDataService,
                                               UpdateFrcInfoLetterDetailsGenerator updateFrcInfoLetterDetailsGenerator) {
        this.genericDocumentService = genericDocumentService;
        this.documentConfiguration = documentConfiguration;
        this.caseDataService = caseDataService;
        this.objectMapper = new ObjectMapper();
        this.updateFrcInfoLetterDetailsGenerator = updateFrcInfoLetterDetailsGenerator;
    }

    public List<CaseDocument> getUpdateFrcInfoLetters(CaseDetails caseDetails, String authToken) {

        List<CaseDocument> lettersToSend = new ArrayList<>();

        if (caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())
            && !caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {

            lettersToSend.add(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        } else if (!caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())) {

            lettersToSend.add(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, APPLICANT));
        }

        if (caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())
            && !caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)) {

            lettersToSend.add(generateSolicitorUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        } else if (!caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())) {

            lettersToSend.add(generateLitigantUpdateFrcInfoLetter(caseDetails, authToken, RESPONDENT));
        }

        return lettersToSend;
    }

    private CaseDocument generateSolicitorUpdateFrcInfoLetter(CaseDetails caseDetails, String authToken,
                                                             DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Update FRC Info Letter for {} solicitor for caseId {}", recipient, caseDetails.getId());
        String template = documentConfiguration.getUpdateFRCInformationSolicitorTemplate();
        String fileName = documentConfiguration.getUpdateFRCInformationSolicitorFilename();

        return generateUpdateFrcInfoLetter(caseDetails, authToken, recipient, template, fileName);
    }

    private CaseDocument generateLitigantUpdateFrcInfoLetter(CaseDetails caseDetails,
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
        HashMap caseDetailsMap = new HashMap<String, Object>();
        HashMap caseDataMap = new HashMap<String, Object>();
        caseDataMap.put(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }
}
