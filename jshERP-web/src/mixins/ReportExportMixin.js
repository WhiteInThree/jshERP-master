import { getAction } from '@/api/manage'

export const ReportExportMixin = {
  data () {
    return { reportExportLoading: false }
  },
  methods: {
    async runReportExport (scope) {
      if (this.reportExportLoading) return
      this.reportExportLoading = true
      try {
        let rows = this.dataSource.map(row => ({ ...row }))
        if (scope === 'all') {
          rows = []
          const params = { ...this.getQueryParams(), currentPage: 1, pageSize: 500 }
          let total = 0
          do {
            const res = await getAction(this.url.list, params)
            if (res.code !== 200 || !res.data || !Array.isArray(res.data.rows)) {
              throw new Error(typeof res.data === 'string' ? res.data : '导出数据加载失败')
            }
            const batch = res.data.rows
            total = Number(res.data.total || 0)
            if (!batch.length) {
              if (rows.length < total) throw new Error('导出数据不完整，请重新查询后重试')
              break
            }
            rows.push(...batch.map(row => ({ ...row })))
            params.currentPage++
          } while (rows.length < total)

          // 在独立上下文中计算总计，避免更改页面数据和分页总数。
          const context = {
            dataSource: rows,
            ipagination: { pageSize: 11, total: rows.length }
          }
          if (this.normalizeGroupValues) {
            rows.forEach(row => this.normalizeGroupValues(row))
            this.appendGroupedTotalRow.call(context)
          } else {
            this.tableAddTotalRow.call(context, this.columns, rows)
          }
        }
        if (!rows.length) {
          this.$message.warning('暂无可导出的数据')
          return
        }
        await this.exportExcel(rows)
      } catch (error) {
        this.$message.error(error.message || '导出失败，请重试')
      } finally {
        this.reportExportLoading = false
      }
    }
  }
}
