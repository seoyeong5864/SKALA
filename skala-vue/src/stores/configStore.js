import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useConfigStore = defineStore('config', () => {

    const unit = ref('celsius')

    const unitSymbol = computed(() => {
        return unit.value === 'celsius' ? '°C' : '°F'
    })

    function toggleUnit() {
        unit.value = unit.value === 'celsius' ? 'fahrenheit' : 'celsius'
    }

    return { unit, unitSymbol, toggleUnit }
})
