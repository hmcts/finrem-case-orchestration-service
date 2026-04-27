package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;

    private record IntervenerCoverSheetMapping(
        DocumentHelper.PaperNotificationRecipient recipient,
        Supplier<CaseDocument> oldCoverSheetSupplier,
        Consumer<CaseDocument> setter
    ) {
    }

    public CaseDocument generateApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        logCoverSheetGeneration(APPLICANT, caseDetails.getCaseIdAsString());
        return generateCoverSheet(caseDetails, authorisationToken, APPLICANT);
    }

    /**
     * Generates an applicant's cover sheet, determines whether it should be public or confidential
     * based on the applicant's address visibility settings, and stores the generated cover sheet
     * accordingly in the case data.
     *
     * @param caseDetails        the {@link FinremCaseDetails} object containing the case information
     * @param authorisationToken the authorization token used to access and store the generated document
     */
    public void generateAndSetApplicantCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        logCoverSheetGenerationAndStorage(APPLICANT, caseDetails.getCaseIdAsString());

        replaceAndStoreCoverSheet(
            generateApplicantCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent(),
            authorisationToken,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetApp,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetAppConfidential,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetApp,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetAppConfidential
        );
    }

    public CaseDocument generateRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        logCoverSheetGeneration(RESPONDENT, caseDetails.getCaseIdAsString());
        return generateCoverSheet(caseDetails, authorisationToken, RESPONDENT);
    }

    /**
     * Generates a respondent's cover sheet, determines if it should be public or confidential
     * based on the respondent's address visibility settings, and updates the case data with
     * the appropriate cover sheet.
     *
     * @param caseDetails        the {@link FinremCaseDetails} object containing the case information
     * @param authorisationToken the authorization token used to access and store the generated document
     */
    public void generateAndSetRespondentCoverSheet(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        logCoverSheetGenerationAndStorage(RESPONDENT, caseDetails.getCaseIdAsString());

        replaceAndStoreCoverSheet(
            generateRespondentCoverSheet(caseDetails, authorisationToken),
            caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant(),
            authorisationToken,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetRes,
            caseData.getBulkPrintCoversheetWrapper()::getBulkPrintCoverSheetResConfidential,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetRes,
            caseData.getBulkPrintCoversheetWrapper()::setBulkPrintCoverSheetResConfidential
        );
    }

    /**
     * Generates a cover sheet document for the specified intervener.
     *
     * @param caseDetails         the {@link FinremCaseDetails} object containing the case information
     * @param authorisationToken  the authorisation token used to authenticate the request
     * @param intervenerRecipient the recipient information for the intervener, specifying the paper notification details
     * @return the generated cover sheet document as a {@code CaseDocument}
     */
    public CaseDocument generateIntervenerCoverSheet(FinremCaseDetails caseDetails,
                                                     String authorisationToken,
                                                     DocumentHelper.PaperNotificationRecipient intervenerRecipient) {
        logCoverSheetGeneration(intervenerRecipient, caseDetails.getCaseIdAsString());
        return generateCoverSheet(caseDetails, authorisationToken, intervenerRecipient);
    }

    /**
     * Generates and stores an intervener coversheet for a specified case and intervener type.
     *
     * @param caseDetails        the {@link FinremCaseDetails} object containing the case information
     * @param intervenerType     the type of the intervener for whom the coversheet is being generated
     * @param authorisationToken the authorization token to be used for generating and storing the coversheet
     */
    public void generateAndStoreIntervenerCoversheet(FinremCaseDetails caseDetails,
                                                     IntervenerType intervenerType,
                                                     String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        BulkPrintCoversheetWrapper wrapper = caseData.getBulkPrintCoversheetWrapper();

        IntervenerCoverSheetMapping mapping = resolveIntervenerCoverSheetMapping(wrapper, intervenerType);

        logCoverSheetGenerationAndStorage(mapping.recipient(), caseDetails.getCaseIdAsString());
        replaceAndStoreIntervenerCoverSheet(
            generateIntervenerCoverSheet(caseDetails, authorisationToken, mapping.recipient()),
            authorisationToken,
            mapping.oldCoverSheetSupplier(),
            mapping.setter()
        );
    }

    /**
     * Removes the intervener cover sheet with specified intervener change details.
     *
     * @param finremCaseDetails       the {@link FinremCaseDetails} object containing the case information
     * @param intervenerChangeDetails The details of the intervener change, including the type of intervener.
     * @param authorisationToken      The authorisation token required to authenticate the operation.
     */
    public void removeIntervenerCoverSheet(FinremCaseDetails finremCaseDetails,
                                           IntervenerChangeDetails intervenerChangeDetails,
                                           String authorisationToken) {
        IntervenerCoverSheetMapping mapping =
            resolveIntervenerCoverSheetMapping(
                finremCaseDetails.getData().getBulkPrintCoversheetWrapper(),
                intervenerChangeDetails.getIntervenerType()
            );

        deleteCoverSheet(mapping.oldCoverSheetSupplier().get().getDocumentUrl(), authorisationToken);
        mapping.setter().accept(null);
    }

    private IntervenerCoverSheetMapping resolveIntervenerCoverSheetMapping(BulkPrintCoversheetWrapper wrapper,
                                                                           IntervenerType intervenerType) {
        if (IntervenerType.INTERVENER_ONE.equals(intervenerType)) {
            return new IntervenerCoverSheetMapping(
                DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE,
                wrapper::getBulkPrintCoverSheetIntv1,
                wrapper::setBulkPrintCoverSheetIntv1
            );
        }
        if (IntervenerType.INTERVENER_TWO.equals(intervenerType)) {
            return new IntervenerCoverSheetMapping(
                DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO,
                wrapper::getBulkPrintCoverSheetIntv2,
                wrapper::setBulkPrintCoverSheetIntv2
            );
        }
        if (IntervenerType.INTERVENER_THREE.equals(intervenerType)) {
            return new IntervenerCoverSheetMapping(
                DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE,
                wrapper::getBulkPrintCoverSheetIntv3,
                wrapper::setBulkPrintCoverSheetIntv3
            );
        }
        if (IntervenerType.INTERVENER_FOUR.equals(intervenerType)) {
            return new IntervenerCoverSheetMapping(
                DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR,
                wrapper::getBulkPrintCoverSheetIntv4,
                wrapper::setBulkPrintCoverSheetIntv4
            );
        }

        throw new IllegalArgumentException("Invalid intervener type: " + intervenerType);
    }

    private void replaceAndStoreIntervenerCoverSheet(CaseDocument coverSheet,
                                                     String authToken,
                                                     Supplier<CaseDocument> oldCoverSheetSupplier,
                                                     Consumer<CaseDocument> publicSetter) {
        replaceAndStoreCoverSheet(coverSheet, null, authToken, oldCoverSheetSupplier, () -> null, publicSetter, caseDocument -> {
        });
    }

    private void replaceAndStoreCoverSheet(CaseDocument coverSheet,
                                           YesOrNo hiddenFlag,
                                           String authToken,
                                           Supplier<CaseDocument> oldCoverSheetSupplier,
                                           Supplier<CaseDocument> oldCoverSheetConfidentialSupplier,
                                           Consumer<CaseDocument> publicSetter,
                                           Consumer<CaseDocument> confidentialSetter) {
        boolean isHiddenFromPublic = YesOrNo.isYes(hiddenFlag);
        Optional<CaseDocument> oldCoverSheet = Optional.ofNullable(isHiddenFromPublic
            ? oldCoverSheetConfidentialSupplier.get()
            : oldCoverSheetSupplier.get());

        oldCoverSheet.ifPresent(cs -> {
            log.info("Deleting old cover sheet with url: {}", cs.getDocumentUrl());
            deleteCoverSheet(cs.getDocumentUrl(), authToken);
        });

        publicSetter.accept(isHiddenFromPublic ? null : coverSheet);
        confidentialSetter.accept(isHiddenFromPublic ? coverSheet : null);
    }

    private void deleteCoverSheet(String coverSheetUrl, String authToken) {
        genericDocumentService.deleteDocument(coverSheetUrl, authToken);
    }

    private CaseDocument generateCoverSheet(FinremCaseDetails caseDetails,
                                            String authorisationToken,
                                            DocumentHelper.PaperNotificationRecipient recipient) {
        Map<String, Object> placeholdersMap = bulkPrintCoverLetterDetailsMapper.getLetterDetailsAsMap(
            caseDetails,
            recipient,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList()
        );

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            placeholdersMap,
            documentConfiguration.getBulkPrintTemplate(),
            documentConfiguration.getBulkPrintFileName(),
            caseDetails.getCaseType()
        );
    }

    private void logCoverSheetGeneration(DocumentHelper.PaperNotificationRecipient recipient, String caseId) {
        log.info("Generating {} Bulkprint cover sheet on Case ID: {}",
            recipient,
            caseId);
    }

    private void logCoverSheetGenerationAndStorage(DocumentHelper.PaperNotificationRecipient recipient, String caseId) {
        log.info("Generating and storing {} Bulkprint cover sheet on Case ID: {}",
            recipient,
            caseId);
    }
}
