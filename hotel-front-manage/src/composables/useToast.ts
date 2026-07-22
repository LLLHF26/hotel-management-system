import { reactive } from 'vue'

export type ToastType = 'success' | 'error' | 'info'

interface ToastState {
  visible: boolean
  message: string
  type: ToastType
  // 自增 id，用作 Transition key，保证连续不同消息也能触发动画
  id: number
}

const state = reactive<ToastState>({
  visible: false,
  message: '',
  type: 'success',
  id: 0,
})

let timer: ReturnType<typeof setTimeout> | null = null

function show(message: string, type: ToastType = 'success', duration = 1500) {
  state.message = message
  state.type = type
  state.id += 1
  state.visible = true
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    state.visible = false
  }, duration)
}

export function useToast() {
  return {
    state,
    success: (msg: string) => show(msg, 'success'),
    error: (msg: string) => show(msg, 'error'),
    info: (msg: string) => show(msg, 'info'),
  }
}
