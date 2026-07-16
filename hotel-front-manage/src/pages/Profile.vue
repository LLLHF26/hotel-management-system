<script setup lang="ts">
</script>

<template>
  <div class="page-wrap">
    <h1>个人信息</h1>
    <p class="page-subtitle">查看并编辑个人资料，支持修改基本信息与密码。</p>

    <div class="form-card" style="margin-bottom: 18px;">
      <div class="form-grid" style="grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px;">
        <div class="input-group">
          <label>用户名</label>
          <input type="text" value="admin" readonly />
        </div>
        <div class="input-group">
          <label>姓名</label>
          <input type="text" value="李建国" />
        </div>
        <div class="input-group">
          <label>手机号</label>
          <input type="text" value="13911112222" />
        </div>
        <div class="input-group">
          <label>邮箱</label>
          <input type="text" value="admin@example.com" />
        </div>
      </div>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <button class="primary-button">保存资料</button>
      </div>
    </div>

    <div class="form-card">
      <h2>修改密码</h2>
      <div class="form-grid" style="grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px;">
        <div class="input-group">
          <label>旧密码</label>
          <input type="password" placeholder="请输入旧密码" />
        </div>
        <div class="input-group">
          <label>新密码</label>
          <input type="password" placeholder="请输入新密码" />
        </div>
        <div class="input-group" style="grid-column: span 2;">
          <label>确认新密码</label>
          <input type="password" placeholder="请再次输入新密码" />
        </div>
      </div>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <button class="primary-button">修改密码</button>
      </div>
    </div>
  </div>
</template>
