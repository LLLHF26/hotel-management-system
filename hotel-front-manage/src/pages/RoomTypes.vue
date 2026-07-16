<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createRoomType, deleteRoomType, getRoomTypeList, updateRoomType, uploadRoomCover, uploadRoomImage } from '../services/roomService'

const roomTypes = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const keyword = ref('')
const editing = ref(false)
const isCreate = ref(true)
const formData = reactive<any>({
  id: null,
  name: '',
  description: '',
  area: '',
  bedType: '',
  maxGuests: '',
  price: '',
  coverImage: '',
  images: '',
  amenities: '',
  sortOrder: ''
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function loadRoomTypes() {
  const res = await getRoomTypeList({ page: page.value, size: size.value, keyword: keyword.value })
  const data = res?.data ?? res
  roomTypes.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
}

function openCreate() {
  resetForm()
  isCreate.value = true
  editing.value = true
}

function openEdit(type: any) {
  formData.id = type.id
  formData.name = type.name
  formData.description = type.description
  formData.area = type.area
  formData.bedType = type.bedType
  formData.maxGuests = type.maxGuests
  formData.price = type.price
  formData.coverImage = type.coverImage
  formData.images = Array.isArray(type.images) ? type.images.join(',') : (type.images || '')
  formData.amenities = type.amenities
  formData.sortOrder = type.sortOrder
  isCreate.value = false
  editing.value = true
}

function resetForm() {
  formData.id = null
  formData.name = ''
  formData.description = ''
  formData.area = ''
  formData.bedType = ''
  formData.maxGuests = ''
  formData.price = ''
  formData.coverImage = ''
  formData.images = ''
  formData.amenities = ''
  formData.sortOrder = ''
}

async function saveRoomType() {
  const payload = {
    name: formData.name,
    description: formData.description,
    area: Number(formData.area),
    bedType: formData.bedType,
    maxGuests: Number(formData.maxGuests),
    price: Number(formData.price),
    coverImage: formData.coverImage,
    images: formData.images ? formData.images.split(',').map((s: string) => s.trim()).filter(Boolean) : [],
    amenities: formData.amenities,
    sortOrder: Number(formData.sortOrder)
  }
  if (isCreate.value) {
    await createRoomType(payload)
  } else {
    await updateRoomType(formData.id, payload)
  }
  editing.value = false
  await loadRoomTypes()
}

async function handleDelete(id: number) {
  if (!confirm('确认删除该房型吗？')) return
  await deleteRoomType(id)
  await loadRoomTypes()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadRoomTypes()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadRoomTypes()
  }
}

function gotoPage() {
  let target = Number(pageInput.value)
  if (Number.isNaN(target) || target < 1) target = 1
  if (target > totalPages.value) target = totalPages.value
  page.value = target
  loadRoomTypes()
}

function onSizeChange() {
  page.value = 1
  pageInput.value = 1
  loadRoomTypes()
}

function cancelForm() {
  editing.value = false
}

const uploadingCover = ref(false)
const uploadingImage = ref(false)

async function handleCoverUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadingCover.value = true
  try {
    const url = await uploadRoomCover(file)
    if (url) formData.coverImage = url
  } finally {
    uploadingCover.value = false
  }
}

async function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadingImage.value = true
  try {
    const url = await uploadRoomImage(file)
    if (url) {
      const existing = formData.images ? formData.images.split(',').map((s: string) => s.trim()).filter(Boolean) : []
      existing.push(url)
      formData.images = existing.join(',')
    }
  } finally {
    uploadingImage.value = false
  }
}

function removeImage(index: number) {
  const existing = formData.images ? formData.images.split(',').map((s: string) => s.trim()).filter(Boolean) : []
  existing.splice(index, 1)
  formData.images = existing.join(',')
}

function removeCover() {
  formData.coverImage = ''
}

function onDropCover(e: DragEvent) {
  e.preventDefault()
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  uploadingCover.value = true
  uploadRoomCover(file).then(url => {
    if (url) formData.coverImage = url
  }).finally(() => uploadingCover.value = false)
}

function onDropImage(e: DragEvent) {
  e.preventDefault()
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  uploadingImage.value = true
  uploadRoomImage(file).then(url => {
    if (url) {
      const existing = formData.images ? formData.images.split(',').map((s: string) => s.trim()).filter(Boolean) : []
      existing.push(url)
      formData.images = existing.join(',')
    }
  }).finally(() => uploadingImage.value = false)
}

function dragover(e: DragEvent) {
  e.preventDefault()
}

onMounted(loadRoomTypes)
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>房型管理</h1>
        <p class="page-subtitle">管理酒店房型信息，包括面积、床型、价格和设施。</p>
      </div>
      <button class="primary-button" @click="openCreate">新增房型</button>
    </div>

    <div class="filters" style="margin-bottom:18px;display:flex;flex-wrap:wrap;gap:12px;align-items:center">
      <input v-model="keyword" placeholder="按房型名称搜索" />
      <button class="primary-button" @click="loadRoomTypes">搜索</button>
      <div style="margin-left:auto; color: #6b7280">共 {{ total }} 条</div>
    </div>

    <div class="table room-type-table">
      <div class="row header">
        <div>房型名称</div>
        <div>面积</div>
        <div>床型</div>
        <div>最大入住</div>
        <div>价格</div>
        <div>设施</div>
        <div>描述</div>
        <div>封面</div>
        <div>排序</div>
        <div>操作</div>
      </div>
      <div class="row" v-for="type in roomTypes" :key="type.id">
        <div>{{ type.name }}</div>
        <div>{{ type.area }}㎡</div>
        <div>{{ type.bedType }}</div>
        <div>{{ type.maxGuests }}</div>
        <div>¥{{ type.price }}</div>
        <div>{{ type.amenities }}</div>
        <div>{{ type.description }}</div>
        <div>
          <img v-if="type.coverImage" :src="type.coverImage" alt="封面" style="max-width:80px; max-height:50px; object-fit:cover" />
          <span v-else>无</span>
        </div>
        <div>{{ type.sortOrder }}</div>
        <div style="display:flex;gap:8px">
          <button class="primary-button" style="background:#2563eb" @click="openEdit(type)">编辑</button>
          <button class="primary-button" style="background:#ef4444" @click="handleDelete(type.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="pagination" style="margin-top:12px;display:flex;gap:8px;align-items:center">
      <button @click="prevPage">上一页</button>
      <div>第 {{ page }} / {{ totalPages }} 页</div>
      <button @click="nextPage">下一页</button>
      <label>跳转到
        <input type="number" min="1" :max="totalPages" v-model.number="pageInput" style="width:72px; margin:0 6px" />页
      </label>
      <button @click="gotoPage">前往</button>
      <select v-model.number="size" @change="onSizeChange">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
      <div style="margin-left:auto">共 {{ total }} 条</div>
    </div>

    <div v-if="editing" class="form-card" style="margin-top:24px">
      <div class="page-header">
        <div>
          <h2>{{ isCreate ? '新增房型' : '编辑房型' }}</h2>
          <p class="page-subtitle">填写房型信息并保存到系统。</p>
        </div>
      </div>
      <div class="form-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));gap:16px">
        <div class="input-group">
          <label>房型名称</label>
          <input v-model="formData.name" />
        </div>
        <div class="input-group">
          <label>价格（元/晚）</label>
          <input type="number" v-model.number="formData.price" />
        </div>
        <div class="input-group">
          <label>面积（㎡）</label>
          <input type="number" v-model.number="formData.area" />
        </div>
        <div class="input-group">
          <label>床型</label>
          <input v-model="formData.bedType" />
        </div>
        <div class="input-group">
          <label>最大入住</label>
          <input type="number" v-model.number="formData.maxGuests" />
        </div>
        <div class="input-group">
          <label>排序权重</label>
          <input type="number" v-model.number="formData.sortOrder" />
        </div>
        <div class="input-group" style="grid-column: span 2">
          <label>封面图</label>
          <div class="upload-zone" @drop="onDropCover" @dragover="dragover" @click="($refs.coverInput as HTMLInputElement).click()">
            <div v-if="!formData.coverImage && !uploadingCover">
              <span class="upload-icon">📷</span>
              <p>拖拽图片到此处或点击上传</p>
            </div>
            <div v-else-if="uploadingCover" class="uploading-text">上传中...</div>
            <div v-else class="upload-preview-wrap">
              <img :src="formData.coverImage" class="upload-preview" />
              <span class="upload-remove" @click.stop="removeCover">✕</span>
            </div>
          </div>
          <input ref="coverInput" type="file" accept="image/*" style="display:none" @change="handleCoverUpload" />
        </div>
        <div class="input-group" style="grid-column: span 2">
          <label>轮播图</label>
          <div class="upload-images-list" v-if="formData.images">
            <div class="upload-image-item" v-for="(url, idx) in formData.images.split(',').map(s => s.trim()).filter(Boolean)" :key="idx">
              <img :src="url" />
              <span class="upload-remove" @click="removeImage(idx)">✕</span>
            </div>
          </div>
          <div class="upload-zone" @drop="onDropImage" @dragover="dragover" @click="($refs.imageInput as HTMLInputElement).click()">
            <div v-if="!uploadingImage">
              <span class="upload-icon">🏞</span>
              <p>拖拽图片到此处或点击上传</p>
            </div>
            <div v-else class="uploading-text">上传中...</div>
          </div>
          <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="handleImageUpload" />
        </div>
        <div class="input-group" style="grid-column: span 2">
          <label>设施</label>
          <input v-model="formData.amenities" placeholder="逗号分隔" />
        </div>
        <div class="input-group" style="grid-column: span 2">
          <label>描述</label>
          <input v-model="formData.description" />
        </div>
      </div>
      <div style="display:flex;gap:12px;margin-top:18px">
        <button class="primary-button" @click="saveRoomType">保存</button>
        <button class="primary-button" style="background:#64748b" @click="cancelForm">取消</button>
      </div>
    </div>
  </div>
</template>
