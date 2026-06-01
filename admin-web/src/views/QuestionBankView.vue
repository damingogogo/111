<template>
  <div class="question-bank grid">
    <section class="panel hero-panel">
      <div>
        <p>情感测试与选择题题库</p>
        <h2>题库管理</h2>
      </div>
      <div class="hero-stats">
        <div>
          <span>测试数量</span>
          <strong>{{ screenings.length }}</strong>
        </div>
        <div>
          <span>题目数量</span>
          <strong>{{ questions.length }}</strong>
        </div>
      </div>
    </section>

    <section class="grid grid-2">
      <section class="panel form-panel">
        <div class="panel-header compact">
          <div>
            <h2 class="panel-title">创建情感测试</h2>
            <small>可以新建测试，也可以勾选下方题库题目后复制组卷</small>
          </div>
        </div>
        <el-form label-width="96px">
          <el-form-item label="测试名称">
            <el-input v-model="screeningForm.title" placeholder="例如：返岗焦虑情感测试" />
          </el-form-item>
          <el-form-item label="测试描述">
            <el-input v-model="screeningForm.description" type="textarea" :rows="3" placeholder="说明本测试的适用场景" />
          </el-form-item>
          <el-form-item label="推送周期">
            <el-select v-model="screeningForm.push_cycle">
              <el-option label="手动" value="手动" />
              <el-option label="每周" value="每周" />
              <el-option label="每月" value="每月" />
              <el-option label="返岗后第1周" value="返岗后第1周" />
            </el-select>
          </el-form-item>
          <el-form-item label="预计分钟">
            <el-input-number v-model="screeningForm.estimated_minutes" :min="1" :max="60" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="screeningForm.status">
              <el-radio-button label="启用" />
              <el-radio-button label="停用" />
            </el-radio-group>
          </el-form-item>
          <div class="actions">
            <el-button type="primary" :loading="savingScreening" @click="createScreening(false)">仅创建测试</el-button>
            <el-button :disabled="selectedQuestions.length === 0" :loading="savingScreening" @click="createScreening(true)">
              创建并复制已选 {{ selectedQuestions.length }} 题
            </el-button>
          </div>
        </el-form>
      </section>

      <section class="panel form-panel">
        <div class="panel-header compact">
          <div>
            <h2 class="panel-title">手动出题</h2>
            <small>直接创建五级量表选择题，保存后小程序立即可用</small>
          </div>
        </div>
        <el-form label-width="96px">
          <el-form-item label="所属测试">
            <el-select v-model="manualForm.screening_id" filterable placeholder="选择情感测试">
              <el-option v-for="item in screenings" :key="item.id" :label="item.title" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目维度">
            <el-select v-model="manualForm.dimension" filterable allow-create>
              <el-option v-for="item in dimensions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="题干">
            <el-input v-model="manualForm.content" type="textarea" :rows="3" placeholder="请输入选择题题干" />
          </el-form-item>
          <div class="option-editor">
            <div v-for="(option, index) in manualForm.options" :key="index" class="option-row">
              <el-input v-model="option.label" />
              <el-input-number v-model="option.score" :min="0" :max="100" />
            </div>
          </div>
          <div class="actions">
            <el-button @click="resetManual">重置</el-button>
            <el-button type="primary" :loading="savingQuestion" @click="saveManual">保存题目</el-button>
          </div>
        </el-form>
      </section>
    </section>

    <section class="panel generator-panel">
      <div class="panel-header compact">
        <div>
          <h2 class="panel-title">智能出题</h2>
          <small>本地规则智能生成，不依赖外部大模型；生成后可先修改再批量保存</small>
        </div>
      </div>
      <div class="generator-grid">
        <el-form label-width="96px">
          <el-form-item label="保存到">
            <el-select v-model="generateForm.screeningId" filterable placeholder="选择情感测试">
              <el-option v-for="item in screenings" :key="item.id" :label="item.title" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="出题主题">
            <el-input v-model="generateForm.topic" placeholder="例如：产后返岗焦虑、睡眠恢复、工作压力" />
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="generateForm.count" :min="1" :max="12" />
          </el-form-item>
          <div class="actions">
            <el-button type="primary" :loading="generating" @click="generateQuestions">智能生成</el-button>
            <el-button :disabled="generatedQuestions.length === 0" :loading="savingGenerated" @click="saveGenerated">保存生成题</el-button>
          </div>
        </el-form>

        <div class="generated-list">
          <div v-if="generatedQuestions.length === 0" class="empty-box">生成后的题目会显示在这里</div>
          <div v-for="(item, index) in generatedQuestions" :key="index" class="generated-item">
            <el-checkbox v-model="item.checked">保存</el-checkbox>
            <el-select v-model="item.dimension" filterable allow-create>
              <el-option v-for="dimension in dimensions" :key="dimension" :label="dimension" :value="dimension" />
            </el-select>
            <el-input v-model="item.content" type="textarea" :rows="2" />
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header compact">
        <div>
          <h2 class="panel-title">题库列表</h2>
          <small>勾选题目后，可在“创建情感测试”中复制题目完成组卷</small>
        </div>
        <div class="toolbar">
          <el-select v-model="filters.screeningId" clearable filterable placeholder="测试">
            <el-option v-for="item in screenings" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
          <el-select v-model="filters.dimension" clearable filterable placeholder="维度">
            <el-option v-for="item in dimensions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="filters.keyword" clearable placeholder="搜索题干" @keyup.enter="loadQuestions" />
          <el-button type="primary" @click="loadQuestions">查询</el-button>
        </div>
      </div>
      <el-table
        :data="questions"
        row-key="id"
        height="420"
        stripe
        @selection-change="selectedQuestions = $event"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="screening_title" label="所属测试" min-width="150" />
        <el-table-column prop="dimension" label="维度" width="110" />
        <el-table-column prop="content" label="题干" min-width="280" show-overflow-tooltip />
        <el-table-column label="选项" min-width="220">
          <template #default="{ row }">
            <span>{{ optionSummary(row.options_json) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="copyToManual(row)">复制出题</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api.js'

const dimensions = ['情绪稳定', '焦虑风险', '工作压力', '睡眠恢复', '支持系统', '角色平衡', '职业信心', '自我照护', '沟通安全', '恢复力']
const screenings = ref([])
const questions = ref([])
const selectedQuestions = ref([])
const generatedQuestions = ref([])
const savingScreening = ref(false)
const savingQuestion = ref(false)
const generating = ref(false)
const savingGenerated = ref(false)

const filters = reactive({
  screeningId: '',
  dimension: '',
  keyword: ''
})

const screeningForm = reactive({
  title: '',
  description: '',
  push_cycle: '手动',
  estimated_minutes: 6,
  status: '启用'
})

const manualForm = reactive({
  screening_id: '',
  content: '',
  dimension: '情绪稳定',
  sort_no: '',
  answer_type: 'single',
  score_rule: 'likert_5',
  status: '启用',
  options: defaultOptions()
})

const generateForm = reactive({
  screeningId: '',
  topic: '产后返岗焦虑',
  count: 5
})

function defaultOptions() {
  return [
    { label: '完全不符合', value: 1, score: 20 },
    { label: '比较不符合', value: 2, score: 40 },
    { label: '一般', value: 3, score: 60 },
    { label: '比较符合', value: 4, score: 80 },
    { label: '完全符合', value: 5, score: 100 }
  ]
}

async function loadScreenings() {
  screenings.value = await api.get('/question-bank/screenings')
  if (!manualForm.screening_id && screenings.value[0]) {
    manualForm.screening_id = screenings.value[0].id
  }
  if (!generateForm.screeningId && screenings.value[0]) {
    generateForm.screeningId = screenings.value[0].id
  }
}

async function loadQuestions() {
  questions.value = await api.get('/question-bank/questions', {
    params: {
      screeningId: filters.screeningId || undefined,
      dimension: filters.dimension || undefined,
      keyword: filters.keyword || undefined
    }
  })
}

async function createScreening(copySelected) {
  if (!screeningForm.title.trim()) {
    ElMessage.warning('请填写测试名称')
    return
  }
  savingScreening.value = true
  try {
    const created = await api.post('/question-bank/screenings', screeningForm)
    ElMessage.success('情感测试已创建')
    if (copySelected && selectedQuestions.value.length) {
      await api.post('/question-bank/questions/batch', {
        screeningId: created.id,
        questions: selectedQuestions.value.map((item, index) => ({
          ...item,
          sort_no: index + 1,
          status: '启用'
        }))
      })
      ElMessage.success('已复制选中题目完成组卷')
    }
    screeningForm.title = ''
    screeningForm.description = ''
    await loadScreenings()
    await loadQuestions()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    savingScreening.value = false
  }
}

async function saveManual() {
  if (!manualForm.screening_id) {
    ElMessage.warning('请选择所属测试')
    return
  }
  if (!manualForm.content.trim()) {
    ElMessage.warning('请填写题干')
    return
  }
  savingQuestion.value = true
  try {
    await api.post('/question-bank/questions', {
      ...manualForm,
      options: manualForm.options.map((option, index) => ({
        label: option.label,
        value: index + 1,
        score: Number(option.score || 0)
      }))
    })
    ElMessage.success('题目已保存')
    resetManual()
    await loadScreenings()
    await loadQuestions()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    savingQuestion.value = false
  }
}

async function generateQuestions() {
  if (!generateForm.topic.trim()) {
    ElMessage.warning('请填写出题主题')
    return
  }
  generating.value = true
  try {
    const data = await api.post('/question-bank/generate', generateForm)
    generatedQuestions.value = data.map((item) => ({ ...item, checked: true }))
    ElMessage.success('已生成题目，可修改后保存')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    generating.value = false
  }
}

async function saveGenerated() {
  if (!generateForm.screeningId) {
    ElMessage.warning('请选择保存到哪个测试')
    return
  }
  const checked = generatedQuestions.value.filter((item) => item.checked)
  if (!checked.length) {
    ElMessage.warning('请至少选择一道生成题')
    return
  }
  savingGenerated.value = true
  try {
    await api.post('/question-bank/questions/batch', {
      screeningId: generateForm.screeningId,
      questions: checked
    })
    ElMessage.success('生成题已保存到题库')
    generatedQuestions.value = []
    await loadScreenings()
    await loadQuestions()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    savingGenerated.value = false
  }
}

function resetManual() {
  manualForm.content = ''
  manualForm.dimension = '情绪稳定'
  manualForm.sort_no = ''
  manualForm.answer_type = 'single'
  manualForm.score_rule = 'likert_5'
  manualForm.status = '启用'
  manualForm.options = defaultOptions()
}

function copyToManual(row) {
  manualForm.screening_id = row.screening_id
  manualForm.content = row.content
  manualForm.dimension = row.dimension || '情绪稳定'
  manualForm.sort_no = ''
  manualForm.options = parseOptions(row.options_json)
  ElMessage.success('已复制到手动出题表单')
}

function parseOptions(value) {
  try {
    const parsed = JSON.parse(value || '[]')
    return Array.isArray(parsed) && parsed.length ? parsed : defaultOptions()
  } catch (error) {
    return defaultOptions()
  }
}

function optionSummary(value) {
  return parseOptions(value).map((item) => `${item.label}:${item.score}`).join(' / ')
}

onMounted(async () => {
  await loadScreenings()
  await loadQuestions()
})
</script>

<style scoped>
.question-bank {
  gap: 18px;
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 22px;
  background:
    linear-gradient(135deg, rgba(237, 244, 255, 0.98), rgba(255, 255, 255, 0.96) 58%, rgba(232, 246, 255, 0.9));
}

.hero-panel p {
  margin: 0 0 8px;
  color: #747b92;
  font-size: 13px;
}

.hero-panel h2 {
  margin: 0;
  font-size: 24px;
}

.hero-stats {
  display: flex;
  gap: 12px;
}

.hero-stats div {
  min-width: 120px;
  padding: 12px 16px;
  border: 1px solid #edf1fb;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
}

.hero-stats span {
  display: block;
  color: #747b92;
  font-size: 12px;
}

.hero-stats strong {
  display: block;
  margin-top: 6px;
  color: #315ed6;
  font-size: 26px;
}

.form-panel {
  padding-bottom: 18px;
}

.compact {
  padding: 16px 18px;
}

.form-panel :deep(.el-form) {
  padding: 18px 18px 0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.option-editor {
  display: grid;
  gap: 10px;
  margin: 0 0 18px 96px;
}

.option-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px;
  gap: 10px;
}

.generator-panel {
  overflow: hidden;
}

.generator-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
}

.generated-list {
  display: grid;
  gap: 12px;
  max-height: 430px;
  overflow: auto;
}

.generated-item {
  display: grid;
  grid-template-columns: 74px 150px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  padding: 12px;
  border: 1px solid #edf1fb;
  border-radius: 8px;
  background: #fbfcff;
}

.empty-box {
  min-height: 160px;
  display: grid;
  place-items: center;
  border: 1px dashed #d3e1ff;
  border-radius: 8px;
  color: #747b92;
  background: #f7fbff;
}

.toolbar {
  display: grid;
  grid-template-columns: 180px 150px 220px auto;
  gap: 10px;
  align-items: center;
}

@media (max-width: 1100px) {
  .generator-grid,
  .grid-2 {
    grid-template-columns: 1fr;
  }

  .toolbar,
  .generated-item {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    flex-direction: column;
  }
}
</style>
