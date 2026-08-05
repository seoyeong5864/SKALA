export const normalizeWeatherStatus = (weatherId) => {
  if (weatherId >= 200 && weatherId < 300) return '천둥번개'
  if (weatherId >= 300 && weatherId < 400) return '이슬비'
  if (weatherId >= 500 && weatherId < 600) return '비'
  if (weatherId >= 600 && weatherId < 700) return '눈'
  if (weatherId >= 700 && weatherId < 800) return '안개'
  if (weatherId === 800) return '맑음'
  if (weatherId === 801) return '구름 조금'
  if (weatherId === 802) return '구름 많음'
  if (weatherId === 803) return '흐림'
  if (weatherId === 804) return '매우 흐림'

  return '날씨 정보 없음'
}
