package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.ConsentInContestMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformaconsented.MiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.miniformacontested.ContestedMiniFormADetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.DefaultCourtListWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineFormDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final MiniFormADetailsMapper miniFormADetailsMapper;
    private final ContestedMiniFormADetailsMapper contestedMiniFormADetailsMapper;
    private final ConsentInContestMiniFormADetailsMapper consentInContestMiniFormADetailsMapper;

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
}