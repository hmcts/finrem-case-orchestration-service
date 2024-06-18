package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public record Coversheet(CaseDetails caseDetails,
                         String partyAddressCcdFieldName,
                         String solicitorAddressCcdFieldName,
                         String solicitorNameCcdFieldName,
                         String partyFirstMiddleNameCcdFieldName,
                         String partyLastNameCcdFieldName,
                         boolean isRepresentedByASolicitor,
                         boolean isInternational) {
}