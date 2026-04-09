# Cenário 3 — Variações adicionais no ambiente local

## Objetivo

Prosseguir a linha experimental **no mesmo host local**, aplicando **novas variações de provisionamento** inspiradas nos **resultados dos Cenários 1 e 2**. A ideia é **afinar** o ambiente (por exemplo, **outro patamar de CPU para o MongoDB**) sem alterar o roteiro de carga do k6, mantendo comparabilidade entre execuções.

## Perfil de carga

- `tests/k6-load-test.js` (inalterado em princípio).
- Mesma narrativa de uso intensivo com picos eventuais.

## Infraestrutura (Docker Compose)

Stack equivalente aos cenários anteriores (app, mongo, observabilidade).

**Exemplo de ajuste** (conferir `docker-compose.yml`):

- Aplicação: frequentemente **1,0 CPU**, **512 MiB**.
- MongoDB: limite de CPU ainda **superior** ao Cenário 1 — por exemplo **5,0 vCPU**, **512 MiB** de RAM.

Assim, o Cenário 3 situa-se como um **terceiro ponto** na exploração local: baseline (1) → forte aumento no DB (2) → variação adicional (3).

## Configuração

- `config.json`: `databaseLocation: internal`.

## Como executar

```bash
cd tests
python3 run-tests.py --scenario scenario-3
```

## O que observar

Evolução das métricas em relação aos Cenários **1** e **2**: se o ganho marginal de performance **estagna** ou se novos gargalos aparecem (rede, disco, aplicação, GC, etc.). Documentar decisões de ajuste no relatório do TCC.
