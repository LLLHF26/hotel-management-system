<template>
  <view class="profile-page">
    <!-- user hero card -->
    <view class="hero-card">
      <view class="hero-bg">
        <view class="hero-circle c1"></view>
        <view class="hero-circle c2"></view>
        <view class="hero-circle c3"></view>
      </view>
      <image v-if="userInfo.avatar" class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
      <view v-else class="user-avatar-placeholder">
        <image class="avatar-img-icon" src="/static/icons/user.png" mode="aspectFit"></image>
      </view>
      <text class="user-name">{{ userInfo.realName || '未登录' }}</text>
      <text class="user-phone">{{ maskedPhone }}</text>
      <view class="badge-row" v-if="userInfo.memberLevel">
        <view class="badge-pill gold">
          <text class="b-text">{{ userInfo.memberLevel }}</text>
        </view>
        <view class="badge-pill green">
          <text class="b-text">{{ userInfo.points || 0 }} 积分</text>
        </view>
      </view>
    </view>

    <!-- menu rows -->
    <view class="menu-section">
      <view class="menu-row" @click="goEdit">
        <view class="mr-left">
          <view class="mr-icon-wrap amber">
            <image class="mr-icon" src="/static/icons/pencil.png" mode="aspectFit"></image>
          </view>
          <text class="mr-label">编辑资料</text>
        </view>
        <text class="mr-arrow">›</text>
      </view>

      <view class="menu-row" @click="goPassword">
        <view class="mr-left">
          <view class="mr-icon-wrap pink">
            <image class="mr-icon" src="/static/icons/lock.png" mode="aspectFit"></image>
          </view>
          <text class="mr-label">修改密码</text>
        </view>
        <text class="mr-arrow">›</text>
      </view>

      <view class="menu-row logout-row" @click="handleLogout">
        <view class="mr-left">
          <view class="mr-icon-wrap red">
            <image class="mr-icon" src="/static/icons/door.png" mode="aspectFit"></image>
          </view>
          <text class="mr-label logout-label">退出登录</text>
        </view>
        <text class="mr-arrow">›</text>
      </view>
    </view>

    <view class="bottom-pad"></view>
  </view>
</template>

<script>
import { getProfile } from '../../src/api/customer'
import { logout } from '../../src/api/auth'

export default {
  data() {
    return { userInfo: {} }
  },
  computed: {
    maskedPhone() {
      const phone = this.userInfo.phone || ''
      if (phone.length >= 11) return phone.substring(0, 3) + '****' + phone.substring(7)
      return phone
    }
  },
  onShow() { this.loadProfile() },
  methods: {
    async loadProfile() {
      try { this.userInfo = (await getProfile()).data || {} } catch { /* ignore */ }
    },
    goOrders() { uni.switchTab({ url: '/pages/order/list' }) },
    goChat() { uni.switchTab({ url: '/pages/chat/chat' }) },
    goRecommend() { uni.navigateTo({ url: '/pages/recommend/recommend' }) },
    goEdit() { uni.navigateTo({ url: '/pages/profile/edit' }) },
    goPassword() { uni.navigateTo({ url: '/pages/profile/password' }) },
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await logout() } catch {}
            uni.removeStorageSync('token')
            uni.removeStorageSync('memberLevel')
            uni.reLaunch({ url: '/pages/login/login' })
          }
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

$radius-xs: 8rpx;
$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;
$radius-xl: 36rpx;

.profile-page { background: $bg; min-height: 100vh; }

/* ====== Hero Card ====== */
.hero-card {
  display: flex; flex-direction: column; align-items: center;
  padding: 56rpx 32rpx 48rpx;
  background: linear-gradient(160deg, #F97316 0%, #FB923C 40%, #FCD34D 80%, #FEF3C7 100%);
  position: relative; overflow: hidden;
}

.hero-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
}
.hero-circle {
  position: absolute; border-radius: 50%;
  &.c1 { width: 360rpx; height: 360rpx; background: rgba(255,255,255,.08); top: -120rpx; right: -80rpx; }
  &.c2 { width: 200rpx; height: 200rpx; background: rgba(255,255,255,.06); bottom: -60rpx; left: -40rpx; }
  &.c3 { width: 140rpx; height: 140rpx; background: rgba(255,255,255,.05); top: 60rpx; left: 30rpx; border-radius: 50%; }
}

.user-avatar {
  width: 130rpx; height: 130rpx; border-radius: 50%;
  border: 5rpx solid rgba(255,255,255,.35);
  margin-bottom: 18rpx; position: relative; z-index: 1;
  background: rgba(255,255,255,.2);
}
.user-avatar-placeholder {
  width: 130rpx; height: 130rpx; border-radius: 50%;
  border: 5rpx solid rgba(255,255,255,.35);
  background: rgba(255,255,255,.2);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 18rpx; position: relative; z-index: 1;
}
.avatar-icon { font-size: 52rpx; }
.avatar-img-icon { width: 52rpx; height: 52rpx; }

.user-name {
  font-size: 38rpx; font-weight: 800; color: #FFF;
  position: relative; z-index: 1; margin-bottom: 6rpx;
}
.user-phone {
  font-size: 25rpx; color: rgba(255,255,255,.75);
  position: relative; z-index: 1; margin-bottom: 18rpx;
}

.badge-row { display: flex; gap: 12rpx; position: relative; z-index: 1; }

.badge-pill {
  display: flex; align-items: center; gap: 4rpx;
  padding: 8rpx 20rpx; border-radius: 24rpx;
  backdrop-filter: blur(8px);
  &.gold { background: rgba(255,255,255,.22); }
  &.green { background: rgba(16,185,129,.22); }
}
.b-text { font-size: 23rpx; color: #FFF; font-weight: 600; }

/* ====== Menu Section ====== */
.menu-section {
  padding: 20rpx 20rpx 0;
}

/* Row items */
.menu-row {
  display: flex; align-items: center; justify-content: space-between;
  background: $card-bg; margin-top: 16rpx;
  padding: 28rpx 24rpx; border-radius: $radius-md;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,.03);
  &:active { background: #FAFAFA; }
}

.mr-left { display: flex; align-items: center; gap: 18rpx; }

.mr-icon-wrap {
  width: 68rpx; height: 68rpx; border-radius: $radius-md;
  display: flex; align-items: center; justify-content: center;
  &.pink { background: linear-gradient(135deg, #FDF2F8, #FCE7F3); }
  &.red { background: linear-gradient(135deg, #FEF2F2, #FEE2E2); }
  &.amber { background: linear-gradient(135deg, #FFFBEB, #FEF3C7); }
}
.mr-icon { width: 34rpx; height: 34rpx; }
.mr-label { font-size: 29rpx; color: $text-primary; font-weight: 500; }
.logout-label { color: #EF4444; }
.mr-arrow { font-size: 34rpx; color: $text-light; }

.logout-row { border: 1rpx solid #FEE2E2; }

.bottom-pad { height: 48rpx; }
</style>
