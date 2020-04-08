package io.jboot.components.restful;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;

import java.lang.reflect.Method;

public class RestfulAction extends Action {

    private String requestMethod;

    public String getRequestMethod() {
        return requestMethod;
    }

    public RestfulAction(String controllerKey, String actionKey, Class<? extends Controller> controllerClass,
                         Method method, String methodName, Interceptor[] interceptors, String viewPath, String requestMethod) {
        super(controllerKey, actionKey, controllerClass, method, methodName, interceptors, viewPath);
        this.requestMethod = requestMethod;
    }


}
