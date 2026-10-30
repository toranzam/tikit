
import {apiClient} from './apiClient'

interface ApiResponse<T> {
  code: string
  message: string
  data: T | null
}

/**
 * 게시된 공연 목록 조회
 * GET /api/v1/performances
 *
 * 실제 응답 DTO 확정 후 반환 타입 구체화 예정
 */
export async function getPerformances(): Promise<unknown> {
  const response = await apiClient.get<ApiResponse<unknown>>('/api/v1/performances')

  if (response.data.code !== 'SUCCESS') {
    throw new Error(response.data.message || '공연 목록을 불러오지 못했습니다.')
  }

  return response.data.data
}
