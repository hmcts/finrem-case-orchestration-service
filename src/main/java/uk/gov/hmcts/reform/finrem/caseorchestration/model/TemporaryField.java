package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field as temporary so that it can be cleared during data sanitisation.
 *
 * <p>Fields annotated with {@code @TemporaryField} are not intended to be persisted
 * or stored long-term. They should be removed as part of the sanitisation process.</p>
 *
 * <p><strong>Developer note:</strong> When adding {@code @TemporaryField} to a field in
 * a new class, ensure that the class is also added to the list returned by
 * {@code getClassesWithTemporaryFieldAnnotation()} so the field is cleared correctly.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TemporaryField {
}
