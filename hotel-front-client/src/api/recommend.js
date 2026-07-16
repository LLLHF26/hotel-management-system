import { post } from '../utils/request'

export function recommendRooms(data) {
  return post('/api/ai/recommend/room', data, true)
}
