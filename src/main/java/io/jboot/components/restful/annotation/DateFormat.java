package io.jboot.components.restful.annotation;

import java.lang.annotation.*;

/**
 *
 * 日期注解，可以结合@PathVarible,@RequestParam,@RequestHeader一起使用
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface DateFormat {

    String value() default "yyyy-MM-dd HH:mm:ss";

    String timeZone() default "GMT+8";

}
