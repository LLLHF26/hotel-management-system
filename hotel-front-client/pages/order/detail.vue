<template>
  <!-- 订单不存在 -->
  <view class="detail-page empty-state" v-if="order && !order.id">
    <view class="empty-icon-wrap">
      <image class="empty-icon" src="/static/icons/document.png" mode="aspectFit"></image>
    </view>
    <text class="empty-title">订单不存在</text>
    <text class="empty-desc">该订单可能未创建成功或已被删除</text>
    <button class="back-btn" @click="goBack">返回</button>
  </view>

  <!-- 正常订单详情 -->
  <view class="detail-page" v-else-if="order">
    <!-- header -->
    <view class="detail-header">
      <text class="order-no">{{ order.orderNo }}</text>
      <text class="status-tag" :class="statusClass">{{ order.status }}</text>
    </view>

    <!-- room info -->
    <view class="info-card">
      <view class="card-title">房型信息</view>
      <view class="info-item">
        <image class="info-icon" src="/static/icons/hotel.png" mode="aspectFit"></image><text>{{ order.roomTypeName || '-' }}</text>
      </view>
      <view class="info-item">
        <image class="info-icon" src="/static/icons/calendar.png" mode="aspectFit"></image><text>入住 {{ order.checkInDate }}（14:00后）</text>
      </view>
      <view class="info-item">
        <image class="info-icon" src="/static/icons/calendar.png" mode="aspectFit"></image><text>退房 {{ order.checkOutDate }}（12:00前）</text>
      </view>
      <view class="info-item">
        <image class="info-icon" src="/static/icons/moon.png" mode="aspectFit"></image><text>共{{ order.nights || computeNights }}晚</text>
      </view>
    </view>

    <!-- price detail -->
    <view class="info-card">
      <view class="card-title">费用明细</view>
      <view class="price-row">
        <text>房费 ({{ order.nights || computeNights }}晚)</text>
        <text>¥{{ (order.roomTotal || 0).toFixed(2) }}</text>
      </view>
      <view v-if="(order.extraTotal || 0) > 0" class="price-row">
        <text>额外消费</text>
        <text>¥{{ (order.extraTotal || 0).toFixed(2) }}</text>
      </view>
      <view class="price-row total">
        <text>应付总额</text>
        <text class="num">¥{{ (order.totalAmount || 0).toFixed(2) }}</text>
      </view>
      <view class="price-row">
        <text>已付金额</text>
        <text>¥{{ (order.paidAmount || 0).toFixed(2) }}</text>
      </view>
    </view>

    <!-- extra items -->
    <view class="info-card" v-if="order.extras && order.extras.length > 0">
      <view class="card-title">消费明细</view>
      <view class="extra-item" v-for="e in order.extras" :key="e.id">
        <view class="extra-left">
          <text class="extra-name">{{ e.itemName }}</text>
          <text class="extra-meta">x{{ e.quantity || 1 }}</text>
        </view>
        <text class="extra-price">¥{{ (e.subtotal || (e.amount || 0) * (e.quantity || 1)).toFixed(2) }}</text>
      </view>
    </view>

    <!-- guest info -->
    <view class="info-card">
      <view class="card-title">入住人信息</view>
      <view class="info-item">姓名：{{ order.customerName || '-' }}</view>
      <view class="info-item">手机：{{ order.customerPhone || '-' }}</view>
      <view class="info-item" v-if="order.remark">备注：{{ order.remark }}</view>
    </view>

    <!-- time -->
    <view class="info-card">
      <view class="info-item" style="color:#999;font-size:24rpx">创建时间：{{ order.createTime || '-' }}</view>
    </view>

    <!-- actions -->
    <view class="action-row" v-if="order.status === '待支付'">
      <button class="cancel-btn" @click="handleCancel">取消订单</button>
      <button class="pay-btn" @click="handlePay">去支付</button>
    </view>
  </view>
</template>

<script>
import { getMyOrderDetail, cancelOrder, payOrder } from '../../src/api/order'

export default {
  data() {
    return { order: null }
  },
  computed: {
    computeNights() {
      if (!this.order) return 0
      const d = (new Date(this.order.checkOutDate) - new Date(this.order.checkInDate)) / 86400000
      return d > 0 ? d : 0
    },
    statusClass() {
      if (!this.order) return ''
      const map = { '待支付': 'pending', '已支付': 'paid', '已入住': 'checked-in', '已完成': 'completed', '已取消': 'cancelled' }
      return map[this.order.status] || ''
    }
  },
  onLoad(options) {
    const id = options.id || options.orderNo || ''
    console.log('=== 详情页收到参数 ===', JSON.stringify(options), '查询ID:', id)
    if (id) this.loadDetail(id)
    else this.order = {} // 触发空状态显示
  },
  methods: {
    async loadDetail(id) {
      try {
        const res = await getMyOrderDetail(id)
        const data = res.data || res
        console.log('=== 订单详情返回 ===', JSON.stringify(data), '有id:', !!data.id)
        this.order = data
        // 如果后端返回了空对象（无 id），视为订单不存在
        if (!data || !data.id) {
          this.order = {}
        }
      } catch (err) {
        console.log('=== 订单详情异常 ===', err)
        this.order = {}
        const msg = (err && err.msg) || (err && err.data && err.data.message) || '订单信息加载失败'
        uni.showToast({ title: msg, icon: 'none', duration: 2000 })
      }
    },
    handleCancel() {
      uni.showModal({
        title: '取消订单',
        content: '确定取消吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await cancelOrder(this.order.id, '用户取消')
              uni.showToast({ title: '已取消', icon: 'success' })
              this.loadDetail(this.order.id)
            } catch { /* ignore */ }
          }
        }
      })
    },
    handlePay() {
      if (!this.order || !this.order.id) {
        uni.showToast({ title: '订单信息不完整，无法支付', icon: 'none' })
        return
      }
      console.log('=== 发起支付 ===', 'orderId:', this.order.id, 'amount:', this.order.totalAmount)
      const methods = ['微信支付', '支付宝', '银行卡']
      uni.showActionSheet({
        itemList: methods,
        success: (e) => {
          const valueMap = { '微信支付': 'WECHAT', '支付宝': 'ALIPAY', '银行卡': 'CARD' }
          const method = valueMap[methods[e.tapIndex]]
          uni.showModal({
            title: '确认支付',
            content: '使用' + methods[e.tapIndex] + '支付 ¥' + (this.order.totalAmount || 0).toFixed(2) + '？',
            success: async (res) => {
              if (res.confirm) {
                try {
                  await payOrder(this.order.id, this.order.totalAmount, method)
                  uni.showToast({ title: '支付成功', icon: 'success' })
                  this.loadDetail(this.order.id)
                } catch { /* ignore */ }
              }
            }
          })
        }
      })
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/order/list' }) })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 48rpx;

  &.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 300rpx;
  }
}

.empty-icon-wrap {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  background: linear-gradient(135deg, #F0F0F0, #E8E8E8);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 28rpx;
}
.empty-icon { width: 56rpx; height: 56rpx; opacity: .4; }
.empty-title { font-size: 34rpx; font-weight: 600; color: #333; margin-bottom: 12rpx; }
.empty-desc { font-size: 26rpx; color: #999; margin-bottom: 40rpx; }
.back-btn {
  width: 240rpx; height: 80rpx; line-height: 80rpx; text-align: center;
  background: linear-gradient(135deg, #C9A96E, #D4B87A); color: #FFF;
  font-size: 28rpx; font-weight: 600; border-radius: 40rpx;
  border: none;
  &::after { border: none; }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FFFFFF;
  padding: 24rpx 28rpx;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.info-card {
  background: #FFFFFF;
  margin: 0 20rpx 16rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.info-item {
  display: flex; align-items: center; gap: 8rpx;
  font-size: 28rpx;
  color: #555;
  line-height: 2;
}
.info-icon { width: 28rpx; height: 28rpx; }

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
  font-size: 28rpx;
  color: #666;

  &.total {
    padding: 14rpx 0;
    border-top: 1rpx solid #F0F0F0;
    margin-top: 4rpx;
    font-weight: 600;
    color: #333;

    .num { color: #C9A96E; font-size: 34rpx; }
  }
}

.action-row {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  margin-top: 12rpx;
}

.cancel-btn {
  flex: 1;
  height: 88rpx;
  background: #F5F6FA;
  color: #666;
  font-size: 30rpx;
  border-radius: 14rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  &::after { border: none; }
}

.pay-btn {
  flex: 2;
  height: 88rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 14rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  &::after { border: none; }
}

.extra-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F5F5F5;

  &:last-child { border-bottom: none; }
}

.extra-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.extra-name {
  font-size: 28rpx;
  color: #555;
}

.extra-meta {
  font-size: 24rpx;
  color: #999;
  background: #F5F6FA;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}

.extra-price {
  font-size: 28rpx;
  color: #C9A96E;
  font-weight: 500;
}
</style>
