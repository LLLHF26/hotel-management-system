<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { marked } from 'marked'
import { getDocumentList, uploadDocument, deleteDocument, updateDocumentStatus, rebuildVectorDB, previewDocument, getDocumentContent } from '../services/knowledgeService'
import { useToast } from '../composables/useToast'

const toast = useToast()

interface Document {
  id: number; name: string; category: string; type: string; size: string
  status: string; uploadTime: string; vectorStatus: string
}

const docs = ref<Document[]>([])
const loading = ref(true)
const isUploading = ref(false)
const isRebuilding = ref(false)
const showUploadModal = ref(false)
const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const pendingFile = ref<File | null>(null)

const showMdPreview = ref(false)
const previewTitle = ref('')
const previewHtml = ref('')
const isPreviewLoading = ref(false)

function normalizeImageSyntax(md: string): { text: string; converted: number } {
  let converted = 0
  const text = md
    .replace(/@image#(\d+):([^\s\)\]\n]+)/g, (_, n, p) => {
      converted++
      return `![image #${n}](${p})`
    })
    .replace(/@image:([^\s\)\]\n]+)/g, (_, p) => {
      converted++
      return `![image](${p})`
    })
  return { text, converted }
}

async function load() {
  loading.value = true
  const result = await getDocumentList()
  docs.value = result?.data || []
  loading.value = false
}
onMounted(load)

function openUpload() { showUploadModal.value = true; isDragging.value = false }
function closeUpload() { showUploadModal.value = false; pendingFile.value = null; isDragging.value = false; if (fileInput.value) fileInput.value.value = '' }
function onFile(e: Event) { pendingFile.value = (e.target as HTMLInputElement).files?.[0] || null }
function openFileInput() { fileInput.value?.click() }
function clearFile() { pendingFile.value = null; if (fileInput.value) fileInput.value.value = '' }
function onDragEnter(e: DragEvent) { e.preventDefault(); isDragging.value = true }
function onDragLeave(e: DragEvent) {
  e.preventDefault()
  if (!(e.currentTarget as HTMLElement)?.contains(e.relatedTarget as Node)) isDragging.value = false
}
function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  const f = e.dataTransfer?.files?.[0]
  if (!f) return
  if (!showUploadModal.value) { showUploadModal.value = true }
  pendingFile.value = f
}

async function confirmUpload() {
  if (!pendingFile.value) return
  isUploading.value = true
  try {
    const result = await uploadDocument(pendingFile.value, '通用')
    const ok = result.code === 200 && result.data
    if (ok) {
      await load()
      toast.success('上传成功')
      closeUpload()
    } else {
      toast.error(result.msg || '上传失败：文件格式不支持或内容为空')
    }
  } catch (e: any) { toast.error('上传失败：' + (e.message || '网络错误')) }
  finally { isUploading.value = false }
}

async function renderMdPreview(d: Document) {
  showMdPreview.value = true
  previewTitle.value = d.name
  isPreviewLoading.value = true
  previewHtml.value = ''
  try {
    const md = await getDocumentContent(d.id)
    const { text } = normalizeImageSyntax(md)
    previewHtml.value = await marked(text, { gfm: true, breaks: true })
  } catch (e: any) {
    toast.error(e?.message || '预览失败')
    previewHtml.value = `<p style="color:#cf222e">预览失败：${e?.message || '未知错误'}</p>`
  } finally {
    isPreviewLoading.value = false
  }
}

async function handlePreview(d: Document) {
  if ((d.type || '').toLowerCase() === 'md') {
    await renderMdPreview(d)
  } else {
    try {
      await previewDocument(d.id)
    } catch (e: any) { toast.error(e?.message || '预览失败') }
  }
}

function closeMdPreview() {
  showMdPreview.value = false
  previewTitle.value = ''
  previewHtml.value = ''
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除该文档吗？')) return
  try {
    await deleteDocument(id)
    docs.value = docs.value.filter(d => d.id !== id)
    toast.success('已删除文档')
  } catch (e: any) { toast.error(e?.message || '删除失败') }
}
async function handleStatusChange(d: Document) {
  const enable = d.status !== '启用'
  try {
    await updateDocumentStatus(d.id, enable)
    d.status = enable ? '启用' : '停用'
    d.vectorStatus = enable ? '已向量化' : '未向量化'
    toast.success(enable ? '已启用文档' : '已停用文档')
  } catch (e: any) { toast.error(e?.message || '操作失败') }
}
async function handleRebuild() {
  if (!confirm('确定要重建向量库吗？此操作可能需要一些时间。')) return
  isRebuilding.value = true
  try {
    await rebuildVectorDB()
    docs.value.forEach(d => (d.vectorStatus = '已向量化'))
    toast.success('向量库重建完成')
  } catch (e: any) { toast.error(e?.message || '重建失败') }
  finally { isRebuilding.value = false }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">知识库管理</h1>
        <p class="page-subtitle">管理 AI 知识库文档，支持上传、分类与向量库重建。</p>
      </div>
      <div class="page-actions">
        <button class="btn" :disabled="isRebuilding" @click="handleRebuild">{{ isRebuilding ? '重建中…' : '重建向量库' }}</button>
        <button class="btn btn-primary" @click="openUpload">上传文档</button>
      </div>
    </div>

    <div class="card mb-4">
      <div
        class="upload-dropzone"
        :class="{ 'is-dragging': isDragging }"
        style="padding: var(--sp-8);"
        @click="openUpload"
        @dragenter.prevent="onDragEnter"
        @dragover.prevent="onDragEnter"
        @dragleave.prevent="onDragLeave"
        @drop.prevent="onDrop"
      >
        <div class="upload-dropzone-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        </div>
        <div class="upload-dropzone-title">点击或拖拽文件到此处上传</div>
        <div class="upload-dropzone-hint">支持 PDF、TXT、DOCX、MD 等格式</div>
      </div>
    </div>

    <div class="card">
      <div class="card-head">
        <div>
          <div class="card-title">文档列表</div>
          <div class="card-sub">共 {{ docs.length }} 个文档</div>
        </div>
      </div>
      <div style="overflow-x: auto;">
        <table class="table">
          <thead>
            <tr>
              <th>文件名</th>
              <th>分类</th>
              <th>类型</th>
              <th class="num">大小</th>
              <th>状态</th>
              <th>向量</th>
              <th>上传时间</th>
              <th class="actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in docs" :key="d.id">
              <td>
                <span class="doc-name" :title="d.name" @click="handlePreview(d)">
                  <span class="doc-icon">📄</span>
                  <span class="doc-label">{{ d.name }}</span>
                </span>
              </td>
              <td>{{ d.category }}</td>
              <td class="mono fz-xs">{{ (d.type || '').toUpperCase() }}</td>
              <td class="num">{{ d.size }}</td>
              <td>
                <button class="badge" :class="d.status === '启用' ? 'badge-success' : 'badge-neutral'" style="border:0; cursor:pointer;" @click="handleStatusChange(d)">{{ d.status }}</button>
              </td>
              <td>
                <span class="badge" :class="d.vectorStatus === '已向量化' ? 'badge-success' : (d.vectorStatus === '处理中' ? 'badge-warning' : (d.vectorStatus === '失败' ? 'badge-danger' : 'badge-neutral'))">{{ d.vectorStatus }}</span>
              </td>
              <td class="mono fz-xs">{{ d.uploadTime }}</td>
              <td class="actions">
                <button class="btn btn-sm btn-danger" @click="handleDelete(d.id)">删除</button>
              </td>
            </tr>
            <tr v-if="loading">
              <td colspan="8">
                <div class="loading-wrap">
                  <div class="loading-dual-ring"></div>
                  <span class="loading-text">加载中…</span>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && docs.length === 0"><td colspan="8" class="empty">暂无文档，请上传</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showUploadModal" class="modal-overlay" @click.self="closeUpload">
      <div class="modal" style="max-width: 420px;">
        <div class="modal-head">
          <div class="modal-title">上传文档</div>
          <button class="modal-close" @click="closeUpload" aria-label="关闭">✕</button>
        </div>
        <div class="modal-body">
          <div
            v-if="!pendingFile"
            class="upload-dropzone"
            :class="{ 'is-dragging': isDragging }"
            @dragenter.prevent="onDragEnter"
            @dragover.prevent="onDragEnter"
            @dragleave.prevent="onDragLeave"
            @drop.prevent="onDrop"
            @click="openFileInput"
          >
            <div class="upload-dropzone-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            </div>
            <div class="upload-dropzone-title">点击或拖拽文件到此处上传</div>
            <div class="upload-dropzone-hint">支持 PDF、TXT、DOCX、MD 等格式</div>
          </div>

          <div v-else class="upload-file-preview">
            <div class="upload-file-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            </div>
            <div class="upload-file-info">
              <div class="upload-file-name" :title="pendingFile.name">{{ pendingFile.name }}</div>
              <div class="upload-file-meta">{{ (pendingFile.size / 1024).toFixed(1) }} KB · {{ pendingFile.type || (pendingFile.name.split('.').pop() || '').toUpperCase() }}</div>
            </div>
            <button class="upload-file-remove" type="button" title="移除" @click.stop="clearFile">✕</button>
          </div>

          <input ref="fileInput" type="file" accept=".pdf,.txt,.docx,.doc,.md" style="display: none;" @change="onFile" />
        </div>
        <div class="modal-foot">
          <button class="btn" @click="closeUpload">取消</button>
          <button class="btn btn-primary" :disabled="!pendingFile || isUploading" @click="confirmUpload">{{ isUploading ? '上传中…' : '确认上传' }}</button>
        </div>
      </div>
    </div>

    <!-- Markdown 在线预览弹窗 -->
    <div v-if="showMdPreview" class="modal-overlay" @click.self="closeMdPreview">
      <div class="modal md-preview-modal">
        <div class="modal-head">
          <div class="modal-title" :title="previewTitle">{{ previewTitle }}</div>
          <button class="modal-close" @click="closeMdPreview" aria-label="关闭">✕</button>
        </div>
        <div class="modal-body md-preview-body">
          <div v-if="isPreviewLoading" class="loading-wrap" style="padding: 40px 0;">
            <div class="loading-dual-ring"></div>
            <span class="loading-text">正在渲染预览…</span>
          </div>
          <div v-else class="markdown-body" v-html="previewHtml"></div>
        </div>
        <div class="modal-foot">
          <button class="btn" @click="closeMdPreview">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.md-preview-modal {
  max-width: 900px;
  width: 90vw;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
}
.md-preview-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 0;
}
.md-preview-body .markdown-body {
  padding: 24px 32px;
  line-height: 1.75;
  font-size: 15px;
  color: #1f2328;
}
.md-preview-body .markdown-body h1,
.md-preview-body .markdown-body h2 {
  border-bottom: 1px solid #d0d7de;
  padding-bottom: .3em;
}
.md-preview-body .markdown-body h1 { font-size: 1.8em; }
.md-preview-body .markdown-body h2 { font-size: 1.5em; }
.md-preview-body .markdown-body h3 { font-size: 1.25em; }
.md-preview-body .markdown-body code {
  background: #f6f8fa;
  padding: .2em .4em;
  border-radius: 6px;
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 85%;
}
.md-preview-body .markdown-body pre {
  background: #f6f8fa;
  padding: 14px 16px;
  border-radius: 8px;
  overflow: auto;
}
.md-preview-body .markdown-body pre code { background: transparent; padding: 0; }
.md-preview-body .markdown-body blockquote {
  border-left: 4px solid #d0d7de;
  color: #656d76;
  padding: 0 1em;
  margin: 0;
}
.md-preview-body .markdown-body table {
  border-collapse: collapse;
  width: 100%;
}
.md-preview-body .markdown-body th,
.md-preview-body .markdown-body td {
  border: 1px solid #d0d7de;
  padding: 6px 13px;
}
.md-preview-body .markdown-body tr:nth-child(2n) { background: #f6f8fa; }
.md-preview-body .markdown-body img { max-width: 100%; border-radius: 6px; }
.md-preview-body .markdown-body a { color: #0969da; }
.md-preview-body .markdown-body ul, .md-preview-body .markdown-body ol { padding-left: 2em; }
.md-preview-body .markdown-body p { margin: 0 0 1em; }
</style>
