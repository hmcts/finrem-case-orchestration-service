package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.rdprofessional;

import au.com.dius.pact.consumer.dsl.DslPart;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

class ReferenceDataDsl {
    static DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.stringType("name", "some organisation name")
                .stringType("organisationIdentifier", "some organisation id")
                .minArrayLike("contactInformation", 1, 1,
                    sh -> {
                        sh.stringType("addressLine1", "address line 1")
                            .stringType("addressLine2", "address line 2")
                            // Properties commented out as the pact provider does not support them
                            // They would need updating here
                            // https://github.com/hmcts/rd-professional-api/blob/master/src/contractTest/java/uk/gov/
                            // hmcts/reform/professionalapi/provider/OrganisationalInternalControllerProviderTest.java
                            //   .stringType("addressLine3", "address line 3")
                            //   .stringType("townCity", "town or city")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX");
                    });
        }).build();
    }
}
