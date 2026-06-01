<template>
  <view class="page">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="title">{{ mode === 'edit' ? '编辑物资' : '新增物资' }}</view>
      <view class="subtitle">{{ mode === 'edit' ? '修改物资基础信息' : '录入新的物资档案' }}</view>
    </view>

    <!-- 表单卡片 -->
    <view class="card">
      <view class="field">
        <view class="field-label">
          <text class="required"></text>物资编码
        </view>
        <input
          class="input"
          v-model.trim="form.materialCode"
          placeholder="例如 WZ-0001"
        />
      </view>

      <view class="field">
        <view class="field-label">
          <text class="required"></text>物资名称
        </view>
        <input
          class="input"
          v-model.trim="form.name"
          placeholder="请输入物资名称"
        />
      </view>

      <view class="field">
        <view class="field-label">型号规格</view>
        <input
          class="input"
          v-model.trim="form.model"
          placeholder="请输入型号规格"
        />
      </view>

      <view class="field">
        <view class="field-label">计量单位</view>
        <input
          class="input"
          v-model.trim="form.unit"
          placeholder="例如 件、米、台、套"
        />
      </view>

      <view class="field">
        <view class="field-label">备注说明</view>
        <textarea
          class="textarea"
          v-model.trim="form.remark"
          placeholder="可选，填写补充信息"
        />
      </view>
    </view>

    <!-- 操作提示 -->
    <view class="form-hint">
      <text class="hint-icon">💡</text>
      <text>带 * 号为必填项，请确保信息准确</text>
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

function blankForm() {
  return {
    id: null,
    materialCode: '',
    name: '',
    model: '',
    unit: '',
    remark: ''
  }
}

export default {
  data() {
    return {
      mode: 'add',
      form: blankForm()
    }
  },
  onLoad(query) {
    this.mode = query.mode || 'add'
    if (this.mode === 'edit') {
      this.form = Object.assign(blankForm(), uni.getStorageSync('EDIT_MATERIAL') || {})
      uni.setNavigationBarTitle({ title: '编辑物资' })
    } else {
      uni.setNavigationBarTitle({ title: '新增物资' })
    }
  },
  methods: {
    cancel() {
      uni.navigateBack()
    },
    async submit() {
      if (!this.form.materialCode || !this.form.name) {
        uni.showToast({ title: '请填写物资编码和名称', icon: 'none' })
        return
      }
      if (this.mode === 'edit' && this.form.id) {
        await request({
          url: `/api/materials/${this.form.id}`,
          method: 'PUT',
          data: this.form
        })
      } else {
        await request({
          url: '/api/materials',
          method: 'POST',
          data: this.form
        })
      }
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 500)
    }
  }
}
</script>

<style scoped>
.form-hint {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 24rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #FEF9C3;
  font-size: 24rpx;
  color: #854D0E;
}

.hint-icon {
  font-size: 28rpx;
}
</style>
