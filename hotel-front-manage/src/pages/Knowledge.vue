<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getDocumentList,
  uploadDocument,
  deleteDocument,
  updateDocumentStatus,
  rebuildVectorDB
} from '../services/knowledgeService'

interface Document {
  id: number
  name: string
  category: string
  type: string
  size: string
  status: string
  uploadTime: string
  vectorStatus: string
}

const docs = ref<Document[]>([])
const isUploading = ref(false)
const isRebuilding = ref(false)
const showUploadModal = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const pendingFile = ref<File | null>(null)

async function loadDocuments() {
  const result = await getDocumentList()
  docs.value = result?.data || []
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  pendingFile.value = target.files?.[0] || null
}

async function confirmUpload() {
  if (!pendingFile.value) return
  await handleUpload(pendingFile.value)
}

async function handleUpload(file: File) {
  isUploading.value = true
  try {
    const result = await uploadDocument(file, '通用')
    if (result.code === 200) {
      await loadDocuments()
      const count = result.data?.length || 0
      if (count > 0) {
        alert('上传成功')
      } else {
        alert('上传失败：文件格式不支持或内容为空')
      }
    } else {
      alert('上传失败：' + (result.msg || '未知错误'))
    }
  } catch (e: any) {
    alert('上传失败：' + (e.message || '网络错误'))
  } finally {
    isUploading.value = false
    showUploadModal.value = false
    pendingFile.value = null
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

async function handleDelete(docId: number) {
  if (confirm('确定要删除该文档吗？')) {
    await deleteDocument(docId)
    docs.value = docs.value.filter(doc => doc.id !== docId)
  }
}

async function handleStatusChange(doc: Document) {
  const newEnabled = doc.status !== '启用'
  await updateDocumentStatus(doc.id, newEnabled)
  doc.status = newEnabled ? '启用' : '停用'
  doc.vectorStatus = newEnabled ? '已向量化' : '未向量化'
}

async function handleRebuildVectorDB() {
  if (confirm('确定要重建向量库吗？此操作可能需要一些时间。')) {
    isRebuilding.value = true
    await rebuildVectorDB()
    // 更新文档向量状态
    docs.value.forEach(doc => {
      doc.vectorStatus = '已向量化'
    })
    isRebuilding.value = false
    alert('向量库重建完成')
  }
}

function openUploadModal() {
  showUploadModal.value = true
}

function closeUploadModal() {
  showUploadModal.value = false
  pendingFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function getStatusClass(status: string) {
  return status === '启用' ? 'status-active' : 'status-inactive'
}

function getVectorStatusClass(status: string) {
  switch (status) {
    case '已向量化': return 'vector-done'
    case '处理中': return 'vector-processing'
    case '失败': return 'vector-failed'
    default: return ''
  }
}

function formatSize(bytes: number): string {
  if (!bytes || bytes <= 0) return '0B'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(2) + 'MB'
}

onMounted(() => {
  loadDocuments()
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>知识库管理</h1>
        <p class="page-subtitle">管理 AI 知识库文档，支持上传、分类与向量库重建。</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openUploadModal">
          上传文档
        </button>
        <button class="btn btn-secondary" @click="handleRebuildVectorDB" :disabled="isRebuilding">
          {{ isRebuilding ? '重建中...' : '重建向量库' }}
        </button>
      </div>
    </div>

    <div class="form-card" style="margin-bottom: 24px;">
      <div class="upload-area" @click="openUploadModal">
        <div class="upload-icon">📁</div>
        <div class="upload-text">拖拽文件到此处，或点击选择文件上传</div>
        <div class="upload-hint">支持 PDF、TXT、DOCX、MD 等格式</div>
      </div>
    </div>

    <div class="table knowledge-table">
      <div class="row header">
        <div>文件名</div>
        <div>分类</div>
        <div>类型</div>
        <div>大小</div>
        <div>状态</div>
        <div>向量状态</div>
        <div>上传时间</div>
        <div style="min-width: 80px;">操作</div>
      </div>
      <div class="row" v-for="doc in docs" :key="doc.id">
        <div class="file-name">
          <span class="file-icon">📄</span>
          <span class="file-name-text">{{ doc.name }}</span>
        </div>
        <div>{{ doc.category }}</div>
        <div>{{ doc.type.toUpperCase() }}</div>
        <div>{{ doc.size }}</div>
        <div>
          <button class="status-btn" :class="getStatusClass(doc.status)" @click="handleStatusChange(doc)">
            {{ doc.status }}
          </button>
        </div>
        <div>
          <span class="vector-status" :class="getVectorStatusClass(doc.vectorStatus)">
            {{ doc.vectorStatus }}
          </span>
        </div>
        <div>{{ doc.uploadTime }}</div>
        <div style="min-width: 80px;">
          <button class="btn btn-sm btn-danger" @click="handleDelete(doc.id)">
            删除
          </button>
        </div>
      </div>
      <div v-if="docs.length === 0" class="empty-panel">暂无文档，请上传文档</div>
    </div>

    <!-- 上传弹窗 -->
    <div v-if="showUploadModal" class="modal-overlay" @click.self="closeUploadModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>上传文档</h3>
          <button class="modal-close" @click="closeUploadModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label class="input-label">选择文件</label>
            <input
              ref="fileInput"
              type="file"
              accept=".pdf,.txt,.docx,.doc,.md"
              @change="handleFileSelect"
              class="file-input"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeUploadModal">取消</button>
          <button
            class="btn btn-primary"
            @click="confirmUpload"
            :disabled="!pendingFile || isUploading"
          >
            {{ isUploading ? '上传中...' : '确认上传' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>