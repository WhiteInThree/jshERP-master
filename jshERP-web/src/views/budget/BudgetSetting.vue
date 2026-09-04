<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-row :gutter="16" type="flex" align="middle">
        <!-- 年份 -->
        <a-col>
          <a-select
            v-model="year"
            style="width: 120px"
            @change="loadData"
            placeholder="选择年份"
          >
            <a-select-option
              v-for="item in yearOptions"
              :key="item"
              :value="item"
            >
              {{ item }}年
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 部门 -->
        <a-col>
          <a-select
            v-model="selectedOrganizations"
            mode="multiple"
            show-search
            allow-clear
            optionFilterProp="children"
            :maxTagCount="2"
            style="width: 300px"
            placeholder="搜索或选择部门"
          >
            <a-select-option
              v-for="item in departmentOptions"
              :key="item.organizationId"
              :value="item.organizationId"
            >
              {{ item.organizationName }}
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 操作按钮 -->
        <a-col>
          <a-space>
            <a-button icon="download" @click="exportReport">
              导出
            </a-button>

            <a-button icon="reload" @click="resetQuery">
              重置筛选
            </a-button>

            <a-button
              type="primary"
              icon="save"
              :loading="saving"
              :disabled="readOnly"
              @click="saveAll"
            >
              保存设置
            </a-button>
          </a-space>
        </a-col>

      </a-row>
    </div>

    <a-row :gutter="16" style="margin: 16px 0">
      <a-col :md="8" :sm="24"><a-statistic title="年度总预算" :value="summary.budgetAmount" :precision="2" suffix="元" /></a-col>
      <a-col :md="8" :sm="24"><a-statistic title="年度已使用" :value="summary.usedAmount" :precision="2" suffix="元" /></a-col>
      <a-col :md="8" :sm="24"><a-statistic title="年度可用预算" :value="summary.availableAmount" :precision="2" suffix="元" /></a-col>
    </a-row>

    <a-table bordered rowKey="organizationId" size="middle" :loading="loading" :pagination="pagination" :dataSource="filteredData" :columns="columns" :scroll="{y: 350}">
      <template slot="budgetAmount" slot-scope="text, record">
        <a-input-number v-model="record.budgetAmount" :min="0" :precision="2" :step="1000" :disabled="readOnly" style="width: 160px" />
      </template>
      <template slot="money" slot-scope="text">{{ formatMoney(text) }}</template>
    </a-table>

  </a-card>
</template>

<script>
import { getBudgetSettingList, saveBudgetSetting } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'

export default {
  name: 'BudgetSetting',
  mixins: [JeecgListMixin],
  data () {
    const currentYear = new Date().getFullYear()
    return {
      year: currentYear,
      selectedOrganizations: [],
      loading: false,
      saving: false,
      dataSource: [],
      columns: [
        { title: '部门', dataIndex: 'organizationName', width: 220 },
        { title: '年度预算（元）', dataIndex: 'budgetAmount', width: 220, scopedSlots: { customRender: 'budgetAmount' } },
        { title: '已用预算（元）', dataIndex: 'usedAmount', width: 180, scopedSlots: { customRender: 'money' } },
        { title: '可用预算（元）', dataIndex: 'availableAmount', width: 180, scopedSlots: { customRender: 'money' } }
      ],
      currentYear,
      yearOptions: Array.from({ length: currentYear - 2026 + 2 }, (_, index) => currentYear + 1 - index)
    }
  },
  computed: {
    summary () {
      return this.dataSource.reduce((result, item) => {
        result.budgetAmount += Number(item.savedBudgetAmount || 0)
        result.usedAmount += Number(item.usedAmount || 0)
        result.availableAmount += Number(item.savedAvailableAmount || 0)
        return result
      }, { budgetAmount: 0, usedAmount: 0, availableAmount: 0 })
    },
    readOnly () { return this.year < this.currentYear },
    departmentOptions () { return this.dataSource.filter(item => item.organizationId != null) },
    filteredData () {
      if (!this.selectedOrganizations.length) return this.dataSource
      return this.dataSource.filter(item => this.selectedOrganizations.indexOf(item.organizationId) !== -1)
    },
    pagination () {
      return { pageSize: 10, pageSizeOptions: ['10', '20', '50', '100', '200', '500'], showSizeChanger: true, showQuickJumper: true, showTotal: total => `共 ${total} 条` }
    }
  },
  mounted () {
    this.loadData()
  },
  methods: {
    exportReport () {
      const head = '部门,年度预算,已用预算,可用预算'
      const list = this.filteredData.map(item => [item.organizationName, item.budgetAmount, item.usedAmount, item.availableAmount])
      this.handleExportXlsPost('预算报表', '预算报表', head, `${this.year}年度预算`, list)
    },
    loadData () {
      this.loading = true
      this.selectedOrganizations = []
      getBudgetSettingList({ year: this.year }).then(res => {
        if (res.code === 200) {
          this.dataSource = (res.data || []).map(item => ({
            ...item,
            budgetAmount: Number(item.budgetAmount || 0),
            usedAmount: Number(item.usedAmount || 0),
            availableAmount: Number(item.availableAmount || 0),
            savedBudgetAmount: Number(item.budgetAmount || 0),
            savedAvailableAmount: Number(item.availableAmount || 0)
          }))
        } else {
          this.$message.error(res.data || '预算数据加载失败')
        }
      }).finally(() => { this.loading = false })
    },
    resetQuery () {
      this.selectedOrganizations = []
    },
    async saveAll () {
      if (this.readOnly) {
        this.$message.warning('以前年度预算不可修改')
        return
      }
      this.saving = true
      try {
        const changedItems = this.dataSource.filter(item =>
          Number(item.budgetAmount || 0) !== Number(item.savedBudgetAmount || 0)
        )
        if (!changedItems.length) {
          this.$message.info('没有需要保存的预算变更')
          return
        }
        const responses = await Promise.all(changedItems.map(item => saveBudgetSetting({
          budgetYear: this.year,
          organizationId: item.organizationId,
          budgetAmount: Number(item.budgetAmount || 0)
        })))
        const failedResponse = responses.find(res => res.code !== 200)
        if (failedResponse) throw new Error(failedResponse.data || failedResponse.message || 'save failed')
        this.$message.success('预算设置已保存')
        this.loadData()
      } catch (e) {
        this.$message.error(e.message || '预算设置保存失败')
      } finally {
        this.saving = false
      }
    },
    formatMoney (value) {
      return Number(value || 0).toFixed(2)
    }
  }
}
</script>
