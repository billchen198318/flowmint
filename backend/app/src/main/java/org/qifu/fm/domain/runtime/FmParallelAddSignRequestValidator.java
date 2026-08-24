package org.qifu.fm.domain.runtime;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.entity.FmTaskPolicy;
import org.springframework.stereotype.Component;

@Component
public class FmParallelAddSignRequestValidator {

    private static final Set<String> RESULTS = Set.of("AGREE", "DISAGREE");

    public List<String> validateStart(
            FmParallelAddSignStartRequest request,
            FmTaskPolicy policy,
            String actorAccount) throws ServiceException {
        if (request == null
                || StringUtils.isAnyBlank(
                        request.taskId(), request.reason(), request.requestKey())
                || request.memberAccounts() == null
                || request.memberAccounts().isEmpty()) {
            throw new ServiceException("平行加簽的 Task、成員、原因及 request key 必填");
        }
        if (policy == null || !"Y".equals(policy.getAllowParallelAddSign())) {
            throw new ServiceException("此關卡未允許平行加簽");
        }
        if (request.reason().trim().length() > 1000
                || request.requestKey().trim().length() > 100) {
            throw new ServiceException("平行加簽原因或 request key 超過長度限制");
        }
        int maximum = policy.getParallelAddSignMaxMembers() == null
                ? 10 : policy.getParallelAddSignMaxMembers();
        if (maximum < 1 || maximum > 20
                || request.memberAccounts().size() > maximum) {
            throw new ServiceException("平行加簽人數超過此關卡上限");
        }
        LinkedHashSet<String> members = new LinkedHashSet<>();
        for (String member : request.memberAccounts()) {
            String account = StringUtils.trimToEmpty(member);
            if (account.isEmpty() || actorAccount.equals(account)) {
                throw new ServiceException("平行加簽成員不可空白或為原處理人");
            }
            if (!members.add(account)) {
                throw new ServiceException("同一批平行加簽不可有重複成員");
            }
        }
        return List.copyOf(members);
    }

    public String validateComplete(
            FmParallelAddSignCompleteRequest request,
            boolean commentRequired) throws ServiceException {
        if (request == null
                || StringUtils.isAnyBlank(request.taskId(), request.result())
                || !RESULTS.contains(request.result())) {
            throw new ServiceException("平行加簽 Task 與回覆結果不合法");
        }
        String comment = StringUtils.trimToEmpty(request.comment());
        if (commentRequired && comment.isEmpty()) {
            throw new ServiceException("平行加簽意見必填");
        }
        if (comment.length() > 1000) {
            throw new ServiceException("平行加簽意見不得超過 1000 字");
        }
        return comment;
    }
}
