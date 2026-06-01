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
    </view>

    <view class="section-title">匹配干预方案</view>
    <view v-for="plan in detail.plans" :key="plan.id" class="card">
      <view class="title">{{ plan.title }}</view>
      <view class="desc">{{ plan.content }}</view>
      <view class="tag">{{ plan.action_type }}</view>
      <view class="btn secondary plan-btn" @tap="openPlan(plan)">执行方案</view>
    </view>

    <view class="action-grid">
      <view class="btn secondary" @tap="go('/pages/course/course')">查看课程</view>
      <view class="btn" @tap="go('/pages/consult/consult')">预约咨询</view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request, requireEmployee } from '../../utils/request.js'

const detail = reactive({ report: {}, plans: [], history: [] })
const chartHistory = computed(() => (detail.history || []).slice().reverse())

onLoad(async (query) => {
  if (!requireEmployee()) return
  const data = await request(`/mobile/reports/${query.id}`)
  Object.assign(detail, data)
})

function go(url) {
  uni.switchTab({ url })
}

function openPlan(plan) {
  const action = String(plan.action_type || '')
  if (action.includes('咨询')) {
    go('/pages/consult/consult')
    return
  }
  go('/pages/course/course')
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
  margin-top: 18rpx;
  font-size: 24rpx;
}
</style>
