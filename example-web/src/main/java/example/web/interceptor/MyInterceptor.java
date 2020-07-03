package example.web.interceptor;

import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * author: Sin
 * time: 2020/7/2 17:35
 */
public class MyInterceptor implements WebRequestInterceptor {
	@Override
	public void preHandle(WebRequest request) throws Exception {
		System.err.println("preHandle");
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		System.err.println("postHandle");
	}

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		System.err.println("afterCompletion");
	}
}
