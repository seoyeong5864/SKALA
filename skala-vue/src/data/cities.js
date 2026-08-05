// 주요 도시 정보 데이터

export const cities = [
  {
    id: 'city_01',
    apiName: 'Seoul',
    name: '서울',
    detailName: '대한민국 서울특별시 중구',
  },
  {
    id: 'city_02',
    apiName: 'Suwon',
    name: '수원',
    detailName: '경기도 수원시 팔달구',
  },
  {
    id: 'city_03',
    apiName: 'Busan',
    name: '부산',
    detailName: '부산광역시 연제구',
  },
  {
    id: 'city_04',
    apiName: 'Incheon',
    name: '인천',
    detailName: '인천광역시 남동구',
  },
  {
    id: 'city_05',
    apiName: 'Daegu',
    name: '대구',
    detailName: '대구광역시 중구',
  },
  {
    id: 'city_06',
    apiName: 'Daejeon',
    name: '대전',
    detailName: '대전광역시 서구',
  },
  {
    id: 'city_07',
    apiName: 'Gwangju',
    name: '광주',
    detailName: '광주광역시 서구',
  },
  {
    id: 'city_08',
    apiName: 'Ulsan',
    name: '울산',
    detailName: '울산광역시 남구',
  },
  {
    id: 'city_09',
    apiName: 'Sejong',
    name: '세종',
    detailName: '세종특별자치시',
  },
  {
    id: 'city_10',
    apiName: 'Jeju City',
    name: '제주',
    detailName: '제주특별자치도 제주시',
  },
]

export const cityMap = Object.fromEntries(cities.map((city) => [city.id, city]))
