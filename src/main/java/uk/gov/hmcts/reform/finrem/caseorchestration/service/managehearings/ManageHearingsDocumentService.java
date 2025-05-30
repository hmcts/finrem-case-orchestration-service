package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StaticDocumentService;
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
    private final StaticDocumentService staticDocumentService;

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

    public CaseDocument generateFormC(FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        Map<String, Object>  documentDataMap = manageHearingFormCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails);

        String template = expressCaseService.isExpressCase(finremCaseDetails.getData()) ? documentConfiguration.getManageHearingExpressFromCTemplate() :
                (finremCaseDetails.getData().isFastTrackApplication() ?
                documentConfiguration.getFormCFastTrackTemplate(finremCaseDetails) :
                        documentConfiguration.getFormCStandardTemplate(finremCaseDetails));

        CaseDocument fromC = genericDocumentService.generateDocumentFromPlaceholdersMap(
                authorisationToken,
                documentDataMap,
                template,
                documentConfiguration.getFormCFileName(),
                finremCaseDetails.getId().toString()
        );

        fromC.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());

        return fromC;
    }

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

    public Map<String, CaseDocument> generatePfdNcdrDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId().toString();

        Map<String, CaseDocument> documentMap = new HashMap<>();
        documentMap.put(PFD_NCDR_COMPLIANCE_LETTER,
                staticDocumentService.uploadPfdNcdrComplianceLetter(caseId, authorisationToken));

        if (staticDocumentService.isPdfNcdrCoverSheetRequired(caseDetails)) {
            documentMap.put(PFD_NCDR_COVER_LETTER,
                    staticDocumentService.uploadPfdNcdrCoverLetter(caseId, authorisationToken));
        }

        return documentMap;
    }

    public CaseDocument generateOutOfCourtResolutionDoc(FinremCaseDetails caseDetails, String authToken) {
        return staticDocumentService.uploadOutOfCourtResolutionDocument(caseDetails.getId().toString(), authToken);
    }
}
