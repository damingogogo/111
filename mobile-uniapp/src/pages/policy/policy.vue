<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">{{ detailMode ? policy.title : '企业关怀政策' }}</view>
      <view class="hero-sub">查看企业内部生育友好政策、母婴福利和心理关怀说明。</view>
      <image class="hero-visual" src="/static/theme/policy.png" mode="aspectFill" />
    </view>

    <view v-if="!detailMode">
      <input class="input search" v-model="keyword" placeholder="搜索政策、福利、关键词" />
      <view class="tabs">
        <view v-for="item in categories" :key="item" class="tab" :class="{ active: category === item }" @tap="category = item">{{ item }}</view>
      </view>
      <view v-if="visiblePolicies.length === 0" class="card empty-card">
        <view class="title">没有匹配的政策</view>
        <view class="desc">可以换个关键词，或在后台“政策内容”中新增发布状态的政策。</view>
      </view>
      <view v-for="item in visiblePolicies" :key="item.id" class="card policy-card" @tap="openPolicy(item)">
        <view class="title">{{ item.title }}</view>
        <view class="desc clamp">{{ item.content }}</view>
        <view class="tag">{{ item.category }}</view>
        <view class="fav" :class="{ active: isFavorite(item.id) }" @tap.stop="toggleFavorite(item)">收藏</view>
      </view>
    </view>

    <view v-else class="card">
      <view class="tag">{{ policy.category }}</view>
      <view class="policy-content">{{ policy.content }}</view>
      <view class="detail-actions">
        <view class="btn secondary" @tap="toggleFavorite(policy)">{{ isFavorite(policy.id) ? '取消收藏' : '收藏政策' }}</view>
        <view class="btn" @tap="copyPolicy">复制标题</view>
      </view>
    </view>

    <view class="btn" @tap="backHome">{{ detailMode ? '返回政策中心' : '返回首页' }}</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request, requireEmployee } from '../../utils/request.js'

const policy = reactive({})
const policies = ref([])
const detailMode = ref(false)
const category = ref('全部')
const keyword = ref('')
const favorites = ref([])
const favoriteKey = 'favoritePolicies'
const categories = computed(() => ['全部', ...new Set(policies.value.map((item) => item.category).filter(Boolean))])
const visiblePolicies = computed(() => {
  const word = keyword.value.trim()
  return policies.value.filter((item) => {
    const categoryMatched = category.value === '全部' || item.category === category.value
    const wordMatched = !word || `${item.title}${item.category}${item.content}`.includes(word)
    return categoryMatched && wordMatched
  })
})

onLoad(async (query) => {
  if (!requireEmployee()) return
  favorites.value = uni.getStorageSync(favoriteKey) || []
  if (query.id) {
    detailMode.value = true
    Object.assign(policy, await request(`/mobile/policies/${query.id}`))
  } else {
    detailMode.value = false
    policies.value = await request('/mobile/policies')
  }
})

function backHome() {
  if (detailMode.value) {
    detailMode.value = false
    if (!policies.value.length) {
      request('/mobile/policies').then((data) => {
        policies.value = data
      })
    }
    return
  }
  uni.switchTab({ url: '/pages/home/home' })
}

function openPolicy(item) {
  detailMode.value = true
  Object.assign(policy, item)
}

function isFavorite(id) {
  return favorites.value.includes(Number(id))
}

function toggleFavorite(item) {
  const id = Number(item.id)
  if (!id) return
  if (favorites.value.includes(id)) {
    favorites.value = favorites.value.filter((value) => value !== id)
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } else {
    favorites.value = [...favorites.value, id]
    uni.showToast({ title: '已收藏', icon: 'none' })
  }
  uni.setStorageSync(favoriteKey, favorites.value)
}

function copyPolicy() {
  uni.setClipboardData({ data: policy.title || '' })
}
</script>

<style scoped>
.search {
  margin-top: 20rpx;
}

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

.policy-card {
  position: relative;
  display: block;
}

.empty-card {
  display: block;
}

.fav {
  position: absolute;
  right: 22rpx;
  bottom: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #f0f5ff;
  color: #747b92;
  font-size: 22rpx;
  font-weight: 800;
}

.fav.active {
  background: #ede7ff;
  color: #6d4fd8;
}

.clamp {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.policy-content {
  margin-top: 18rpx;
  color: #36405f;
  font-size: 28rpx;
  line-height: 1.9;
}

.detail-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 22rpx;
}
</style>
