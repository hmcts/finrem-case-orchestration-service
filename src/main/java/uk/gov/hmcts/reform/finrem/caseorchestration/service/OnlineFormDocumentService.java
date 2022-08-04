package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.ConsentInContestMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.MiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested.ContestedMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.VARIATION;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineFormDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final MiniFormADetailsMapper miniFormADetailsMapper;
    private final ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapper;
    private final ConsentInContestMiniFormADetailsMapper consentInContestMiniFormADetailsMapper;
    private final ConsentedApplicationHelper consentedApplicationHelper;

    public Document generateMiniFormA(String authorisationToken, FinremCaseDetails caseDetails) {
        Map<String, Object> miniFormADetailsMap = miniFormADetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            new DefaultCourtListWrapper());

        log.info("Generating Consented Mini Form A for Case ID : {}", caseDetails.getId());
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            miniFormADetailsMap,
            documentConfiguration.getMiniFormTemplate(),
            documentConfiguration.getMiniFormFileName());
    }

    public Document generateContestedMiniFormA(String authorisationToken, FinremCaseDetails caseDetails) {
        Map<String, Object> contestedMiniFormPlaceholdersMap = contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(
            caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        log.info("Generating Contested Mini Form A for Case ID : {}", caseDetails.getId());
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            contestedMiniFormPlaceholdersMap,
            documentConfiguration.getContestedMiniFormTemplate(),
            documentConfiguration.getContestedMiniFormFileName());
    }

    public Document generateDraftContestedMiniFormA(String authorisationToken, FinremCaseDetails caseDetails) {
        log.info("Generating Draft Contested Mini Form A for Case ID : {}", caseDetails.getId());

        Map<String, Object> contestedMiniFormPlaceholdersMap = contestedMiniFormADetailsMapper.getDocumentTemplateDetailsAsMap(
            caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document caseDocument = genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            contestedMiniFormPlaceholdersMap,
            documentConfiguration.getContestedDraftMiniFormTemplate(),
            documentConfiguration.getContestedDraftMiniFormFileName());

        Optional.ofNullable(caseDetails.getCaseData().getMiniFormA()).ifPresent(data ->
            deleteOldMiniFormA(data, authorisationToken));
        return caseDocument;
    }

    private void deleteOldMiniFormA(Document document, String authorisationToken) {
        CompletableFuture.runAsync(() -> {
            try {
                genericDocumentService.deleteDocument(document.getUrl(), authorisationToken);
            } catch (Exception e) {
                log.info("Failed to delete existing mini-form-a. Error occurred: {}", e.getMessage());
            }
        });
    }

    public Document generateConsentedInContestedMiniFormA(FinremCaseDetails caseDetails, String authorisationToken) {

        log.info("Generating 'Consented in Contested' Mini Form A for Case ID : {}", caseDetails.getId());

        Map<String, Object> consentInContestFormDetailsMap = consentInContestMiniFormADetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            consentInContestFormDetailsMap,
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
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_3B, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_3B));

        //Order For Children Reasons
        caseData.put(CONSENTED_ORDER_FOR_CHILDREN, caseData.remove(CONSENT_IN_CONTESTED_ORDER_FOR_CHILDREN));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_5, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_5));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_6, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_6));
        caseData.put(CONSENTED_NATURE_OF_APPLICATION_7, caseData.remove(CONSENT_IN_CONTESTED_NATURE_OF_APPLICATION_7));

        if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseDetails.getData()))) {
            caseData.put(ORDER_TYPE, VARIATION);
        } else {
            caseData.put(ORDER_TYPE, CONSENT);
        }
    }
}