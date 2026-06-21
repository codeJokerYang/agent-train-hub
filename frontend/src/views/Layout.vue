<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentUser } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const displayName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || '用户'
)
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

onMounted(async () => {
  // 刷新后 token 还在但内存里的用户信息丢失，拉一次当前用户
  if (userStore.token && !userStore.userInfo) {
    try {
      const user = await getCurrentUser()
      userStore.setUserInfo(user)
    } catch {
      // token 失效时拦截器会跳转登录页
    }
  }
})

function handleLogout() {
  userStore.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">AgentTrainHub</div>
      <el-menu :default-active="activeMenu" router class="layout-menu">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/datasets">数据集</el-menu-item>
        <el-menu-item index="/jobs">训练任务</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <span class="title">Agent+ 算法训练与实验管理平台</span>
        <div class="user-area">
          <el-tag v-if="userStore.userInfo" size="small" type="info">
            {{ userStore.userInfo.role }}
          </el-tag>
          <span class="username">{{ displayName }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.layout-aside {
  background: #001529;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}
.layout-menu {
  border-right: none;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  background: #fff;
}
.layout-header .title {
  font-size: 16px;
  font-weight: 600;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  font-size: 14px;
  color: #333;
}
</style>
