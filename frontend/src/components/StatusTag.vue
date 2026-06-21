<script setup lang="ts">
import { computed } from 'vue'

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

const props = defineProps<{ status: string }>()

const typeMap: Record<string, TagType> = {
  PENDING: 'info',
  RUNNING: 'warning',
  SUCCESS: 'success',
  FAILED: 'danger',
  CANCELLED: 'info'
}

const tagType = computed<TagType>(() => typeMap[props.status] ?? 'info')
</script>

<template>
  <el-tag :type="tagType" disable-transitions>{{ status }}</el-tag>
</template>
