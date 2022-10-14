package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generator.BaseContestedLetterDetailsGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Component
@Slf4j
public class BarristerLetterDetailsGenerator extends BaseContestedLetterDetailsGenerator {

    private final PrdOrganisationService prdOrganisationService;

    public BarristerLetterDetailsGenerator(CaseDataService caseDataService,
                                           DocumentHelper documentHelper,
                                           PrdOrganisationService prdOrganisationService) {
        super(caseDataService, documentHelper);
        this.prdOrganisationService = prdOrganisationService;
    }

    @Override
    public BarristerLetterDetails generate(CaseDetails caseDetails,
                                        DocumentHelper.PaperNotificationRecipient recipient) {
        return BarristerLetterDetails.builder()
            .courtDetails(buildFrcCourtDetails(caseDetails.getData()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .addressee(getAddressee(caseDetails, recipient))
            .caseNumber(caseDetails.getId().toString())
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .build();
    }

    public void setBarristerFields(Barrister barrister,
                                   BarristerLetterDetails barristerLetterDetails) {
        String barristerOrgId = barrister.getOrganisation().getOrganisationID();
        barristerLetterDetails.setReference(barristerOrgId);
        String barristerFirmName = prdOrganisationService.findOrganisationByOrgId(barristerOrgId).getName();
        barristerLetterDetails.setBarristerFirmName(barristerFirmName);
    }
}
