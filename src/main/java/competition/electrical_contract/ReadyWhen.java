package competition.electrical_contract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a contract getter as belonging to a {@link Hardware} mechanism. The device only exists on a
 * robot whose contract lists that mechanism in its readiness set. Used by the electrical report
 * tool to leave out devices a robot doesn't have.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadyWhen {
    /** The mechanism this device is part of. */
    Hardware value();
}
