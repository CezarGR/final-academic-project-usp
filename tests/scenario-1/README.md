# Cenário 1 — Baseline local com provisionamento inicial

## Objetivo

Estabelecer uma **linha de base** experimental: aplicação e banco de dados **provisionados localmente** na mesma máquina do pesquisador (via Docker), com **limites iniciais** de CPU e memória nos containers, e stack de **observabilidade completa** (Prometheus, Grafana, cAdvisor, **mongodb_exporter**, Node Exporter) no compose.

## Perfil de carga

- Script: `k6-load-test.js` (raiz de `tests/`).
- Pressupõe **alta utilização** da API com consumo relativamente constante e **picos eventuais**, alinhado ao uso esperado pelo time de negócio (vide `config.json`).

## Infraestrutura (Docker Compose)

| Serviço | Papel |
|---------|--------|
| `app` (`transaction-api`) | API Spring Boot, build a partir de `../../application/` |
| `mongo` | MongoDB 7, dados em volume |
| `prometheus` | Scrape da API (`/actuator/prometheus`), cAdvisor, exporters |
| `grafana` | Dashboards provisionados (ex.: `provisionamento-infra`) |
| `cadvisor` | Métricas dos containers |
| `mongodb_exporter` | Métricas do servidor MongoDB |

*(O **Node Exporter** pode constar noutros composes ou na instância em nuvem; neste `docker-compose` dos cenários 1–3 local não está listado por defeito — ver ficheiro concreto.)*

**Limites típicos** (conferir `docker-compose.yml`):

- Aplicação: **1,0 CPU**, **512 MiB** RAM.
- MongoDB: **1,0 CPU**, **512 MiB** RAM.

## Configuração

- `config.json`: `databaseLocation: internal`, `applicationUri` apontando para o serviço Docker (`http://transaction-api:8080` no host da rede do compose).
- Variáveis em `.env` / `.env.example` (MongoDB, porta, Tomcat, etc.).

## Como executar

Na pasta `tests/`:

```bash
python3 run-tests.py --scenario scenario-1
```

Opcional: `--pause-before-docker-down` para inspecionar o Grafana antes do `docker compose down`.

## O que observar

Saturation de CPU/memória da **API** e do **MongoDB**, latência HTTP (P95/P99), RPS, erros, threads JVM/Tomcat e, se aplicável, métricas de host e de pool de conexões do driver MongoDB nos painéis do Grafana.
