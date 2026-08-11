package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmNotification;

@Mapper
public interface FmNotificationMapper extends IBaseMapper<FmNotification, String> {

	List<FmNotification> findInbox(Map<String, Object> paramMap);

	long countUnread(Map<String, Object> paramMap);

	int markRead(Map<String, Object> paramMap);

	int markAllRead(Map<String, Object> paramMap);

	int insertIfAbsent(FmNotification notification);
}
