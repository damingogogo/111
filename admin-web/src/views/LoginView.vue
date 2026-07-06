<template>
  <main class="login-page">
    <section class="login-shell">
      <aside class="brand-panel">
        <div class="brand-logo">
          <span>返</span>
          <strong>情绪支持</strong>
        </div>
        <h1>产后返岗情绪支持平台</h1>
        <p>企业级员工心理健康与返岗适应管理后台，统一管理员工、筛查、课程、政策与数据统计。</p>
        <div class="brand-line"></div>
        <div class="brand-foot">
          <strong>管理端</strong>
          <span>网页后台 · 微信小程序 · 数据看板</span>
        </div>
      </aside>

      <section class="form-panel">
        <div class="form-head">
          <h2>登录</h2>
          <p>使用管理员账号进入后台</p>
        </div>

        <el-form class="login-form" :model="form" @submit.prevent="login">
          <label class="field-label" for="admin-username">账号</label>
          <el-form-item>
            <el-input
              id="admin-username"
              v-model.trim="form.username"
              size="large"
              name="username"
              autocomplete="username"
              placeholder="请输入账号"
              clearable
              @keyup.enter="login"
            />
          </el-form-item>

          <label class="field-label" for="admin-password">密码</label>
          <el-form-item>
            <el-input
              id="admin-password"
              v-model="form.password"
              size="large"
              type="password"
              name="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="login"
            />
          </el-form-item>

          <div class="form-helper">
            <span>演示账号：admin</span>
            <span>密码：123456</span>
          </div>

          <el-button class="login-button" size="large" native-type="submit" :loading="loading">
            登录
          </el-button>
        </el-form>
      </section>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api.js'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

async function login() {
  loading.value = true
  try {
    const data = await api.post('/auth/login', form)
    localStorage.setItem('emotion-admin-token', data.token)
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 24px;
  background: #f7f8fb;
  overflow: hidden;
  position: relative;
}

.login-page::before,
.login-page::after {
  content: "";
  position: absolute;
  pointer-events: none;
  z-index: 0;
}

.login-page::before {
  width: 460px;
  height: 460px;
  right: -110px;
  bottom: -140px;
  border-radius: 50%;
  background: #e8edf5;
}

.login-page::after {
  width: 360px;
  height: 240px;
  left: 5vw;
  bottom: 28px;
  background:
    linear-gradient(135deg, transparent 42%, rgba(88, 105, 130, 0.08) 42% 58%, transparent 58%),
    linear-gradient(45deg, transparent 42%, rgba(88, 105, 130, 0.08) 42% 58%, transparent 58%);
  background-size: 88px 88px;
  opacity: 0.75;
}

.login-shell {
  width: min(960px, 100%);
  min-height: 520px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  position: relative;
  z-index: 1;
  background: #ffffff;
  box-shadow: 0 28px 80px rgba(33, 42, 62, 0.15);
}

.brand-panel {
  padding: 52px 54px;
  display: flex;
  flex-direction: column;
  color: #ffffff;
  background:
    radial-gradient(circle at 0 0, rgba(255, 255, 255, 0.18), transparent 34%),
    radial-gradient(circle at 92% 88%, rgba(255, 255, 255, 0.1), transparent 38%),
    linear-gradient(145deg, #536277 0%, #344154 100%);
  position: relative;
  overflow: hidden;
}

.brand-panel::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.2) 1px, transparent 1px);
  background-size: 18px 18px;
  mask-image: radial-gradient(circle at 12% 18%, #000, transparent 46%);
  opacity: 0.7;
}

.brand-panel > * {
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 34px;
  font-weight: 800;
}

.brand-logo span {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #ff6b1a;
  color: #ffffff;
  font-size: 22px;
}

.brand-logo strong {
  font-size: 34px;
}

.brand-panel h1 {
  margin: 46px 0 0;
  max-width: 420px;
  color: #ffffff;
  font-size: 28px;
  font-weight: 600;
  line-height: 1.45;
}

.brand-panel p {
  margin: 28px 0 0;
  max-width: 420px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 16px;
  line-height: 1.9;
}

.brand-line {
  width: 100%;
  height: 1px;
  margin-top: auto;
  background: rgba(255, 255, 255, 0.18);
}

.brand-foot {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 26px;
  color: rgba(255, 255, 255, 0.88);
}

.brand-foot span {
  color: rgba(255, 255, 255, 0.72);
}

.form-panel {
  padding: 64px 58px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 18px;
  margin-bottom: 28px;
}

.form-head h2 {
  margin: 0;
  color: #273043;
  font-size: 28px;
  font-weight: 700;
}

.form-head p {
  margin: 0 0 3px;
  color: #7b8597;
  font-size: 14px;
}

.field-label {
  display: block;
  margin: 18px 0 8px;
  color: #4b5568;
  font-size: 14px;
  font-weight: 600;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 46px;
  width: 100%;
  border-radius: 4px;
  background: #eef3fb;
  box-shadow: inset 0 0 0 1px transparent;
}

.login-form :deep(.el-input) {
  width: 100%;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #ff7a1a, 0 0 0 3px rgba(255, 122, 26, 0.12);
}

.login-form :deep(.el-input__inner) {
  color: #273043;
  font-size: 15px;
}

.form-helper {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin: 18px 0 20px;
  color: #7b8597;
  font-size: 13px;
}

.login-button {
  width: 100%;
  border-color: #ff7417;
  background: #ff7417;
  border-radius: 4px;
  color: #ffffff;
  font-size: 17px;
  font-weight: 700;
}

.login-button:hover,
.login-button:focus {
  border-color: #f15f00;
  background: #f15f00;
}

@media (max-width: 860px) {
  .login-page {
    padding: 20px;
  }

  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .brand-panel {
    padding: 34px 28px;
  }

  .brand-panel h1 {
    margin-top: 28px;
    font-size: 24px;
  }

  .brand-line,
  .brand-foot {
    display: none;
  }

  .form-panel {
    padding: 34px 28px;
  }

  .form-head {
    display: block;
  }

  .form-head p {
    margin-top: 6px;
  }
}
</style>
