<template>
  <view class="page">
    <!-- 用户信息栏 -->
    <view class="user-bar">
      <view class="user-info">
        <view class="user-avatar">{{ avatarText }}</view>
        <view>
          <view class="user-name">{{ displayName }}</view>
          <view class="user-greeting">{{ greeting }}</view>
        </view>
      </view>
      <button class="btn-ghost btn-sm" @tap="logout">退出</button>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-row">
      <view class="stat-card" @tap="go('/pages/materials/materials')">
        <view class="stat-icon">📋</view>
        <view class="stat-value">{{ stats.materials || 0 }}</view>
        <view class="stat-label">物资档案</view>
        <view class="stat-arrow">›</view>
      </view>
      <view class="stat-card" @tap="switchTab('/pages/arrivals/arrivals')">
        <view class="stat-icon">🚚</view>
        <view class="stat-value">{{ totalArrivals }}</view>
        <view class="stat-label">到货记录</view>
        <view class="stat-arrow">›</view>
      </view>
    </view>

    <!-- 状态统计 -->
    <view class="section-header">
      <view class="section-dot"></view>
      <view class="section-title">状态统计</view>
    </view>

    <view class="status-list">
      <view class="status-item" v-for="item in statusRows" :key="item.name" @tap="goStatus(item.name)">
        <view class="row">
          <view class="status-tag" :class="statusMap[item.name] || ''">
            <view class="status-dot"></view>
            {{ item.name }}
          </view>
          <view class="status-count">{{ item.value }}</view>
        </view>
        <view class="status-bar">
          <view class="status-bar-fill" :style="{ width: statusPercent(item.value) + '%', background: statusColor(item.name) }"></view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="section-header">
      <view class="section-dot"></view>
      <view class="section-title">快捷操作</view>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @tap="go('/pages/arrival-form/arrival-form')">
        <view class="quick-icon-wrap" style="background: linear-gradient(135deg, #FEF3C7, #FDE68A);">
          <text class="quick-icon">➕</text>
        </view>
        <view class="quick-label">新增到货</view>
      </view>
      <view class="quick-item" @tap="switchTab('/pages/warehousing/warehousing')">
        <view class="quick-icon-wrap" style="background: linear-gradient(135deg, #DBEAFE, #BFDBFE);">
          <text class="quick-icon">📥</text>
        </view>
        <view class="quick-label">入库处理</view>
      </view>
      <view class="quick-item" @tap="go('/pages/material-form/material-form')">
        <view class="quick-icon-wrap" style="background: linear-gradient(135deg, #D1FAE5, #A7F3D0);">
          <text class="quick-icon">📝</text>
        </view>
        <view class="quick-label">新增物资</view>
      </view>
      <view class="quick-item" @tap="switchTab('/pages/arrivals/arrivals')">
        <view class="quick-icon-wrap" style="background: linear-gradient(135deg, #EDE9FE, #DDD6FE);">
          <text class="quick-icon">🔍</text>
        </view>
        <view class="quick-label">到货查询</view>
      </view>
    </view>
  </view>
</template>

<script>
import { request, clearSession, getUser } from '../../common/request.js'
import { STATUS_OPTIONS, statusClass } from '../../common/format.js'

export default {
  data() {
    return {
      stats: {
        materials: 0,
        byStatus: {}
      },
      user: {},
      statusMap: {
        '待认领': 'status-waiting',
        '已认领': 'status-claimed',
        '已验收': 'status-accepted',
        '已上架': 'status-shelved',
        '已入库': 'status-stored'
      }
    }
  },
  computed: {
    displayName() {
      return this.user.realName || this.user.username || '用户'
    },
    avatarText() {
      const name = this.displayName
      return name.length > 2 ? name.slice(-2) : name
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 12) return '上午好，开始高效工作吧'
      if (h < 18) return '下午好，继续加油'
      return '晚上好，辛苦了'
    },
    statusRows() {
      const byStatus = this.stats.byStatus || {}
      return STATUS_OPTIONS
        .filter(name => name !== '全部')
        .map(name => ({ name, value: byStatus[name] || 0 }))
    },
    totalArrivals() {
      return this.statusRows.reduce((sum, item) => sum + Number(item.value || 0), 0)
    },
    maxStatusValue() {
      const vals = this.statusRows.map(r => r.value)
      return Math.max(...vals, 1)
    }
  },
  onShow() {
    this.user = getUser()
    this.loadStats()
  },
  onPullDownRefresh() {
    const done = () => uni.stopPullDownRefresh()
    this.loadStats().then(done).catch(done)
  },
  methods: {
    statusPercent(value) {
      if (!this.maxStatusValue) return 0
      return Math.max((value / this.maxStatusValue) * 100, 3)
    },
    statusColor(status) {
      const map = {
        '待认领': '#F59E0B',
        '已认领': '#3B82F6',
        '已验收': '#8B5CF6',
        '已上架': '#06B6D4',
        '已入库': '#10B981'
      }
      return map[status] || '#94A3B8'
    },
    goStatus(status) {
      const pages = ['/pages/warehousing/warehousing', '/pages/arrivals/arrivals']
      const target = status === '已入库' ? '/pages/arrivals/arrivals' : '/pages/warehousing/warehousing'
      const idx = pages.indexOf(target)
      if (idx >= 0) {
        uni.switchTab({ url: target })
      }
    },
    async loadStats() {
      const data = await request({ url: '/api/stats', loading: false })
      this.stats = data || { materials: 0, byStatus: {} }
    },
    go(url) {
      uni.navigateTo({ url })
    },
    switchTab(url) {
      uni.switchTab({ url })
    },
    async logout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出当前账号吗？',
        confirmColor: '#4F46E5',
        success: async res => {
          if (!res.confirm) return
          try {
            await request({ url: '/api/mobile/logout', method: 'POST', loading: false })
          } catch (e) {}
          clearSession()
          uni.reLaunch({ url: '/pages/login/login' })
        }
      })
    }
  }
}
</script>

<style scoped>
/* 统计卡片行 */
.stats-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.stats-row .stat-card {
  flex: 1;
  position: relative;
  overflow: hidden;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 16rpx rgba(15, 23, 42, 0.04);
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

.stats-row .stat-card:active {
  transform: scale(0.98);
  transition: transform 0.1s;
}

.stats-row .stat-icon {
  font-size: 44rpx;
  margin-bottom: 12rpx;
}

.stats-row .stat-value {
  font-size: 44rpx;
  font-weight: 800;
  color: #0F172A;
  line-height: 1.1;
}

.stats-row .stat-label {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #64748B;
}

.stats-row .stat-arrow {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-size: 32rpx;
  color: #CBD5E1;
  font-weight: 300;
}

/* 区块标题 */
.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 32rpx 0 20rpx;
}

.section-dot {
  width: 8rpx;
  height: 32rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4F46E5, #818CF8);
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A;
}

/* 状态列表 */
.status-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.status-item {
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.03);
  border: 1rpx solid rgba(226, 232, 240, 0.5);
}

.status-item .row {
  justify-content: space-between;
  align-items: center;
}

.status-count {
  font-size: 32rpx;
  font-weight: 800;
  color: #0F172A;
}

.status-bar {
  height: 6rpx;
  margin-top: 12rpx;
  border-radius: 3rpx;
  background: #F1F5F9;
  overflow: hidden;
}

.status-bar-fill {
  height: 100%;
  border-radius: 3rpx;
  transition: width 0.5s ease;
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 8rpx;
  border-radius: 20rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.03);
  border: 1rpx solid rgba(226, 232, 240, 0.5);
}

.quick-item:active {
  transform: scale(0.95);
  transition: transform 0.1s;
}

.quick-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
}

.quick-icon {
  font-size: 40rpx;
}

.quick-label {
  font-size: 22rpx;
  color: #475569;
  font-weight: 500;
}
</style>
