<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

import BaseDashboardCard from '../components/exercise/BaseDashboardCard.vue'
import SearchBar from '../components/exercise/SearchBar.vue'
import WeatherCard from '../components/exercise/WeatherCard.vue'

const router = useRouter()
const route = useRoute()

// 실시간 날씨 API 연동 준비
const searchQuery = ref('')
const selectedCityInfo = ref('카드를 클릭하거나 검색해보세요.')
const isLoading = ref(false)
const weatherList = ref([])

// API KEY와 URL은 env 파일에서 관리
const API_KEY = import.meta.env.VITE_OPENWEATHER_KEY
const BASE_URL = import.meta.env.VITE_OPENWEATHER_URL

// 날씨 데이터 가져오기
const fetchRealTimeWeather = async () => {
  isLoading.value = true
  try {
    const [seoulRes, suwonRes, busanRes] = await Promise.all([
      axios.get(`${BASE_URL}?q=Seoul&appid=${API_KEY}&units=metric&lang=kr`),
      axios.get(`${BASE_URL}?q=Suwon&appid=${API_KEY}&units=metric&lang=kr`),
      axios.get(`${BASE_URL}?q=Busan&appid=${API_KEY}&units=metric&lang=kr`),
    ])

    weatherList.value = [
      {
        id: 'city_01',
        name: '서울',
        temp: seoulRes.data.main.temp,
        status: seoulRes.data.weather[0].description,
      },
      {
        id: 'city_02',
        name: '수원',
        temp: suwonRes.data.main.temp,
        status: suwonRes.data.weather[0].description,
      },
      {
        id: 'city_03',
        name: '부산',
        temp: busanRes.data.main.temp,
        status: busanRes.data.weather[0].description,
      },
    ]
    console.log('🟢 [API 통신 완료] 메인 대시보드 실시간 기상 정보 동기화:', weatherList.value)
  } catch (error) {
    console.error('🔴 날씨 API 연동 실패:', error)
  } finally {
    isLoading.value = false
  }
}

// 초기 마운트시 날씨 데이터 가져오기
onMounted(() => {
  if (route.query.search) {
    searchQuery.value = route.query.search
  }
  fetchRealTimeWeather()
})

const filteredWeatherList = computed(() => {
  const text = searchQuery.value.trim()
  if (!text) return weatherList.value

  return weatherList.value.filter((weather) => weather.name.includes(text))
})

const handleSearchQueryUpdate = (value) => {
  searchQuery.value = value
}

const handleSelectedCard = (weather) => {
  selectedCityInfo.value = `${weather.name}이 선택되었습니다.`
}

const handleShowDetail = (weather) => {
  router.push(`/weather/${weather.id}`)
}

// // 반응형 변수 변화 감시
// watch(selectedCityInfo, (newInfo) => {
//   console.log(`[watch 감지] 상태바 문구가 업데이트 되었습니다 -> "${newInfo}" `)
// })

// watchEffect(() => {
//   console.log(
//     `[watchEffect 자동호출] 현재 검색어 "${searchQuery.value}" 에 매칭되는 API 데이터를 필터링합니다. `,
//   )
// })
</script>

<template>
  <main class="weather-parent">
    <SearchBar :search-query="searchQuery" @update-search-query="handleSearchQueryUpdate" />

    <div>
      <BaseDashboardCard class="card-list">
        <h3>🏙️ 지역별 날씨 현황</h3>
        <p v-if="isLoading" class="loading-message">⛅ 날씨 정보를 불러오는 중입니다...</p>
        <template v-else>
          <WeatherCard
            v-for="weather in filteredWeatherList"
            :key="weather.id"
            :weather="weather"
            @select-card="handleSelectedCard"
            @show-detail="handleShowDetail"
          />
        </template>
      </BaseDashboardCard>
      <p v-if="!isLoading && searchQuery && filteredWeatherList.length === 0">
        검색어와 일치하는 도시가 없습니다.
      </p>
    </div>

    <div class="status-bar">
      {{ selectedCityInfo }}
    </div>

    <!-- <div style="margin-bottom: 36px">
      <h2>API 연결 확인용</h2>
      <AxiosWeather />
    </div> -->
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
}

.loading-message {
  padding: 32px 20px;
  color: #64748b;
  text-align: center;
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 56px;
  margin-top: 24px;
  margin-bottom: 24px;
  padding: 12px 20px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background-color: #f8fafc;
  text-align: center;
}
</style>
