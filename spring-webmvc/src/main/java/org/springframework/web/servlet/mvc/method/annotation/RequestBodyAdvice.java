/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.mvc.method.annotation;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

/**
 * 允许在读取请求的主体并将其转换为Object之前自定义请求，
 * 还允许在将生成的Object作为{@code @RequestBody}
 * 或{@code HttpEntity}方法参数传递到控制器方法之前进行处理。
 *
 * Allows customizing the request before its body is read and converted into an
 * Object and also allows for processing of the resulting Object before it is
 * passed into a controller method as an {@code @RequestBody} or an
 * {@code HttpEntity} method argument.
 *
 * <p>Implementations of this contract may be registered directly with the
 * {@code RequestMappingHandlerAdapter} or more likely annotated with
 * {@code @ControllerAdvice} in which case they are auto-detected.
 *
 * @author Rossen Stoyanchev
 * @since 4.2
 */
public interface RequestBodyAdvice {

	/**
	 * Invoked first to determine if this interceptor applies.
	 * @param methodParameter the method parameter
	 * @param targetType the target type, not necessarily the same as the method
	 * parameter type, e.g. for {@code HttpEntity<String>}.
	 * @param converterType the selected converter type
	 * @return whether this interceptor should be invoked or not
	 */
	boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType);

	/**
	 * 在读取和转换请求正文之前调用第二秒。
	 *
	 * Invoked second before the request body is read and converted.
	 * @param inputMessage the request
	 * @param parameter the target method parameter
	 * @param targetType the target type, not necessarily the same as the method
	 * parameter type, e.g. for {@code HttpEntity<String>}.
	 * @param converterType the converter used to deserialize the body
	 * @return the input request or a new instance, never {@code null}
	 */
	HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException;

	/**
	 * 将请求正文转换为对象后调用的第三个（也是最后一个）。
	 *
	 * Invoked third (and last) after the request body is converted to an Object.
	 * @param body set to the converter Object before the first advice is called
	 * @param inputMessage the request
	 * @param parameter the target method parameter
	 * @param targetType the target type, not necessarily the same as the method
	 * parameter type, e.g. for {@code HttpEntity<String>}.
	 * @param converterType the converter used to deserialize the body
	 * @return the same body or a new instance
	 */
	Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType);

	/**
	 * 如果主体为空，则调用第二（也是最后一个）。
	 *
	 * Invoked second (and last) if the body is empty.
	 * @param body usually set to {@code null} before the first advice is called
	 * @param inputMessage the request
	 * @param parameter the method parameter
	 * @param targetType the target type, not necessarily the same as the method
	 * parameter type, e.g. for {@code HttpEntity<String>}.
	 * @param converterType the selected converter type
	 * @return the value to use or {@code null} which may then raise an
	 * {@code HttpMessageNotReadableException} if the argument is required.
	 */
	@Nullable
	Object handleEmptyBody(@Nullable Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType);


}
