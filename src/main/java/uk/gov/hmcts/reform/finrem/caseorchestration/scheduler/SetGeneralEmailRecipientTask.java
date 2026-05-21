package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class SetGeneralEmailRecipientTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "SetGeneralEmailRecipientTask";
    private static final String SUMMARY = "DFR-5061 CT Fix generalEmailRecipient";

    @Value("${cron.generalEmailRecipient.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.generalApplicgeneralEmailRecipientationReferToJudgeEmail.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.generalEmailRecipient.caseListFileName:updateGeneralEmailRecipient-encrypted.csv}")
    private String csvFile;

    public SetGeneralEmailRecipientTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                        SystemUserService systemUserService,
                                        FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        GeneralEmailWrapper generalEmailWrapper = caseData.getGeneralEmailWrapper();

        String currentEmail = generalEmailWrapper.getGeneralEmailRecipient();
        if (hasLeadingDot(currentEmail)) {
            String updatedEmail = removeLeadingDot(currentEmail);
            generalEmailWrapper.setGeneralEmailRecipient(updatedEmail);

            log.info("Updated generalEmailRecipient for case id {} from [{}] to [{}]",
                finremCaseDetails.getId(), currentEmail, updatedEmail);
        }

        List<GeneralEmailCollection> currentEmailInCollection =
            generalEmailWrapper.getGeneralEmailCollection();

        if (currentEmailInCollection == null) {
            return;
        }

        currentEmailInCollection.stream()
            .filter(Objects::nonNull)
            .map(GeneralEmailCollection::getValue)
            .filter(Objects::nonNull)
            .forEach(generalEmail -> {
                String recipient = generalEmail.getGeneralEmailRecipient();

                if (!hasLeadingDot(recipient)) {
                    return;
                }

                String updatedRecipient = removeLeadingDot(recipient);
                generalEmail.setGeneralEmailRecipient(updatedRecipient);

                log.info("Updated collection generalEmailRecipient for case id {}",
                    finremCaseDetails.getId());
            });
    }

    private boolean hasLeadingDot(String email) {
        return email != null && email.startsWith(".");
    }

    private String removeLeadingDot(String email) {
        return email.substring(1);
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }
}
