<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">我的报告</view>
      <view class="hero-sub">筛查结果仅你本人可查看，平台会根据风险等级匹配不同干预方案。</view>
      <image class="hero-visual" src="/static/theme/profile.png" mode="aspectFill" />
    </view>
    <view class="card profile-card" v-if="profile.name">
      <view>
        <view class="title">{{ profile.name }}</view>
        <view class="desc">{{ profile.department_name }} · {{ profile.position }}</view>
      </view>
      <view class="btn secondary logout" @tap="logout">退出登录</view>
    </view>
    <view class="stats-grid">
      <view class="card stat-card">
        <view class="tag">报告数</view>
        <view class="stat">{{ reports.length }}</view>
      </view>
      <view class="card stat-card">
        <view class="tag">平均分</view>
        <view class="stat">{{ averageScore }}</view>
      </view>
    </view>
    <view class="menu-grid">
      <view class="menu-item" @tap="go('/pages/mood/mood')">状态记录</view>
      <view class="menu-item" @tap="go('/pages/consult/consult')">心理支持</view>
      <view class="menu-item" @tap="go('/pages/policy/policy')">政策中心</view>
    </view>

    <view class="section-title">账号设置</view>
    <view class="card settings-card">
      <view class="setting-row" @tap="passwordVisible = !passwordVisible">
        <view>
          <view class="title">修改密码</view>
          <view class="desc">默认演示原密码为 123456，修改后会保存到数据库。</view>
        </view>
        <view class="arrow">{{ passwordVisible ? '收起' : '修改' }}</view>
      </view>
      <view v-if="passwordVisible" class="password-form">
        <input class="input" v-model="passwordForm.oldPassword" password placeholder="原密码" />
        <input class="input" v-model="passwordForm.newPassword" password placeholder="新密码，至少 6 位" />
        <input class="input" v-model="passwordForm.confirmPassword" password placeholder="再次输入新密码" />
        <view class="form-actions">
          <view class="btn secondary action-btn" @tap="resetPasswordForm">取消</view>
          <view class="btn action-btn" @tap="changePassword">保存密码</view>
        </view>
      </view>
      <view class="setting-row" @tap="clearCache">
        <view>
          <view class="title">清除本地缓存</view>
          <view class="desc">清除筛查草稿、课程笔记和政策收藏，不退出当前账号。</view>
        </view>
        <view class="arrow">清除</view>
      </view>
      <view class="setting-row danger-row" @tap="logout">
        <view>
          <view class="title">退出登录</view>
          <view class="desc">退出后需要重新使用手机号和验证码登录。</view>
        </view>
        <view class="arrow danger-text">退出</view>
      </view>
    </view>

    <view v-if="reports.length === 0" class="card empty-card">
      <view class="title">还没有筛查报告</view>
      <view class="desc">完成一次情绪筛查后，系统会形成风险等级、得分趋势和干预建议。</view>
      <view class="btn" @tap="go('/pages/assessment/assessment')">去做筛查</view>
    </view>
    <view v-for="report in reports" :key="report.id" class="card" @tap="openReport(report)">
      <view class="report-head">
        <view>
          <view class="title">{{ report.screening_title || report.risk_level }}</view>
          <view class="desc">{{ report.created_at }}</view>
        </view>
        <view class="score">{{ report.score }}</view>
      </view>
      <view class="desc">{{ report.summary }}</view>
      <view class="tag">{{ report.suggestion }}</view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import { request, requireEmployee } from '../../utils/request.js'

const reports = ref([])
const profile = reactive({})
const passwordVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const averageScore = computed(() => {
  if (!reports.value.length) return '--'
  const total = reports.value.reduce((sum, item) => sum + Number(item.score || 0), 0)
  return Math.round(total / reports.value.length)
})

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前员工账号吗？',
    success: (res) => {
      if (!res.confirm) return
      clearLogin()
      uni.reLaunch({ url: '/pages/login/login' })
    }
  })
}

function clearLogin() {
  uni.removeStorageSync('employeeId')
  uni.removeStorageSync('employeeProfile')
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = false
}

async function changePassword() {
  const employeeId = requireEmployee()
  if (!employeeId) return
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    uni.showToast({ title: '请填写密码', icon: 'none' })
    return
  }
  if (passwordForm.newPassword.length < 6) {
    uni.showToast({ title: '新密码至少 6 位', icon: 'none' })
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    uni.showToast({ title: '两次新密码不一致', icon: 'none' })
    return
  }
  await request('/mobile/password', {
    method: 'PUT',
    data: {
      employeeId,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }
  })
  uni.showToast({ title: '密码已修改' })
  resetPasswordForm()
}

function clearCache() {
  uni.showModal({
    title: '清除本地缓存',
    content: '确认清除草稿、收藏和课程笔记吗？',
    success: (res) => {
      if (!res.confirm) return
      const keys = uni.getStorageInfoSync().keys || []
      keys.forEach((key) => {
        if (key === 'employeeId' || key === 'employeeProfile') return
        uni.removeStorageSync(key)
      })
      uni.showToast({ title: '缓存已清除', icon: 'none' })
    }
  })
}

function openReport(report) {
  uni.navigateTo({ url: `/pages/profile/report-detail?id=${report.id}` })
}

function go(url) {
  const tabPages = ['/pages/assessment/assessment', '/pages/consult/consult', '/pages/profile/profile']
  if (tabPages.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

onShow(async () => {
  const id = requireEmployee()
  if (!id) return
  Object.assign(profile, uni.getStorageSync('employeeProfile') || {})
  reports.value = await request(`/mobile/reports?employeeId=${id}`)
})
</script>

<style scoped>
.profile-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.logout {
  width: 176rpx;
  height: 64rpx;
  font-size: 24rpx;
}

.report-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18rpx;
}

.score {
  width: 90rpx;
  height: 90rpx;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #eef5ff 0%, #ddd3ff 100%);
  color: #315ed6;
  font-weight: 900;
}

.stats-grid,
.menu-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 18rpx;
}

.stat-card {
  display: block;
}

.stat {
  margin-top: 8rpx;
  color: #315ed6;
  font-size: 42rpx;
  font-weight: 900;
}

.menu-grid {
  grid-template-columns: repeat(3, 1fr);
}

.menu-item {
  height: 76rpx;
  display: grid;
  place-items: center;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
  color: #315ed6;
  font-size: 25rpx;
  font-weight: 800;
}

.empty-card {
  display: block;
}

.empty-card .btn {
  margin-top: 18rpx;
}

.settings-card {
  display: block;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #edf1fb;
}

.setting-row:last-child {
  border-bottom: 0;
}

.arrow {
  flex: 0 0 auto;
  color: #3f72f5;
  font-size: 24rpx;
  font-weight: 800;
}

.danger-row .title,
.danger-text {
  color: #d64242;
}

.password-form {
  display: grid;
  gap: 16rpx;
  padding: 16rpx 0 22rpx;
  border-bottom: 1rpx solid #edf1fb;
}

.form-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
}

.action-btn {
  height: 68rpx;
  font-size: 24rpx;
}
</style>
