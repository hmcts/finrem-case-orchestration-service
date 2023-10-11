package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
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
public class BarristerLetterDetailsGenerator extends BaseContestedLetterDetailsGenerator<Barrister> {

    private final PrdOrganisationService prdOrganisationService;

    public BarristerLetterDetailsGenerator(CaseDataService caseDataService,
                                           DocumentHelper documentHelper,
                                           LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper,
                                           PrdOrganisationService prdOrganisationService) {
        super(caseDataService, documentHelper, letterAddresseeGeneratorMapper);
        this.prdOrganisationService = prdOrganisationService;
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public BarristerLetterDetails generate(CaseDetails caseDetails,
                                           DocumentHelper.PaperNotificationRecipient recipient,
                                           Barrister barrister) {

        String barristerOrgId = barrister.getOrganisation().getOrganisationID();
        String barristerFirmName = prdOrganisationService.findOrganisationByOrgId(barristerOrgId).getName();

        return BarristerLetterDetails.builder()
            .courtDetails(buildFrcCourtDetails(caseDetails.getData()))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER), StringUtils.EMPTY))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .addressee(getAddressee(caseDetails, recipient))
            .caseNumber(caseDetails.getId().toString())
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .barristerFirmName(barristerFirmName)
            .reference(barristerOrgId)
            .build();
    }
}
