# ============================================================
# 프로그램명: Practice3 (Pandas EDA, Polars Lazy, DuckDB SQL 비교)
# 작성자: 판교_8반_박서영
# 작성일: 2026.08.07
#
# 프로그램 설명:
# sales_100k.csv 데이터를 대상으로 Pandas EDA와 IQR 이상치 제거를 수행한 뒤,
# region·category별 매출 집계를 Pandas / Polars Lazy / DuckDB SQL 세 가지 방식으로 각각 작성하고
# timeit으로 세 도구의 실행 시간을 비교한다.
#
# 변경 내역:
# 2026.08.07 최초 작성 (EDA, 이상치 제거, 세 도구 집계, timeit 성능 비교)
# ============================================================

import os
import pandas as pd
import polars as pl
import duckdb
import timeit

# 1. Pandas EDA 기초 탐색 + 이상치 처리
# sales_100k.csv를 로딩하고 기본 EDA를 수행한 뒤 IQR 방법으로 이상치를 제거

# CSV 로딩
NOTEBOOK_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(NOTEBOOK_DIR, "..", "data", "sales_100k.csv")

try:
    df = pd.read_csv(DATA_PATH)
except FileNotFoundError:
    raise FileNotFoundError(f"파일을 찾을 수 없습니다: {DATA_PATH}")

# 기초 EDA 수행
print("=== df.shape ===")
print(df.shape)

print("=== df.info() ===")
df.info()

print("=== df.describe(include='all') ===")
print(df.describe(include='all'))

print("=== df.isnull().sum() ===")
print(df.isnull().sum())
# 결측치 확인 결과
# region 10000개
# category 8000개
# amount 5000개


# 이상치 제거 (IQR)
# 연속형 변수인 amount에 대해서 IQR 이상치 제거 진행
Q1 = df["amount"].quantile(0.25)
Q3 = df["amount"].quantile(0.75)
IQR = Q3 - Q1

rows_before = len(df)
df = df[df["amount"].between(Q1 - 1.5 * IQR, Q3 + 1.5 * IQR)]
rows_after = len(df)

print("=== IQR 이상치 제거 ===")
print(f"제거 전 행 수: {rows_before}")
print(f"제거 후 행 수: {rows_after}")


# 2. Pandas groupby named aggregation
# region, category 별 총매출, 평균, 건수를 named aggregation으로 계산하고 총매출 내림차순으로 정렬
# named aggregation: total=('amount', 'sum') 형태로 결과 컬럼명을 직접 지정

def run_pandas():
    return (
        df.groupby(["region", "category"], as_index=False)
        .agg(
            total_amount = ("amount", "sum"),
            avg_amount = ("amount", "mean"),
            order_cnt = ("order_id", "count")
        )
        .sort_values("total_amount", ascending=False)
    )

pandas_result = run_pandas()
print(pandas_result)

# 3. Polars Lazy API로 동일 집계 작성
# 2번 실습과 동일한 집계를 Polars Lazy API로 작성
# scan_csv -> filter -> group_by -> agg -> sort -> collect

def run_polars():
    return (
        pl.scan_csv(DATA_PATH)
        .filter(
            pl.col("amount").is_between(Q1 - 1.5 * IQR, Q3 + 1.5 * IQR)
            & pl.col("region").is_not_null()
            & pl.col("category").is_not_null()
        )
        .group_by(["region", "category"])
        .agg(
            pl.col("amount").sum().alias("total_amount"),
            pl.col("amount").mean().alias("avg_amount"),
            pl.col("order_id").count().alias("order_cnt")
        )
        .sort("total_amount", descending=True)
        .collect()
    )

polars_result = run_polars()
print(polars_result)


# 4. DuckDB SQL + 세 도구 성능 비교
# DuckDB로 동일 집계를 SQL로 작성하고, timeit으로 세 도구의 실행 시간을 비교
query = f"""
    select
        region, category,
        sum(amount) as total_amount,
        avg(amount) as avg_amount,
        count(order_id) as order_cnt
    from read_csv('{DATA_PATH}')
    where amount between {Q1 - 1.5 * IQR} and {Q3 + 1.5 * IQR}
      and region is not null
      and category is not null
    group by region, category
    order by total_amount desc
"""

def run_duckdb():
    return duckdb.sql(query).pl()

duckdb_result = run_duckdb()
print(duckdb_result)

# timeit으로 세 도구 실행 시간 비교 (동일 repeat, number 사용)
pandas_times = timeit.repeat(run_pandas, repeat=5, number=1)
polars_times = timeit.repeat(run_polars, repeat=5, number=1)
duckdb_times = timeit.repeat(run_duckdb, repeat=5, number=1)

print("=== 실행 시간 비교 (repeat=5, number=1) ===")
print(f"Pandas : min={min(pandas_times):.4f}초, 평균={sum(pandas_times)/5:.4f}초")
print(f"Polars : min={min(polars_times):.4f}초, 평균={sum(polars_times)/5:.4f}초")
print(f"DuckDB : min={min(duckdb_times):.4f}초, 평균={sum(duckdb_times)/5:.4f}초")

