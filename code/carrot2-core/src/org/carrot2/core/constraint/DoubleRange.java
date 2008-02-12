package org.carrot2.core.constraint;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@IsConstraint(implementation = RangeConstraint.class)
public @interface DoubleRange {
    double min();
    double max();
}
