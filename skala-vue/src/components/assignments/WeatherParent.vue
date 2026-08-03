<script setup>
import { computed, ref, watch, watchEffect } from 'vue'
import SearchBar from './SearchBar.vue'
import WeatherCard from './WeatherCard.vue'

const searchText = ref('')
const selectedCityInfo = ref('도시를 선택해주세요.')

const weatherList = ref([
  { id: 'city_01', name: '서울', temp: 28, status: '맑음' },
  { id: 'city_02', name: '수원', temp: 24, status: '비' },
  { id: 'city_03', name: '부산', temp: 26, status: '구름' },
])

const filteredWeatherList = computed(() => {
  const text = searchText.value.trim()
  if (!text) return weatherList.value

  return weatherList.value.filter((weather) => weather.name.includes(text))
})

const handleSearchTextUpdate = (value) => {
  searchText.value = value
}

const handleSelectedCard = (weather) => {
  selectedCityInfo.value = `${weather.name}이 선택되었습니다.`
}

const handleShowDetail = (weather) => {
  window.alert(`${weather.name}의 현재 날씨는 [${weather.status}] 상태입니다.`)
}

// 반응형 변수 변화 감시
watch(selectedCityInfo, (newInfo) => {
  console.log(`[watch 감지] 상태바 문구가 업데이트 되었습니다 -> "${newInfo}" `)
})

watchEffect(() => {
  console.log(
    `[watchEffect 자동호출] 현재 검색어 "${searchText.value}" 에 매칭되는 API 데이터를 필터링합니다. `,
  )
})
</script>

<template>
  <main class="weather-parent">
    <h1>날씨 Mockup</h1>

    <SearchBar :search-text="searchText" @update-search-text="handleSearchTextUpdate" />

    <div>
      <section class="card-list">
        <h3>🏙️ 지역별 날씨 현황</h3>
        <WeatherCard
          v-for="weather in filteredWeatherList"
          :key="weather.id"
          :weather="weather"
          @select-card="handleSelectedCard"
          @show-detail="handleShowDetail"
        />
      </section>
      <p v-if="searchText && filteredWeatherList.length === 0">
        검색어와 일치하는 도시가 없습니다.
      </p>
    </div>

    <div class="status-bar">
      {{ selectedCityInfo }}
    </div>
  </main>
</template>

<style scoped>
.weather-parent {
  width: 100%;
}

.card-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background-color: #ffffff;
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 56px;
  margin-top: 24px;
  padding: 12px 20px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background-color: #f8fafc;
  text-align: center;
}
</style>
