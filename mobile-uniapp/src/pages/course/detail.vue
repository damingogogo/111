<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">{{ detail.course?.title || '课程详情' }}</view>
      <view class="hero-sub">{{ detail.course?.summary || '围绕返岗适应提供短视频、图文解析和行动练习。' }}</view>
      <image class="hero-visual" src="/static/theme/course.png" mode="aspectFill" />
    </view>

    <view class="card row">
      <image class="cover" :src="imageUrl(detail.course?.cover_url)" mode="aspectFill" @error="useCoursePlaceholder" />
      <view class="grow">
        <view class="title">{{ detail.course?.category }}</view>
        <view class="desc">{{ detail.course?.duration_minutes }} 分钟 · 当前进度 {{ progress }}% · 小测 {{ latestQuizScore }}</view>
        <progress :percent="progress" activeColor="#3f72f5" backgroundColor="#e7eefb" stroke-width="6" />
      </view>
    </view>

    <view class="action-grid">
      <view class="btn secondary" @tap="toggleFavorite">{{ isFavorite ? '取消收藏' : '收藏课程' }}</view>
      <view class="btn secondary" @tap="saveProgress(60)">倍速学习</view>
      <view class="btn" @tap="saveProgress(100)">完成打卡</view>
    </view>

    <view class="section-title">课程内容</view>
    <view class="card">
      <view class="lesson" v-for="item in lessons" :key="item.title">
        <view class="lesson-dot" :class="{ done: checklist[item.title] }" @tap="toggleLesson(item.title)"></view>
        <view>
          <view class="title">{{ item.title }}</view>
          <view class="desc">{{ item.desc }}</view>
        </view>
      </view>
    </view>

    <view class="section-title">思维导图</view>
    <view class="card mind-map-card">
      <view v-for="(node, index) in mindMapNodes" :key="node" class="mind-node">
        <view class="node-index">{{ index + 1 }}</view>
        <view>{{ node }}</view>
      </view>
    </view>

    <view class="section-title">课后小测</view>
    <view class="card quiz-card">
      <view v-for="(question, qIndex) in quizList" :key="qIndex" class="quiz-item">
        <view class="title">{{ question.question }}</view>
        <view
          v-for="(option, oIndex) in question.options"
          :key="oIndex"
          class="option"
          :class="{ selected: quizAnswers[qIndex] === oIndex }"
          @tap="quizAnswers[qIndex] = oIndex"
        >
          {{ option.label }}
        </view>
      </view>
      <view class="btn quiz-submit" @tap="submitQuiz">提交小测</view>
    </view>

    <view class="section-title">学习笔记</view>
    <view class="card">
      <textarea class="textarea" v-model="note" placeholder="写下今天可以执行的一件小事" />
      <view class="note-actions">
        <view class="btn secondary" @tap="clearNote">清空</view>
        <view class="btn" @tap="saveNote">保存笔记</view>
      </view>
      <view v-if="savedNote" class="saved-note">
        <view class="tag">已保存</view>
        <view class="desc">{{ savedNote }}</view>
      </view>
    </view>

    <view class="section-title">学习操作</view>
    <view class="action-grid">
      <view class="btn secondary" @tap="saveProgress(25)">开始学习</view>
      <view class="btn secondary" @tap="saveProgress(60)">学到一半</view>
      <view class="btn" @tap="saveProgress(100)">完成并打卡</view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { imageUrl } from '../../utils/images.js'
import { request, requireEmployee } from '../../utils/request.js'

const detail = reactive({ course: {}, progress: {}, favorite: {}, latestQuiz: {} })
const checklist = reactive({})
const quizAnswers = reactive({})
const note = ref('')
const savedNote = ref('')
const noteKey = computed(() => `course-note-${detail.course?.id || 'pending'}`)
const checklistKey = computed(() => `course-checklist-${detail.course?.id || 'pending'}`)
const progress = computed(() => Number(detail.progress?.progress || 0))
const isFavorite = computed(() => Boolean(detail.favorite?.id))
const latestQuizScore = computed(() => detail.latestQuiz?.id ? `${detail.latestQuiz.score} 分` : '未完成')
const mindMapNodes = computed(() => String(detail.course?.mind_map || '课程目标>核心方法>场景练习>行动打卡').split('>').filter(Boolean))
const quizList = computed(() => {
  try {
    const parsed = JSON.parse(detail.course?.quiz_json || '[]')
    return Array.isArray(parsed) && parsed.length ? parsed : defaultQuiz()
  } catch (error) {
    return defaultQuiz()
  }
})
const lessons = [
  { title: '场景识别', desc: '识别返岗阶段最容易消耗能量的工作和家庭场景。' },
  { title: '方法练习', desc: '用 3-8 分钟完成一次呼吸、沟通或任务拆解练习。' },
  { title: '行动清单', desc: '生成一条今天可以执行的小行动，并记录完成情况。' }
]

onLoad(async (query) => {
  const employeeId = requireEmployee()
  if (!employeeId) return
  const data = await request(`/mobile/courses/${query.id}?employeeId=${employeeId}`)
  Object.assign(detail, data)
  Object.keys(quizAnswers).forEach((key) => delete quizAnswers[key])
  savedNote.value = uni.getStorageSync(noteKey.value) || ''
  note.value = savedNote.value
  Object.assign(checklist, uni.getStorageSync(checklistKey.value) || {})
})

function defaultQuiz() {
  return [
    {
      question: '返岗适应最推荐的第一步是什么？',
      options: [
        { label: '拆成今天能完成的小行动', score: 100 },
        { label: '把所有压力都先忍住', score: 0 }
      ]
    }
  ]
}

function useCoursePlaceholder() {
  detail.course = {
    ...(detail.course || {}),
    cover_url: '/static/theme/course.png'
  }
}

function toggleLesson(title) {
  checklist[title] = !checklist[title]
  uni.setStorageSync(checklistKey.value, { ...checklist })
  const doneCount = lessons.filter((item) => checklist[item.title]).length
  if (doneCount === lessons.length) {
    saveProgress(100)
  } else if (doneCount > 0 && progress.value < 60) {
    saveProgress(60)
  }
}

function saveNote() {
  savedNote.value = note.value.trim()
  uni.setStorageSync(noteKey.value, savedNote.value)
  uni.showToast({ title: '笔记已保存', icon: 'none' })
}

function clearNote() {
  note.value = ''
  savedNote.value = ''
  uni.removeStorageSync(noteKey.value)
}

async function saveProgress(value) {
  const employeeId = requireEmployee()
  if (!employeeId || !detail.course?.id) return
  await request('/mobile/course-progress', {
    method: 'POST',
    data: {
      employeeId,
      courseId: detail.course.id,
      progress: value,
      completed: value >= 100
    }
  })
  detail.progress = { progress: value, completed: value >= 100 }
  uni.showToast({ title: value >= 100 ? '已完成课程' : '进度已保存' })
}

async function toggleFavorite() {
  const employeeId = requireEmployee()
  if (!employeeId || !detail.course?.id) return
  if (isFavorite.value) {
    await request(`/mobile/course-favorites?employeeId=${employeeId}&courseId=${detail.course.id}`, { method: 'DELETE' })
    detail.favorite = {}
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } else {
    await request('/mobile/course-favorites', {
      method: 'POST',
      data: { employeeId, courseId: detail.course.id }
    })
    detail.favorite = { id: Date.now() }
    uni.showToast({ title: '已收藏', icon: 'none' })
  }
}

async function submitQuiz() {
  const employeeId = requireEmployee()
  if (!employeeId || !detail.course?.id) return
  const questions = quizList.value
  const answered = questions.every((_, index) => quizAnswers[index] !== undefined)
  if (!answered) {
    uni.showToast({ title: '请完成所有小测题', icon: 'none' })
    return
  }
  const score = Math.round(questions.reduce((sum, question, index) => {
    const option = question.options[quizAnswers[index]]
    return sum + Number(option?.score || 0)
  }, 0) / questions.length)
  detail.latestQuiz = await request('/mobile/course-quiz', {
    method: 'POST',
    data: {
      employeeId,
      courseId: detail.course.id,
      score,
      answers: { ...quizAnswers }
    }
  })
  uni.showToast({ title: `小测 ${score} 分`, icon: 'none' })
}
</script>

<style scoped>
.grow {
  flex: 1;
}

.lesson {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #edf1fb;
}

.lesson:last-child {
  border-bottom: 0;
}

.lesson-dot {
  width: 18rpx;
  height: 18rpx;
  margin-top: 10rpx;
  border-radius: 50%;
  background: #3f72f5;
}

.lesson-dot.done {
  box-shadow: 0 0 0 8rpx rgba(63, 114, 245, 0.16);
}

.textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 18rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  box-sizing: border-box;
}

.note-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 16rpx;
}

.saved-note {
  margin-top: 16rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.mind-map-card {
  display: grid;
  gap: 14rpx;
}

.mind-node {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 64rpx;
  padding: 0 16rpx;
  border-radius: 12rpx;
  background: #f0f5ff;
  color: #315ed6;
  font-size: 25rpx;
  font-weight: 800;
}

.node-index {
  width: 38rpx;
  height: 38rpx;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #ffffff;
}

.quiz-card,
.quiz-item {
  display: grid;
  gap: 14rpx;
}

.quiz-item {
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid #edf1fb;
}

.option {
  min-height: 68rpx;
  display: flex;
  align-items: center;
  padding: 0 18rpx;
  border: 2rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  color: #36405f;
  font-size: 25rpx;
  font-weight: 700;
}

.option.selected {
  border-color: #3f72f5;
  background: #f0f5ff;
  color: #315ed6;
}

.quiz-submit {
  margin-top: 4rpx;
}
</style>
