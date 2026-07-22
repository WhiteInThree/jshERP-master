<template>
  <div class="office-dashboard">
    <section class="office-hero">
      <div>
        <h1>办公室发放工作台</h1>
        <p>集中处理领用申请，掌握库存风险与部门领用趋势。</p>
      </div>
      <div class="hero-badge">
        <a-icon type="calendar" />
        <strong>{{ currentMonth }}</strong>
        <span>运行概览</span>
      </div>
    </section>

    <a-spin :spinning="loading">
      <section class="dashboard-grid">
        <article class="dashboard-panel todo-panel">
          <panel-title index="01" title="待办事项" note="需要办公室处理" />
          <div class="todo-list">
            <div class="todo-item todo-review">
              <span class="todo-icon"><a-icon type="audit" /></span>
              <div>
                <span>待审核申请</span>
                <small>等待审核确认</small>
              </div>
              <strong>{{ summary.pendingApplyCount || 0 }}</strong>
            </div>
            <div class="todo-item todo-issue">
              <span class="todo-icon"><a-icon type="export" /></span>
              <div>
                <span>待发放申请</span>
                <small>已审核，等待发放</small>
              </div>
              <strong>{{ summary.pendingIssueCount || 0 }}</strong>
            </div>
          </div>
          <div class="todo-footnote">
            <a-icon type="info-circle" /> 待办数量随申请审核和确认发放实时更新
          </div>
        </article>

        <article class="dashboard-panel warning-panel">
          <panel-title index="02" title="库存预警" note="缺口最大的 5 项" />
          <div v-if="stockWarnings.length" class="warning-table">
            <div class="warning-head">
              <span>物品</span><span>当前库存</span><span>最低安全库存</span>
            </div>
            <div v-for="item in stockWarnings" :key="item.mname + '-' + item.depotName" class="warning-row">
              <div>
                <strong>{{ item.mname }}</strong>
                <small>{{ item.depotName || '默认仓库' }}</small>
              </div>
              <span class="current-stock">{{ formatNumber(item.currentNumber) }}</span>
              <span>{{ formatNumber(item.lowSafeStock) }}</span>
            </div>
          </div>
          <a-empty v-else description="暂无低库存预警" />
        </article>

        <article class="dashboard-panel ranking-panel">
          <div class="ranking-heading">
            <panel-title index="03" title="本月部门领用排行" note="按发放数据统计" />
            <a-radio-group v-model="rankingMode" size="small" buttonStyle="solid">
              <a-radio-button value="quantity">按数量</a-radio-button>
              <a-radio-button value="amount">按金额</a-radio-button>
            </a-radio-group>
          </div>
          <div v-if="sortedRanking.length" class="ranking-list">
            <div v-for="(item, index) in sortedRanking" :key="item.departmentName" class="ranking-row">
              <span class="ranking-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <div class="ranking-name">
                <strong>{{ item.departmentName || '未分配部门' }}</strong>
                <span><i :style="{ width: rankingBarWidth(item) }"></i></span>
              </div>
              <b v-if="rankingMode === 'quantity'">{{ formatNumber(item.quantity) }}</b>
              <b v-else>¥{{ formatMoney(item.amount) }}</b>
            </div>
          </div>
          <a-empty v-else description="本月暂无部门领用数据" />
        </article>

        <article class="dashboard-panel analytics-panel">
          <panel-title index="04" title="统计分析" note="申请与发放全局统计" />
          <div class="analytics-grid">
            <div class="analytics-card tone-blue">
              <a-icon type="file-add" />
              <span>本月领用次数</span>
              <strong>{{ summary.monthApplyCount || 0 }}</strong>
              <small>本月新增申请单数</small>
            </div>
            <div class="analytics-card tone-green">
              <a-icon type="check-square" />
              <span>本月发放次数</span>
              <strong>{{ summary.monthIssueCount || 0 }}</strong>
              <small>本月生成发放单数</small>
            </div>
            <div class="analytics-card tone-orange">
              <a-icon type="database" />
              <span>累计发放数量</span>
              <strong>{{ formatNumber(summary.totalIssueQuantity) }}</strong>
              <small>历史累计发放数量</small>
            </div>
            <div class="analytics-card tone-slate">
              <a-icon type="pay-circle" />
              <span>累计发放金额</span>
              <strong class="money-value">¥{{ formatMoney(summary.totalIssueAmount) }}</strong>
              <small>历史累计发放金额</small>
            </div>
          </div>
        </article>
      </section>
    </a-spin>
  </div>
</template>

<script>
  import { getOfficeDashboard } from '@/api/api'

  const PanelTitle = {
    functional: true,
    props: ['index', 'title', 'note'],
    render(h, context) {
      return h('header', { class: 'panel-title' }, [
        h('div', [
          h('span', { class: 'panel-index' }, context.props.index),
          h('h2', context.props.title)
        ]),
        h('span', { class: 'panel-note' }, context.props.note)
      ])
    }
  }

  export default {
    name: 'OfficeDashboard',
    components: { PanelTitle },
    data() {
      return {
        loading: true,
        rankingMode: 'quantity',
        summary: {
          pendingApplyCount: 0,
          pendingIssueCount: 0,
          monthApplyCount: 0,
          monthIssueCount: 0,
          totalIssueQuantity: 0,
          totalIssueAmount: 0
        },
        stockWarnings: [],
        departmentRanking: []
      }
    },
    computed: {
      currentMonth() {
        return `${new Date().getFullYear()} · ${new Date().getMonth() + 1}月`
      },
      sortedRanking() {
        const key = this.rankingMode
        return this.departmentRanking.slice().sort((a, b) => Number(b[key] || 0) - Number(a[key] || 0))
      },
      rankingMaxValue() {
        return Math.max(...this.sortedRanking.map(item => Number(item[this.rankingMode]) || 0), 1)
      }
    },
    created() {
      this.loadDashboard()
    },
    methods: {
      loadDashboard() {
        this.loading = true
        getOfficeDashboard().then((res) => {
          if(res && res.code === 200) {
            const data = res.data || {}
            this.summary = Object.assign({}, this.summary, data.summary || {})
            this.stockWarnings = data.stockWarnings || []
            this.departmentRanking = data.departmentRanking || []
          } else {
            this.$message.warning('办公室首页数据加载失败')
          }
        }).catch(() => {
          this.$message.error('办公室首页数据加载失败')
        }).finally(() => {
          this.loading = false
        })
      },
      formatNumber(value) {
        const number = Number(value || 0)
        return Number.isInteger(number) ? number : number.toFixed(2).replace(/0+$/, '').replace(/\.$/, '')
      },
      formatMoney(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      rankingBarWidth(item) {
        const value = Number(item[this.rankingMode]) || 0
        return `${Math.max(value / this.rankingMaxValue * 100, 5)}%`
      }
    }
  }
</script>

<style scoped>
  .office-dashboard {
    min-height: calc(100vh - 110px);
    color: #333;
  }

  .office-hero {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 16px;
    padding: 18px 20px;
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
  }

  .office-hero h1 {
    margin: 0;
    color: #262626;
    font-size: 22px;
    font-weight: 600;
  }

  .office-hero p {
    margin: 8px 0 0;
    color: #8c8c8c;
  }

  .hero-badge {
    display: grid;
    grid-template-columns: 24px auto;
    align-items: center;
    padding: 12px 17px;
    color: #1890ff;
    background: #e6f7ff;
    border-radius: 4px;
  }

  .hero-badge i { grid-row: 1 / 3; font-size: 17px; }
  .hero-badge strong { font-size: 14px; }
  .hero-badge span { color: #69c0ff; font-size: 10px; }

  .dashboard-grid {
    display: grid;
    grid-template-columns: minmax(0, .85fr) minmax(0, 1.15fr);
    gap: 12px;
  }

  .dashboard-panel {
    min-height: 330px;
    padding: 20px;
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
  }

  /deep/ .panel-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  /deep/ .panel-title > div { display: flex; align-items: center; }
  /deep/ .panel-index { margin-right: 9px; color: #1890ff; font-size: 11px; font-weight: 700; }
  /deep/ .panel-title h2 { margin: 0; color: #262626; font-size: 18px; font-weight: 600; }
  /deep/ .panel-note { color: #bfbfbf; font-size: 11px; }

  .todo-list { display: grid; gap: 13px; }

  .todo-item {
    display: grid;
    grid-template-columns: 48px minmax(0, 1fr) auto;
    align-items: center;
    min-height: 92px;
    padding: 16px;
    border-radius: 4px;
  }

  .todo-review { background: #fff7e6; }
  .todo-issue { background: #f6ffed; }

  .todo-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;
    color: #fff;
    font-size: 17px;
    border-radius: 4px;
  }

  .todo-review .todo-icon { background: #faad14; }
  .todo-issue .todo-icon { background: #52c41a; }
  .todo-item div span, .todo-item div small { display: block; }
  .todo-item div span { color: #595959; font-weight: 600; }
  .todo-item div small { margin-top: 4px; color: #96a09f; font-size: 11px; }
  .todo-item strong { color: #262626; font-size: 30px; }

  .todo-footnote {
    margin-top: 17px;
    color: #98a3a3;
    font-size: 11px;
  }

  .warning-head,
  .warning-row {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) .7fr .8fr;
    align-items: center;
    gap: 10px;
  }

  .warning-head {
    padding: 9px 12px;
    color: #98a3a5;
    font-size: 11px;
    border-bottom: 1px solid #e8ebe6;
  }

  .warning-row {
    min-height: 48px;
    padding: 7px 12px;
    color: #5d6d70;
    border-bottom: 1px dashed #e6e9e4;
  }

  .warning-row:last-child { border-bottom: 0; }
  .warning-row strong, .warning-row small { display: block; }
  .warning-row strong { color: #365158; font-size: 13px; }
  .warning-row small { margin-top: 2px; color: #a0aaa9; font-size: 10px; }
  .current-stock { color: #ff4d4f; font-weight: 700; }

  .ranking-heading { position: relative; }
  .ranking-heading .ant-radio-group { position: absolute; top: -4px; right: 0; }
  .ranking-list { max-height: 330px; overflow-y: auto; padding-right: 5px; }

  .ranking-row {
    display: grid;
    grid-template-columns: 28px minmax(0, 1fr) 92px;
    align-items: center;
    gap: 11px;
    min-height: 48px;
  }

  .ranking-index {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 23px;
    height: 23px;
    color: #8e9b9d;
    font-size: 11px;
    font-weight: 800;
    background: #eef0ec;
    border-radius: 4px;
  }

  .ranking-index.top { color: #fff; background: #1890ff; }
  .ranking-name strong { display: block; margin-bottom: 6px; color: #3f565c; font-size: 13px; }
  .ranking-name span { display: block; height: 4px; overflow: hidden; background: #eceee9; border-radius: 4px; }
  .ranking-name i { display: block; height: 100%; background: #1890ff; border-radius: 4px; }
  .ranking-row b { color: #284950; text-align: right; font-size: 13px; }

  .analytics-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 13px;
  }

  .analytics-card {
    position: relative;
    min-height: 112px;
    padding: 15px 16px;
    overflow: hidden;
    background: #f5f6f2;
    border-radius: 4px;
  }

  .analytics-card > i { position: absolute; right: 14px; top: 14px; color: currentColor; font-size: 18px; opacity: .7; }
  .analytics-card span, .analytics-card small { display: block; }
  .analytics-card span { color: #6f7f82; font-size: 12px; }
  .analytics-card strong { display: block; margin: 5px 0 1px; color: #1f4149; font-size: 27px; }
  .analytics-card small { color: #a0aaa9; font-size: 10px; }
  .analytics-card .money-value { font-size: 22px; }
  .tone-blue { color: #39768d; background: #edf5f7; }
  .tone-green { color: #3e8375; background: #edf6f2; }
  .tone-orange { color: #c77a4d; background: #fff3e9; }
  .tone-slate { color: #596f7e; background: #eff2f3; }

  @media (max-width: 1050px) {
    .dashboard-grid { grid-template-columns: 1fr; }
  }

  @media (max-width: 600px) {
    .office-dashboard { min-height: auto; }
    .office-hero { align-items: flex-start; }
    .office-hero h1 { font-size: 24px; }
    .hero-badge { padding: 9px 11px; }
    .dashboard-panel { min-height: auto; padding: 18px 14px; }
    .analytics-grid { grid-template-columns: 1fr; }
    .warning-head, .warning-row { grid-template-columns: 1fr .62fr .8fr; gap: 5px; }
    .ranking-heading .ant-radio-group { position: static; margin: -8px 0 17px; }
  }
</style>
