package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.APPLICANTCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPONDENTCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CUCtCitizenApplicantDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CUCtCitizenRespondentDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenDocumentWrapper {

    @CCD(
            label = "Citizen Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CU_ct_citizenApplicantDocument",
            typeParameterClass = CUCtCitizenApplicantDocument.class,
            gate = "!CCD_DEF_ENV:prod",
            access = {APPLICANTCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<CitizenDocumentCollection> citizenApplicantDocument;
    @CCD(
            label = "Citizen Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CU_ct_citizenRespondentDocument",
            typeParameterClass = CUCtCitizenRespondentDocument.class,
            gate = "!CCD_DEF_ENV:prod",
            access = {RESPONDENTCrudAccess.class, CaseworkerDivorceFinancialremedyCourtadminCudAccess.class}
    )
    private List<CitizenDocumentCollection> citizenRespondentDocument;
}
