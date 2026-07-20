<template>
  <div class="department-dashboard">
    <section class="dashboard-hero">
      <div>
        <div class="eyebrow">OFFICE SUPPLY CENTER</div>
        <h1>{{ summary.departmentName || '部门' }}领用工作台</h1>
        <p>申请进度、领用记录和库存情况，一页掌握。</p>
      </div>
      <div class="hero-date">
        <span>{{ currentMonth }}</span>
        <small>本月概览</small>
      </div>
    </section>

    <a-spin :spinning="loading">
      <section class="metric-grid">
        <article v-for="(metric, index) in metrics" :key="metric.key" class="metric-card" :class="'tone-' + metric.tone" :style="{ animationDelay: (index * 70) + 'ms' }">
          <div class="metric-icon"><a-icon :type="metric.icon" /></div>
          <div class="metric-copy">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small>{{ metric.note }}</small>
          </div>
        </article>
      </section>

      <section class="content-grid">
        <article class="panel recent-panel">
          <header class="panel-header">
            <div>
              <span class="section-index">01</span>
              <h2>最近申请</h2>
            </div>
            <span class="panel-note">最近 5 条</span>
          </header>
          <div v-if="recentApplications.length" class="application-list">
            <div class="application-head">
              <span>申请编号</span>
              <span>申请时间</span>
              <span>状态</span>
            </div>
            <div v-for="item in recentApplications" :key="item.id" class="application-row">
              <span class="application-number">{{ item.number }}</span>
              <span>{{ formatShortDate(item.operTimeStr) }}</span>
              <span><i class="status-dot" :class="statusClass(item.status)"></i>{{ statusText(item.status) }}</span>
            </div>
          </div>
          <a-empty v-else description="暂无领用申请" />
        </article>

        <article class="panel top-panel">
          <header class="panel-header">
            <div>
              <span class="section-index">02</span>
              <h2>本月领用 TOP5</h2>
            </div>
            <span class="panel-note">按领用数量</span>
          </header>
          <div v-if="topMaterials.length" class="top-list">
            <div v-for="(item, index) in topMaterials" :key="item.materialId + '-' + item.materialUnit" class="top-row">
              <span class="rank">{{ String(index + 1).padStart(2, '0') }}</span>
              <div class="top-name">
                <strong>{{ item.mName }}</strong>
                <span><i :style="{ width: topBarWidth(item.numSum) }"></i></span>
              </div>
              <b>{{ formatNumber(item.numSum) }}<small>{{ item.materialUnit || '' }}</small></b>
            </div>
          </div>
          <a-empty v-else description="本月暂无领用记录" />
        </article>
      </section>
    </a-spin>
  </div>
</template>

<script>
  import { getDepartmentDashboard } from '@/api/api'

  export default {
    name: 'DepartmentDashboard',
    data() {
      return {
        loading: true,
        summary: {
          departmentName: '',
          pendingApplyCount: 0,
          approvedPendingIssueCount: 0,
          monthReceivedCount: 0,
          currentStockMaterialCount: 0
        },
        recentApplications: [],
        topMaterials: []
      }
    },
    computed: {
      currentMonth() {
        return `${new Date().getMonth() + 1}月`
      },
      metrics() {
        return [
          { key: 'pending', label: '待审核申请', value: this.summary.pendingApplyCount || 0, note: '等待办公室处理', icon: 'clock-circle', tone: 'coral' },
          { key: 'approved', label: '已审核待发放', value: this.summary.approvedPendingIssueCount || 0, note: '即将安排发放', icon: 'check-circle', tone: 'amber' },
          { key: 'received', label: '本月已领用', value: this.formatNumber(this.summary.monthReceivedCount), note: '本月累计发放数量', icon: 'inbox', tone: 'teal' },
          { key: 'stock', label: '当前库存物品种类', value: this.summary.currentStockMaterialCount || 0, note: '库存大于零的物品种类', icon: 'appstore', tone: 'navy' }
        ]
      },
      maxTopValue() {
        return Math.max(...this.topMaterials.map(item => Number(item.numSum) || 0), 1)
      }
    },
    created() {
      this.loadDashboard()
    },
    methods: {
      loadDashboard() {
        this.loading = true
        getDepartmentDashboard().then((res) => {
          if(res && res.code === 200) {
            const data = res.data || {}
            this.summary = Object.assign({}, this.summary, data.summary || {})
            this.recentApplications = data.recentApplications || []
            this.topMaterials = data.topMaterials || []
          } else {
            this.$message.warning('部门首页数据加载失败')
          }
        }).catch(() => {
          this.$message.error('部门首页数据加载失败')
        }).finally(() => {
          this.loading = false
        })
      },
      formatNumber(value) {
        const number = Number(value || 0)
        return Number.isInteger(number) ? number : number.toFixed(2).replace(/0+$/, '').replace(/\.$/, '')
      },
      formatShortDate(value) {
        if(!value) return '-'
        return value.split('-').map(part => Number(part)).join('-')
      },
      statusText(status) {
        const statusMap = {
          '0': '待审核',
          '1': '待发放',
          '2': '待发放',
          '3': '待发放',
          '4': '已发放',
          '9': '审核中'
        }
        return statusMap[status] || '处理中'
      },
      statusClass(status) {
        if(status === '4') return 'status-issued'
        if(status === '1' || status === '2' || status === '3') return 'status-approved'
        return 'status-pending'
      },
      topBarWidth(value) {
        return `${Math.max((Number(value) || 0) / this.maxTopValue * 100, 8)}%`
      }
    }
  }
</script>

<style scoped>
  .department-dashboard {
    min-height: calc(100vh - 110px);
    padding: 24px;
    color: #18332f;
    background:
      radial-gradient(circle at 92% 8%, rgba(236, 173, 98, .22), transparent 24%),
      radial-gradient(circle at 5% 82%, rgba(44, 122, 111, .12), transparent 28%),
      #f4f0e7;
    border-radius: 18px 18px 0 0;
  }

  .dashboard-hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;
    padding: 8px 4px;
  }

  .eyebrow {
    margin-bottom: 8px;
    color: #c9664d;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 3px;
  }

  .dashboard-hero h1 {
    margin: 0;
    color: #173b35;
    font-family: "Microsoft YaHei", sans-serif;
    font-size: 32px;
    font-weight: 800;
    letter-spacing: -1px;
  }

  .dashboard-hero p {
    margin: 8px 0 0;
    color: #6c7d78;
    font-size: 14px;
  }

  .hero-date {
    min-width: 90px;
    padding: 12px 18px;
    color: #f9f6ef;
    text-align: center;
    background: #173b35;
    border-radius: 16px 16px 4px 16px;
    box-shadow: 0 12px 28px rgba(23, 59, 53, .16);
  }

  .hero-date span,
  .hero-date small {
    display: block;
  }

  .hero-date span {
    font-size: 22px;
    font-weight: 800;
  }

  .hero-date small {
    margin-top: 2px;
    color: #bcd0c9;
    font-size: 11px;
  }

  .metric-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 18px;
  }

  .metric-card {
    position: relative;
    display: flex;
    min-height: 148px;
    padding: 22px;
    overflow: hidden;
    background: rgba(255, 253, 248, .9);
    border: 1px solid rgba(31, 74, 66, .08);
    border-radius: 18px;
    box-shadow: 0 12px 30px rgba(61, 69, 60, .07);
    animation: rise-in .45s ease both;
  }

  .metric-card::after {
    content: '';
    position: absolute;
    right: -28px;
    bottom: -38px;
    width: 105px;
    height: 105px;
    border-radius: 50%;
    background: currentColor;
    opacity: .07;
  }

  .metric-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    margin-right: 16px;
    color: #fff;
    font-size: 19px;
    border-radius: 12px;
    background: currentColor;
  }

  .metric-icon i {
    color: #fff;
  }

  .metric-copy span,
  .metric-copy small {
    display: block;
  }

  .metric-copy span {
    color: #63736f;
    font-size: 13px;
    font-weight: 600;
  }

  .metric-copy strong {
    display: block;
    margin: 5px 0 2px;
    color: #173b35;
    font-size: 34px;
    line-height: 1.15;
  }

  .metric-copy small {
    color: #99a49f;
    font-size: 11px;
  }

  .tone-coral { color: #c9664d; }
  .tone-amber { color: #d29438; }
  .tone-teal { color: #2f8074; }
  .tone-navy { color: #31546c; }

  .content-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.45fr) minmax(340px, .85fr);
    gap: 18px;
  }

  .panel {
    min-height: 360px;
    padding: 24px;
    background: rgba(255, 253, 248, .94);
    border: 1px solid rgba(31, 74, 66, .08);
    border-radius: 20px;
    box-shadow: 0 14px 34px rgba(61, 69, 60, .07);
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .panel-header > div {
    display: flex;
    align-items: center;
  }

  .section-index {
    margin-right: 10px;
    color: #cf6f55;
    font-size: 12px;
    font-weight: 800;
  }

  .panel-header h2 {
    margin: 0;
    color: #173b35;
    font-size: 18px;
    font-weight: 750;
  }

  .panel-note {
    color: #97a39f;
    font-size: 11px;
  }

  .application-head,
  .application-row {
    display: grid;
    grid-template-columns: 1.4fr .75fr .75fr;
    align-items: center;
    gap: 12px;
  }

  .application-head {
    padding: 10px 16px;
    color: #93a09b;
    font-size: 11px;
    border-bottom: 1px solid #e9e5db;
  }

  .application-row {
    min-height: 52px;
    padding: 8px 16px;
    color: #5c6d68;
    font-size: 13px;
    border-bottom: 1px dashed #e9e5db;
  }

  .application-row:last-child {
    border-bottom: 0;
  }

  .application-number {
    color: #244f47;
    font-weight: 700;
  }

  .status-dot {
    display: inline-block;
    width: 7px;
    height: 7px;
    margin-right: 7px;
    border-radius: 50%;
  }

  .status-pending { background: #d3775e; }
  .status-approved { background: #d5a143; }
  .status-issued { background: #3e8a7e; }

  .top-row {
    display: grid;
    grid-template-columns: 34px minmax(0, 1fr) auto;
    align-items: center;
    gap: 12px;
    min-height: 55px;
  }

  .rank {
    color: #bdc5c1;
    font-size: 12px;
    font-weight: 800;
  }

  .top-name strong {
    display: block;
    margin-bottom: 7px;
    color: #35504b;
    font-size: 13px;
  }

  .top-name > span {
    display: block;
    width: 100%;
    height: 4px;
    overflow: hidden;
    background: #ece8de;
    border-radius: 4px;
  }

  .top-name i {
    display: block;
    height: 100%;
    background: linear-gradient(90deg, #377f73, #e0a654);
    border-radius: 4px;
  }

  .top-row b {
    color: #173b35;
    font-size: 16px;
  }

  .top-row b small {
    margin-left: 3px;
    color: #899792;
    font-size: 11px;
    font-weight: 500;
  }

  @keyframes rise-in {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
  }

  @media (max-width: 1100px) {
    .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .content-grid { grid-template-columns: 1fr; }
  }

  @media (max-width: 600px) {
    .department-dashboard { padding: 16px; border-radius: 12px 12px 0 0; }
    .dashboard-hero { align-items: flex-start; }
    .dashboard-hero h1 { font-size: 24px; }
    .hero-date { min-width: 72px; padding: 10px 12px; }
    .metric-grid { grid-template-columns: 1fr; }
    .metric-card { min-height: 128px; }
    .panel { padding: 18px 14px; }
    .application-head, .application-row { grid-template-columns: 1.25fr .65fr .7fr; gap: 6px; }
    .application-head, .application-row { padding-left: 8px; padding-right: 8px; }
  }
</style>
