<template>
  <view class="home-page">
    <!-- banner -->
    <swiper class="banner-swiper" indicator-dots indicator-color="rgba(255,255,255,.4)" indicator-active-color="#FFFFFF" autoplay circular :interval="4000">
      <swiper-item v-for="(b, i) in banners" :key="i">
        <view class="banner-card">
          <image class="banner-img" :src="b.img" mode="aspectFill"></image>
          <view class="banner-overlay" :style="{ background: b.overlay }"></view>
          <view class="banner-content">
            <text class="banner-badge">{{ b.badge }}</text>
            <text class="banner-title">{{ b.title }}</text>
            <text class="banner-desc">{{ b.desc }}</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <!-- quick entries -->
    <view class="quick-section">
      <view class="quick-row">
        <view
          v-for="(entry, i) in quickEntries"
          :key="i"
          class="quick-chip"
          @click="entry.action"
        >
          <image class="quick-icon-img" :src="entry.icon" mode="aspectFit"></image>
          <text class="quick-label">{{ entry.label }}</text>
        </view>
      </view>
    </view>

    <!-- hot rooms -->
    <view class="section-block">
      <view class="section-head">
        <view class="section-title-row">
          <image class="section-icon" src="/static/icons/sparkle.png" mode="aspectFit"></image>
          <text class="section-title">热门房型</text>
        </view>
        <text class="section-more" @click="goRoomList">查看全部</text>
      </view>

      <scroll-view scroll-x class="hot-scroll" :show-scrollbar="false">
        <view class="hot-card" v-for="room in hotRooms" :key="room.id" @click="goRoomDetail(room.id)">
          <view class="hot-img-wrap">
            <image class="hot-img" :src="room.coverImage || defaultImg" mode="aspectFill"></image>
            <view class="hot-img-overlay"></view>
          </view>
          <view class="hot-body">
            <text class="hot-name line-clamp-1">{{ room.name }}</text>
            <view class="hot-tags">
              <text class="hot-tag" v-if="room.bedType">{{ room.bedType }}</text>
              <text class="hot-tag" v-if="room.maxGuests">{{ room.maxGuests }}人</text>
              <text class="hot-tag" v-if="room.area">{{ room.area }}㎡</text>
            </view>
            <view class="hot-price-row">
              <text class="hot-price">{{ room.price }}</text>
              <text class="hot-price-unit">/晚</text>
            </view>
          </view>
          <view class="hot-book-btn">预订</view>
        </view>
      </scroll-view>
    </view>

    <!-- 服务功能 -->
    <view class="section-block service-section">
      <view class="section-head">
        <view class="section-title-row">
          <image class="section-icon" src="/static/icons/sparkle.png" mode="aspectFit"></image>
          <text class="section-title">服务功能</text>
        </view>
      </view>

      <!-- 呼叫人员 -->
      <view class="svc-card">
        <view class="svc-card-head">
          <text class="svc-card-title">呼叫人员</text>
          <text class="svc-card-sub">打扫 / 送物 / 维修，一键呼叫</text>
        </view>
        <view class="svc-types">
          <view class="svc-type" v-for="t in serviceTypes" :key="t" @click="openService(t)">
            <text class="svc-type-icon">{{ serviceTypeIcon(t) }}</text>
            <text class="svc-type-label">{{ t }}</text>
          </view>
        </view>
        <view class="svc-history" v-if="myRequests.length">
          <text class="svc-history-title">我的呼叫</text>
          <view class="svc-req" v-for="r in myRequests" :key="r.id">
            <text class="svc-req-type">{{ r.type }}</text>
            <text class="svc-req-remark" v-if="r.remark">{{ r.remark }}</text>
            <text class="svc-req-status" :class="statusClass(r.status)">{{ r.status }}</text>
          </view>
        </view>
      </view>

      <!-- 客房好物 -->
      <view class="svc-card">
        <view class="svc-card-head">
          <text class="svc-card-title">客房好物</text>
          <text class="svc-card-sub">美食饮品日用品，送到房间</text>
        </view>
        <view class="prod-list">
          <view class="prod-item" v-for="p in products" :key="p.id">
            <image class="prod-img" :src="p.image || defaultImg" mode="aspectFill"></image>
            <view class="prod-info">
              <text class="prod-name line-clamp-1">{{ p.name }}</text>
              <text class="prod-cat">{{ p.category }}</text>
              <view class="prod-price-row">
                <text class="prod-price">{{ p.price }}</text>
                <text class="prod-unit">/{{ p.unit }}</text>
              </view>
            </view>
            <view class="prod-stepper">
              <view class="step-btn" @click="changeQty(p, -1)">−</view>
              <text class="step-num">{{ p.qty }}</text>
              <view class="step-btn" @click="changeQty(p, 1)">＋</view>
            </view>
          </view>
        </view>
        <view v-if="!products.length" class="svc-empty">暂无在售商品</view>
      </view>

      <!-- 积分抵扣提示 -->
      <view class="points-bar" v-if="pointsConfig.enabled && cartCount > 0">
        <text class="points-text">可用积分 {{ customerPoints }}，本单最多抵 ¥{{ maxDiscount.toFixed(2) }}（{{ ratioText }}）</text>
      </view>
    </view>

    <!-- 呼叫弹窗 -->
    <view class="modal-mask" v-if="serviceShow" @click="closeService">
      <view class="modal" @click.stop>
        <text class="modal-title">呼叫「{{ serviceType }}」</text>
        <view class="svc-types svc-types-modal">
          <view class="svc-type" v-for="t in serviceTypes" :key="t" :class="{ active: serviceType === t }" @click="serviceType = t">
            <text class="svc-type-label">{{ t }}</text>
          </view>
        </view>
        <textarea class="modal-textarea" v-model="serviceRemark" placeholder="补充说明（可选）" maxlength="200" />
        <view class="modal-actions">
          <view class="modal-btn ghost" @click="closeService">取消</view>
          <view class="modal-btn primary" @click="submitService">提交呼叫</view>
        </view>
      </view>
    </view>

    <!-- 购物车结算弹窗 -->
    <view class="modal-mask" v-if="cartShow" @click="closeCart">
      <view class="modal" @click.stop>
        <text class="modal-title">确认下单</text>
        <scroll-view scroll-y class="cart-lines">
          <view class="cart-line" v-for="it in cartItems" :key="it.id">
            <text class="cart-line-name">{{ it.name }} ×{{ it.qty }}</text>
            <text class="cart-line-amt">¥{{ (it.price * it.qty).toFixed(2) }}</text>
          </view>
        </scroll-view>
        <view class="cart-row"><text class="cart-row-label">小计</text><text class="cart-row-val">¥{{ subtotal.toFixed(2) }}</text></view>
        <view class="cart-points" v-if="pointsConfig.enabled">
          <text class="cart-points-label">使用积分（最多 {{ maxPoints }}）</text>
          <input class="cart-points-input" type="number" v-model.number="pointsUsed" @input="onPointsInput" />
        </view>
        <view class="cart-row" v-if="discount > 0">
          <text class="cart-discount-label">积分抵扣</text>
          <text class="cart-discount">-¥{{ discount.toFixed(2) }}</text>
        </view>
        <view class="cart-row total">
          <text class="cart-row-label">应付</text>
          <text class="cart-payable">¥{{ payable.toFixed(2) }}</text>
        </view>
        <view class="modal-actions">
          <view class="modal-btn ghost" @click="closeCart">取消</view>
          <view class="modal-btn primary" @click="submitCart">提交订单</view>
        </view>
      </view>
    </view>

    <!-- 底部购物车条 -->
    <view class="cart-bar" v-if="cartCount > 0">
      <view class="cart-bar-left">
        <view class="cart-bar-badge"><text class="cart-bar-count">{{ cartCount }}</text></view>
        <text class="cart-bar-amt">¥{{ payable.toFixed(2) }}<text v-if="discount > 0" class="cart-bar-discount"> 省¥{{ discount.toFixed(2) }}</text></text>
      </view>
      <view class="cart-bar-btn" @click="openCart">去结算</view>
    </view>

    <!-- bottom spacer for tabbar -->
    <view class="tabbar-spacer"></view>
  </view>
</template>

<script>
import { getRoomTypeList, getHotRoomTypes } from '../../src/api/room'
import { getProductsWithConfig, submitProductOrder } from '../../src/api/product'
import { createServiceRequest, getMyServiceRequests } from '../../src/api/service'
import { getProfile } from '../../src/api/customer'
import { getMyOrders } from '../../src/api/order'

export default {
  data() {
    return {
      checkIn: '',
      checkOut: '',
      guests: 2,
      guestIdx: 1,
      guestRange: ['1', '2', '3', '4', '5', '6'],
      banners: [
        { badge: '精选推荐', title: '奢华体验', desc: '五星级服务，尊享非凡旅程', img: '/static/banner/banner-1.jpg', overlay: 'linear-gradient(135deg, rgba(249,115,22,.75) 0%, rgba(251,146,60,.45) 50%, transparent 100%)' },
        { badge: '限时优惠', title: '舒适客房', desc: '多样房型，满足商务与休闲', img: '/static/banner/banner-2.jpg', overlay: 'linear-gradient(135deg, rgba(217,119,6,.7) 0%, rgba(245,158,11,.4) 60%, transparent 100%)' },
        { badge: '度假优选', title: '泳池套房', desc: '阳光沙滩，尽享惬意时光', img: '/static/banner/banner-3.jpg', overlay: 'linear-gradient(135deg, rgba(16,185,129,.7) 0%, rgba(52,199,149,.4) 60%, transparent 100%)' },
        { badge: 'AI 助手', title: '智能服务', desc: 'AI 客服 24 小时在线', img: '/static/banner/banner-4.jpg', overlay: 'linear-gradient(135deg, rgba(59,130,246,.7) 0%, rgba(147,197,253,.4) 60%, transparent 100%)' }
      ],
      quickEntries: [
        { icon: '/static/quick/room.png', label: '浏览房型' },
        { icon: '/static/quick/ai.png', label: '智能客服' },
        { icon: '/static/quick/recommend.png', label: '智能推荐' },
        { icon: '/static/quick/order.png', label: '我的订单' }
      ],
      hotRooms: [],
      defaultImg: '/static/lose.png',
      // 服务功能
      serviceTypes: ['打扫', '送物', '维修', '其他'],
      products: [],
      pointsConfig: { enabled: false, ratio: 0.1 },
      customerPoints: 0,
      activeOrderId: null,
      myRequests: [],
      serviceShow: false,
      serviceType: '打扫',
      serviceRemark: '',
      cartShow: false,
      pointsUsed: 0
    }
  },
  computed: {
    todayStr() {
      const d = new Date()
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    minCheckOut() {
      if (!this.checkIn) return this.todayStr
      const d = new Date(this.checkIn)
      d.setDate(d.getDate() + 1)
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    // ===== 服务功能计算属性 =====
    cartItems() {
      return this.products.filter(p => p.qty > 0)
    },
    cartCount() {
      return this.cartItems.reduce((s, p) => s + p.qty, 0)
    },
    subtotal() {
      return this.cartItems.reduce((s, p) => s + (Number(p.price) || 0) * p.qty, 0)
    },
    maxPoints() {
      if (!this.pointsConfig.enabled || this.subtotal <= 0) return 0
      const byRatio = Math.floor(this.subtotal / this.pointsConfig.ratio)
      return Math.max(0, Math.min(this.customerPoints || 0, byRatio))
    },
    effectivePoints() {
      const v = Math.min(Number(this.pointsUsed) || 0, this.maxPoints)
      return Math.max(0, v)
    },
    discount() {
      if (!this.pointsConfig.enabled) return 0
      return Math.min(this.effectivePoints * this.pointsConfig.ratio, this.subtotal)
    },
    payable() {
      return Math.max(0, this.subtotal - this.discount)
    },
    maxDiscount() {
      return this.maxPoints * this.pointsConfig.ratio
    },
    ratioText() {
      const ratio = Number(this.pointsConfig.ratio) || 0
      if (ratio <= 0) return ''
      const perYuan = Math.round(1 / ratio)
      return '每 ' + perYuan + ' 积分抵 ¥1'
    }
  },
  onLoad() {
    // bind actions after data init
    this.quickEntries[0].action = this.goRoomList
    this.quickEntries[1].action = this.goChat
    this.quickEntries[2].action = this.goRecommend
    this.quickEntries[3].action = this.goOrders
    this.loadHotRooms()
    this.loadServiceData()
  },
  onShow() {
    const cIn = uni.getStorageSync('checkIn')
    const cOut = uni.getStorageSync('checkOut')
    if (cIn) this.checkIn = cIn
    if (cOut) this.checkOut = cOut
  },
  methods: {
    async loadHotRooms() {
      try {
        const res = await getHotRoomTypes()
        this.hotRooms = res.data || []
      } catch { /* ignore */ }
    },
    onCheckIn(e) {
      this.checkIn = e.detail.value
      uni.setStorageSync('checkIn', e.detail.value)
      if (this.checkOut && this.checkOut <= this.checkIn) {
        this.checkOut = ''
        uni.removeStorageSync('checkOut')
      }
    },
    onCheckOut(e) {
      this.checkOut = e.detail.value
      uni.setStorageSync('checkOut', e.detail.value)
    },
    onGuestChange(e) {
      this.guestIdx = e.detail.value
      this.guests = Number(this.guestRange[e.detail.value])
    },
    goRoomList() {
      uni.setStorageSync('checkIn', this.checkIn)
      uni.setStorageSync('checkOut', this.checkOut)
      uni.navigateTo({ url: '/pages/room/list' })
    },
    goRoomDetail(id) {
      uni.navigateTo({ url: '/pages/room/detail?id=' + id })
    },
    goChat() {
      uni.switchTab({ url: '/pages/chat/chat' })
    },
    goRecommend() {
      uni.navigateTo({ url: '/pages/recommend/recommend' })
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/list' })
    },
    // ===== 服务功能 =====
    async loadServiceData() {
      this.loadProducts()
      this.loadProfile()
      this.loadActiveOrder()
      this.loadMyRequests()
    },
    async loadProducts() {
      try {
        const res = await getProductsWithConfig()
        const data = res.data || {}
        const list = data.products || []
        this.products = (Array.isArray(list) ? list : []).map(p => ({ ...p, qty: 0 }))
        this.pointsConfig = {
          enabled: data.pointsDiscountEnabled === true || data.pointsDiscountEnabled === 'true',
          ratio: Number(data.pointsDiscountRatio) || 0.1
        }
      } catch { /* ignore */ }
    },
    async loadProfile() {
      try {
        const res = await getProfile()
        this.customerPoints = (res.data && res.data.points) || 0
      } catch { /* ignore */ }
    },
    async loadActiveOrder() {
      try {
        let res = await getMyOrders({ status: '已入住' })
        let records = (res.data && res.data.records) || []
        if (!records.length) {
          res = await getMyOrders({ status: '已支付' })
          records = (res.data && res.data.records) || []
        }
        if (records.length) this.activeOrderId = records[0].id
      } catch { /* ignore */ }
    },
    async loadMyRequests() {
      try {
        const res = await getMyServiceRequests()
        this.myRequests = (res.data || []).slice(0, 3)
      } catch { /* ignore */ }
    },
    serviceTypeIcon(t) {
      const map = { '打扫': '🧹', '送物': '📦', '维修': '🔧', '其他': '💬' }
      return map[t] || '🔔'
    },
    statusClass(s) {
      if (s === '待处理') return 'st-pending'
      if (s === '已处理') return 'st-done'
      return 'st-cancel'
    },
    openService(t) {
      this.serviceType = t
      this.serviceRemark = ''
      this.serviceShow = true
    },
    closeService() { this.serviceShow = false },
    async submitService() {
      if (!this.serviceType) return
      try {
        await createServiceRequest({ type: this.serviceType, remark: this.serviceRemark })
        uni.showToast({ title: '已呼叫，请稍候', icon: 'success' })
        this.serviceShow = false
        this.loadMyRequests()
      } catch { /* ignore */ }
    },
    changeQty(p, delta) {
      const next = (p.qty || 0) + delta
      if (next < 0) return
      if (p.stock != null && p.stock !== -1 && next > p.stock) {
        uni.showToast({ title: '库存不足', icon: 'none' })
        return
      }
      p.qty = next
      // 自动使用最大可用积分（比例由管理端 system_setting 配置）
      this.pointsUsed = this.pointsConfig.enabled ? this.maxPoints : 0
    },
    openCart() {
      if (!this.cartCount) return
      // 打开结算时自动使用最大可用积分
      this.pointsUsed = this.pointsConfig.enabled ? this.maxPoints : 0
      this.cartShow = true
    },
    closeCart() { this.cartShow = false },
    onPointsInput() {
      if (Number(this.pointsUsed) > this.maxPoints) this.pointsUsed = this.maxPoints
      if (Number(this.pointsUsed) < 0 || !this.pointsUsed) this.pointsUsed = 0
    },
    async submitCart() {
      if (!this.cartCount) return
      if (!this.activeOrderId) {
        uni.showToast({ title: '请先完成入住后再下单', icon: 'none' })
        return
      }
      const payload = {
        items: this.cartItems.map(it => ({ productId: it.id, quantity: it.qty })),
        pointsUsed: this.pointsConfig.enabled ? this.effectivePoints : 0,
        remark: ''
      }
      try {
        await submitProductOrder(this.activeOrderId, payload)
        uni.showToast({ title: '下单成功', icon: 'success' })
        this.cartShow = false
        this.products = this.products.map(p => ({ ...p, qty: 0 }))
        this.pointsUsed = 0
        this.loadProfile()
        this.loadMyRequests()
      } catch { /* ignore */ }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ====== Design Tokens ====== */
$bg: #F8F6F3;
$card-bg: #FFFFFF;
$text-primary: #1A1A2E;
$text-secondary: #6B7280;
$text-light: #9CA3AF;
$accent-warm: #F59E0B;
$accent-gold: #D97706;
$accent-orange: #EA580C;
$accent-pink: #EC4899;
$accent-blue: #3B82F6;
$accent-green: #10B981;
$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;
$radius-xl: 36rpx;

.home-page {
  background: $bg;
  min-height: 100vh;
}

/* ====== Banner ====== */
.banner-swiper {
  height: 360rpx;
  padding: 0 20rpx;
}

.banner-card {
  height: 100%;
  margin: 0 10rpx;
  border-radius: $radius-lg;
  position: relative;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0; left: 0;
}

.banner-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40rpx 36rpx;
}

.banner-badge {
  display: inline-block;
  background: rgba(255,255,255,.25);
  color: #FFF;
  font-size: 22rpx;
  font-weight: 600;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  margin-bottom: 16rpx;
}

.banner-title {
  font-size: 44rpx;
  font-weight: 800;
  color: #FFF;
  line-height: 1.3;
  display: block;
  margin-bottom: 10rpx;
  letter-spacing: 1rpx;
}

.banner-desc {
  font-size: 26rpx;
  color: rgba(255,255,255,.9);
  line-height: 1.5;
}

/* ====== Quick Entries ====== */
.quick-section {
  padding: 36rpx 24rpx;
  background: $card-bg;
}

.quick-row {
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
}

.quick-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.quick-icon-img { width: 64rpx; height: 64rpx; }

.quick-label {
  font-size: 25rpx;
  color: $text-secondary;
  font-weight: 500;
  margin-top: 12rpx;
}

/* ====== Section Block ====== */
.section-block {
  padding: 28rpx 0;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28rpx;
  margin-bottom: 20rpx;
}

.section-title-row {
  display: flex;
  align-items: center;
}

.section-icon { width: 30rpx; height: 30rpx; margin-right: 8rpx; }

.section-title {
  font-size: 34rpx;
  font-weight: 800;
  color: $text-primary;
}

.section-more {
  font-size: 26rpx;
  color: $accent-blue;
  font-weight: 600;
}

/* ====== Hot Rooms ====== */
.hot-scroll {
  white-space: nowrap;
  padding: 0 8rpx 8rpx 20rpx;
}

.hot-card {
  display: inline-flex;
  flex-direction: column;
  width: 300rpx;
  background: $card-bg;
  border-radius: $radius-lg;
  margin-right: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,.06);

  &:active { transform: scale(.98); }
}

.hot-img-wrap {
  position: relative;
  height: 220rpx;
  overflow: hidden;
}

.hot-img {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #E8E8E8, #D4D4D4);
}

.hot-img-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 80rpx;
  background: linear-gradient(to top, rgba(0,0,0,.15), transparent);
}

.hot-body {
  padding: 18rpx 20rpx 16rpx;
}

.hot-name {
  font-size: 29rpx;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 8rpx;
  display: block;
}

.hot-tags {
  display: flex;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.hot-tag {
  font-size: 20rpx;
  color: $text-secondary;
  background: #F3F0EB;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.hot-price-row {
  display: flex;
  align-items: baseline;
}

.hot-price {
  color: $accent-orange;
  font-weight: 800;
  font-size: 36rpx;

  &::before { content: '¥'; font-size: 24rpx; margin-right: 2rpx; font-weight: 700; }
}

.hot-price-unit {
  font-size: 22rpx;
  color: $text-light;
  margin-left: 4rpx;
}

.hot-book-btn {
  margin: 0 20rpx 16rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: #FFF;
  font-size: 25rpx;
  font-weight: 700;
  border-radius: $radius-md;
  letter-spacing: 2rpx;
}

/* ====== Tabbar Spacer ====== */
.tabbar-spacer {
  height: 120rpx;
}

/* ====== 服务功能 ====== */
.service-section {
  padding: 8rpx 0 20rpx;
}
.svc-card {
  background: $card-bg;
  border-radius: $radius-lg;
  margin: 0 24rpx 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,.05);
}
.svc-card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.svc-card-title {
  font-size: 31rpx;
  font-weight: 800;
  color: $text-primary;
}
.svc-card-sub {
  font-size: 22rpx;
  color: $text-light;
}
.svc-types {
  display: flex;
  gap: 16rpx;
}
.svc-type {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120rpx;
  background: #F7F4EF;
  border-radius: $radius-md;
  border: 2rpx solid transparent;

  &:active { transform: scale(.97); }
  &.active { border-color: $accent-orange; background: #FFF4E9; }
}
.svc-type-icon { font-size: 44rpx; line-height: 1; }
.svc-type-label { font-size: 25rpx; color: $text-secondary; font-weight: 600; margin-top: 10rpx; }
.svc-types-modal { margin: 8rpx 0 24rpx; }
.svc-history { margin-top: 20rpx; border-top: 1rpx solid #F0EDE8; padding-top: 16rpx; }
.svc-history-title { font-size: 24rpx; color: $text-light; font-weight: 600; }
.svc-req {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}
.svc-req-type { font-size: 26rpx; color: $text-primary; font-weight: 600; }
.svc-req-remark { font-size: 23rpx; color: $text-light; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.svc-req-status { font-size: 23rpx; font-weight: 700; margin-left: auto; }
.st-pending { color: $accent-gold; }
.st-done { color: $accent-green; }
.st-cancel { color: $text-light; }

/* 商品列表 */
.prod-list { display: flex; flex-direction: column; gap: 16rpx; }
.prod-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F0EDE8;

  &:last-child { border-bottom: none; }
}
.prod-img { width: 96rpx; height: 96rpx; border-radius: $radius-md; background: linear-gradient(135deg, #E8E8E8, #D4D4D4); flex-shrink: 0; }
.prod-info { flex: 1; min-width: 0; }
.prod-name { font-size: 28rpx; font-weight: 700; color: $text-primary; display: block; }
.prod-cat { font-size: 21rpx; color: $text-light; }
.prod-price-row { display: flex; align-items: baseline; margin-top: 6rpx; }
.prod-price { color: $accent-orange; font-weight: 800; font-size: 30rpx;
  &::before { content: '¥'; font-size: 21rpx; margin-right: 2rpx; font-weight: 700; }
}
.prod-unit { font-size: 21rpx; color: $text-light; margin-left: 4rpx; }
.prod-stepper { display: flex; align-items: center; gap: 12rpx; }
.step-btn {
  width: 48rpx; height: 48rpx;
  border-radius: 50%;
  background: #F3F0EB;
  color: $text-primary;
  font-size: 32rpx;
  line-height: 48rpx;
  text-align: center;
  font-weight: 700;
  &:active { transform: scale(.92); }
}
.step-num { font-size: 28rpx; color: $text-primary; min-width: 32rpx; text-align: center; font-weight: 600; }

.svc-empty { text-align: center; color: $text-light; font-size: 25rpx; padding: 20rpx 0; }

.points-bar {
  margin: 0 24rpx;
  padding: 16rpx 20rpx;
  background: #FFF7E6;
  border-radius: $radius-md;
}
.points-text { font-size: 24rpx; color: $accent-gold; font-weight: 600; }

/* ====== 弹窗 ====== */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.modal {
  width: 100%;
  background: #FFF;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: 36rpx 32rpx calc(36rpx + env(safe-area-inset-bottom));
}
.modal-title { font-size: 34rpx; font-weight: 800; color: $text-primary; display: block; margin-bottom: 24rpx; }
.modal-textarea {
  width: 100%;
  height: 160rpx;
  background: #F7F4EF;
  border-radius: $radius-md;
  padding: 20rpx;
  font-size: 27rpx;
  color: $text-primary;
  box-sizing: border-box;
}
.modal-actions { display: flex; gap: 20rpx; margin-top: 28rpx; }
.modal-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  font-size: 29rpx;
  font-weight: 700;

  &.primary { background: linear-gradient(135deg, #F97316, #EA580C); color: #FFF; }
  &.ghost { background: #F3F0EB; color: $text-secondary; }
}
.cart-lines { max-height: 360rpx; }
.cart-line {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #F0EDE8;
}
.cart-line-name { font-size: 27rpx; color: $text-primary; }
.cart-line-amt { font-size: 27rpx; color: $text-primary; font-weight: 600; }
.cart-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14rpx 0;

  &.total { border-top: 2rpx solid #F0EDE8; margin-top: 6rpx; padding-top: 20rpx; }
}
.cart-row-label { font-size: 27rpx; color: $text-secondary; }
.cart-row-val { font-size: 28rpx; color: $text-primary; font-weight: 600; }
.cart-discount-label { font-size: 27rpx; color: $accent-green; }
.cart-discount { font-size: 28rpx; color: $accent-green; font-weight: 700; }
.cart-payable { font-size: 38rpx; color: $accent-orange; font-weight: 800; }
.cart-points { display: flex; align-items: center; justify-content: space-between; padding: 14rpx 0; }
.cart-points-label { font-size: 26rpx; color: $text-secondary; }
.cart-points-input {
  width: 180rpx;
  height: 64rpx;
  background: #F7F4EF;
  border-radius: $radius-sm;
  text-align: center;
  font-size: 27rpx;
  color: $text-primary;
}

/* ====== 底部购物车条 ====== */
.cart-bar {
  position: fixed;
  left: 0; right: 0;
  bottom: calc(100rpx + env(safe-area-inset-bottom));
  z-index: 90;
  margin: 0 24rpx;
  height: 96rpx;
  background: rgba(26,26,46,.94);
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16rpx 0 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,.25);
}
.cart-bar-left { display: flex; align-items: center; gap: 16rpx; }
.cart-bar-badge {
  min-width: 40rpx; height: 40rpx;
  background: $accent-orange;
  border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center;
  padding: 0 10rpx;
}
.cart-bar-count { color: #FFF; font-size: 24rpx; font-weight: 700; }
.cart-bar-amt { color: #FFF; font-size: 30rpx; font-weight: 800; }
.cart-bar-discount { color: #FCD34D; font-size: 22rpx; font-weight: 600; margin-left: 4rpx; }
.cart-bar-btn {
  height: 72rpx;
  padding: 0 40rpx;
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: #FFF;
  font-size: 28rpx;
  font-weight: 700;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
