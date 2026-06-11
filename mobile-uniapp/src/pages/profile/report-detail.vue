<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">{{ detail.report?.risk_level || '报告详情' }}</view>
      <view class="hero-sub">{{ detail.report?.summary || '完成筛查后，系统会生成个人情绪报告和干预建议。' }}</view>
      <image class="hero-visual" src="/static/theme/report.png" mode="aspectFill" />
    </view>

    <view class="card score-card">
      <view>
        <view class="title">{{ detail.report?.screening_title || 'AI 情绪筛查' }}</view>
        <view class="desc">{{ detail.report?.created_at }}</view>
      </view>
      <view class="score">{{ detail.report?.score || '--' }}</view>
    </view>

    <view class="section-title">得分趋势</view>
    <view class="card trend-card">
      <view class="bars">
        <view v-for="item in chartHistory" :key="item.id" class="bar-item">
          <view class="bar-bg">
            <view class="bar-fill" :style="{ height: `${item.score}%` }"></view>
          </view>
          <view class="bar-score">{{ item.score }}</view>
        </view>
      </view>
    </view>

    <view class="section-title">AI 建议</view>
    <view class="card">
      <view class="desc">{{ detail.report?.suggestion || '暂无建议' }}</view>
      <view class="tag risk-points" v-if="detail.report?.risk_points">风险点：{{ detail.report.risk_points }}</view>
      <view class="tag" v-if="detail.report?.risk_level === '高风险'">
        {{ Number(detail.report.enterprise_notice_consent) ? '已同意企业脱敏关怀提示' : '未告知企业，仅保留个人建议' }}
      </view>
    </view>

    <view class="section-title">匹配干预方案</view>
    <view v-for="plan in detail.plans" :key="plan.id" class="card">
      <view class="title">{{ plan.title }}</view>
      <view class="desc">{{ plan.content }}</view>
      <view class="tag">{{ plan.action_type }}</view>
      <textarea class="textarea plan-note" v-model="planNotes[plan.id]" placeholder="记录执行前后的状态变化（可选）" />
      <view class="plan-actions">
        <view class="btn secondary plan-btn" @tap="recordPlan(plan, '收藏')">收藏</view>
        <view class="btn secondary plan-btn" @tap="openPlan(plan)">打开入口</view>
        <view class="btn plan-btn" @tap="recordPlan(plan, '打卡')">完成打卡</view>
      </view>
    </view>

    <view class="section-title">执行记录</view>
    <view v-if="records.length === 0" class="card">
      <view class="desc">还没有干预执行记录。</view>
    </view>
    <view v-for="record in records" :key="record.id" class="card record-card">
      <view class="record-top">
        <view class="title">{{ record.plan_title || record.action_type }}</view>
        <view class="tag">{{ record.action }}</view>
      </view>
      <view class="desc">{{ record.state_note || '已记录一次方案执行' }}</view>
      <view class="desc">{{ record.created_at }}</view>
    </view>

    <view class="action-grid">
      <view class="btn secondary" @tap="go('/pages/course/course')">查看课程</view>
      <view class="btn" @tap="go('/pages/consult/consult')">预约咨询</view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request, requireEmployee } from '../../utils/request.js'

const detail = reactive({ report: {}, plans: [], history: [] })
const records = ref([])
const planNotes = reactive({})
const chartHistory = computed(() => (detail.history || []).slice().reverse())

onLoad(async (query) => {
  if (!requireEmployee()) return
  const data = await request(`/mobile/reports/${query.id}`)
  Object.assign(detail, data)
  await loadRecords()
})

function go(url) {
  const tabPages = ['/pages/course/course', '/pages/consult/consult', '/pages/profile/profile']
  if (tabPages.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

function openPlan(plan) {
  recordPlan(plan, '查看')
  const action = String(plan.action_type || '')
  if (action.includes('咨询')) {
    go('/pages/consult/consult')
    return
  }
  if (action.includes('状态')) {
    go('/pages/mood/mood')
    return
  }
  go('/pages/course/course')
}

async function recordPlan(plan, action) {
  const employeeId = requireEmployee()
  if (!employeeId) return
  await request('/mobile/intervention-records', {
    method: 'POST',
    data: {
      employeeId,
      planId: plan.id,
      reportId: detail.report?.id,
      action,
      stateNote: planNotes[plan.id] || `${action}${plan.title}`
    }
  })
  if (action !== '查看') {
    uni.showToast({ title: action === '打卡' ? '已打卡' : '已收藏', icon: 'none' })
  }
  planNotes[plan.id] = ''
  await loadRecords()
}

async function loadRecords() {
  const employeeId = requireEmployee()
  if (!employeeId) return
  records.value = await request(`/mobile/intervention-records?employeeId=${employeeId}`)
}
</script>

<style scoped>
.score-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.score {
  width: 116rpx;
  height: 116rpx;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #eef5ff 0%, #ddd3ff 100%);
  color: #315ed6;
  font-size: 38rpx;
  font-weight: 900;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 24rpx;
}

.trend-card {
  display: block;
}

.bars {
  height: 190rpx;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14rpx;
  align-items: end;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.bar-bg {
  width: 34rpx;
  height: 140rpx;
  display: flex;
  align-items: end;
  border-radius: 999rpx;
  background: #e7eefb;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  min-height: 8rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #3f72f5 0%, #8ed8ff 100%);
}

.bar-score {
  color: #747b92;
  font-size: 20rpx;
}

.plan-btn {
  height: 68rpx;
  font-size: 24rpx;
}

.risk-points {
  margin-top: 16rpx;
}

.plan-note {
  width: 100%;
  min-height: 120rpx;
  margin-top: 16rpx;
  padding: 16rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  box-sizing: border-box;
}

.plan-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-top: 16rpx;
}

.record-card {
  display: block;
}

.record-top {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}
</style>
