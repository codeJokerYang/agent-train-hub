<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import StatusTag from '@/components/StatusTag.vue'
import type { TrainingJobItem } from '@/api/job'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const jobs = ref<TrainingJobItem[]>([])

function goCreate() {
  router.push({ name: 'job-create' })
}

function goDetail(id: number) {
  router.push({ name: 'job-detail', params: { id } })
}
</script>

<template>
  <div>
    <div class="toolbar">
      <h3 class="page-title">训练任务</h3>
      <el-button type="primary" @click="goCreate">新建任务</el-button>
    </div>
    <el-table :data="jobs" v-loading="loading" border empty-text="暂无任务（第一阶段骨架）">
      <el-table-column prop="taskName" label="任务名称" min-width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <StatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="进度" width="200">
        <template #default="{ row }">
          <el-progress :percentage="row.progress ?? 0" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
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
</style>
