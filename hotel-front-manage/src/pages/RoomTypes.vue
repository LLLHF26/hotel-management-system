<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createRoomType, deleteRoomType, getRoomTypeList, updateRoomType, uploadRoomCover, uploadRoomImage } from '../services/roomService'
import { useToast } from '../composables/useToast'

const toast = useToast()

// 兼容 images 字段的多种格式：数组 / JSON 数组字符串 / 逗号分隔字符串
function parseImagesField(raw: any): string[] {
  if (Array.isArray(raw)) return raw.map(String).filter(Boolean)
  if (typeof raw === 'string') {
    const s = raw.trim()
    if (!s) return []
    if (s.startsWith('[')) {
      try {
        const arr = JSON.parse(s)
        if (Array.isArray(arr)) return arr.map(String).filter(Boolean)
      } catch { /* fall through */ }
    }
    return s.split(',').map(x => x.trim()).filter(Boolean)
  }
  return []
}

const roomTypes = ref<any[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1); const pageInput = ref(1); const size = ref(10)
const keyword = ref('')
const editing = ref(false)
const isCreate = ref(true)
const form = reactive<any>({ id: null, name: '', description: '', area: '', bedType: '', maxGuests: '', price: '', coverImage: '', images: '', amenities: '', sortOrder: '' })

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function load() {
  loading.value = true
  const res = await getRoomTypeList({ page: page.value, size: size.value, keyword: keyword.value })
  const data = res?.data ?? res
  roomTypes.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}
onMounted(load)

function openCreate() { resetForm(); isCreate.value = true; editing.value = true }
function openEdit(t: any) {
  form.id = t.id; form.name = t.name; form.description = t.description; form.area = t.area
  form.bedType = t.bedType; form.maxGuests = t.maxGuests; form.price = t.price
  form.coverImage = t.coverImage
  form.images = parseImagesField(t.images).join(',')
  form.amenities = t.amenities; form.sortOrder = t.sortOrder
  isCreate.value = false; editing.value = true
}
function resetForm() { Object.assign(form, { id: null, name: '', description: '', area: '', bedType: '', maxGuests: '', price: '', coverImage: '', images: '', amenities: '', sortOrder: '' }) }
function cancelForm() { editing.value = false }

async function save() {
  const payload = {
    name: form.name, description: form.description,
    area: Number(form.area), bedType: form.bedType, maxGuests: Number(form.maxGuests),
    price: Number(form.price), coverImage: form.coverImage,
    images: form.images ? form.images.split(',').map((s:string)=>s.trim()).filter(Boolean) : [],
    amenities: form.amenities, sortOrder: Number(form.sortOrder),
  }
  try {
    if (isCreate.value) await createRoomType(payload)
    else await updateRoomType(form.id, payload)
    editing.value = false
    await load()
    toast.success(isCreate.value ? '房型创建成功' : '房型更新成功')
  } catch (e: any) {
    toast.error(e?.message || '保存失败')
  }
}
async function handleDelete(id: number) { if (!confirm('确认删除该房型吗？')) return; try { await deleteRoomType(id); await load(); toast.success('已删除房型') } catch (e: any) { toast.error(e?.message || '删除失败') } }

const uploadingCover = ref(false); const uploadingImage = ref(false)
const coverInput = ref<HTMLInputElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)
async function handleCoverUpload(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]; if (!f) return
  uploadingCover.value = true
  try { const url = await uploadRoomCover(f); if (url) { form.coverImage = url; toast.success('封面上传成功') } else toast.error('上传失败') } catch (e: any) { toast.error(e?.message || '上传失败') } finally { uploadingCover.value = false }
}
async function handleImageUpload(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]; if (!f) return
  uploadingImage.value = true
  try {
    const url = await uploadRoomImage(f)
    if (url) {
      const arr = form.images ? form.images.split(',').map((s:string)=>s.trim()).filter(Boolean) : []
      arr.push(url); form.images = arr.join(',')
      toast.success('图片上传成功')
    } else toast.error('上传失败')
  } catch (e: any) { toast.error(e?.message || '上传失败') } finally { uploadingImage.value = false }
}
function removeImage(idx: number) {
  const arr = form.images ? form.images.split(',').map((s:string)=>s.trim()).filter(Boolean) : []
  arr.splice(idx, 1); form.images = arr.join(',')
}
const imageList = computed(() => form.images ? form.images.split(',').map((s:string)=>s.trim()).filter(Boolean) : [])

// 拖拽排序
const dragIndex = ref<number | null>(null)
function onDragStart(e: DragEvent, i: number) {
  dragIndex.value = i
  e.dataTransfer?.setData('text/plain', String(i))
  if (e.dataTransfer) e.dataTransfer.effectAllowed = 'move'
}
function onDragOver(e: DragEvent) { e.preventDefault(); if (e.dataTransfer) e.dataTransfer.dropEffect = 'move' }
function onDrop(e: DragEvent, targetIdx: number) {
  e.preventDefault()
  const src = dragIndex.value
  if (src === null || src === targetIdx) { dragIndex.value = null; return }
  const arr = imageList.value.slice()
  const [moved] = arr.splice(src, 1)
  arr.splice(targetIdx, 0, moved)
  form.images = arr.join(',')
  dragIndex.value = null
}
function onDragEnd() { dragIndex.value = null }

// 设施标签
const amenitiesList = computed(() => form.amenities ? form.amenities.split(',').map((s:string)=>s.trim()).filter(Boolean) : [])
const amenityInput = ref('')
function addAmenity() {
  const v = amenityInput.value.trim()
  if (!v) return
  const arr = amenitiesList.value.slice()
  if (arr.includes(v)) { amenityInput.value = ''; return }
  arr.push(v)
  form.amenities = arr.join(',')
  amenityInput.value = ''
}
function removeAmenity(i: number) {
  const arr = amenitiesList.value.slice()
  arr.splice(i, 1)
  form.amenities = arr.join(',')
}

function prevPage() { if (page.value > 1) { page.value--; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; load() } }
function gotoPage() { let t = Number(pageInput.value); if (Number.isNaN(t)||t<1) t=1; if (t>totalPages.value) t=totalPages.value; page.value=t; load() }
function onSizeChange() { page.value=1; pageInput.value=1; load() }
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">房型管理</h1>
        <p class="page-subtitle">管理酒店房型信息，包括面积、床型、价格和设施。</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" @click="openCreate">新增房型</button>
      </div>
    </div>

    <div v-if="editing" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">{{ isCreate ? '新增房型' : '编辑房型' }}</div>
          <div class="card-sub">填写房型信息后保存</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="cancelForm">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2"><label class="label-text">房型名称</label><input class="input input-block" v-model="form.name" /></div>
          <div class="col-gap-2"><label class="label-text">价格 (元/晚)</label><input class="input input-block" type="number" v-model.number="form.price" /></div>
          <div class="col-gap-2"><label class="label-text">面积 (㎡)</label><input class="input input-block" type="number" v-model.number="form.area" /></div>
          <div class="col-gap-2"><label class="label-text">床型</label><input class="input input-block" v-model="form.bedType" /></div>
          <div class="col-gap-2"><label class="label-text">最大入住</label><input class="input input-block" type="number" v-model.number="form.maxGuests" /></div>
          <div class="col-gap-2"><label class="label-text">排序权重</label><input class="input input-block" type="number" v-model.number="form.sortOrder" /></div>
          <div class="col-gap-2" style="grid-column: span 3;"><label class="label-text">封面图</label>
            <div style="display: flex; gap: 8px; align-items: center;">
              <div style="width: 80px; height: 60px; border: 1px dashed var(--border-strong); border-radius: var(--r-sm); display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; background: var(--bg-subtle);" @click="coverInput?.click()">
                <img v-if="form.coverImage" :src="form.coverImage" style="width:100%; height:100%; object-fit: cover;" />
                <span v-else class="text-muted fz-xs">{{ uploadingCover ? '上传中…' : '点击上传' }}</span>
              </div>
              <input ref="coverInput" type="file" accept="image/*" style="display:none" @change="handleCoverUpload" />
              <button v-if="form.coverImage" class="btn btn-sm btn-ghost" @click="form.coverImage = ''">移除</button>
            </div>
          </div>
          <div class="col-gap-2" style="grid-column: span 3;">
            <label class="label-text">轮播图（点击右侧上传，可拖动排序）</label>
            <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: center; margin-top: 4px;">
              <div
                v-for="(url, i) in imageList"
                :key="url + i"
                :class="['rt-thumb', { 'is-dragging': dragIndex === Number(i) }]"
                draggable="true"
                @dragstart="onDragStart($event, Number(i))"
                @dragover="onDragOver"
                @drop="onDrop($event, Number(i))"
                @dragend="onDragEnd"
              >
                <img :src="url" />
                <span class="rt-thumb-remove" @click="removeImage(Number(i))" title="删除">✕</span>
              </div>
              <button class="btn btn-sm rt-thumb-add" :disabled="uploadingImage" @click="imageInput?.click()">{{ uploadingImage ? '上传中…' : '+ 上传' }}</button>
              <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="handleImageUpload" />
            </div>
          </div>
          <div class="col-gap-2" style="grid-column: span 3;">
            <label class="label-text">设施</label>
            <div v-if="amenitiesList.length" style="display:flex; flex-wrap:wrap; gap:6px;">
              <span v-for="(a, i) in amenitiesList" :key="a + i" class="rt-amenity">
                {{ a }}
                <button type="button" class="rt-amenity-x" @click="removeAmenity(Number(i))" title="删除">×</button>
              </span>
            </div>
            <div style="display:flex; gap:6px; margin-top: 6px;">
              <input v-model="amenityInput" class="input" style="flex:1;" placeholder="输入设施后回车或点击添加" @keyup.enter="addAmenity" />
              <button class="btn" type="button" :disabled="!amenityInput.trim()" @click="addAmenity">添加</button>
            </div>
          </div>
          <div class="col-gap-2" style="grid-column: span 3;"><label class="label-text">描述</label><input class="input input-block" v-model="form.description" /></div>
        </div>
      </div>
    </div>

    <div class="filterbar">
      <input v-model="keyword" class="input" placeholder="按房型名称搜索" />
      <button class="btn btn-primary" @click="(page=1, load())">搜索</button>
      <span class="filterbar-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>房型</th>
            <th class="num">面积</th>
            <th>床型</th>
            <th class="num">最大入住</th>
            <th class="num">价格</th>
            <th>设施</th>
            <th>描述</th>
            <th>封面</th>
            <th class="num">排序</th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in roomTypes" :key="t.id">
            <td class="fw-600">{{ t.name }}</td>
            <td class="num">{{ t.area }}㎡</td>
            <td>{{ t.bedType }}</td>
            <td class="num">{{ t.maxGuests }}</td>
            <td class="num">¥{{ t.price }}</td>
            <td class="text-muted fz-sm">{{ t.amenities }}</td>
            <td class="text-muted fz-sm" style="max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ t.description }}</td>
            <td>
              <img v-if="t.coverImage" :src="t.coverImage" style="width: 60px; height: 40px; object-fit: cover; border-radius: 4px;" />
              <span v-else class="text-muted fz-xs">无</span>
            </td>
            <td class="num">{{ t.sortOrder }}</td>
            <td class="actions">
              <button class="btn btn-sm" @click="openEdit(t)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(t.id)">删除</button>
            </td>
          </tr>
          <tr v-if="loading">
            <td colspan="10">
              <div class="loading-wrap">
                <div class="loading-dual-ring"></div>
                <span class="loading-text">加载中…</span>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && roomTypes.length === 0"><td colspan="10" class="empty">暂无房型</td></tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <button class="btn" :disabled="page<=1" @click="prevPage">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button class="btn" :disabled="page>=totalPages" @click="nextPage">下一页</button>
      <label class="label-text">跳转</label>
      <input type="number" min="1" :max="totalPages" v-model.number="pageInput" class="input" style="width:72px" />
      <button class="btn" @click="gotoPage">前往</button>
      <select v-model.number="size" @change="onSizeChange" class="select">
        <option :value="10">10 / 页</option><option :value="20">20 / 页</option><option :value="50">50 / 页</option>
      </select>
      <span class="pagination-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>
  </div>
</template>

<style scoped>
.rt-thumb {
  position: relative;
  width: 64px;
  height: 48px;
  border-radius: var(--r-sm);
  overflow: hidden;
  border: 1px solid var(--border);
  cursor: grab;
  background: var(--bg-subtle);
  transition: transform 0.15s, opacity 0.15s, border-color 0.15s;
  user-select: none;
}
.rt-thumb:active { cursor: grabbing; }
.rt-thumb:hover { border-color: var(--accent); }
.rt-thumb.is-dragging { opacity: 0.4; transform: scale(0.95); }
.rt-thumb img { width: 100%; height: 100%; object-fit: cover; display: block; pointer-events: none; }
.rt-thumb-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  background: var(--danger);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 10px;
  line-height: 1;
  opacity: 0;
  transition: opacity 0.15s;
}
.rt-thumb:hover .rt-thumb-remove { opacity: 1; }
.rt-thumb-add {
  width: 64px;
  height: 48px;
  border-style: dashed !important;
  background: transparent !important;
  color: var(--text-muted) !important;
  font-size: var(--fz-sm);
}
.rt-amenity {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 6px 4px 10px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
  font-size: var(--fz-sm);
  font-weight: 500;
  line-height: 1.2;
}
.rt-amenity-x {
  background: transparent;
  border: 0;
  color: inherit;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  padding: 0 4px;
  border-radius: 50%;
  opacity: 0.7;
}
.rt-amenity-x:hover { opacity: 1; background: rgba(37, 99, 235, 0.12); }
</style>
