// 주요 도시 정보 데이터

export const cities = [
  // 대한민국 주요 도시
  {
    id: 'city_01',
    apiName: 'Seoul,KR',
    name: '서울',
    detailName: '서울특별시 중구',
  },
  {
    id: 'city_02',
    apiName: 'Suwon,KR',
    name: '수원',
    detailName: '경기도 수원시 팔달구',
  },
  {
    id: 'city_03',
    apiName: 'Busan,KR',
    name: '부산',
    detailName: '부산광역시 연제구',
  },
  {
    id: 'city_04',
    apiName: 'Incheon,KR',
    name: '인천',
    detailName: '인천광역시 남동구',
  },
  {
    id: 'city_05',
    apiName: 'Daegu,KR',
    name: '대구',
    detailName: '대구광역시 중구',
  },
  {
    id: 'city_06',
    apiName: 'Daejeon,KR',
    name: '대전',
    detailName: '대전광역시 서구',
  },
  {
    id: 'city_07',
    apiName: 'Gwangju,KR',
    name: '광주',
    detailName: '광주광역시 서구',
  },
  {
    id: 'city_08',
    apiName: 'Ulsan,KR',
    name: '울산',
    detailName: '울산광역시 남구',
  },
  {
    id: 'city_09',
    apiName: 'Sejong,KR',
    name: '세종',
    detailName: '세종특별자치시',
  },
  {
    id: 'city_10',
    apiName: 'Jeju City,KR',
    name: '제주',
    detailName: '제주특별자치도 제주시',
  },

  // 세계 주요 도시
  {
    id: 'city_11',
    apiName: 'Tokyo,JP',
    name: '도쿄',
    detailName: '일본 도쿄',
  },
  {
    id: 'city_12',
    apiName: 'Beijing,CN',
    name: '베이징',
    detailName: '중국 베이징',
  },
  {
    id: 'city_13',
    apiName: 'Singapore,SG',
    name: '싱가포르',
    detailName: '싱가포르',
  },
  {
    id: 'city_14',
    apiName: 'Bangkok,TH',
    name: '방콕',
    detailName: '태국 방콕',
  },
  {
    id: 'city_15',
    apiName: 'London,GB',
    name: '런던',
    detailName: '영국 런던',
  },
  {
    id: 'city_16',
    apiName: 'Paris,FR',
    name: '파리',
    detailName: '프랑스 파리',
  },
  {
    id: 'city_17',
    apiName: 'New York,US',
    name: '뉴욕',
    detailName: '미국 뉴욕',
  },
  {
    id: 'city_18',
    apiName: 'Los Angeles,US',
    name: '로스앤젤레스',
    detailName: '미국 로스앤젤레스',
  },
  {
    id: 'city_19',
    apiName: 'Sydney,AU',
    name: '시드니',
    detailName: '호주 시드니',
  },
  {
    id: 'city_20',
    apiName: 'Dubai,AE',
    name: '두바이',
    detailName: '아랍에미리트 두바이',
  },
]

export const cityMap = Object.fromEntries(cities.map((city) => [city.id, city]))
