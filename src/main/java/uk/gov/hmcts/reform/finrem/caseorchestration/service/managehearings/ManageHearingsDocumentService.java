package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.formg.FormGLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.ManageHearingFormCLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final HearingNoticeLetterDetailsMapper hearingNoticeLetterDetailsMapper;
    private final ManageHearingFormCLetterDetailsMapper manageHearingFormCLetterDetailsMapper;
    private final FormGLetterDetailsMapper formGLetterDetailsMapper;
    private final ExpressCaseService expressCaseService;

    /**
     * Generates a hearing notice document for the given hearing and case details.
     *
     * @param hearing the hearing information to include in the notice
     * @param finremCaseDetails the case details containing case data
     * @param authorisationToken the authorisation token for document generation
     * @return the generated hearing notice as a {@link CaseDocument}
     */
    public CaseDocument generateHearingNotice(Hearing hearing,
                                              FinremCaseDetails finremCaseDetails,
                                              String authorisationToken) {

        Map<String, Object>  documentDataMap = hearingNoticeLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
                hearing.getHearingCourtSelection());

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

    public CaseDocument generateFormC(Hearing hearing,
                                      FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        Map<String, Object>  documentDataMap = manageHearingFormCLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
                hearing.getHearingCourtSelection());

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

    public CaseDocument generateFormG(Hearing hearing,
                                      FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        Map<String, Object>  documentDataMap = formGLetterDetailsMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails,
                hearing.getHearingCourtSelection());

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
}
