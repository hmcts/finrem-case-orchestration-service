package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        if (addr == null) return null;

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

}
