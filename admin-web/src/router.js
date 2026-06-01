import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import CrudView from './views/CrudView.vue'
import QuestionBankView from './views/QuestionBankView.vue'
import UploadView from './views/UploadView.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: LoginView },
  { path: '/dashboard', component: DashboardView, meta: { title: '数据统计' } },
  { path: '/question-bank', component: QuestionBankView, meta: { title: '题库管理' } },
  { path: '/manage/:table', component: CrudView, meta: { title: '业务管理' } },
  { path: '/uploads', component: UploadView, meta: { title: '图片上传' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !localStorage.getItem('emotion-admin-token')) {
    return '/login'
  }
})

export default router
