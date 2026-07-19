<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { useRoute } from 'vue-router'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use, type ECharts } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import StatusTag from '@/components/StatusTag.vue'

use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const route = useRoute()
const jobId = String(route.params.id ?? '')

const activeTab = ref('info')
const chartRef = ref<HTMLDivElement>()
let chart: ECharts | null = null

function renderChart() {
  if (!chartRef.value) return
  chart = init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['loss', 'accuracy'] },
    xAxis: { type: 'category', data: ['1', '2', '3', '4', '5'] },
    yAxis: { type: 'value' },
    series: [
      { name: 'loss', type: 'line', data: [1.0, 0.8, 0.65, 0.55, 0.48] },
      { name: 'accuracy', type: 'line', data: [0.4, 0.55, 0.66, 0.72, 0.78] }
    ]
  })
}

function handleTabChange(name: string | number) {
  if (name === 'metrics') {
    requestAnimationFrame(() => {
      if (!chart) renderChart()
      else chart.resize()
    })
  }
}

onBeforeUnmount(() => {
  chart?.dispose()
})
</script>

<template>
  <div>
    <div class="toolbar">
      <h3 class="page-title">任务详情 #{{ jobId }}</h3>
      <StatusTag status="PENDING" />
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="基本信息" name="info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">-</el-descriptions-item>
          <el-descriptions-item label="状态">PENDING</el-descriptions-item>
          <el-descriptions-item label="数据集">-</el-descriptions-item>
          <el-descriptions-item label="模板">-</el-descriptions-item>
          <el-descriptions-item label="进度">0%</el-descriptions-item>
          <el-descriptions-item label="创建时间">-</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="训练日志" name="logs">
        <el-empty description="第一阶段骨架，日志将在第二阶段通过 SSE 实时展示" />
      </el-tab-pane>

      <el-tab-pane label="指标曲线" name="metrics">
        <div ref="chartRef" class="chart"></div>
      </el-tab-pane>

      <el-tab-pane label="模型产物" name="artifacts">
        <el-empty description="任务完成后展示可下载的模型产物与报告" />
      </el-tab-pane>

      <el-tab-pane label="Agent 诊断" name="diagnose">
        <el-empty description="第二阶段：调用 Agent 对失败任务进行日志诊断" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
}
.chart {
  height: 320px;
}
</style>
