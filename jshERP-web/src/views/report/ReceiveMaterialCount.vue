<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :bordered="false">
        <div class="table-page-search-wrapper">
          <a-row :gutter="16" type="flex" align="middle">
            <a-col>
              <a-select v-model="year" style="width: 120px" @change="loadData" placeholder="选择年份">
                <a-select-option v-for="item in yearOptions" :key="item" :value="item">{{ item }}年</a-select-option>
              </a-select>
            </a-col>
            <a-col>
              <a-input v-model="materialName" allow-clear style="width: 220px" placeholder="搜索物品名称" @pressEnter="loadData" />
            </a-col>
            <a-col>
              <a-space>
                <a-button type="primary" icon="search" @click="loadData">查询</a-button>
                <a-button icon="reload" @click="resetQuery">重置</a-button>
                <a-button v-print="'#receiveMaterialCountPrint'" icon="printer">打印</a-button>
                <a-button icon="download" @click="exportReport">导出</a-button>
              </a-space>
            </a-col>
          </a-row>
        </div>
        <section id="receiveMaterialCountPrint">
          <a-table bordered size="small" rowKey="materialId" :loading="loading" :pagination="pagination"
            :dataSource="filteredData" :columns="columns" :scroll="{ x: 1500, y: 450}" />
        </section>
      </a-card>
    </a-col>
  </a-row>
</template>

<script>
import { getReceiveMaterialCount } from '@/api/api'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'

const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']

export default {
  name: 'ReceiveMaterialCount',
  mixins: [JeecgListMixin],
  data () {
    const currentYear = new Date().getFullYear()
    return {
      year: currentYear,
      materialName: '',
      yearOptions: Array.from({ length: currentYear - 2026 + 2 }, (_, index) => currentYear + 1 - index),
      loading: false,
      dataSource: [],
      columns: [
        { title: '序号', dataIndex: 'rowIndex', width: 70, fixed: 'left', customRender: (value, row, index) => index + 1 },
        { title: '名称', dataIndex: 'mName', width: 180, fixed: 'left' },
        ...monthNames.map((title, index) => ({ title, dataIndex: `month${index + 1}`, width: 95, align: 'right', customRender: value => Number(value || 0) })),
        { title: '合计', dataIndex: 'total', width: 110, align: 'right', customRender: value => Number(value || 0) }
      ]
    }
  },
  mounted () {
    this.loadData()
  },
  computed: {
    filteredData () {
      const keyword = (this.materialName || '').trim().toLowerCase()
      if (!keyword) return this.dataSource
      return this.dataSource.filter(item => (item.mName || '').toLowerCase().indexOf(keyword) !== -1)
    },
    pagination () {
      return { pageSize: 10, pageSizeOptions: ['10', '20', '50', '100', '200', '500'], showSizeChanger: true, showQuickJumper: true, showTotal: total => `共 ${total} 条` }
    }
  },
  methods: {
    loadData () {
      this.loading = true
      getReceiveMaterialCount({ year: this.year }).then(res => {
        if (res.code === 200) this.dataSource = res.data || []
        else this.$message.error(res.data || '领用数量汇总加载失败')
      }).finally(() => { this.loading = false })
    },
    resetQuery () {
      this.year = new Date().getFullYear()
      this.materialName = ''
      this.loadData()
    },
    exportReport () {
      const head = ['序号', '名称', ...monthNames, '合计']
      const list = this.filteredData.map((item, index) => [index + 1, item.mName, ...monthNames.map((_, month) => item[`month${month + 1}`] || 0), item.total || 0])
      this.handleExportXlsPost('领用数量汇总', '领用数量汇总', head.join(','), `${this.year}年度领用数量汇总`, list)
    }
  }
}
</script>

<style scoped>
#receiveMaterialCountPrint /deep/ .ant-table-thead > tr > th { text-align: center; white-space: nowrap; }
</style>
