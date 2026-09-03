<template>
  <a-row :gutter="24">
    <a-col :md="24">
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
                v-model="organizationIds"
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
                <a-button
                  type="primary"
                  icon="search"
                  @click="loadData"
                >
                  查询
                </a-button>

                <a-button
                  icon="reload"
                  @click="resetQuery"
                >
                  重置
                </a-button>

                <a-button
                  v-print="'#budgetReportPrint'"
                  icon="printer"
                >
                  打印
                </a-button>

                <a-button
                  icon="download"
                  @click="exportReport"
                >
                  导出
                </a-button>
              </a-space>
            </a-col>

          </a-row>
        </div>
        <section id="budgetReportPrint">
          <a-table bordered size="small" rowKey="organizationId" :loading="loading"
            :pagination="pagination" :dataSource="filteredData" :columns="columns" :scroll="{ x: 3150, y: 400 }">
          </a-table>
        </section>
      </a-card>
    </a-col>
  </a-row>
</template>

<script>
import { getBudgetStatistics } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'

const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
const moneyColumn = (title, dataIndex, width = 110) => ({
  title,
  dataIndex,
  width,
  align: 'right',
  customRender: value => Number(value || 0).toFixed(2)
})

export default {
  name: 'BudgetReport',
  mixins: [JeecgListMixin],
  data () {
    const currentYear = new Date().getFullYear()
    return {
      year: currentYear,
      yearOptions: Array.from({ length: currentYear - 2026 + 2 }, (_, index) => currentYear + 1 - index),
      organizationIds: [],
      loading: false,
      dataSource: [],
      columns: [
        { title: '部门', dataIndex: 'organizationName', width: 160, fixed: 'left' },
        moneyColumn('期初（年度初始预算）', 'initialBudget', 160),
        ...monthNames.map((name, index) => ({
          title: name,
          children: [
            moneyColumn('上期结余', `month${index + 1}CarryOver`),
            moneyColumn('支出', `month${index + 1}Expense`)
          ]
        })),
        {
          title: '年末结余',
          children: [
            moneyColumn('支出合计', 'totalExpense', 120),
            moneyColumn('年末结余数', 'yearEndBalance', 120)
          ]
        }
      ]
    }
  },
  computed: {
    departmentOptions () {
      return this.dataSource
    },
    filteredData () {
      if (!this.organizationIds.length) return this.dataSource
      return this.dataSource.filter(item => this.organizationIds.indexOf(item.organizationId) !== -1)
    },
    pagination () {
      return { pageSize: 10, pageSizeOptions: ['10', '20', '50', '100', '200', '500'], showSizeChanger: true, showQuickJumper: true, showTotal: total => `共 ${total} 条` }
    }
  },
  mounted () {
    this.loadData()
  },
  methods: {
    loadData () {
      this.loading = true
      getBudgetStatistics({ year: this.year }).then(res => {
        if (res.code === 200) {
          this.dataSource = res.data || []
          this.organizationIds = this.organizationIds.filter(id =>
            this.dataSource.some(item => item.organizationId === id)
          )
        } else {
          this.$message.error(res.data || '预算统计加载失败')
        }
      }).finally(() => { this.loading = false })
    },
    resetQuery () {
      this.organizationIds = []
      this.year = new Date().getFullYear()
      this.loadData()
    },
    exportReport () {
      const head = ['部门', '期初（年度初始预算）']
      monthNames.forEach(name => head.push(`${name}-上期结余`, `${name}-支出`))
      head.push('支出合计', '年末结余数')
      const list = this.filteredData.map(item => {
        const row = [item.organizationName, item.initialBudget]
        for (let index = 1; index <= 12; index++) {
          row.push(item[`month${index}CarryOver`] || 0, item[`month${index}Expense`] || 0)
        }
        row.push(item.totalExpense || 0, item.yearEndBalance || 0)
        return row
      })
      this.handleExportXlsPost('预算统计', '预算统计', head.join(','), `${this.year}年度预算统计`, list)
    }
  }
}
</script>

<style scoped>
#budgetReportPrint /deep/ .ant-table-thead > tr > th {
  text-align: center;
  white-space: nowrap;
}
</style>
