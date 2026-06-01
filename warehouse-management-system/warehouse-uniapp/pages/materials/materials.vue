<template>
  <view class="page">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="title">物资档案</view>
      <view class="subtitle">共 {{ list.length }} 条记录</view>
    </view>

    <!-- 操作栏 -->
    <view class="search-wrap">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="input grow"
          v-model.trim="keyword"
          placeholder="搜索编码、名称、型号"
          confirm-type="search"
          @confirm="loadData"
        />
      </view>
      <button class="btn-primary btn-sm" @tap="add">➕ 新增</button>
    </view>

    <view class="row row-wrap gap-sm mb-md">
      <button class="btn-ghost btn-sm" @tap="importMaterials">导入物资Excel</button>
      <button class="btn-ghost btn-sm" @tap="importPurchaseOrders">导入采购订单</button>
      <button class="btn-ghost btn-sm" @tap="downloadTemplate">下载模板</button>
      <button class="btn-ghost btn-sm" @tap="exportMaterials">导出物资</button>
      <button class="btn-ghost btn-sm" @tap="exportInboundStatus">导出入库情况</button>
      <button class="btn-ghost btn-sm" @tap="exportReferenceStatus">导出订单引用</button>
    </view>

    <!-- 空状态 -->
    <view v-if="list.length === 0" class="empty-state">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无物资数据</text>
      <text class="empty-hint">点击右上角新增物资</text>
    </view>

    <!-- 列表 -->
    <view class="card material-card" v-for="item in list" :key="item.id" @tap="edit(item)">
      <view class="row row-between">
        <view class="col">
          <view class="item-title">{{ item.name || '-' }}</view>
          <view class="item-code">{{ item.materialCode }}</view>
        </view>
        <view class="unit-badge">
          <text>{{ item.unit || '未填' }}</text>
        </view>
      </view>

      <view class="material-detail" v-if="item.model">
        <text class="detail-label">型号：</text>
        <text class="detail-value">{{ item.model }}</text>
      </view>
      <view class="material-detail" v-if="item.remark">
        <text class="detail-label">备注：</text>
        <text class="detail-value">{{ item.remark }}</text>
      </view>

      <view class="actions-bar">
        <button class="btn-ghost btn-sm" @tap.stop="edit(item)">
          ✏️ 编辑
        </button>
        <button class="btn-danger btn-sm" @tap.stop="remove(item)">
          🗑️ 删除
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../common/request.js'
import { uploadExcel, downloadExcel } from '../../common/file.js'

export default {
  data() {
    return {
      keyword: '',
      list: []
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
    async loadData() {
      this.list = await request({
        url: '/api/materials',
        data: { keyword: this.keyword },
        loading: false
      }) || []
    },
    add() {
      uni.removeStorageSync('EDIT_MATERIAL')
      uni.navigateTo({ url: '/pages/material-form/material-form' })
    },
    async importMaterials() {
      const result = await uploadExcel('/api/materials/import')
      const inserted = result.inserted || 0
      const updated = result.updated || 0
      uni.showToast({ title: `新增${inserted} 更新${updated}`, icon: 'none' })
      this.loadData()
    },
    async importPurchaseOrders() {
      const result = await uploadExcel('/api/purchase-orders/import')
      const inserted = result.inserted || 0
      const updated = result.updated || 0
      uni.showToast({ title: `订单新增${inserted} 更新${updated}`, icon: 'none' })
      this.loadData()
    },
    downloadTemplate() {
      downloadExcel('/api/materials/template')
    },
    exportMaterials() {
      downloadExcel('/api/materials/export', { keyword: this.keyword })
    },
    exportInboundStatus() {
      downloadExcel('/api/export/inbound-status')
    },
    exportReferenceStatus() {
      downloadExcel('/api/export/purchase-order-reference')
    },
    edit(item) {
      uni.setStorageSync('EDIT_MATERIAL', item)
      uni.navigateTo({ url: '/pages/material-form/material-form?mode=edit' })
    },
    remove(item) {
      uni.showModal({
        title: '删除物资',
        content: `确认删除「${item.name || item.materialCode}」？\n删除后无法恢复`,
        confirmColor: '#EF4444',
        success: async res => {
          if (!res.confirm) return
          await request({ url: `/api/materials/${item.id}`, method: 'DELETE' })
          uni.showToast({ title: '已删除', icon: 'success' })
          this.loadData()
        }
      })
    }
  }
}
</script>

<style scoped>
.material-card {
  position: relative;
  transition: transform 0.15s;
}

.material-card:active {
  transform: scale(0.99);
}

.material-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 28rpx;
  bottom: 28rpx;
  width: 4rpx;
  border-radius: 0 4rpx 4rpx 0;
  background: linear-gradient(180deg, #4F46E5, #818CF8);
}

.unit-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 80rpx;
  height: 48rpx;
  padding: 0 16rpx;
  border-radius: 24rpx;
  background: #EEF2FF;
  color: #4F46E5;
  font-size: 22rpx;
  font-weight: 600;
}

.material-detail {
  margin-top: 12rpx;
  padding-left: 16rpx;
  font-size: 24rpx;
  color: #475569;
  border-left: 2rpx solid #E2E8F0;
}

.detail-label {
  color: #94A3B8;
}

.detail-value {
  color: #475569;
}
</style>
