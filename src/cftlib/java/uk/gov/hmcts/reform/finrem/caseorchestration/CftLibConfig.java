package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftLibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        createCcdRoles(lib);
        createIdamUsers(lib);
        importDefinitions(lib);
    }

    private void importDefinitions(CFTLib lib) throws IOException {
        var consentedDef = Files.readAllBytes(Path.of("build/definitionsToBeImported/ccd-config-local-consented-base.xlsx"));
        lib.importDefinition(consentedDef);

        var contestedDef = Files.readAllBytes(Path.of("build/definitionsToBeImported/ccd-config-local-contested-base.xlsx"));
        lib.importDefinition(contestedDef);
    }

    private void createCcdRoles(CFTLib lib) {
        lib.createRoles(
            "citizen",
            "caseworker",
            "caseworker-approver",
            "caseworker-divorce-financialremedy-courtadmin",
            "caseworker-divorce-financialremedy-solicitor",
            "caseworker-divorce-financialremedy-judiciary",
            "caseworker-divorce-systemupdate",
            "caseworker-divorce-bulkscan",
            "caseworker-divorce-financialremedy",
            "caseworker-caa",
            "IDAM_SUPER_USER"
        );
    }

    private void createIdamUsers(CFTLib lib) {
        lib.createIdamUser("fr_applicant_solicitor@mailinator.com", "caseworker", "caseworker-divorce-financialremedy-solicitor");
        lib.createIdamUser("fr_applicant_solicitor2@mailinator.com", "caseworker", "caseworker-divorce-financialremedy-solicitor");
        lib.createIdamUser("fr_respondent_solicitor@mailinator.com", "caseworker", "caseworker-divorce-financialremedy-solicitor");
        lib.createIdamUser("fr_respondent_solicitor2@mailinator.com", "caseworker", "caseworker-divorce-financialremedy-solicitor");
        lib.createIdamUser("fr_judge@mailinator.com", "caseworker", "caseworker-divorce-financialremedy-judiciary");

        lib.createIdamUser("fr_courtadmin@mailinator.com", "caseworker", "caseworker-divorce",
            "caseworker-divorce-bulkscan", "caseworker-divorce-financialremedy",
            "caseworker-divorce-financialremedy-courtadmin");

        lib.createIdamUser("fr_citizen@mailinator.com", "citizen");
    }
}
