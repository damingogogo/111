<template>
  <div class="grid">
    <section class="panel export-panel">
      <div>
        <h2>服务统计总览</h2>
        <p>按企业管理端脱敏口径导出筛查、课程、咨询、状态追踪等核心数据。</p>
      </div>
      <div class="export-actions">
        <el-segmented v-model="period" :options="periodOptions" @change="loadDashboard" />
        <el-button type="primary" @click="exportReport">导出报表</el-button>
      </div>
    </section>
    <section class="grid metric-grid">
      <div v-for="card in cards" :key="card.label" class="metric panel">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </div>
    </section>
    <section class="grid grid-2">
      <ChartBox :option="riskOption" />
      <ChartBox :option="departmentOption" />
      <ChartBox :option="moodOption" />
      <ChartBox :option="usageOption" />
    </section>

    <section class="grid grid-2">
      <section class="panel table-panel">
        <div class="panel-header compact">
          <div>
            <h2 class="panel-title">高风险关怀跟进</h2>
            <small>仅展示部门、工号和授权状态，避免泄露姓名与手机号。</small>
          </div>
        </div>
        <el-table :data="dashboard.careFollowups" height="330" stripe>
          <el-table-column prop="department_name" label="部门" min-width="120" />
          <el-table-column prop="employee_no" label="工号" min-width="120" />
          <el-table-column prop="risk_level" label="风险" width="92">
            <template #default="{ row }">
              <el-tag :type="row.risk_level === '高风险' ? 'danger' : 'warning'" size="small">{{ row.risk_level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="告知企业" width="96">
            <template #default="{ row }">
              <el-tag :type="Number(row.consent_to_notify) ? 'success' : 'info'" size="small">
                {{ Number(row.consent_to_notify) ? '已同意' : '未同意' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="96" />
          <el-table-column label="操作" width="112" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === '已跟进'" @click="completeFollowup(row)">完成跟进</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel table-panel">
        <div class="panel-header compact">
          <div>
            <h2 class="panel-title">服务通知</h2>
            <small>用于筛查推送、课程提醒和企业内部关怀提示。</small>
          </div>
          <el-button type="primary" @click="notificationVisible = true">发起通知</el-button>
        </div>
        <el-table :data="dashboard.notifications" height="330" stripe>
          <el-table-column prop="title" label="标题" min-width="170" show-overflow-tooltip />
          <el-table-column prop="target_type" label="对象" width="86">
            <template #default="{ row }">
              {{ targetLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="channel" label="渠道" min-width="120" />
          <el-table-column prop="status" label="状态" width="80" />
        </el-table>
      </section>
    </section>

    <section class="panel table-panel">
      <div class="panel-header compact">
        <div>
          <h2 class="panel-title">服务效果量化分析</h2>
          <small>对比服务前后员工留存、幸福感、生产力和岗位适配度变化。</small>
        </div>
      </div>
      <el-table :data="dashboard.effectMetrics" stripe>
        <el-table-column prop="period_label" label="周期" width="110" />
        <el-table-column prop="department_name" label="部门" min-width="120" />
        <el-table-column prop="retention_rate" label="留存率" width="100">
          <template #default="{ row }">{{ row.retention_rate }}%</template>
        </el-table-column>
        <el-table-column prop="happiness_delta" label="幸福感变化" width="116" />
        <el-table-column prop="productivity_delta" label="生产力变化" width="116" />
        <el-table-column prop="fit_delta" label="适配度变化" width="116" />
        <el-table-column prop="report_summary" label="分析摘要" min-width="260" show-overflow-tooltip />
      </el-table>
    </section>

    <el-dialog v-model="notificationVisible" title="发起服务通知" width="560px">
      <el-form label-width="96px">
        <el-form-item label="通知对象">
          <el-select v-model="notificationForm.targetType">
            <el-option label="全员" value="all" />
            <el-option label="部门" value="department" />
            <el-option label="员工" value="employee" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象ID">
          <el-input-number v-model="notificationForm.targetId" :min="0" :disabled="notificationForm.targetType === 'all'" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="notificationForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="notificationForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="notificationForm.channel">
            <el-option label="企业内部通知" value="企业内部通知" />
            <el-option label="小程序通知" value="小程序通知" />
            <el-option label="短信/企业微信" value="短信/企业微信" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="notificationVisible = false">取消</el-button>
        <el-button type="primary" @click="sendNotification">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ChartBox from '../components/ChartBox.vue'
import { api, apiOrigin } from '../api.js'

const dashboard = ref({
  stats: {},
  riskLevels: [],
  departmentRisk: [],
  moodTrend: [],
  serviceUsage: [],
  careFollowups: [],
  effectMetrics: [],
  notifications: []
})
const chartColors = ['#3f72f5', '#8ed8ff', '#9a5ce4', '#f4c95d', '#70d6c0']
const period = ref('all')
const notificationVisible = ref(false)
const notificationForm = ref({
  targetType: 'all',
  targetId: 0,
  title: '月度情绪筛查提醒',
  content: '本月 AI 情绪筛查已开放，预计 5-8 分钟完成。',
  channel: '企业内部通知'
})
const periodOptions = [
  { label: '全部', value: 'all' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本季度', value: 'quarter' },
  { label: '本年', value: 'year' }
]

const cards = computed(() => [
  { label: '员工总数', value: dashboard.value.stats.employees ?? 0 },
  { label: '高风险预警', value: dashboard.value.stats.highRisk ?? 0 },
  { label: '待跟进预警', value: dashboard.value.stats.carePending ?? 0 },
  { label: '咨询预约量', value: dashboard.value.stats.appointments ?? 0 },
  { label: '课程平均进度', value: `${dashboard.value.stats.courseCompletion ?? 0}%` },
  { label: '员工留存率', value: `${dashboard.value.stats.retentionRate ?? 0}%` }
])

const riskOption = computed(() => ({
  title: { text: '风险等级分布', left: 16, top: 12 },
  tooltip: { trigger: 'item' },
  color: chartColors,
  series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '56%'], data: dashboard.value.riskLevels }]
}))

const departmentOption = computed(() => ({
  title: { text: '部门平均压力指数', left: 16, top: 12 },
  tooltip: {},
  grid: { left: 48, right: 24, bottom: 42, top: 70 },
  xAxis: { type: 'category', data: dashboard.value.departmentRisk.map((item) => item.name) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: dashboard.value.departmentRisk.map((item) => item.score || 0), itemStyle: { color: '#3f72f5', borderRadius: [6, 6, 0, 0] } }]
}))

const moodOption = computed(() => ({
  title: { text: '情绪与压力趋势', left: 16, top: 12 },
  tooltip: { trigger: 'axis' },
  legend: { top: 44 },
  grid: { left: 48, right: 24, bottom: 36, top: 82 },
  xAxis: { type: 'category', data: dashboard.value.moodTrend.map((item) => item.day) },
  yAxis: { type: 'value' },
  series: [
    { name: '情绪分', type: 'line', smooth: true, data: dashboard.value.moodTrend.map((item) => item.mood), color: '#3f72f5', areaStyle: { color: 'rgba(63, 114, 245, 0.16)' } },
    { name: '工作压力', type: 'line', smooth: true, data: dashboard.value.moodTrend.map((item) => item.stress), color: '#9a5ce4', areaStyle: { color: 'rgba(154, 92, 228, 0.1)' } }
  ]
}))

const usageOption = computed(() => ({
  title: { text: '服务使用情况', left: 16, top: 12 },
  tooltip: {},
  grid: { left: 48, right: 24, bottom: 42, top: 70 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: dashboard.value.serviceUsage.map((item) => item.name) },
  series: [{ type: 'bar', data: dashboard.value.serviceUsage.map((item) => item.value), itemStyle: { color: '#8ed8ff', borderRadius: [0, 6, 6, 0] } }]
}))

onMounted(loadDashboard)

async function loadDashboard() {
  dashboard.value = await api.get('/dashboard', { params: { period: period.value } })
}

function exportReport() {
  window.open(`${apiOrigin}/api/dashboard/export?period=${period.value}`, '_blank')
}

function targetLabel(row) {
  if (row.target_type === 'all') return '全员'
  if (row.target_type === 'department') return row.department_name || '部门'
  return row.employee_no || '员工'
}

async function completeFollowup(row) {
  await api.put(`/dashboard/care-followups/${row.id}/complete`, {
    followNote: '已由管理端完成关怀跟进'
  })
  ElMessage.success('已完成跟进')
  await loadDashboard()
}

async function sendNotification() {
  if (!notificationForm.value.title.trim()) {
    ElMessage.warning('请填写通知标题')
    return
  }
  if (notificationForm.value.targetType !== 'all' && !Number(notificationForm.value.targetId)) {
    ElMessage.warning('请填写部门或员工对象ID')
    return
  }
  await api.post('/dashboard/notifications', {
    ...notificationForm.value,
    targetId: notificationForm.value.targetType === 'all' ? null : notificationForm.value.targetId
  })
  ElMessage.success('通知已发起')
  notificationVisible.value = false
  await loadDashboard()
}
</script>

<style scoped>
.export-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  background:
    linear-gradient(135deg, rgba(237, 244, 255, 0.96), rgba(255, 255, 255, 0.96) 58%, rgba(232, 246, 255, 0.92));
}

.export-panel h2 {
  margin: 0;
  font-size: 18px;
}

.export-panel p {
  margin: 8px 0 0;
  color: #747b92;
  font-size: 13px;
}

.export-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.compact {
  padding: 16px 18px;
}

.panel-header small {
  color: #747b92;
}

.table-panel {
  overflow: hidden;
}

@media (max-width: 1320px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .export-panel,
  .export-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
