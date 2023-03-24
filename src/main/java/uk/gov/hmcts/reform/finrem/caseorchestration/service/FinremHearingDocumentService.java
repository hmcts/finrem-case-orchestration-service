package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formc.FormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg.FormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremFormCandGCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinremHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final FormCLetterDetailsMapper formCLetterDetailsMapper;
    private final FormGLetterDetailsMapper formGLetterDetailsMapper;

    private final FinremFormCandGCorresponder formCandGCorresponder;


    public void generateHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        if (caseDetails.getData().getFastTrackDecision() != null) {
            courtCoverSheetDocuments(caseDetails, authorisationToken);
        } else {
            throw new IllegalArgumentException("Fast track decision is null");
        }
    }

    private void courtCoverSheetDocuments(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        if (finremCaseDetails.getData().isFastTrackApplication()) {
            generateFastTrackFormC(finremCaseDetails, authorisationToken);
        } else {
            generateFormCAndG(finremCaseDetails, authorisationToken);
        }
        finremCaseDetails.getData().setOutOfFamilyCourtResolution(generateOutOfFamilyCourtResolutionDocument(finremCaseDetails, authorisationToken));

    }

    private void generateFormCAndG(FinremCaseDetails finremCaseDetails, String authorisationToken) {

        Map<String, Object> formCDetailsMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
            finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList());

        CaseDocument formCNonFastTrack =
            genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, formCDetailsMap,
                documentConfiguration.getFormCNonFastTrackTemplate(finremCaseDetails), documentConfiguration.getFormCFileName());
        finremCaseDetails.getData().setFormC(formCNonFastTrack);

        Map<String, Object> formGDetailsMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
            finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList());

        CaseDocument formG = genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken, formGDetailsMap,
            documentConfiguration.getFormGTemplate(finremCaseDetails), documentConfiguration.getFormGFileName());
        finremCaseDetails.getData().setFormG(formG);


    }

    private void generateFastTrackFormC(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        Map<String, Object> formCDetailsMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
            finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList());
        finremCaseDetails.getData().setFormC(
            genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, formCDetailsMap,
                documentConfiguration.getFormCFastTrackTemplate(finremCaseDetails), documentConfiguration.getFormCFileName()));
    }

    private CaseDocument generateOutOfFamilyCourtResolutionDocument(FinremCaseDetails finremCaseDetails, String authorisationToken) {
        final Map<String, Object> documentTemplateDetailsAsMap = formCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(
            finremCaseDetails, finremCaseDetails.getData().getRegionWrapper().getDefaultCourtList());
        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken,
            documentTemplateDetailsAsMap,
            documentConfiguration.getOutOfFamilyCourtResolutionTemplate(),
            documentConfiguration.getOutOfFamilyCourtResolutionName());
    }

    public boolean alreadyHadFirstHearing(FinremCaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails.getData().getFormC()).isPresent();
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(FinremCaseData caseData) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();

        log.info("Fetching Contested Paper Case bulk print document from Case Data: {}", caseData);

        documentHelper.getDocumentAsBulkPrintDocument(caseData.getFormC()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getFormG()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getMiniFormA()).ifPresent(caseDocuments::add);
        documentHelper.getDocumentAsBulkPrintDocument(caseData.getOutOfFamilyCourtResolution()).ifPresent(caseDocuments::add);

        List<CaseDocument> formADocuments = Optional.ofNullable(caseData.getCopyOfPaperFormA().stream()
                .map(collectionElement -> collectionElement.getValue().getUploadedDocument()).toList())
            .orElse(new ArrayList<>());

        caseDocuments.addAll(formADocuments.stream()
            .map(documentHelper::getDocumentAsBulkPrintDocument)
            .flatMap(Optional::stream)
            .toList());

        log.info("Sending Contested Paper Case bulk print documents for {} from Case Data: {}",
            caseData.getCcdCaseId(), caseData);

        return caseDocuments;
    }


    public void sendInitialHearingCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        formCandGCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }
}
