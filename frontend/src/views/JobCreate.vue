<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  taskName: '',
  datasetId: undefined as number | undefined,
  templateId: undefined as number | undefined,
  epochs: 20,
  batchSize: 16,
  learningRate: 0.001,
  validationRatio: 0.2
})

const agentText = ref('')
const submitting = ref(false)

function handleAgentGenerate() {
  // 第二阶段：调用 /api/agent/training-params，将返回的草稿回填到 form
  ElMessage.info('Agent 参数生成将在第二阶段接入')
}

function handleSubmit() {
  if (!form.taskName) {
    ElMessage.warning('请输入任务名称')
    return
  }
  // 第二阶段：调用 createJob(form) 并跳转任务详情
  ElMessage.success('提交占位成功（第一阶段不落库）')
  router.push({ name: 'jobs' })
}
</script>

<template>
  <div class="create-page">
    <h3 class="page-title">创建训练任务</h3>
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <el-form :model="form" label-width="120px">
            <el-form-item label="任务名称">
              <el-input v-model="form.taskName" placeholder="例如：PCB缺陷检测训练任务" />
            </el-form-item>
            <el-form-item label="数据集">
              <el-select v-model="form.datasetId" placeholder="选择数据集" class="full">
                <el-option label="（第一阶段无数据）" :value="0" disabled />
              </el-select>
            </el-form-item>
            <el-form-item label="模型模板">
              <el-select v-model="form.templateId" placeholder="选择模板" class="full">
                <el-option label="IMAGE_CLASSIFY_DEMO" :value="1" />
                <el-option label="YOLO_DEMO" :value="2" />
                <el-option label="TEXT_CLASSIFY_DEMO" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="Epochs">
              <el-input-number v-model="form.epochs" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item label="Batch Size">
              <el-input-number v-model="form.batchSize" :min="1" :max="1024" />
            </el-form-item>
            <el-form-item label="Learning Rate">
              <el-input-number v-model="form.learningRate" :min="0" :step="0.0001" :precision="4" />
            </el-form-item>
            <el-form-item label="验证集比例">
              <el-input-number
                v-model="form.validationRatio"
                :min="0"
                :max="0.9"
                :step="0.05"
                :precision="2"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">创建任务</el-button>
              <el-button @click="router.back()">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>Agent 生成参数</template>
          <el-input
            v-model="agentText"
            type="textarea"
            :rows="6"
            placeholder="用自然语言描述训练需求，例如：用 PCB 缺陷图片训练目标检测，跑 50 个 epoch，batch size 16，学习率小一点。"
          />
          <el-button class="agent-btn" type="success" plain @click="handleAgentGenerate">
            生成参数草稿
          </el-button>
          <p class="agent-tip">生成的参数需用户确认后回填，不会自动启动任务。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 16px;
}
.full {
  width: 100%;
}
.agent-btn {
  width: 100%;
  margin-top: 12px;
}
.agent-tip {
  color: #aaa;
  font-size: 12px;
  margin-top: 8px;
}
</style>
