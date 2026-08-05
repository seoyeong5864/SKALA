<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import BaseDashboardCard from './BaseDashboardCard.vue'

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

const debouncedSearchQuery = ref(props.searchQuery)
let debounceTimer

// debounce 적용
watch(
  () => props.searchQuery,
  (value) => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      debouncedSearchQuery.value = value
    }, 300)
  },
)

onBeforeUnmount(() => {
  clearTimeout(debounceTimer)
})
</script>

<template>
  <BaseDashboardCard class="search-box">
    <h2>🔎 도시 검색</h2>
    <p class="search-description">우리나라 및 세계 주요 도시의 실시간 날씨를 검색해보세요.</p>
    <!-- lazy 적용 확인 > 이벤트 발생시 갱신 -->
    <!-- (예) 입력창 밖 클릭, 포커스 이동 등 -->
    <!-- <input
      v-model.trim.lazy="searchQueryModel"
      type="search"
      placeholder="도시 이름을 입력해주세요."
    /> -->

    <!-- <el-input
      v-model="searchQueryModel"
      placeholder="도시 이름을 입력해주세요"
      class="search-input"
      :prefix-icon="Search"
    >
    </el-input> -->
    <input
      :value="searchQueryModel"
      type="search"
      placeholder="도시 이름을 입력해주세요."
      @input="$emit('update-search-query', $event.target.value)"
    />
    <p>검색 중인 도시: {{ debouncedSearchQuery }}</p>
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
  margin-bottom: 12px;
}

.search-box h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.search-description {
  margin: 6px 0 6px;
  color: #64748b;
  font-size: 14px;
}
</style>
