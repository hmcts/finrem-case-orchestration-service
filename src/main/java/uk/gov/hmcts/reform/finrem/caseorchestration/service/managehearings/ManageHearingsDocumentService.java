package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.VacateOrAdjournNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;

@Service
@RequiredArgsConstructor
@Slf4j
// Service for managing hearing-related document generation.
public class ManageHearingsDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final HearingNoticeLetterDetailsMapper hearingNoticeLetterDetailsMapper;
    private final VacateOrAdjournNoticeLetterDetailsMapper vacateOrAdjournNoticeLetterDetailsMapper;
    private final ManageHearingFormCLetterDetailsMapper manageHearingFormCLetterDetailsMapper;
    private final ManageHearingFormGLetterDetailsMapper formGLetterDetailsMapper;
    private final ExpressCaseService expressCaseService;
    private final StaticHearingDocumentService staticHearingDocumentService;

    /**
     * Generates a hearing notice document for the given hearing and case details.
     *
     * @param finremCaseDetails  the case details containing case data
     * @param authorisationToken the authorisation token for document generation
     * @return the generated hearing notice as a {@link CaseDocument}
     */
    public CaseDocument generateHearingNotice(FinremCaseDetails finremCaseDetails,
                                              Region courtRegion,
                                              String authorisationToken) {

        Map<String, Object> documentDataMap = hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            documentConfiguration.getManageHearingNoticeTemplate(courtRegion),
            documentConfiguration.getManageHearingNoticeFileName(),
            finremCaseDetails.getCaseType()
        );
    }

    /**
     * Generates a notice document to say that a hearing is being vacated.
     * Includes the given hearing and case details.
     *
     * @param finremCaseDetails  the case details containing case data
     * @param authorisationToken the authorisation token for document generation
     * @return the generated vacate hearing notice as a {@link CaseDocument}
     */
    public CaseDocument generateVacateOrAdjournNotice(FinremCaseDetails finremCaseDetails,
                                                      Region courtRegion,
                                                      String authorisationToken,
                                                      VacateOrAdjournAction vacateOrAdjournAction) {

        Map<String, Object> documentDataMap = vacateOrAdjournNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        String template = documentConfiguration.getVacateOrAdjournNoticeTemplate(courtRegion);

        String fileName =  VacateOrAdjournAction.VACATE_HEARING.equals(vacateOrAdjournAction)
            ? documentConfiguration.getVacateHearingNoticeFileName()
            : documentConfiguration.getAdjournHearingNoticeFileName();

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            template,
            fileName,
            finremCaseDetails.getCaseType()
        );
    }

    /**
     * Generates appropriate Form C document based on the case type (standard/fast track/express).
     *
     * @param finremCaseDetails  the case details
     * @param authorisationToken the token for document generation
     * @return the generated Form C document as a {@link CaseDocument}
     */
    public CaseDocument generateFormC(FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        Map<String, Object> documentDataMap = manageHearingFormCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            determineFormCTemplate(finremCaseDetails).getRight(),
            documentConfiguration.getFormCFileName(),
            finremCaseDetails.getCaseType()
        );
    }

    /**
     * Generates Form G document.
     *
     * @param finremCaseDetails  the case details
     * @param authorisationToken the token for document generation
     * @return the generated Form G document as a {@link CaseDocument}
     */
    public CaseDocument generateFormG(FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        Map<String, Object> documentDataMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            documentConfiguration.getFormGTemplate(finremCaseDetails),
            documentConfiguration.getFormGFileName(),
            finremCaseDetails.getCaseType()
        );
    }

    /**
     * Generates PFD NCDR documents including compliance letter and cover letter if required.
     *
     * @param caseDetails        the case details
     * @param authorisationToken the authorisation token for document generation
     * @return a map containing the generated PFD NCDR documents
     */
    public Map<String, CaseDocument> generatePfdNcdrDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        Map<String, CaseDocument> documentMap = new HashMap<>();

        documentMap.put(
            PFD_NCDR_COMPLIANCE_LETTER,
            staticHearingDocumentService.uploadPfdNcdrComplianceLetter(caseDetails.getCaseType(), authorisationToken)
        );

        if (staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)) {
            documentMap.put(
                PFD_NCDR_COVER_LETTER,
                staticHearingDocumentService.uploadPfdNcdrCoverLetter(caseDetails.getCaseType(), authorisationToken)
            );
        }

        return documentMap;
    }

    /**
     * Generates an Out-of-Court Resolution document.
     *
     * @param caseDetails the case details containing case data
     * @param authToken   the authorization token for document generation
     * @return the generated Out Of Court Resolution document as a {@link CaseDocument}
     */
    public CaseDocument generateOutOfCourtResolutionDoc(FinremCaseDetails caseDetails, String authToken) {
        CaseDocument outOfCourtDoc = staticHearingDocumentService.uploadOutOfCourtResolutionDocument(caseDetails.getCaseType(), authToken);
        outOfCourtDoc.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());
        return outOfCourtDoc;
    }

    /**
     * Categorizes system duplicate documents by setting their category ID to SYSTEM_DUPLICATES.
     * This method processes both hearings and hearing documents collections, ensuring that
     * any additional hearing documents or hearing document values are appropriately marked.
     *
     * @param hearings         the list of hearing collection items to process
     * @param hearingDocuments the list of hearing document collection items to process
     */
    public void categoriseSystemDuplicateDocs(List<ManageHearingsCollectionItem> hearings,
                                              List<ManageHearingDocumentsCollectionItem> hearingDocuments) {
        // !!! Changes to this method may affect the manage hearing !!!
        // see ManageHearingsMigrationTask.classesToOverrideJsonInclude.
        if (hearings != null) {
            hearings.stream()
                .map(ManageHearingsCollectionItem::getValue)
                .filter(hearing -> hearing.getAdditionalHearingDocs() != null)
                .flatMap(hearing -> hearing.getAdditionalHearingDocs().stream())
                .forEach(document -> document.getValue().setCategoryId(SYSTEM_DUPLICATES.getDocumentCategoryId()));
        }

        if (hearingDocuments != null) {
            hearingDocuments.stream()
                .map(ManageHearingDocumentsCollectionItem::getValue)
                .filter(value -> value != null && value.getHearingDocument() != null)
                .forEach(value -> value.getHearingDocument().setCategoryId(SYSTEM_DUPLICATES.getDocumentCategoryId()));
        }
    }

    /**
     * Determines the appropriate Form C template to use for a given case and
     * returns it along with the corresponding {@link CaseDocumentType}.
     *
     * @param caseDetails the details of the case
     * @return a pair containing the {@link CaseDocumentType} and the string for the template
     */
    public Pair<CaseDocumentType, String> determineFormCTemplate(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        if (expressCaseService.isExpressCase(caseData)) {
            return Pair.of(
                CaseDocumentType.FORM_C_EXPRESS,
                documentConfiguration.getManageHearingExpressFormCTemplate()
            );
        } else if (caseData.isFastTrackApplication()) {
            return Pair.of(
                CaseDocumentType.FORM_C_FAST_TRACK,
                documentConfiguration.getFormCFastTrackTemplate(caseDetails)
            );
        } else {
            return Pair.of(
                CaseDocumentType.FORM_C,
                documentConfiguration.getFormCStandardTemplate(caseDetails)
            );
        }
    }

}
