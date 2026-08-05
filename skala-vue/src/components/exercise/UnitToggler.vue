<script setup>
import { useConfigStore } from '@/stores/configStore'

const configStore = useConfigStore()

const selectUnit = (unit) => {
  if (configStore.unit !== unit) {
    configStore.toggleUnit()
  }
}
</script>

<template>
  <div class="unit-toggle" role="group" aria-label="날씨 단위 변경">
    <button
      type="button"
      class="unit-option"
      :class="{ active: configStore.unit === 'celsius' }"
      :aria-pressed="configStore.unit === 'celsius'"
      @click="selectUnit('celsius')"
    >
      °C
    </button>
    <button
      type="button"
      class="unit-option"
      :class="{ active: configStore.unit === 'fahrenheit' }"
      :aria-pressed="configStore.unit === 'fahrenheit'"
      @click="selectUnit('fahrenheit')"
    >
      °F
    </button>
  </div>
</template>

<style scoped>
.unit-toggle {
  display: inline-grid;
  overflow: hidden;
  grid-template-columns: repeat(2, 1fr);
  margin-left: auto;
  padding: 2px;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background-color: #e5e7eb;
}

.unit-option {
  width: 46px;
  height: 30px;
  padding: 0;
  border: 1px solid transparent;
  border-radius: 999px;
  background-color: transparent;
  color: #64748b;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}

.unit-option.active {
  background-color: #ffffff;
  color: #0f172a;
  font-weight: 700;
}

.unit-option:focus-visible {
  outline: 2px solid rgb(37, 99, 235);
  outline-offset: 2px;
}
</style>
