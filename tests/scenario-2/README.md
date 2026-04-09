# Cenário 2 — Local com aumento substancial de recursos (foco no MongoDB)

## Objetivo

Repetir o **mesmo perfil de carga** do Cenário 1 em ambiente **local**, alterando de forma **intencional** os **limites de recursos dos containers** — em especial **aumentando bastante a CPU alocada ao MongoDB** em relação ao Cenário 1 — para observar o impacto na **capacidade do banco** sob a mesma carga de teste.

> Em termos de metodologia, este passo aproxima-se da ideia de “**mais recursos para o datastore**” sem mudar o script k6, permitindo comparar métricas lado a lado com o baseline.

## Perfil de carga

- Mesmo arquivo: `tests/k6-load-test.js`.
- Descrição de negócio: alta utilização com picos eventuais (ver `config.json`).

## Infraestrutura (Docker Compose)

A stack é análoga à do Cenário 1 (app, mongo, prometheus, grafana, cadvisor, mongodb_exporter, etc.).

**Diferença principal** em relação ao Cenário 1 (validar sempre no `docker-compose.yml`):

- **Aplicação**: em geral mantém **1,0 CPU** e **512 MiB** (baseline).
- **MongoDB**: limite de CPU elevado — tipicamente **4,0 vCPU** (frente a **1,0** no Cenário 1), **512 MiB** de RAM.

## Configuração

- `config.json`: `databaseLocation: internal`, URI interna do compose para a API.

## Como executar

```bash
cd tests
python3 run-tests.py --scenario scenario-2
```

## O que comparar com o Cenário 1

- Latência (P95) e throughput (RPS) com **menor gargalo de CPU no MongoDB**.
- Uso de CPU/memória **por container** (cAdvisor) e métricas do **mongodb_exporter**.
- Comportamento da JVM/Tomcat se o gargalo migrar da persistência para a aplicação.
