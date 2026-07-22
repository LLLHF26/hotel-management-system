<script>
export default {
  onLaunch() {
    console.log('App Launch')
    const token = uni.getStorageSync('token')
    if (!token) {
      const pages = getCurrentPages()
      if (pages.length === 0) return
      const current = pages[pages.length - 1]
      if (current && current.route) {
        const white = ['pages/login/login', 'pages/register/register', 'pages/index/index', 'pages/room/list', 'pages/room/detail']
        if (!white.includes(current.route)) { uni.reLaunch({ url: '/pages/login/login' }) }
      }
    }
  },
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
@import './uni.scss';

/* ====== Page Reset ====== */
page {
  background-color: #F8F6F3;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  color: #1A1A2E;
  font-size: 28rpx;
  line-height: 1.5;
}

view, text, image, scroll-view, input, button, picker { box-sizing: border-box; }

/* ====== Design System Tokens ====== */

/* Colors */
$bg:           #F8F6F3;          /* warm cream background */
$card-bg:      #FFFFFF;
$text-primary: #1A1A2E;          /* near black */
$text-secondary:#6B7280;         /* medium gray */
$text-light:   #9CA3AF;          /* light gray */

$accent-orange:#EA580C;          /* primary CTA orange */
$accent-gold:  #D97706;          /* secondary gold */
$accent-warm:  #F59E0B;          /* amber */
$accent-blue:  #3B82F6;
$accent-green: #10B981;
$accent-pink:  #EC4899;
$accent-purple:#8B5CF6;

$status-pending:  #F59E0B;
$status-paid:     #3B82F6;
$status-checked-in:#10B981;
$status-completed: #6B7280;
$status-cancelled: #EF4444;

/* Radius */
$r-xs: 8rpx;
$r-sm: 12rpx;
$r-md: 20rpx;
$r-lg: 28rpx;
$r-xl: 36rpx;

/* Shadows */
$shadow-sm:  0 2rpx 10rpx rgba(0,0,0,.03);
$shadow-md:  0 4rpx 16rpx rgba(0,0,0,.05);
$shadow-lg:  0 8rpx 30rpx rgba(0,0,0,.08);

/* ====== Common Utilities ====== */
.flex-row    { display: flex; flex-direction: row; align-items: center; }
.flex-col    { display: flex; flex-direction: column; }
.flex-between{ display: flex; align-items: center; justify-content: space-between; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.flex-1      { flex: 1; }

/* ====== Card Component ====== */
.card {
  background: $card-bg;
  border-radius: $r-md;
  padding: 24rpx;
  margin: 20rpx;
  box-shadow: $shadow-sm;
}

/* ====== Button Components ====== */
.btn-primary {
  background: linear-gradient(135deg, #F97316, $accent-orange);
  color: #FFFFFF;
  border: none;
  border-radius: $r-md;
  font-size: 32rpx;
  font-weight: 700;
  padding: 24rpx;
  text-align: center;
  letter-spacing: 2rpx;
  &:active { opacity: .88; transform: scale(.99); }
  &::after { border: none; }
}

.btn-outline {
  background: transparent;
  color: $accent-orange;
  border: 2rpx solid $accent-orange;
  border-radius: $r-md;
  font-size: 28rpx;
  padding: 18rpx 32rpx;
  text-align: center;
  &:active { background: rgba(234,88,12,.06); }
}

.btn-ghost {
  background: #F3F0EB;
  color: $text-secondary;
  border-radius: 28rpx;
  font-size: 26rpx;
  font-weight: 500;
  padding: 14rpx 28rpx;
  &:active { opacity: .75; }
}

/* ====== Price Display ====== */
.price {
  color: $accent-orange;
  font-weight: 800;
  font-size: 36rpx;
  &::before { content: '¥'; font-size: 26rpx; margin-right: 2rpx; font-weight: 700; }
}

/* ====== Tag / Label ====== */
.tag {
  display: inline-block;
  background: #F3F0EB;
  color: $text-secondary;
  border-radius: 8rpx;
  padding: 4rpx 14rpx;
  font-size: 22rpx;
  margin-right: 8rpx;
  margin-bottom: 8rpx;
}

/* ====== Status Tags ====== */
.status-tag {
  display: inline-block;
  border-radius: 20rpx;
  padding: 6rpx 18rpx;
  font-size: 22rpx;
  font-weight: 600;
  &.pending     { background: rgba(245,158,11,.12); color: $status-pending; }
  &.paid        { background: rgba(59,130,246,.12); color: $status-paid; }
  &.checked-in  { background: rgba(16,185,129,.12); color: $status-checked-in; }
  &.completed   { background: rgba(107,114,128,.15); color: $status-completed; }
  &.cancelled   { background: rgba(239,68,68,.10);  color: $status-cancelled; }
}

/* ====== Text Utilities ====== */
.line-clamp-1 { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.line-clamp-2 { overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

/* Safe area */
.safe-bottom { padding-bottom: constant(safe-area-inset-bottom); padding-bottom: env(safe-area-inset-bottom); }

/* Scrollbar hide (where supported) */
::-webkit-scrollbar { display: none; width: 0; height: 0; }
</style>
