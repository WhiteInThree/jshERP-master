<template>
  <div class="analysis-page">
    <div v-if="roleLoading" class="role-loading">
      <a-spin tip="正在加载首页..." />
    </div>
    <department-dashboard v-else-if="roleCode === 'ROLE_DEPT'" />
    <index-chart v-else />
  </div>
</template>

<script>
  import IndexChart from './IndexChart'
  import DepartmentDashboard from './DepartmentDashboard'
  import { getAction } from '@/api/manage'

  export default {
    name: "Analysis",
    components: {
      IndexChart,
      DepartmentDashboard
    },
    data() {
      return {
        roleCode: '',
        roleLoading: true
      }
    },
    created() {
      this.loadCurrentRole()
    },
    methods: {
      loadCurrentRole() {
        getAction('/user/getRoleTypeByCurrentUser').then((res) => {
          if(res && res.code === 200) {
            this.roleCode = res.data.roleCode || ''
          }
        }).finally(() => {
          this.roleLoading = false
        })
      }
    }
  }
</script>

<style scoped>
  .analysis-page {
    margin: 12px 12px 0;
  }

  .role-loading {
    min-height: 420px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
