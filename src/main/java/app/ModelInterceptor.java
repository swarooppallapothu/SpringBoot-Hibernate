//package app;
//
//import com.assetrabbit.util.Util;
//
//import org.springframework.security.web.csrf.CsrfToken;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * Always add certain fields to the model in post-processing.
// */
//public class ModelInterceptor extends HandlerInterceptorAdapter {
//
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//            ModelAndView modelAndView) throws Exception {
//
//        // Model population is only applicable if a modelAndView is present.
//        if (modelAndView == null) {
//            return;
//        }
//
//        // Always pass along any present CSRF token to the model.
//        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//        if (token != null) {
//            modelAndView.addObject(token.getParameterName(), token.getToken());
//        }
//
//        // Provide the currently logged-in user.
//        modelAndView.addObject("currentUser", Util.getCurrentUser());
//    }
//
//}
