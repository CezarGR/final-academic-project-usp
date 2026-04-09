# Transaction API

API REST construída com Spring Boot (Java 17) para gestão de transações financeiras entre utilizadores do mesmo ecossistema. O sistema trata criação de utilizadores, depósitos de saldo, transferências entre utilizadores e mantém histórico completo de transações com gestão de um **limite especial** (crédito adicional).

## Objetivo do projeto

Esta API serve de base a um sistema de transações financeiras, oferecendo operações consistentes para:

- Gestão de contas de utilizador  
- Depósitos com pagamento automático de dívida do limite especial  
- Transferências entre utilizadores com validações completas  
- Histórico de transações com filtros (incluindo paginação)

## Arquitetura

A aplicação segue uma arquitetura em camadas:

- **Camada de controladores**: endpoints REST e tratamento de pedidos HTTP  
- **Camada de serviços**: regras de negócio e orquestração  
- **Camada de repositórios**: acesso a dados com Spring Data MongoDB  
- **Domínio**: entidades, DTOs, exceções e utilitários

### Stack tecnológica

- **Java 17**  
- **Spring Boot 3.5.x**  
- **MongoDB** (base de dados)  
- **Lombok** (geração de código)  
- **Spring Validation** (validação de pedidos)  
- **Spring Actuator** (monitorização e métricas Prometheus)

## Regras de negócio

### Gestão de utilizadores

- Cada utilizador é criado com:
  - Nome  
  - Documento (CPF, CNPJ, etc.)  
  - Data de nascimento  
- Cada novo utilizador recebe automaticamente um **limite especial padrão de 1000 centavos (10,00)**  
- O saldo inicial é **0 centavos**

### Sistema de limite especial

- Os utilizadores dispõem de um limite especial utilizável quando o saldo é insuficiente.  
- Num débito que excede o saldo:
  1. Utiliza-se primeiro o saldo disponível  
  2. O valor restante consome o limite especial  
- Num crédito (depósito ou transferência recebida):
  1. Paga-se primeiro a dívida do limite especial (até restaurar o limite a 1000)  
  2. O valor remanescente incrementa o saldo

### Regras financeiras

- **Uma transação não pode deixar o saldo abaixo do limite permitido**  
- As transferências exigem saldo suficiente (incluindo limite especial disponível)  
- Todos os valores monetários são armazenados e processados em **centavos** (inteiros)

### Histórico de transações

- O histórico pode ser filtrado por número de dias (com validação de negócio)  
- O filtro por dias tem **máximo de 90 dias**  
- Consultas paginadas: parâmetros `page` e `size` (comportamento padrão documentado na API)

## Fluxos de operação

### 1. Criação de utilizador

```
POST /api/users
Corpo:
{
  "name": "João Silva",
  "document": "12345678900",
  "birthDate": "1990-01-15"
}

Resposta:
{
  "id": "...",
  "name": "João Silva",
  "document": "12345678900",
  "birthDate": "1990-01-15",
  "specialLimit": 1000,
  "balance": 0
}
```

### 2. Consulta de utilizador

```
GET /api/users/{id}

Resposta:
{
  "id": "...",
  "name": "João Silva",
  "document": "12345678900",
  "birthDate": "1990-01-15",
  "specialLimit": 1000,
  "balance": 5000,
  "lastTransactions": [ ... ]
}
```

### 3. Depósito

**Fluxo:** validar utilizador; validar montante positivo; aplicar crédito (pagar dívida do limite especial primeiro, depois saldo); registar transação.

```
POST /api/users/{userId}/deposits
Corpo:
{
  "amount": 5000
}
```

**Exemplo:** saldo 0, `usedSpecialLimit` 500; depósito 2000 → após operação, dívida do limite quitada e saldo atualizado conforme regras de crédito.

### 4. Transferência entre utilizadores

**Fluxo:** validar existência dos dois utilizadores; validar saldo na origem (incluindo limite especial); validar regras financeiras; débito na origem; crédito no destino; dois registos de transação (débito e crédito).

```
POST /api/users/{originUserId}/transfers
Corpo:
{
  "destinationUserId": "id-do-destino",
  "amount": 3000,
  "description": "Pagamento de serviço"
}
```

### 5. Histórico de transações (paginado)

```
GET /api/users/{userId}/transactions?page=0&size=10
GET /api/users/{userId}/transactions?days=30&page=0&size=10
```

Resposta: objeto paginado (`Page`) com conteúdo ordenado (tipicamente do mais recente para o mais antigo).

## Endpoints da API

### Utilizadores

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users` | Criar utilizador |
| GET | `/api/users/{id}` | Obter utilizador por ID (com últimas transações) |

### Transações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/users/{userId}/transactions` | Histórico paginado; parâmetros opcionais `days`, `page`, `size` |

### Depósitos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users/{userId}/deposits` | Registar depósito |

### Transferências

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users/{originUserId}/transfers` | Transferir para outro utilizador |

### Manutenção (testes / operação)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/maintenance/health-check` | Estado da API |
| GET | `/api/maintenance/timeout-test` | Simulação de lentidão (configurável; pode gerar HTTP 408 com limite de tempo) |

## Como executar

### Pré-requisitos

- Java 17 ou superior  
- Maven 3.6+  
- MongoDB 7+ (ou Docker)

### Com Docker Compose (na raiz de um compose que inclua a API)

```bash
docker compose up -d
```

Serviços típicos: MongoDB (27017), aplicação (8080), Prometheus (9090), Grafana (3000) — conforme o ficheiro utilizado.

### Localmente (Maven)

1. Iniciar MongoDB (ex.: `docker run -d -p 27017:27017 --name mongo mongo:7`).  
2. Configurar `spring.data.mongodb.uri` em `application.properties` ou variáveis de ambiente.  
3. Compilar e executar:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080`.

## Exemplos com `curl`

### Criar utilizadores

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "document": "12345678900",
    "birthDate": "1990-01-15"
  }'
```

### Consultar utilizador

```bash
curl http://localhost:8080/api/users/{userId}
```

### Depósito

```bash
curl -X POST http://localhost:8080/api/users/{userId}/deposits \
  -H "Content-Type: application/json" \
  -d '{ "amount": 10000 }'
```

### Transferência

```bash
curl -X POST http://localhost:8080/api/users/{originUserId}/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "destinationUserId": "id-destino",
    "amount": 5000,
    "description": "Pagamento"
  }'
```

### Histórico

```bash
curl "http://localhost:8080/api/users/{userId}/transactions?page=0&size=10"
curl "http://localhost:8080/api/users/{userId}/transactions?days=30"
```

## Tratamento de erros

Respostas de erro padronizadas (exemplo):

```json
{
  "message": "User not found with ID: invalid-id",
  "error": "USER_NOT_FOUND",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Tipos de erro (exemplos)

- **USER_NOT_FOUND** (404): utilizador inexistente  
- **INSUFFICIENT_BALANCE** (400): saldo insuficiente  
- **INVALID_DAYS_LIMIT** (400): filtro de dias acima do máximo  
- **FINANCIAL_RULE_VIOLATION** (400): violação de regra financeira  
- **VALIDATION_ERROR** (400): falha de validação (`@Valid`)  
- **INVALID_ARGUMENT** (400): argumento inválido  
- **REQUEST_TIMEOUT** (408): tempo limite de operação configurado (cenários de teste)  
- **INTERNAL_SERVER_ERROR** (500): erro não tratado

## Estrutura do código

```
src/main/java/com/tcc/transaction_api/
├── application/
│   ├── controller/
│   └── exception/
├── domain/
│   ├── dto/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── util/
└── TransactionApiApplication.java
```

## Componentes principais

### Utilitários

- **BalanceCalculator**: saldo disponível e verificação de suficiência  
- **LimitApplier**: operações de débito/crédito com limite especial  
- **FinancialValidator**: validações antes de transações

### Serviços

- **UserService**: criação e consulta de utilizadores  
- **TransactionService**: histórico e paginação  
- **DepositService**: depósitos  
- **TransferService**: transferências com validações

## Monitorização

Endpoints Spring Actuator (exemplos):

- Saúde: `http://localhost:8080/actuator/health`  
- Informação: `http://localhost:8080/actuator/info`  
- Prometheus: `http://localhost:8080/actuator/prometheus`  

Métricas HTTP, JVM, Tomcat e, quando configurado, pool do driver MongoDB (`mongodb_driver_pool_*`) para observabilidade em Grafana/Prometheus.

## Consistência financeira

- Operações **transacionais** (`@Transactional`)  
- **Validações** em múltiplas camadas  
- **Rastreabilidade** com registo de transações  
- **Regras de limite especial** aplicadas de forma uniforme

## Boas práticas adotadas

- Arquitetura em camadas e separação de responsabilidades  
- Utilitários reutilizáveis para cálculos financeiros  
- Exceções de domínio com mensagens claras  
- Gestão transacional para consistência  
- Documentação alinhada ao uso em trabalho académico (TCC)

## Notas

- Valores em **centavos** (inteiros)  
- Limite especial padrão: **1000 centavos**  
- Filtro máximo de histórico por dias: **90**  
- Cada transferência gera **dois** registos de transação (origem e destino)  
- Identificadores de utilizador: IDs gerados pelo MongoDB  

## Contribuições

Projeto de **TCC** (MBA em Engenharia de Software). O código pretende ser legível, manutenível e extensível no contexto académico.

## Licença

Trabalho académico (TCC). Utilização sujeita às normas da instituição.
