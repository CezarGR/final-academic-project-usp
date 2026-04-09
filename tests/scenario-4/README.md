# Cenário de nuvem (EC2) — aplicação e observabilidade na instância, Grafana local

> **Nota:** Na árvore atual do repositório esta configuração vive na pasta **`scenario-5`**. No texto do TCC ela corresponde ao **experimento em nuvem** (ex.: “Cenário 4”): API em **EC2**, ferramentas de observabilidade na mesma instância, e análise no Grafana a partir da sua máquina.

## Objetivo

Executar o **mesmo tipo de teste de carga** usado nos cenários locais, com a aplicação **provisionada em computação em nuvem** (**AWS EC2**), mantendo **Docker** para padronizar runtime da API e dos componentes de observabilidade (**Prometheus**, **cAdvisor**, **Node Exporter**) na instância. O **Grafana** roda **localmente** (neste repositório, via `docker-compose.yml` mínimo só com Grafana), com **datasource Prometheus** apontando para o endpoint remoto (`prometheusUri` no `config.json`).

Permite correlacionar métricas da **aplicação** (Actuator), dos **containers** e do **SO** (Node Exporter) com latência e erros observados sob carga, incluindo efeitos de **MongoDB remoto** (ex.: Atlas) quando `databaseLocation` é `external`.

## Perfil de carga

- `tests/k6-load-test.js`.
- O k6 executa na máquina do pesquisador contra a **URL pública** da API na EC2.

## Infraestrutura

- **EC2** (ex.: família **t3.micro** com 2 vCPU e 2 GiB RAM — ajuste conforme sua implantação): containers Docker para API + stack de métricas, conforme scripts/`docker-compose` usados na instância.
- **MongoDB**: tipicamente **externo** (ex.: **MongoDB Atlas** com tier dedicado); latência de rede e capacidade do cluster influenciam os resultados.
- **Nesta pasta (`tests/scenario-5/`)**: apenas **Grafana** + provisioning (`monitoring/grafana/...`) e `config.json` com:
  - `externalApplication: true`
  - `applicationUri`, `healthCheckUri`, `prometheusUri` da EC2 (ou endpoints atualizados)
  - `databaseLocation: external`

Atualize **URLs e credenciais** no `config.json` e nos `.env` antes de publicar ou versionar dados sensíveis.

## Como executar

```bash
cd tests
python3 run-tests.py --scenario scenario-5 --pause-before-docker-down
```

A flag `--pause-before-docker-down` é especialmente útil aqui: o Grafana local permanece ativo até você pressionar **Enter**, depois o compose derruba só o container local.

## O que observar

- CPU da instância (~60–80% se esse for o alvo experimental) vs. saturação.
- **Latência** e **erros** com banco **remoto** em comparação aos cenários com Mongo no mesmo host Docker.
- Painéis com **Node Exporter** (quando o Prometheus remoto faz scrape) e métricas HTTP da API.

## Segurança

Não commite chaves, URIs internas ou dados reais de produção. Use `config-example.json` / `.env.example` como modelo.
