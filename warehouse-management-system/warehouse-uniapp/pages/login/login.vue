<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="bg-circle bg-circle-1"></view>
    <view class="bg-circle bg-circle-2"></view>

    <view class="login-content">
      <!-- 品牌区 -->
      <view class="brand">
        <view class="brand-icon">&#128230;</view>
        <view class="brand-title">物资到货入库管理</view>
        <view class="brand-subtitle">移动端管理系统</view>
      </view>

      <!-- 登录卡片 -->
      <view class="login-card">
        <view class="card-header">
          <view class="card-title">欢迎登录</view>
          <view class="card-desc">请选择角色或输入账号密码</view>
        </view>

        <view class="role-list">
          <view
            class="role-item"
            :class="{ active: selectedRole === item.value }"
            v-for="item in roleOptions"
            :key="item.value"
            @tap="selectRole(item)"
          >
            <view class="role-name">{{ item.label }}</view>
            <view class="role-user">{{ item.desc }}</view>
          </view>
        </view>

        <view class="form-body">
          <view class="form-field">
            <view class="field-icon">&#128100;</view>
            <input
              class="field-input"
              v-model.trim="form.username"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
            />
          </view>

          <view class="form-field">
            <view class="field-icon">&#128274;</view>
            <input
              class="field-input"
              v-model="form.password"
              placeholder="请输入密码"
              placeholder-class="placeholder"
              password
            />
          </view>
        </view>

        <button
          class="login-btn"
          :loading="submitting"
          :disabled="submitting"
          @tap="login"
        >
          {{ submitting ? '登录中...' : '登 录' }}
        </button>

        <view class="account-tip">
          <text class="tip-dot">&#9679;</text>
          也可手动输入后台已创建的库管员账号
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request, saveUser } from '../../common/request.js'

export default {
  data() {
    return {
      submitting: false,
      selectedRole: 'keeper',
      roleOptions: [
        { label: '管理员', value: 'admin', desc: '系统维护' },
        { label: '库管员', value: 'keeper', desc: '认领入库' },
        { label: '验收员', value: 'checker', desc: '验收上架' }
      ],
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    selectRole(item) {
      this.selectedRole = item.value
    },
    async login() {
      if (!this.form.username || !this.form.password) {
        uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const user = await request({
          url: '/api/mobile/login',
          method: 'POST',
          data: this.form
        })
        saveUser(user)
        uni.switchTab({ url: '/pages/index/index' })
      } catch (e) {
        // request.js 已经显示了 toast，这里只处理 submitting 状态
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(160deg, #4F46E5 0%, #6366F1 35%, #818CF8 70%, #C7D2FE 100%);
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.bg-circle-1 {
  width: 600rpx;
  height: 600rpx;
  top: -200rpx;
  right: -200rpx;
}

.bg-circle-2 {
  width: 400rpx;
  height: 400rpx;
  bottom: 100rpx;
  left: -150rpx;
}

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 60rpx 48rpx;
}

/* 品牌区 */
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.brand-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.brand-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #FFFFFF;
  text-align: center;
}

.brand-subtitle {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 640rpx;
  padding: 48rpx 40rpx;
  border-radius: 28rpx;
  background: #FFFFFF;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.card-header {
  margin-bottom: 40rpx;
}

.card-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #0F172A;
}

.card-desc {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #94A3B8;
}

/* 表单 */
.form-body {
  margin-bottom: 40rpx;
}

.role-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-bottom: 30rpx;
}

.role-item {
  padding: 18rpx 20rpx;
  border: 1rpx solid #E2E8F0;
  border-radius: 16rpx;
  background: #F8FAFC;
}

.role-item.active {
  border-color: #4F46E5;
  background: #EEF2FF;
}

.role-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #1E293B;
}

.role-user {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #64748B;
}

.form-field {
  display: flex;
  align-items: center;
  gap: 16rpx;
  height: 100rpx;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
  border-radius: 16rpx;
  background: #F8FAFC;
  border: 1rpx solid #E2E8F0;
  transition: border-color 0.2s;
}

.form-field:last-child {
  margin-bottom: 0;
}

.form-field:focus-within {
  border-color: #4F46E5;
  background: #FFFFFF;
}

.field-icon {
  font-size: 32rpx;
  flex-shrink: 0;
}

.field-input {
  flex: 1;
  font-size: 28rpx;
  color: #1E293B;
  height: 100%;
}

.placeholder {
  color: #CBD5E1;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 24rpx rgba(79, 70, 229, 0.35);
}

.login-btn:active {
  opacity: 0.9;
}

.login-btn[disabled] {
  opacity: 0.6;
}

/* 提示 */
.account-tip {
  margin-top: 28rpx;
  text-align: center;
  font-size: 22rpx;
  color: #94A3B8;
}

.tip-dot {
  color: #4F46E5;
  margin-right: 6rpx;
}
</style>
