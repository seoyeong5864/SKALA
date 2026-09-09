# 판교캠퍼스(`skala-2026`) 전용 매니페스트

상위 폴더의 `ch05`~`ch13` 은 **교재 본문의 발췌**다. 이 폴더는 그것을
**이 클러스터에서 그대로 돌아가게** 고친 것이다. 시나리오 문서(`실습시나리오.md`)와 짝이다.

## 쓰는 법

```bash
export ME=p000                                   # 자기 식별자
kubectl config set-context --current --namespace=class-6

./render.sh l07-pod.yaml                          # ① 먼저 읽는다
./render.sh l07-pod.yaml | kubectl apply -f -     # ② 그 다음 적용
./render.sh l07-pod.yaml | kubectl delete -f -    # ③ 지울 때도 같은 파일로
```

`render.sh` 는 자리표시자만 채워 표준출력으로 낸다. **적용은 하지 않는다** —
반 전체가 한 네임스페이스를 쓰므로, 무엇이 들어가는지 보고 나서 적용하는 습관이 필요하다.

| 자리표시자 | 채워지는 값 | 출처 |
| --- | --- | --- |
| `__ME__` | 자기 식별자 | `$ME` (필수) |
| `__NS__` | 네임스페이스 | 현재 컨텍스트, 또는 `$NS` |
| `__REG__` | `skala-registry.skala-ai.com/<NS>` | `$REG` 로 덮어쓰기 가능 |
| `__TAG__` | `1.0.0` | `$TAG` |

## 파일

| 파일 | 시나리오 | 무엇을 보는가 |
| --- | --- | --- |
| `l07-pod.yaml` | 실습 7 | 세 프로브 · Downward API |
| `l03-deployment.yaml` | 실습 3·실습 9·실습 10 | Harbor 이미지 · 무중단 롤링 · Service |
| `l14-pvc-pod.yaml` | 실습 14 | `ebs-sc` RWO — replicas 2 에서 실패한다 |
| `l15-efs-shared.yaml` | 실습 15 | `efs-sc` RWX — 실습 14 과 두 줄만 다르다 |
| `l16-oom.yaml` | 실습 16 | limit 256Mi 로 OOMKilled 재현 |
| `l18-complete/` | 실습 18 | 완성 매니페스트 (판교판) |

## 교재판(`ch12-complete/`)과 다른 세 곳

`l18-complete/` 안의 주석에 `★ 교재와 다름` 으로 표시해 두었다.

| | 교재 (일반 EKS) | `skala-2026` |
| --- | --- | --- |
| 레지스트리 | ECR, 노드 IAM 역할로 pull | **사내 Harbor** — 노드가 바로 받는다. 풀 시크릿 없음 |
| Ingress | `alb.ingress.kubernetes.io/*` → ALB 생성 | **`ingressClassName: nginx`** — alb 애노테이션은 무시된다 |
| 분산 | `topologySpreadConstraints` 로 zone 분산 | **단일 AZ** — hostname 분산만 유효 |

## 풀 시크릿이 없다

```bash
kubectl get secret
#   No resources found in class-6 namespace.
```

이 클러스터는 **노드가 레지스트리에서 바로 받도록 구성되어 있다.** 그래서 매니페스트에
`imagePullSecrets` 줄이 없고, 만들 시크릿도 없다. 비어 있는 것이 정상이다.
사내 다른 클러스터로 옮기면 그때는 `docker-registry` 타입 시크릿을 만들어 연결해야 한다.
