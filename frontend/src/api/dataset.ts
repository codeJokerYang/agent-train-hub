import service, { request } from './request'

export interface DatasetItem {
  id: number
  name: string
  type: string
  fileSize: number
  fileHash?: string
  ownerId?: number
  ownerName?: string
  status: string
  createdAt: string
}

export interface PageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

export interface PageData<T> {
  pageNum: number
  pageSize: number
  total: number
  records: T[]
}

/** 数据集分页（GET /api/datasets）。 */
export function listDatasets(params: PageQuery) {
  return request<PageData<DatasetItem>>({ url: '/datasets', method: 'get', params })
}

/**
 * 上传数据集（POST /api/datasets，multipart）。
 * 不手动设置 Content-Type：浏览器会自动带上正确的 multipart boundary。
 */
export function uploadDataset(formData: FormData) {
  return request<DatasetItem>({ url: '/datasets', method: 'post', data: formData })
}

/** 重新分析（POST /api/datasets/{id}/analyze）。 */
export function analyzeDataset(id: number) {
  return request<DatasetItem>({ url: `/datasets/${id}/analyze`, method: 'post' })
}

/** 删除（DELETE /api/datasets/{id}）。 */
export function deleteDataset(id: number) {
  return request<void>({ url: `/datasets/${id}`, method: 'delete' })
}

/** 下载原始文件，返回带 Blob 的原始响应（不走 Result 解包）。 */
export function downloadDataset(id: number) {
  return service.get(`/datasets/${id}/download`, { responseType: 'blob' })
}
