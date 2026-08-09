package org.qifu.fm.domain.resolver;

import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmTaskAssignmentRule;

public interface IFmAssignmentResolverService {

    FmResolverPreviewView resolve(
            FmTaskAssignmentRule rule,
            String initiatorAccount) throws ServiceException;

    FmResolverPreviewView resolve(
            FmTaskAssignmentRule rule,
            String initiatorAccount,
            Map<String, Object> variables) throws ServiceException;
}
