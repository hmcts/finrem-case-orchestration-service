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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactDetailsWrapper {
    private YesOrNo updateIncludesRepresentativeChange;
    private NoticeOfChangeParty nocParty;
    private YesOrNo applicantRepresented;
    private Address applicantSolicitorAddress;
    private String applicantSolicitorName;
    private String applicantSolicitorFirm;
    private String solicitorReference;
    private String applicantSolicitorPhone;
    private String applicantSolicitorEmail;
    @JsonProperty("applicantSolicitorDXnumber")
    private String applicantSolicitorDxNumber;
    private YesOrNo applicantSolicitorConsentForEmails;
    @JsonProperty("applicantFMName")
    private String applicantFmName;
    @JsonProperty("applicantLName")
    private String applicantLname;
    private Address applicantAddress;
    private YesOrNo applicantResideOutsideUK;
    private String applicantPhone;
    private String applicantEmail;
    @JsonProperty("applicantAddressConfidential")
    private YesOrNo applicantAddressHiddenFromRespondent;
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    @JsonProperty("respondentLName")
    private String respondentLname;
    @JsonProperty("respondentRepresented")
    private YesOrNo contestedRespondentRepresented;
    @JsonProperty("rSolicitorName")
    private String respondentSolicitorName;
    @JsonProperty("rSolicitorFirm")
    private String respondentSolicitorFirm;
    @JsonProperty("rSolicitorReference")
    private String respondentSolicitorReference;
    @JsonProperty("rSolicitorAddress")
    private Address respondentSolicitorAddress;
    @JsonProperty("rSolicitorPhone")
    private String respondentSolicitorPhone;
    @JsonProperty("rSolicitorEmail")
    private String respondentSolicitorEmail;
    @JsonProperty("rSolicitorDXnumber")
    private String respondentSolicitorDxNumber;
    private Address respondentAddress;
    private YesOrNo respondentResideOutsideUK;
    private String respondentPhone;
    private String respondentEmail;
    @JsonProperty("respondentAddressConfidential")
    private YesOrNo respondentAddressHiddenFromApplicant;
    // solicitorXXXX fields are for consented cases
    private String solicitorName;
    private String solicitorFirm;
    private Address solicitorAddress;
    private String solicitorPhone;
    private String solicitorEmail;
    @JsonProperty("solicitorDXnumber")
    private String solicitorDxNumber;
    private YesOrNo solicitorAgreeToReceiveEmails;
    @JsonProperty("appRespondentFMName")
    private String appRespondentFmName;
    private String appRespondentLName;
    @JsonProperty("appRespondentRep")
    private YesOrNo consentedRespondentRepresented;
    private String isAdmin;
    @TemporaryField
    private YesOrNo currentUserIsApplicantSolicitor;
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
}
