package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineFormDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final OptionIdToValueTranslator optionIdToValueTranslator;
    private final DocumentHelper documentHelper;

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {

        log.info("Generating Consented Mini Form A for Case ID : {}", caseDetails.getId());
        return genericDocumentService.generateDocument(authorisationToken, caseDetails,
            documentConfiguration.getMiniFormTemplate(),
            documentConfiguration.getMiniFormFileName());
    }

    public CaseDocument generateContestedMiniFormA(String authorisationToken, CaseDetails caseDetails) {

        log.info("Generating Contested Mini Form A for Case ID : {}", caseDetails.getId());
        return genericDocumentService.generateDocument(authorisationToken, translateOptions(caseDetails),
            documentConfiguration.getContestedMiniFormTemplate(),
            documentConfiguration.getContestedMiniFormFileName());
    }

    public CaseDocument generateDraftContestedMiniFormA(String authorisationToken, CaseDetails caseDetails) {

        log.info("Generating Draft Contested Mini Form A for Case ID : {}", caseDetails.getId());
        CaseDocument caseDocument = genericDocumentService.generateDocument(authorisationToken, translateOptions(caseDetails),
            documentConfiguration.getContestedDraftMiniFormTemplate(),
            documentConfiguration.getContestedDraftMiniFormFileName());

        Optional.ofNullable(miniFormData(caseDetails)).ifPresent(data -> deleteOldMiniFormA(data, authorisationToken));
        return caseDocument;
    }

    private CaseDetails translateOptions(CaseDetails caseDetails) {
        CaseDetails copy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        optionIdToValueTranslator.translateOptionsValues.accept(copy);

        return copy;
    }

    private Map<String, Object> miniFormData(CaseDetails caseDetails) {
        return (Map<String, Object>) caseDetails.getData().get(MINI_FORM_A);
    }

    private void deleteOldMiniFormA(Map<String, Object> documentData, String authorisationToken) {
        String documentUrl = (String) documentData.get("document_url");
        CompletableFuture.runAsync(() -> {
            try {
                genericDocumentService.deleteDocument(documentUrl, authorisationToken);
            } catch (Exception e) {
                log.info("Failed to delete existing mini-form-a. Error occurred: {}", e.getMessage());
            }
        });
    }

    public CaseDocument generateConsentedInContestedMiniFormA(CaseDetails caseDetails, String authorisationToken) {

        log.info("Generating 'Consented in Contested' Mini Form A for Case ID : {}", caseDetails.getId());

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareMiniFormFields(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getMiniFormTemplate(),
            documentConfiguration.getMiniFormFileName());
    }

    private void prepareMiniFormFields(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        //Solicitor Details
        caseData.put("solicitorName", nullToEmpty(caseData.get("applicantSolicitorName")));
        caseData.put("solicitorFirm", nullToEmpty(caseData.get("applicantSolicitorFirm")));
        caseData.put("solicitorAddress", nullToEmpty(caseData.get("applicantSolicitorAddress")));

        //Respondent Details
        caseData.put("appRespondentFMName", nullToEmpty(caseData.get("respondentFMName")));
        caseData.put("appRespondentLName", nullToEmpty(caseData.get("respondentLName")));
        caseData.put("appRespondentRep", nullToEmpty(caseData.get("respondentRepresented")));

        //Checklist
        caseData.put("natureOfApplicationChecklist", nullToEmpty(caseData.get("consentNatureOfApplicationChecklist")));
        caseData.put("natureOfApplication3a", nullToEmpty(caseData.get("consentNatureOfApplicationAddress")));
        caseData.put("natureOfApplication3b", nullToEmpty(caseData.get("consentNatureOfApplicationMortgage")));

        //Order For Children Reasons
        caseData.put("orderForChildrenQuestion1", nullToEmpty(caseData.get("consentOrderForChildrenQuestion1")));
        caseData.put("natureOfApplication5", nullToEmpty(caseData.get("consentNatureOfApplication5")));
        caseData.put("natureOfApplication6", nullToEmpty(caseData.get("consentNatureOfApplication6")));
        caseData.put("natureOfApplication7", nullToEmpty(caseData.get("consentNatureOfApplication7")));
    }
}