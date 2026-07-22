<template>
  <view class="order-list-page">
    <!-- header area -->
    <view class="page-header">
      <text class="header-title">我的订单</text>
      <text class="header-sub">管理您的预订记录</text>
    </view>

    <!-- status tabs -->
    <scroll-view scroll-x class="status-scroll" :show-scrollbar="false">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-chip"
        :class="{ active: currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="status-text">{{ tab.label }}</text>
      </view>
    </scroll-view>

    <!-- order list -->
    <view v-if="orders.length > 0" class="order-list">
      <view class="order-card" v-for="order in orders" :key="order.id" @click="goDetail(order.id)">
        <!-- status indicator -->
        <view class="card-status" :class="statusBg(order.status)">
          <image class="status-icon" :src="statusIcon(order.status)" mode="aspectFit"></image>
          <text class="status-label">{{ order.status }}</text>
        </view>

        <!-- body -->
        <view class="card-body">
          <view class="room-row">
            <text class="room-name">{{ order.roomTypeName || '房型' }}</text>
            <text class="order-no">{{ order.orderNo }}</text>
          </view>

          <view class="info-line">
            <view class="info-label"><image class="info-icon-sm" src="/static/icons/calendar.png" mode="aspectFit"></image>日期</view>
            <text class="info-val">{{ order.checkInDate }} ~ {{ order.checkOutDate }} · {{ order.nights || computeNights(order) }}晚</text>
          </view>

          <view class="info-line">
            <view class="info-label"><image class="info-icon-sm" src="/static/icons/money.png" mode="aspectFit"></image>金额</view>
            <text class="info-price">¥{{ (order.totalAmount || 0).toFixed(2) }}</text>
          </view>

          <!-- countdown -->
          <view class="countdown-bar" v-if="order.status === '待支付' && order.createTime">
            <image class="cd-icon" src="/static/icons/pending.png" mode="aspectFit"></image>
            <text class="cd-text" :class="{ urgent: isUrgent(order) }">{{ countdownText(order) }}</text>
          </view>
        </view>

        <!-- actions -->
        <view class="card-actions" v-if="order.status === '待支付'">
          <view class="action-btn ghost" @click.stop="cancelOrder(order)">取消</view>
          <view class="action-btn primary" @click.stop="payOrder(order)">去支付</view>
        </view>
        <view class="card-actions" v-else-if="order.status !== '待支付'">
          <view class="action-btn ghost" @click.stop="goDetail(order.id)">查看详情</view>
        </view>
      </view>

      <view class="load-more" v-if="hasMore" @click="loadMoreOrders">
        <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
      </view>
      <view v-else class="end-hint">
        <text>— 已到底 —</text>
      </view>
    </view>

    <!-- empty -->
    <view v-if="!loading && orders.length === 0" class="empty-state">
      <view class="empty-art">
        <image class="empty-emoji" src="/static/icons/clipboard.png" mode="aspectFit"></image>
      </view>
      <text class="empty-title">暂无订单</text>
      <text class="empty-desc">您的预订记录会显示在这里</text>
      <view class="empty-go" @click="goHome">
        <text class="empty-go-text">去选房</text>
      </view>
    </view>

    <view class="bottom-pad"></view>
  </view>
</template>

<script>
import { getMyOrders, cancelOrder, payOrder as payOrderApi } from '../../src/api/order'

export default {
  data() {
    return {
      currentStatus: '',
      statusTabs: [
        { label: '全部', value: '' },
        { label: '待支付', value: '待支付' },
        { label: '已确认', value: '已支付' },
        { label: '入住中', value: '已入住' },
        { label: '已完成', value: '已完成' },
        { label: '已取消', value: '已取消' }
      ],
      orders: [],
      page: 1,
      size: 10,
      hasMore: true,
      loading: false,
      loadingMore: false,
      now: Date.now(),
      _timer: null
    }
  },
  onLoad() {
    this.loadOrders(true)
  },
  onShow() {
    this._timer = setInterval(() => { this.now = Date.now() }, 1000)
  },
  onHide() {
    if (this._timer) { clearInterval(this._timer); this._timer = null }
  },
  onUnload() {
    if (this._timer) { clearInterval(this._timer); this._timer = null }
  },
  methods: {
    computeNights(order) {
      if (order.checkInDate && order.checkOutDate) {
        const diff = (new Date(order.checkOutDate) - new Date(order.checkInDate)) / 86400000
        return diff > 0 ? diff : 0
      }
      return 0
    },
    statusIcon(status) {
      const map = { '待支付': '/static/icons/pending.png', '已支付': '/static/icons/check.png', '已入住': '/static/icons/key.png', '已完成': '/static/icons/completed.png', '已取消': '/static/icons/cancel.png' }
      return map[status] || '/static/icons/document.png'
    },
    statusBg(status) {
      const map = { '待支付': 'pending', '已支付': 'paid', '已入住': 'checked-in', '已完成': 'completed', '已取消': 'cancelled' }
      return map[status] || ''
    },
    isUrgent(order) {
      if (!order.createTime) return false
      const deadline = new Date(order.createTime).getTime() + 30 * 60 * 1000
      return (deadline - this.now) < 600 * 1000
    },
    countdownText(order) {
      const createTime = new Date(order.createTime).getTime()
      if (isNaN(createTime)) return '--'
      const deadline = createTime + 30 * 60 * 1000
      const remaining = Math.max(0, Math.floor((deadline - this.now) / 1000))
      if (remaining <= 0) return '已超时'
      const m = Math.floor(remaining / 60)
      const s = remaining % 60
      return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0')
    },
    async loadOrders(reset) {
      if (reset) { this.page = 1; this.loading = true } else { this.loadingMore = true }
      try {
        const params = { page: this.page, size: this.size }
        if (this.currentStatus) params.status = this.currentStatus
        const res = await getMyOrders(params)
        const records = (res.data && res.data.records) ? res.data.records : []
        if (reset) this.orders = records
        else this.orders = this.orders.concat(records)
        this.hasMore = records.length >= this.size
      } catch { /* ignore */ }
      finally { this.loading = false; this.loadingMore = false }
    },
    switchStatus(val) {
      if (this.currentStatus === val) return
      this.currentStatus = val
      this.loadOrders(true)
    },
    loadMoreOrders() {
      if (this.loadingMore || !this.hasMore) return
      this.page++
      this.loadOrders(false)
    },
    goDetail(id) { uni.navigateTo({ url: '/pages/order/detail?id=' + id }) },
    goHome() { uni.switchTab({ url: '/pages/index/index' }) },
    cancelOrder(order) {
      uni.showModal({
        title: '取消订单',
        content: '确定取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await cancelOrder(order.id, '用户取消'); uni.showToast({ title: '已取消', icon: 'success' }); this.loadOrders(true) } catch { /* ignore */ }
          }
        }
      })
    },
    async payOrder(order) {
      const methods = ['微信支付', '支付宝', '银行卡']
      uni.showActionSheet({
        itemList: methods,
        success: (e) => {
          const methodMap = { '微信支付': 'WECHAT', '支付宝': 'ALIPAY', '银行卡': 'CARD' }
          const method = methodMap[methods[e.tapIndex]]
          uni.showModal({
            title: '确认支付',
            content: '使用' + methods[e.tapIndex] + '支付 ¥' + (order.totalAmount || 0).toFixed(2) + '？',
            success: async (res) => {
              if (res.confirm) {
                try { await payOrderApi(order.id, order.totalAmount, method); uni.showToast({ title: '支付成功', icon: 'success' }); this.loadOrders(true) } catch { /* ignore */ }
              }
            }
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$bg: #F8F6F3;
$card-bg: #FFFFFF;
$text-primary: #1A1A2E;
$text-secondary: #6B7280;
$text-light: #9CA3AF;
$accent-orange: #EA580C;
$accent-gold: #D97706;
$radius-sm: 14rpx;
$radius-md: 20rpx;

.order-list-page { background: $bg; min-height: 100vh; }

/* ====== Header ====== */
.page-header {
  padding: calc(24rpx + env(safe-area-inset-top)) 28rpx 16rpx;
  background: $card-bg;
}
.header-title { font-size: 38rpx; font-weight: 800; color: $text-primary; display: block; }
.header-sub { font-size: 25rpx; color: $text-light; margin-top: 4rpx; }

/* ====== Status Tabs ====== */
.status-scroll {
  white-space: nowrap;
  background: $card-bg;
  padding: 4rpx 0 18rpx 20rpx;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120rpx;
  height: 64rpx;
  background: #F3F0EB;
  border-radius: 32rpx;
  margin-right: 14rpx;
  padding: 0 28rpx;
}

.status-text { font-size: 26rpx; color: $text-secondary; font-weight: 500; }

.status-chip.active {
  background: linear-gradient(135deg, #F97316, #EA580C);
  .status-text { color: #FFF; font-weight: 700; }
}

/* ====== Order Card ====== */
.order-list { padding: 20rpx 20rpx 0; }

.order-card {
  background: $card-bg;
  border-radius: $radius-md;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,.04);

  &:active { transform: scale(.99); }
}

.card-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid #F0EDE8;

  &.pending { .status-label { color: #F59E0B; } }
  &.paid { .status-label { color: #3B82F6; } }
  &.checked-in { .status-label { color: #10B981; } }
  &.completed { .status-label { color: #6B7280; } }
  &.cancelled { .status-label { color: #EF4444; } }
}

.status-icon { width: 26rpx; height: 26rpx; }
.status-label { font-size: 25rpx; font-weight: 700; }

.card-body { padding: 20rpx 24rpx; }

.room-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.room-name { font-size: 31rpx; font-weight: 700; color: $text-primary; }
.order-no { font-size: 22rpx; color: $text-light; }

.info-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}
.info-label { display: flex; align-items: center; gap: 4rpx; font-size: 25rpx; color: $text-secondary; }
.info-icon-sm { width: 24rpx; height: 24rpx; }
.info-val { font-size: 25rpx; color: $text-primary; }
.info-price { font-size: 29rpx; color: $accent-orange; font-weight: 700; }

.countdown-bar {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  padding: 10rpx 16rpx;
  background: #FEF3C7;
  border-radius: $radius-sm;
  width: fit-content;
}
.cd-icon { width: 24rpx; height: 24rpx; }
.cd-text { font-size: 24rpx; color: $accent-gold; font-weight: 600; &.urgent { color: #EF4444; } }

.card-actions {
  display: flex;
  gap: 14rpx;
  padding: 0 24rpx 22rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 14rpx 32rpx;
  border-radius: 28rpx;
  font-size: 26rpx;
  font-weight: 600;

  &.primary {
    background: linear-gradient(135deg, #F97316, #EA580C);
    color: #FFF;
  }
  &.ghost {
    background: #F3F0EB;
    color: $text-secondary;
  }
}

/* ====== Empty State ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
}
.empty-art {
  width: 140rpx; height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 24rpx;
}
.empty-emoji { width: 56rpx; height: 56rpx; }
.empty-title { font-size: 32rpx; font-weight: 700; color: $text-primary; margin-bottom: 8rpx; }
.empty-desc { font-size: 26rpx; color: $text-light; margin-bottom: 28rpx; }
.empty-go {
  background: linear-gradient(135deg, #F97316, #EA580C);
  padding: 18rpx 48rpx; border-radius: 36rpx;
}
.empty-go-text { color: #FFF; font-size: 28rpx; font-weight: 600; }

.load-more { text-align: center; padding: 24rpx; }
.load-more text { color: $accent-gold; font-size: 26rpx; font-weight: 600; }
.end-hint { text-align: center; padding: 24rpx; }
.end-hint text { color: $text-light; font-size: 24rpx; }
.bottom-pad { height: 48rpx; }
</style>
