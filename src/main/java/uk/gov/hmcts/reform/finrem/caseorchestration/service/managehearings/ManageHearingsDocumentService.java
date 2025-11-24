package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final HearingNoticeLetterDetailsMapper hearingNoticeLetterDetailsMapper;
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
                                              String authorisationToken) {

        Map<String, Object> documentDataMap = hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            documentConfiguration.getManageHearingNoticeTemplate(finremCaseDetails),
            documentConfiguration.getManageHearingNoticeFileName(),
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
     * Get the hearing notice document, or return null if not found.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a {@link CaseDocument}
     */
    public CaseDocument getHearingNotice(FinremCaseDetails finremCaseDetails) {
        return getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.HEARING_NOTICE);
    }

    /**
     * Retrieves the additional hearing documents from the working hearing in the provided wrapper.
     *
     * <p>
     * This method navigates through the `ManageHearingsWrapper` to locate the working hearing
     * using its ID, extracts the additional hearing documents, and returns them as a list of
     * {@link CaseDocument} objects. If the wrapper or any intermediate data is null, an empty list is returned.
     * </p>
     *
     * @param wrapper the {@link ManageHearingsWrapper} containing hearing data
     * @return a list of {@link CaseDocument} representing the additional hearing documents
     */
    public List<CaseDocument> getAdditionalHearingDocsFromWorkingHearing(ManageHearingsWrapper wrapper) {
        return Optional.of(wrapper)
            .map(w -> w.getManageHearingsCollectionItemById(w.getWorkingHearingId()))
            .map(ManageHearingsCollectionItem::getValue)
            .map(Hearing::getAdditionalHearingDocs)
            .stream()
            .flatMap(List::stream)
            .map(DocumentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Retrieves all hearing documents that need to be posted for the current working hearing.
     * Note: FDR Hearings should only reach this point for Express cases.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a {@link CaseDocument}
     */
    public List<CaseDocument> getHearingDocumentsToPost(FinremCaseDetails finremCaseDetails) {
        HearingType workingHearingType = getLastWorkingHearingType(finremCaseDetails);

        ArrayList<CaseDocument> hearingDocumentsToPost = new ArrayList<>();

        if (HearingType.FDA.equals(workingHearingType)) {
            hearingDocumentsToPost.addAll(getFdaHearingDocumentsToPost(finremCaseDetails));
        }

        if (HearingType.FDR.equals(workingHearingType)) {
            hearingDocumentsToPost.addAll(getFdrExpressHearingDocumentsToPost(finremCaseDetails));
        }

        // These hearing documents are always needed, so add to the list
        hearingDocumentsToPost.addAll(getHearingDocumentsThatAreAlwaysPosted(finremCaseDetails));

        return hearingDocumentsToPost;
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

    /**
     * Retrieves the case's current working hearing.
     * Then uses that to get the most recent hearing document with the passed CaseDocumentType argument.
     * If no notice is found, returns an empty list.
     *
     * @param finremCaseDetails the case details containing the hearing documents.
     * @param documentType      a {@link CaseDocumentType} identifying the type of hearing document.
     * @return a {@link CaseDocument}
     */
    private CaseDocument getByWorkingHearingAndDocumentType(FinremCaseDetails finremCaseDetails,
                                                            CaseDocumentType documentType) {
        ManageHearingsWrapper wrapper = finremCaseDetails.getData().getManageHearingsWrapper();
        UUID hearingId = wrapper.getWorkingHearingId();

        return wrapper.getHearingDocumentsCollection().stream()
            .map(ManageHearingDocumentsCollectionItem::getValue)
            .filter(Objects::nonNull)
            .filter(doc -> Objects.equals(hearingId, doc.getHearingId()))
            .filter(doc -> Objects.equals(documentType, doc.getHearingCaseDocumentType()))
            .map(ManageHearingDocument::getHearingDocument)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(CaseDocument::getUploadTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(null);
    }

    /**
     * Hearings have a core set of documents that need to be posted.
     * These are the documents that are always posted.
     * <ul>
     *     <li>Hearing Notice</li>
     *     <li>Form A</li>
     *     <li>Out of Court Resolution</li>
     *     <li>PDF NCDR Compliance Letter</li>
     *     <li>PDF NCDR Cover Letter</li>
     * </ul>
     * Removes non-null objects from the list, so exceptions are not thrown when documents are missing.
     * The Form A doesn't exist in the hearing documents collection, so it is added separately.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a list of {@link CaseDocument} that are posted for all cases
     */
    private List<CaseDocument> getHearingDocumentsThatAreAlwaysPosted(FinremCaseDetails finremCaseDetails) {
        return Stream.of(
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.HEARING_NOTICE),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.OUT_OF_COURT_RESOLUTION),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER),
                getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.PFD_NCDR_COVER_LETTER),
                finremCaseDetails.getData().getMiniFormA()
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Gets an Express Form C and Form G CaseDocument.
     * Filters out non-null case documents from the list, so exceptions are not thrown when documents are missing.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a list of {@link CaseDocument} that are posted for FDR Express cases
     */
    private List<CaseDocument> getFdrExpressHearingDocumentsToPost(FinremCaseDetails finremCaseDetails) {
        CaseDocument formC = getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C_EXPRESS);
        CaseDocument formG = getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_G);

        return Stream.of(formC, formG)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Posted documents for FDA Hearings depend on whether the case is fast track or not.
     * Fast track cases only post the fast track Form C.
     * Standard FDA cases post both Form C and Form G.
     * Filters out non-null case documents from the list, so exceptions are not thrown when documents are missing.
     *
     * @param finremCaseDetails the case details containing the hearing documents
     * @return a list of {@link CaseDocument} that are posted for FDA cases
     */
    private List<CaseDocument> getFdaHearingDocumentsToPost(FinremCaseDetails finremCaseDetails) {
        CaseDocument formC = finremCaseDetails.getData().isFastTrackApplication()
            ? getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C_FAST_TRACK)
            : getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C);

        CaseDocument formG = finremCaseDetails.getData().isFastTrackApplication()
            ? null
            : getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_G);

        return Stream.of(formC, formG)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves hearing type for the latest hearing to be the working hearing.
     * The working hearing is nullified in about to submit handler, but working hearing ID is retained.
     *
     * @param finremCaseDetails the case details containing the hearing information
     * @return the type of working hearing
     */
    private HearingType getLastWorkingHearingType(FinremCaseDetails finremCaseDetails) {
        return ofNullable(finremCaseDetails)
            .map(FinremCaseDetails::getData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(wrapper ->
                wrapper.getManageHearingsCollectionItemById(wrapper.getWorkingHearingId()))
            .map(ManageHearingsCollectionItem::getValue)
            .map(Hearing::getHearingType)
            .orElse(null);
    }
}
