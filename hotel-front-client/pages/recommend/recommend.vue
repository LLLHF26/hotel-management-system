<template>
  <view class="recommend-page">
    <!-- header -->
    <view class="page-hero">
      <image class="hero-art" src="/static/icons/target.png" mode="aspectFit"></image>
      <text class="hero-title">智能房型推荐</text>
      <text class="hero-desc">告诉我您的需求，AI 为您推荐最合适的房型</text>
    </view>

    <!-- form card -->
    <view class="form-card">
      <!-- dates -->
      <view class="form-row">
        <view class="field-half">
          <text class="field-label">入住日期</text>
          <picker mode="date" :value="form.checkIn" :start="todayStr" @change="onCheckIn">
            <text class="field-val">{{ form.checkIn || '请选择' }}</text>
          </picker>
        </view>
        <view class="field-arrow">
          <text>→</text>
        </view>
        <view class="field-half">
          <text class="field-label">退房日期</text>
          <picker mode="date" :value="form.checkOut" :start="minCheckOut" @change="onCheckOut">
            <text class="field-val">{{ form.checkOut || '请选择' }}</text>
          </picker>
        </view>
      </view>

      <!-- guests -->
      <view class="field-item">
        <text class="field-label">入住人数</text>
        <picker :range="guestOpts" :value="guestIdx" @change="onGuest">
          <view class="picker-wrap"><text>{{ form.guests }}人</text></view>
        </picker>
      </view>

      <!-- budget -->
      <view class="field-item">
        <text class="field-label">预算范围（元/晚）</text>
        <view class="budget-row">
          <input class="budget-in" v-model.number="form.budgetMin" type="digit" placeholder="最低" />
          <text class="budget-sep">—</text>
          <input class="budget-in" v-model.number="form.budgetMax" type="digit" placeholder="最高" />
        </view>
      </view>

      <!-- preference -->
      <view class="field-item">
        <text class="field-label">偏好要求</text>
        <input class="pref-input" v-model="form.prefer" placeholder="如：高层、安静、有景观（选填）" />
      </view>

      <!-- submit -->
      <button class="rec-btn" :loading="loading" @click="doRecommend">
        <text class="btn-text">开始推荐</text><image class="btn-icon" src="/static/icons/search.png" mode="aspectFit"></image>
      </button>
    </view>

    <!-- results -->
    <view v-if="results.length > 0" class="results-area">
      <view class="results-head">
        <text class="results-title">推荐结果</text>
        <text class="results-count">{{ results.length }} 个推荐</text>
      </view>

      <view class="rec-card" v-for="(r, idx) in results" :key="r.room_type_id || idx">
        <!-- rank -->
        <view class="rank-badge" :class="'rank-' + (idx + 1)">
          <text class="rank-num">{{ idx + 1 }}</text>
        </view>

        <view class="rc-body">
          <view class="rc-top">
            <text class="rc-name">{{ r.room_type_name }}</text>
            <view class="score-pill">
              <text class="score-val">{{ Math.round((r.score || 0) * 100) }}%</text>
              <text class="score-label">匹配度</text>
            </view>
          </view>

          <view class="rc-price-row">
            <text class="rc-price">¥{{ r.price }}</text>
            <text class="rc-unit">/晚</text>
            <text class="rc-avail" v-if="r.available_count !== undefined">· {{ r.available_count || 0 }}间可订</text>
          </view>

          <view class="rc-tags" v-if="r.area || r.bed_type || r.max_guests">
            <text class="rc-tag" v-if="r.area">{{ r.area }}㎡</text>
            <text class="rc-tag" v-if="r.bed_type">{{ r.bed_type }}</text>
            <text class="rc-tag" v-if="r.max_guests">{{ r.max_guests }}人</text>
          </view>

          <view class="rc-reason" v-if="r.reason">
            <image class="reason-icon" src="/static/icons/lightbulb.png" mode="aspectFit"></image>
            <text class="reason-text">{{ r.reason }}</text>
          </view>

          <view class="rc-actions">
            <view class="rc-btn ghost" @click="goDetail(r.room_type_id)">查看详情</view>
            <view class="rc-btn primary" @click="goBook(r)">立即预订</view>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-pad"></view>
  </view>
</template>

<script>
import { recommendRooms } from '../../src/api/recommend'

export default {
  data() {
    return {
      form: { checkIn: '', checkOut: '', guests: 2, budgetMin: '', budgetMax: '', prefer: '' },
      guestIdx: 1,
      guestOpts: ['1', '2', '3', '4', '5', '6'],
      results: [],
      loading: false
    }
  },
  computed: {
    todayStr() {
      const d = new Date()
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    minCheckOut() {
      if (!this.form.checkIn) return this.todayStr
      const d = new Date(this.form.checkIn)
      d.setDate(d.getDate() + 1)
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    }
  },
  onLoad() {
    const cIn = uni.getStorageSync('checkIn')
    const cOut = uni.getStorageSync('checkOut')
    if (cIn) this.form.checkIn = cIn
    if (cOut) this.form.checkOut = cOut
  },
  methods: {
    onCheckIn(e) {
      this.form.checkIn = e.detail.value
      if (this.form.checkOut && this.form.checkOut <= this.form.checkIn) this.form.checkOut = ''
    },
    onCheckOut(e) { this.form.checkOut = e.detail.value },
    onGuest(e) { this.guestIdx = e.detail.value; this.form.guests = Number(this.guestOpts[e.detail.value]) },
    async doRecommend() {
      if (!this.form.checkIn || !this.form.checkOut) { uni.showToast({ title: '请选择入住和退房日期', icon: 'none' }); return }
      this.loading = true
      try {
        const data = { check_in_date: this.form.checkIn, check_out_date: this.form.checkOut, guests: this.form.guests }
        if (this.form.budgetMin) data.budget_min = Number(this.form.budgetMin)
        if (this.form.budgetMax) data.budget_max = Number(this.form.budgetMax)
        if (this.form.prefer.trim()) data.prefer = this.form.prefer.trim()
        const res = await recommendRooms(data)
        this.results = (res.data && res.data.recommendations) ? res.data.recommendations : []
        if (this.results.length === 0) uni.showToast({ title: '暂无推荐结果', icon: 'none' })
      } catch {} finally { this.loading = false }
    },
    goDetail(id) { uni.navigateTo({ url: '/pages/room/detail?id=' + id }) },
    goBook(r) {
      const qs = Object.entries({
        roomTypeId: r.room_type_id,
        roomName: r.room_type_name || '',
        checkIn: this.form.checkIn,
        checkOut: this.form.checkOut,
        guests: r.max_guests || this.form.guests,
        price: r.price || 0
      }).map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v)).join('&')
      uni.navigateTo({ url: '/pages/order/create?' + qs })
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

.recommend-page { background: $bg; min-height: 100vh; }

/* ====== Hero ====== */
.page-hero {
  display: flex; flex-direction: column; align-items: center;
  padding: calc(32rpx + env(safe-area-inset-top)) 28rpx 24rpx;
  background: linear-gradient(160deg, #F97316 0%, #FB923C 50%, #FCD34D 100%);
}
.hero-art { width: 72rpx; height: 72rpx; margin-bottom: 12rpx; opacity: .4; }
.hero-title { font-size: 36rpx; font-weight: 800; color: #FFF; margin-bottom: 8rpx; }
.hero-desc { font-size: 26rpx; color: rgba(255,255,255,.8); text-align: center; }

/* ====== Form Card ====== */
.form-card {
  background: $card-bg; margin: -16rpx 20rpx 0;
  border-radius: $radius-md; padding: 28rpx 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,.08); position: relative; z-index: 1;
}

.form-row { display: flex; align-items: flex-end; gap: 10rpx; margin-bottom: 22rpx; }

.field-half {
  flex: 1; background: #FAFAFA; border-radius: $radius-sm; padding: 16rpx 18rpx;
}
.field-label { font-size: 23rpx; color: $text-light; display: block; margin-bottom: 6rpx; }
.field-val { font-size: 28rpx; color: $text-primary; font-weight: 600; }

.field-arrow {
  font-size: 28rpx; color: $text-light; padding-bottom: 18rpx; flex-shrink: 0;
}

.field-item { background: #FAFAFA; border-radius: $radius-sm; padding: 16rpx 18rpx; margin-bottom: 18rpx; }

.picker-wrap { font-size: 28rpx; color: $text-primary; font-weight: 600; }

.budget-row { display: flex; align-items: center; gap: 10rpx; }
.budget-in {
  flex: 1; height: 60rpx; background: #FFF; border-radius: 10rpx;
  font-size: 27rpx; color: $text-primary; text-align: center; padding: 0 12rpx;
}
.budget-sep { color: $text-light; font-size: 26rpx; }

.pref-input { width: 100%; font-size: 27rpx; color: $text-primary; margin-top: 4rpx; }

.rec-btn {
  width: 100%; height: 88rpx;
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: #FFF; font-size: 32rpx; font-weight: 700;
  border-radius: $radius-md; border: none; display: flex; align-items: center; justify-content: center; gap: 10rpx;
  &::after { border: none; }
  &:active { opacity: .9; }
}
.btn-text { letter-spacing: 3rpx; }
.btn-icon { width: 30rpx; height: 30rpx; }

/* ====== Results ====== */
.results-area { margin-top: 28rpx; padding: 0 20rpx; }

.results-head {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: 18rpx; padding: 0 4rpx;
}
.results-title { font-size: 33rpx; font-weight: 800; color: $text-primary; }
.results-count { font-size: 25rpx; color: $text-light; }

.rec-card {
  background: $card-bg; border-radius: $radius-md; margin-bottom: 18rpx;
  overflow: hidden; box-shadow: 0 2rpx 16rpx rgba(0,0,0,.04);
  display: flex; position: relative;

  &:active { transform: scale(.99); }
}

.rank-badge {
  width: 70rpx; height: auto; min-height: 70rpx;
  display: flex; align-items: center; justify-content: center;
  border-radius: $radius-md; flex-shrink: 0;
  &.rank-1 { background: linear-gradient(180deg, #FEF3C7, #FDE68A); }
  &.rank-2 { background: linear-gradient(180deg, #E5E7EB, #D1D5DB); }
  &.rank-3 { background: linear-gradient(180deg, #FEE2E2, #FECACA); }
}
.rank-num { font-size: 28rpx; font-weight: 800; }
.rank-1 .rank-num { color: #B45309; }
.rank-2 .rank-num { color: #4B5563; }
.rank-3 .rank-num { color: #DC2626; }

.rc-body { flex: 1; padding: 22rpx 20rpx 20rpx; }

.rc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.rc-name { font-size: 31rpx; font-weight: 700; color: $text-primary; flex: 1; }

.score-pill {
  display: flex; align-items: center; gap: 4rpx; background: #ECFDF5;
  padding: 6rpx 14rpx; border-radius: 16rpx; flex-shrink: 0;
}
.score-val { font-size: 24rpx; color: #059669; font-weight: 700; }
.score-label { font-size: 20rpx; color: #059669; }

.rc-price-row { display: flex; align-items: baseline; margin-bottom: 10rpx; }
.rc-price { font-size: 34rpx; color: $accent-orange; font-weight: 800; &::before { content: '¥'; font-size: 24rpx; } }
.rc-unit { font-size: 22rpx; color: $text-light; margin-left: 2rpx; }
.rc-avail { font-size: 22rpx; color: $text-light; margin-left: 10rpx; }

.rc-tags { display: flex; gap: 8rpx; margin-bottom: 12rpx; flex-wrap: wrap; }
.rc-tag {
  font-size: 21rpx; color: $text-secondary; background: #F3F0EB;
  padding: 4rpx 14rpx; border-radius: 8rpx;
}

.rc-reason { display: flex; align-items: flex-start; gap: 8rpx; margin-bottom: 14rpx; }
.reason-icon { width: 24rpx; height: 24rpx; flex-shrink: 0; margin-top: 2rpx; }
.reason-text { font-size: 25rpx; color: $text-secondary; line-height: 1.6; }

.rc-actions { display: flex; gap: 12rpx; }
.rc-btn {
  flex: 1; height: 64rpx; display: flex; align-items: center; justify-content: center;
  border-radius: 28rpx; font-size: 26rpx; font-weight: 600;
  &.primary { background: linear-gradient(135deg, #F97316, #EA580C); color: #FFF; }
  &.ghost { background: #F3F0EB; color: $text-secondary; }
}

.bottom-pad { height: 48rpx; }
</style>
