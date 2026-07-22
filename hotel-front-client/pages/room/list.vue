<template>
  <view class="room-list-page">
    <!-- search -->
    <view class="search-area">
      <view class="search-pill" @click="$refs.searchInput && $refs.searchInput.focus()">
        <image class="search-icon" src="/static/icons/search.png" mode="aspectFit"></image>
        <input
          ref="searchInput"
          class="search-input"
          v-model="keyword"
          placeholder="搜索房型名称..."
          placeholder-style="color:#9CA3AF"
          confirm-type="search"
          @confirm="onSearch"
        />
      </view>
      <view class="search-go" @click="onSearch">
        <text class="search-go-text">搜索</text>
      </view>
    </view>

    <!-- filter chips -->
    <scroll-view scroll-x class="filter-scroll" :show-scrollbar="false">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-chip"
        :class="{ active: currentFilter === tab.value }"
        @click="switchFilter(tab.value)"
      >
        <text class="filter-dot" v-if="currentFilter === tab.value"></text>
        {{ tab.label }}
      </view>
    </scroll-view>

    <!-- room list -->
    <view class="room-list" v-if="rooms.length > 0">
      <view class="room-card" v-for="(room, idx) in rooms" :key="room.id" @click="goDetail(room.id)">
        <!-- left status dot -->
        <view class="status-dot" :style="{ background: statusColors[idx % statusColors.length] }"></view>
        <!-- content -->
        <view class="room-main">
          <view class="room-top">
            <image class="room-cover" :src="room.coverImage || defaultImg" mode="aspectFill"></image>
            <view class="room-info">
              <text class="room-title line-clamp-1">{{ room.name }}</text>
              <text class="room-desc line-clamp-2">{{ room.description || '暂无描述' }}</text>
              <view class="room-meta-row">
                <view class="meta-item" v-if="room.area"><image class="meta-icon-img" src="/static/icons/area.png" mode="aspectFit"></image><text>{{ room.area }}㎡</text></view>
                <view class="meta-item" v-if="room.bedType"><image class="meta-icon-img" src="/static/quick/room.png" mode="aspectFit"></image><text>{{ room.bedType }}</text></view>
                <view class="meta-item" v-if="room.maxGuests"><image class="meta-icon-img" src="/static/icons/people.png" mode="aspectFit"></image><text>{{ room.maxGuests }}人</text></view>
              </view>
            </view>
          </view>
          <view class="room-bottom">
            <view class="room-tags" v-if="room.amenities">
              <text class="tag-pill" v-for="a in splitAmenities(room.amenities)" :key="a">{{ a }}</text>
            </view>
            <view class="price-block">
              <text class="price-val">{{ room.price }}</text>
              <text class="price-suffix">/晚</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!loading && rooms.length === 0" class="empty-state">
      <view class="empty-circle">
        <image class="empty-emoji" src="/static/icons/hotel.png" mode="aspectFit"></image>
      </view>
      <text class="empty-title">暂无房型</text>
      <text class="empty-hint">试试调整筛选条件或关键词</text>
    </view>

    <view v-if="loading" class="loading-wrap">
      <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot delay-1"></view>
        <view class="dot delay-2"></view>
      </view>
      <text class="loading-text">加载中...</text>
    </view>

    <view v-if="hasMore && rooms.length > 0" class="load-more" @click="loadMore">
      <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
    </view>

    <view class="bottom-spacer"></view>
  </view>
</template>

<script>
import { getRoomTypeList } from '../../src/api/room'

export default {
  data() {
    return {
      keyword: '',
      currentFilter: 'all',
      filterTabs: [
        { label: '全部', value: 'all' },
        { label: '大床', value: '大床' },
        { label: '双床', value: '双床' },
        { label: '套房', value: '套房' }
      ],
      statusColors: ['#10B981', '#F59E0B', '#3B82F6', '#EC4899', '#8B5CF6'],
      rooms: [],
      page: 1,
      size: 10,
      hasMore: true,
      loading: false,
      loadingMore: false,
      defaultImg: '/static/lose.png'
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    splitAmenities(val) {
      if (!val) return []
      return typeof val === 'string' ? val.split(/[,，、]/).slice(0, 3) : val.slice(0, 3)
    },
    async loadData(reset = true) {
      if (reset) {
        this.page = 1
        this.loading = true
      } else {
        this.loadingMore = true
      }
      try {
        const params = { page: this.page, size: this.size }
        if (this.keyword.trim()) params.keyword = this.keyword.trim()
        if (this.currentFilter !== 'all') params.bedType = this.currentFilter
        const res = await getRoomTypeList(params)
        const records = (res.data && res.data.records) ? res.data.records : []
        if (reset) {
          this.rooms = records
        } else {
          this.rooms = this.rooms.concat(records)
        }
        this.hasMore = records.length >= this.size
      } catch {
        // ignore
      } finally {
        this.loading = false
        this.loadingMore = false
      }
    },
    onSearch() {
      this.loadData(true)
    },
    switchFilter(val) {
      if (this.currentFilter === val) return
      this.currentFilter = val
      this.loadData(true)
    },
    loadMore() {
      if (this.loadingMore || !this.hasMore) return
      this.page++
      this.loadData(false)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/room/detail?id=' + id })
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
$accent-orange: #EA580C;
$accent-gold: #D97706;
$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;

.room-list-page {
  background: $bg;
  min-height: 100vh;
}

/* ====== Search ====== */
.search-area {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-top: calc(16rpx + env(safe-area-inset-top));
  background: $card-bg;
}

.search-pill {
  flex: 1;
  display: flex;
  align-items: center;
  background: #F3F0EB;
  border-radius: 36rpx;
  padding: 14rpx 24rpx;
}

.search-icon { width: 28rpx; height: 28rpx; margin-right: 12rpx; }

.search-input {
  flex: 1;
  font-size: 27rpx;
  color: $text-primary;
}

.search-go {
  margin-left: 16rpx;
  background: linear-gradient(135deg, #F59E0B, #EA580C);
  border-radius: 28rpx;
  padding: 12rpx 28rpx;
}

.search-go-text {
  color: #FFF;
  font-size: 26rpx;
  font-weight: 600;
}

/* ====== Filter Chips ====== */
.filter-scroll {
  white-space: nowrap;
  background: $card-bg;
  padding: 8rpx 0 18rpx 20rpx;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 28rpx;
  background: #F3F0EB;
  border-radius: 28rpx;
  font-size: 26rpx;
  color: $text-secondary;
  margin-right: 16rpx;
  font-weight: 500;
  transition: all .2s;

  &.active {
    background: linear-gradient(135deg, #FEF3C7, #FDE68A);
    color: $accent-gold;
    font-weight: 700;
  }
}

.filter-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: $accent-gold;
}

/* ====== Room Card List ====== */
.room-list {
  padding: 20rpx 20rpx 0;
}

.room-card {
  display: flex;
  background: $card-bg;
  border-radius: $radius-md;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, .04);

  &:active { transform: scale(.99); }
}

.status-dot {
  width: 8rpx;
  flex-shrink: 0;
  border-radius: 4rpx;
}

.room-main {
  flex: 1;
  padding: 22rpx 20rpx 20rpx 16rpx;
}

.room-top {
  display: flex;
  gap: 18rpx;
  margin-bottom: 16rpx;
}

.room-cover {
  width: 200rpx;
  height: 160rpx;
  border-radius: $radius-sm;
  background: linear-gradient(135deg, #E8E8E8, #D4D4D4);
  flex-shrink: 0;
}

.room-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.room-title {
  font-size: 31rpx;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.room-desc {
  font-size: 23rpx;
  color: $text-light;
  line-height: 1.5;
  margin-bottom: 8rpx;
}

.room-meta-row {
  display: flex;
  gap: 14rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: flex; align-items: center; gap: 4rpx;
  font-size: 22rpx;
  color: $text-secondary;
}
.meta-icon-img { width: 22rpx; height: 22rpx; }

.room-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.room-tags {
  flex: 1;
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
  margin-right: 16rpx;
}

.tag-pill {
  font-size: 21rpx;
  color: $text-secondary;
  background: #F3F0EB;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}

.price-block {
  flex-shrink: 0;
  display: flex;
  align-items: baseline;
}

.price-val {
  color: $accent-orange;
  font-weight: 800;
  font-size: 36rpx;

  &::before { content: '¥'; font-size: 24rpx; margin-right: 2rpx; font-weight: 700; }
}

.price-suffix {
  font-size: 22rpx;
  color: $text-light;
  margin-left: 4rpx;
}

/* ====== Empty State ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-circle {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.empty-emoji { width: 60rpx; height: 60rpx; }

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 8rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: $text-light;
}

/* ====== Loading ====== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.loading-dots {
  display: flex;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $accent-gold;
  animation: bounce 1s infinite;

  &.delay-1 { animation-delay: .15s; }
  &.delay-2 { animation-delay: .3s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(.6); opacity: .4; }
  40% { transform: scale(1); opacity: 1; }
}

.loading-text { font-size: 26rpx; color: $text-light; }

.load-more {
  text-align: center;
  padding: 28rpx;
}

.load-more text {
  color: $accent-gold;
  font-size: 26rpx;
  font-weight: 600;
}

.bottom-spacer { height: 40rpx; }
</style>
