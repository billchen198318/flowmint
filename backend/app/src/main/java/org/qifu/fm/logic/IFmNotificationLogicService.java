package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmNotificationInboxView;

public interface IFmNotificationLogicService {

	DefaultResult<FmNotificationInboxView> inbox(String tenantId) throws ServiceException;

	DefaultResult<Long> markRead(String tenantId, String notificationId) throws ServiceException;

	DefaultResult<Long> markAllRead(String tenantId) throws ServiceException;
}
