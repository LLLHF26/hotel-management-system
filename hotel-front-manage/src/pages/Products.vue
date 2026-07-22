<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createProduct, deleteProduct, getProducts, updateProduct, uploadProductImage } from '../services/productService'
import { useToast } from '../composables/useToast'

const toast = useToast()

const products = ref<any[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1); const pageInput = ref(1); const size = ref(10)
const keyword = ref('')
const editing = ref(false)
const isCreate = ref(true)
const form = reactive<any>({
  id: null, name: '', category: '食物', price: '', unit: '份', stock: -1,
  status: '上架', image: '', description: '', sortOrder: 0,
})

// 图片上传相关
const isDragging = ref(false)
const imageInput = ref<HTMLInputElement | null>(null)
const uploadingImage = ref(false)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const categoryOptions = ['食物', '饮用水', '日用品', '其他']

async function load() {
  loading.value = true
  const res = await getProducts({ page: page.value, size: size.value, keyword: keyword.value })
  const data = res?.data ?? res
  products.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}
onMounted(load)

function openCreate() {
  Object.assign(form, { id: null, name: '', category: '食物', price: '', unit: '份', stock: -1, status: '上架', image: '', description: '', sortOrder: 0 })
  isCreate.value = true; editing.value = true
}
function openEdit(p: any) {
  Object.assign(form, {
    id: p.id, name: p.name, category: p.category, price: p.price, unit: p.unit || '份',
    stock: p.stock ?? -1, status: p.status, image: p.image || '', description: p.description || '',
    sortOrder: p.sortOrder ?? 0,
  })
  isCreate.value = false; editing.value = true
}
function cancelForm() { editing.value = false }

async function save() {
  if (!form.name || !form.price) { toast.error('请填写商品名称和价格'); return }
  const payload = {
    name: form.name, category: form.category, price: Number(form.price), unit: form.unit,
    stock: Number(form.stock ?? -1), status: form.status, image: form.image || undefined,
    description: form.description || undefined, sortOrder: Number(form.sortOrder ?? 0),
  }
  try {
    if (isCreate.value) await createProduct(payload)
    else await updateProduct(form.id, payload)
    editing.value = false
    await load()
    toast.success(isCreate.value ? '商品创建成功' : '商品更新成功')
  } catch (e: any) {
    toast.error(e?.message || '保存失败')
  }
}
async function handleDelete(id: number) {
  if (!confirm('确认删除该商品吗？')) return
  try { await deleteProduct(id); await load(); toast.success('已删除商品') } catch (e: any) { toast.error(e?.message || '删除失败') }
}

function prevPage() { if (page.value > 1) { page.value--; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; load() } }
function gotoPage() { let t = Number(pageInput.value); if (Number.isNaN(t) || t < 1) t = 1; if (t > totalPages.value) t = totalPages.value; page.value = t; load() }
function onSearch() { page.value = 1; pageInput.value = 1; load() }

// ===== 图片拖拽上传 =====
function onDragEnter(e: DragEvent) { e.preventDefault(); isDragging.value = true }
function onDragLeave(e: DragEvent) {
  e.preventDefault()
  if (!(e.currentTarget as HTMLElement)?.contains(e.relatedTarget as Node)) isDragging.value = false
}
async function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  const f = e.dataTransfer?.files?.[0]
  if (!f) return
  if (!f.type.startsWith('image/')) { toast.error('请上传图片文件（jpg/png/gif/webp）'); return }
  await doUploadImage(f)
}
function onImageSelect(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (f) doUploadImage(f)
}
function openImagePicker() { imageInput.value?.click() }
async function doUploadImage(file: File) {
  uploadingImage.value = true
  try {
    const url = await uploadProductImage(file)
    if (url) {
      form.image = url
      toast.success('封面上传成功')
    } else {
      toast.error('上传失败，未返回图片地址')
    }
  } catch (e: any) {
    toast.error(e?.message || '封面上传失败')
  } finally {
    uploadingImage.value = false
    if (imageInput.value) imageInput.value.value = ''
  }
}
function removeImage() { form.image = '' }

// 是否已有图片（编辑时从服务端加载的，或新上传的）
const hasImage = computed(() => !!form.image)

// ===== 补货 =====
const showRestock = ref(false)
const restockTarget = ref<any>(null)
const restockAmount = ref<number | string>(0)

function openRestock(p: any) {
  restockTarget.value = p
  restockAmount.value = 0
  showRestock.value = true
}
function closeRestock() { showRestock.value = false; restockTarget.value = null }
async function submitRestock() {
  if (!restockTarget.value) return
  const add = Number(restockAmount.value)
  if (!add || add <= 0 || Number.isNaN(add)) { toast.error('请输入有效的补货数量'); return }
  const currentStock = restockTarget.value.stock ?? -1
  // -1 表示不限量，补货后改为具体数量；否则累加
  const newStock = currentStock === -1 ? Math.max(0, add) : currentStock + add
  try {
    await updateProduct(restockTarget.value.id, { stock: newStock })
    showRestock.value = false
    await load()
    toast.success(`已为「${restockTarget.value.name}」补货 +${add}（当前库存：${newStock}）`)
  } catch (e: any) {
    toast.error(e?.message || '补货失败')
  }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">商品管理</h1>
        <p class="page-subtitle">管理客房商品（食物、饮用水、日用品等），客户可在小程序中下单。</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" @click="openCreate">新增商品</button>
      </div>
    </div>

    <div class="card mb-4">
      <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center;">
        <input class="input" v-model="keyword" placeholder="搜索商品名称" @keyup.enter="onSearch" />
        <button class="btn" @click="onSearch">查询</button>
      </div>
    </div>

    <div class="card">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>商品名称</th>
            <th>分类</th>
            <th>单价</th>
            <th>单位</th>
            <th>库存</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="8" class="text-center text-muted">加载中…</td></tr>
          <tr v-else-if="!products.length"><td colspan="8" class="text-center text-muted">暂无商品</td></tr>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>{{ p.category }}</td>
            <td>¥{{ p.price }}</td>
            <td>{{ p.unit }}</td>
            <td>{{ p.stock == -1 ? '不限' : p.stock }}</td>
            <td>
              <span :class="['badge', p.status === '上架' ? 'badge-green' : 'badge-gray']">{{ p.status }}</span>
            </td>
            <td>
              <button class="btn btn-sm" @click="openEdit(p)">编辑</button>
              <button class="btn btn-sm" :disabled="p.stock === -1" @click="openRestock(p)"
                style="background:var(--success-soft);color:var(--success);border-color:transparent;">补货</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(p.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pager">
        <button class="btn btn-sm" :disabled="page <= 1" @click="prevPage">上一页</button>
        <span class="pager-info">第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
        <button class="btn btn-sm" :disabled="page >= totalPages" @click="nextPage">下一页</button>
        <input class="input" type="number" v-model.number="pageInput" @keyup.enter="gotoPage" style="width:64px" />
        <button class="btn btn-sm" @click="gotoPage">跳转</button>
      </div>
    </div>

    <div v-if="editing" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">{{ isCreate ? '新增商品' : '编辑商品' }}</div>
          <div class="card-sub">填写商品信息后保存</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="cancelForm">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2"><label class="label-text">商品名称</label><input class="input input-block" v-model="form.name" /></div>
          <div class="col-gap-2"><label class="label-text">分类</label>
            <select class="input input-block" v-model="form.category">
              <option v-for="c in categoryOptions" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <div class="col-gap-2"><label class="label-text">单价（元）</label><input class="input input-block" type="number" v-model.number="form.price" /></div>
          <div class="col-gap-2"><label class="label-text">单位</label><input class="input input-block" v-model="form.unit" placeholder="份/瓶/个/盒" /></div>
          <div class="col-gap-2"><label class="label-text">库存（-1 不限量）</label><input class="input input-block" type="number" v-model.number="form.stock" /></div>
          <div class="col-gap-2"><label class="label-text">状态</label>
            <select class="input input-block" v-model="form.status">
              <option value="上架">上架</option>
              <option value="下架">下架</option>
            </select>
          </div>
          <div class="col-gap-2"><label class="label-text">排序权重</label><input class="input input-block" type="number" v-model.number="form.sortOrder" /></div>

          <!-- 封面图：拖拽上传区 -->
          <div class="col-gap-2" style="grid-column: span 2;">
            <label class="label-text">封面图</label>
            <!-- 已有图片 → 预览 + 删除 -->
            <div v-if="hasImage" class="img-preview-row">
              <img class="img-preview-thumb" :src="form.image" alt="" />
              <div class="img-preview-info">
                <span class="img-url line-clamp-1">{{ form.image }}</span>
                <span class="img-hint text-muted text-xs">点击下方重新上传可替换</span>
              </div>
              <button class="btn btn-sm btn-danger img-remove-btn" @click="removeImage">移除</button>
            </div>
            <!-- 拖拽上传区（无图时显示，或始终在预览下方作为替换入口） -->
            <div
              class="upload-dropzone"
              :class="{ 'is-dragging': isDragging }"
              style="padding: var(--sp-5); min-height: 100px;"
              @click="openImagePicker"
              @dragenter.prevent="onDragEnter"
              @dragover.prevent="onDragEnter"
              @dragleave.prevent="onDragLeave"
              @drop.prevent="onDrop"
            >
              <div class="upload-dropzone-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
              </div>
              <div class="upload-dropzone-title">{{ uploadingImage ? '正在上传…' : (hasImage ? '点击或拖拽替换封面图' : '点击或拖拽上传封面图') }}</div>
              <div class="upload-dropzone-hint">支持 JPG / PNG / GIF / WebP，建议尺寸 400×300 以上</div>
            </div>
            <input ref="imageInput" type="file" accept="image/*" style="display:none;" @change="onImageSelect" />
          </div>

          <div class="col-gap-2" style="grid-column: span 3;">
            <label class="label-text">描述</label>
            <textarea class="input input-block" rows="3" v-model="form.description"></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- 补货弹窗 -->
    <div v-if="showRestock" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">补货：{{ restockTarget?.name }}</div>
          <div class="card-sub">当前库存：{{ restockTarget?.stock == -1 ? '不限量' : (restockTarget?.stock || 0) }} {{ restockTarget?.unit }}</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="closeRestock">取消</button>
          <button class="btn btn-primary" @click="submitRestock">确认补货</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: flex; align-items: center; gap: 12px;">
          <label class="label-text" style="margin: 0;">增加库存数量：</label>
          <input class="input" type="number" v-model.number="restockAmount" placeholder="输入正整数"
            style="width: 160px;" min="1" @keyup.enter="submitRestock" />
          <span class="text-muted text-xs">{{ restockTarget?.unit }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ====== 封面图预览行 ====== */
.img-preview-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: #F7F6F3;
  border-radius: var(--r-md);
  border: 1rpx solid #EAE6E0;
  margin-bottom: 8px;
}
.img-preview-thumb {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  background: #EAE6E0;
}
.img-preview-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.img-url {
  font-size: 13px;
  color: var(--text);
  word-break: break-all;
}
.img-remove-btn {
  flex-shrink: 0;
}
</style>
