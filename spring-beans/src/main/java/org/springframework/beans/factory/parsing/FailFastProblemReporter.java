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

package org.springframework.beans.factory.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * 简单的{@link ProblemReporter}实现，在遇到错误时显示快速失败行为。
 * <p>
 * 遇到的第一个错误导致引发{@link BeanDefinitionParsingException}。
 * <p>
 * Simple {@link ProblemReporter} implementation that exhibits fail-fast
 * behavior when errors are encountered.
 *
 * <p>The first error encountered results in a {@link BeanDefinitionParsingException}
 * being thrown.
 *
 * <p>Warnings are written to
 * {@link #setLogger(org.apache.commons.logging.Log) the log} for this class.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public class FailFastProblemReporter implements ProblemReporter {

	private Log logger = LogFactory.getLog(getClass());


	/**
	 * 设置用于报告警告的{@link Log logger}。
	 * 如果设置为{@code null}，则将使用设置为实例类名称的默认{@link Log logger}。
	 *
	 * Set the {@link Log logger} that is to be used to report warnings.
	 * <p>If set to {@code null} then a default {@link Log logger} set to
	 * the name of the instance class will be used.
	 *
	 * @param logger the {@link Log logger} that is to be used to report warnings
	 */
	public void setLogger(@Nullable Log logger) {
		this.logger = (logger != null ? logger : LogFactory.getLog(getClass()));
	}


	/**
	 * 引发{@link BeanDefinitionParsingException}，详细说明发生的错误。
	 *
	 * Throws a {@link BeanDefinitionParsingException} detailing the error
	 * that has occurred.
	 *
	 * @param problem the source of the error
	 */
	@Override
	public void fatal(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	/**
	 * 引发{@link BeanDefinitionParsingException}，详细说明发生的错误。
	 *
	 * Throws a {@link BeanDefinitionParsingException} detailing the error
	 * that has occurred.
	 *
	 * @param problem the source of the error
	 */
	@Override
	public void error(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	/**
	 * 将提供的{@link Problem}写入{@link Log}的{@code WARN}级别。
	 *
	 * Writes the supplied {@link Problem} to the {@link Log} at {@code WARN} level.
	 *
	 * @param problem the source of the warning
	 */
	@Override
	public void warning(Problem problem) {
		logger.warn(problem, problem.getRootCause());
	}

}
