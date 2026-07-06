<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">我的状态追踪</view>
      <view class="hero-sub">记录每日情绪、职场压力和家庭困扰，形成个人心理成长轨迹。</view>
      <image class="hero-visual" src="/static/theme/mood.png" mode="aspectFill" />
    </view>

    <view class="card">
      <view class="mood-presets">
        <view v-for="preset in presets" :key="preset.label" class="preset" @tap="applyPreset(preset)">
          <text>{{ preset.label }}</text>
          <text>{{ preset.score }}</text>
        </view>
      </view>
      <view class="title">今日情绪 {{ form.moodScore }}</view>
      <slider :value="form.moodScore" min="0" max="100" show-value @change="form.moodScore = $event.detail.value" />
      <view class="title">工作压力 {{ form.workStress }}</view>
      <slider :value="form.workStress" min="0" max="100" show-value @change="form.workStress = $event.detail.value" />
      <view class="title">家庭压力 {{ form.familyStress }}</view>
      <slider :value="form.familyStress" min="0" max="100" show-value @change="form.familyStress = $event.detail.value" />
      <textarea class="textarea" v-model="form.note" placeholder="写一点今天的变化" />
      <view class="btn" @tap="submit">保存记录</view>
    </view>

    <view class="section-title">状态趋势</view>
    <view class="card trend-card">
      <view class="trend-line">
        <view>
          <view class="title">{{ summaryText }}</view>
          <view class="desc">最近 {{ logs.length }} 条记录</view>
        </view>
        <view class="score-pill">{{ averageMood }}</view>
      </view>
      <view class="bars">
        <view v-for="item in chartLogs" :key="item.id" class="bar-item">
          <view class="bar-bg">
            <view class="bar-fill" :style="{ height: `${item.mood_score}%` }"></view>
          </view>
          <view class="bar-label">{{ shortDate(item.logged_at) }}</view>
        </view>
      </view>
      <view class="action-grid">
        <view class="btn secondary" @tap="go('/pages/profile/profile')">看报告</view>
        <view class="btn" @tap="go('/pages/consult/consult')">需要支持</view>
      </view>
    </view>

    <view class="section-title">自我照护建议</view>
    <view class="card tip-card">
      <view class="title">{{ careTip.title }}</view>
      <view class="desc">{{ careTip.desc }}</view>
      <view class="btn secondary tip-btn" @tap="applyTip">加入今日记录</view>
    </view>

    <view class="section-title">最近记录</view>
    <view v-if="logs.length === 0" class="card">
      <view class="desc">还没有状态记录。</view>
    </view>
    <view v-for="item in logs" :key="item.id" class="card log-card">
      <view class="log-top">
        <view class="title">情绪 {{ item.mood_score }}</view>
        <view class="tag">{{ item.logged_at }}</view>
      </view>
      <view class="desc">工作压力 {{ item.work_stress }} · 家庭压力 {{ item.family_stress }}</view>
      <view class="desc" v-if="item.note">{{ item.note }}</view>
      <view class="log-actions">
        <view class="btn secondary log-btn" @tap="reuse(item)">复用这条</view>
        <view class="btn danger log-btn" @tap="removeLog(item)">删除</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import { request, requireEmployee } from '../../utils/request.js'

const form = reactive({
  moodScore: 78,
  workStress: 42,
  familyStress: 35,
  note: ''
})
const logs = ref([])
const presets = [
  { label: '平稳', score: 78, work: 38, family: 32 },
  { label: '疲惫', score: 52, work: 70, family: 55 },
  { label: '低落', score: 36, work: 76, family: 68 }
]
const chartLogs = computed(() => logs.value.slice(0, 7).reverse())
const averageMood = computed(() => {
  if (!logs.value.length) return '--'
  const total = logs.value.reduce((sum, item) => sum + Number(item.mood_score || 0), 0)
  return Math.round(total / logs.value.length)
})
const summaryText = computed(() => {
  if (!logs.value.length) return '先记录一次状态'
  if (averageMood.value >= 75) return '整体状态平稳'
  if (averageMood.value >= 55) return '近期略有波动'
  return '建议查看支持资源'
})
const careTip = computed(() => {
  if (Number(form.workStress) >= 70) {
    return { title: '先拆一件小任务', desc: '把当前最重的一件事拆成 15 分钟内能完成的第一步。' }
  }
  if (Number(form.familyStress) >= 65) {
    return { title: '做一次边界沟通', desc: '把今天最需要协助的一件家务或育儿安排说清楚。' }
  }
  if (Number(form.moodScore) < 55) {
    return { title: '安排三分钟呼吸', desc: '先做一次慢呼吸，再决定是否查看支持资源或记录更多细节。' }
  }
  return { title: '保持微小行动', desc: '今天记录一个让你状态稳定的小因素，明天继续观察。' }
})

function applyPreset(preset) {
  form.moodScore = preset.score
  form.workStress = preset.work
  form.familyStress = preset.family
}

function applyTip() {
  form.note = form.note ? `${form.note}\n${careTip.value.desc}` : careTip.value.desc
}

function reuse(item) {
  form.moodScore = Number(item.mood_score || 78)
  form.workStress = Number(item.work_stress || 42)
  form.familyStress = Number(item.family_stress || 35)
  form.note = item.note || ''
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
}

function shortDate(value) {
  return String(value || '').slice(5, 10)
}

function go(url) {
  const tabPages = ['/pages/course/course', '/pages/consult/consult', '/pages/profile/profile']
  if (tabPages.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

async function loadLogs() {
  const id = requireEmployee()
  if (!id) return
  logs.value = await request(`/mobile/moods?employeeId=${id}`)
}

async function submit() {
  const id = requireEmployee()
  if (!id) return
  await request('/mobile/moods', {
    method: 'POST',
    data: { ...form, employeeId: id }
  })
  uni.showToast({ title: '已记录' })
  form.note = ''
  await loadLogs()
}

async function removeLog(item) {
  const id = requireEmployee()
  if (!id) return
  uni.showModal({
    title: '删除记录',
    content: '确认删除这条状态记录吗？',
    success: async (res) => {
      if (!res.confirm) return
      await request(`/mobile/moods/${item.id}?employeeId=${id}`, { method: 'DELETE' })
      uni.showToast({ title: '已删除' })
      await loadLogs()
    }
  })
}

onShow(loadLogs)
</script>

<style scoped>
.title {
  margin-top: 20rpx;
}

.mood-presets {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.preset {
  min-height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18rpx;
  border-radius: 12rpx;
  background: #f0f5ff;
  color: #315ed6;
  font-size: 24rpx;
  font-weight: 800;
}

.textarea {
  width: 100%;
  min-height: 180rpx;
  margin: 20rpx 0;
  padding: 18rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  box-sizing: border-box;
}

.log-card {
  display: block;
}

.log-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.trend-card {
  display: block;
}

.trend-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.score-pill {
  width: 94rpx;
  height: 94rpx;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #eef5ff 0%, #ddd3ff 100%);
  color: #315ed6;
  font-size: 34rpx;
  font-weight: 900;
}

.bars {
  height: 210rpx;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12rpx;
  align-items: end;
  margin-top: 22rpx;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.bar-bg {
  width: 30rpx;
  height: 150rpx;
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

.bar-label {
  color: #7a8299;
  font-size: 18rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 22rpx;
}

.tip-card {
  display: block;
}

.tip-btn {
  height: 68rpx;
  margin-top: 18rpx;
  font-size: 24rpx;
}

.log-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.log-btn {
  height: 66rpx;
  font-size: 24rpx;
}

.danger {
  background: #fff0f0;
  color: #d64242;
  box-shadow: none;
}
</style>
