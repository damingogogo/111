<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">你好，{{ home.employee?.name || '返岗伙伴' }}</view>
      <view class="hero-sub">你的筛查报告仅本人可见，企业端只看到脱敏统计与必要关怀提示。</view>
      <image class="hero-visual" src="/static/theme/hero-care.png" mode="aspectFill" />
    </view>

    <view class="quick-grid">
      <view class="quick" @tap="go('/pages/assessment/assessment')">
        <text>AI筛查</text>
        <text>5-8分钟</text>
      </view>
      <view class="quick" @tap="go('/pages/mood/mood')">
        <text>记录状态</text>
        <text>每日变化</text>
      </view>
      <view class="quick" @tap="go('/pages/consult/consult')">
        <text>预约咨询</text>
        <text>隐私加密</text>
      </view>
      <view class="quick" @tap="go('/pages/policy/policy')">
        <text>政策中心</text>
        <text>福利说明</text>
      </view>
      <view class="quick" @tap="go('/pages/community/community')">
        <text>互助社区</text>
        <text>主题分享</text>
      </view>
    </view>

    <view class="section-title">服务通知</view>
    <view v-if="home.notifications.length === 0" class="card">
      <view class="desc">暂无新通知。</view>
    </view>
    <view v-for="notice in home.notifications" :key="notice.id" class="card notice-card">
      <view class="notice-top">
        <view class="title">{{ notice.title }}</view>
        <view class="tag">{{ notice.status }}</view>
      </view>
      <view class="desc">{{ notice.content }}</view>
    </view>

    <view class="section-title">最近报告</view>
    <view class="card report" @tap="openLatestReport">
      <view>
        <view class="title">{{ home.latestReport?.risk_level || '暂无报告' }}</view>
        <view class="desc">{{ home.latestReport?.summary || '完成一次 AI 情绪筛查后，这里会生成个人报告。' }}</view>
      </view>
      <view class="score">{{ home.latestReport?.score || '--' }}</view>
    </view>

    <view class="status-grid">
      <view class="card mini-card" @tap="go('/pages/mood/mood')">
        <view class="tag">今日状态</view>
        <view class="title">{{ home.latestMood?.mood_score ? `${home.latestMood.mood_score} 分` : '去记录' }}</view>
        <view class="desc">工作压力 {{ home.latestMood?.work_stress || '--' }}</view>
      </view>
      <view class="card mini-card" @tap="go('/pages/consult/consult')">
        <view class="tag">下次咨询</view>
        <view class="title">{{ home.nextAppointment?.consultant_name || '立即预约' }}</view>
        <view class="desc">{{ home.nextAppointment?.appointment_time || '选择合适时间' }}</view>
      </view>
    </view>

    <view class="section-title">推荐课程</view>
    <view v-for="course in home.courses" :key="course.id" class="card row" @tap="openCourse(course)">
      <image class="cover" :src="imageUrl(course.cover_url)" mode="aspectFill" @error="usePlaceholder(course, 'cover_url')" />
      <view>
        <view class="title">{{ course.title }}</view>
        <view class="desc">{{ course.summary }}</view>
        <view class="tag">{{ course.duration_minutes }} 分钟 · 进度 {{ course.progress || 0 }}% · {{ Number(course.favorite) ? '已收藏' : '可收藏' }}</view>
      </view>
    </view>

    <view class="section-row">
      <view class="section-title">互助社区</view>
      <view class="link" @tap="go('/pages/community/community')">全部</view>
    </view>
    <view v-for="post in home.communityPosts" :key="post.id" class="card policy-card" @tap="go('/pages/community/community')">
      <view class="title">{{ post.title }}</view>
      <view class="desc">{{ post.content }}</view>
      <view class="tag">{{ post.category }} · {{ post.author_name }}</view>
    </view>

    <view class="section-row">
      <view class="section-title">企业关怀政策</view>
      <view class="link" @tap="go('/pages/policy/policy')">全部</view>
    </view>
    <view v-for="policy in home.policies" :key="policy.id" class="card policy-card" @tap="openPolicy(policy)">
      <view class="title">{{ policy.title }}</view>
      <view class="desc">{{ policy.content }}</view>
      <view class="tag">{{ policy.category }}</view>
    </view>
  </view>
</template>

<script setup>
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { reactive } from 'vue'
import { imageUrl, usePlaceholder } from '../../utils/images.js'
import { request, requireEmployee } from '../../utils/request.js'

const home = reactive({ employee: {}, latestReport: {}, latestMood: {}, nextAppointment: {}, courses: [], notifications: [], communityPosts: [], policies: [] })

function go(url) {
  const tabPages = ['/pages/assessment/assessment', '/pages/course/course', '/pages/consult/consult', '/pages/profile/profile']
  if (tabPages.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

function openCourse(course) {
  uni.navigateTo({ url: `/pages/course/detail?id=${course.id}` })
}

function openPolicy(policy) {
  uni.navigateTo({ url: `/pages/policy/policy?id=${policy.id}` })
}

function openLatestReport() {
  if (!home.latestReport?.id) {
    go('/pages/assessment/assessment')
    return
  }
  uni.navigateTo({ url: `/pages/profile/report-detail?id=${home.latestReport.id}` })
}

async function loadHome() {
  const id = requireEmployee()
  if (!id) return
  const data = await request(`/mobile/home?employeeId=${id}`)
  Object.assign(home, data)
}

onShow(loadHome)

onPullDownRefresh(async () => {
  await loadHome()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 20rpx;
}

.quick {
  min-height: 132rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
  box-shadow: 0 12rpx 30rpx rgba(78, 97, 145, 0.08);
}

.quick text:first-child {
  font-size: 27rpx;
  font-weight: 800;
}

.quick text:last-child {
  color: #7a8299;
  font-size: 22rpx;
}

.report {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score {
  width: 106rpx;
  height: 106rpx;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #eef5ff 0%, #ddd3ff 100%);
  color: #315ed6;
  font-size: 36rpx;
  font-weight: 900;
}

.policy-card {
  display: block;
}

.notice-card {
  display: block;
}

.notice-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 20rpx;
}

.mini-card {
  display: block;
  min-height: 140rpx;
}

.section-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.link {
  margin-top: 28rpx;
  color: #3f72f5;
  font-size: 26rpx;
  font-weight: 800;
}
</style>
