<template>
  <view class="room-list-page">
    <!-- search -->
    <view class="search-box">
      <view class="search-input-row">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索房型名称..."
          placeholder-style="color:#B0B0B0"
          @confirm="onSearch"
        />
        <text class="search-btn" @click="onSearch">搜索</text>
      </view>
    </view>

    <!-- filter tabs -->
    <scroll-view scroll-x class="filter-tabs" :show-scrollbar="false">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentFilter === tab.value }"
        @click="switchFilter(tab.value)"
      >{{ tab.label }}</view>
    </scroll-view>

    <!-- room list -->
    <view class="room-grid" v-if="rooms.length > 0">
      <view class="room-cell" v-for="room in rooms" :key="room.id" @click="goDetail(room.id)">
        <image class="room-cover" :src="room.coverImage || defaultImg" mode="aspectFill"></image>
        <view class="room-body">
          <text class="room-title line-clamp-1">{{ room.name }}</text>
          <view class="room-meta">
            <text v-if="room.area">📐 {{ room.area }}㎡</text>
            <text v-if="room.bedType">🛏 {{ room.bedType }}</text>
            <text v-if="room.maxGuests">👥 {{ room.maxGuests }}人</text>
          </view>
          <view class="room-amenities" v-if="room.amenities">
            <text class="tag" v-for="a in splitAmenities(room.amenities)" :key="a">{{ a }}</text>
          </view>
          <view class="room-bottom">
            <text class="price">{{ room.price }}</text>
            <text class="price-label">/晚</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!loading && rooms.length === 0" class="empty-state">
      <text class="empty-icon">🏨</text>
      <text class="empty-text">暂无房型</text>
    </view>

    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>

    <view v-if="hasMore && rooms.length > 0" class="load-more" @click="loadMore">
      <text>{{ loadingMore ? '加载中...' : '加载更多' }}</text>
    </view>
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
      return typeof val === 'string' ? val.split(/[,，、]/).slice(0, 4) : val.slice(0, 4)
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
.room-list-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 24rpx;
}

.search-box {
  background: #FFFFFF;
  padding: 16rpx 28rpx;
}

.search-input-row {
  display: flex;
  align-items: center;
  background: #F5F6FA;
  border-radius: 36rpx;
  padding: 0 24rpx;
  height: 72rpx;
}

.search-icon {
  font-size: 30rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.search-btn {
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  font-size: 26rpx;
  padding: 12rpx 28rpx;
  border-radius: 28rpx;
  flex-shrink: 0;

  &:active { opacity: 0.8; }
}

.filter-tabs {
  white-space: nowrap;
  background: #FFFFFF;
  padding: 0 28rpx 16rpx;
}

.filter-tab {
  display: inline-block;
  padding: 12rpx 28rpx;
  background: #F5F6FA;
  border-radius: 28rpx;
  font-size: 26rpx;
  color: #666666;
  margin-right: 16rpx;

  &.active {
    background: rgba(201, 169, 110, 0.12);
    color: #C9A96E;
    font-weight: 600;
  }
}

.room-grid {
  padding: 20rpx;
}

.room-cell {
  background: #FFFFFF;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  display: flex;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.room-cover {
  width: 260rpx;
  height: 200rpx;
  background: #E8E8E8;
  flex-shrink: 0;
}

.room-body {
  flex: 1;
  padding: 16rpx 20rpx;
  display: flex;
  flex-direction: column;
}

.room-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8rpx;
}

.room-meta {
  font-size: 24rpx;
  color: #999999;
  display: flex;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.room-amenities {
  margin-bottom: 8rpx;
}

.room-bottom {
  margin-top: auto;
  display: flex;
  align-items: baseline;
}

.price {
  color: #C9A96E;
  font-weight: 700;
  font-size: 34rpx;

  &::before { content: '¥'; font-size: 24rpx; margin-right: 2rpx; }
}

.price-label {
  font-size: 22rpx;
  color: #999999;
  margin-left: 4rpx;
}

.empty-state, .loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  color: #999999;
}

.empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; }

.load-more {
  text-align: center;
  padding: 24rpx;
  color: #C9A96E;
  font-size: 26rpx;
}
</style>
