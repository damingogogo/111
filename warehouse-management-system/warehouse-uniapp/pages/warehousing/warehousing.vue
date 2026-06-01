<template>
  <view class="page">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="title">入库处理</view>
      <view class="subtitle">认领、验收、上架、入库</view>
    </view>

    <view class="row row-wrap gap-sm mb-md">
      <button class="btn-ghost btn-sm" @tap="exportProgress">导出当前进度</button>
      <button class="btn-ghost btn-sm" @tap="exportInboundStatus">导出入库情况</button>
      <button class="btn-ghost btn-sm" @tap="exportReferenceStatus">导出订单引用</button>
    </view>

    <!-- 状态筛选 -->
    <picker :range="statusOptions" :value="statusIndex" @change="onStatusChange">
      <view class="filter-bar">
        <text class="filter-icon">⚙️</text>
        <text>状态筛选：</text>
        <text style="color: #4F46E5; font-weight: 600;">{{ statusOptions[statusIndex] }}</text>
        <text style="margin-left: auto; color: #94A3B8; font-size: 20rpx;">▼</text>
      </view>
    </picker>

    <!-- 空状态 -->
    <view v-if="list.length === 0" class="empty-state">
      <text class="empty-icon">✅</text>
      <text class="empty-text">暂无待处理记录</text>
      <text class="empty-hint">所有到货记录已处理完毕</text>
    </view>

    <!-- 列表 -->
    <view class="card warehouse-card" v-for="item in list" :key="item.id">
      <!-- 顶部：标题 + 状态 -->
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

      <!-- 信息网格 -->
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
          <text class="info-label">管库员</text>
          <text>{{ item.warehouseKeeper || '-' }}</text>
        </view>
        <view>
          <text class="info-label">验收时间</text>
          <text>{{ formatDateTime(item.acceptanceTime) || '-' }}</text>
        </view>
        <view>
          <text class="info-label">上架时间</text>
          <text>{{ formatDateTime(item.shelvingTime) || '-' }}</text>
        </view>
        <view>
          <text class="info-label">入库单号</text>
          <text>{{ item.receiptNumber || '-' }}</text>
        </view>
      </view>

      <!-- 流程操作按钮 -->
      <view class="flow-actions">
        <view v-if="item.status === '待认领'" class="flow-step">
          <view class="flow-label">当前步骤：认领处理</view>
          <button class="btn-primary btn-sm btn-block" style="margin-top: 12rpx;" @tap="openDialog('claim', item)">
            👤 认领
          </button>
        </view>

        <view v-else-if="item.status === '已认领'" class="flow-step">
          <view class="flow-label">当前步骤：验收确认</view>
          <button class="btn-success btn-sm btn-block" style="margin-top: 12rpx;" @tap="openDialog('accept', item)">
            ✓ 确认验收
          </button>
        </view>

        <view v-else-if="item.status === '已验收'" class="flow-step">
          <view class="flow-label">当前步骤：上架操作</view>
          <button class="btn-ghost btn-sm btn-block" style="margin-top: 12rpx; border-color: #4F46E5; color: #4F46E5; background: #EEF2FF;" @tap="openDialog('shelf', item)">
            📦 确认上架
          </button>
        </view>

        <view v-else-if="item.status === '已上架'" class="flow-step">
          <view class="flow-label">当前步骤：最终入库</view>
          <button class="btn-primary btn-sm btn-block" style="margin-top: 12rpx;" @tap="openDialog('store', item)">
            🏭 入库
          </button>
        </view>

        <view v-else class="flow-step">
          <view class="flow-label" style="color: #10B981;">✓ 已完成入库</view>
        </view>

        <!-- 编辑按钮 -->
        <view class="actions-bar" style="margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9;">
          <button class="btn-ghost btn-sm" @tap="edit(item)">
            ✏️ 编辑信息
          </button>
        </view>
      </view>
    </view>

    <!-- 弹窗 -->
    <view v-if="dialog.show" class="dialog-mask" @tap="closeDialog">
      <view class="dialog-box" @tap.stop>
        <view class="dialog-title">{{ dialog.title }}</view>
        <view v-if="dialog.type === 'claim' || dialog.type === 'store'">
          <input
            class="input"
            v-model.trim="dialog.value"
            :placeholder="dialog.placeholder"
            style="text-align: center;"
          />
        </view>
        <view v-else>
          <view class="field">
            <view class="field-label">{{ dialog.type === 'accept' ? '验收完成日期' : '上架日期' }}</view>
            <picker mode="date" :value="dialog.date" @change="onDialogDateChange">
              <view class="picker-box">
                <text>{{ dialog.date }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view class="field">
            <view class="field-label">{{ dialog.type === 'accept' ? '验收完成时间' : '上架时间' }}</view>
            <picker mode="time" :value="dialog.time" @change="onDialogTimeChange">
              <view class="picker-box">
                <text>{{ dialog.time }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="row dialog-actions">
          <button class="btn-ghost grow" @tap="closeDialog">取消</button>
          <button class="btn-primary grow" @tap="confirmDialog">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request, getUser } from '../../common/request.js'
import { downloadExcel } from '../../common/file.js'
import { STATUS_OPTIONS, compactNumber, formatDateTime } from '../../common/format.js'

function pad(number) {
  return Number(number) < 10 ? `0${number}` : String(number)
}

function currentParts() {
  const date = new Date()
  return {
    date: `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
    time: `${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
}

function toPickerParts(value) {
  if (!value) return currentParts()
  const text = String(value).replace('T', ' ')
  return {
    date: text.slice(0, 10),
    time: text.slice(11, 16) || currentParts().time
  }
}

export default {
  data() {
    return {
      statusOptions: STATUS_OPTIONS,
      statusIndex: 0,
      list: [],
      statusMap: {
        '待认领': 'status-waiting',
        '已认领': 'status-claimed',
        '已验收': 'status-accepted',
        '已上架': 'status-shelved',
        '已入库': 'status-stored'
      },
      dialog: {
        show: false,
        type: '',
        title: '',
        placeholder: '',
        value: '',
        date: '',
        time: '',
        item: null
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
    formatDateTime,
    onStatusChange(event) {
      this.statusIndex = Number(event.detail.value || 0)
      this.loadData()
    },
    async loadData() {
      const status = this.statusIndex === 0 ? '' : this.statusOptions[this.statusIndex]
      this.list = await request({
        url: '/api/arrivals',
        data: { status },
        loading: false
      }) || []
    },
    exportProgress() {
      const status = this.statusIndex === 0 ? '' : this.statusOptions[this.statusIndex]
      downloadExcel('/api/export/arrivals', { status })
    },
    exportInboundStatus() {
      downloadExcel('/api/export/inbound-status')
    },
    exportReferenceStatus() {
      downloadExcel('/api/export/purchase-order-reference')
    },
    edit(item) {
      uni.setStorageSync('EDIT_ARRIVAL', item)
      uni.navigateTo({ url: '/pages/arrival-form/arrival-form?mode=edit' })
    },
    openDialog(type, item) {
      const user = getUser()
      const timeValue = type === 'accept' ? item.acceptanceTime : item.shelvingTime
      const parts = toPickerParts(timeValue)
      const titleMap = {
        claim: '填写管库员',
        accept: '填写验收完成时间',
        shelf: '填写上架时间',
        store: '填写入库单号'
      }
      const placeholderMap = {
        claim: '请输入管库员姓名',
        store: '请输入入库单号'
      }
      this.dialog = {
        show: true,
        type,
        item,
        title: titleMap[type],
        placeholder: placeholderMap[type] || '',
        value: type === 'claim' ? (item.warehouseKeeper || user.realName || user.username || '') : (item.receiptNumber || ''),
        date: parts.date,
        time: parts.time
      }
    },
    closeDialog() {
      this.dialog.show = false
    },
    onDialogDateChange(event) {
      this.dialog.date = event.detail.value
    },
    onDialogTimeChange(event) {
      this.dialog.time = event.detail.value
    },
    async confirmDialog() {
      if ((this.dialog.type === 'claim' || this.dialog.type === 'store') && !this.dialog.value) {
        uni.showToast({ title: '请填写内容', icon: 'none' })
        return
      }
      const item = this.dialog.item
      const dataMap = {
        claim: { keeper: this.dialog.value },
        accept: { acceptanceTime: `${this.dialog.date} ${this.dialog.time}:00` },
        shelf: { shelvingTime: `${this.dialog.date} ${this.dialog.time}:00` },
        store: { receiptNumber: this.dialog.value }
      }
      await request({
        url: `/api/arrivals/${item.id}/${this.dialog.type}`,
        method: 'POST',
        data: dataMap[this.dialog.type]
      })
      this.closeDialog()
      uni.showToast({ title: '处理成功', icon: 'success' })
      this.loadData()
    }
  }
}
</script>

<style scoped>
.warehouse-card {
  position: relative;
}

.warehouse-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 28rpx;
  bottom: 28rpx;
  width: 4rpx;
  border-radius: 0 4rpx 4rpx 0;
  background: linear-gradient(180deg, #4F46E5, #818CF8);
}

.flow-actions {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F1F5F9;
}

.flow-step {
  padding: 4rpx 0;
}

.flow-label {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.flow-label::before {
  content: '➤ ';
  color: #4F46E5;
}
</style>
