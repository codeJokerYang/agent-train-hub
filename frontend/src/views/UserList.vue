<script setup lang="ts">
import { ref } from 'vue'

interface UserRow {
  id: number
  username: string
  realName?: string
  role: string
  status: number
}

const loading = ref(false)
const users = ref<UserRow[]>([
  { id: 1, username: 'admin', realName: '管理员', role: 'ADMIN', status: 1 },
  { id: 2, username: 'teacher', realName: '教师', role: 'TEACHER', status: 1 },
  { id: 3, username: 'student', realName: '学生', role: 'STUDENT', status: 1 }
])
</script>

<template>
  <div>
    <div class="toolbar">
      <h3 class="page-title">用户管理</h3>
      <el-button type="primary" disabled>新增用户</el-button>
    </div>
    <el-alert
      class="tip"
      title="第一阶段展示三个默认账号（占位数据），第二阶段接入用户管理接口（仅 ADMIN 可访问）。"
      type="info"
      :closable="false"
    />
    <el-table :data="users" v-loading="loading" border class="table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" width="160" />
      <el-table-column prop="realName" label="姓名" width="160" />
      <el-table-column prop="role" label="角色" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
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
.tip {
  margin-bottom: 16px;
}
.table {
  margin-top: 8px;
}
</style>
