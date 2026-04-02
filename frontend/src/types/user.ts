export interface UserDTO {
  id: number
  nickname: string
  avatar: string
  phone: string
  communityName: string
  building: string
  creditScore: number
  borrowCount: number
  lendCount: number
  co2Saved: number
}

export interface UserStatsDTO {
  lentCount: number
  borrowedCount: number
  pendingCount: number
  dueSoonCount: number
  todayCo2Saved: number
}
