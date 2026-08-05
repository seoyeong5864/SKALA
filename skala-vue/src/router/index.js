import { createRouter, createWebHistory } from 'vue-router'
import WeatherHomeView from '../views/WeatherHomeView.vue'
import { cities } from '@/data/cities'

// const VALID_CITY_IDS = new Set(['city_01', 'city_02', 'city_03'])
const VALID_CITY_IDS = new Set(cities.map((city) => city.id))

// 지연 로딩 적용 > import() 함수 사용
const routes = [
  {
    path: '/',
    name: 'WeatherHome',
    component: WeatherHomeView,
  },
  {
    path: '/about',
    name: 'WeatherAbout',
    component: () => import('../views/WeatherAboutView.vue'),
  },
  {
    path: '/weather/:cityId',
    name: 'WeatherDetail',
    component: () => import('../views/WeatherDetailView.vue'),
  },
  // Catch-all Route 적용 > 미리 정의하지 않은 모든 URL 경로 처리
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 존재하지 않는 도시 상세 경로는 NotFound 페이지로 이동
router.beforeEach((to) => {
  if (to.name !== 'WeatherDetail') return true

  const cityId = Array.isArray(to.params.cityId) ? to.params.cityId[0] : to.params.cityId
  if (VALID_CITY_IDS.has(cityId)) return true

  return {
    name: 'NotFound',
    params: { pathMatch: ['not-found'] },
    replace: true,
  }
})

export default router
