<template>
  <view class="order-list-page">
    <!-- status tabs -->
    <scroll-view scroll-x class="status-tabs" :show-scrollbar="false">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tab"
        :class="{ active: currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >{{ tab.label }}</view>
    </scroll-view>

    <!-- order list -->
    <view v-if="orders.length > 0">
      <view class="order-card" v-for="order in orders" :key="order.id" @click="goDetail(order.id)">
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="status-tag" :class="statusClass(order.status)">{{ order.status }}</text>
        </view>
        <view class="order-body">
          <text class="order-room">🏨 {{ order.roomTypeName || '房型' }}</text>
          <text class="order-date">📅 {{ order.checkInDate }} ~ {{ order.checkOutDate }} 共{{ order.nights || computeNights(order) }}晚</text>
          <text class="order-amount">💰 ¥{{ (order.totalAmount || 0).toFixed(2) }}</text>
          <text class="order-countdown" v-if="order.status === '待支付' && order.createTime">⏱ {{ countdownText(order) }}</text>
        </view>
        <view class="order-actions">
          <template v-if="order.status === '待支付'">
            <view class="action-btn outline" @click.stop="cancelOrder(order)">取消订单</view>
            <view class="action-btn primary" @click.stop="payOrder(order)">立即支付</view>
          </template>
          <view v-else class="action-btn outline" style="margin-left:auto" @click.stop="goDetail(order.id)">查看详情</view>
        </view>
      </view>
      <view class="load-more" v-if="hasMore" @click="loadMoreOrders">
        <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
      </view>
      <view v-else class="no-more">没有更多订单了</view>
    </view>

    <view v-if="!loading && orders.length === 0" class="empty-state">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
    </view>
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
        { label: '已支付', value: '已支付' },
        { label: '已入住', value: '已入住' },
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
    statusClass(status) {
      const map = { '待支付': 'pending', '已支付': 'paid', '已入住': 'checked-in', '已完成': 'completed', '已取消': 'cancelled' }
      return map[status] || ''
    },
    countdownText(order) {
      const createTime = new Date(order.createTime).getTime()
      if (isNaN(createTime)) return ''
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
    goDetail(id) {
      uni.navigateTo({ url: '/pages/order/detail?id=' + id })
    },
    cancelOrder(order) {
      uni.showModal({
        title: '取消订单',
        content: '确定取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await cancelOrder(order.id, '用户取消')
              uni.showToast({ title: '已取消', icon: 'success' })
              this.loadOrders(true)
            } catch { /* ignore */ }
          }
        }
      })
    },
    async payOrder(order) {
      const methods = ['微信支付', '支付宝', '银行卡']
      uni.showActionSheet({
        itemList: methods,
        success: (e) => {
          const valueMap = { '微信支付': 'WECHAT', '支付宝': 'ALIPAY', '银行卡': 'CARD' }
          const method = valueMap[methods[e.tapIndex]]
          uni.showModal({
            title: '确认支付',
            content: '使用' + methods[e.tapIndex] + '支付 ¥' + (order.totalAmount || 0).toFixed(2) + '？',
            success: async (res) => {
              if (res.confirm) {
                try {
                  await payOrderApi(order.id, order.totalAmount, method)
                  uni.showToast({ title: '支付成功', icon: 'success' })
                  this.loadOrders(true)
                } catch { /* ignore */ }
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
.order-list-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 24rpx;
}

.status-tabs {
  white-space: nowrap;
  background: #FFFFFF;
  padding: 20rpx 24rpx;
}

.status-tab {
  display: inline-block;
  padding: 12rpx 28rpx;
  background: #F5F6FA;
  border-radius: 28rpx;
  font-size: 26rpx;
  color: #666;
  margin-right: 16rpx;

  &.active {
    background: rgba(201, 169, 110, 0.12);
    color: #C9A96E;
    font-weight: 600;
  }
}

.order-card {
  background: #FFFFFF;
  margin: 16rpx 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 26rpx;
  color: #999;
}

.order-body {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.order-room { font-weight: 600; display: block; }
.order-date, .order-amount { display: block; }
.order-countdown { display: block; font-size: 24rpx; color: #E74C3C; margin-top: 4rpx; }

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 28rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 500;

  &.primary {
    background: linear-gradient(135deg, #C9A96E, #D4B87A);
    color: #FFF;
  }
  &.outline {
    background: #F5F6FA;
    color: #666;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  color: #999;
}
.empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; }

.load-more, .no-more {
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
}
.load-more { color: #C9A96E; }
.no-more { color: #CCC; }
</style>
