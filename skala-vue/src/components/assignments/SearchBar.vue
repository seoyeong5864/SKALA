<script setup>
import BaseDashboardCard from './BaseDashboardCard.vue'

defineProps({
  searchText: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['update-search-text'])

const handleInput = (event) => {
  emit('update-search-text', event.target.value)
}
</script>

<template>
  <BaseDashboardCard class="search-box">
    <h3>🔎 도시 검색</h3>
    <input
      :value="searchText"
      type="search"
      placeholder="도시 이름을 입력해주세요."
      @input="handleInput"
    />
    <p>검색 중인 도시: {{ searchText }}</p>
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
</style>
