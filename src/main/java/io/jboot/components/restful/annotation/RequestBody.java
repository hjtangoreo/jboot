package io.jboot.components.restful.annotation;

import java.lang.annotation.*;

/**
 * 请求体参数注解
 * 支持如下类型参数：
 *  string
 *  int
 *  double
 *  float
 *  boolean
 *  long
 *  object
 *  bigDecimal
 *  date
 *  short
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestBody {
}
