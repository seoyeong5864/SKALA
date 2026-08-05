<script setup>
import { computed } from 'vue'
import { useConfigStore } from '@/stores/configStore'

import BaseDashboardCard from './BaseDashboardCard.vue'

const props = defineProps({
  weather: {
    type: Object,
    required: true,
  },
  isFavorite: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['select-card', 'show-detail', 'toggle-favorite'])

// 날씨 단위 변경 적용
const configStore = useConfigStore()

const displayTemp = computed(() => {
  const rawTemp = props.weather.temp
  const convertedTemp =
    configStore.unit === 'fahrenheit' ? (rawTemp * 9) / 5 + 32 : rawTemp

  return convertedTemp.toFixed(1)
})

const selectCard = () => {
  emit('select-card', props.weather)
}

const showDetail = () => {
  emit('show-detail', props.weather)
}

const toggleFavorite = () => {
  emit('toggle-favorite', props.weather)
}
</script>

<template>
  <BaseDashboardCard
    class="weather-card"
    :class="{ 'is-favorite': isFavorite }"
    @click="selectCard"
  >
    <template #header>
      <p class="city-name">{{ weather.name }}</p>
    </template>

    <button
      type="button"
      class="favorite-button"
      :class="{ active: isFavorite }"
      :aria-label="isFavorite ? `${weather.name} 즐겨찾기 해제` : `${weather.name} 즐겨찾기 추가`"
      :aria-pressed="isFavorite"
      :title="isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'"
      @click.stop="toggleFavorite"
    >
      {{ isFavorite ? '★' : '☆' }}
    </button>

    <p class="status">{{ weather.status }}</p>
    <p class="temperature">{{ displayTemp }}{{ configStore.unitSymbol }}</p>
    <p v-if="weather.temp >= 25" class="badge hot">🔥 더움</p>
    <p v-else-if="weather.temp >= 20" class="badge normal">🌤️ 보통</p>
    <p v-else class="badge cool">🍃 선선함</p>

    <template #footer>
      <button type="button" class="detail-button" @click.stop="showDetail">상세보기</button>
    </template>
  </BaseDashboardCard>
</template>

<style scoped>
.weather-card {
  position: relative;
  display: flex;
  min-width: 0;
  aspect-ratio: 1 / 1;
  flex-direction: column;
  padding: 80px 20px 20px;
  box-shadow: 0 4px 12px rgb(15 23 42 / 8%);
  text-align: center;
}

.weather-card :deep(.base-dashboard-card__footer) {
  margin-top: auto;
}

.weather-card .city-name {
  font-weight: 700;
  margin: 0;
  font-size: 40px;
  line-height: 1.25;
}

.temperature {
  margin: 4px 0 0;
  color: #2563eb;
  font-size: 44px;
  font-weight: 700;
}

.status {
  margin: 12px 0 0;
  color: #64748b;
}

.favorite-button {
  position: absolute;
  top: 10px;
  right: 12px;
  display: inline-flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background-color: transparent;
  color: #94a3b8;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.favorite-button.active {
  color: #f5e20b;
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
  background-color: #c5cad1;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.detail-button:hover {
  background-color: #2563eb;
}
</style>
