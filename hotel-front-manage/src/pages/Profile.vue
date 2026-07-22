<script setup lang="ts">
const profile = [
  { label: '用户名', value: 'admin',  readonly: true },
  { label: '姓名',   value: '李建国' },
  { label: '手机号', value: '13911112222' },
  { label: '邮箱',   value: 'admin@example.com' },
]
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">个人信息</h1>
        <p class="page-subtitle">查看并编辑个人资料，支持修改基本信息与密码。</p>
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">基本资料</div>
          <div class="card-sub">仅本人可见</div>
        </div>
        <div class="page-actions">
          <button class="btn btn-primary">保存资料</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
          <div v-for="p in profile" :key="p.label" class="col-gap-2">
            <label class="label-text">{{ p.label }}</label>
            <input class="input input-block" :value="p.value" :readonly="p.readonly" />
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-head">
        <div>
          <div class="card-title">修改密码</div>
          <div class="card-sub">建议每 90 天更换一次</div>
        </div>
        <div class="page-actions">
          <button class="btn btn-primary">修改密码</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2">
            <label class="label-text">旧密码</label>
            <input class="input input-block" type="password" placeholder="请输入旧密码" />
          </div>
          <div class="col-gap-2">
            <label class="label-text">新密码</label>
            <input class="input input-block" type="password" placeholder="请输入新密码" />
          </div>
          <div class="col-gap-2" style="grid-column: span 2;">
            <label class="label-text">确认新密码</label>
            <input class="input input-block" type="password" placeholder="请再次输入新密码" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
