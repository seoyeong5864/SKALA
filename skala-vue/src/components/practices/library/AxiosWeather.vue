<script setup>
import {ref} from 'vue'
import axios from 'axios'

const weatherData = ref(null)
const isLoading = ref(false)

const handleFetchWeather = async()=>{
    isLoading.value = true
    
    // API KEY는 env 파일에서 관리
    const URL = `https://api.openweathermap.org/data/2.5/weather?lat=37.4096152&lon=127.095017&appid=${import.meta.env.VITE_OPENWEATHER_KEY}&units=metric&lang=kr`

    try{
        const response = await axios.get(URL)

        console.log('Axios 통신 응답 전체 객체:', response)
        console.log('백엔드가 준 핵심 날씨 데이터(JSON):', response.data)
        weatherData.value = response.data
    } catch(error){
        console.error('Axios 통신 중 에러 발생:', error)
    } finally{
        isLoading.value = false
    }
}
</script>

<template>
  <div class="practice-section">
    <h2>⚡ Axios 통신 검증</h2>
    <button @click="handleFetchWeather" :disabled="isLoading">
      {{ isLoading ? '데이터 로딩 중...' : '실시간 날씨 데이터 당겨오기' }}
    </button>
    <div v-if="weatherData" class="result-card">
      <p>
        📍 위치: <strong>{{ weatherData.name }}</strong>
      </p>
      <p>
        🌡️ 현재 기온: <strong>{{ weatherData.main.temp }}°C</strong> (정상 섭씨 변환 완료)
      </p>
      <p>
        ☁️ 날씨 상태: <strong>{{ weatherData.weather[0].description }}</strong>
      </p>
      <p>
        💧 습도: <strong>{{ weatherData.main.humidity }}%</strong>
      </p>
    </div>
    <div v-else>
      <p>아직 가져온 데이터가 없습니다. 버튼을 눌러 통신을 가동하세요.</p>
    </div>
  </div>
</template>