<template>
  <view class="chat-page">
    <!-- messages -->
    <scroll-view
      class="chat-scroll"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <view v-if="messages.length === 0" class="welcome">
        <view class="welcome-icon">🤖</view>
        <text class="welcome-title">酒店AI助手</text>
        <text class="welcome-desc">您好！我是酒店AI助手，可以帮您：</text>
        <view class="welcome-list">
          <text>· 解答设施问题</text>
          <text>· 查询订单</text>
          <text>· 推荐房型</text>
        </view>
      </view>

      <view v-for="(msg, idx) in messages" :key="idx">
        <view class="msg-row" :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'">
          <view class="msg-avatar" v-if="msg.role === 'ai'">🤖</view>
          <view class="msg-bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'">
            <text>{{ msg.content }}</text>
          </view>
          <view class="msg-avatar" v-if="msg.role === 'user'">👤</view>
        </view>
        <view class="intent-tag" v-if="msg.intent">
          <text>{{ msg.intent }}</text>
        </view>
      </view>

      <view v-if="waiting" class="msg-row msg-ai">
        <view class="msg-avatar">🤖</view>
        <view class="msg-bubble bubble-ai typing">
          <text>...</text>
        </view>
      </view>
    </scroll-view>

    <!-- quick commands -->
    <view class="quick-cmds">
      <text class="quick-cmd" @click="sendQuick('酒店有游泳池吗')">💬 设施</text>
      <text class="quick-cmd" @click="sendQuick('帮我查一下我的订单')">📋 订单</text>
      <text class="quick-cmd" @click="sendQuick('推荐一个适合我的房型')">🏨 推荐</text>
      <text class="quick-cmd" @click="sendQuick('退房时间是几点')">❓ 政策</text>
    </view>

    <!-- input -->
    <view class="input-bar">
      <input
        class="chat-input"
        v-model="inputText"
        placeholder="输入您的问题..."
        placeholder-style="color:#B0B0B0"
        :adjust-position="false"
        @confirm="sendMessage"
      />
      <view class="send-btn" @click="sendMessage">发送</view>
    </view>

    <!-- history picker -->
    <view class="history-bar" @click="showSessions = true">
      <text>📋 历史对话</text>
    </view>

    <!-- sessions modal -->
    <view class="modal-mask" v-if="showSessions" @click="showSessions = false">
      <view class="modal-content" @click.stop>
        <view class="modal-title">历史会话</view>
        <scroll-view scroll-y class="session-list">
          <view
            v-for="s in sessions"
            :key="s.sessionId || s.id"
            class="session-item"
            :class="{ active: s.sessionId === sessionId || s.id === sessionId }"
            @click="loadSession(s)"
          >{{ s.title || s.sessionId || s.id }}</view>
          <view v-if="sessions.length === 0" class="no-sessions">暂无历史会话</view>
        </scroll-view>
        <button class="modal-close" @click="showSessions = false">关闭</button>
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
  onLoad() {
    this.loadSessions()
  },
  methods: {
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 99999
      })
    },
    async loadSessions() {
      try {
        const res = await getChatSessions()
        const list = res.data && res.data.records ? res.data.records : (res.data || [])
        this.sessions = (Array.isArray(list) ? list : []).map(s => ({
          ...s,
          sessionId: s.session_id || s.sessionId || s.id,
          title: s.first_message || s.title || s.session_id || ''
        }))
      } catch { /* ignore */ }
    },
    async loadSession(s) {
      const sid = s.sessionId || s.session_id || s.id
      this.sessionId = sid
      this.showSessions = false
      try {
        const res = await getChatHistory(sid)
        const data = res.data || {}
        const list = data.messages || (Array.isArray(data) ? data : [])
        this.messages = list.map(h => ({
          role: h.role || 'ai',
          content: h.content || '',
          intent: h.intent || ''
        }))
        this.scrollToBottom()
      } catch { /* ignore */ }
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.waiting) return
      this.inputText = ''

      // add user message
      this.messages.push({ role: 'user', content: text })
      this.scrollToBottom()
      this.waiting = true

      try {
        const history = this.messages.slice(0, -1).map(m => ({
          role: m.role,
          content: m.content
        }))
        const res = await chatSync(text, this.sessionId || undefined, history)
        const data = res.data || res
        const reply = data.reply || data.message || '抱歉，我暂时无法回答。'
        const sessionId = data.session_id || data.sessionId || ''
        const intent = data.intent || ''

        if (sessionId && !this.sessionId) {
          this.sessionId = sessionId
          this.loadSessions()
        }

        this.messages.push({ role: 'ai', content: reply, intent })
        this.scrollToBottom()
      } catch {
        this.messages.push({ role: 'ai', content: '网络异常，请稍后重试。' })
      } finally {
        this.waiting = false
      }
    },
    sendQuick(text) {
      this.inputText = text
      this.sendMessage()
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F5F6FA;
}

.chat-scroll {
  flex: 1;
  padding: 20rpx;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
}

.welcome-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.welcome-title { font-size: 36rpx; font-weight: 700; color: #2C3E50; margin-bottom: 16rpx; }
.welcome-desc { font-size: 28rpx; color: #888; margin-bottom: 16rpx; }
.welcome-list { font-size: 26rpx; color: #AAA; line-height: 2; }

.msg-row {
  display: flex;
  margin-bottom: 20rpx;

  &.msg-user { justify-content: flex-end; }
  &.msg-ai { justify-content: flex-start; }
}

.msg-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #FFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,.06);
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 70%;
  padding: 18rpx 24rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.7;
  word-break: break-all;
}

.bubble-ai {
  background: #FFFFFF;
  color: #333;
  margin-left: 12rpx;
  border-top-left-radius: 4rpx;
}

.bubble-user {
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  margin-right: 12rpx;
  border-top-right-radius: 4rpx;
}

.typing {
  color: #999;

  text { animation: blink 1s infinite; }
}

.intent-tag {
  display: flex;
  justify-content: flex-start;
  margin-left: 76rpx;
  margin-top: -12rpx;
  margin-bottom: 16rpx;

  text {
    display: inline-block;
    background: rgba(201, 169, 110, 0.1);
    color: #C9A96E;
    border-radius: 20rpx;
    padding: 4rpx 18rpx;
    font-size: 22rpx;
  }
}

.quick-cmds {
  display: flex;
  padding: 12rpx 20rpx;
  background: #FFF;
  border-top: 1rpx solid #F0F0F0;
}

.quick-cmd {
  font-size: 24rpx;
  color: #888;
  padding: 8rpx 16rpx;
  background: #F5F6FA;
  border-radius: 20rpx;
  margin-right: 12rpx;
  white-space: nowrap;

  &:active { background: rgba(201, 169, 110, 0.1); color: #C9A96E; }
}

.input-bar {
  display: flex;
  align-items: center;
  background: #FFF;
  padding: 12rpx 20rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #F0F0F0;
}

.chat-input {
  flex: 1;
  height: 72rpx;
  background: #F5F6FA;
  border-radius: 36rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
}

.send-btn {
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  padding: 14rpx 28rpx;
  border-radius: 28rpx;
  font-size: 28rpx;
  font-weight: 500;
  margin-left: 16rpx;

  &:active { opacity: 0.8; }
}

.history-bar {
  text-align: center;
  padding: 12rpx;
  font-size: 24rpx;
  color: #999;
  background: #FFF;
}

.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,.4);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.modal-content {
  background: #FFF;
  width: 100%;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx 28rpx;
  max-height: 60vh;
  display: flex;
  flex-direction: column;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 700;
  text-align: center;
  margin-bottom: 24rpx;
}

.session-list {
  flex: 1;
  max-height: 400rpx;
}

.session-item {
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #555;
  border-bottom: 1rpx solid #F0F0F0;

  &.active { color: #C9A96E; font-weight: 600; }
}

.no-sessions { text-align: center; color: #CCC; padding: 40rpx 0; }

.modal-close {
  margin-top: 20rpx;
  height: 80rpx;
  background: #F5F6FA;
  color: #666;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
  &::after { border: none; }
}
</style>
