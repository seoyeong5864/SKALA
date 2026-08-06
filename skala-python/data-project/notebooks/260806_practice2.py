# ============================================================
# 프로그램명: Practice2 (파일I/O, 예외처리, Pydantic 검증 파이프라인)
# 작성자: 판교_8반_박서영
# 작성일: 2026.08.06
#
# 프로그램 설명:
# 데이터 파일을 안전하게 읽고 Pydantic으로 검증한 뒤
# 정상 데이터와 오류 데이터를 각각 CSV와 JSON 파일로 저장한다.
#
# 변경 내역:
# - 파일 읽기 예외 처리 추가
# - SalesRecord 검증 모델 추가
# - 정상/오류 데이터 분리 기능 추가
# - 결과 저장 및 재로딩 검증 기능 추가
# ============================================================

import csv
import json
import logging
from pathlib import Path

from pydantic import BaseModel, Field, ValidationError, field_validator
from typing import Optional


logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")
logger = logging.getLogger(__name__)


DATA_DIR = Path(__file__).resolve().parent.parent / "data"
DATA_PATH = DATA_DIR / "Python_Practice2_Data.json"


# 1. safe_load_csv() 함수
# 완성파일이 없으면 None 반환
# 성공 시 dict 리스트 반환
def safe_load_csv(file_path):
    data = None
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        logger.info(f"{len(data)}건 로딩 완료: {file_path}")
    except FileNotFoundError:
        logger.error(f"파일을 찾을 수 없습니다: {file_path}")
        data = None
    finally:
        print("로딩 종료")

    return data


# 실제 데이터 파일을 정상적으로 읽는 케이스 검증
# -> dict 리스트가 반환되는지 assert로 확인
raw_data = safe_load_csv(DATA_PATH)
assert raw_data is not None

# 존재하지 않는 파일을 읽는 케이스 검증
# -> None이 반환되는지 assert로 확인 (체크포인트: "assert None 통과")
missing_data = safe_load_csv("존재하지_않는_파일.json")
assert missing_data is None


# 2. SalesRecord Pydantic v2 모델
# month, region: 비어있으면 안됨
# amount: 0 초과
# category: 없어도 됨
class SalesRecord(BaseModel):
    month: str
    region: str
    amount: float = Field(gt=0)
    category: Optional[str] = None

    @field_validator("month", "region")
    @classmethod
    def not_blank(cls, v):
        if not v.strip():
            raise ValueError("비어있는 값은 허용되지 않습니다")
        return v


# 3. 검증 파이프라인 (valid/errors 분리)
# raw_data를 순회하며 SalesRecord로 변환
# 성공 -> valid, 실패 -> errors({row, error}) 리스트
def validate_records(raw_data):
    valid = []
    errors = []

    for row in raw_data:
        try:
            record = SalesRecord(**row)
            valid.append(record)
        except ValidationError as e:
            logger.error(f"검증 실패: {row} -> {e}")
            errors.append({"row": row, "error": str(e)})

    return valid, errors


valid, errors = validate_records(raw_data)
assert len(valid) + len(errors) == len(raw_data)


# 4. 결과 파일 저장 + 재로딩 확인
# valid 레코드를 csv로, errors를 JSON으로 저장하고 다시 읽어 건수를 검증
VALID_CSV_PATH = DATA_DIR / "valid_sales.csv"
ERRORS_JSON_PATH = DATA_DIR / "error_sales.json"


def save_results(valid, errors, csv_path, json_path):
    fieldnames = list(SalesRecord.model_fields.keys())
    with open(csv_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for record in valid:
            writer.writerow(record.model_dump())

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(errors, f, ensure_ascii=False, indent=2)

    logger.info(f"valid {len(valid)}건 -> {csv_path}, errors {len(errors)}건 -> {json_path} 저장 완료")


def reload_and_verify(csv_path, json_path, expected_valid, expected_errors):
    with open(csv_path, "r", encoding="utf-8") as f:
        reloaded_valid = list(csv.DictReader(f))

    with open(json_path, "r", encoding="utf-8") as f:
        reloaded_errors = json.load(f)

    assert len(reloaded_valid) == expected_valid
    assert len(reloaded_errors) == expected_errors
    logger.info("재로딩 검증 통과")


save_results(valid, errors, VALID_CSV_PATH, ERRORS_JSON_PATH)
reload_and_verify(VALID_CSV_PATH, ERRORS_JSON_PATH, len(valid), len(errors))

