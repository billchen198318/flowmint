/*
 * Copyright 2019-2021 qifu of copyright Chen Xin Nien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -----------------------------------------------------------------------
 *
 * author: 	Chen Xin Nien
 * contact: chen.xin.nien@gmail.com
 *
 */
package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmSystemTaskExecution;

@Mapper
public interface FmSystemTaskExecutionMapper extends IBaseMapper<FmSystemTaskExecution, String> {

	public int claim(Map<String, Object> paramMap);

    public int rearm(Map<String, Object> paramMap);

	public List<FmSystemTaskExecution> findAutomaticRetries(Map<String, Object> paramMap);

    List<FmSystemTaskExecution> findStalledReady(Map<String, Object> paramMap);

    int finishReady(Map<String, Object> paramMap);

	public int complete(Map<String, Object> paramMap);

	public int cancel(Map<String, Object> paramMap);

	public FmSystemTaskExecution lockInvocation(Map<String, Object> paramMap);

	public int fail(Map<String, Object> paramMap);

	public List<FmSystemTaskExecution> findExpired(Map<String, Object> paramMap);

	public int expire(Map<String, Object> paramMap);

	public List<FmSystemTaskExecution> lockActiveForProcess(Map<String, Object> paramMap);

	public List<FmSystemTaskExecution> findIncidents(Map<String, Object> paramMap);
}
