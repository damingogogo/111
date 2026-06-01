<template>
  <router-view v-if="isLoginPage" />
  <div v-else class="shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <strong>返岗情绪支持</strong>
          <span>企业管理后台</span>
        </div>
      </div>
      <nav class="nav">
        <RouterLink to="/dashboard" class="nav-item">
          <BarChart3 :size="18" />数据统计
        </RouterLink>
        <RouterLink to="/question-bank" class="nav-item">
          <Brain :size="18" />题库管理
        </RouterLink>
        <RouterLink v-for="item in menu" :key="item.table" :to="`/manage/${item.table}`" class="nav-item">
          <component :is="item.icon" :size="18" />{{ item.label }}
        </RouterLink>
        <RouterLink to="/uploads" class="nav-item">
          <ImageUp :size="18" />图片上传
        </RouterLink>
      </nav>
    </aside>
    <main class="main">
      <header class="topbar">
        <div>
          <p>隐私化、可视化、数据化的员工心理健康管理</p>
          <h1>{{ route.meta.title || '管理后台' }}</h1>
        </div>
        <div class="top-actions">
          <el-tag effect="dark" type="success">MinIO 已配置</el-tag>
          <el-button :icon="LogOut" @click="logout">退出</el-button>
        </div>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  BarChart3,
  BookOpen,
  Brain,
  Building2,
  CalendarClock,
  FileText,
  HeartPulse,
  ImageUp,
  LogOut,
  MessageCircle,
  Settings,
  ShieldAlert,
  Users
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isLoginPage = computed(() => route.path === '/login')

const menu = [
  { table: 'admin_users', label: '账号管理', icon: Users },
  { table: 'departments', label: '部门管理', icon: Building2 },
  { table: 'employees', label: '员工管理', icon: Users },
  { table: 'screenings', label: 'AI筛查', icon: Brain },
  { table: 'screening_reports', label: '健康报告', icon: HeartPulse },
  { table: 'intervention_plans', label: '干预方案', icon: ShieldAlert },
  { table: 'courses', label: '课程管理', icon: BookOpen },
  { table: 'course_progress', label: '学习进度', icon: BookOpen },
  { table: 'consultants', label: '咨询师', icon: MessageCircle },
  { table: 'appointments', label: '咨询预约', icon: CalendarClock },
  { table: 'mood_logs', label: '状态追踪', icon: HeartPulse },
  { table: 'policies', label: '政策内容', icon: FileText },
  { table: 'system_settings', label: '后台设置', icon: Settings },
  { table: 'upload_files', label: '上传记录', icon: ImageUp }
]

function logout() {
  localStorage.removeItem('emotion-admin-token')
  router.push('/login')
}
</script>
