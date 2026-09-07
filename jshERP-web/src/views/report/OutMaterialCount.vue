<!-- from 7 5 2 7 18920 -->
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
                  <span>出库总数量：{{numSumTotalStr}}，出库总金额：{{priceSumTotalStr}}</span>
                </a-form-item>
              </a-col>
            </a-row>
            <template v-if="toggleSearchStatus">
              <a-row :gutter="24">
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
                  <a-form-item label="商品类别" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-tree-select style="width:100%" :dropdownStyle="{maxHeight:'200px',overflow:'auto'}" allow-clear
                                   :treeData="categoryTree" v-model="queryParam.categoryId" placeholder="请选择商品类别">
                    </a-tree-select>
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
          </a-table>
          <a-row :gutter="24" style="margin-top: 8px;text-align:right;">
            <a-col :md="24" :sm="24">
              <a-pagination @change="paginationChange" @showSizeChange="paginationShowSizeChange"
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
      </a-card>
    </a-col>
  </a-row>
</template>
<script>
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { getFormatDate, getPrevMonthFormatDate } from '@/utils/util'
  import {getAction} from '@/api/manage'
  import {findBySelectOrgan, queryMaterialCategoryTreeList} from '@/api/api'
  import JEllipsis from '@/components/jeecg/JEllipsis'
  import moment from 'moment'
  import Vue from 'vue'
  export default {
    name: "OutMaterialCount",
    mixins:[JeecgListMixin],
    components: {
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
          materialParam:'',
          depotId: undefined,
          beginTime: getPrevMonthFormatDate(3),
          endTime: getFormatDate(),
          createTimeRange: [moment(getPrevMonthFormatDate(3)), moment(getFormatDate())],
          type: "出库",
        },
        ipagination:{
          pageSize: 11,
          pageSizeOptions: ['11', '21', '31', '101', '201']
        },
        organList: [],
        depotList: [],
        categoryTree:[],
        numSumTotalStr: '0',
        priceSumTotalStr: '0',
        setTimeFlag: null,
        roleCode: '',
        tabKey: "1",
        pageName: 'outMaterialCount',
        // 默认索引
        defDataIndex:['rowIndex','barCode','mName','standard','model','categoryName','materialUnit','branchGroup','officeGroup','summaryGroup'],
        // 默认列
        defColumns: [
          {
            dataIndex: 'rowIndex', width:40, align:"center", slots: { title: 'customTitle' },
            customRender:function (t,r,index) {
              return (r && r.rowIndex === '合计') ? t : (parseInt(index) + 1)
            }
          },
          {title: '条码', dataIndex: 'barCode', sorter: (a, b) => a.barCode - b.barCode, width: 120},
          {title: '名称', dataIndex: 'mName', width: 120, ellipsis:true},
          {title: '规格', dataIndex: 'standard', width: 100, ellipsis:true},
          {title: '型号', dataIndex: 'model', width: 100, ellipsis:true},
          {title: '颜色', dataIndex: 'color', width: 60, ellipsis:true},
          {title: '品牌', dataIndex: 'brand', width: 100, ellipsis:true},
          {title: '制造商', dataIndex: 'mfrs', width: 100, ellipsis:true},
          {title: '类别', dataIndex: 'categoryName', width: 120, ellipsis:true},
          {title: '单位', dataIndex: 'materialUnit', width: 120, ellipsis:true},
          {title: '分局', dataIndex: 'branchGroup', children: [
            {title: '数量', dataIndex: 'branchNumSum', width: 100, customRender: value => Number(value || 0)},
            {title: '单价', dataIndex: 'branchUnitPrice', width: 100, customRender: value => value === '' ? '' : Number(value || 0).toFixed(2)},
            {title: '金额', dataIndex: 'branchPriceSum', width: 110, customRender: value => Number(value || 0).toFixed(2)}
          ]},
          {title: '机关科室', dataIndex: 'officeGroup', children: [
            {title: '数量', dataIndex: 'officeNumSum', width: 100, customRender: value => Number(value || 0)},
            {title: '单价', dataIndex: 'officeUnitPrice', width: 100, customRender: value => value === '' ? '' : Number(value || 0).toFixed(2)},
            {title: '金额', dataIndex: 'officePriceSum', width: 110, customRender: value => Number(value || 0).toFixed(2)}
          ]},
          {title: '汇总', dataIndex: 'summaryGroup', children: [
            {title: '数量', dataIndex: 'summaryNumSum', width: 100, customRender: value => Number(value || 0)},
            {title: '单价', dataIndex: 'summaryUnitPrice', width: 100, customRender: value => value === '' ? '' : Number(value || 0).toFixed(2)},
            {title: '金额', dataIndex: 'summaryPriceSum', width: 110, customRender: value => Number(value || 0).toFixed(2)}
          ]}
        ],
        url: {
          list: "/depotHead/findInOutMaterialCount",
        }
      }
    },
    created () {
      this.getDepotData()
      this.initOrgan()
      this.loadCategoryTreeData()
      this.ensureGroupedColumnsSetting()
      this.initColumnsSetting()
      this.loadCurrentRoleCode()
    },
    methods: {
      moment,
      loadCurrentRoleCode() {
        getAction('/user/getRoleTypeByCurrentUser').then((res) => {
          if(res && res.code === 200) {
            this.roleCode = res.data.roleCode || ''
            if(this.roleCode === 'ROLE_DEPT') {
              this.updateColumnTitle('numSum', '领用数量')
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
      getQueryParams() {
        let param = Object.assign({}, this.queryParam, this.isorter);
        param.field = this.getQueryField();
        param.currentPage = this.ipagination.current;
        param.pageSize = this.ipagination.pageSize-1;
        return param;
      },
      ensureGroupedColumnsSetting() {
        const required = ['branchGroup', 'officeGroup', 'summaryGroup']
        const stored = Vue.ls.get(this.pageName)
        if (stored && stored.indexOf(',') > -1) {
          const keys = stored.split(',')
          required.forEach(key => { if (keys.indexOf(key) === -1) keys.push(key) })
          Vue.ls.set(this.pageName, keys.join(','))
        }
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
            this.dataSource = (res.data.rows || []).map(row => this.normalizeGroupValues(row));
            this.ipagination.total = res.data.total;
            this.numSumTotalStr = Number(res.data.numSumTotal || 0).toFixed(2)
            this.priceSumTotalStr = Number(res.data.priceSumTotal || 0).toFixed(2)
            this.appendGroupedTotalRow()
          } else if(res.code===510){
            this.$message.warning(res.data)
          } else {
            this.$message.warning(res.data.message)
          }
          this.loading = false;
        })
      },
      normalizeGroupValues(row) {
        const groups = [
          ['branchNumSum', 'branchPriceSum', 'branchUnitPrice'],
          ['officeNumSum', 'officePriceSum', 'officeUnitPrice'],
          ['summaryNumSum', 'summaryPriceSum', 'summaryUnitPrice']
        ]
        groups.forEach(([numberKey, amountKey, priceKey]) => {
          const quantity = Number(row[numberKey] || 0)
          const amount = Number(row[amountKey] || 0)
          row[numberKey] = quantity
          row[amountKey] = amount
          row[priceKey] = quantity ? amount / quantity : ''
        })
        return row
      },
      appendGroupedTotalRow() {
        if (!this.dataSource.length || this.ipagination.pageSize % 10 !== 1) return
        const totalRow = { rowIndex: '合计', mName: '' }
        ;['branch', 'office', 'summary'].forEach(prefix => {
          const numberKey = `${prefix}NumSum`
          const amountKey = `${prefix}PriceSum`
          const priceKey = `${prefix}UnitPrice`
          const quantity = this.dataSource.reduce((sum, row) => sum + Number(row[numberKey] || 0), 0)
          const amount = this.dataSource.reduce((sum, row) => sum + Number(row[amountKey] || 0), 0)
          totalRow[numberKey] = quantity
          totalRow[amountKey] = amount
          totalRow[priceKey] = 0
        })
        this.dataSource.push(totalRow)
        this.ipagination.total += Math.ceil(this.ipagination.total / (this.ipagination.pageSize - 1))
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
      searchQuery() {
        if(this.queryParam.beginTime == '' || this.queryParam.endTime == ''){
          this.$message.warning('请选择单据日期！')
        } else {
          this.loadData(1);
        }
      },
      exportExcel() {
        let list = []
        let quantityTitle = this.roleCode === 'ROLE_DEPT' ? '领用数量' : '出库数量'
        let head = '条码,名称,规格,型号,颜色,品牌,制造商,类型,单位,分局,,,' +
          '机关科室,,,' + '汇总,,,' + '\n,,,,,,,,,数量,单价,金额,数量,单价,金额,数量,单价,金额'
        for (let i = 0; i < this.dataSource.length; i++) {
          let item = []
          let ds = this.dataSource[i]
          if (ds.rowIndex === '合计') {
            item.push('', '合计', '', '', '', '', '', '', '')
          } else {
            item.push(ds.barCode, ds.mName, ds.standard, ds.model, ds.color, ds.brand, ds.mfrs,
              ds.categoryName, ds.materialUnit)
          }
          ;['branch', 'office', 'summary'].forEach(prefix => {
            item.push(ds[`${prefix}NumSum`] || 0, ds[`${prefix}UnitPrice`] === '' ? '' : Number(ds[`${prefix}UnitPrice`] || 0).toFixed(2), ds[`${prefix}PriceSum`] || 0)
          })
          list.push(item)
        }
        let tip = '单据日期：' + this.queryParam.beginTime + '~' + this.queryParam.endTime
        this.handleExportXlsPost('出库汇总', '出库汇总', head, tip, list)
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
