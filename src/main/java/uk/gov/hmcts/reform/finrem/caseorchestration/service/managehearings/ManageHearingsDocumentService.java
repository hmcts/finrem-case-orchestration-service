package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings.HearingNoticeMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final HearingNoticeMapper hearingNoticeMapper;

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

        Map<String, Object>  documentDataMap = hearingNoticeMapper.getDocumentTemplateDetailsAsMap(finremCaseDetails, hearing.getHearingCourtSelection());

        CaseDocument hearingDoc = genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, documentDataMap,
                documentConfiguration.getManageHearingNoticeTemplate(),
                documentConfiguration.getManageHearingNoticeFileName(),
                finremCaseDetails.getId().toString());

        hearingDoc.setCategoryId(DocumentCategory.HEARING_DOCUMENTS.getDocumentCategoryId());

        return hearingDoc;
    }

    public CaseDocument generateFormC(Hearing hearing,
                                      FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        return CaseDocument.builder().build();
    }

    public CaseDocument generateFormG(Hearing hearing,
                                      FinremCaseDetails finremCaseDetails,
                                      String authorisationToken) {

        return CaseDocument.builder().build();
    }
}
