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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_AUTHORISATION_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3B;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3B;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_IN_CONTESTED_ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_AUTHORISATION_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
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

        //translation is required to map the natureOfApplication2 values from contested to the consented equivalent
        // i.e. periodicalPaymentOrder + propertyAdjustmentOrder
        optionIdToValueTranslator.translateOptionsValues.accept(caseDetailsCopy);
        prepareMiniFormFields(caseDetailsCopy);

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getMiniFormTemplate(),
            documentConfiguration.getMiniFormFileName());
    }

    private void prepareMiniFormFields(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        //Solicitor Details
        caseData.put(CONSENTED_SOLICITOR_NAME, caseData.remove(CONTESTED_SOLICITOR_NAME));
        caseData.put(CONSENTED_AUTHORISATION_FIRM, caseData.remove(CONTESTED_AUTHORISATION_FIRM));
        caseData.put(CONSENTED_SOLICITOR_FIRM, caseData.remove(CONTESTED_SOLICITOR_FIRM));
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, caseData.remove(CONTESTED_SOLICITOR_ADDRESS));

        //Respondent Details
        caseData.put(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, caseData.remove(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME));
        caseData.put(CONSENTED_RESPONDENT_LAST_NAME, caseData.remove(CONTESTED_RESPONDENT_LAST_NAME));
        caseData.put(CONSENTED_RESPONDENT_REPRESENTED, caseData.remove(CONTESTED_RESPONDENT_REPRESENTED));

        //Checklist
        caseData.put(CONSENTED_NATURE_OF_APPLICATION, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_3A, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3A));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_3B,caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3B));

        //Order For Children Reasons
        caseData.put(CONSENTED_ORDER_FOR_CHILDREN, caseData.remove(CONSENT_IN_CONTESTED_ORDER_FOR_CHILDREN));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_5, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_5));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_6, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_6));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_7, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_7));
    }
}