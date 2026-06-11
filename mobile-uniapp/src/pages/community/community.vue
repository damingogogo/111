<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">互助社区</view>
      <view class="hero-sub">同频返岗女性的经验分享与主题交流，默认匿名发布，减少孤立感。</view>
      <image class="hero-visual" src="/static/theme/mood.png" mode="aspectFill" />
    </view>

    <view class="tabs">
      <view v-for="item in categories" :key="item" class="tab" :class="{ active: category === item }" @tap="changeCategory(item)">{{ item }}</view>
    </view>

    <view class="card post-form">
      <view class="title">发布主题</view>
      <input class="input" v-model="form.title" placeholder="例如：返岗第一周怎么安排比较不累？" />
      <picker mode="selector" :range="postCategories" @change="form.category = postCategories[$event.detail.value]">
        <view class="input picker">分类：{{ form.category }}</view>
      </picker>
      <textarea class="textarea" v-model="form.content" placeholder="写下经验、问题或今天的小进展" />
      <view class="switch-row">
        <view class="desc">匿名发布</view>
        <switch :checked="form.anonymous" color="#3f72f5" @change="form.anonymous = $event.detail.value" />
      </view>
      <view class="btn" @tap="submitPost">发布</view>
    </view>

    <view class="section-title">主题分享</view>
    <view v-if="posts.length === 0" class="card">
      <view class="desc">还没有主题，先发布一条经验吧。</view>
    </view>
    <view v-for="item in posts" :key="item.id" class="card post-card">
      <view class="post-head">
        <view>
          <view class="title">{{ item.title }}</view>
          <view class="desc">{{ item.author_name }} · {{ item.created_at }}</view>
        </view>
        <view class="tag">{{ item.category }}</view>
      </view>
      <view class="desc content">{{ item.content }}</view>
      <view class="post-actions">
        <view class="mini-pill">回复 {{ item.reply_count || 0 }}</view>
        <view class="mini-pill" @tap="copyPost(item)">复制主题</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { reactive, ref } from 'vue'
import { request, requireEmployee } from '../../utils/request.js'

const category = ref('全部')
const posts = ref([])
const postCategories = ['时间管理', '角色平衡', '职场沟通', '情绪调节', '母婴福利', '经验分享']
const categories = ['全部', ...postCategories]
const form = reactive({
  title: '',
  category: '经验分享',
  content: '',
  anonymous: true
})

async function loadPosts() {
  if (!requireEmployee()) return
  posts.value = await request(`/mobile/community-posts?category=${encodeURIComponent(category.value)}`)
}

async function changeCategory(value) {
  category.value = value
  await loadPosts()
}

async function submitPost() {
  const employeeId = requireEmployee()
  if (!employeeId) return
  if (!form.title.trim() || !form.content.trim()) {
    uni.showToast({ title: '请填写标题和内容', icon: 'none' })
    return
  }
  await request('/mobile/community-posts', {
    method: 'POST',
    data: {
      employeeId,
      title: form.title,
      category: form.category,
      content: form.content,
      anonymous: form.anonymous
    }
  })
  uni.showToast({ title: '已发布', icon: 'none' })
  form.title = ''
  form.content = ''
  await loadPosts()
}

function copyPost(item) {
  uni.setClipboardData({ data: item.title || '' })
}

onShow(loadPosts)
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  margin: 20rpx 0;
}

.tab {
  flex: 0 0 auto;
  padding: 16rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid #edf1fb;
  color: #747b92;
  font-size: 24rpx;
  font-weight: 800;
}

.tab.active {
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
}

.post-form,
.post-card {
  display: block;
}

.picker {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}

.textarea {
  width: 100%;
  min-height: 160rpx;
  margin-top: 16rpx;
  padding: 18rpx;
  border: 1rpx solid #e4eaf7;
  border-radius: 12rpx;
  background: #fbfcff;
  box-sizing: border-box;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.post-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.content {
  margin-top: 16rpx;
}

.post-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.mini-pill {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #f0f5ff;
  color: #315ed6;
  font-size: 23rpx;
  font-weight: 800;
}
</style>
