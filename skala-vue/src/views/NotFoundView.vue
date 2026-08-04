<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <h1>404 Not Found</h1>
    <p>요청하신 페이지를 찾을 수 없습니다.</p>
    <button class="back-btn" @click="router.push('/')">← 메인 대시보드로 돌아가기</button>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  width: 100%;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

h1 {
  font-size: 50px;
  font-weight: 700;
}

.back-btn {
  margin-top: 20px;
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background-color: #2563eb;
  color: white;
  font-weight: 600;
  cursor: pointer;
}
</style>
