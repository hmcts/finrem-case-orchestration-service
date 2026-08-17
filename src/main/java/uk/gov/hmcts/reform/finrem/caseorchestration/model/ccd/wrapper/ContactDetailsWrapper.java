package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCaseworkerCaaCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceSystemupdateCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerApproverCrudCaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyJudiciaryCrAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERRESPSOLICITORCudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.RESPBARRISTERCudRESPSOLICITORCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactDetailsWrapper {
    @CCD(
            label = "Does this update include a change in representation for either party?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private YesOrNo updateIncludesRepresentativeChange;
    @CCD(
            label = "Select Party to which the change in representation applies: ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus2RolesSfvgopAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerDivorceSystemupdateCrudAccess.class}
    )
    private NoticeOfChangeParty nocParty;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerApproverCrudAccess.class}
    )
    private YesOrNo applicantRepresented;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private Address applicantSolicitorAddress;
    @CCD(
            label = "Solicitor’s name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private String applicantSolicitorName;
    @CCD(
            label = "Solicitor’s firm",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private String applicantSolicitorFirm;
    @CCD(
            label = "Your reference number",
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private String solicitorReference;
    @CCD(
            label = "Phone Number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private String applicantSolicitorPhone;
    @CCD(
            label = "Email",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private String applicantSolicitorEmail;
    @CCD(label = "DX number", searchable = false, access = {DefaultAccess.class, CaseworkerApproverCrudAccess.class})
    @JsonProperty("applicantSolicitorDXnumber")
    private String applicantSolicitorDxNumber;
    @CCD(
            label = "Do you consent to receive emails from the court about your case ?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    private YesOrNo applicantSolicitorConsentForEmails;
    @CCD(label = "Current First and Middle names", access = {DefaultAccess.class, CaseworkerCaaCudAccess.class})
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    @CCD(label = "Current Last Name", access = {DefaultAccess.class, CaseworkerCaaCudAccess.class})
    @JsonProperty("applicantLName")
    private String applicantLname;
    @CCD(label = "          ", searchable = false, typeOverride = FieldType.AddressUK, access = {DefaultAccess.class})
    private Address applicantAddress;
    @CCD(
            label = "Does the applicant live outside of the UK?",
            hint = "If Yes, please enter the Country in the field below.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo applicantResideOutsideUK;
    @CCD(label = "Phone Number", searchable = false, access = {DefaultAccess.class})
    private String applicantPhone;
    @CCD(label = "Email", searchable = false, typeOverride = FieldType.Email, access = {DefaultAccess.class})
    private String applicantEmail;
    @CCD(
            label = "Keep the Applicant's contact details private from the Respondent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class, CaseworkerDivorceFinancialremedyJudiciaryCrAccess.class}
    )
    @JsonProperty("applicantAddressConfidential")
    private YesOrNo applicantAddressHiddenFromRespondent;
    @CCD(
            label = "Current First and Middle names",
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerCaaCudAccess.class}
    )
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    @CCD(
            label = "Current Last Name",
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerCaaCudAccess.class}
    )
    @JsonProperty("respondentLName")
    private String respondentLname;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerApproverCrudAccess.class}
    )
    @JsonProperty("respondentRepresented")
    private YesOrNo contestedRespondentRepresented;
    @CCD(
            label = "Solicitor’s name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class, RESPBARRISTERCudRESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("rSolicitorName")
    private String respondentSolicitorName;
    @CCD(
            label = "Solicitor’s firm",
            searchable = false,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class}
    )
    @JsonProperty("rSolicitorFirm")
    private String respondentSolicitorFirm;
    @CCD(
            label = "Respondent solicitor’s reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class, RESPBARRISTERCudRESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class, RESPBARRISTERCudRESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("rSolicitorAddress")
    private Address respondentSolicitorAddress;
    @CCD(
            label = "Phone Number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class, RESPBARRISTERCudRESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("rSolicitorPhone")
    private String respondentSolicitorPhone;
    @CCD(
            label = "Email",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {DefaultAccess.class, CaseworkerApproverCrudCaseworkerCaaCudAccess.class, RESPBARRISTERCudRESPSOLICITORCrudAccess.class}
    )
    @JsonProperty("rSolicitorEmail")
    private String respondentSolicitorEmail;
    @CCD(
            label = "DX number",
            searchable = false,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class, CaseworkerApproverCrudAccess.class}
    )
    @JsonProperty("rSolicitorDXnumber")
    private String respondentSolicitorDxNumber;
    @CCD(
            label = "          ",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private Address respondentAddress;
    @CCD(
            label = "Does the respondent live outside of the UK?",
            hint = "If yes, please enter the Country in the field below.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    private YesOrNo respondentResideOutsideUK;
    @CCD(
            label = "Phone Number",
            searchable = false,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private String respondentPhone;
    @CCD(
            label = "Email",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    private String respondentEmail;
    @CCD(
            label = "Keep the Respondent's contact details private from the Applicant?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESPBARRISTERRESPSOLICITORCudAccess.class}
    )
    @JsonProperty("respondentAddressConfidential")
    private YesOrNo respondentAddressHiddenFromApplicant;
    // solicitorXXXX fields are for consented cases
    @CCD(ignore = true)
    private String solicitorName;
    @CCD(
            label = "Solicitor Firm",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminRPlus1RolesEhdmahAccess.class, CaseworkerDivorceFinancialremedySolicitorCrudPlus1RolesOpciwwAccess.class}
    )
    private String solicitorFirm;
    @CCD(ignore = true)
    private Address solicitorAddress;
    @CCD(ignore = true)
    private String solicitorPhone;
    @CCD(ignore = true)
    private String solicitorEmail;
    @CCD(ignore = true)
    @JsonProperty("solicitorDXnumber")
    private String solicitorDxNumber;
    @CCD(ignore = true)
    private YesOrNo solicitorAgreeToReceiveEmails;
    @CCD(ignore = true)
    @JsonProperty("appRespondentFMName")
    private String appRespondentFmName;
    @CCD(ignore = true)
    private String appRespondentLName;
    @CCD(ignore = true)
    @JsonProperty("appRespondentRep")
    private YesOrNo consentedRespondentRepresented;
    @CCD(
            label = "  ",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesGpeopvAccess.class}
    )
    private String isAdmin;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo currentUserIsApplicantSolicitor;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo currentUserIsRespondentSolicitor;

    @JsonIgnore
    private static final Set<String> APPLICANT_ADDRESS_DETAIL_FIELDS = Set.of(
        "applicantFmName",
        "applicantLname",
        "applicantAddress",
        "solicitorName",
        "solicitorAddress",
        "applicantSolicitorName",
        "applicantSolicitorAddress",
        "applicantAddressHiddenFromRespondent"
    );

    @JsonIgnore
    private static final Set<String> RESPONDENT_ADDRESS_DETAIL_FIELDS = Set.of(
        "respondentFmName",
        "respondentLname",
        "respondentAddress",
        "appRespondentFmName",
        "appRespondentLName",
        "respondentSolicitorName",
        "respondentSolicitorAddress",
        "respondentAddressHiddenFromApplicant"
    );

    public static boolean hasApplicantAddressDetailsChanged(ContactDetailsWrapper a, ContactDetailsWrapper b) {
        return hasAnyFieldChanged(diff(a, b), APPLICANT_ADDRESS_DETAIL_FIELDS);
    }

    public static boolean hasRespondentAddressDetailsChanged(ContactDetailsWrapper a, ContactDetailsWrapper b) {
        return hasAnyFieldChanged(diff(a, b), RESPONDENT_ADDRESS_DETAIL_FIELDS);
    }

    private static boolean hasAnyFieldChanged(Map<String, Object[]> fieldsChanged, Set<String> trackedFields) {
        return trackedFields.stream().anyMatch(fieldsChanged::containsKey);
    }

    /**
     * Compares two {@link ContactDetailsWrapper} objects field by field and returns a map
     * of the fields that are different.
     *
     * <p>Each entry in the returned map uses the field name as the key, and an
     * {@code Object[]} with two elements as the value:
     * <ul>
     *     <li>index 0 – the value of the field from {@code a}</li>
     *     <li>index 1 – the value of the field from {@code b}</li>
     * </ul>
     *
     * <p>The comparison treats empty values as {@code null} using
     * {@code areEqualTreatingEmptyAsNull}. If the values differ, the field is included
     * in the result.
     *
     * @param a the first wrapper to compare
     * @param b the second wrapper to compare
     * @return a map of field names to their differing values
     * @throws RuntimeException if the fields cannot be accessed
     */
    public static Map<String, Object[]> diff(ContactDetailsWrapper a, ContactDetailsWrapper b) {
        Map<String, Object[]> differences = new HashMap<>();

        try {
            for (Field field : ContactDetailsWrapper.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object v1 = field.get(a);
                Object v2 = field.get(b);

                if (!areEqualTreatingEmptyAsNull(v1, v2)) {
                    differences.put(field.getName(), new Object[]{v1, v2});
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return differences;
    }

    private static boolean areEqualTreatingEmptyAsNull(Object a, Object b) {

        // Strings: treat "" as null
        if (a instanceof String || b instanceof String) {
            String s1 = (a == null || a.toString().isBlank()) ? null : a.toString();
            String s2 = (b == null || b.toString().isBlank()) ? null : b.toString();
            return Objects.equals(s1, s2);
        }

        // Recursively compare nested Address (or other CCD objects)
        if (a instanceof Address && b instanceof Address) {
            return Objects.equals(
                normaliseAddress((Address) a),
                normaliseAddress((Address) b)
            );
        }

        // Everything else uses normal comparison
        return Objects.equals(a, b);
    }

    private static Address normaliseAddress(Address addr) {
        if (addr == null) {
            return null;
        }

        return Address.builder()
            .addressLine1(normaliseString(addr.getAddressLine1()))
            .addressLine2(normaliseString(addr.getAddressLine2()))
            .addressLine3(normaliseString(addr.getAddressLine3()))
            .postTown(normaliseString(addr.getPostTown()))
            .county(normaliseString(addr.getCounty()))
            .postCode(normaliseString(addr.getPostCode()))
            .country(normaliseString(addr.getCountry()))
            .build();
    }

    private static String normaliseString(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * Clears all stored information related to the respondent's solicitor.
     *
     * <p>
     * This method sets the following fields to {@code null}:
     * <ul>
     *     <li>Respondent solicitor name</li>
     *     <li>Respondent solicitor firm</li>
     *     <li>Respondent solicitor reference</li>
     *     <li>Respondent solicitor address</li>
     *     <li>Respondent solicitor phone number</li>
     *     <li>Respondent solicitor email</li>
     *     <li>Respondent solicitor DX number</li>
     * </ul>
     *
     * <p>
     * This is typically used when the respondent no longer has a solicitor,
     * or when resetting solicitor information is required for a case update.
     */
    public void clearRespondentSolicitorFields() {
        setRespondentSolicitorName(null);
        setRespondentSolicitorFirm(null);
        setRespondentSolicitorReference(null);
        setRespondentSolicitorAddress(null);
        setRespondentSolicitorPhone(null);
        setRespondentSolicitorEmail(null);
        setRespondentSolicitorDxNumber(null);
    }

    /**
     * Clears all applicant solicitor details from the case data.
     *
     * <p>
     * This method resets solicitor-related fields used in both consented and contested
     * case types by setting them to {@code null}.
     * <ul>
     *     <li><b>Common fields (consented & contested)</b> – solicitor reference</li>
     *     <li><b>Consented case fields</b> – solicitor name, firm, address, phone, email,
     *     DX number, and agreement to receive emails</li>
     *     <li><b>Contested case fields</b> – applicant solicitor name, firm, address,
     *     phone, email, DX number, and consent to receive emails</li>
     * </ul>
     *
     * <p>
     * Typically used when the applicant solicitor representation is removed or reset.
     */
    public void clearApplicantSolicitorFields() {
        // consented & contested
        setSolicitorReference(null);
        // consented
        setSolicitorName(null);
        setSolicitorFirm(null);
        setSolicitorAddress(null);
        setSolicitorPhone(null);
        setSolicitorEmail(null);
        setSolicitorDxNumber(null);
        setSolicitorAgreeToReceiveEmails(null);
        // contested
        setApplicantSolicitorName(null);
        setApplicantSolicitorFirm(null);
        setApplicantSolicitorAddress(null);
        setApplicantSolicitorPhone(null);
        setApplicantSolicitorEmail(null);
        setApplicantSolicitorDxNumber(null);
        setApplicantSolicitorConsentForEmails(null);
    }

    /**
     * Returns appropriate App solicitor address field value based on casetype.
     *
     * @param caseType the case type.
     * @return the solicitor address for the given case type.
     * @throws IllegalArgumentException if the case type is unsupported.
     */
    @JsonIgnore
    public Address getAppSolicitorAddress(CaseType caseType) {
        return switch (caseType) {
            case CONSENTED -> getSolicitorAddress();
            case CONTESTED -> getApplicantSolicitorAddress();
            default -> throw new IllegalArgumentException("Unsupported case type: " + caseType);
        };
    }

    /**
     * Returns Resp solicitor address field value based on casetype, this method exists as an
     * indicator that unlike applicant solicitor address, respondent solicitor address
     * is stored in a single field for both contested and consented cases.
     *
     * @param caseType the case type.
     * @return the solicitor address for the given case type.
     * @throws IllegalArgumentException if the case type is unsupported.
     */
    @JsonIgnore
    public Address getRespSolicitorAddress(CaseType caseType) {
        return switch (caseType) {
            case CONSENTED, CONTESTED -> getRespondentSolicitorAddress();
            default -> throw new IllegalArgumentException("Unsupported case type: " + caseType);
        };
    }
}
