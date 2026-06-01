<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <h2 class="panel-title">{{ currentLabel }}</h2>
        <small>支持新增、编辑、删除和图片字段本地上传到 MinIO</small>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索" @keyup.enter="loadData" />
        <el-button :icon="Search" @click="loadData" />
        <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
      </div>
    </div>
    <el-table :data="rows" height="calc(100vh - 210px)" stripe>
      <el-table-column prop="id" label="ID" width="72" fixed />
      <el-table-column v-for="column in visibleColumns" :key="column" :prop="column" :label="fieldLabel(column)" min-width="150">
        <template #default="{ row }">
          <img
            v-if="isImageColumn(column)"
            class="cover"
            :src="displayImageUrl(row[column], currentTable, column)"
            alt=""
            @error="handleImageError($event, currentTable, column)"
          />
          <el-tag v-else-if="isTagColumn(column)" size="small">{{ displayValue(column, row[column]) }}</el-tag>
          <span v-else>{{ displayValue(column, row[column]) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit3" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除这条记录？" @confirm="remove(row)">
            <template #reference>
              <el-button link type="danger" :icon="Trash2">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </section>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑记录' : '新增记录'" width="720px">
    <el-form label-width="120px" class="edit-form">
      <el-form-item v-for="column in writableColumns" :key="column" :label="fieldLabel(column)">
        <div v-if="isImageColumn(column)" class="upload-row">
          <img
            class="preview"
            :src="displayImageUrl(form[column], currentTable, column)"
            alt=""
            @error="handleImageError($event, currentTable, column)"
          />
          <el-upload :show-file-list="false" :auto-upload="false" :on-change="(file) => uploadField(column, file)">
            <el-button :icon="ImageUp">上传图片</el-button>
          </el-upload>
        </div>
        <el-select v-else-if="selectOptions(column).length" v-model="form[column]" clearable filterable>
          <el-option v-for="item in selectOptions(column)" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input-number v-else-if="isNumberColumn(column)" v-model="form[column]" :min="0" :max="100" />
        <el-date-picker v-else-if="isDateOnlyColumn(column)" v-model="form[column]" type="date" value-format="YYYY-MM-DD" />
        <el-date-picker v-else-if="isDateTimeColumn(column)" v-model="form[column]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        <el-input v-else-if="isLongTextColumn(column)" v-model="form[column]" type="textarea" :rows="4" />
        <el-input v-else v-model="form[column]" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit3, ImageUp, Plus, Search, Trash2 } from 'lucide-vue-next'
import { api, normalizeUploadUrl, uploadImage } from '../api.js'
import { displayImageUrl, handleImageError } from '../image-utils.js'

const route = useRoute()
const tableDefs = ref([])
const rows = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({})

const currentTable = computed(() => route.params.table)
const currentDef = computed(() => tableDefs.value.find((item) => item.name === currentTable.value) || { writableColumns: [] })
const currentLabel = computed(() => currentDef.value.label || '业务管理')
const writableColumns = computed(() => currentDef.value.writableColumns || [])
const visibleColumns = computed(() => {
  const first = rows.value[0] || {}
  return Object.keys(first).filter((key) => key !== 'id').slice(0, 8)
})

const labels = {
  username: '账号',
  password: '密码',
  login_password: '登录密码',
  real_name: '姓名',
  role: '角色',
  phone: '手机号',
  url: '访问地址',
  employee_no: '工号',
  leader: '负责人',
  position: '岗位',
  return_work_date: '返岗日期',
  description: '描述',
  push_cycle: '推送周期',
  estimated_minutes: '预计分钟',
  score: '分数',
  action_type: '行动类型',
  answer_type: '答题类型',
  options_json: '选项配置JSON',
  score_rule: '计分规则',
  category: '分类',
  duration_minutes: '课程分钟',
  speciality: '擅长领域',
  appointment_time: '预约时间',
  method: '咨询方式',
  notes: '备注',
  mood_score: '情绪分',
  work_stress: '工作压力',
  family_stress: '家庭压力',
  note: '记录',
  logged_at: '记录时间',
  setting_key: '配置键',
  setting_value: '配置值',
  file_name: '文件名',
  object_name: '对象名',
  content_type: '文件类型',
  size_bytes: '文件大小',
  biz_type: '业务类型',
  avatar_url: '头像',
  image_url: '图片',
  cover_url: '封面',
  chart_image_url: '图表',
  status: '状态',
  name: '名称',
  title: '标题',
  content: '内容',
  summary: '摘要',
  suggestion: '建议',
  risk_level: '风险等级',
  employee_id: '员工ID',
  screening_id: '筛查ID',
  consultant_id: '咨询师ID',
  department_id: '部门ID',
  completed: '是否完成',
  created_at: '创建时间'
}

const valueLabels = {
  role: {
    SUPER_ADMIN: '超级管理员',
    HR: '人力资源',
    MANAGER: '部门主管',
    CARE: '关怀专员',
    DATA: '数据分析员',
    DEPT_ADMIN: '部门管理员',
    AUDITOR: '审计员'
  },
  answer_type: {
    single: '单选题'
  },
  score_rule: {
    likert_5: '五级量表',
    custom: '自定义计分'
  },
  completed: {
    0: '未完成',
    1: '已完成',
    false: '未完成',
    true: '已完成'
  },
  content_type: {
    'image/jpeg': 'JPEG 图片',
    'image/png': 'PNG 图片',
    'image/webp': 'WebP 图片',
    'image/gif': 'GIF 图片'
  },
  biz_type: {
    avatar: '头像',
    course: '课程',
    report: '报告',
    policy: '政策',
    consultant: '咨询师',
    department: '部门',
    mood: '状态记录',
    screening: '筛查',
    intervention: '干预方案',
    setting: '系统设置',
    manual: '手动上传',
    common: '通用图片',
    admin_users: '账号管理',
    departments: '部门管理',
    employees: '员工管理',
    screenings: 'AI筛查',
    screening_questions: '筛查题库',
    screening_reports: '健康报告',
    intervention_plans: '干预方案',
    courses: '课程管理',
    course_progress: '学习进度',
    consultants: '咨询师',
    appointments: '咨询预约',
    mood_logs: '状态追踪',
    policies: '政策内容',
    system_settings: '后台设置',
    upload_files: '上传记录'
  },
  setting_key: {
    screening_push_time: '筛查推送时间',
    risk_warning_threshold: '高风险预警阈值',
    medium_risk_threshold: '中风险阈值',
    privacy_mode: '隐私展示模式',
    consultation_duration: '咨询默认时长',
    course_remind_days: '课程提醒间隔',
    report_export_enabled: '报表导出开关',
    care_follow_days: '关怀跟进天数',
    minio_bucket: 'MinIO 桶名',
    company_name: '企业名称'
  },
  setting_value: {
    anonymous: '匿名模式',
    true: '是',
    false: '否'
  }
}

function fieldLabel(column) {
  return labels[column] || column
}

function isImageColumn(column) {
  return column.endsWith('_url') || column === 'url'
}

function isTagColumn(column) {
  return column.includes('status') ||
    column.includes('risk_level') ||
    ['role', 'answer_type', 'score_rule', 'completed', 'content_type', 'biz_type', 'method', 'action_type'].includes(column)
}

function isNumberColumn(column) {
  return ['score', 'progress', 'duration_minutes', 'estimated_minutes', 'mood_score', 'work_stress', 'family_stress', 'sort_no', 'size_bytes'].includes(column)
}

function isDateOnlyColumn(column) {
  return column === 'return_work_date'
}

function isDateTimeColumn(column) {
  return (column.includes('time') || column.includes('_at')) && !isDateOnlyColumn(column)
}

function isLongTextColumn(column) {
  return ['content', 'description', 'summary', 'suggestion', 'notes', 'options_json'].includes(column)
}

function selectOptions(column) {
  const options = {
    status: ['启用', '停用', '在职', '离职', '上架', '下架', '发布', '草稿', '可预约', '不可预约', '待确认', '已确认', '已完成'],
    risk_level: ['低风险', '中风险', '高风险'],
    role: ['SUPER_ADMIN', 'HR', 'MANAGER', 'CARE', 'DATA', 'DEPT_ADMIN', 'AUDITOR'],
    method: ['视频', '语音'],
    answer_type: ['single'],
    score_rule: ['likert_5', 'custom'],
    completed: [0, 1],
    content_type: ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
    biz_type: ['avatar', 'course', 'report', 'policy', 'consultant', 'department', 'mood', 'screening', 'intervention', 'setting', 'manual', 'common', 'admin_users', 'departments', 'employees', 'screenings', 'screening_questions', 'screening_reports', 'intervention_plans', 'courses', 'course_progress', 'consultants', 'appointments', 'mood_logs', 'policies', 'system_settings', 'upload_files'],
    action_type: ['自助工具', '专业咨询', '职场指导', '状态追踪', '预警对接', '关怀提醒', '角色平衡'],
    category: ['时间管理', '角色平衡', '职业信心', '情绪调节', '职场沟通', '恢复力', '生活支持', '自助工具', '团队协作', '成长轨迹', '企业政策', '母婴福利', '心理福利']
  }
  return (options[column] || []).map((value) => ({
    value,
    label: displayValue(column, value)
  }))
}

function displayValue(column, value) {
  if (value === null || value === undefined || value === '') return '-'
  const labels = valueLabels[column]
  if (labels && Object.prototype.hasOwnProperty.call(labels, String(value))) {
    return labels[String(value)]
  }
  return value
}

async function loadTables() {
  tableDefs.value = await api.get('/admin/tables')
}

async function loadData() {
  rows.value = await api.get(`/admin/${currentTable.value}`, { params: { keyword: keyword.value, page: 1, size: 80 } })
}

function resetForm(row = {}) {
  Object.keys(form).forEach((key) => delete form[key])
  writableColumns.value.forEach((column) => {
    form[column] = row[column] ?? ''
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  resetForm(row)
  dialogVisible.value = true
}

async function uploadField(column, file) {
  const uploaded = await uploadImage(file.raw, currentTable.value)
  form[column] = normalizeUploadUrl(uploaded)
  ElMessage.success('图片已上传到 MinIO')
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/admin/${currentTable.value}/${editingId.value}`, form)
    } else {
      await api.post(`/admin/${currentTable.value}`, form)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await api.delete(`/admin/${currentTable.value}/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(async () => {
  await loadTables()
  await loadData()
})

watch(currentTable, loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.panel-header small {
  color: #747b92;
}

.edit-form {
  max-height: 62vh;
  overflow: auto;
  padding-right: 10px;
}

.upload-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.cover {
  width: 74px;
  height: 54px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #edf1fb;
  background: #eef5ff;
}

.preview {
  width: 88px;
  height: 66px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #edf1fb;
  background: #eef5ff;
}
</style>
