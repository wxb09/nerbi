<template>
  <div class="image-uploader">
    <div class="image-list">
      <div
        v-for="(image, index) in imageList"
        :key="index"
        class="image-item"
      >
        <img :src="getImageUrl(image)" :alt="`图片${index + 1}`" />
        <button class="remove-btn" @click="removeImage(index)">×</button>
      </div>
      
      <div
        v-if="imageList.length < maxCount"
        class="upload-btn"
        @click="triggerUpload"
        :class="{ 'is-dragover': isDragover }"
        @dragover.prevent="isDragover = true"
        @dragleave.prevent="isDragover = false"
        @drop.prevent="handleDrop"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          @change="handleFileChange"
          hidden
        />
        <div class="upload-icon">+</div>
        <div class="upload-text">上传图片</div>
        <div class="upload-hint">{{ imageList.length }}/{{ maxCount }}</div>
      </div>
    </div>
    
    <div v-if="uploading" class="uploading-overlay">
      <div class="uploading-spinner"></div>
      <div>上传中...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { uploadApi } from '../api/upload'

const props = defineProps<{
  modelValue?: string[]
  maxCount?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const maxCount = props.maxCount || 9
const imageList = ref<string[]>([...(props.modelValue || [])])
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const isDragover = ref(false)

watch(() => props.modelValue, (newVal) => {
  imageList.value = [...(newVal || [])]
})

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files && files.length > 0) {
    await uploadFiles(Array.from(files))
  }
  target.value = ''
}

const handleDrop = async (event: DragEvent) => {
  isDragover.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const imageFiles = Array.from(files).filter(file =>
      file.type.startsWith('image/')
    )
    if (imageFiles.length > 0) {
      await uploadFiles(imageFiles)
    }
  }
}

const uploadFiles = async (files: File[]) => {
  const remainingSlots = maxCount - imageList.value.length
  const filesToUpload = files.slice(0, remainingSlots)
  
  if (filesToUpload.length === 0) {
    alert(`最多上传${maxCount}张图片`)
    return
  }
  
  uploading.value = true
  try {
    const results = await uploadApi.uploadImages(filesToUpload)
    const newUrls = results.map(r => r.url)
    imageList.value = [...imageList.value, ...newUrls]
    emit('update:modelValue', imageList.value)
  } catch (error) {
    console.error('上传失败', error)
    alert('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const removeImage = (index: number) => {
  imageList.value.splice(index, 1)
  emit('update:modelValue', imageList.value)
}

const getImageUrl = (path: string) => {
  if (!path) return ''
  if (path.startsWith('http')) {
    return path
  }
  // 如果已经有 /uploads/ 前缀，直接返回
  if (path.startsWith('/uploads/')) {
    return path
  }
  // 否则添加 /uploads/ 前缀
  return `/uploads/${path}`
}
</script>

<style scoped>
.image-uploader {
  position: relative;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  border-color: #E2B04D;
  background: #fefce8;
}

.upload-btn.is-dragover {
  border-color: #E2B04D;
  background: #fefce8;
}

.upload-icon {
  font-size: 24px;
  color: #9ca3af;
}

.upload-text {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.upload-hint {
  font-size: 10px;
  color: #9ca3af;
  margin-top: 2px;
}

.uploading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 8px;
}

.uploading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e5e7eb;
  border-top-color: #E2B04D;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
