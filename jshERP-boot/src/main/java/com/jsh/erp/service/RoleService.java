package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.Role;
import com.jsh.erp.datasource.entities.RoleEx;
import com.jsh.erp.datasource.entities.RoleExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.RoleMapper;
import com.jsh.erp.datasource.mappers.RoleMapperEx;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RoleService {
    private Logger logger = LoggerFactory.getLogger(RoleService.class);
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMapperEx roleMapperEx;
    @Resource
    private LogService logService;
    @Resource
    private UserService userService;

    //超管的专用角色
    private static Long MANAGE_ROLE_ID = 4L;

    public Role getRole(long id)throws Exception {
        Role result=null;
        try{
            result=roleMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Role> getRoleListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<Role> list = new ArrayList<>();
        try{
            RoleExample example = new RoleExample();
            example.createCriteria().andIdIn(idList);
            list = roleMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Role> allList()throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andEnabledEqualTo(true).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Role> list=null;
        try{
            list=roleMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Role> tenantRoleList() {
        List<Role> list=null;
        try{
            if(BusinessConstants.DEFAULT_MANAGER.equals(userService.getCurrentUser().getLoginName())) {
                RoleExample example = new RoleExample();
                example.createCriteria().andEnabledEqualTo(true).andTenantIdIsNull().andIdNotEqualTo(MANAGE_ROLE_ID)
                        .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                example.setOrderByClause("sort asc, id asc");
                list=roleMapper.selectByExample(example);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<RoleEx> select(String name, String description)throws Exception {
        List<RoleEx> list=null;
        try{
            PageUtils.startPage();
            list=roleMapperEx.selectByConditionRole(name, description);
            for(RoleEx roleEx: list) {
                String priceLimit = roleEx.getPriceLimit();
                if(StringUtil.isNotEmpty(priceLimit)) {
                    String priceLimitStr = priceLimit
                        .replace("1", "屏蔽首页采购价")
                        .replace("2", "屏蔽首页零售价")
                        .replace("3", "屏蔽首页销售价")
                        .replace("4", "屏蔽单据采购价")
                        .replace("5", "屏蔽单据零售价")
                        .replace("6", "屏蔽单据销售价")
                        .replace("7", "屏蔽库存成本价");
                    roleEx.setPriceLimitStr(priceLimitStr);
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertRole(JSONObject obj, HttpServletRequest request)throws Exception {
        Role role = JSONObject.parseObject(obj.toJSONString(), Role.class);
        validateRoleCodeChange(null, role.getValue(), obj.containsKey("value"));
        int result=0;
        try{
            role.setEnabled(true);
            result=roleMapper.insertSelective(role);
            logService.insertLog("角色",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(role.getName()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateRole(JSONObject obj, HttpServletRequest request) throws Exception{
        Role role = JSONObject.parseObject(obj.toJSONString(), Role.class);
        Role oldRole = role.getId() == null ? null : getRole(role.getId());
        validateRoleCodeChange(oldRole, role.getValue(), obj.containsKey("value"));
        int result=0;
        try{
            result=roleMapper.updateByPrimaryKeySelective(role);
            logService.insertLog("角色",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(role.getName()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteRole(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteRoleByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteRole(String ids, HttpServletRequest request) throws Exception{
        return batchDeleteRoleByIds(ids);
    }

    public int checkIsNameExist(Long id, String name) throws Exception{
        RoleExample example = new RoleExample();
        example.createCriteria().andIdNotEqualTo(id).andNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Role> list =null;
        try{
            list=roleMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<Role> findUserRole()throws Exception{
        RoleExample example = new RoleExample();
        example.setOrderByClause("Id");
        example.createCriteria().andEnabledEqualTo(true).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Role> list=null;
        try{
            list=roleMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }
    /**
     * create by: qiankunpingtai
     *  逻辑删除角色信息
     * create time: 2019/3/28 15:44
     * @Param: ids
     * @return int
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteRoleByIds(String ids) throws Exception{
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<Role> list = getRoleListByIds(ids);
        for(Role role: list){
            checkOfficeCannotOperateAdminRole(role);
            sb.append("[").append(role.getName()).append("]");
        }
        logService.insertLog("角色", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        try{
            result=roleMapperEx.batchDeleteRoleByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public Role getRoleWithoutTenant(Long roleId) {
        return roleMapperEx.getRoleWithoutTenant(roleId);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(Boolean status, String ids)throws Exception {
        logService.insertLog("角色",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ENABLED).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        List<Long> roleIds = StringUtil.strToLongList(ids);
        List<Role> roles = getRoleListByIds(ids);
        for(Role item : roles) {
            checkOfficeCannotOperateAdminRole(item);
        }
        Role role = new Role();
        role.setEnabled(status);
        RoleExample example = new RoleExample();
        example.createCriteria().andIdIn(roleIds);
        int result=0;
        try{
            result = roleMapper.updateByExampleSelective(role, example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    private void validateRoleCodeChange(Role oldRole, String newRoleCode, boolean containsRoleCode) throws Exception {
        Role operatorRole = getCurrentOperatorRole();
        String operatorRoleCode = operatorRole == null ? null : operatorRole.getValue();
        boolean isAdmin = BusinessConstants.ROLE_CODE_ADMIN.equals(operatorRoleCode);
        boolean isOffice = BusinessConstants.ROLE_CODE_OFFICE.equals(operatorRoleCode);

        if(isOffice && oldRole != null
                && BusinessConstants.ROLE_CODE_ADMIN.equals(oldRole.getValue())) {
            throw new BusinessRunTimeException(ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_CODE,
                    ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_MSG);
        }
        if(!containsRoleCode) {
            return;
        }

        String normalizedRoleCode = StringUtil.isEmpty(newRoleCode) ? null : newRoleCode.trim();
        String oldRoleCode = oldRole == null || StringUtil.isEmpty(oldRole.getValue())
                ? null : oldRole.getValue().trim();
        if(!Objects.equals(oldRoleCode, normalizedRoleCode) && !isAdmin && !isOffice) {
            throw new BusinessRunTimeException(ExceptionConstants.ROLE_CODE_OPERATION_FORBIDDEN_CODE,
                    ExceptionConstants.ROLE_CODE_OPERATION_FORBIDDEN_MSG);
        }
        if(normalizedRoleCode != null
                && !BusinessConstants.ROLE_CODE_ADMIN.equals(normalizedRoleCode)
                && !BusinessConstants.ROLE_CODE_OFFICE.equals(normalizedRoleCode)
                && !BusinessConstants.ROLE_CODE_DEPT.equals(normalizedRoleCode)) {
            throw new BusinessRunTimeException(ExceptionConstants.ROLE_CODE_INVALID_CODE,
                    ExceptionConstants.ROLE_CODE_INVALID_MSG);
        }
        if(isOffice && BusinessConstants.ROLE_CODE_ADMIN.equals(normalizedRoleCode)) {
            throw new BusinessRunTimeException(ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_CODE,
                    ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_MSG);
        }
    }

    private void checkOfficeCannotOperateAdminRole(Role targetRole) throws Exception {
        Role operatorRole = getCurrentOperatorRole();
        if(operatorRole != null && BusinessConstants.ROLE_CODE_OFFICE.equals(operatorRole.getValue())
                && targetRole != null && BusinessConstants.ROLE_CODE_ADMIN.equals(targetRole.getValue())) {
            throw new BusinessRunTimeException(ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_CODE,
                    ExceptionConstants.ROLE_ADMIN_OPERATION_FORBIDDEN_MSG);
        }
    }

    private Role getCurrentOperatorRole() throws Exception {
        User operator = userService.getCurrentUser();
        return operator == null || operator.getId() == null
                ? null : userService.getRoleTypeByUserId(operator.getId());
    }

    /**
     * 根据权限进行屏蔽价格-首页
     * @param price
     * @param type
     * @return
     */
    public Object parseHomePriceByLimit(BigDecimal price, String type, String priceLimit, String emptyInfo, HttpServletRequest request) throws Exception {
        if(StringUtil.isNotEmpty(priceLimit)) {
            if("buy".equals(type) && priceLimit.contains("1")) {
                return emptyInfo;
            }
            if("retail".equals(type) && priceLimit.contains("2")) {
                return emptyInfo;
            }
            if("sale".equals(type) && priceLimit.contains("3")) {
                return emptyInfo;
            }
        }
        return price;
    }

    /**
     * 根据权限进行屏蔽价格-单据
     * @param price
     * @param billCategory
     * @param priceLimit
     * @param request
     * @return
     * @throws Exception
     */
    public BigDecimal parseBillPriceByLimit(BigDecimal price, String billCategory, String priceLimit, HttpServletRequest request) throws Exception {
        if(StringUtil.isNotEmpty(priceLimit)) {
            if("buy".equals(billCategory) && priceLimit.contains("4")) {
                return BigDecimal.ZERO;
            }
            if("retail".equals(billCategory) && priceLimit.contains("5")) {
                return BigDecimal.ZERO;
            }
            if("sale".equals(billCategory) && priceLimit.contains("6")) {
                return BigDecimal.ZERO;
            }
        }
        return price;
    }

    /**
     * 根据权限进行屏蔽成本价-库存报表
     * @param price
     * @param priceLimit
     * @param request
     * @return
     * @throws Exception
     */
    public BigDecimal parseStockPriceByLimit(BigDecimal price, String priceLimit, HttpServletRequest request) throws Exception {
        if(StringUtil.isNotEmpty(priceLimit)) {
            if(priceLimit.contains("7")) {
                return null;
            }
        }
        return price;
    }

    /**
     * 根据权限进行屏蔽价格-物料
     * @param price
     * @param type
     * @return
     */
    public Object parseMaterialPriceByLimit(BigDecimal price, String type, String emptyInfo, HttpServletRequest request) throws Exception {
        Long userId = userService.getUserId(request);
        String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
        if(StringUtil.isNotEmpty(priceLimit)) {
            if("buy".equals(type) && priceLimit.contains("4")) {
                return emptyInfo;
            }
            if("retail".equals(type) && priceLimit.contains("5")) {
                return emptyInfo;
            }
            if("sale".equals(type) && priceLimit.contains("6")) {
                return emptyInfo;
            }
        }
        return price;
    }

    public String getCurrentPriceLimit(HttpServletRequest request) throws Exception {
        Long userId = userService.getUserId(request);
        return userService.getRoleTypeByUserId(userId).getPriceLimit();
    }
}
