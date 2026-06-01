<template>
  <view class="page">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="title">到货登记</view>
      <view class="subtitle">共 {{ list.length }} 条到货记录</view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-wrap">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="input grow"
          v-model.trim="keyword"
          placeholder="搜索编码、名称、运单号、来源"
          confirm-type="search"
          @confirm="loadData"
        />
      </view>
      <button class="btn-primary btn-sm" @tap="add">➕ 新增</button>
    </view>

    <view class="row row-wrap gap-sm mb-md">
      <button class="btn-ghost btn-sm" @tap="exportProgress">导出进度</button>
      <button class="btn-ghost btn-sm" @tap="downloadTemplate">下载登记模板</button>
    </view>

    <!-- 状态筛选 -->
    <picker :range="statusOptions" :value="statusIndex" @change="onStatusChange">
      <view class="filter-bar">
        <text class="filter-icon">⚙️</text>
        <text>状态筛选：</text>
        <text style="color: #4F46E5; font-weight: 600;">{{ statusOptions[statusIndex] }}</text>
        <text class="filter-arrow" style="margin-left: auto; color: #94A3B8;">▼</text>
      </view>
    </picker>

    <!-- 空状态 -->
    <view v-if="list.length === 0" class="empty-state">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无到货记录</text>
      <text class="empty-hint">点击右上角新增到货登记</text>
    </view>

    <!-- 列表 -->
    <view class="card arrival-card" v-for="item in list" :key="item.id">
      <view class="row row-between">
        <view class="col">
          <view class="item-title">{{ item.name || '-' }}</view>
          <view class="item-code">{{ item.materialCode }}</view>
        </view>
        <view class="status-tag" :class="statusMap[item.status] || ''">
          <view class="status-dot"></view>
          {{ item.status || '-' }}
        </view>
      </view>

      <view class="info-grid">
        <view>
          <text class="info-label">采购订单</text>
          <text>{{ item.purchaseOrderNo || '-' }}</text>
        </view>
        <view>
          <text class="info-label">来源</text>
          <text>{{ item.source || '-' }}</text>
        </view>
        <view>
          <text class="info-label">运单号</text>
          <text>{{ item.waybillNo || '-' }}</text>
        </view>
        <view>
          <text class="info-label">到货数量</text>
          <text class="font-semibold text-slate-800">{{ compactNumber(item.arrivalQuantity) }} {{ item.unit || '' }}</text>
        </view>
        <view>
          <text class="info-label">件数</text>
          <text>{{ item.packageCount || 0 }}</text>
        </view>
        <view>
          <text class="info-label">包装</text>
          <text>{{ item.packaging || '-' }}</text>
        </view>
        <view>
          <text class="info-label">供应商</text>
          <text>{{ item.supplier || '-' }}</text>
        </view>
      </view>

      <view class="actions-bar">
        <button class="btn-ghost btn-sm" @tap="edit(item)">
          ✏️ 编辑
        </button>
        <button class="btn-danger btn-sm" @tap="remove(item)">
          🗑️ 删除
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../common/request.js'
import { downloadExcel } from '../../common/file.js'
import { STATUS_OPTIONS, compactNumber } from '../../common/format.js'

export default {
  data() {
    return {
      keyword: '',
      statusOptions: STATUS_OPTIONS,
      statusIndex: 0,
      list: [],
      statusMap: {
        '待认领': 'status-waiting',
        '已认领': 'status-claimed',
        '已验收': 'status-accepted',
        '已上架': 'status-shelved',
        '已入库': 'status-stored'
      }
    }
  },
  onShow() {
    this.loadData()
  },
  onPullDownRefresh() {
    const done = () => uni.stopPullDownRefresh()
    this.loadData().then(done).catch(done)
  },
  methods: {
    compactNumber,
    onStatusChange(event) {
      this.statusIndex = Number(event.detail.value || 0)
      this.loadData()
    },
    async loadData() {
      const status = this.statusIndex === 0 ? '' : this.statusOptions[this.statusIndex]
      this.list = await request({
        url: '/api/arrivals',
        data: { keyword: this.keyword, status },
        loading: false
      }) || []
    },
    add() {
      uni.removeStorageSync('EDIT_ARRIVAL')
      uni.navigateTo({ url: '/pages/arrival-form/arrival-form' })
    },
    exportProgress() {
      const status = this.statusIndex === 0 ? '' : this.statusOptions[this.statusIndex]
      downloadExcel('/api/export/arrivals', {
        keyword: this.keyword,
        status
      })
    },
    downloadTemplate() {
      downloadExcel('/api/arrivals/template')
    },
    edit(item) {
      uni.setStorageSync('EDIT_ARRIVAL', item)
      uni.navigateTo({ url: '/pages/arrival-form/arrival-form?mode=edit' })
    },
    remove(item) {
      uni.showModal({
        title: '删除到货记录',
        content: `确认删除「${item.name || item.materialCode}」？\n删除后无法恢复`,
        confirmColor: '#EF4444',
        success: async res => {
          if (!res.confirm) return
          await request({ url: `/api/arrivals/${item.id}`, method: 'DELETE' })
          uni.showToast({ title: '已删除', icon: 'success' })
          this.loadData()
        }
      })
    }
  }
}
</script>

<style scoped>
.arrival-card {
  position: relative;
}

.arrival-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 28rpx;
  bottom: 28rpx;
  width: 4rpx;
  border-radius: 0 4rpx 4rpx 0;
  background: linear-gradient(180deg, #4F46E5, #818CF8);
}

.filter-arrow {
  font-size: 20rpx;
}
</style>
