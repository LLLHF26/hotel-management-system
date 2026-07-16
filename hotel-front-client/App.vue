<script>
export default {
  onLaunch() {
    console.log('App Launch')
    const token = uni.getStorageSync('token')
    if (!token) {
      const pages = getCurrentPages()
      if (pages.length === 0) {
        return
      }
      const current = pages[pages.length - 1]
      if (current && current.route) {
        const white = ['pages/login/login', 'pages/register/register', 'pages/index/index', 'pages/room/list', 'pages/room/detail']
        if (!white.includes(current.route)) {
          uni.reLaunch({ url: '/pages/login/login' })
        }
      }
    }
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import './uni.scss';

page {
  background-color: #F5F6FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #333333;
  font-size: 28rpx;
}

view, text, image, scroll-view, input, button {
  box-sizing: border-box;
}

/* ---- color tokens ---- */
$primary: #C9A96E;
$primary-dark: #B8953F;
$dark: #2C3E50;
$danger: #E74C3C;
$success: #27AE60;
$info: #3498DB;
$warning: #F39C12;
$text: #333333;
$text-light: #666666;
$text-muted: #999999;
$border: #EEEEEE;
$bg: #F5F6FA;
$white: #FFFFFF;

/* ---- common classes ---- */
.flex-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.flex-col {
  display: flex;
  flex-direction: column;
}
.flex-between {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.card {
  background: $white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.btn-primary {
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFFFFF;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  padding: 24rpx;
  text-align: center;
  letter-spacing: 2rpx;

  &:active {
    opacity: 0.85;
  }
}

.btn-outline {
  background: transparent;
  color: $primary;
  border: 2rpx solid $primary;
  border-radius: 12rpx;
  font-size: 28rpx;
  padding: 18rpx 32rpx;
  text-align: center;

  &:active {
    background: rgba(201, 169, 110, 0.06);
  }
}

.price {
  color: $primary-dark;
  font-weight: 700;
  font-size: 36rpx;

  &::before {
    content: '¥';
    font-size: 26rpx;
  }
}

.tag {
  display: inline-block;
  background: rgba(201, 169, 110, 0.12);
  color: $primary-dark;
  border-radius: 6rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  margin-right: 8rpx;
  margin-bottom: 8rpx;
}

.status-tag {
  display: inline-block;
  border-radius: 20rpx;
  padding: 6rpx 18rpx;
  font-size: 22rpx;
  font-weight: 500;

  &.pending { background: rgba(243, 156, 18, 0.12); color: #F39C12; }
  &.paid { background: rgba(52, 152, 219, 0.12); color: #3498DB; }
  &.checked-in { background: rgba(39, 174, 96, 0.12); color: #27AE60; }
  &.completed { background: rgba(149, 165, 166, 0.15); color: #7F8C8D; }
  &.cancelled { background: rgba(231, 76, 60, 0.10); color: #E74C3C; }
}

.line-clamp-1 { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.line-clamp-2 { overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

.safe-bottom { padding-bottom: constant(safe-area-inset-bottom); padding-bottom: env(safe-area-inset-bottom); }
</style>
