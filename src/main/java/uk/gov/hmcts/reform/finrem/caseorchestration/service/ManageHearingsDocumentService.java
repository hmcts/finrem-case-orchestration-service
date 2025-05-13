package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildHearingFrcCourtDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageHearingsDocumentService {

    private final DocumentHelper documentHelper;
    private final CourtDetailsConfiguration courtDetailsConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final DocumentConfiguration documentConfiguration;

    public CaseDocument generateHearingNotice(Hearing hearing,
                                              FinremCaseDetails caseDetails,
                                              String authorisationToken) {

        CaseDetails caseDetailsCopy = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        FinremCaseData caseData = caseDetails.getData();
        Map<String, Object> documentDataMap = caseDetailsCopy.getData();


        //TODO: Pass correct details into hearing document generation

        documentDataMap.put("ccdCaseNumber", caseData.getCcdCaseId());
        documentDataMap.put("applicantName", caseData.getFullApplicantName());
        documentDataMap.put("respondentName", caseData.getFullRespondentNameContested());
        documentDataMap.put("letterDate", String.valueOf(LocalDate.now()));
        documentDataMap.put("interimHearingType", hearing.getHearingType());
        documentDataMap.put("interimHearingDate", hearing.getHearingDate());
        documentDataMap.put("interimHearingTime", hearing.getHearingTime());
        documentDataMap.put("interimTimeEstimate", hearing.getHearingTimeEstimate());
        documentDataMap.put("interimAdditionalInformationAboutHearing",
            hearing.getAdditionalHearingInformation());

        documentDataMap.put("courtDetails", buildHearingFrcCourtDetails(caseData));
        documentDataMap.put("hearingVenue", courtDetailsConfiguration.getCourts().get(caseData.getSelectedHearingCourt()).getCourtAddress());

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(caseDetailsCopy),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());
    }
}
