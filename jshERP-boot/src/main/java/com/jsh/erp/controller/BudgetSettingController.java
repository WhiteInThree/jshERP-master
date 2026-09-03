package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.entities.BudgetSetting;
import com.jsh.erp.service.BudgetSettingService;
import com.jsh.erp.utils.BaseResponseInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/budget")
public class BudgetSettingController {
    @Resource private BudgetSettingService service;

    @GetMapping("/list")
    public BaseResponseInfo list(@RequestParam Integer year,
                                 @RequestParam(required = false) String name) {
        BaseResponseInfo res = new BaseResponseInfo();
        try { res.code = 200; res.data = service.list(year, name); }
        catch (Exception e) { res.code = 500; res.data = e.getMessage(); }
        return res;
    }

    @PostMapping("/save")
    public BaseResponseInfo save(@RequestBody JSONObject obj, HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        try { res.code = service.save(obj, request) > 0 ? 200 : 500; res.data = "保存成功"; }
        catch (Exception e) { res.code = 500; res.data = e.getMessage(); }
        return res;
    }

    @PostMapping("/saveBatch")
    public BaseResponseInfo saveBatch(@RequestBody List<BudgetSetting> settings) {
        BaseResponseInfo res = new BaseResponseInfo();
        try { res.code = service.saveBatch(settings) > 0 ? 200 : 500; res.data = "保存成功"; }
        catch (Exception e) { res.code = 500; res.data = e.getMessage(); }
        return res;
    }

    @GetMapping("/report")
    public BaseResponseInfo report(@RequestParam Integer year) {
        BaseResponseInfo res = new BaseResponseInfo();
        try { res.code = 200; res.data = service.report(year); }
        catch (Exception e) { res.code = 500; res.data = e.getMessage(); }
        return res;
    }

}
