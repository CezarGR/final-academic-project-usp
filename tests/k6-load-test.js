/**
 * =============================================================================
 * TESTE DE CARGA K6 — API DE TRANSAÇÕES (TCC / pesquisa acadêmica)
 * =============================================================================
 *
 * Este script é compartilhado por todos os cenários em tests/scenario-N/.
 * A URL base da API vem da variável de ambiente BASE_URL (definida pelo
 * run-tests.py a partir de config.json → applicationUri).
 *
 * INDICADORES ALVO (pesquisa)
 * ---------------------------
 * - Latência: p(95) < 1 s e p(99) < 2 s (http_req_duration)
 * - Taxa de falha HTTP: < 5% (http_req_failed)
 * - Métricas de negócio: checks abaixo validam corpo e status das respostas
 *
 * USABILIDADE SIMULADA (modelo por taxa de chegada)
 * ---------------------------------------------------
 * - Executor: **constant-arrival-rate** (iterações por segundo fixas em cada fase).
 * - Várias fases encadeadas via ``startTime`` + ``duration`` reproduzem o perfil antigo
 *   (ramping-vus): aquecimento, ~60 VUs equivalentes, pico ~80, retorno e ramp-down.
 * - A taxa alvo (iter/s) foi estimada com iteração ~0,65 s (sleep + HTTP); ajuste ``rate``
 *   nas fases se o mix de endpoints ou latência mudar.
 * - Duração total: 20 minutos.
 *
 * FLUXO DE CADA ITERAÇÃO (default)
 * --------------------------------
 * 1) Escolhe um usuário aleatório da lista USER_IDS (dados seed no MongoDB).
 * 2) Escolhe uma operação conforme probabilidades (mix de uso realista).
 * 3) Executa a requisição HTTP correspondente.
 * 4) Aplica checks de negócio (status, tempo, campos JSON esperados).
 * 5) Registra falha na métrica customizada `errors` se algum check falhar.
 * 6) sleep aleatório curto antes da próxima iteração (pensa-tempo do usuário).
 *
 * ENDPOINTS EXERCITADOS
 * ---------------------
 * - GET  /api/users/:id                    → extrato (dados do usuário)
 * - GET  /api/users/:id/transactions?days&page&size → histórico paginado (padrão page=0, size=10)
 * - POST /api/users/:id/deposits          → depósito (crédito)
 * - POST /api/users/:id/transfers         → transferência (débito + crédito)
 *
 * O orquestrador Python exporta também k6-summary.json para avaliar SLAs na planilha.
 * =============================================================================
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

/** Taxa de falhas nos checks de negócio (incrementada quando check() retorna false). */
const errorRate = new Rate('errors');

/**
 * URL base da API.
 * - Dentro da rede Docker do cenário: http://transaction-api:8080
 * - Cenário cloud: https://... (definido em config.json → applicationUri)
 */
const BASE_URL = __ENV.BASE_URL || 'http://transaction-api:8080';

/** IDs MongoDB dos usuários seed (tests/users-collection.json). */
const USER_IDS = [
  '698a8a229cc78c2b3222eb8f',
  '698a8a229cc78c2b3222eb9f',
  '698a8a229cc78c2b3222ebaf',
  '698a8a229cc78c2b3222ebbf',
  '698a8a229cc78c2b3222ebcf',
  '698a8a229cc78c2b3222ebdf',
  '698a8a229cc78c2b3222ebef',
  '698a8a229cc78c2b3222ebff',
  '698a8a229cc78c2b3222ec0f',
  '698a8a229cc78c2b3222ec1f',
  '698a8a229cc78c2b3222ec2f',
  '698a8a229cc78c2b3222ec3f',
  '698a8a229cc78c2b3222ec4f',
  '698a8a229cc78c2b3222ec5f',
  '698a8a229cc78c2b3222ec6f',
  '698a8a229cc78c2b3222ec7f',
  '698a8a229cc78c2b3222ec8f',
  '698a8a229cc78c2b3222ec9f',
  '698a8a229cc78c2b3222ecaf',
  '698a8a229cc78c2b3222ecbf',
];

/**
 * Opções globais: **constant-arrival-rate** em fases (20 min no total).
 *
 * Cada fase fixa ``rate`` iterações por ``timeUnit`` (1 s). Fases não se sobrepõem
 * no tempo; juntas equivalem ao antigo ramping-vus (60 / 80 VUs aproximados).
 *
 * Linha do tempo:
 *   0–1m   → aquecimento baixo (~30 it/s)
 *   1–2m   → aquecimento médio (~60 it/s)
 *   2–10m  → estável (~90 it/s ≈ 60 VUs com iteração ~0,65 s)
 *   10–11m → transição para pico (~110 it/s)
 *   11–14m → pico (~125 it/s ≈ 80 VUs)
 *   14–15m → volta do pico (~90 it/s)
 *   15–19m → estável (~90 it/s)
 *   19–20m → ramp-down (~12 it/s)
 */
export const options = {
  scenarios: {
    fase_aquecimento_1: {
      executor: 'constant-arrival-rate',
      startTime: '0s',
      duration: '1m',
      rate: 30,
      timeUnit: '1s',
      preAllocatedVUs: 40,
      maxVUs: 150,
    },
    fase_aquecimento_2: {
      executor: 'constant-arrival-rate',
      startTime: '1m',
      duration: '1m',
      rate: 60,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 150,
    },
    fase_estavel_1: {
      executor: 'constant-arrival-rate',
      startTime: '2m',
      duration: '8m',
      rate: 90,
      timeUnit: '1s',
      preAllocatedVUs: 60,
      maxVUs: 180,
    },
    fase_pico_sobe: {
      executor: 'constant-arrival-rate',
      startTime: '10m',
      duration: '1m',
      rate: 110,
      timeUnit: '1s',
      preAllocatedVUs: 70,
      maxVUs: 200,
    },
    fase_pico_topo: {
      executor: 'constant-arrival-rate',
      startTime: '11m',
      duration: '3m',
      rate: 125,
      timeUnit: '1s',
      preAllocatedVUs: 80,
      maxVUs: 220,
    },
    fase_pos_pico: {
      executor: 'constant-arrival-rate',
      startTime: '14m',
      duration: '1m',
      rate: 90,
      timeUnit: '1s',
      preAllocatedVUs: 60,
      maxVUs: 180,
    },
    fase_estavel_2: {
      executor: 'constant-arrival-rate',
      startTime: '15m',
      duration: '4m',
      rate: 90,
      timeUnit: '1s',
      preAllocatedVUs: 60,
      maxVUs: 180,
    },
    fase_encerramento: {
      executor: 'constant-arrival-rate',
      startTime: '19m',
      duration: '1m',
      rate: 12,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 80,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<950', 'p(99)<1000'],
    http_req_failed: ['rate<0.05'],
    errors: ['rate<0.05'],
  },
};

/**
 * Função default: uma iteração (uma requisição de negócio + think time).
 * Com constant-arrival-rate o k6 agenda novas iterações na taxa da fase; VUs sobem até maxVUs se preciso.
 */
export default function () {
  const userId = USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
  const destinationUserId = USER_IDS.find((id) => id !== userId) || USER_IDS[0];

  const operation = Math.random();

  if (operation < 0.35) {
    consultarExtrato(userId);
  } else if (operation < 0.55) {
    consultarHistorico(userId);
  } else if (operation < 0.75) {
    realizarDeposito(userId);
  } else {
    realizarTransferencia(userId, destinationUserId);
  }

  sleep(Math.random() * 0.5 + 0.15);
}

/**
 * GET extrato do usuário — valida status 200, latência, presença de id, balance, lastTransactions.
 */
function consultarExtrato(userId) {
  const url = `${BASE_URL}/api/users/${userId}`;
  const response = http.get(url);

  const success = check(response, {
    'status é 200': (r) => r.status === 200,
    'tempo de resposta': (r) => r.timings.duration < 1000,
    'resposta contém id': (r) => {
      try {
        return JSON.parse(r.body).id !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém balance': (r) => {
      try {
        return JSON.parse(r.body).balance !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém lastTransactions': (r) => {
      try {
        return JSON.parse(r.body).lastTransactions !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * GET histórico de transações — resposta Spring Page (content, totalElements, …).
 */
function consultarHistorico(userId) {
  const days = Math.floor(Math.random() * 81) + 10;
  const url = `${BASE_URL}/api/users/${userId}/transactions?days=${days}`;
  const response = http.get(url);

  const success = check(response, {
    'status é 200': (r) => r.status === 200,
    'tempo de resposta': (r) => r.timings.duration < 1000,
    'resposta paginada (content array)': (r) => {
      try {
        const b = JSON.parse(r.body);
        return Array.isArray(b.content);
      } catch {
        return false;
      }
    },
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * POST depósito — valida 201, latência, amount, type (crédito), balanceAfter.
 */
function realizarDeposito(userId) {
  const url = `${BASE_URL}/api/users/${userId}/deposits`;
  const payload = JSON.stringify({
    amount: Math.floor(Math.random() * 15000) + 1000,
  });
  const params = {
    headers: { 'Content-Type': 'application/json' },
  };
  const response = http.post(url, payload, params);

  const success = check(response, {
    'status é 201': (r) => r.status === 201,
    'tempo de resposta': (r) => r.timings.duration < 2000,
    'resposta contém amount': (r) => {
      try {
        return JSON.parse(r.body).amount !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém type': (r) => {
      try {
        return JSON.parse(r.body).type !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém balanceAfter': (r) => {
      try {
        return JSON.parse(r.body).balanceAfter !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * POST transferência — valida 201, latência, type, relatedUserId, amount.
 */
function realizarTransferencia(userId, destinationUserId) {
  const url = `${BASE_URL}/api/users/${userId}/transfers`;
  const payload = JSON.stringify({
    destinationUserId: destinationUserId,
    amount: Math.floor(Math.random() * 8000) + 200,
    description: 'pagamento',
  });
  const params = {
    headers: { 'Content-Type': 'application/json' },
  };
  const response = http.post(url, payload, params);

  const success = check(response, {
    'status é 201': (r) => r.status === 201,
    'tempo de resposta': (r) => r.timings.duration < 2000,
    'resposta contém type': (r) => {
      try {
        return JSON.parse(r.body).type !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém relatedUserId': (r) => {
      try {
        return JSON.parse(r.body).relatedUserId !== undefined;
      } catch {
        return false;
      }
    },
    'resposta contém amount': (r) => {
      try {
        return JSON.parse(r.body).amount !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (!success) {
    errorRate.add(1);
  }
}
