package com.mta.tehreer.internal.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
public @interface Sustain {
}
