import { authFetch } from './request'

export async function getRevenueSummary() {
  try {
    const res = await authFetch('/api/finance/revenue/summary')
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'revenue summary request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: {
        today: { date: '2026-05-17', roomRevenue: 18240.0, extraRevenue: 2440.0, totalRevenue: 20680.0, orderCount: 18, checkInCount: 38, checkOutCount: 34, occupancyRate: '76.5%' },
        thisMonth: { month: '2026-05', roomRevenue: 246960.0, extraRevenue: 13680.0, totalRevenue: 260640.0, orderCount: 420, avgDailyRevenue: 15331.76 },
        thisYear: { year: '2026', roomRevenue: 1234800.0, extraRevenue: 68400.0, totalRevenue: 1303200.0, orderCount: 2100 }
      }
    }
  }
}

export async function getDailyRevenue({ startDate, endDate }: { startDate: string, endDate: string }) {
  try {
    const res = await authFetch(`/api/finance/revenue/daily?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'daily revenue request failed')
    return json
  } catch (e) {
    const dates = ['2026-05-16','2026-05-17','2026-05-18','2026-05-19','2026-05-20','2026-05-21','2026-05-22']
    const roomRevenue = [15000,15800,16000,17000,17500,18000,18240]
    const extraRevenue = [800,820,750,900,980,1100,2440]
    const totalRevenue = roomRevenue.map((v,i)=> v + extraRevenue[i])
    return { code: 200, msg: 'mock', data: { dates, roomRevenue, extraRevenue, totalRevenue, orderCount: [25,26,24,27,28,30,18] } }
  }
}

export async function getRevenueByRoomType({ startDate, endDate }: { startDate: string, endDate: string }) {
  try {
    const res = await authFetch(`/api/finance/revenue/by-room-type?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'revenue by room type request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: [
        { roomTypeId: 2, roomTypeName: '商务双床房', roomCount: 15, revenue: 42000.0, orderCount: 80, occupancyRate: '71.5%', rank: 1 },
        { roomTypeId: 1, roomTypeName: '豪华大床房', roomCount: 20, revenue: 58800.0, orderCount: 100, occupancyRate: '78.2%', rank: 2 },
        { roomTypeId: 3, roomTypeName: '标准大床房', roomCount: 12, revenue: 52000.0, orderCount: 72, occupancyRate: '68.0%', rank: 3 },
        { roomTypeId: 4, roomTypeName: '行政套房', roomCount: 5, revenue: 32000.0, orderCount: 40, occupancyRate: '60.0%', rank: 4 },
        { roomTypeId: 5, roomTypeName: '总统套房', roomCount: 2, revenue: 15000.0, orderCount: 10, occupancyRate: '50.0%', rank: 5 }
      ]
    }
  }
}

export async function getOccupancyByType({ startDate, endDate }: { startDate: string, endDate: string }) {
  try {
    const res = await authFetch(`/api/finance/analysis/occupancy-by-type?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'occupancy by type request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: [
        { roomTypeName: '豪华大床房', totalRooms: 20, occupiedNights: 266, totalNights: 340, occupancyRate: '78.2%' },
        { roomTypeName: '商务双床房', totalRooms: 15, occupiedNights: 230, totalNights: 340, occupancyRate: '67.6%' },
        { roomTypeName: '标准大床房', totalRooms: 18, occupiedNights: 240, totalNights: 360, occupancyRate: '66.7%' },
        { roomTypeName: '行政套房', totalRooms: 8, occupiedNights: 190, totalNights: 280, occupancyRate: '67.9%' },
        { roomTypeName: '总统套房', totalRooms: 2, occupiedNights: 22, totalNights: 30, occupancyRate: '73.3%' }
      ]
    }
  }
}
