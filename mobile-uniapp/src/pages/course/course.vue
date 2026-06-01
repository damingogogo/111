<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">职场适应指导</view>
      <view class="hero-sub">短视频、图文解析和课后小测，覆盖时间管理、角色平衡、职业信心重建。</view>
      <image class="hero-visual" src="/static/theme/course.png" mode="aspectFill" />
    </view>
    <input class="input search" v-model="keyword" placeholder="搜索课程、分类或关键词" />
    <view class="tabs">
      <view v-for="item in tabs" :key="item.value" class="tab" :class="{ active: filter === item.value }" @tap="filter = item.value">{{ item.label }}</view>
    </view>
    <view v-if="visibleCourses.length === 0" class="card empty-card">
      <view class="title">没有匹配的课程</view>
      <view class="desc">可以切换筛选条件，或在后台“课程”中新增上架课程。</view>
    </view>
    <view v-for="course in visibleCourses" :key="course.id" class="card">
      <view class="row" @tap="openDetail(course)">
        <image class="cover" :src="imageUrl(course.cover_url)" mode="aspectFill" @error="usePlaceholder(course, 'cover_url')" />
        <view class="grow">
          <view class="title">{{ course.title }}</view>
          <view class="desc">{{ course.summary }}</view>
          <view class="tag">{{ course.category }} · {{ course.duration_minutes }} 分钟 · {{ course.progress || 0 }}%</view>
          <progress :percent="Number(course.progress || 0)" activeColor="#3f72f5" backgroundColor="#e7eefb" stroke-width="5" />
        </view>
      </view>
      <view class="course-actions">
        <view class="btn secondary progress-btn" @tap.stop="openDetail(course)">查看详情</view>
        <view class="btn progress-btn" @tap.stop="learn(course)">{{ Number(course.progress || 0) >= 100 ? '重新打卡' : '完成学习' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { imageUrl, usePlaceholder } from '../../utils/images.js'
import { request, requireEmployee } from '../../utils/request.js'

const courses = ref([])
const filter = ref('all')
const keyword = ref('')
const tabs = [
  { label: '全部', value: 'all' },
  { label: '未完成', value: 'doing' },
  { label: '已完成', value: 'done' }
]
const visibleCourses = computed(() => {
  const word = keyword.value.trim()
  const filtered = courses.value.filter((course) => !word || `${course.title}${course.summary}${course.category}`.includes(word))
  if (filter.value === 'done') {
    return filtered.filter((course) => Number(course.progress || 0) >= 100)
  }
  if (filter.value === 'doing') {
    return filtered.filter((course) => Number(course.progress || 0) < 100)
  }
  return filtered
})

function openDetail(course) {
  uni.navigateTo({ url: `/pages/course/detail?id=${course.id}` })
}

async function learn(course) {
  const id = requireEmployee()
  if (!id) return
  await request('/mobile/course-progress', {
    method: 'POST',
    data: { employeeId: id, courseId: course.id, progress: 100, completed: true }
  })
  course.progress = 100
  course.completed = 1
  uni.showToast({ title: '已打卡' })
}

onShow(async () => {
  const id = requireEmployee()
  if (!id) return
  courses.value = await request(`/mobile/courses?employeeId=${id}`)
})
</script>

<style scoped>
.tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
  margin: 20rpx 0;
}

.search {
  margin-top: 20rpx;
}

.empty-card {
  display: block;
}

.tab {
  height: 68rpx;
  display: grid;
  place-items: center;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
  color: #747b92;
  font-size: 25rpx;
  font-weight: 800;
}

.tab.active {
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
}

.grow {
  flex: 1;
}

.course-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 20rpx;
}

.progress-btn {
  height: 72rpx;
}
</style>
