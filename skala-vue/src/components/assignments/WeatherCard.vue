<script setup>
import BaseDashboardCard from './BaseDashboardCard.vue'

const props = defineProps({
  weather: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['select-card', 'show-detail'])

const selectCard = () => {
  emit('select-card', props.weather)
}

const showDetail = () => {
  emit('show-detail', props.weather)
}
</script>

<template>
  <BaseDashboardCard class="weather-card" @click="selectCard">
    <h2>{{ weather.name }}({{ weather.status }})</h2>
    <p class="temperature">{{ weather.temp }}℃</p>
    <p v-if="weather.temp >= 25" class="badge hot">🔥 더움 (25도 이상)</p>
    <p v-else class="badge cool">🍃 선선함(25도 미만)</p>
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

.weather-card h2 {
  margin: 0;
  font-size: 20px;
}

.temperature {
  margin: 14px 0 6px;
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
