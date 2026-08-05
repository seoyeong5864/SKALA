<script setup>
import { computed } from 'vue'
import BaseDashboardCard from './BaseDashboardCard.vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  searchQuery: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['update-search-query'])

const searchQueryModel = computed({
  get: () => props.searchQuery,
  set: (value) => emit('update-search-query', value),
})
</script>

<template>
  <BaseDashboardCard class="search-box">
    <template #header>
      <h2>🔎 도시 검색</h2>
    </template>

    <!-- <input
      v-model.trim.lazy="searchQueryModel"
      type="search"
      placeholder="도시 이름을 입력해주세요."
    /> -->
    <el-input
      v-model.trim.lazy="searchQueryModel"
      placeholder="도시 이름을 입력해주세요"
      class="search-input"
      :prefix-icon="Search"
    >
    </el-input>
    <p>검색 중인 도시: {{ searchQuery }}</p>
  </BaseDashboardCard>
</template>

<style scoped>
.search-box {
  margin: 24px 0;
  padding: 20px;
}

.search-box input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  font-size: 16px;
  outline: none;
}

.search-box input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgb(37 99 235 / 15%);
}

.search-box h2 {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
}
</style>
