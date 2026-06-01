<template>
  <view class="page">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="title">{{ mode === 'edit' ? '编辑到货' : '新增到货' }}</view>
      <view class="subtitle">{{ mode === 'edit' ? '修改到货登记信息' : '录入新的到货记录' }}</view>
    </view>

    <!-- 物资信息卡片 -->
    <view class="card">
      <view class="section-header" style="margin: 0 0 20rpx;">
        <view class="section-dot" style="height: 24rpx;"></view>
        <view class="section-title" style="font-size: 26rpx;">物资信息</view>
      </view>

      <view class="field">
        <view class="field-label">
          <text class="required"></text>物资编码
        </view>
        <view class="row" style="gap: 12rpx;">
          <input
            class="input grow"
            v-model.trim="form.materialCode"
            placeholder="输入后可自动带出物资信息"
            @blur="lookupMaterial(true)"
          />
          <button class="btn-ghost" style="min-height: 92rpx; padding: 0 24rpx; font-size: 24rpx;" @tap="lookupMaterial(false)">
            🔍 带出
          </button>
        </view>
      </view>

      <view class="field">
        <view class="field-label">
          <text class="required"></text>物资名称
        </view>
        <input class="input" v-model.trim="form.name" placeholder="请输入名称" />
      </view>

      <view class="row" style="gap: 20rpx;">
        <view class="field" style="flex: 2;">
          <view class="field-label">型号规格</view>
          <input class="input" v-model.trim="form.model" placeholder="型号" />
        </view>
        <view class="field" style="width: 160rpx; flex-shrink: 0;">
          <view class="field-label">单位</view>
          <input class="input" v-model.trim="form.unit" placeholder="单位" />
        </view>
      </view>
    </view>

    <!-- 来源信息卡片 -->
    <view class="card">
      <view class="section-header" style="margin: 0 0 20rpx;">
        <view class="section-dot" style="height: 24rpx;"></view>
        <view class="section-title" style="font-size: 26rpx;">来源信息</view>
      </view>

      <view class="field">
        <view class="field-label">采购订单号</view>
        <view class="row" style="gap: 12rpx;">
          <input class="input grow" v-model.trim="form.purchaseOrderNo" placeholder="可手填；未填时自动匹配" />
          <button class="btn-ghost" style="min-height: 92rpx; padding: 0 24rpx; font-size: 24rpx;" @tap="matchPurchaseOrder(false)">
            匹配
          </button>
        </view>
      </view>

      <view class="field">
        <view class="field-label">订单行号</view>
        <input class="input" v-model.trim="form.purchaseOrderLine" placeholder="采购订单行号" />
      </view>

      <view class="field">
        <view class="field-label">来源</view>
        <input class="input" v-model.trim="form.source" placeholder="例如 项目现场、供应商直送" />
      </view>

      <view class="field">
        <view class="field-label">供应商</view>
        <input class="input" v-model.trim="form.supplier" placeholder="请输入供应商名称" />
      </view>

      <view class="field">
        <view class="field-label">运单号</view>
        <input class="input" v-model.trim="form.waybillNo" placeholder="请输入运单号" />
      </view>
    </view>

    <!-- 数量信息卡片 -->
    <view class="card">
      <view class="section-header" style="margin: 0 0 20rpx;">
        <view class="section-dot" style="height: 24rpx;"></view>
        <view class="section-title" style="font-size: 26rpx;">数量信息</view>
      </view>

      <view class="field">
        <view class="field-label">
          <text class="required"></text>到货数量
        </view>
        <view class="row" style="gap: 12rpx;">
          <input
            class="input grow"
            type="digit"
            v-model="form.arrivalQuantity"
            placeholder="0"
          />
          <button class="btn-voice" @tap="voice('arrivalQuantity', '到货数量')">
            🎤
          </button>
        </view>
      </view>

      <view class="field">
        <view class="field-label">包装方式</view>
        <input class="input" v-model.trim="form.packaging" placeholder="例如 木箱、纸箱、托盘" />
      </view>

      <view class="field">
        <view class="field-label">件数</view>
        <view class="row" style="gap: 12rpx;">
          <input
            class="input grow"
            type="number"
            v-model="form.packageCount"
            placeholder="0"
          />
          <button class="btn-voice" @tap="voice('packageCount', '件数')">
            🎤
          </button>
        </view>
      </view>

      <view class="field">
        <view class="field-label">重量(kg)</view>
        <input class="input" type="digit" v-model="form.weight" placeholder="0" />
      </view>

      <view class="field">
        <view class="field-label">备注说明</view>
        <textarea class="textarea" v-model.trim="form.remark" placeholder="可选，填写补充信息"></textarea>
      </view>
    </view>

    <!-- 底部固定按钮 -->
    <view class="fixed-bottom-actions">
      <view class="row" style="gap: 20rpx;">
        <button class="btn-ghost grow" style="min-height: 92rpx;" @tap="cancel">取消</button>
        <button class="btn-primary grow" style="min-height: 92rpx;" @tap="submit">💾 保存</button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../common/request.js'
import { startVoiceNumber } from '../../common/voice.js'

function blankForm() {
  return {
    id: null,
    materialCode: '',
    name: '',
    model: '',
    unit: '',
    purchaseOrderNo: '',
    purchaseOrderLine: '',
    source: '',
    supplier: '',
    waybillNo: '',
    arrivalQuantity: '',
    packaging: '',
    packageCount: '',
    weight: '',
    remark: ''
  }
}

export default {
  data() {
    return {
      mode: 'add',
      form: blankForm(),
      lastLookupCode: ''
    }
  },
  onLoad(query) {
    this.mode = query.mode || 'add'
    if (this.mode === 'edit') {
      this.form = Object.assign(blankForm(), uni.getStorageSync('EDIT_ARRIVAL') || {})
      uni.setNavigationBarTitle({ title: '编辑到货' })
    } else {
      uni.setNavigationBarTitle({ title: '新增到货' })
    }
  },
  methods: {
    cancel() {
      uni.navigateBack()
    },
    async lookupMaterial(silent = false) {
      if (!this.form.materialCode) {
        if (!silent) {
          uni.showToast({ title: '请先输入物资编码', icon: 'none' })
        }
        return
      }
      if (silent && this.form.materialCode === this.lastLookupCode) {
        return
      }
      const material = await request({
        url: `/api/materials/${encodeURIComponent(this.form.materialCode)}`,
        loading: !silent
      })
      if (!material) {
        if (!silent) {
          uni.showToast({ title: '没有找到物资', icon: 'none' })
        }
        return
      }
      this.form.name = material.name || ''
      this.form.model = material.model || ''
      this.form.unit = material.unit || ''
      this.lastLookupCode = this.form.materialCode
      await this.matchPurchaseOrder(true)
      if (!silent) {
        uni.showToast({ title: '已带出物资信息', icon: 'success' })
      }
    },
    async matchPurchaseOrder(silent = true) {
      if (!this.form.materialCode || this.form.purchaseOrderNo) {
        return
      }
      const order = await request({
        url: '/api/purchase-orders/match',
        data: {
          materialCode: this.form.materialCode,
          supplier: this.form.supplier || ''
        },
        loading: !silent
      })
      if (!order) {
        if (!silent) {
          uni.showToast({ title: '没有可引用的采购订单', icon: 'none' })
        }
        return
      }
      this.form.purchaseOrderNo = order.orderNo || ''
      this.form.purchaseOrderLine = order.orderLine || ''
      if (!this.form.name) this.form.name = order.name || ''
      if (!this.form.model) this.form.model = order.model || ''
      if (!this.form.unit) this.form.unit = order.unit || ''
      if (!this.form.supplier) this.form.supplier = order.supplier || ''
      if (!silent) {
        uni.showToast({ title: '已匹配采购订单', icon: 'success' })
      }
    },
    async voice(field, label) {
      try {
        const number = await startVoiceNumber(label)
        this.form[field] = field === 'packageCount' ? String(Math.round(number)) : String(number)
      } catch (e) {
        // voice helper already shows toast
      }
    },
    buildPayload() {
      return Object.assign({}, this.form, {
        arrivalQuantity: Number(this.form.arrivalQuantity || 0),
        packageCount: parseInt(this.form.packageCount || 0, 10),
        weight: Number(this.form.weight || 0)
      })
    },
    async submit() {
      if (!this.form.materialCode || !this.form.name) {
        uni.showToast({ title: '请填写物资编码和名称', icon: 'none' })
        return
      }
      const payload = this.buildPayload()
      if (this.mode === 'edit' && this.form.id) {
        await request({
          url: `/api/arrivals/${this.form.id}`,
          method: 'PUT',
          data: payload
        })
      } else {
        await request({
          url: '/api/arrivals',
          method: 'POST',
          data: payload
        })
      }
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 500)
    }
  }
}
</script>
