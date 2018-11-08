package com.sword.gsa.spis.scs.algolia.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
//@Target(ElementType.PACKAGE)
@Inherited
@Retention(RetentionPolicy.CLASS)
public @interface IndexAnnotation{
    String indexName();
}

