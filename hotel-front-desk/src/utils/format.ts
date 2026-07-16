export function formatMoney(value?: number | string | null): string {
  const num = Number(value ?? 0)
  return `${num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function formatDate(value?: string | null, fmt?: string): string {
  if (!value) return '-'
  const d = value.slice(0, 10)
  if (!fmt) return d
  const [y, m, day] = d.split('-')
  if (fmt === 'MM/DD') return `${m}/${day}`
  if (fmt === 'YYYY-MM-DD HH:mm') {
    const t = value.length > 10 ? value.slice(11, 16) : ''
    return t ? `${d} ${t}` : d
  }
  return d
}

export function todayStr(): string {
  const d = new Date()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

export function dateRangeLabel(start?: string, end?: string): string {
  if (!start && !end) return '-'
  return `${formatDate(start)} ~ ${formatDate(end)}`
}
