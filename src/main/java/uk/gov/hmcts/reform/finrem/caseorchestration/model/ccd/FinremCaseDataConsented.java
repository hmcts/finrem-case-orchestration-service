package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentedContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinremCaseDataConsented extends FinremCaseData {

    private Provision provisionMadeFor;
    private Intention applicantIntendsTo;
    private List<PeriodicalPaymentSubstitute> dischargePeriodicalPaymentSubstituteFor;
    private YesOrNo applyingForConsentOrder;
    @JsonProperty("ChildSupportAgencyCalculationMade")
    private YesOrNo childSupportAgencyCalculationMade;
    @JsonProperty("ChildSupportAgencyCalculationReason")
    private String childSupportAgencyCalculationReason;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private NatureApplicationWrapper natureApplicationWrapper;
    private String authorisationFirm;
    private CaseDocument consentOrderText;
    private CaseDocument latestConsentOrder;
    private YesOrNo d81Question;
    private CaseDocument d81Joint;
    private CaseDocument d81Applicant;
    private CaseDocument d81Respondent;
    private List<PensionTypeCollection> pensionCollection;
    @JsonProperty("otherCollection")
    private List<OtherDocumentCollection> otherDocumentsCollection;
    private OrderDirection orderDirection;
    private CaseDocument orderDirectionOpt1;
    private String orderDirectionOpt2;
    private YesOrNo orderDirectionAbsolute;
    private JudgeType orderDirectionJudge;
    private String orderDirectionJudgeName;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDirectionDate;
    private String orderDirectionAddComments;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private AssignToJudgeReason assignedToJudgeReason;
    private List<UploadConsentOrderDocumentCollection> uploadConsentOrderDocuments;
    private List<UploadDocumentCollection> uploadDocuments;
    private List<SolUploadDocumentCollection> solUploadDocuments;
    private List<RespondToOrderDocumentCollection> respondToOrderDocuments;
    private List<AmendedConsentOrderCollection> amendedConsentOrderCollection;
    private CaseDocument approvedConsentOrderLetter;
    private List<ConsentOrderCollection> approvedOrderCollection;
    private ApplicantRole divRoleOfFrApplicant;
    private ApplicantRepresentedPaper applicantRepresentedPaper;
    private String authorisationSolicitorAddress;
    private YesOrNo authorisationSigned;
    private AuthorisationSignedBy authorisationSignedBy;
    private List<ChildrenInfoCollection> childrenInfo;
    private CaseDocument formA;
    private List<DocumentCollection> scannedD81s;
    private String transferLocalCourtName;
    private String transferLocalCourtEmail;
    private String transferLocalCourtInstructions;
    private List<TransferCourtEmailCollection> transferLocalCourtEmailCollection;
    private List<ConsentedHearingDataWrapper> listForHearings;
    private String consentVariationOrderLabelC;
    private String consentVariationOrderLabelL;
    private String otherDocLabel;
    private List<VariationDocumentTypeCollection> otherVariationCollection;
    private CaseDocument uploadApprovedConsentOrder;
    private List<GeneralOrderCollectionItem> generalOrderCollection;

    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private ConsentedContactDetailsWrapper contactDetailsWrapper;

    @JsonIgnore
    public ConsentedContactDetailsWrapper getContactDetailsWrapper() {
        if (contactDetailsWrapper == null) {
            this.contactDetailsWrapper = new ConsentedContactDetailsWrapper();
        }
        return contactDetailsWrapper;
    }

    @JsonIgnore
    public String getAppSolicitorName() {
        return getContactDetailsWrapper().getSolicitorName();
    }

    @JsonIgnore
    public Address getAppSolicitorAddress() {
        return getContactDetailsWrapper().getSolicitorAddress();
    }

    @JsonIgnore
    public String getAppSolicitorEmail() {
        return getContactDetailsWrapper().getSolicitorEmail();
    }

    @JsonIgnore
    public boolean isRespondentRepresentedByASolicitor() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getConsentedRespondentRepresented());
    }

    @JsonIgnore
    public boolean isApplicantSolicitorAgreeToReceiveEmails() {
        return YesOrNo.YES.equals(getContactDetailsWrapper().getSolicitorAgreeToReceiveEmails());
    }


    @JsonIgnore
    public String getRespondentFullName() {
        return (
            nullToEmpty(getContactDetailsWrapper().getAppRespondentFmName()).trim()
                + " "
                + nullToEmpty(getContactDetailsWrapper().getAppRespondentLName()).trim()
        ).trim();
    }

    @JsonIgnore
    public NatureApplicationWrapper getNatureApplicationWrapper() {
        if (natureApplicationWrapper == null) {
            this.natureApplicationWrapper = new NatureApplicationWrapper();
        }

        return natureApplicationWrapper;
    }

    @JsonIgnore
    public boolean isConsentedInContestedCase() {
        return false;
    }

}

