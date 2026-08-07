# ============================================================
# 프로그램명: Practice4 (시각화, 통계 검정, sklearn Pipeline)
# 작성자: 판교_8반_박서영
# 작성일: 2026.08.07
#
# 프로그램 설명:
# practice3에서 정제한(IQR 이상치 제거) sales_100k.csv 데이터를 대상으로
# 2x2 서브플롯 EDA 시각화, t-test/카이제곱 통계 검정, sklearn Pipeline 학습 및 저장,
# Plotly 인터랙티브 차트 저장까지 수행한다.
#
# 변경 내역:
# 2026.08.07 최초 작성 (EDA 시각화, 통계 검정, Pipeline 학습/저장, Plotly 차트)
# ============================================================

import os
import platform
import pandas as pd

import matplotlib.pyplot as plt
import seaborn as sns

from scipy import stats

from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error
import joblib

import plotly.express as px

# OS별 한글 폰트 설정 (한글 깨짐 방지)
if platform.system() == "Darwin":
    plt.rcParams["font.family"] = "AppleGothic"      # macOS
elif platform.system() == "Windows":
    plt.rcParams["font.family"] = "Malgun Gothic"    # Windows
else:
    plt.rcParams["font.family"] = "NanumGothic"      # Linux (나눔고딕 설치 필요)

plt.rcParams["axes.unicode_minus"] = False   # 한글 폰트 사용 시 마이너스 기호 깨짐 방지



# CSV 로딩
NOTEBOOK_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(NOTEBOOK_DIR, "..", "data", "sales_100k.csv")

try:
    df = pd.read_csv(DATA_PATH)
except FileNotFoundError:
    raise FileNotFoundError(f"파일을 찾을 수 없습니다: {DATA_PATH}")

# 이상치 제거 (IQR, amount 기준)
Q1 = df["amount"].quantile(0.25)
Q3 = df["amount"].quantile(0.75)
IQR = Q3 - Q1
df = df[df["amount"].between(Q1 - 1.5 * IQR, Q3 + 1.5 * IQR)]

# 1. EDA 시각화 4종 (2 x 2 서브플롯)
# fig, axes = plot.subplots(2,2)로 히스토그램 + KDE / 박스플롯 / 월별 라인 / 상관히트맵 작성

try:
    fig, axes = plt.subplots(
        nrows = 2,
        ncols = 2
    )

    # 히스토그램
    sns.histplot(data=df, x="amount", bins = 30, kde = True, ax=axes[0,0])
    axes[0,0].set_title("매출 분포")

    # 박스플롯
    sns.boxplot(data=df, x="region", y="amount", ax=axes[0,1])
    axes[0,1].set_title("지역별 매출 분포")

    # 월별 라인
    df["order_date"] = pd.to_datetime(df["order_date"])
    monthly = df.groupby(df["order_date"].dt.to_period("M"))["amount"].sum().reset_index()
    monthly["order_date"] = monthly["order_date"].astype(str)

    axes[1,0].plot(monthly["order_date"], monthly["amount"])
    axes[1,0].set_title("월별 총매출 추이")
    axes[1,0].tick_params(axis='x', rotation=45)

    # 상관히트맵 (수치형 변수)
    corr = df[["quantity", "unit_price", "customer_age", "amount"]].corr()
    sns.heatmap(corr, annot=True, fmt=".2f", cmap="coolwarm", ax=axes[1,1])
    axes[1,1].set_title("수치형 변수 간 상관관계")

    fig.tight_layout()
    plt.show()
except Exception as e:
    print(f"[1번 시각화 오류] {e}")



# 2. 통계 검정 - t-test + 카이제곱
# 서울-부산 평균 매출 차이를 t-test 로, 지역 x 카테고리 독립성을 카이제곱 검정으로 확인

try:
    # t-test 시행
    seoul_amount = df[df["region"] == "서울"]["amount"]
    busan_amount = df[df["region"] == "부산"]["amount"]

    t_stat, p_value = stats.ttest_ind(seoul_amount, busan_amount, equal_var=False)

    print("=== t-test (서울 vs 부산 평균 매출) ===")
    print(f"t-statistic: {t_stat:.4f}")
    print(f"p-value: {p_value:.4f}")
    if p_value < 0.05:
        print("p < 0.05 이므로 서울과 부산의 평균 매출 차이는 통계적으로 유의미하다")
    else:
        print("p >= 0.05 이므로 서울과 부산의 평균 매출 차이는 통계적으로 유의미하지 않다")
except Exception as e:
    print(f"[2번 t-test 오류] {e}")

try:
    # 카이제곱 검정 시행
    contingency = pd.crosstab(df["region"], df["category"])
    print("=== 분할표 (region x category) ===")
    print(contingency)

    chi2, p_value_chi2, dof, expected = stats.chi2_contingency(contingency)

    print("=== 카이제곱 검정 (region x category 독립성) ===")
    print(f"chi2: {chi2:.4f}")
    print(f"p-value: {p_value_chi2:.4f}")
    print(f"자유도: {dof}")
    if p_value_chi2 < 0.05:
        print("p < 0.05 이므로 region과 category는 서로 독립이 아니다(유의미한 연관이 있다)")
    else:
        print("p >= 0.05 이므로 region과 category는 통계적으로 독립적이다(유의미한 연관 없음)")
except Exception as e:
    print(f"[2번 카이제곱 검정 오류] {e}")



# 3. sklearn pipeline 구성 + 저장
# ColumnTransformer + Pipeline을 완성하고 훈련, 평가, 저장, 재로딩을 순서대로 수행

try:
    numeric_features = ["quantity", "unit_price", "customer_age"]
    categorical_features = ["region", "category", "payment_method", "customer_gender"]

    X = df[numeric_features + categorical_features].dropna()
    y = df.loc[X.index, "amount"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 전처리: 수치형은 표준화, 범주형은 원핫인코딩
    preprocessor = ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), numeric_features),
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_features)
        ]
    )

    # 전처리 + 모델을 하나의 Pipeline으로 구성
    model_pipeline = Pipeline(steps=[
        ("preprocessor", preprocessor),
        ("regressor", LinearRegression())
    ])

    # 훈련
    model_pipeline.fit(X_train, y_train)

    # 평가
    y_pred = model_pipeline.predict(X_test)
    score = model_pipeline.score(X_test, y_test)
    mae = mean_absolute_error(y_test, y_pred)

    print("=== sklearn Pipeline 학습 결과 ===")
    print(f"R^2 score: {score:.4f}")
    print(f"MAE: {mae:.4f}")

    # 저장
    MODEL_PATH = os.path.join(NOTEBOOK_DIR, "practice4_pipeline.joblib")
    joblib.dump(model_pipeline, MODEL_PATH)
    print(f"모델 저장 완료: {MODEL_PATH}")

    # 재로딩
    loaded_pipeline = joblib.load(MODEL_PATH)
    reload_score = loaded_pipeline.score(X_test, y_test)
    print(f"재로딩한 모델 R^2 score: {reload_score:.4f}")
except Exception as e:
    print(f"[3번 Pipeline 오류] {e}")

# 4. Plotly 인터랙티브 차트 저장
# 지역, 카테고리별 총매출을 Plotly Express 막대 차트로 만들로 HTML로 저장

try:
    sales_by_region_category = df.groupby(["region", "category"], as_index=False)["amount"].sum()

    fig_plotly = px.bar(
        sales_by_region_category,
        x="region",
        y="amount",
        color="category",
        barmode="group",
        title="지역·카테고리별 총매출"
    )

    PLOTLY_HTML_PATH = os.path.join(NOTEBOOK_DIR, "practice4_sales_chart.html")
    fig_plotly.write_html(PLOTLY_HTML_PATH)
    print(f"Plotly 차트 저장 완료: {PLOTLY_HTML_PATH}")
except Exception as e:
    print(f"[4번 Plotly 오류] {e}")