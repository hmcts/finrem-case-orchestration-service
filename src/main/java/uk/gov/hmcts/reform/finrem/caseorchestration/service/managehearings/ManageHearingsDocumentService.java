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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COMPLIANCE_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PFD_NCDR_COVER_LETTER;

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

        Map<String, Object>  documentDataMap = hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        CaseDocument hearingDoc = genericDocumentService.generateDocumentFromPlaceholdersMap(
                authorisationToken,
                documentDataMap,
                documentConfiguration.getManageHearingNoticeTemplate(finremCaseDetails),
                documentConfiguration.getManageHearingNoticeFileName(),
                finremCaseDetails.getId().toString()
        );

        hearingDoc.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());

        return hearingDoc;
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

        Map<String, Object>  documentDataMap = manageHearingFormCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        CaseDocument formC = genericDocumentService.generateDocumentFromPlaceholdersMap(
                authorisationToken,
                documentDataMap,
                determineFormCTemplate(finremCaseDetails),
                documentConfiguration.getFormCFileName(),
                finremCaseDetails.getId().toString()
        );

        formC.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());

        return formC;
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

        Map<String, Object>  documentDataMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        CaseDocument formG = genericDocumentService.generateDocumentFromPlaceholdersMap(
                authorisationToken,
                documentDataMap,
                documentConfiguration.getFormGTemplate(finremCaseDetails),
                documentConfiguration.getFormGFileName(),
                finremCaseDetails.getId().toString()
        );

        formG.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());

        return formG;
    }

    /**
     * Generates PFD NCDR documents including compliance letter and cover letter if required.
     *
     * @param caseDetails        the case details
     * @param authorisationToken the authorisation token for document generation
     * @return a map containing the generated PFD NCDR documents
     */
    public Map<String, Pair<CaseDocument, CaseDocumentType>> generatePfdNcdrDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId().toString();

        Map<String, Pair<CaseDocument, CaseDocumentType>> documentMap = new HashMap<>();

        documentMap.put(
                PFD_NCDR_COMPLIANCE_LETTER,
                Pair.of(
                        staticHearingDocumentService.uploadPfdNcdrComplianceLetter(caseId, authorisationToken),
                        CaseDocumentType.PFD_NCDR_COMPLIANCE_LETTER
                )
        );

        if (staticHearingDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)) {
            documentMap.put(
                    PFD_NCDR_COVER_LETTER,
                    Pair.of(
                            staticHearingDocumentService.uploadPfdNcdrCoverLetter(caseId, authorisationToken),
                            CaseDocumentType.PFD_NCDR_COVER_LETTER
                    )
            );
        }

        return documentMap;
    }

    /**
     * Generates an Out-of-Court Resolution document.
     *
     * @param caseDetails  the case details containing case data
     * @param authToken    the authorization token for document generation
     * @return the generated Out Of Court Resolution document as a {@link CaseDocument}
     */
    public CaseDocument generateOutOfCourtResolutionDoc(FinremCaseDetails caseDetails, String authToken) {
        return staticHearingDocumentService.uploadOutOfCourtResolutionDocument(caseDetails.getId().toString(), authToken);
    }

    private String determineFormCTemplate(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        if (expressCaseService.isExpressCase(caseData)) {
            return documentConfiguration.getManageHearingExpressFormCTemplate();
        } else if (caseData.isFastTrackApplication()) {
            return documentConfiguration.getFormCFastTrackTemplate(caseDetails);
        } else {
            return documentConfiguration.getFormCStandardTemplate(caseDetails);
        }
    }
}
