<template>
  <view class="chat-page">
    <!-- header -->
    <view class="chat-header">
      <text class="header-title">AI 客服</text>
      <text class="header-sub">随时为您解答问题</text>
    </view>

    <!-- messages -->
    <scroll-view
      class="msg-scroll"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <!-- welcome -->
      <view v-if="messages.length === 0" class="welcome-card">
        <view class="welcome-avatar">
          <image class="avatar-img" src="/static/quick/ai.png" mode="aspectFit"></image>
        </view>
        <text class="welcome-title">您好！我是酒店AI助手</text>
        <text class="welcome-desc">可以帮您：</text>
        <view class="welcome-grid">
          <view class="wg-item" @click="sendQuick('查询房型和价格')">
            <view class="wg-icon-wrap"><image class="wg-img" src="/static/icons/bed.png" mode="aspectFit"></image></view>
            <text class="wg-label">查询房型和价格</text>
          </view>
          <view class="wg-item" @click="sendQuick('帮我查一下我的订单')">
            <view class="wg-icon-wrap"><image class="wg-img" src="/static/icons/clipboard.png" mode="aspectFit"></image></view>
            <text class="wg-label">查看订单状态</text>
          </view>
          <view class="wg-item" @click="sendQuick('推荐一个适合我的房型')">
            <view class="wg-icon-wrap"><image class="wg-img" src="/static/icons/target.png" mode="aspectFit"></image></view>
            <text class="wg-label">推荐合适的房型</text>
          </view>
          <view class="wg-item" @click="sendQuick('退房时间是几点')">
            <view class="wg-icon-wrap"><image class="wg-img" src="/static/icons/book.png" mode="aspectFit"></image></view>
            <text class="wg-label">解答酒店政策</text>
          </view>
        </view>
      </view>

      <!-- message list -->
      <view v-for="(msg, idx) in messages" :key="idx" class="msg-wrap" :class="'msg-' + msg.role">
        <view v-if="msg.role === 'ai'" class="ai-avatar">
          <image class="avt-img" src="/static/quick/ai.png" mode="aspectFit"></image>
        </view>
        <view class="bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
          <text class="bubble-text">{{ msg.content }}</text>
        </view>
        <view v-if="msg.role === 'user'" class="user-avatar"><image class="ua-img" src="/static/icons/user.png" mode="aspectFit"></image></view>
      </view>

      <!-- typing -->
      <view v-if="waiting" class="msg-wrap msg-ai">
        <view class="ai-avatar">
          <image class="avt-img" src="/static/quick/ai.png" mode="aspectFit"></image>
        </view>
        <view class="bubble ai-bubble typing-bubble">
          <view class="typing-dots">
            <view class="t-dot"></view>
            <view class="t-dot d2"></view>
            <view class="t-dot d3"></view>
          </view>
        </view>
      </view>

      <view style="height: 20rpx;"></view>
    </scroll-view>

    <!-- bottom area: quick + input combined -->
    <view class="bottom-area">
      <!-- quick commands - 2x2 grid -->
      <view class="quick-grid">
        <view class="qg-item" @click="sendQuick('酒店有游泳池吗')">
          <image class="qg-img" src="/static/icons/pool.png" mode="aspectFit"></image>
          <text class="qg-text">设施</text>
        </view>
        <view class="qg-item" @click="sendQuick('帮我查一下我的订单')">
          <image class="qg-img" src="/static/icons/clipboard.png" mode="aspectFit"></image>
          <text class="qg-text">订单</text>
        </view>
        <view class="qg-item" @click="sendQuick('推荐一个适合我的房型')">
          <image class="qg-img" src="/static/icons/hotel.png" mode="aspectFit"></image>
          <text class="qg-text">推荐</text>
        </view>
        <view class="qg-item" @click="sendQuick('退房时间是几点')">
          <image class="qg-img" src="/static/icons/question.png" mode="aspectFit"></image>
          <text class="qg-text">政策</text>
        </view>
      </view>

      <!-- input bar -->
      <view class="input-row">
        <input
          class="chat-input"
          v-model="inputText"
          placeholder="输入您的问题..."
          placeholder-style="color:#9CA3AF"
          :adjust-position="false"
          confirm-type="send"
          @confirm="sendMessage"
        />
        <view class="send-btn" :class="{ active: inputText.trim() }" @click="sendMessage">
          <text class="send-icon">↑</text>
        </view>
      </view>
    </view>

    <!-- sessions modal -->
    <view class="modal-mask" v-if="showSessions" @click="showSessions = false">
      <view class="modal-panel" @click.stop>
        <text class="modal-head">历史会话</text>
        <scroll-view scroll-y class="session-scroll">
          <view
            v-for="s in sessions"
            :key="s.sessionId || s.id"
            class="s-item"
            :class="{ sActive: s.sessionId === sessionId || s.id === sessionId }"
            @click="loadSession(s)"
          >{{ s.title || s.sessionId || s.id }}</view>
          <view v-if="sessions.length === 0" class="s-empty">暂无历史会话</view>
        </scroll-view>
        <view class="modal-close-btn" @click="showSessions = false">关闭</view>
      </view>
    </view>
  </view>
</template>

<script>
import { chatSync, getChatSessions, getChatHistory } from '../../src/api/chat'

export default {
  data() {
    return {
      messages: [],
      inputText: '',
      sessionId: '',
      waiting: false,
      showSessions: false,
      sessions: [],
      scrollTop: 0
    }
  },
  onLoad() { this.loadSessions() },
  methods: {
    scrollToBottom() { this.$nextTick(() => { this.scrollTop = 99999 }) },
    async loadSessions() {
      try {
        const res = await getChatSessions()
        const list = res.data && res.data.records ? res.data.records : (res.data || [])
        this.sessions = (Array.isArray(list) ? list : []).map(s => ({
          ...s, sessionId: s.session_id || s.sessionId || s.id,
          title: s.first_message || s.title || s.session_id || ''
        }))
      } catch {}
    },
    async loadSession(s) {
      const sid = s.sessionId || s.session_id || s.id
      this.sessionId = sid; this.showSessions = false
      try {
        const res = await getChatHistory(sid)
        const data = res.data || {}
        const list = data.messages || (Array.isArray(data) ? data : [])
        this.messages = list.map(h => ({ role: h.role || 'ai', content: h.content || '', intent: h.intent || '' }))
        this.scrollToBottom()
      } catch {}
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.waiting) return
      this.inputText = ''
      this.messages.push({ role: 'user', content: text })
      this.scrollToBottom()
      this.waiting = true

      try {
        const history = this.messages.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
        const res = await chatSync(text, this.sessionId || undefined, history)
        const data = res.data || res
        const reply = data.reply || data.message || '抱歉，我暂时无法回答。'
        if (data.session_id || data.sessionId) { this.sessionId = data.session_id || data.sessionId; this.loadSessions() }
        this.messages.push({ role: 'ai', content: reply })
        this.scrollToBottom()
      } catch { this.messages.push({ role: 'ai', content: '网络异常，请稍后重试。' }) }
      finally { this.waiting = false }
    },
    sendQuick(text) { this.inputText = text; this.sendMessage() }
  }
}
</script>

<style lang="scss" scoped>
$bg: #F8F6F3;
$card-bg: #FFFFFF;
$text-primary: #1A1A2E;
$text-secondary: #6B7280;
$text-light: #9CA3AF;
$accent-gold: #D97706;

.chat-page { display: flex; flex-direction: column; height: 100vh; background: $bg; }

/* ====== Header ====== */
.chat-header {
  background: $card-bg;
  padding: calc(16rpx + env(safe-area-inset-top)) 28rpx 14rpx;
}
.header-title { font-size: 32rpx; font-weight: 800; color: $text-primary; display: block; }
.header-sub { font-size: 23rpx; color: $text-light; margin-top: 2rpx; }

/* ====== Messages ====== */
.msg-scroll { flex: 1; padding: 16rpx 20rpx 8rpx; }

/* Welcome Card - evenly distributed */
.welcome-card {
  display: flex; flex-direction: column; align-items: center;
  padding: 32rpx 24rpx 36rpx;
}
.welcome-avatar {
  width: 100rpx; height: 100rpx; border-radius: 50%;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 18rpx;
  box-shadow: 0 4rpx 16rpx rgba(217,119,6,.15);
}
.avatar-img { width: 72rpx; height: 72rpx; }
.welcome-title { font-size: 32rpx; font-weight: 700; color: $text-primary; margin-bottom: 8rpx; }
.welcome-desc { font-size: 25rpx; color: $text-secondary; margin-bottom: 20rpx; }

/* 2x2 Grid for welcome actions */
.welcome-grid {
  display: flex; flex-wrap: wrap; width: 100%; gap: 16rpx;
  justify-content: center;
}
.wg-item {
  width: 44%; display: flex; flex-direction: column; align-items: center;
  padding: 24rpx 12rpx 18rpx; background: $card-bg;
  border-radius: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04);
  &:active { transform: scale(.97); opacity: .85; }
}
.wg-icon-wrap {
  width: 88rpx; height: 88rpx; border-radius: 50%;
  background: linear-gradient(135deg, #FFF7ED, #FFEDD5);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12rpx;
}
.wg-img { width: 50rpx; height: 50rpx; }
.wg-label { font-size: 24rpx; color: $text-secondary; text-align: center; line-height: 1.4; }

/* Message */
.msg-wrap {
  display: flex; align-items: flex-start; margin-bottom: 18rpx; gap: 10rpx;
  &.msg-user { justify-content: flex-end; }
  &.msg-ai { justify-content: flex-start; }
}

.ai-avatar, .user-avatar {
  width: 56rpx; height: 56rpx; border-radius: 50%;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; box-shadow: 0 2rpx 8rpx rgba(0,0,0,.06);
}
.avt-img { width: 42rpx; height: 42rpx; }
.ua-img { width: 34rpx; height: 34rpx; }

.bubble {
  max-width: 70%; padding: 16rpx 20rpx; border-radius: 20rpx;
}
.bubble-text { font-size: 27rpx; line-height: 1.65; word-break: break-all; }

.user-bubble {
  background: linear-gradient(135deg, #F97316, #EA580C);
  .bubble-text { color: #FFF; }
  border-top-right-radius: 6rpx;
}
.ai-bubble {
  background: $card-bg;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,.04);
  .bubble-text { color: $text-primary; }
  border-top-left-radius: 6rpx;
}

.typing-bubble { padding: 20rpx 24rpx; }
.typing-dots { display: flex; gap: 8rpx; }
.t-dot {
  width: 12rpx; height: 12rpx; border-radius: 50%; background: $accent-gold;
  animation: pulse 1.2s infinite ease-in-out;
  &.d2 { animation-delay: .15s; }
  &.d3 { animation-delay: .3s; }
}
@keyframes pulse {
  0%,80%,100% { transform: scale(.5); opacity: .35; }
  40% { transform: scale(1); opacity: 1; }
}

/* ====== Bottom Area (quick + input) ====== */
.bottom-area {
  background: $card-bg;
  border-top: 1rpx solid #F0EDE8;
  padding-bottom: env(safe-area-inset-bottom);
}

/* Quick commands - 2x2 even grid */
.quick-grid {
  display: flex; justify-content: space-around;
  padding: 18rpx 16rpx 14rpx;
}
.qg-item {
  display: flex; flex-direction: column; align-items: center;
  gap: 6rpx; padding: 8rpx 16rpx;
  &:active { opacity: .7; }
}
.qg-img { width: 36rpx; height: 36rpx; }
.qg-text { font-size: 23rpx; color: $text-secondary; font-weight: 500; }

/* Input Row */
.input-row {
  display: flex; align-items: center;
  padding: 10rpx 20rpx 16rpx; gap: 14rpx;
}
.chat-input {
  flex: 1; height: 72rpx; background: #F3F0EB; border-radius: 36rpx;
  padding: 0 26rpx; font-size: 28rpx; color: $text-primary;
}
.send-btn {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: #E8E4DD; display: flex; align-items: center; justify-content: center;
  transition: all .2s;
  &.active { background: linear-gradient(135deg, #F97316, #EA580C); }
  &:active { opacity: .85; }
}
.send-icon { font-size: 32rpx; color: #FFF; font-weight: 300; }

/* Modal */
.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 999;
  display: flex; align-items: flex-end;
}
.modal-panel {
  background: $card-bg; width: 100%;
  border-radius: 28rpx 28rpx 0 0; padding: 32rpx 28rpx;
  max-height: 65vh; display: flex; flex-direction: column;
}
.modal-head { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.session-scroll { flex: 1; max-height: 450rpx; overflow: hidden; }
.s-item {
  padding: 22rpx 0; font-size: 28rpx; color: #555;
  border-bottom: 1rpx solid #F0EDE8;
  &.sActive { color: $accent-gold; font-weight: 600; }
}
.s-empty { text-align: center; color: #CCC; padding: 48rpx 0; font-size: 26rpx; }
.modal-close-btn {
  margin-top: 20rpx; height: 80rpx; background: #F3F0EB;
  color: $text-secondary; font-size: 28rpx; border-radius: 16rpx;
  display: flex; align-items: center; justify-content: center;
}
</style>
