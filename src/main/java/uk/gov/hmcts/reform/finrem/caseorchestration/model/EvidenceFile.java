package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EvidenceFileValidator.class)
public @interface EvidenceFile {
    String message() default "{EvidenceFile.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
