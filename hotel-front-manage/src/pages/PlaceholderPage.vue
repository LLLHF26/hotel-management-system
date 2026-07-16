<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ title: string }>()

const summaryCards = [
  { label: '今日在住', value: '128', sub: '空余房间 42' },
  { label: '当前入住率', value: '86%', sub: '可出租 158 间' },
  { label: '今日营收', value: '¥21,860', sub: '较昨日 +12%' },
  { label: '待处理告警', value: '4 条', sub: '2 条未读' }
]

const pageType = computed(() => {
  if (props.title.includes('房态')) return 'room'
  if (props.title.includes('财务')) return 'finance'
  return 'dashboard'
})
</script>

<template>
  <div class="page-wrap dashboard-page">
    <div class="page-header">
      <div>
        <h1>{{ props.title }}</h1>
        <p class="page-subtitle">实时概览与快捷入口，帮助您快速进入常用业务。</p>
      </div>
    </div>

    <section class="cards">
      <div class="card" v-for="card in summaryCards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
        <div class="sub">{{ card.sub }}</div>
      </div>
    </section>

    <section class="charts">
      <div class="chart big">
        <div class="chart-title">{{ pageType === 'finance' ? '近7日营收趋势' : '今日房态概览' }}</div>
        <div class="chart-body placeholder-body">
          <div class="placeholder-stat">
            <div class="placeholder-number">{{ pageType === 'finance' ? '¥134,720' : '84%' }}</div>
            <div class="placeholder-label">{{ pageType === 'finance' ? '近7日总营收' : '整体入住率' }}</div>
          </div>
        </div>
      </div>
      <div class="chart side">
        <div class="chart-title">{{ pageType === 'finance' ? '重点房型收益' : '房型占用详情' }}</div>
        <div class="chart-body">
          <ul class="trend-list">
            <li v-for="item in pageType === 'finance' ? ['豪华套房', '商务双床', '标准间'] : ['大床房', '双床房', '行政套房']" :key="item">
              {{ item }}
            </li>
          </ul>
        </div>
      </div>
    </section>

    <section class="shortcuts">
      <router-link class="shortcut" to="/admin/orders">订单管理</router-link>
      <router-link class="shortcut" to="/admin/finance">营收报表</router-link>
    </section>
  </div>
</template>
