import { get } from './http'

export interface HealthInfo {
  serviceName: string
  status: string
  timestamp: string
}

export function fetchHealth() {
  return get<HealthInfo>('/health')
}

