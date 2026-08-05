<script setup>
import { useConfigStore } from '@/stores/configStore'

const configStore = useConfigStore()
</script>

<template>
  <div
    style="
      text-align: center;
      margin-left: auto;
      display: inline-flex;
      align-items: center;
      gap: 8px;
    "
  >
    <span
      >날씨단위: <strong>{{ configStore.unit === 'celsius' ? '섭씨(℃)' : '화씨(℉)' }}</strong></span
    >
    <!-- <button @click="configStore.toggleUnit" class="toggle-btn">단위변경</button> -->
    <el-button @click="configStore.toggleUnit" round color="#2563eb">단위변경</el-button>
  </div>
</template>

<style scoped>
.toggle-btn {
  padding: 6px 10px;
  background-color: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.toggle-btn:hover {
  background-color: #1d4ed8;
}

el-button {
  font-weight: bold;
  background-color: #2563eb;
  color: white;
}
</style>
