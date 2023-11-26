package com.distsystem.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface DaoTable {
    /** name of table representing that entity */
    public String tableName() default "";
    /** name of key column */
    public String keyName() default "";
    /** */
    public boolean keyIsUnique() default true;
}
