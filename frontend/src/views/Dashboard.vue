<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { LineChart } from 'echarts/charts'
import { GridComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { init, use, type ECharts } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'

use([LineChart, GridComponent, TitleComponent, TooltipComponent, CanvasRenderer])

const stats = ref([
  { label: '任务总数', value: 0 },
  { label: '运行中', value: 0 },
  { label: '成功', value: 0 },
  { label: '失败', value: 0 }
])

const chartRef = ref<HTMLDivElement>()
let chart: ECharts | null = null

function renderChart() {
  if (!chartRef.value) return
  chart = init(chartRef.value)
  chart.setOption({
    title: { text: '示例 loss 曲线（占位数据）', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1', '2', '3', '4', '5', '6', '7', '8'] },
    yAxis: { type: 'value' },
    series: [
      { name: 'loss', type: 'line', smooth: true, data: [1.0, 0.82, 0.71, 0.6, 0.52, 0.46, 0.4, 0.36] }
    ]
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<template>
  <div>
    <h3 class="page-title">仪表盘</h3>
    <el-row :gutter="16">
      <el-col v-for="item in stats" :key="item.label" :span="6">
        <el-card shadow="hover">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never">
      <div ref="chartRef" class="chart"></div>
    </el-card>

    <el-alert
      class="tip"
      title="第一阶段为骨架页面，统计与曲线均为占位数据，第二阶段接入真实接口。"
      type="info"
      :closable="false"
    />
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 16px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
}
.stat-label {
  color: #888;
  margin-top: 4px;
}
.chart-card {
  margin-top: 16px;
}
.chart {
  height: 320px;
}
.tip {
  margin-top: 16px;
}
</style>
