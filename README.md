# Projeto acadêmico — TCC (MBA Engenharia de Software)

Repositório do **Trabalho de Conclusão de Curso** da pós-graduação **MBA em Engenharia de Software** da **USP/Esalq**. O projeto reúne uma API de transações financeiras (simulação de gateway de pagamento), infraestrutura em Docker, testes de carga com **k6** e observabilidade com **Prometheus**, **Grafana**, **cAdvisor** e **Node Exporter**, organizados em **cenários experimentais** reprodutíveis.

---

## Título da pesquisa

**Do Palpite à Previsão: Uma Metodologia Experimental para Determinação dos Limites de Performance em Aplicações Webs**

---

## Contexto geral da pesquisa

O trabalho investiga como **testes de carga** podem ser utilizados como ferramenta para determinar o **provisionamento adequado** de uma aplicação web.

A ideia central é transformar o provisionamento de infraestrutura baseado em **“palpite”** em um provisionamento baseado em **“previsão”**, utilizando testes de **performance** e **carga** para identificar, de forma **experimental**, a quantidade ideal de **recursos computacionais** (CPU, memória, efeitos de I/O e latência) antes de decisões de produção.

---

## Problema de pesquisa

Muitas equipes utilizam mecanismos automáticos de escalabilidade ou realizam o provisionamento **manual** sem conhecer previamente a **necessidade computacional real** da aplicação. Isso tende a gerar:

- **Superprovisionamento**: desperdício de recursos e aumento de custos.  
- **Subprovisionamento**: lentidão, indisponibilidade e falhas sob carga.

A pesquisa propõe uma **metodologia experimental** para descobrir um ponto de provisionamento mais adequado **antes** da entrada em produção, apoiada em métricas objetivas coletadas durante os experimentos.

---

## Aplicação estudada

A aplicação utilizada nos experimentos é uma API **Java Spring Boot** que **simula um gateway de pagamento**: gestão de usuários, depósitos, transferências, histórico de transações e regras de limite especial, com persistência em **MongoDB**. Detalhes de negócio, endpoints e execução local estão em [`application/README.md`](application/README.md).

---

## Ferramentas e tecnologias

| Área | Tecnologias |
|------|-------------|
| Backend | Java, Spring Boot |
| Conteinerização | Docker, Docker Compose (e imagens publicadas no Docker Hub quando aplicável) |
| Nuvem | AWS EC2 (experimentos com aplicação e stack de observabilidade na instância) |
| Dados | MongoDB (local em container ou serviço gerenciado/externo, conforme o cenário) |
| Carga | k6 (`tests/k6-load-test.js`) |
| Visualização | Grafana (dashboards em `monitoring/grafana/dashboards/`) |
| Métricas | Prometheus |
| Containers / host | cAdvisor, Node Exporter |
| Observabilidade | CPU, memória, rede, latência HTTP, throughput (RPS), erros (incluindo HTTP 408 em cenários configurados), concorrência e pool do driver MongoDB (Micrometer), além de métricas de JVM/Tomcat expostas pelo Actuator |

---

## Cenários experimentais (visão geral)

Os cenários estão em `tests/scenario-*`, cada um com `config.json`, `docker-compose.yml` (quando aplicável) e um **README** próprio.

| Cenário (metodologia) | Pasta no repositório | Ideia |
|----------------------|----------------------|--------|
| **1** — Baseline local, provisionamento inicial | `tests/scenario-1/` | Aplicação e MongoDB no mesmo host (Docker), limites iniciais de CPU/memória nos containers; stack completa de observabilidade local. |
| **2** — Local com aumento agressivo de recursos (ex.: MongoDB) | `tests/scenario-2/` | Mesmo perfil de carga; **limites de CPU do container MongoDB** aumentados em relação ao cenário 1 (ex.: 4 vCPU), mantendo a API com perfil semelhante ao baseline. |
| **3** — Local, variações adicionais com base em resultados anteriores | `tests/scenario-3/` | Ajuste fino do ambiente local (ex.: **5 vCPU** no MongoDB) para explorar efeitos observados nos cenários anteriores. |
| **4** — Nuvem (EC2) + observabilidade na instância | `tests/scenario-4/`¹ | Aplicação (e Prometheus/cAdvisor/Node Exporter, conforme implantação) em **EC2**; Grafana costuma rodar **localmente** com datasource apontando para o Prometheus remoto; MongoDB pode ser externo (ex.: Atlas). |

¹ *No repositório, a configuração alinhada ao “cenário de nuvem” encontra-se na pasta `scenario-5`; o campo `scenario` dentro do `config.json` pode ser renomeado para refletir o número do experimento, se desejar consistência estrita entre pasta e metadados.*

---

## Execução dos experimentos (testes automatizados)

O script [`tests/run-tests.py`](tests/run-tests.py) descobre pastas `scenario-*` com `config.json`, sobe o Docker Compose quando configurado, executa o k6, opcionalmente captura painéis do Grafana, consolida métricas do Prometheus e grava resultados em `tests/result.xlsx`.

Exemplos:

```bash
# Um cenário
python3 tests/run-tests.py --scenario scenario-1

# Pausa antes do docker compose down (analisar Grafana com containers ativos)
python3 tests/run-tests.py --scenario scenario-1 --pause-before-docker-down
```

Variáveis úteis: `GRAFANA_URL`, `GRAFANA_USER`, `GRAFANA_PASSWORD`. Ver comentários e `--help` no próprio script.

---

## Estrutura resumida do repositório

```
application/          # API Spring Boot
monitoring/           # Prometheus e provisioning Grafana (referência)
tests/                # Cenários, k6, seeds MongoDB, run-tests.py, result.xlsx
```

---

## Licença e uso

Projeto de natureza **acadêmica** (TCC). Reutilização de código ou dados deve respeitar a política da instituição e a atribuição adequada.
