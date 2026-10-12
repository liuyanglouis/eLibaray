// 文件路径: D:\Download\eLibaray\src\main\java\com\elibrary\common\security\LoginRequired.java
package com.elibrary.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}