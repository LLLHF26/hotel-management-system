<script setup lang="ts">
import { useToast } from '../composables/useToast'
const { state } = useToast()
</script>

<template>
  <Transition name="toast">
    <div
      v-if="state.visible"
      :key="state.id"
      :class="['toast-global', state.type]"
      role="status"
      aria-live="polite"
    >
      <span class="toast-icon">
        <svg v-if="state.type === 'success'" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
        <svg v-else-if="state.type === 'error'" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
        <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8v4M12 16h.01"/></svg>
      </span>
      <span class="toast-msg">{{ state.message }}</span>
    </div>
  </Transition>
</template>

<style scoped>
.toast-global {
  position: fixed;
  top: 76px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px 10px 14px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.18), 0 2px 6px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  max-width: 80vw;
}
.toast-global.success { background: rgba(22, 163, 74, 0.96); }
.toast-global.error   { background: rgba(220, 38, 38, 0.96); }
.toast-global.info    { background: rgba(37, 99, 235, 0.96); }

.toast-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  flex-shrink: 0;
}
.toast-msg { line-height: 1.3; }

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.32s cubic-bezier(0.34, 1.56, 0.64, 1),
              transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-18px) scale(0.9);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px) scale(0.95);
}
</style>
