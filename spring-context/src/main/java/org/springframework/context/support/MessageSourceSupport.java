/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * MessageSourceSupport为消息源的基础实现类，提供了消除格式化处理的基础支撑。
 * 但是没有实现spring上下文中消息源的具体方法。
 *
 * {@link AbstractMessageSource}继承了基类，并提供了{@code getMessage}的具体实现，可以代理
 * 消息编码解决的中心模板方法。
 *
 * Base class for message source implementations, providing support infrastructure
 * such as {@link java.text.MessageFormat} handling but not implementing concrete
 * methods defined in the {@link org.springframework.context.MessageSource}.
 *
 * <p>{@link AbstractMessageSource} derives from this class, providing concrete
 * {@code getMessage} implementations that delegate to a central template
 * method for message code resolution.
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public abstract class MessageSourceSupport {

	/**
	 * 这是一个无效的 MessageFormat
	 */
	private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 是否消息格式，解析没有参数的消息
	 */
	private boolean alwaysUseMessageFormat = false;

	/**
	 * 缓存已经产生消息的消息格式
	 *
	 * Cache to hold already generated MessageFormats per message.
	 * Used for passed-in default messages. MessageFormats for resolved
	 * codes are cached on a specific basis in subclasses.
	 */
	private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage = new HashMap<>();

	/**
	 * Set whether to always apply the {@code MessageFormat} rules,
	 * parsing even messages without arguments.
	 * <p>Default is "false": Messages without arguments are by default
	 * returned as-is, without parsing them through MessageFormat.
	 * Set this to "true" to enforce MessageFormat for all messages,
	 * expecting all message texts to be written with MessageFormat escaping.
	 * <p>For example, MessageFormat expects a single quote to be escaped
	 * as "''". If your message texts are all written with such escaping,
	 * even when not defining argument placeholders, you need to set this
	 * flag to "true". Else, only message texts with actual arguments
	 * are supposed to be written with MessageFormat escaping.
	 * @see java.text.MessageFormat
	 */
	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}

	/**
	 * Return whether to always apply the MessageFormat rules, parsing even
	 * messages without arguments.
	 */
	protected boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}


	/**
	 * 渲染给定的默认消息
	 * 默认的实现，将会使用传入的参数，解决消息中的占位符。
	 * 子类可以重写此方法，用于定制消息的处理过程
	 *
	 * Render the given default message String. The default message is
	 * passed in as specified by the caller and can be rendered into
	 * a fully formatted default message shown to the user.
	 * <p>The default implementation passes the String to {@code formatMessage},
	 * resolving any argument placeholders found in them. Subclasses may override
	 * this method to plug in custom processing of default messages.
	 * @param defaultMessage the passed-in default message String
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none.
	 * @param locale the Locale used for formatting
	 * @return the rendered default message (with resolved arguments)
	 * @see #formatMessage(String, Object[], java.util.Locale)
	 */
	protected String renderDefaultMessage(String defaultMessage, @Nullable Object[] args, Locale locale) {
		return formatMessage(defaultMessage, args, locale);
	}

	/**
	 * 使用缓存的消息格式，格式化给定消息字符串。
	 * 默认使用参数解决消息中的占位符
	 *
	 * Format the given message String, using cached MessageFormats.
	 * By default invoked for passed-in default messages, to resolve
	 * any argument placeholders found in them.
	 * @param msg the message to format
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none
	 * @param locale the Locale used for formatting
	 * @return the formatted message (with resolved arguments)
	 */
	protected String formatMessage(String msg, @Nullable Object[] args, Locale locale) {
		// tips: 统一下语法，不然会晕的~
		// locale: 区域
		// msg: 消息
		// args: 参数

		// <1> 是否允许消息格式化 并且 参数为空，就直接返回了
		if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
			return msg;
		}
		MessageFormat messageFormat = null;
		// <2> 对 messageFormatsPerMessage 缓存进行加锁，里面保存的是 msg 所有本地消息
		synchronized (this.messageFormatsPerMessage) {
			// <3> 从缓存中获取，msg 的所有本地消息
			Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
			// <4> 通过msg 所有本地消息，获取对应的区域 MessageFormat
			if (messageFormatsPerLocale != null) {
				messageFormat = messageFormatsPerLocale.get(locale);
			}
			else {
				// <5> 没有 msg 的本地消息，就 new HashMap() 保存
				messageFormatsPerLocale = new HashMap<>();
				this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
			}
			// <6> 没有msg 这个区域的 messageFormat 进入，创建一个 messageFormat
			if (messageFormat == null) {
				try {
					// <7> 根据msg 和 区域，创建一个 MessageFormat
					messageFormat = createMessageFormat(msg, locale);
				}
				catch (IllegalArgumentException ex) {
					// Invalid message format - probably not intended for formatting,
					// rather using a message structure with no arguments involved...
					if (isAlwaysUseMessageFormat()) {
						throw ex;
					}
					// Silently proceed with raw message if format not enforced...
					messageFormat = INVALID_MESSAGE_FORMAT;
				}
				// <8> 将 messageFormat 保存到 messageFormatsPerLocale 缓存
				messageFormatsPerLocale.put(locale, messageFormat);
			}
		}
		// <9> MessageFormat == 非法的MessageFormat，那么直接返回
		if (messageFormat == INVALID_MESSAGE_FORMAT) {
			return msg;
		}
		// <10> 对 messageFormat，进行加锁，然后 format 返回对应的 value
		synchronized (messageFormat) {
			// <11> 根据区域，格式化消息
			return messageFormat.format(resolveArguments(args, locale));
		}
	}

	/**
	 * 根基给定的消息和本地化创建消息格式
	 *
	 * Create a MessageFormat for the given message and Locale.
	 * @param msg the message to create a MessageFormat for
	 * @param locale the Locale to create a MessageFormat for
	 * @return the MessageFormat instance
	 */
	protected MessageFormat createMessageFormat(String msg, Locale locale) {
		return new MessageFormat(msg, locale);
	}

	/**
	 * 将参数解析为本地化参数
	 * 默认实现，简单返回给定的参数数组，为了解决特殊的参数类型，子类可以重写此方。
	 *
	 * Template method for resolving argument objects.
	 * <p>The default implementation simply returns the given argument array as-is.
	 * Can be overridden in subclasses in order to resolve special argument types.
	 * @param args the original argument array
	 * @param locale the Locale to resolve against
	 * @return the resolved argument array
	 */
	protected Object[] resolveArguments(@Nullable Object[] args, Locale locale) {
		return (args != null ? args : new Object[0]);
	}

}
