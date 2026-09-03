<!-- from 7 5 2 71 8 9 2 0 -->
<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <!-- 查询区域 -->
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="searchQuery">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="商品信息" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入条码、名称、助记码、规格、型号等信息" v-model="queryParam.materialParam"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="单据日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-range-picker
                    style="width: 100%"
                    v-model="queryParam.createTimeRange"
                    format="YYYY-MM-DD"
                    :placeholder="['开始时间', '结束时间']"
                    @change="onDateChange"
                  />
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24" >
                <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                  <a-button type="primary" @click="searchQuery">查询</a-button>
                  <a-button style="margin-left: 8px" v-print="'#reportPrint'" icon="printer">打印</a-button>
                  <a-button style="margin-left: 8px" @click="exportExcel" icon="download">导出</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? '收起' : '展开' }}
                    <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
                  </a>
                </span>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item>
                  <span>总数量：{{operNumberTotalStr}}，总金额：{{allPriceTotalStr}}</span>
                </a-form-item>
              </a-col>
            </a-row>
            <template v-if="toggleSearchStatus">
              <a-row :gutter="24">
                <a-col :md="6" :sm="24">
                  <a-form-item label="单据编号" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-input placeholder="请输入单据编号" v-model="queryParam.number"></a-input>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="往来单位" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择往来单位" v-model="queryParam.organId"
                              :dropdownMatchSelectWidth="false" showSearch allow-clear optionFilterProp="children" @search="handleSearchOrgan">
                      <div slot="dropdownRender" slot-scope="menu">
                        <v-nodes :vnodes="menu" />
                        <a-divider style="margin: 4px 0;" />
                        <div class="dropdown-btn" @mousedown="e => e.preventDefault()" @click="initOrgan"><a-icon type="reload" /> 刷新列表</div>
                      </div>
                      <a-select-option v-for="(item,index) in organList" :key="index" :value="item.id">
                        {{ item.supplier }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="仓库" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select
                      optionFilterProp="children"
                      :dropdownMatchSelectWidth="false"
                      showSearch allow-clear style="width: 100%"
                      placeholder="请选择仓库"
                      v-model="queryParam.depotId">
                      <a-select-option v-for="(depot,index) in depotList" :value="depot.id" :key="index">
                        {{ depot.depotName }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="操作员" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择操作员" showSearch allow-clear optionFilterProp="children" v-model="queryParam.creator">
                      <a-select-option v-for="(item,index) in userList" :key="index" :value="item.id">
                        {{ item.userName }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24" v-if="orgaTree.length">
                  <a-form-item label="部门" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-tree-select style="width:100%" allow-clear :treeData="orgaTree"
                                   v-model="queryParam.organizationId" placeholder="请选择部门">
                    </a-tree-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="商品类别" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-tree-select style="width:100%" :dropdownStyle="{maxHeight:'200px',overflow:'auto'}" allow-clear
                                   :treeData="categoryTree" v-model="queryParam.categoryId" placeholder="请选择商品类别">
                    </a-tree-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-input placeholder="请输入备注" v-model="queryParam.remark"></a-input>
                  </a-form-item>
                </a-col>
              </a-row>
            </template>
          </a-form>
        </div>
        <!-- table区域-begin -->
        <section ref="print" id="reportPrint">
          <a-table
            bordered
            ref="table"
            size="middle"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :components="handleDrag(columns)"
            :pagination="false"
            :scroll="scroll"
            :loading="loading"
            @change="handleTableChange">
            <span slot="customTitle">
              <a-popover trigger="click" placement="right">
                <template slot="content">
                  <a-checkbox-group @change="onColChange" v-model="settingDataIndex" :defaultValue="settingDataIndex">
                    <a-row style="width: 600px">
                      <template v-for="(item,index) in defColumns">
                        <template>
                          <a-col :span="6">
                            <a-checkbox :value="item.dataIndex" v-if="item.dataIndex==='rowIndex'" disabled></a-checkbox>
                            <a-checkbox :value="item.dataIndex" v-if="item.dataIndex!=='rowIndex'">
                              <j-ellipsis :value="item.title" :length="10"></j-ellipsis>
                            </a-checkbox>
                          </a-col>
                        </template>
                      </template>
                    </a-row>
                    <a-row style="padding-top: 10px;">
                      <a-col>
                        恢复默认列配置：<a-button @click="handleRestDefault" type="link" size="small">恢复默认</a-button>
                      </a-col>
                    </a-row>
                  </a-checkbox-group>
                </template>
                <a-icon type="setting" />
              </a-popover>
            </span>
            <span slot="numberCustomRender" slot-scope="text, record">
              <a @click="myHandleDetail(record)">{{record.number}}</a>
            </span>
          </a-table>
          <a-row :gutter="24" style="margin-top: 8px;text-align:right;">
            <a-col :md="24" :sm="24">
          <a-pagination v-if="false" @change="paginationChange" @showSizeChange="paginationShowSizeChange"
                size="small"
                show-size-changer
                :showQuickJumper="true"
                :current="ipagination.current"
                :page-size="ipagination.pageSize"
                :page-size-options="ipagination.pageSizeOptions"
                :total="ipagination.total"
                :show-total="(total, range) => `共 ${total-Math.ceil(total/ipagination.pageSize)} 条`">
                <template slot="buildOptionText" slot-scope="props">
                  <span>{{ props.value-1 }}条/页</span>
                </template>
              </a-pagination>
            </a-col>
          </a-row>
        </section>
        <!-- table区域-end -->
        <!-- 表单区域 -->
        <bill-detail ref="modalDetail"></bill-detail>
      </a-card>
    </a-col>
  </a-row>
</template>
<script>
  import BillDetail from '../bill/dialog/BillDetail'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { getFormatDate, getPrevMonthFormatDate } from '@/utils/util'
  import {getAction} from '@/api/manage'
  import {findBySelectOrgan, findBillDetailByNumber, getUserList, queryMaterialCategoryTreeList, getAllOrganizationTreeByUser} from '@/api/api'
  import JEllipsis from '@/components/jeecg/JEllipsis'
  import moment from 'moment'
  import Vue from 'vue'
  export default {
    name: "OutDetail",
    mixins:[JeecgListMixin],
    components: {
      BillDetail,
      JEllipsis,
      VNodes: {
        functional: true,
        render: (h, ctx) => ctx.props.vnodes,
      }
    },
    data () {
      return {
        labelCol: {
          span: 5
        },
        wrapperCol: {
          span: 18,
          offset: 1
        },
        // 查询条件
        queryParam: {
          organId: undefined,
          number: '',
          materialParam:'',
          depotId: undefined,
          beginTime: getPrevMonthFormatDate(3),
          endTime: getFormatDate(),
          createTimeRange: [moment(getPrevMonthFormatDate(3)), moment(getFormatDate())],
          type: "出库",
          creator: undefined,
          organizationId: undefined,
          remark: ''
        },
        ipagination:{
          pageSize: 11,
          pageSizeOptions: ['11', '21', '31', '101', '201', '301', '1001', '2001', '3001']
        },
        organList: [],
        depotList: [],
        userList: [],
        orgaTree: [],
        categoryTree:[],
        departmentNames: [],
        operNumberTotalStr: '0',
        allPriceTotalStr: '0',
        setTimeFlag: null,
        roleCode: '',
        tabKey: "1",
        pageName: 'outDetail',
        // 默认索引
        defDataIndex:['rowIndex','mname','mUnit'],
        // 默认列
        defColumns: [
          {
            dataIndex: 'rowIndex', width:40, align:"center", slots: { title: 'customTitle' },
            customRender:function (t,r,index) {
              return (t !== '合计') ? (parseInt(index) + 1) : t
            }
          },
          {title: '名称', dataIndex: 'mname', width: 120, ellipsis:true},
          {title: '单位', dataIndex: 'mUnit', width: 50, ellipsis:true}
        ],
        url: {
          list: "/depotHead/findInOutDetail",
        }
      }
    },
    created () {
      this.getDepotData()
      this.initOrgan()
      this.initUser()
      this.loadAllOrgaData()
      this.loadCategoryTreeData()
      this.initColumnsSetting()
      this.migrateDepartmentColumnSetting()
      this.loadCurrentRoleCode()
    },
    methods: {
      moment,
      loadCurrentRoleCode() {
        getAction('/user/getRoleTypeByCurrentUser').then((res) => {
          if(res && res.code === 200) {
            this.roleCode = res.data.roleCode || ''
            if(this.roleCode === 'ROLE_DEPT') {
              this.updateColumnTitle('operTime', '领用日期')
            }
          }
        })
      },
      updateColumnTitle(dataIndex, title) {
        const defaultColumn = this.defColumns.find(item => item.dataIndex === dataIndex)
        const currentColumn = this.columns.find(item => item.dataIndex === dataIndex)
        if(defaultColumn) defaultColumn.title = title
        if(currentColumn) currentColumn.title = title
      },
      migrateDepartmentColumnSetting() {
        const columnsStr = Vue.ls.get(this.pageName)
        if(columnsStr && columnsStr.indexOf(',') > -1) {
          const columns = columnsStr.split(',')
          if(columns.indexOf('issueDepartment') === -1) {
            columns.splice(2, 0, 'issueDepartment')
            Vue.ls.set(this.pageName, columns.join())
            this.initColumnsSetting()
          }
        }
      },
      getQueryParams() {
        let param = Object.assign({}, this.queryParam, this.isorter);
        param.field = this.getQueryField();
        param.currentPage = 1;
        param.pageSize = 100000;
        return param;
      },
      onDateChange: function (value, dateString) {
        this.queryParam.beginTime=dateString[0]
        this.queryParam.endTime=dateString[1]
        if(dateString[0] && dateString[1]) {
          this.queryParam.createTimeRange = [moment(dateString[0]), moment(dateString[1])]
        }
      },
      loadData(arg) {
        if (arg === 1) {
          this.ipagination.current = 1;
        }
        let params = this.getQueryParams();//查询条件
        this.loading = true;
        getAction(this.url.list, params).then((res) => {
          if (res.code===200) {
            const matrix = this.buildDepartmentMatrix(res.data.rows || [])
            this.columns = matrix.columns
            this.departmentNames = matrix.departmentNames
            this.dataSource = matrix.rows
            this.ipagination.total = matrix.rows.length
            this.operNumberTotalStr = res.data.operNumberTotal.toFixed(2)
            this.allPriceTotalStr = res.data.allPriceTotal.toFixed(2)
          } else if(res.code===510){
            this.$message.warning(res.data)
          } else {
            this.$message.warning(res.data.message)
          }
          this.loading = false;
        })
      },
      buildDepartmentMatrix(details) {
        const departmentNames = []
        const departmentIndex = {}
        details.forEach(item => {
          const name = item.issueDepartment || '未分配部门'
          if(departmentIndex[name] === undefined) {
            departmentIndex[name] = departmentNames.length
            departmentNames.push(name)
          }
        })
        const baseColumns = [
          {title: '#', dataIndex: 'rowIndex', width: 40, align: 'center'},
          {title: '名称', dataIndex: 'mname', width: 140},
          {title: '单位', dataIndex: 'mUnit', width: 60}
        ]
        const departmentColumns = departmentNames.map((name, index) => ({
          title: name,
          children: [
            {title: '数量', dataIndex: `dept_${index}_number`, width: 80},
            {title: '单价', dataIndex: `dept_${index}_price`, width: 80},
            {title: '金额', dataIndex: `dept_${index}_amount`, width: 90}
          ]
        }))
        const rowMap = {}
        details.forEach(item => {
          const materialKey = item.materialId != null
            ? String(item.materialId)
            : [item.barCode || '', item.sku || '', item.mUnit || ''].join('|')
          if(!rowMap[materialKey]) {
            rowMap[materialKey] = {
              materialId: item.materialId,
              barCode: item.barCode,
              mname: item.mname,
              standard: item.standard,
              model: item.model,
              color: item.color,
              brand: item.brand,
              mfrs: item.mfrs,
              sku: item.sku,
              mUnit: item.mUnit
            }
          }
          const index = departmentIndex[item.issueDepartment || '未分配部门']
          const numberKey = `dept_${index}_number`
          const priceKey = `dept_${index}_price`
          const amountKey = `dept_${index}_amount`
          const row = rowMap[materialKey]
          row[numberKey] = (row[numberKey] || 0) + Number(item.operNumber || 0)
          row[amountKey] = (row[amountKey] || 0) + Number(item.allPrice || 0)
          row[priceKey] = row[numberKey] ? Number((row[amountKey] / row[numberKey]).toFixed(4)) : 0
        })
        const rows = Object.keys(rowMap).map((key, index) => Object.assign({id: key, rowIndex: index + 1}, rowMap[key]))
        return {columns: baseColumns.concat(departmentColumns), rows, departmentNames}
      },
      initOrgan() {
        let that = this;
        findBySelectOrgan({limit:1}).then((res)=>{
          if(res) {
            that.organList = res;
          }
        });
      },
      handleSearchOrgan(value) {
        let that = this
        if(this.setTimeFlag != null){
          clearTimeout(this.setTimeFlag);
        }
        this.setTimeFlag = setTimeout(()=>{
          findBySelectOrgan({key: value, limit:1}).then((res) => {
            if(res) {
              that.organList = res;
            }
          })
        },500)
      },
      getDepotData() {
        getAction('/depot/findDepotByCurrentUser').then((res)=>{
          if(res.code === 200){
            this.depotList = res.data;
          }else{
            this.$message.info(res.data);
          }
        })
      },
      initUser() {
        getUserList({}).then((res)=>{
          if(res) {
            this.userList = res;
          }
        });
      },
      loadAllOrgaData(){
        let that = this
        let params = {}
        getAllOrganizationTreeByUser(params).then((res)=>{
          if(res){
            that.orgaTree = res
          }
        })
      },
      loadCategoryTreeData(){
        let that = this;
        let params = {};
        params.id='';
        queryMaterialCategoryTreeList(params).then((res)=>{
          if(res){
            that.categoryTree = [];
            for (let i = 0; i < res.length; i++) {
              let temp = res[i];
              that.categoryTree.push(temp);
            }
          }
        })
      },
      myHandleDetail(record) {
        findBillDetailByNumber({ number: record.number }).then((res) => {
          if (res && res.code === 200) {
            this.$refs.modalDetail.isCanBackCheck = false
            this.handleDetail(res.data, record.newType)
          }
        })
      },
      searchQuery() {
        if(this.queryParam.beginTime == '' || this.queryParam.endTime == ''){
          this.$message.warning('请选择单据日期！')
        } else {
          this.loadData(1);
        }
      },
      exportExcel() {
        const allDepartments = []
        const collectDepartments = nodes => {
          ;(nodes || []).forEach(node => {
            if (node.title && allDepartments.indexOf(node.title) === -1) allDepartments.push(node.title)
            collectDepartments(node.children)
          })
        }
        collectDepartments(this.orgaTree)
        const departments = allDepartments.length ? allDepartments : this.departmentNames.slice()
        // 导出采用两行表头：第一行部门名称，第二行为数量、单价、金额。
        const headParts = ['序号', '名称', '单位']
        departments.forEach(name => { headParts.push(String(name).replace(/,/g, '，'), '', '') })
        const subHead = ['', '', '']
        departments.forEach(() => { subHead.push('数量', '单价', '金额') })
        const head = headParts.join(',') + '\n' + subHead.join(',')
        const detailMap = {}
        this.dataSource.forEach(ds => {
          if (ds.materialId == null) return
          const key = String(ds.materialId)
          if (!detailMap[key]) detailMap[key] = {}
          const target = detailMap[key]
          this.departmentNames.forEach((name, departmentIndex) => {
            const numberKey = `dept_${departmentIndex}_number`
            const priceKey = `dept_${departmentIndex}_price`
            const amountKey = `dept_${departmentIndex}_amount`
            target[numberKey] = Number(target[numberKey] || 0) + Number(ds[numberKey] || 0)
            target[amountKey] = Number(target[amountKey] || 0) + Number(ds[amountKey] || 0)
            target[priceKey] = target[numberKey] ? Number((target[amountKey] / target[numberKey]).toFixed(4)) : 0
          })
        })
        const search = {}
        getAction('/material/list', {
          search: JSON.stringify(search),
          currentPage: 1,
          pageSize: 100000
        }).then(res => {
          if (!res || res.code !== 200) {
            this.$message.error('物品信息加载失败，导出取消')
            return
          }
          const list = (res.data.rows || []).map((material, index) => {
            const ds = detailMap[String(material.id)] || {}
            const row = [index + 1, material.name || '', material.unit || material.unitName || '']
            departments.forEach(name => {
              const departmentIndex = this.departmentNames.indexOf(name)
              row.push(
                departmentIndex === -1 ? 0 : Number(ds[`dept_${departmentIndex}_number`] || 0),
                departmentIndex === -1 ? 0 : Number(ds[`dept_${departmentIndex}_price`] || 0),
                departmentIndex === -1 ? 0 : Number(ds[`dept_${departmentIndex}_amount`] || 0)
              )
            })
            return row
          })
          const tip = '单据日期：' + this.queryParam.beginTime + '~' + this.queryParam.endTime
          this.handleExportXlsPost('出库明细', '出库明细', head, tip, list)
        }).catch(() => {
          this.$message.error('物品信息加载失败，导出取消')
        })
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
