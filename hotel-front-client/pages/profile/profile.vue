<template>
  <view class="profile-page">
    <!-- user card -->
    <view class="user-card">
      <image v-if="userInfo.avatar" class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
      <view v-else class="user-avatar-placeholder">👤</view>
      <text class="user-name">{{ userInfo.realName || '未登录' }}</text>
      <text class="user-phone">{{ maskedPhone }}</text>
      <view class="user-badges" v-if="userInfo.memberLevel">
        <text class="badge">⭐ {{ userInfo.memberLevel || '普通会员' }}</text>
        <text class="badge">💎 积分 {{ userInfo.points || 0 }}</text>
      </view>
    </view>

    <!-- menu -->
    <view class="menu-card">
      <view class="menu-item" @click="goOrders">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goChat">
        <text class="menu-icon">🤖</text>
        <text class="menu-text">智能客服</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goRecommend">
        <text class="menu-icon">🎯</text>
        <text class="menu-text">智能推荐</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goEdit">
        <text class="menu-icon">✏️</text>
        <text class="menu-text">编辑资料</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPassword">
        <text class="menu-icon">🔒</text>
        <text class="menu-text">修改密码</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="menu-card">
      <view class="menu-item logout" @click="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getProfile } from '../../src/api/customer'
import { logout } from '../../src/api/auth'

export default {
  data() {
    return {
      userInfo: {}
    }
  },
  computed: {
    maskedPhone() {
      const phone = this.userInfo.phone || ''
      if (phone.length >= 11) {
        return phone.substring(0, 3) + '****' + phone.substring(7)
      }
      return phone
    }
  },
  onShow() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const res = await getProfile()
        this.userInfo = res.data || {}
      } catch { /* handle if not logged in */ }
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/list' })
    },
    goChat() {
      uni.switchTab({ url: '/pages/chat/chat' })
    },
    goRecommend() {
      uni.navigateTo({ url: '/pages/recommend/recommend' })
    },
    goEdit() {
      uni.navigateTo({ url: '/pages/profile/edit' })
    },
    goPassword() {
      uni.navigateTo({ url: '/pages/profile/password' })
    },
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await logout()
            } catch { /* ignore */ }
            uni.removeStorageSync('token')
            uni.removeStorageSync('customerId')
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
.profile-page {
  background: #F5F6FA;
  min-height: 100vh;
}

.user-card {
  background: linear-gradient(160deg, #2C3E50, #3D5166);
  padding: 60rpx 32rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-bottom: 16rpx;
  border: 4rpx solid rgba(255,255,255,.2);
  background: rgba(255,255,255,.15);
}

.user-avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255,255,255,.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  margin-bottom: 16rpx;
  border: 4rpx solid rgba(255,255,255,.2);
}

.user-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #FFF;
  margin-bottom: 6rpx;
}

.user-phone {
  font-size: 26rpx;
  color: rgba(255,255,255,.7);
  margin-bottom: 16rpx;
}

.user-badges {
  display: flex;
  gap: 16rpx;
}

.badge {
  background: rgba(255,255,255,.15);
  color: #FFF;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.menu-card {
  background: #FFF;
  margin: 16rpx 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,.03);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #F5F5F5;

  &:last-child { border-bottom: none; }
  &:active { background: #F8F9FA; }

  &.logout {
    .menu-text { color: #E74C3C; }
  }
}

.menu-icon { font-size: 36rpx; margin-right: 20rpx; }
.menu-text { flex: 1; font-size: 30rpx; color: #333; }
.menu-arrow { font-size: 32rpx; color: #CCC; }
</style>
