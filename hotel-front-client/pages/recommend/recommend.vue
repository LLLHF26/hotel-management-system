<template>
  <view class="recommend-page">
    <view class="page-intro">
      <text class="intro-title">🎯 智能房型推荐</text>
      <text class="intro-desc">告诉我您的需求，AI为您推荐最合适的房型</text>
    </view>

    <!-- form -->
    <view class="form-card">
      <view class="form-row">
        <view class="form-half">
          <text class="form-label">入住日期</text>
          <picker mode="date" :value="form.checkIn" :start="todayStr" @change="onCheckIn">
            <text class="form-val">{{ form.checkIn || '请选择' }}</text>
          </picker>
        </view>
        <view class="form-half">
          <text class="form-label">退房日期</text>
          <picker mode="date" :value="form.checkOut" :start="minCheckOut" @change="onCheckOut">
            <text class="form-val">{{ form.checkOut || '请选择' }}</text>
          </picker>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">入住人数</text>
        <picker :range="guestOpts" :value="guestIdx" @change="onGuest">
          <view class="picker-val">{{ form.guests }}人</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">预算范围</text>
        <view class="budget-row">
          <input class="budget-input" v-model.number="form.budgetMin" type="digit" placeholder="最低" />
          <text class="budget-sep">~</text>
          <input class="budget-input" v-model.number="form.budgetMax" type="digit" placeholder="最高" />
          <text class="budget-unit">/晚</text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">偏好要求</text>
        <input class="form-input" v-model="form.prefer" placeholder="如：高层、安静、有景观（选填）" />
      </view>

      <button class="rec-btn" :loading="loading" @click="doRecommend">开始推荐 🔍</button>
    </view>

    <!-- results -->
    <view v-if="results.length > 0" class="results-section">
      <text class="results-title">推荐结果</text>
      <view class="rec-card" v-for="(r, idx) in results" :key="r.room_type_id || idx">
        <view class="rec-header">
          <view class="rec-rank">
            <text v-if="idx === 0">🥇</text>
            <text v-else-if="idx === 1">🥈</text>
            <text v-else-if="idx === 2">🥉</text>
            <text v-else>{{ idx + 1 }}</text>
          </view>
          <text class="rec-name">{{ r.room_type_name }}</text>
          <text class="rec-score">匹配度 {{ Math.round((r.score || 0) * 100) }}%</text>
        </view>
        <view class="rec-body">
          <text class="rec-price">¥{{ r.price }} / 晚</text>
          <view class="rec-tags">
            <text class="tag" v-if="r.area">{{ r.area }}㎡</text>
            <text class="tag" v-if="r.bed_type">{{ r.bed_type }}</text>
            <text class="tag" v-if="r.max_guests">{{ r.max_guests }}人</text>
          </view>
          <view class="rec-reason">
            <text class="reason-icon">💡</text>
            <text class="reason-text">{{ r.reason }}</text>
          </view>
          <text class="rec-avail">目前{{ r.available_count || 0 }}间可订</text>
        </view>
        <view class="rec-actions">
          <view class="rec-action outline" @click="goDetail(r.room_type_id)">查看详情</view>
          <view class="rec-action primary" @click="goBook(r)">立即预订</view>
        </view>
      </view>
    </view>
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
      if (this.form.checkOut && this.form.checkOut <= this.form.checkIn) {
        this.form.checkOut = ''
      }
    },
    onCheckOut(e) {
      this.form.checkOut = e.detail.value
    },
    onGuest(e) {
      this.guestIdx = e.detail.value
      this.form.guests = Number(this.guestOpts[e.detail.value])
    },
    async doRecommend() {
      if (!this.form.checkIn || !this.form.checkOut) {
        uni.showToast({ title: '请选择入住和退房日期', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const data = {
          check_in_date: this.form.checkIn,
          check_out_date: this.form.checkOut,
          guests: this.form.guests
        }
        if (this.form.budgetMin) data.budget_min = Number(this.form.budgetMin)
        if (this.form.budgetMax) data.budget_max = Number(this.form.budgetMax)
        if (this.form.prefer.trim()) data.prefer = this.form.prefer.trim()

        const res = await recommendRooms(data)
        this.results = (res.data && res.data.recommendations) ? res.data.recommendations : []
        if (this.results.length === 0) {
          uni.showToast({ title: '暂无推荐结果', icon: 'none' })
        }
      } catch { /* ignore */ }
      finally { this.loading = false }
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/room/detail?id=' + id })
    },
    goBook(r) {
      const params = new URLSearchParams({
        roomTypeId: r.room_type_id,
        checkIn: this.form.checkIn,
        checkOut: this.form.checkOut,
        guests: r.max_guests || this.form.guests,
        price: r.price || 0
      }).toString()
      uni.navigateTo({ url: '/pages/order/create?' + params })
    }
  }
}
</script>

<style lang="scss" scoped>
.recommend-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 48rpx;
}

.page-intro {
  padding: 32rpx 28rpx 8rpx;
}

.intro-title { font-size: 34rpx; font-weight: 700; color: #2C3E50; display: block; margin-bottom: 8rpx; }
.intro-desc { font-size: 26rpx; color: #999; }

.form-card {
  background: #FFF;
  margin: 16rpx 20rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04);
}

.form-row { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
.form-half {
  flex: 1;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 20rpx;
}

.form-item { background: #F8F9FA; border-radius: 12rpx; padding: 20rpx; margin-bottom: 20rpx; }

.form-label { font-size: 24rpx; color: #999; display: block; margin-bottom: 6rpx; }
.form-val { font-size: 28rpx; color: #333; font-weight: 500; }
.form-input { font-size: 28rpx; color: #333; }
.picker-val { font-size: 28rpx; color: #333; font-weight: 500; }

.budget-row { display: flex; align-items: center; }
.budget-input { flex: 1; font-size: 28rpx; color: #333; background: #FFF; border-radius: 8rpx; padding: 8rpx 12rpx; text-align: center; }
.budget-sep { margin: 0 12rpx; color: #999; }
.budget-unit { margin-left: 8rpx; color: #999; font-size: 24rpx; }

.rec-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 14rpx;
  border: none;
  &::after { border: none; }
}

.results-section { margin-top: 24rpx; }

.results-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2C3E50;
  padding: 0 32rpx;
  margin-bottom: 16rpx;
  display: block;
}

.rec-card {
  background: #FFF;
  margin: 0 20rpx 16rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04);
}

.rec-header {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.rec-rank { font-size: 40rpx; margin-right: 12rpx; }
.rec-name { flex: 1; font-size: 30rpx; font-weight: 600; color: #333; }
.rec-score { font-size: 26rpx; color: #27AE60; font-weight: 600; }

.rec-body { padding-bottom: 16rpx; border-bottom: 1rpx solid #F0F0F0; }

.rec-price { font-size: 34rpx; color: #C9A96E; font-weight: 700; display: block; margin-bottom: 10rpx; }
.rec-tags { margin-bottom: 12rpx; }

.rec-reason { display: flex; margin-bottom: 10rpx; }
.reason-icon { margin-right: 8rpx; }
.reason-text { font-size: 26rpx; color: #888; line-height: 1.6; flex: 1; }

.rec-avail { font-size: 24rpx; color: #27AE60; }

.rec-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }

.rec-action {
  flex: 1;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28rpx;
  font-size: 26rpx;
  font-weight: 500;

  &.primary { background: linear-gradient(135deg, #C9A96E, #D4B87A); color: #FFF; }
  &.outline { background: #F5F6FA; color: #666; }
}
</style>
