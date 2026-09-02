package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.BudgetSetting;
import com.jsh.erp.datasource.vo.BudgetSettingVo;
import com.jsh.erp.datasource.vo.BudgetMonthlyExpenseVo;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BudgetSettingMapper {
    @Select("select b.id,b.budget_year budgetYear,o.id organizationId,o.org_abr organizationName," +
            "ifnull(b.budget_amount,0) budgetAmount,ifnull(u.used_amount,0) usedAmount," +
            "(ifnull(b.budget_amount,0)-ifnull(u.used_amount,0)) availableAmount " +
            "from jsh_organization o left join jsh_budget_setting b on b.organization_id=o.id and b.budget_year=#{year} and b.tenant_id=#{tenantId} and ifnull(b.delete_flag,'0')!='1' " +
            "left join (select a.organ_id organization_id,sum(ifnull(di.all_price,0)) used_amount from jsh_depot_head dh " +
            "join jsh_depot_item di on di.header_id=dh.id and ifnull(di.delete_flag,'0')!='1' " +
            "join jsh_depot_head a on a.number=dh.link_number and a.type='其它' and a.sub_type='请购单' and a.status='4' and ifnull(a.delete_flag,'0')!='1' " +
            "where dh.type='出库' and dh.sub_type='其它' and ifnull(dh.delete_flag,'0')!='1' " +
            "and dh.tenant_id=#{tenantId} and year(dh.oper_time)=#{year} group by a.organ_id) u on u.organization_id=o.id " +
            "where o.tenant_id=#{tenantId} and ifnull(o.delete_flag,'0')!='1' and o.parent_id is not null " +
            "and (#{name} is null or #{name}='' or o.org_abr like concat('%',#{name},'%')) order by o.sort,o.id")
    List<BudgetSettingVo> list(@Param("year") Integer year, @Param("tenantId") Long tenantId, @Param("name") String name);

    @Select("select * from jsh_budget_setting where budget_year=#{year} and organization_id=#{organizationId} and tenant_id=#{tenantId} and ifnull(delete_flag,'0')!='1' limit 1")
    BudgetSetting find(@Param("year") Integer year, @Param("organizationId") Long organizationId, @Param("tenantId") Long tenantId);

    @Insert("insert into jsh_budget_setting(budget_year,organization_id,budget_amount,tenant_id,delete_flag,create_time,update_time) values(#{budgetYear},#{organizationId},#{budgetAmount},#{tenantId},'0',now(),now())")
    int insert(BudgetSetting setting);

    @Update("update jsh_budget_setting set budget_amount=#{budgetAmount},update_time=now() where id=#{id} and tenant_id=#{tenantId}")
    int update(BudgetSetting setting);

    @Select("select a.organ_id organizationId,month(dh.oper_time) expenseMonth,sum(ifnull(di.all_price,0)) expenseAmount " +
            "from jsh_depot_head dh join jsh_depot_item di on di.header_id=dh.id and ifnull(di.delete_flag,'0')!='1' " +
            "join jsh_depot_head a on a.number=dh.link_number and a.type='其它' and a.sub_type='请购单' and a.status='4' and ifnull(a.delete_flag,'0')!='1' " +
            "where dh.type='出库' and dh.sub_type='其它' and ifnull(dh.delete_flag,'0')!='1' " +
            "and dh.tenant_id=#{tenantId} and a.tenant_id=#{tenantId} and year(dh.oper_time)=#{year} " +
            "group by a.organ_id,month(dh.oper_time)")
    List<BudgetMonthlyExpenseVo> monthlyExpenses(@Param("year") Integer year, @Param("tenantId") Long tenantId);

    @Select("select id from jsh_organization where org_abr=#{name} and tenant_id=#{tenantId} and ifnull(delete_flag,'0')!='1' limit 1")
    Long findOrganizationId(@Param("name") String name, @Param("tenantId") Long tenantId);
}
