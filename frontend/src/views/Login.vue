<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: 'admin',
  password: '123456'
})
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    userStore.setToken(res.token)
    userStore.setUserInfo(res.user)
    ElMessage.success('登录成功')
    router.replace({ name: 'dashboard' })
  } catch {
    // 错误提示已在 axios 响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="login-title">AgentTrainHub</h2>
      <p class="login-subtitle">Agent+ 算法训练与实验管理平台</p>
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin / teacher / student" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="123456" />
        </el-form-item>
        <el-button type="primary" native-type="submit" class="login-btn" :loading="loading">
          登录
        </el-button>
      </el-form>
      <p class="login-tip">默认账号：admin / teacher / student，密码均为 123456</p>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f6feb 0%, #001529 100%);
}
.login-card {
  width: 380px;
}
.login-title {
  margin: 0;
  text-align: center;
}
.login-subtitle {
  margin: 4px 0 16px;
  text-align: center;
  color: #888;
  font-size: 13px;
}
.login-btn {
  width: 100%;
}
.login-tip {
  margin-top: 12px;
  color: #aaa;
  font-size: 12px;
  text-align: center;
}
</style>
