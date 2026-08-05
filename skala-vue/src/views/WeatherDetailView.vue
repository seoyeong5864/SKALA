<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useConfigStore } from '@/stores/configStore'
import axios from 'axios'
import BaseDashboardCard from '@/components/exercise/BaseDashboardCard.vue'

const route = useRoute()
const router = useRouter()
const configStore = useConfigStore()

const cityData = ref(null)
const isLoading = ref(true)

// 실시간 날씨 API 연동 준비
// 영어 - 한글 매핑
const cityMapping = {
  city_01: { english: 'Seoul', korean: '대한민국 서울특별시' },
  city_02: { english: 'Suwon', korean: '경기도 수원시 영통구' },
  city_03: { english: 'Busan', korean: '부산광역시 해운대구' },
}

onMounted(async () => {
  const cityId = route.params.cityId
  const cityInfo = cityMapping[cityId]

  if (!cityInfo) {
    console.error('해당 지역의 상세 데이터 정보가 존재하지 않습니다.')
    isLoading.value = false
    return
  }

  try {
    // API KEY와 URL은 env 파일에서 관리
    const API_KEY = import.meta.env.VITE_OPENWEATHER_KEY
    const BASE_URL = import.meta.env.VITE_OPENWEATHER_URL

    const response = await axios.get(
      `${BASE_URL}?q=${cityInfo.english}&appid=${API_KEY}&units=metric&lang=kr`,
    )
    const data = response.data

    cityData.value = {
      name: cityInfo.korean,
      temp: data.main.temp,
      status: data.weather[0].description,
      humidity: `${data.main.humidity}%`,
      wind: `${data.wind.speed}m/s`,
    }
  } catch (error) {
    console.error('🔴 날씨 API 연동 실패:', error)
  } finally {
    isLoading.value = false
  }
})

// const mockDetails = {
//   city_01: {
//     name: '대한민국 서울특별시',
//     temp: 28,
//     status: '맑음',
//     humidity: '55%',
//     wind: '2.5m/s',
//   },
//   city_02: {
//     name: '경기도 수원시 영통구',
//     temp: 24,
//     status: '비',
//     humidity: '85%',
//     wind: '4.1m/s',
//   },
//   city_03: {
//     name: '부산광역시 해운대구',
//     temp: 26,
//     status: '구름',
//     humidity: '65%',
//     wind: '5.0m/s',
//   },
// }

// onMounted(() => {
//   const id = route.params.cityId
//   if (mockDetails[id]) {
//     cityData.value = mockDetails[id]
//   }
// })

// 기온 단위 변경
const displayTemp = computed(() => {
  if (!cityData.value) return 0
  const rawTemp = cityData.value.temp
  if (configStore.unit === 'fahrenheit') {
    return Math.round((rawTemp * 9) / 5 + 32)
  }
  return rawTemp
})
</script>

<template>
  <main class="detail-container">
    <div v-if="isLoading" class="state-message">날씨 정보를 불러오는 중입니다...</div>

    <template v-else-if="cityData">
      <header class="detail-header">
        <p>상세 기상 관측 정보</p>
        <h1>{{ cityData.name }}</h1>
      </header>

      <BaseDashboardCard class="info-card">
        <section class="current-weather">
          <p class="info-label">현재 기온</p>
          <p class="temperature">{{ displayTemp }}{{ configStore.unitSymbol }}</p>
          <p class="weather-status">{{ cityData.status }}</p>
        </section>

        <dl class="weather-metrics">
          <div>
            <dt>대기 습도</dt>
            <dd>{{ cityData.humidity }}</dd>
          </div>
          <div>
            <dt>현재 풍속</dt>
            <dd>{{ cityData.wind }}</dd>
          </div>
        </dl>
      </BaseDashboardCard>
    </template>

    <div v-else class="state-message">해당 지역의 상세 데이터 정보가 존재하지 않습니다.</div>

    <!-- <button class="back-btn" @click="router.push('/')">← 메인 대시보드로 돌아가기</button> -->
    <div class="back-btn">
      <el-button @click="router.push('/')" round color="#2563eb">← 대시보드로 돌아가기</el-button>
    </div>
  </main>
</template>

<style scoped>
.detail-container {
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header p {
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.detail-header h1 {
  margin-top: 4px;
  color: #0f172a;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.3;
}

.info-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
}

.current-weather {
  display: flex;
  min-height: 240px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  border-right: 1px solid #e2e8f0;
  text-align: center;
}

.info-label,
.weather-metrics dt {
  color: #64748b;
  font-size: 14px;
  font-weight: 700;
}

.temperature {
  margin: 8px 0 0;
  color: #2563eb;
  font-size: 50px;
  font-weight: 700;
}

.weather-status {
  margin-top: 8px;
  color: #1e293b;
  font-size: 18px;
}

.weather-metrics {
  display: grid;
  grid-template-rows: 1fr 1fr;
  margin: 0;
}

.weather-metrics > div {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 32px;
}

.weather-metrics > div + div {
  border-top: 1px solid #e2e8f0;
}

.weather-metrics dd {
  margin-top: 4px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
}

.state-message {
  padding: 48px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  color: #64748b;
  text-align: center;
}

/* .back-btn {
  margin-top: 20px;
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background-color: #2563eb;
  color: white;
  font-weight: 600;
  cursor: pointer;
} */

.back-btn {
  margin-top: 20px;
}

/* .back-btn:hover {
  background-color: #1d4ed8;
} */

@media (max-width: 640px) {
  .detail-header h1 {
    font-size: 27px;
  }

  .info-card {
    grid-template-columns: 1fr;
  }

  .current-weather {
    min-height: 210px;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
  }
}
</style>
