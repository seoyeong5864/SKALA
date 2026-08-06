# ============================
# 작성자: 판교_8반_박서영
# 작성목적: 데이터분석을 위한 Python 심화실습1
# 작성일: 2026.08.06
# ============================

import sys
from collections import Counter, defaultdict


sales = [
    {"region": "서울", "category": "전자", "amount": 1500, "month": "2024-01"},
    {"region": "부산", "category": "의류", "amount": 800, "month": "2024-01"},
    {"region": "서울", "category": "의류", "amount": 1200, "month": "2024-02"},
    {"region": "대구", "category": "전자", "amount": 950, "month": "2024-01"},
    {"region": "서울", "category": "전자", "amount": 2100, "month": "2024-02"},
    {"region": "부산", "category": "전자", "amount": 650, "month": "2024-02"},
    {"region": "대구", "category": "의류", "amount": 1100, "month": "2024-02"},
    {"region": "인천", "category": "전자", "amount": 1350, "month": "2024-01"},
    {"region": "광주", "category": "의류", "amount": 720, "month": "2024-01"},
    {"region": "대전", "category": "전자", "amount": 1100, "month": "2024-03"},
    {"region": "울산", "category": "의류", "amount": 890, "month": "2024-02"},
    {"region": "세종", "category": "전자", "amount": 1400, "month": "2024-03"},
    {"region": "서울", "category": "식품", "amount": 450, "month": "2024-01"},
    {"region": "부산", "category": "식품", "amount": 380, "month": "2024-03"},
    {"region": "인천", "category": "의류", "amount": 950, "month": "2024-02"},
    {"region": "대구", "category": "식품", "amount": 510, "month": "2024-04"},
    {"region": "광주", "category": "전자", "amount": 1250, "month": "2024-02"},
    {"region": "대전", "category": "식품", "amount": 420, "month": "2024-01"},
    {"region": "울산", "category": "전자", "amount": 1750, "month": "2024-03"},
    {"region": "세종", "category": "의류", "amount": 680, "month": "2024-01"},
    {"region": "서울", "category": "전자", "amount": 1850, "month": "2024-03"},
    {"region": "부산", "category": "의류", "amount": 1050, "month": "2024-04"},
    {"region": "인천", "category": "식품", "amount": 620, "month": "2024-03"},
    {"region": "대구", "category": "전자", "amount": 1420, "month": "2024-03"},
    {"region": "광주", "category": "식품", "amount": 310, "month": "2024-04"},
    {"region": "대전", "category": "의류", "amount": 870, "month": "2024-02"},
    {"region": "울산", "category": "식품", "amount": 490, "month": "2024-01"},
    {"region": "세종", "category": "식품", "amount": 530, "month": "2024-02"},
    {"region": "서울", "category": "의류", "amount": 1600, "month": "2024-04"},
    {"region": "부산", "category": "전자", "amount": 920, "month": "2024-02"},
    {"region": "인천", "category": "전자", "amount": 2200, "month": "2024-04"},
    {"region": "대구", "category": "의류", "amount": 780, "month": "2024-01"},
    {"region": "광주", "category": "전자", "amount": 1050, "month": "2024-03"},
    {"region": "대전", "category": "의류", "amount": 1150, "month": "2024-04"},
    {"region": "울산", "category": "전자", "amount": 1300, "month": "2024-04"},
    {"region": "세종", "category": "전자", "amount": 1650, "month": "2024-04"},
    {"region": "서울", "category": "식품", "amount": 720, "month": "2024-02"},
    {"region": "부산", "category": "식품", "amount": 540, "month": "2024-04"},
    {"region": "인천", "category": "의류", "amount": 1300, "month": "2024-01"},
    {"region": "대구", "category": "전자", "amount": 1150, "month": "2024-04"},
    {"region": "광주", "category": "의류", "amount": 910, "month": "2024-03"},
    {"region": "대전", "category": "식품", "amount": 390, "month": "2024-02"},
    {"region": "울산", "category": "의류", "amount": 620, "month": "2024-03"},
    {"region": "세종", "category": "의류", "amount": 840, "month": "2024-03"},
    {"region": "서울", "category": "전자", "amount": 2500, "month": "2024-04"},
    {"region": "부산", "category": "전자", "amount": 1100, "month": "2024-01"},
    {"region": "인천", "category": "식품", "amount": 480, "month": "2024-04"},
    {"region": "대구", "category": "식품", "amount": 630, "month": "2024-02"},
    {"region": "광주", "category": "식품", "amount": 420, "month": "2024-01"},
    {"region": "대전", "category": "전자", "amount": 1480, "month": "2024-01"},
    {"region": "울산", "category": "식품", "amount": 510, "month": "2024-04"},
    {"region": "세종", "category": "식품", "amount": 600, "month": "2024-04"},
    {"region": "서울", "category": "의류", "amount": 1420, "month": "2024-03"},
    {"region": "부산", "category": "의류", "amount": 930, "month": "2024-03"},
    {"region": "인천", "category": "전자", "amount": 1600, "month": "2024-02"},
    {"region": "대구", "category": "의류", "amount": 1250, "month": "2024-03"},
    {"region": "광주", "category": "전자", "amount": 1380, "month": "2024-04"},
    {"region": "대전", "category": "의류", "amount": 790, "month": "2024-03"},
    {"region": "울산", "category": "전자", "amount": 1520, "month": "2024-02"},
    {"region": "세종", "category": "전자", "amount": 1200, "month": "2024-01"},
    {"region": "서울", "category": "식품", "amount": 580, "month": "2024-04"},
    {"region": "부산", "category": "전자", "amount": 1250, "month": "2024-04"},
    {"region": "인천", "category": "의류", "amount": 1100, "month": "2024-04"},
    {"region": "대구", "category": "전자", "amount": 1050, "month": "2024-02"},
    {"region": "광주", "category": "의류", "amount": 850, "month": "2024-02"},
    {"region": "대전", "category": "전자", "amount": 980, "month": "2024-04"},
    {"region": "울산", "category": "의류", "amount": 740, "month": "2024-01"},
    {"region": "세종", "category": "의류", "amount": 920, "month": "2024-04"},
    {"region": "서울", "category": "의류", "amount": 1350, "month": "2024-01"},
    {"region": "부산", "category": "식품", "amount": 410, "month": "2024-02"},
    {"region": "인천", "category": "전자", "amount": 1750, "month": "2024-03"},
    {"region": "대구", "category": "의류", "amount": 990, "month": "2024-04"},
    {"region": "광주", "category": "식품", "amount": 500, "month": "2024-02"},
    {"region": "대전", "category": "식품", "amount": 460, "month": "2024-04"},
    {"region": "울산", "category": "전자", "amount": 1100, "month": "2024-01"},
    {"region": "세종", "category": "식품", "amount": 370, "month": "2024-02"},
    {"region": "서울", "category": "전자", "amount": 2200, "month": "2024-01"},
    {"region": "부산", "category": "의류", "amount": 1150, "month": "2024-02"},
    {"region": "인천", "category": "식품", "amount": 530, "month": "2024-01"},
    {"region": "대구", "category": "전자", "amount": 1300, "month": "2024-03"},
    {"region": "광주", "category": "의류", "amount": 690, "month": "2024-04"},
    {"region": "대전", "category": "전자", "amount": 1250, "month": "2024-02"},
    {"region": "울산", "category": "의류", "amount": 820, "month": "2024-04"},
    {"region": "세종", "category": "전자", "amount": 1500, "month": "2024-02"},
    {"region": "서울", "category": "식품", "amount": 640, "month": "2024-03"},
    {"region": "부산", "category": "전자", "amount": 880, "month": "2024-03"},
    {"region": "인천", "category": "의류", "amount": 1200, "month": "2024-03"},
    {"region": "대구", "category": "식품", "amount": 480, "month": "2024-03"},
    {"region": "광주", "category": "전자", "amount": 1150, "month": "2024-01"},
    {"region": "대전", "category": "의류", "amount": 930, "month": "2024-01"},
    {"region": "울산", "category": "식품", "amount": 360, "month": "2024-02"},
    {"region": "세종", "category": "의류", "amount": 710, "month": "2024-02"},
    {"region": "서울", "category": "전자", "amount": 1950, "month": "2024-02"},
    {"region": "부산", "category": "의류", "amount": 870, "month": "2024-04"},
    {"region": "인천", "category": "전자", "amount": 1450, "month": "2024-02"},
    {"region": "대구", "category": "의류", "amount": 1050, "month": "2024-01"},
    {"region": "광주", "category": "식품", "amount": 390, "month": "2024-03"},
    {"region": "대전", "category": "전자", "amount": 1320, "month": "2024-03"},
    {"region": "울산", "category": "전자", "amount": 1600, "month": "2024-04"},
    {"region": "세종", "category": "식품", "amount": 420, "month": "2024-01"}
]

# 1. 리스트/딕셔너리 컴프리헨션
# - amount >= 1000인 거래만 필터링하고
# - 지열별 총매출 dict를 컴프리헨션으로 계산

# amount >= 1000 필터링
filtered_sales = [ sale for sale in sales if sale["amount"] >= 1000]

# 지역별 총매출
region_total = {
    region: sum(sale["amount"] for sale in filtered_sales if sale["region"] == region)
    for region in set(sale["region"] for sale in filtered_sales)
}

print(region_total)


# 2. Counter + defaultdict
# - Counter로 지역별 거래 건수
# - defaultdict로 카테고리별 amount 리스트

# 지역별 거래 건수
region_count = Counter(sale["region"] for sale in sales)

print(region_count)

# 카테고리별 amount
category_amounts = defaultdict(int)

for sale in sales:
    category_amounts[sale["category"]] += sale["amount"]

print(dict(category_amounts))


# 3. 제너레이터 - 메모리 비교
# - amount > 1000인 행만 yield 하는 제너레이터 작성
# - 리스트 버전과 메모리 크기 비교

# 제너레이터 생성
def amount_generator(sales):
    for sale in sales:
        if sale["amount"] > 1000:
            yield sale

# 리스트 버전
list_result = [
    sale for sale in sales
    if sale["amount"] > 1000
]

# 제너레이터 버전
generator_result = (
    sale for sale in sales
    if sale["amount"] > 1000
)

# 메모리 크기 비교
print("리스트 메모리:", sys.getsizeof(list_result), "bytes")
print("제너레이터 메모리:", sys.getsizeof(generator_result), "bytes")



# ### 4. 종합 - 월별 카테고리 메출 집계
# - sales 데이터를 month와 category 기준으로 그룹핑해 총매출 dict 완성 (컴프리헨션 + defaultdict)

grouped_sales = defaultdict(lambda: defaultdict(int))

for sale in sales:
    month = sale["month"]
    category = sale["category"]
    grouped_sales[month][category] += sale["amount"]

total_sales = {
   month: {
        category : amount
        for category, amount in category_sales.items()
   }
   for month, category_sales in grouped_sales.items()
}

print(total_sales)
