package io.jboot.components.restful;

import com.jfinal.aop.Invocation;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import io.jboot.components.restful.annotation.ResponseHeader;
import io.jboot.components.restful.annotation.ResponseHeaders;
import io.jboot.utils.ArrayUtil;
import io.jboot.web.handler.JbootActionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class RestfulHandler extends JbootActionHandler {

    private static final Log log = Log.getLog(JbootActionHandler.class);

    @Override
    public Action getAction(String target, String[] urlPara, HttpServletRequest request) {
        //优先从restful action 获取请求，防止url被当成urlPara处理
        Action action = JbootRestfulManager.me().getRestfulAction(target, request.getMethod());
        if (action == null) {
            action = super.getAction(target, urlPara);
        }
        return action;
    }

    @Override
    public Invocation getInvocation(Action action, Controller controller) {
        if (action instanceof RestfulAction) {
            Object[] args = RestfulUtils.parseActionMethodParameters(action.getActionKey(), action.getActionKey(),
                    action.getMethod(), controller.getRequest(), controller.getRawData());
            return new RestfulInvocation(action, controller, args);
        } else {
            return super.getInvocation(action, controller);
        }
    }

    @Override
    public void setResponse(HttpServletResponse response, Action action) {
        ResponseHeader[] responseHeaders = action.getMethod().getAnnotationsByType(ResponseHeader.class);
        ResponseHeaders responseHeadersList = action.getMethod().getAnnotation(ResponseHeaders.class);
        if (responseHeadersList != null && responseHeadersList.value().length > 0) {
            if (responseHeaders != null && responseHeaders.length > 0) {
                responseHeaders = ArrayUtil.concat(responseHeaders, responseHeadersList.value());
            } else {
                responseHeaders = responseHeadersList.value();
            }
        }
        if (responseHeaders.length > 0) {
            Arrays.asList(responseHeaders).forEach((ResponseHeader header) -> {
                response.setHeader(header.key(), header.value());
            });
        }
    }

}
