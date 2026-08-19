package org.qifu.fm.domain.form;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class FmFormScriptContractValidator {

    private static final int MAX_SCRIPT_LENGTH = 200_000;
    private static final Set<String> LIFECYCLES = Set.of(
            "onFormLoad", "onFieldChange", "beforeSubmit", "afterSubmit",
            "onDataActionSuccess", "onDataActionError", "onDestroy");
    private static final Pattern RETURN_OBJECT = Pattern.compile("\\breturn\\s*\\{");
    private static final Pattern LIFECYCLE_LIKE = Pattern.compile(
            "(?<![.\\w$])(?:async\\s+)?((?:on|before|after)[A-Z][A-Za-z0-9_$]*)"
                    + "\\s*(?:\\(|:)");
    private static final Pattern MODULE_SYNTAX = Pattern.compile(
            "(?m)^\\s*(?:import\\s|export\\s|module\\.exports|exports\\.)");

    public void validate(String script) throws ServiceException {
        if (StringUtils.isBlank(script)) {
            return;
        }
        if (script.length() > MAX_SCRIPT_LENGTH) {
            throw new ServiceException("表單客製 JavaScript 不可超過 200,000 字元");
        }
        if (MODULE_SYNTAX.matcher(script).find()) {
            throw new ServiceException("表單客製 JavaScript 不支援 import、export 或 CommonJS");
        }
        if (!RETURN_OBJECT.matcher(script).find()) {
            throw new ServiceException("表單客製 JavaScript 必須以 return { ... } 回傳生命週期物件");
        }
        Matcher matcher = LIFECYCLE_LIKE.matcher(script);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!LIFECYCLES.contains(name)) {
                throw new ServiceException("不支援的表單 JavaScript 生命週期：" + name);
            }
        }
    }
}
