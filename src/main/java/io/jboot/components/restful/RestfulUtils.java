package io.jboot.components.restful;


import com.jfinal.core.ActionException;
import com.jfinal.kit.JsonKit;
import com.jfinal.render.RenderManager;
import io.jboot.components.restful.annotation.*;
import io.jboot.utils.StrUtil;
import io.jboot.web.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class RestfulUtils {

    private static final RenderManager renderManager = RenderManager.me();

    /**
     * 从url中解析路径参数
     *
     * @param url
     * @param actionKey
     * @return
     */
    public static Map<String, String> parsePathVariables(String url, String actionKey) {
        if (actionKey.contains("{") && actionKey.contains("}")) {
            Map<String, String> pathVariables = new HashMap<>();
            String[] paths = url.split("/");
            String[] _paths = actionKey.split("/");
            for (int i = 0; i < paths.length; i++) {
                if (_paths[i].startsWith("{") && _paths[i].endsWith("}")) {
                    String pathKey = _paths[i].substring(1, _paths[i].length() - 1);
                    String value = paths[i];
                    pathVariables.put(pathKey, value);
                }
            }
            return pathVariables;
        } else {
            return null;
        }
    }

    /**
     * 转换请求action请求的参数信息
     *
     * @param target
     * @param actionKey
     * @param actionMethod
     * @param request
     * @param rawData
     * @return
     * @throws ActionException
     */
    public static Object[] parseActionMethodParameters(String target, String actionKey, Method actionMethod, HttpServletRequest request, String rawData)
            throws ActionException {
        Object[] args = new Object[actionMethod.getParameters().length];
        for (int i = 0; i < actionMethod.getParameters().length; i++) {
            Parameter parameter = actionMethod.getParameters()[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            String parameterName = parameter.getName();
            String values[];
            if (requestParam != null) {
                if (StrUtil.isNotBlank(requestParam.value())) {
                    parameterName = requestParam.value();
                }
                values = request.getParameterValues(parameterName);
                parameter.getType();
                args[i] = parseRequestParamToParameter(values, parameterName, parameter.getType(), parameter);
                if (args[i] == null && requestParam.required()) {
                    //要求参数不为空，但是却并没有提供参数
                    throw genBindError("Parameter '" + parameterName + "' specifies a forced check, but the value is null");
                }
            } else if (requestBody != null) {
                args[i] = parseRequestBodyToParameter(rawData, parameterName, parameter.getType(), parameter);
            } else if (requestHeader != null) {
                if (StrUtil.isNotBlank(requestHeader.value())) {
                    parameterName = requestHeader.value();
                }
                String value = request.getHeader(parameterName);
                args[i] = parseRequestHeaderToParameter(value, parameterName, parameter.getType(), parameter);
                if (args[i] == null && requestHeader.required()) {
                    //要求参数不为空，但是却并没有提供参数
                    throw genBindError("Parameter '" + parameterName + "' specifies a forced check, but the value is null");
                }
            } else if (pathVariable != null) {
                if (StrUtil.isNotBlank(pathVariable.value())) {
                    parameterName = pathVariable.value();
                }
                args[i] = parsePathVariableToParameter(target, actionKey, parameterName, parameter.getType(), parameter);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    /**
     * 比对url请求路径
     *
     * @param sourcePaths action配置的原路径
     * @param targetPaths 请求的目标路径
     * @return
     */
    public static boolean comparePaths(String[] sourcePaths, String[] targetPaths) {
        int matchingCount = 0;
        for (int i = 0; i < sourcePaths.length; i++) {
            if (sourcePaths[i].equals(targetPaths[i])
                    || (sourcePaths[i].startsWith("{") && sourcePaths[i].endsWith("}"))) {
                matchingCount += 1;
            }
        }
        return matchingCount == sourcePaths.length;
    }

    private static Object parseRequestParamToParameter(String[] value, String name, Class<?> parameterTypeClass, Parameter parameter) {
        if (parameterTypeClass.isArray()) {
            Object[] objects = new Object[value.length];
            for (int i = 0; i < value.length; i++) {
                objects[i] = parseCommonValue(value[i], name, parameterTypeClass, parameter);
            }
            return objects;
        } else {
            if (value != null && value.length > 0) {
                return parseCommonValue(value[0], name, parameterTypeClass, parameter);
            }
        }

        return null;
    }

    private static Object parseRequestHeaderToParameter(String header, String name, Class<?> parameterTypeClass, Parameter parameter) {
        return parseCommonValue(header, name, parameterTypeClass, parameter);
    }

    private static Object parseRequestBodyToParameter(String body, String name, Class<?> parameterTypeClass, Parameter parameter) {
        //先当作基本数据来转换
        Object value = parseCommonValue(body, name, parameterTypeClass, parameter);
        if (value == null) {
            value = JsonKit.parse(body, parameterTypeClass);
        }
        return value;
    }

    private static Object parsePathVariableToParameter(String target, String actionKey, String parameterName, Class<?> parameterTypeClass, Parameter parameter) {
        Map<String, String> pathVariables = parsePathVariables(target, actionKey);
        String value = pathVariables.get(parameterName);
        return parseCommonValue(value, parameterName, parameterTypeClass, parameter);
    }

    /**
     * 转换基本类型参数，目前支持string,int,double,float,boolean,long基本类型数据
     *
     * @param value
     * @param name
     * @param parameterTypeClass
     * @param parameter
     * @return
     */
    private static Object parseCommonValue(String value, String name, Class<?> parameterTypeClass, Parameter parameter) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        if (parameterTypeClass.equals(String.class)) {
            return value;
        } else if (parameterTypeClass.equals(int.class)
                || parameterTypeClass.equals(double.class)
                || parameterTypeClass.equals(float.class)
                || parameterTypeClass.equals(long.class)
                || parameterTypeClass.equals(BigDecimal.class)
                || parameterTypeClass.equals(short.class)) {
            try {
                if (parameterTypeClass.equals(int.class)) {
                    return Integer.valueOf(value);
                } else if (parameterTypeClass.equals(double.class)) {
                    return Double.valueOf(value);
                } else if (parameterTypeClass.equals(float.class)) {
                    return Float.valueOf(value);
                } else if (parameterTypeClass.equals(long.class)) {
                    return Long.valueOf(value);
                } else if (parameterTypeClass.equals(BigDecimal.class)) {
                    return new BigDecimal(value);
                } else if (parameterTypeClass.equals(short.class)) {
                    return Short.valueOf(value);
                } else {
                    return null;
                }
            } catch (NumberFormatException e) {
                throw genBindError("Error resolving parameter '" + name + "', unable to match value '"
                        + value + "' to specified type '" + parameterTypeClass.getName() + "'");
            }
        } else if (parameterTypeClass.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (parameterTypeClass.equals(Date.class)) {
            DateFormat dateFormat = parameter.getAnnotation(DateFormat.class);
            SimpleDateFormat simpleDateFormat;
            if (dateFormat != null) {
                simpleDateFormat = new SimpleDateFormat(dateFormat.value());
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(dateFormat.timeZone()));
            } else {
                simpleDateFormat = new SimpleDateFormat();
            }
            try {
                return simpleDateFormat.parse(value);
            } catch (ParseException e) {
                throw genBindError("Error resolving parameter '" + name + "', unable to match value '"
                        + value + "' to specified type '" + parameterTypeClass.getName() + "'");
            }
        } else {
            return null;
        }
    }

    private static ActionException genBindError(String message) {
        return new ActionException(HttpStatus.BAD_REQUEST.value(),
                renderManager.getRenderFactory().getErrorRender(HttpStatus.BAD_REQUEST.value()), message);
    }

}
