export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

export interface LoginVO {
  token: string
  userId: number
  username: string
  realName: string
  role: string
}

export interface UserVO {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  role: string
  avatar?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface CustomerVO {
  id: number
  realName: string
  idCard?: string
  phone: string
  gender?: number
  avatar?: string
  points?: number
  totalConsumed?: number
  memberLevel?: string
  status?: number
  createTime?: string
}

export interface CleanerVO {
  id: number
  realName: string
  phone?: string
  currentTaskCount?: number
}

export interface RoomVO {
  id: number
  roomNumber: string
  roomTypeId?: number
  roomTypeName?: string
  floor?: number
  status: string
  price?: number
  description?: string
  cleanerName?: string
  taskStartTime?: string
  currentTask?: {
    taskId?: number
    cleanerName?: string
    startTime?: string
    status?: string
    reason?: string
  }
}

export interface DashboardVO {
  total: number
  summary: Record<string, number>
  rooms: RoomVO[]
}

export interface ExtraVO {
  id: number
  itemName: string
  amount: number
  quantity: number
  totalAmount?: number
  createTime?: string
}

export interface PaymentVO {
  id: number
  paymentNo?: string
  amount: number
  method: string
  methodName?: string
  status?: string
  createTime?: string
}

export interface OrderVO {
  id: number
  orderNo: string
  customerId?: number
  customerName?: string
  customerPhone?: string
  roomId?: number
  roomNumber?: string
  roomTypeName?: string
  checkInDate?: string
  checkOutDate?: string
  nights?: number
  actualCheckIn?: string
  actualCheckOut?: string
  roomPrice?: number
  roomTotal?: number
  extraTotal?: number
  totalAmount?: number
  paidAmount?: number
  deposit?: number
  status: string
  statusName?: string
  source?: string
  sourceName?: string
  remark?: string
  createTime?: string
  extras?: ExtraVO[]
  payments?: PaymentVO[]
}

export interface TodaySummary {
  date?: string
  roomRevenue?: number
  extraRevenue?: number
  totalRevenue?: number
  orderCount?: number
  checkInCount?: number
  checkOutCount?: number
  occupancyRate?: string
}

export interface RevenueSummaryVO {
  today: TodaySummary
  thisMonth?: unknown
  thisYear?: unknown
}

export interface DailyTrendVO {
  dates: string[]
  roomRevenue: number[]
  extraRevenue: number[]
  totalRevenue: number[]
  orderCount: number[]
}

export interface OccupancyByTypeVO {
  roomTypeName: string
  occupancyRate: string
}

export interface AlertItem {
  id?: string | number
  title?: string
  message?: string
  level?: string
  time?: string
  read?: boolean
}

export interface AlertStatusVO {
  alerts: AlertItem[]
  unread_count: number
}

export interface OrderListParams {
  page?: number
  size?: number
  status?: string
  customerPhone?: string
  roomNumber?: string
  checkInDate?: string
  startDate?: string
  endDate?: string
}

export interface CustomerListParams {
  page?: number
  size?: number
  keyword?: string
  memberLevel?: string
}

export interface RoomListParams {
  page?: number
  size?: number
  status?: string
  floor?: number
  keyword?: string
}
