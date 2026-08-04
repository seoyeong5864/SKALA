<template>
  <main class="about-page">
    <header class="about-header">
      <h1>서비스 소개</h1>
      <p>도시별 날씨를 간편하게 검색하고 필요한 기상 정보를 확인할 수 있는 서비스입니다.</p>
    </header>

    <ol class="feature-list">
      <li>
        <span class="index">01</span>
        <div>
          <h2>도시 검색</h2>
          <p>도시 이름을 검색해 원하는 지역의 날씨 정보만 빠르게 확인할 수 있습니다.</p>
        </div>
      </li>
      <li>
        <span class="index">02</span>
        <div>
          <h2>현재 날씨 확인</h2>
          <p>도시별 현재 기온과 맑음, 비, 구름 등의 날씨 상태를 제공합니다.</p>
        </div>
      </li>
      <li>
        <span class="index">03</span>
        <div>
          <h2>온도 라벨 표시</h2>
          <p>기온을 더움, 보통, 선선함의 세 단계로 구분해 직관적으로 표시합니다.</p>
        </div>
      </li>
      <li>
        <span class="index">04</span>
        <div>
          <h2>상세 기상 정보</h2>
          <p>상세 페이지에서 선택한 도시의 기온, 날씨, 습도와 풍속을 확인할 수 있습니다.</p>
        </div>
      </li>
    </ol>
  </main>
</template>

<style scoped>
.about-page {
  width: 100%;
}

.about-header {
  padding-bottom: 28px;
  border-bottom: 2px solid #2563eb;
}

.about-header span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.about-header h1 {
  margin-top: 8px;
  color: #0f172a;
  font-size: 36px;
  font-weight: 800;
}

.about-header p {
  margin-top: 12px;
  color: #64748b;
}

.feature-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.feature-list li {
  display: flex;
  padding: 28px 0;
  gap: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.index {
  color: #2563eb;
  font-size: 18px;
  font-weight: 800;
}

.feature-list h2 {
  color: #1e293b;
  font-size: 19px;
  font-weight: 700;
}

.feature-list p {
  margin-top: 6px;
  color: #64748b;
}

.notice {
  margin-top: 28px;
  padding: 14px 16px;
  border-radius: 8px;
  background-color: #f1f5f9;
  color: #64748b;
  font-size: 13px;
}
</style>
