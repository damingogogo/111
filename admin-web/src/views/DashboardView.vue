<template>
  <div class="grid">
    <section class="panel export-panel">
      <div>
        <h2>服务统计总览</h2>
        <p>按企业管理端脱敏口径导出筛查、课程、咨询、状态追踪等核心数据。</p>
      </div>
      <el-button type="primary" @click="exportReport">导出报表</el-button>
    </section>
    <section class="grid grid-4">
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ChartBox from '../components/ChartBox.vue'
import { api, apiOrigin } from '../api.js'

const dashboard = ref({
  stats: {},
  riskLevels: [],
  departmentRisk: [],
  moodTrend: [],
  serviceUsage: []
})
const chartColors = ['#3f72f5', '#8ed8ff', '#9a5ce4', '#f4c95d', '#70d6c0']

const cards = computed(() => [
  { label: '员工总数', value: dashboard.value.stats.employees ?? 0 },
  { label: '高风险预警', value: dashboard.value.stats.highRisk ?? 0 },
  { label: '咨询预约量', value: dashboard.value.stats.appointments ?? 0 },
  { label: '课程平均进度', value: `${dashboard.value.stats.courseCompletion ?? 0}%` }
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

onMounted(async () => {
  dashboard.value = await api.get('/dashboard')
})

function exportReport() {
  window.open(`${apiOrigin}/api/dashboard/export`, '_blank')
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
</style>
