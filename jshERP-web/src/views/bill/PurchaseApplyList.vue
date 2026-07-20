<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <!-- 查询区域 -->
        <div class="table-page-search-wrapper">
          <!-- 搜索区域 -->
          <a-form layout="inline" @keyup.enter.native="searchQuery">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="单据编号" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入单据编号" v-model="queryParam.number"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="物品信息" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入条码、名称、助记码、规格、型号等信息" v-model="queryParam.materialParam"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="单据日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-range-picker
                    style="width:100%"
                    v-model="queryParam.createTimeRange"
                    format="YYYY-MM-DD"
                    :placeholder="['开始时间', '结束时间']"
                    @change="onDateChange"
                    @ok="onDateOk"
                  />
                </a-form-item>
              </a-col>
              <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                <a-col :md="6" :sm="24">
                  <a-button type="primary" @click="searchQuery">查询</a-button>
                  <a-button style="margin-left: 8px" @click="searchReset">重置</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? '收起' : '展开' }}
                    <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
                  </a>
                </a-col>
              </span>
            </a-row>
            <template v-if="toggleSearchStatus">
              <a-row :gutter="24">
                <a-col :md="6" :sm="24">
                  <a-form-item label="操作员" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择操作员" showSearch allow-clear optionFilterProp="children" v-model="queryParam.creator">
                      <a-select-option v-for="(item,index) in userList" :key="index" :value="item.id">
                        {{ item.userName }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="单据状态" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择单据状态" allow-clear v-model="queryParam.status">
                      <a-select-option value="0">未审核</a-select-option>
                      <a-select-option value="9" v-if="!checkFlag">审核中</a-select-option>
                      <a-select-option value="1">已审核</a-select-option>
                      <a-select-option value="4">已发放</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="单据备注" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-input placeholder="请输入单据备注" v-model="queryParam.remark"></a-input>
                  </a-form-item>
                </a-col>
              </a-row>
            </template>
          </a-form>
        </div>
        <!-- 操作按钮区域 -->
        <div class="table-operator"  style="margin-top: 5px">
          <a-button v-if="btnEnableList.indexOf(1)>-1" @click="myHandleAdd" type="primary" icon="plus">新增</a-button>
          <a-button v-if="btnEnableList.indexOf(1)>-1" icon="delete" @click="batchDel">删除</a-button>
          <a-button v-if="btnEnableList.indexOf(1)>-1" icon="edit" @click="handleQuickEdit">备注</a-button>
          <a-button v-if="canConfirmIssue" icon="export" :loading="issueLoading" @click="confirmIssue">确认发放</a-button>
          <a-button v-if="canAuditApply && checkFlag && btnEnableList.indexOf(2)>-1" icon="check" @click="batchSetStatus(1)">审核</a-button>
          <a-button v-if="canAuditApply && checkFlag && btnEnableList.indexOf(7)>-1" icon="stop" @click="batchSetStatus(0)">反审核</a-button>
          <a-button v-if="isShowExcel && btnEnableList.indexOf(3)>-1" icon="download" @click="handleExport">导出</a-button>
          <a-popover trigger="click" placement="right">
            <template slot="content">
              <a-checkbox-group @change="onColChange" v-model="settingDataIndex" :defaultValue="settingDataIndex">
                <a-row style="width: 500px">
                  <template v-for="(item,index) in defColumns">
                    <template>
                      <a-col :span="8">
                        <a-checkbox :value="item.dataIndex">
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
            <a-button icon="setting">列设置</a-button>
          </a-popover>
            <a-tooltip placement="left" :title="applyHelpText" slot="action">
            <a-icon v-if="btnEnableList.indexOf(1)>-1" type="question-circle" style="font-size:20px;float:right;" />
          </a-tooltip>
        </div>
        <!-- table区域-begin -->
        <div>
          <a-table
            ref="table"
            size="middle"
            bordered
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :components="handleDrag(columns)"
            :pagination="ipagination"
            :scroll="scroll"
            :loading="loading"
            :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
            :expandedRowKeys="expandedRowKeys"
            @expand="onExpand"
            @change="handleTableChange">
            <span slot="action" slot-scope="text, record">
              <a @click="viewApplyDetail(record)">查看</a>
              <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
              <a v-if="btnEnableList.indexOf(1)>-1" @click="myHandleEdit(record)">编辑</a>
              <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
              <a v-if="btnEnableList.indexOf(1)>-1" @click="myHandleCopyAdd(record)">复制</a>
              <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
              <a-popconfirm v-if="btnEnableList.indexOf(1)>-1" title="确定删除吗?" @confirm="() => myHandleDelete(record)">
                <a>删除</a>
              </a-popconfirm>
            </span>
            <template slot="customRenderStatus" slot-scope="status">
              <a-tag v-if="status == '0'" color="red">未审核</a-tag>
              <a-tag v-if="status == '1' || status == '2' || status == '3'" color="green">已审核</a-tag>
              <a-tag v-if="status == '4'" color="purple">已发放</a-tag>
              <a-tag v-if="status == '9'" color="orange">审核中</a-tag>
            </template>
            <a-table
              bordered
              size="small"
              slot="expandedRowRender"
              slot-scope="record"
              :loading="record.loading"
              :columns="detailColumns"
              :dataSource="record.childrens"
              :row-key="record => record.id"
              :pagination="false">
            </a-table>
          </a-table>
        </div>
        <!-- table区域-end -->
        <!-- 表单区域 -->
        <purchase-apply-modal ref="modalForm" :role-code="roleCode" @ok="modalFormOk" @close="modalFormClose"></purchase-apply-modal>
        <other-out-modal ref="issueModalForm" @ok="modalFormOk" @close="modalFormClose"></other-out-modal>
        <bill-detail ref="modalDetail" @ok="modalFormOk" @close="modalFormClose"></bill-detail>
        <bill-excel-iframe ref="billExcelIframe" @ok="modalFormOk" @close="modalFormClose"></bill-excel-iframe>
        <quick-edit-modal ref="quickEditModal" @ok="modalFormOk" @close="modalFormClose"></quick-edit-modal>
      </a-card>
    </a-col>
  </a-row>
</template>
<!-- by  ji  sheng  hua-->
<script>
  import PurchaseApplyModal from './modules/PurchaseApplyModal'
  import OtherOutModal from './modules/OtherOutModal'
  import BillDetail from './dialog/BillDetail'
  import BillExcelIframe from '@/components/tools/BillExcelIframe'
  import QuickEditModal from './dialog/QuickEditModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { BillListMixin } from './mixins/BillListMixin'
  import JEllipsis from '@/components/jeecg/JEllipsis'
  import JDate from '@/components/jeecg/JDate'
  import { getAction } from '@/api/manage'
  export default {
    name: "PurchaseApplyList",
    mixins:[JeecgListMixin,BillListMixin],
    components: {
      PurchaseApplyModal,
      OtherOutModal,
      BillDetail,
      BillExcelIframe,
      QuickEditModal,
      JEllipsis,
      JDate
    },
    data () {
      return {
        roleCode: '',
        issueLoading: false,
        // 查询条件
        queryParam: {
          number: "",
          materialParam: "",
          type: "其它",
          subType: "请购单",
          creator: undefined,
          status: undefined,
          remark: ""
        },
        prefixNo: 'QGD',
        urlPath: '/bill/purchase_apply',
        labelCol: {
          span: 5
        },
        wrapperCol: {
          span: 18,
          offset: 1
        },
        // 默认索引
        defDataIndex:['action','number','materialsList','operTimeStr','userName','materialCount','status'],
        // 默认列
        defColumns: [
          {
            title: '操作',
            dataIndex: 'action',
            align:"center", width: 180,
            scopedSlots: { customRender: 'action' },
          },
          { title: '单据编号', dataIndex: 'number',width:180},
          { title: '物品信息', dataIndex: 'materialsList',width:320, ellipsis:true},
          { title: '单据日期', dataIndex: 'operTimeStr',width:185},
          { title: '操作员', dataIndex: 'userName',width:120, ellipsis:true},
          { title: '数量', dataIndex: 'materialCount',width:80},
          { title: '备注', dataIndex: 'remark',width:250},
          { title: '状态', dataIndex: 'status', width: 100, align: "center",
            scopedSlots: { customRender: 'customRenderStatus' }
          }
        ],
        url: {
          list: "/depotHead/list",
          delete: "/depotHead/delete",
          deleteBatch: "/depotHead/deleteBatch",
          batchSetStatusUrl: "/depotHead/batchSetStatus"
        }
      }
    },
    created() {
      this.initSystemConfig()
      this.initUser()
      this.initQuickBtn()
      this.getDepotByCurrentUser()
      this.loadCurrentRoleCode()
    },
    computed: {
      canConfirmIssue() {
        return this.roleCode === 'ROLE_ADMIN' || this.roleCode === 'ROLE_OFFICE'
      },
      canAuditApply() {
        return this.roleCode === 'ROLE_ADMIN' || this.roleCode === 'ROLE_OFFICE'
      },
      applyHelpText() {
        return this.canAuditApply
          ? '领用申请只涉及数量，办公室审核后可确认发放。勾选单据后可以进行删除、审核、反审核或确认发放。'
          : '领用申请只涉及数量，提交后由办公室审核并确认发放。'
      }
    },
    methods: {
      viewApplyDetail(record) {
        this.myHandleDetail(record, '请购单', this.prefixNo)
        if(!this.canAuditApply) {
          this.$refs.modalDetail.isCanBackCheck = false
        }
        this.$refs.modalDetail.title = '领用申请-详情'
      },
      loadCurrentRoleCode() {
        getAction('/user/getRoleTypeByCurrentUser').then((res) => {
          if(res && res.code === 200) {
            this.roleCode = res.data.roleCode || ''
          }
        })
      },
      confirmIssue() {
        if(this.selectedRowKeys.length !== 1) {
          this.$message.warning('请选择一条领用申请！')
          return
        }
        const info = this.selectionRows[0]
        if(info.status !== '1' && info.status !== '2' && info.status !== '3') {
          this.$message.warning('只能发放已审核的领用申请！')
          return
        }
        this.issueLoading = true
        const params = {
          headerId: info.id,
          mpList: '',
          linkType: 'basic'
        }
        getAction('/depotItem/getDetailList', params).then((res) => {
          if(res && res.code === 200) {
            const issueModal = this.$refs.issueModalForm
            issueModal.action = 'add'
            issueModal.transferParam = {
              issueMode: true,
              list: res.data.rows,
              number: info.number,
              departmentName: info.issueDepartment,
              remark: info.remark
            }
            issueModal.defaultDepotId = this.defaultDepotId
            issueModal.add()
            issueModal.title = '确认发放'
          }
        }).finally(() => {
          this.issueLoading = false
        })
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
