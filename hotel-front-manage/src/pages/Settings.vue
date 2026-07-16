<script setup lang="ts">
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>系统设置</h1>
        <p class="page-subtitle">配置酒店基础信息与业务参数，当前为前端显示版面。</p>
      </div>
    </div>

    <div class="form-card">
      <div class="form-grid" style="grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px;">
        <div class="input-group">
          <label>酒店名称</label>
          <input type="text" value="云端酒店" />
        </div>
        <div class="input-group">
          <label>联系电话</label>
          <input type="text" value="010-88882222" />
        </div>
        <div class="input-group">
          <label>酒店地址</label>
          <input type="text" value="北京市朝阳区示例路 88 号" />
        </div>
        <div class="input-group">
          <label>入住时间</label>
          <input type="text" value="14:00" />
        </div>
        <div class="input-group">
          <label>退房时间</label>
          <input type="text" value="12:00" />
        </div>
        <div class="input-group">
          <label>满房预警阈值</label>
          <input type="text" value="90%" />
        </div>
      </div>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <button class="primary-button">保存设置</button>
      </div>
    </div>
  </div>
</template>
