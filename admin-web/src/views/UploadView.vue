<template>
  <section class="panel upload-page">
    <div class="panel-header">
      <div>
        <h2 class="panel-title">本地图片上传</h2>
        <small>所有业务图片统一上传到 MinIO 桶 deng，返回 URL 可写入任意图片字段</small>
      </div>
    </div>
    <div class="upload-body">
      <el-upload drag :show-file-list="false" :auto-upload="false" :on-change="handleUpload">
        <ImageUp :size="46" />
        <p>拖拽图片到这里，或点击选择本地图片</p>
      </el-upload>
      <div v-if="result.url" class="result">
        <img :src="displayImageUrl(result.url, 'upload_files', 'url')" alt="" @error="handleImageError($event, 'upload_files', 'url')" />
        <el-input v-model="result.url" readonly />
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { ImageUp } from 'lucide-vue-next'
import { normalizeUploadUrl, uploadImage } from '../api.js'
import { displayImageUrl, handleImageError } from '../image-utils.js'

const result = reactive({ url: '' })

async function handleUpload(file) {
  const uploaded = await uploadImage(file.raw, 'manual')
  result.url = normalizeUploadUrl(uploaded)
  ElMessage.success('上传成功')
}
</script>

<style scoped>
.upload-body {
  padding: 28px;
}

.upload-body :deep(.el-upload-dragger) {
  border-radius: 8px;
  padding: 46px 20px;
  border-color: #d3e1ff;
  background: #f7fbff;
  color: #59658a;
}

.result {
  margin-top: 22px;
  display: grid;
  gap: 14px;
}

.result img {
  max-width: 360px;
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #edf1fb;
  background: #eef5ff;
}
</style>
