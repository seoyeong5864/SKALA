<script setup>
import { RouterLink, RouterView } from 'vue-router'
import UnitToggler from './components/exercise/UnitToggler.vue'
</script>

<template>
  <div>
    <nav class="navbar">
      <div class="nav-links">
        <RouterLink to="/">날씨 대시보드</RouterLink>
        <RouterLink to="/about">서비스 소개</RouterLink>
      </div>
      <UnitToggler />
    </nav>

    <div class="page-content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.navbar {
  display: flex;
  width: 100%;
  max-width: 760px;
  align-items: center;
  justify-content: space-between;
  margin: 2rem auto;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  font-size: 20px;
  text-align: center;
}

.page-content {
  width: 100%;
  max-width: 760px;
  margin: 0 auto;
}

.navbar a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
  color: #64748b;
}

.navbar a:hover {
  background-color: transparent;
}

.navbar a.router-link-exact-active {
  color: #2563eb;
  font-weight: 800;
}

.navbar a:first-of-type {
  border: 0;
}
</style>
