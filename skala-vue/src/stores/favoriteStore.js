import { ref } from 'vue'
import { defineStore } from 'pinia'
import { cityMap } from '@/data/cities'

const STORAGE_KEY = 'weather-dashboard-favorite-city-ids'

const loadFavoriteCityIds = () => {
  try {
    const storedValue = localStorage.getItem(STORAGE_KEY)
    if (!storedValue) return []

    const cityIds = JSON.parse(storedValue)
    if (!Array.isArray(cityIds)) return []

    return cityIds.filter((cityId) => typeof cityId === 'string' && cityMap[cityId])
  } catch {
    return []
  }
}

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteCityIds = ref(loadFavoriteCityIds())

  const saveFavoriteCityIds = () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(favoriteCityIds.value))
    } catch {
      // 브라우저 저장소를 사용할 수 없어도 현재 화면의 즐겨찾기 상태는 유지합니다.
    }
  }

  const isFavorite = (cityId) => favoriteCityIds.value.includes(cityId)

  const toggleFavorite = (cityId) => {
    if (isFavorite(cityId)) {
      favoriteCityIds.value = favoriteCityIds.value.filter((id) => id !== cityId)
    } else {
      favoriteCityIds.value = [...favoriteCityIds.value, cityId]
    }

    saveFavoriteCityIds()
    return isFavorite(cityId)
  }

  return { favoriteCityIds, isFavorite, toggleFavorite }
})
