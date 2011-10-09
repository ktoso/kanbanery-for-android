package pl.project13.kanbanery.guice.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation, use this to mark features only available on Honeycomb devices
 *
 * @author Konrad Malawski
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Honeycomb
{
}
