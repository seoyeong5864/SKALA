<script setup>
import { computed } from 'vue'
import { useConfigStore } from '@/stores/configStore'

import BaseDashboardCard from './BaseDashboardCard.vue'

const props = defineProps({
  weather: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['select-card', 'show-detail'])

// 날씨 단위 변경 적용
const configStore = useConfigStore()

const displayTemp = computed(() => {
    const rawTemp = props.weather.temp
    if(configStore.unit === 'fahrenheit'){
      return Math.round((rawTemp * 9/5) + 32)
    }
    return rawTemp
})

const selectCard = () => {
  emit('select-card', props.weather)
}

const showDetail = () => {
  emit('show-detail', props.weather)
}
</script>

<template>
  <BaseDashboardCard class="weather-card" @click="selectCard">
    <p class="city-name">{{ weather.name }}</p>
    <p>{{ weather.status }}</p>
    <p class="temperature">{{ displayTemp }}{{ configStore.unitSymbol }}</p>
    <p v-if="weather.temp >= 25" class="badge hot">🔥 더움</p>
    <p v-else-if="weather.temp >= 20" class="badge normal">🌤️ 보통</p>
    <p v-else class="badge cool">🍃 선선함</p>
    <button type="button" class="detail-button" @click.stop="showDetail">상세보기</button>
  </BaseDashboardCard>
</template>

<style scoped>
.weather-card {
  position: relative;
  padding: 56px 20px 20px;
  box-shadow: 0 4px 12px rgb(15 23 42 / 8%);
  text-align: center;
}

.weather-card .city-name {
  font-weight: 700;
  margin: 0;
  font-size: 32px;
}

.temperature {
  margin: 8px 0 0;
  color: #2563eb;
  font-size: 32px;
  font-weight: 700;
}

.status {
  margin: 0;
  color: #64748b;
}

.badge {
  position: absolute;
  top: 16px;
  left: 16px;
  display: inline-block;
  margin: 0;
  padding: 6px 10px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
}

.badge.hot {
  background-color: #ff7676;
}

.badge.normal {
  background-color: #f59e0b;
}

.badge.cool {
  background-color: #00b3ff;
}

.detail-button {
  width: 100%;
  margin-top: 18px;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  background-color: #2563eb;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.detail-button:hover {
  background-color: #1d4ed8;
}
</style>
