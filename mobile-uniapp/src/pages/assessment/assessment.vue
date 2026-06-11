<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">AI 情绪筛查</view>
      <view class="hero-sub">支持中途暂停与断点续答，提交后实时生成个人筛查报告。</view>
      <image class="hero-visual" src="/static/theme/screening.png" mode="aspectFill" />
    </view>

    <view v-if="!active" class="list">
      <view v-if="screenings.length === 0" class="card empty-card">
        <view class="title">暂无可用筛查</view>
        <view class="desc">请在后台管理的“筛查”和“筛查题库”中启用问卷并配置题目。</view>
      </view>

      <view v-if="draft.screeningId" class="card draft-card">
        <view>
          <view class="title">继续上次筛查</view>
          <view class="desc">{{ draft.title }} · 已答 {{ Object.keys(draft.answers || {}).length }} 题</view>
        </view>
        <view class="draft-actions">
          <view class="mini-btn secondary-mini" @tap="clearDraft">清除</view>
          <view class="mini-btn" @tap="resumeDraft">继续</view>
        </view>
      </view>

      <view v-for="item in screenings" :key="item.id" class="card row">
        <image class="cover" :src="imageUrl(item.cover_url)" mode="aspectFill" @error="usePlaceholder(item, 'cover_url')" />
        <view class="grow">
          <view class="title">{{ item.title }}</view>
          <view class="desc">{{ item.description }}</view>
          <view class="tag">{{ item.estimated_minutes }} 分钟 · {{ item.push_cycle }}</view>
        </view>
        <view class="mini-btn" @tap="start(item)">开始</view>
      </view>
    </view>

    <view v-else>
      <view class="section-title">{{ active.title }}</view>
      <view class="progress-card">
        <view class="desc">已完成 {{ answeredCount }}/{{ questions.length }} 题</view>
        <progress :percent="progressPercent" activeColor="#3f72f5" backgroundColor="#e7eefb" stroke-width="6" />
      </view>
      <view class="card consent-card">
        <view class="title">高风险关怀选择</view>
        <view class="desc">若筛查提示高风险，你可以自主选择是否让企业授权关怀人员收到脱敏提示。企业端只显示部门、工号和风险等级，不展示姓名、手机号和具体答题内容。</view>
        <view class="switch-row">
          <view>
            <view class="tag">{{ enterpriseNoticeConsent ? '愿意接收企业关怀' : '暂不告知企业' }}</view>
          </view>
          <switch :checked="enterpriseNoticeConsent" color="#3f72f5" @change="enterpriseNoticeConsent = $event.detail.value" />
        </view>
      </view>
      <view v-if="questions.length === 0" class="card empty-card">
        <view class="title">该筛查还没有配置题目</view>
        <view class="desc">请在后台“筛查题库”中新增题目，填写筛查ID、题干、选项配置JSON，并设置状态为启用。</view>
      </view>
      <view v-for="question in questions" :key="question.id" class="card">
        <view class="question-head">
          <view class="tag">{{ question.dimension || '综合评估' }}</view>
          <view class="question-no">{{ question.sort_no }}/{{ questions.length }}</view>
        </view>
        <view class="title">{{ question.content }}</view>
        <view class="options">
          <view
            v-for="option in optionList(question)"
            :key="option.value"
            class="option"
            :class="{ selected: answerValue(question.id) === option.value }"
            @tap="setAnswer(question.id, option)"
          >
            <view class="radio-dot"></view>
            <view>{{ option.label }}</view>
          </view>
        </view>
      </view>
      <view class="btn" :class="{ disabled: !canSubmit }" @tap="submit">提交筛查</view>
      <view class="btn secondary space" @tap="pause">稍后再答</view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import { imageUrl, usePlaceholder } from '../../utils/images.js'
import { request, requireEmployee } from '../../utils/request.js'

const screenings = ref([])
const questions = ref([])
const active = ref(null)
const answers = reactive({})
const draft = reactive({ screeningId: null, title: '', answers: {}, enterpriseNoticeConsent: false })
const enterpriseNoticeConsent = ref(false)
const draftKey = 'screeningDraft'
const answeredCount = computed(() => Object.keys(answers).length)
const progressPercent = computed(() => questions.value.length ? Math.round(answeredCount.value / questions.value.length * 100) : 0)
const canSubmit = computed(() => questions.value.length > 0 && answeredCount.value === questions.value.length)
const defaultOptions = [
  { label: '完全不符合', value: 1, score: 20 },
  { label: '比较不符合', value: 2, score: 40 },
  { label: '一般', value: 3, score: 60 },
  { label: '比较符合', value: 4, score: 80 },
  { label: '完全符合', value: 5, score: 100 }
]

async function start(item) {
  active.value = item
  questions.value = await request(`/mobile/screenings/${item.id}/questions`)
  Object.keys(answers).forEach((key) => delete answers[key])
  enterpriseNoticeConsent.value = false
  saveDraft()
}

async function resumeDraft() {
  const item = screenings.value.find((screening) => Number(screening.id) === Number(draft.screeningId))
  if (!item) {
    clearDraft()
    return
  }
  active.value = item
  questions.value = await request(`/mobile/screenings/${item.id}/questions`)
  Object.keys(answers).forEach((key) => delete answers[key])
  Object.assign(answers, draft.answers || {})
  enterpriseNoticeConsent.value = Boolean(draft.enterpriseNoticeConsent)
}

function optionList(question) {
  if (!question?.options_json) return defaultOptions
  try {
    const parsed = JSON.parse(question.options_json)
    if (Array.isArray(parsed) && parsed.length) {
      return parsed.map((item, index) => ({
        label: item.label || `选项 ${index + 1}`,
        value: item.value ?? index + 1,
        score: Number(item.score ?? item.value ?? index + 1)
      }))
    }
  } catch (error) {
    return defaultOptions
  }
  return defaultOptions
}

function answerValue(questionId) {
  return answers[questionId]?.value
}

function setAnswer(questionId, option) {
  answers[questionId] = {
    value: option.value,
    label: option.label,
    score: Number(option.score || 0)
  }
  saveDraft()
}

function saveDraft() {
  if (!active.value) return
  const value = { screeningId: active.value.id, title: active.value.title, answers: { ...answers }, enterpriseNoticeConsent: enterpriseNoticeConsent.value }
  uni.setStorageSync(draftKey, value)
  Object.assign(draft, value)
}

function clearDraft() {
  uni.removeStorageSync(draftKey)
  Object.assign(draft, { screeningId: null, title: '', answers: {}, enterpriseNoticeConsent: false })
  Object.keys(answers).forEach((key) => delete answers[key])
  enterpriseNoticeConsent.value = false
}

function pause() {
  saveDraft()
  active.value = null
  uni.showToast({ title: '已保存草稿', icon: 'none' })
}

async function submit() {
  const id = requireEmployee()
  if (!id) return
  if (!canSubmit.value) {
    uni.showToast({ title: '请完成所有选择题', icon: 'none' })
    return
  }
  const values = Object.values(answers)
  const score = Math.round(values.reduce((sum, val) => sum + Number(val.score || 0), 0) / values.length)
  const report = await request(`/mobile/screenings/${active.value.id}/submit`, {
    method: 'POST',
    data: { employeeId: id, score, answers: { ...answers }, enterpriseNoticeConsent: enterpriseNoticeConsent.value }
  })
  uni.showToast({ title: '报告已生成' })
  active.value = null
  clearDraft()
  if (report?.id) {
    uni.navigateTo({ url: `/pages/profile/report-detail?id=${report.id}` })
  }
}

onLoad(async () => {
  if (!requireEmployee()) return
  Object.assign(draft, uni.getStorageSync(draftKey) || { screeningId: null, title: '', answers: {}, enterpriseNoticeConsent: false })
  enterpriseNoticeConsent.value = Boolean(draft.enterpriseNoticeConsent)
  screenings.value = await request('/mobile/screenings')
})
</script>

<style scoped>
.grow {
  flex: 1;
}

.mini-btn {
  width: 104rpx;
  height: 58rpx;
  display: grid;
  place-items: center;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
  font-size: 24rpx;
  box-shadow: 0 10rpx 20rpx rgba(76, 97, 223, 0.16);
}

.secondary-mini {
  background: #f0f5ff;
  color: #3e5b99;
  box-shadow: none;
}

.draft-card {
  display: block;
}

.draft-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.draft-actions .mini-btn {
  width: 100%;
}

.progress-card {
  padding: 20rpx;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
}

.consent-card {
  display: block;
  margin-top: 18rpx;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 18rpx;
}

.empty-card {
  display: block;
}

.question-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  margin-bottom: 12rpx;
}

.question-no {
  color: #7a8299;
  font-size: 23rpx;
}

.options {
  display: grid;
  gap: 14rpx;
  margin-top: 22rpx;
}

.option {
  min-height: 76rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 18rpx;
  border: 2rpx solid #e4eaf7;
  border-radius: 14rpx;
  background: #fbfcff;
  color: #28314d;
  font-size: 26rpx;
  font-weight: 700;
}

.option.selected {
  border-color: #5b7df4;
  background: #f0f5ff;
  color: #315ed6;
}

.radio-dot {
  width: 28rpx;
  height: 28rpx;
  flex: 0 0 auto;
  border: 4rpx solid #cfd8eb;
  border-radius: 50%;
  box-sizing: border-box;
}

.option.selected .radio-dot {
  border: 8rpx solid #3f72f5;
}

.btn.disabled {
  opacity: 0.45;
}

.space {
  margin-top: 18rpx;
}
</style>
