/*
 * Copyright 2019-2021 qifu of copyright Chen Xin Nien
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmAiAnalysis;

@Mapper
public interface FmAiAnalysisMapper extends IBaseMapper<FmAiAnalysis, String> {

	FmAiAnalysis findLatestSucceeded(Map<String, Object> paramMap);

	Integer findNextGenerationNo(Map<String, Object> paramMap);

	int complete(Map<String, Object> paramMap);

	int fail(Map<String, Object> paramMap);
}
