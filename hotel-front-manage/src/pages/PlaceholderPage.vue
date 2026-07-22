<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ title: string }>()

const summaryCards = [
  { label: '今日在住',   value: '128',   sub: '空余房间 42' },
  { label: '当前入住率', value: '86%',   sub: '可出租 158 间' },
  { label: '今日营收',   value: '¥21,860', sub: '较昨日 +12%' },
  { label: '待处理告警', value: '4 条',  sub: '2 条未读' },
]

const isFinance = computed(() => props.title.includes('财务'))
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">{{ props.title }}</h1>
        <p class="page-subtitle">实时概览与快捷入口，帮助您快速进入常用业务。</p>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat" v-for="c in summaryCards" :key="c.label">
        <div class="stat-label">{{ c.label }}</div>
        <div class="stat-value">{{ c.value }}</div>
        <div class="stat-sub">{{ c.sub }}</div>
      </div>
    </div>

    <div class="two-col mt-4">
      <div class="card">
        <div class="card-head">
          <div>
            <div class="card-title">{{ isFinance ? '近 7 日营收趋势' : '今日房态概览' }}</div>
            <div class="card-sub">实时数据</div>
          </div>
        </div>
        <div class="card-body" style="min-height: 220px; display: flex; align-items: center; justify-content: center;">
          <div class="text-center">
            <div style="font-size: 36px; font-weight: 700; letter-spacing: -0.02em;">{{ isFinance ? '¥134,720' : '84%' }}</div>
            <div class="text-muted fz-sm">{{ isFinance ? '近 7 日总营收' : '整体入住率' }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-head">
          <div class="card-title">{{ isFinance ? '重点房型收益' : '房型占用详情' }}</div>
        </div>
        <div class="card-body">
          <ul class="col-gap-2">
            <li v-for="item in (isFinance ? ['豪华套房','商务双床','标准间'] : ['大床房','双床房','行政套房'])" :key="item"
                class="row row-gap-2" style="padding: 10px 12px; background: var(--bg-subtle); border-radius: var(--r-md);">
              <span class="fw-600">{{ item }}</span>
              <span class="filterbar-spacer"></span>
              <span class="text-muted fz-sm">详情 ›</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div class="row row-gap-2 mt-4">
      <router-link to="/admin/orders"  class="btn">订单管理</router-link>
      <router-link to="/admin/finance" class="btn">营收报表</router-link>
    </div>
  </div>
</template>
