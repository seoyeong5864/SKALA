import { computed, ref, watch } from 'vue'

const normalizeSearchQuery = (value) => {
  if (Array.isArray(value)) return value[0] ?? ''
  return typeof value === 'string' ? value : ''
}

export const useWeatherSearch = (weatherList, initialQuery = '') => {
  const searchQuery = ref(normalizeSearchQuery(initialQuery))

  const filteredWeatherList = computed(() => {
    const text = searchQuery.value.trim()
    if (!text) return weatherList.value

    return weatherList.value.filter((weather) => weather.name.includes(text))
  })

  watch(searchQuery, (newQuery) => {
    const text = newQuery.trim()
    console.log(
      `[watch 감지] 검색어 "${text}"에 일치하는 도시가 ${filteredWeatherList.value.length}개 있습니다.`,
    )
  })

  const updateSearchQuery = (value) => {
    searchQuery.value = normalizeSearchQuery(value)
  }

  return {
    searchQuery,
    filteredWeatherList,
    updateSearchQuery,
  }
}
