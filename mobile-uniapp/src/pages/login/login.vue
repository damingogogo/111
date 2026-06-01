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
      <view class="agreement" @tap="agreed = !agreed">
        <view class="check" :class="{ checked: agreed }"></view>
        <text>我已阅读并同意隐私保护说明，理解筛查报告仅本人可见。</text>
      </view>
      <view class="btn" :class="{ disabled: !canSubmit }" @tap="activate">激活并进入</view>
      <view class="hint">系统会将账号与企业组织、部门、岗位信息绑定，离职后可由后台停用。</view>
    </view>

    <!-- 快速登录小圆点（连点 3 次切换面板） -->
    <view class="quick-dot" @tap="onDotTap"></view>

    <!-- 快速登录面板 -->
    <view v-if="quickPanelVisible" class="quick-mask" @tap="quickPanelVisible = false">
      <view class="quick-panel" @tap.stop>
        <view class="quick-title">快速登录（演示账号）</view>
        <view class="quick-sub">点击任意账号一键登录，验证码统一为 123456</view>
        <scroll-view scroll-y class="quick-list">
          <view
            v-for="acc in quickAccounts"
            :key="acc.phone"
            class="quick-item"
            @tap="quickLogin(acc)"
          >
            <view class="quick-item-main">
              <view class="quick-item-name">{{ acc.name }} <text class="quick-item-pos">{{ acc.position }}</text></view>
              <view class="quick-item-meta">{{ acc.dept }} · {{ acc.phone }}</view>
            </view>
            <view class="quick-item-go">登录</view>
          </view>
        </scroll-view>
        <view class="quick-close" @tap="quickPanelVisible = false">关闭</view>
      </view>
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

const quickPanelVisible = ref(false)
const tapTimes = ref([])
const agreed = ref(true)
const countdown = ref(0)
const TRIPLE_WINDOW_MS = 800
let timer = null

const quickAccounts = [
  { name: '安然', position: 'HRBP',       dept: '人力资源中心', phone: '13900000001' },
  { name: '白露', position: '产品经理',   dept: '产品研发部',   phone: '13900000002' },
  { name: '楚瑶', position: '运营主管',   dept: '内容运营部',   phone: '13900000003' },
  { name: '丁宁', position: '销售经理',   dept: '商务拓展部',   phone: '13900000004' },
  { name: '方晴', position: '大客户经理', dept: '大客户部',     phone: '13900000005' },
  { name: '顾言', position: '会计',       dept: '财务中心',     phone: '13900000006' },
  { name: '韩溪', position: '合规专员',   dept: '风控合规部',   phone: '13900000007' },
  { name: '姜禾', position: '品牌策划',   dept: '品牌市场部',   phone: '13900000008' },
  { name: '李沐', position: '客户成功',   dept: '客户成功部',   phone: '13900000009' },
  { name: '孟夏', position: '行政专员',   dept: '行政支持部',   phone: '13900000010' }
]

function onDotTap() {
  const now = Date.now()
  tapTimes.value = tapTimes.value.filter(t => now - t < TRIPLE_WINDOW_MS)
  tapTimes.value.push(now)
  if (tapTimes.value.length >= 3) {
    tapTimes.value = []
    quickPanelVisible.value = !quickPanelVisible.value
  }
}

async function quickLogin(acc) {
  form.phone = acc.phone
  form.code = '123456'
  agreed.value = true
  quickPanelVisible.value = false
  await activate()
}

const canSubmit = computed(() => /^1\d{10}$/.test(form.phone) && form.code.length >= 4 && agreed.value)

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
    uni.showToast({ title: agreed.value ? '请填写手机号和验证码' : '请先同意隐私说明', icon: 'none' })
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

/* 快速登录小圆点 */
.quick-dot {
  position: fixed;
  right: 24rpx;
  bottom: 48rpx;
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: rgba(63, 114, 245, 0.28);
  z-index: 90;
}

/* 遮罩 */
.quick-mask {
  position: fixed;
  inset: 0;
  background: rgba(21, 28, 50, 0.42);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100;
}

.quick-panel {
  width: 100%;
  max-height: 78vh;
  background: #fbfcff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 28rpx 28rpx 36rpx;
  box-sizing: border-box;
  box-shadow: 0 -12rpx 32rpx rgba(78, 97, 145, 0.16);
}

.quick-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #121a33;
}

.quick-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #7a8299;
}

.quick-list {
  margin-top: 18rpx;
  max-height: 58vh;
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 18rpx;
  border-radius: 18rpx;
  background: #f0f5ff;
  margin-bottom: 14rpx;
}

.quick-item-main {
  flex: 1;
  min-width: 0;
}

.quick-item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #121a33;
}

.quick-item-pos {
  margin-left: 10rpx;
  font-size: 22rpx;
  font-weight: 400;
  color: #59658a;
  background: #e7eefb;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}

.quick-item-meta {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #7a8299;
}

.quick-item-go {
  margin-left: 18rpx;
  padding: 12rpx 24rpx;
  background: linear-gradient(90deg, #3f72f5 0%, #9a5ce4 100%);
  color: #ffffff;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.quick-close {
  margin-top: 14rpx;
  text-align: center;
  padding: 22rpx;
  background: #f0f5ff;
  border-radius: 16rpx;
  color: #59658a;
  font-size: 26rpx;
}
</style>
