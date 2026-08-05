const weatherImageModules = import.meta.glob('/src/assets/weather/*.png', {
  eager: true,
  query: '?url',
  import: 'default',
})

const weatherImages = Object.fromEntries(
  Object.entries(weatherImageModules).map(([path, imageUrl]) => {
    const fileName = path.split('/').pop().replace('.png', '').normalize('NFC')
    return [fileName, imageUrl]
  }),
)

const WEATHER_IMAGE_KEYS = {
  천둥번개: '천둥번개',
  이슬비: '비',
  비: '비',
  눈: '눈',
  안개: '안개',
  맑음: '맑음',
  '구름 조금': '구름조금',
  '구름 많음': '구름많음',
  흐림: '흐림',
  '매우 흐림': '흐림',
}

export const getWeatherImage = (weatherStatus) => {
  const imageKey = WEATHER_IMAGE_KEYS[weatherStatus]
  return weatherImages[imageKey] ?? weatherImages.흐림
}
