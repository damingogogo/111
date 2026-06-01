<template>
  <main class="login-page">
    <section class="login-hero">
      <h1>产后返岗情绪支持平台</h1>
      <p>企业统一开通、员工实名激活、分级权限使用，支持网页后台与微信小程序双端访问。</p>
    </section>
    <el-form class="login-card" :model="form" @submit.prevent="login">
      <h2>后台登录</h2>
      <el-form-item>
        <el-input v-model="form.username" size="large" placeholder="账号：admin" />
      </el-form-item>
      <el-form-item>
        <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码：123456" />
      </el-form-item>
      <el-button type="primary" size="large" native-type="submit" :loading="loading">登录</el-button>
    </el-form>
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  align-items: center;
  gap: 56px;
  padding: 56px 9vw;
  background:
    linear-gradient(180deg, rgba(244, 238, 255, 0.96), rgba(247, 251, 255, 0.9)),
    url("/theme/hero-care.png") center/cover;
}

.login-hero {
  color: #121a33;
  max-width: 720px;
}

.login-hero h1 {
  margin: 0;
  font-size: clamp(42px, 6vw, 76px);
  line-height: 1.05;
  letter-spacing: 0;
}

.login-hero p {
  max-width: 600px;
  margin: 22px 0 0;
  color: #68718b;
  font-size: 19px;
  line-height: 1.8;
}

.login-card {
  padding: 30px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #edf1fb;
  box-shadow: 0 18px 52px rgba(78, 97, 145, 0.14);
}

.login-card h2 {
  margin: 0 0 22px;
}

.login-card .el-button {
  width: 100%;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 34px 20px;
  }
}
</style>
