<script setup>
import {useCounterStore} from '@/stores/counter'

const counterStore = useCounterStore()

</script>

<template>
    <h2>Counter Store 활용 실습</h2>
    <p>현재 카운트: {{ counterStore.count }}</p>
    <button @click="counterStore.increment">증가</button>
    <p>2배 연산: {{ counterStore.doubleCount }}</p>
</template>