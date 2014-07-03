package com.turn.i18ntest;
/**
 * Copyright (C) 2014 Turn Inc.  All Rights Reserved.
 * Proprietary and confidential.
 * 
 * @author nxin
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface I18nTest {
	boolean enable() default true;
	String dictionary();
}
