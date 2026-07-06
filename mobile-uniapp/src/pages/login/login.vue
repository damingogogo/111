<template>
  <view class="page login-page">
    <view class="hero">
      <view class="hero-title">账号激活</view>
      <view class="hero-sub">企业 HR 录入员工信息后，员工使用手机号和验证码激活小程序账号。</view>
      <image class="hero-visual" src="/static/theme/profile.png" mode="aspectFill" />
    </view>

    <view class="card form">
      <view class="label">手机号</view>
      <input class="input" v-model="form.phone" type="number" placeholder="演示：13900000001" />
      <view class="label">验证码</view>
      <view class="code-row">
        <input class="input code-input" v-model="form.code" type="number" placeholder="演示验证码：123456" />
        <view class="code-btn" :class="{ disabled: countdown > 0 }" @tap="sendCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</view>
      </view>
      <view class="agreement" @tap="toggleAgreement">
        <view class="check" :class="{ checked: agreed }"></view>
        <view class="agreement-copy">
          <text>我已阅读并同意</text>
          <text class="link" @tap.stop="openServiceAgreement">《用户服务协议》</text>
          <text>及</text>
          <text class="link" @tap.stop="openPrivacyAgreement">《隐私政策》</text>
          <text>，理解筛查报告仅本人可见。</text>
        </view>
      </view>
      <view class="btn" :class="{ disabled: !canSubmit }" @tap="activate">激活并进入</view>
      <view class="hint">系统会将账号与企业组织、部门、岗位信息绑定，离职后可由后台停用。</view>
    </view>

  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { request } from '../../utils/request.js'

const form = reactive({
  phone: '13900000001',
  code: '123456'
})

const agreed = ref(false)
const countdown = ref(0)
let timer = null

const canSubmit = computed(() => /^1\d{10}$/.test(form.phone) && form.code.length >= 4 && agreed.value)

function toggleAgreement() {
  agreed.value = !agreed.value
}

function openServiceAgreement() {
  uni.showModal({
    title: '用户服务协议',
    content: '本工具用于员工账号激活、情绪记录、筛查问卷、课程学习、政策查看与心理支持资源展示。请确认你理解服务用途后，自主选择是否继续使用。',
    showCancel: false,
    confirmText: '我知道了'
  })
}

function openPrivacyAgreement() {
  if (typeof wx !== 'undefined' && wx.openPrivacyContract) {
    wx.openPrivacyContract({
      fail: showPrivacyAgreementFallback
    })
    return
  }
  showPrivacyAgreementFallback()
}

function showPrivacyAgreementFallback() {
  uni.showModal({
    title: '隐私政策',
    content: '本工具仅收集登录手机号、员工账号信息、情绪记录、筛查作答和学习进度等必要信息，用于提供本人可见的记录与支持服务。未勾选同意前不会进入系统。',
    showCancel: false,
    confirmText: '我知道了'
  })
}

function sendCode() {
  if (countdown.value > 0) return
  if (!/^1\d{10}$/.test(form.phone)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  form.code = '123456'
  countdown.value = 60
  timer && clearInterval(timer)
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
  uni.showToast({ title: '验证码已发送', icon: 'none' })
}

async function activate() {
  if (!canSubmit.value) {
    uni.showToast({ title: agreed.value ? '请填写手机号和验证码' : '请先阅读并同意协议', icon: 'none' })
    return
  }
  const employee = await request('/mobile/activate', {
    method: 'POST',
    data: form
  })
  uni.setStorageSync('employeeId', employee.id)
  uni.setStorageSync('employeeProfile', employee)
  uni.switchTab({ url: '/pages/home/home' })
}
</script>

<style scoped>
.login-page {
  padding-top: 46rpx;
  position: relative;
  min-height: 100vh;
  background:
    linear-gradient(180deg, #f4eeff 0%, rgba(247, 251, 255, 0) 330rpx),
    #f7fbff;
}

.form {
  margin-top: 24rpx;
}

.label {
  margin: 22rpx 0 12rpx;
  color: #68718b;
  font-size: 25rpx;
}

.input {
  width: 100%;
  box-sizing: border-box;
}

.code-row {
  display: grid;
  grid-template-columns: 1fr 190rpx;
  gap: 14rpx;
}

.code-input {
  min-width: 0;
}

.code-btn {
  height: 76rpx;
  display: grid;
  place-items: center;
  border-radius: 12rpx;
  background: #f0f5ff;
  color: #315ed6;
  font-size: 24rpx;
  font-weight: 800;
}

.disabled {
  opacity: 0.48;
}

.btn {
  margin-top: 28rpx;
}

.agreement {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 22rpx;
  color: #747b92;
  font-size: 23rpx;
  line-height: 1.6;
}

.agreement-copy {
  flex: 1;
  min-width: 0;
}

.link {
  color: #315ed6;
  font-weight: 700;
}

.check {
  width: 30rpx;
  height: 30rpx;
  flex: 0 0 auto;
  margin-top: 4rpx;
  border: 3rpx solid #cfd8eb;
  border-radius: 8rpx;
  box-sizing: border-box;
}

.check.checked {
  border-color: #3f72f5;
  background: #3f72f5;
}

.hint {
  margin-top: 22rpx;
  color: #7a8299;
  font-size: 24rpx;
  line-height: 1.7;
}

</style>
