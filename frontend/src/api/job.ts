import { request } from './request'
import type { PageData, PageQuery } from './dataset'

export type JobStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED'

export interface TrainingJobItem {
  id: number
  taskName: string
  status: JobStatus
  progress: number
  currentEpoch: number
  totalEpoch: number
  createdAt: string
}

export interface CreateJobPayload {
  taskName: string
  datasetId: number
  templateId: number
  params: Record<string, unknown>
}

/** 任务分页（/api/training-jobs）。 */
export function listJobs(params: PageQuery) {
  return request<PageData<TrainingJobItem>>({ url: '/training-jobs', method: 'get', params })
}

/** 任务详情。 */
export function getJob(id: number | string) {
  return request<TrainingJobItem>({ url: `/training-jobs/${id}`, method: 'get' })
}

/** 创建任务。 */
export function createJob(payload: CreateJobPayload) {
  return request<TrainingJobItem>({ url: '/training-jobs', method: 'post', data: payload })
}

/** 启动 / 停止 / 重跑。 */
export function startJob(id: number | string) {
  return request<TrainingJobItem>({ url: `/training-jobs/${id}/start`, method: 'post' })
}

export function cancelJob(id: number | string) {
  return request<TrainingJobItem>({ url: `/training-jobs/${id}/cancel`, method: 'post' })
}

export function rerunJob(id: number | string) {
  return request<TrainingJobItem>({ url: `/training-jobs/${id}/rerun`, method: 'post' })
}
