package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.entities.BudgetSetting;
import com.jsh.erp.datasource.mappers.BudgetSettingMapper;
import com.jsh.erp.datasource.vo.BudgetSettingVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Service
public class BudgetSettingService {
    @Resource private BudgetSettingMapper mapper;
    @Resource private UserService userService;

    private void checkAccess() throws Exception {
        if (!userService.isAdminOrOfficeUser(userService.getCurrentUser())) {
            throw new SecurityException("仅管理员或办公室账号可管理预算");
        }
    }
    public List<BudgetSettingVo> list(Integer year, String name) throws Exception {
        checkAccess();
        checkYearInRange(year);
        Long tenantId = userService.getCurrentUser().getTenantId();
        return mapper.list(year, tenantId, name);
    }
    @Transactional(rollbackFor = Exception.class)
    public int save(JSONObject obj, HttpServletRequest request) throws Exception {
        checkAccess();
        BudgetSetting setting = JSONObject.parseObject(obj.toJSONString(), BudgetSetting.class);
        return saveSetting(setting);
    }

    @Transactional(rollbackFor = Exception.class)
    public int saveBatch(List<BudgetSetting> settings) throws Exception {
        checkAccess();
        if (settings == null || settings.isEmpty()) {
            throw new IllegalArgumentException("预算设置不能为空");
        }
        int result = 0;
        for (BudgetSetting setting : settings) {
            result += saveSetting(setting);
        }
        return result;
    }

    private int saveSetting(BudgetSetting setting) throws Exception {
        if (setting.getBudgetYear() == null || setting.getOrganizationId() == null) {
            throw new IllegalArgumentException("预算年度和部门不能为空");
        }
        checkEditableYear(setting.getBudgetYear());
        if (setting.getBudgetAmount() == null || setting.getBudgetAmount().compareTo(BigDecimal.ZERO) < 0) {
            setting.setBudgetAmount(BigDecimal.ZERO);
        }
        setting.setTenantId(userService.getCurrentUser().getTenantId());
        BudgetSetting old = mapper.find(setting.getBudgetYear(), setting.getOrganizationId(), setting.getTenantId());
        return old == null ? mapper.insert(setting) : updateExisting(old, setting);
    }
    private int updateExisting(BudgetSetting old, BudgetSetting setting) {
        setting.setId(old.getId());
        return mapper.update(setting);
    }

    private void checkYearInRange(Integer year) {
        int currentYear = Year.now().getValue();
        if (year == null || year < 2026 || year > currentYear + 1) {
            throw new IllegalArgumentException("预算年度只能从2026年起选择，且最多选择下一年度");
        }
    }

    private void checkEditableYear(Integer year) {
        checkYearInRange(year);
        if (year < Year.now().getValue()) {
            throw new IllegalArgumentException("以前年度预算不可修改");
        }
    }
}
