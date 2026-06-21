<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import {
  listDatasets,
  uploadDataset,
  deleteDataset,
  downloadDataset,
  type DatasetItem
} from '@/api/dataset'
import { formatDateTime, formatFileSize } from '@/utils/format'

const loading = ref(false)
const uploading = ref(false)
const datasets = ref<DatasetItem[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function load() {
  loading.value = true
  try {
    const data = await listDatasets({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword
    })
    datasets.value = data.records
    total.value = data.total
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  load()
}

function handlePageChange(page: number) {
  query.pageNum = page
  load()
}

function beforeUpload(file: File) {
  const maxMB = 500
  if (file.size > maxMB * 1024 * 1024) {
    ElMessage.error(`文件不能超过 ${maxMB}MB`)
    return false
  }
  return true
}

async function customUpload(options: UploadRequestOptions) {
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('name', options.file.name)
  uploading.value = true
  try {
    await uploadDataset(formData)
    ElMessage.success('上传成功')
    query.pageNum = 1
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    uploading.value = false
  }
}

async function handleDownload(row: DatasetItem) {
  try {
    const res = await downloadDataset(row.id)
    const blob = res.data as Blob
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.name
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch {
    // 拦截器已提示
  }
}

async function handleDelete(row: DatasetItem) {
  try {
    await ElMessageBox.confirm(`确定删除数据集「${row.name}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return // 用户取消
  }
  try {
    await deleteDataset(row.id)
    ElMessage.success('已删除')
    await load()
  } catch {
    // 拦截器已提示
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h3 class="page-title">数据集</h3>
      <div class="actions">
        <el-input
          v-model="query.keyword"
          placeholder="按名称搜索"
          clearable
          class="search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button @click="handleSearch">搜索</el-button>
        <el-upload
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="customUpload"
          accept=".zip,.csv,.json,.txt,.png,.jpg,.jpeg"
        >
          <el-button type="primary" :loading="uploading">上传数据集</el-button>
        </el-upload>
      </div>
    </div>

    <el-table :data="datasets" v-loading="loading" border empty-text="暂无数据">
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column label="大小" width="110">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="ownerName" label="上传人" width="120" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="上传时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :current-page="query.pageNum"
      :page-size="query.pageSize"
      @current-change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search {
  width: 220px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
