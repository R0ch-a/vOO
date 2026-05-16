# vOO Airline — Backend
<img width="2160" height="3840" alt="image" src="https://github.com/user-attachments/assets/0c64d172-bd69-4522-837c-d9069aeda5b5" />
API REST da companhia aérea vOO, desenvolvida como projeto acadêmico de **Programação Orientada a Objetos** com Java 21 e Spring Boot 3.

---

## Tecnologias

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.3.0 | Framework web e IoC |
| Spring Data JPA | 3.3.0 | Abstração de persistência |
| Hibernate | 6.x | ORM (mapeamento objeto-relacional) |
| PostgreSQL | 14+ | Banco de dados relacional |
| Flyway | 10.x | Migrations versionadas do banco |
| Lombok | latest | Redução de boilerplate |
| SpringDoc OpenAPI | 2.5.0 | Documentação Swagger automática |
| JUnit 5 + Mockito | 5.x | Testes unitários |
| H2 | latest | Banco em memória para testes |
| Maven | 3.9+ | Gerenciamento de dependências e build |

---

## Pré-requisitos

- JDK 21 ou superior
- Maven 3.9+
- PostgreSQL 14+ rodando localmente

---

## Como rodar

**1. Criar o banco de dados**

```sql
CREATE DATABASE voo_airline;
```

**2. Configurar credenciais**

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/voo_airline
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

**3. Compilar e executar**

```bash
# Executar direto pelo Maven
mvn spring-boot:run

# Ou gerar o JAR e executar
mvn clean package -DskipTests
java -jar target/airline-1.0.0.jar
```

**4. Rodar os testes**

```bash
mvn test
```

A API estará disponível em `http://localhost:8080`.  
O Flyway cria todas as tabelas automaticamente na primeira execução.

---

## Documentação interativa (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

---

## Estrutura de pacotes

```
src/main/java/com/voo/airline/
│
├── VooAirlineApplication.java          ← Entrypoint
│
├── config/
│   ├── AsyncConfig.java                ← Habilita @Async (Observer assíncrono)
│   ├── CorsConfig.java                 ← Libera origens do frontend
│   └── OpenApiConfig.java              ← Metadados do Swagger
│
├── controller/                         ← Camada C do MVC
│   ├── BookingController.java          ← /api/bookings
│   ├── PassengerController.java        ← /api/passengers
│   └── HealthController.java           ← /api/health
│
├── service/                            ← Camada de negócio do MVC
│   ├── BookingService.java             ← Interface
│   ├── PassengerService.java           ← Interface
│   └── impl/
│       ├── BookingServiceImpl.java     ← Orquestra todos os padrões
│       └── PassengerServiceImpl.java
│
├── entity/                             ← Camada M do MVC (modelo de domínio)
│   ├── AbstractEntity.java             ← Classe abstrata base (id, timestamps)
│   ├── Booking.java                    ← Entidade de reserva
│   ├── Passenger.java                  ← Entidade de passageiro
│   └── Payment.java                    ← Entidade de pagamento
│
├── repository/                         ← Acesso ao banco (Spring Data JPA)
│   ├── BookingRepository.java
│   ├── PassengerRepository.java
│   └── PaymentRepository.java
│
├── dto/
│   ├── request/
│   │   ├── CreateBookingRequest.java   ← Dados de entrada da reserva
│   │   └── PassengerDataRequest.java   ← Dados de entrada do passageiro
│   └── response/
│       ├── ApiResponse.java            ← Envelope genérico de resposta
│       ├── BookingResponse.java        ← Saída da reserva
│       └── PassengerResponse.java      ← Saída do passageiro
│
├── enums/
│   ├── BookingStatus.java              ← PENDING, CONFIRMED, CANCELLED, COMPLETED
│   ├── DocType.java                    ← CPF, RG, PASSPORT
│   ├── FlightClass.java                ← ECONOMY, PREMIUM_ECONOMY, EXECUTIVE
│   ├── FlightType.java                 ← ONEWAY, ROUNDTRIP
│   ├── PaymentMethod.java              ← CREDIT_CARD, DEBIT_CARD, PIX, BOLETO
│   └── PaymentStatus.java             ← PENDING, PAID, REFUNDED, FAILED
│
├── exception/
│   ├── BusinessException.java          ← Violação de regra de negócio (422)
│   ├── ResourceNotFoundException.java  ← Recurso não encontrado (404)
│   └── GlobalExceptionHandler.java     ← Tratamento centralizado com @RestControllerAdvice
│
├── strategy/pricing/                   ← Padrão Strategy
│   ├── PriceStrategy.java              ← Interface da estratégia
│   ├── AbstractPriceStrategy.java      ← Classe abstrata com Template Method
│   ├── EconomyPriceStrategy.java       ← Tarifa Economy (R$ 350)
│   ├── PremiumEconomyPriceStrategy.java← Tarifa Premium Economy (R$ 750)
│   └── ExecutivePriceStrategy.java     ← Tarifa Executiva (R$ 1.800)
│
├── factory/                            ← Padrão Factory
│   ├── PriceStrategyFactory.java       ← Resolve a Strategy pelo FlightClass
│   └── PaymentFactory.java             ← Cria instâncias de Payment com validação
│
├── validator/                          ← Template Method + Chain of Responsibility
│   ├── AbstractBookingValidator.java   ← Classe abstrata que define o esqueleto
│   ├── BookingValidatorChain.java      ← Monta e dispara a cadeia
│   ├── RouteValidator.java             ← Valida origem != destino
│   ├── RoundTripDateValidator.java     ← Valida datas de ida e volta
│   └── SeatAvailabilityValidator.java  ← Valida disponibilidade do assento
│
├── observer/                           ← Padrão Observer
│   ├── event/
│   │   ├── BookingCreatedEvent.java    ← Evento: reserva criada
│   │   └── BookingCancelledEvent.java  ← Evento: reserva cancelada
│   └── listener/
│       └── BookingNotificationListener.java ← Reage aos eventos (assíncrono)
│
└── mapper/
    └── BookingMapper.java              ← Converte entity ↔ DTO

src/test/java/com/voo/airline/
├── BookingServiceTest.java             ← Testes legados do service
├── factory/
│   ├── PaymentFactoryTest.java         ← Testa PaymentFactory
│   └── PriceStrategyFactoryTest.java   ← Testa PriceStrategyFactory
├── service/
│   ├── BookingEntityTest.java          ← Testa encapsulamento das entidades
│   └── BookingServiceImplTest.java     ← Testa orquestração do Service
├── strategy/
│   └── PriceStrategyTest.java          ← Testa as três strategies e o Template Method
└── validator/
    └── BookingValidatorChainTest.java  ← Testa a cadeia de validação
```

---

## Padrões de Projeto

### MVC (Model-View-Controller)

A divisão de responsabilidades segue o padrão MVC adaptado para APIs REST:

- **Model** — pacote `entity/`: as classes `Booking`, `Passenger` e `Payment` representam o domínio. Não contêm lógica HTTP nem SQL.
- **Controller** — pacote `controller/`: recebe a requisição HTTP, delega ao Service e devolve a resposta. Não contém regras de negócio.
- **Service** — atua como a camada intermediária que orquestra a lógica de negócio, os repositórios e os demais padrões.

O pacote `dto/` separa os contratos da API (o que entra e o que sai) dos objetos de domínio, evitando que mudanças internas quebrem o contrato com o frontend.

### Strategy

**Onde:** `strategy/pricing/`

**Problema resolvido:** o cálculo de preço muda conforme a classe da cabine. Sem Strategy, o Service teria um bloco `if/else` ou `switch` que cresceria a cada nova classe, violando o princípio Aberto/Fechado.

**Como funciona:** a interface `PriceStrategy` define o contrato `calculate(FlightType, int)`. Cada classe de cabine tem sua própria implementação:

```
PriceStrategy (interface)
    └── AbstractPriceStrategy (abstract) ← Template Method aqui
            ├── EconomyPriceStrategy         R$ 350 base
            ├── PremiumEconomyPriceStrategy  R$ 750 base
            └── ExecutivePriceStrategy       R$ 1.800 base, multiplier 1.8x
```

O `BookingServiceImpl` nunca sabe qual implementação está usando — recebe sempre `PriceStrategy` e chama `calculate()`. Isso é polimorfismo em ação.

### Template Method

**Onde:** `AbstractPriceStrategy` e `AbstractBookingValidator`

**Problema resolvido:** o algoritmo geral é sempre o mesmo, mas alguns passos variam entre subclasses.

**Em `AbstractPriceStrategy`:** o método `calculate()` é `final` e define o esqueleto:
1. Obtém o preço base da subclasse (`getBasePrice()` — abstrato)
2. Aplica o multiplicador de roundtrip (`getRoundTripMultiplier()` — hook, pode ser sobrescrito)
3. Multiplica pelos passageiros
4. Aplica desconto de grupo (`applyGroupDiscount()` — hook, pode ser sobrescrito)

A `ExecutivePriceStrategy` sobrescreve os dois hooks: usa multiplicador `1.8x` em vez de `2x` na volta, e desabilita o desconto de grupo.

**Em `AbstractBookingValidator`:** o método `validate()` é `final` e executa `doValidate()` (abstrato, implementado pela subclasse) e depois passa para o próximo da cadeia automaticamente.

### Factory

**Onde:** `factory/PriceStrategyFactory.java` e `factory/PaymentFactory.java`

**`PriceStrategyFactory`:** recebe um `FlightClass` e retorna a `PriceStrategy` concreta correspondente. O Service nunca instancia strategies diretamente — a fábrica centraliza essa decisão. O Spring injeta automaticamente o mapa de beans `Map<String, PriceStrategy>`.

**`PaymentFactory`:** cria instâncias de `Payment` aplicando validações de negócio antes da criação (ex: Boleto exige valor mínimo de R$ 50). Centraliza a lógica de criação num único ponto.

### Observer

**Onde:** `observer/event/` e `observer/listener/`

**Problema resolvido:** quando uma reserva é criada ou cancelada, outras partes do sistema precisam reagir (notificações, auditoria, integração com sistemas externos). Acoplar essas responsabilidades ao Service tornaria o código frágil e difícil de estender.

**Como funciona:** o `BookingServiceImpl` publica eventos via `ApplicationEventPublisher` do Spring:

```java
eventPublisher.publishEvent(new BookingCreatedEvent(this, booking));
```

O `BookingNotificationListener` escuta esses eventos com `@EventListener` e reage de forma **assíncrona** (`@Async`), sem bloquear a resposta HTTP. O Service não sabe que o Listener existe — é o Spring que gerencia o acoplamento.

### Chain of Responsibility

**Onde:** `validator/`

**Problema resolvido:** existem múltiplas regras de validação para uma reserva. Colocá-las todas no Service cria um método longo e difícil de manter. Adicionar uma nova regra exige mexer no Service.

**Como funciona:** cada validador estende `AbstractBookingValidator` e implementa apenas sua regra específica. A cadeia é montada em `BookingValidatorChain` e disparada com uma única chamada:

```
RouteValidator → RoundTripDateValidator → SeatAvailabilityValidator
```

Se qualquer validador lançar `BusinessException`, a cadeia para imediatamente. O Service chama apenas `validatorChain.validateAll(request)`.

---

## Conceitos de Orientação a Objetos

### Herança

`AbstractEntity` é a superclasse de todas as entidades JPA. Centraliza `id`, `createdAt` e `updatedAt`, além de uma implementação correta de `equals()` e `hashCode()` para o contexto JPA (comparação por id persistido).

`AbstractPriceStrategy` é a superclasse de todas as strategies de preço. Implementa o Template Method e herda `PriceStrategy`.

`AbstractBookingValidator` é a superclasse de todos os validadores. Define a cadeia de responsabilidade e o esqueleto de validação.

### Encapsulamento

As entidades `Booking`, `Passenger` e `Payment` têm construtores `protected` — o JPA os usa internamente, mas o código de aplicação nunca pode chamar `new Booking()` diretamente. A criação de objetos válidos passa obrigatoriamente pelos factory methods estáticos:

- `Passenger.of(...)` — valida que o nome não é vazio
- `Booking.create(...)` — sempre inicia com status `CONFIRMED`
- `Payment.of(...)` — sempre inicia com status `PENDING`

O status de `Booking` só muda por métodos de negócio que guardam as invariantes do domínio:

```java
booking.cancel();    // valida que não está já cancelada
booking.complete();  // valida que está CONFIRMED antes de completar
```

As listas internas (`bookings` em `Passenger`, `payments` em `Booking`) são expostas como cópias imutáveis via `Collections.unmodifiableList()`, impedindo que código externo modifique o estado interno das entidades.

### Polimorfismo

O `BookingServiceImpl` recebe uma `PriceStrategy` da factory e chama `calculate()`. Não sabe — e não precisa saber — se é `EconomyPriceStrategy` ou `ExecutivePriceStrategy`. O comportamento correto é resolvido em tempo de execução.

Da mesma forma, o `BookingValidatorChain` chama `validate()` em cada `AbstractBookingValidator` sem conhecer a implementação concreta.

### Abstração

As interfaces `PriceStrategy`, `BookingService` e `PassengerService` definem contratos sem expor implementação. O código cliente depende sempre da interface, nunca da classe concreta — facilitando substituição, teste e extensão.

---

## Banco de Dados

O Flyway gerencia as migrações automaticamente. O arquivo `V1__create_initial_tables.sql` cria todas as tabelas e tipos ENUM nativos do PostgreSQL na primeira execução.

### Esquema

**passengers**

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGSERIAL PK | Identificador |
| name | VARCHAR(255) | Nome completo |
| email | VARCHAR(255) | E-mail |
| phone | VARCHAR(30) | Telefone |
| cpf | VARCHAR(20) | CPF (apenas dígitos) |
| birth_date | DATE | Data de nascimento |
| doc_type | ENUM | CPF / RG / PASSPORT |
| doc_number | VARCHAR(60) | Número do documento |
| created_at | TIMESTAMP | Criação automática |
| updated_at | TIMESTAMP | Atualização automática |

**bookings**

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGSERIAL PK | Identificador |
| locator | VARCHAR(20) UNIQUE | Localizador (ex: VOOABC123) |
| flight_num | VARCHAR(20) | Número do voo (ex: VO1234) |
| origin | VARCHAR(10) | Código IATA de origem |
| destination | VARCHAR(10) | Código IATA de destino |
| dep_date | DATE | Data de ida |
| ret_date | DATE | Data de volta (nullable) |
| flight_type | ENUM | ONEWAY / ROUNDTRIP |
| flight_class | ENUM | ECONOMY / PREMIUM_ECONOMY / EXECUTIVE |
| seat | VARCHAR(10) | Poltrona (ex: 12A) |
| gate | VARCHAR(10) | Portão de embarque |
| aircraft | VARCHAR(100) | Nome da aeronave |
| departure | VARCHAR(10) | Horário de partida |
| boarding | VARCHAR(10) | Horário de embarque |
| total_price | NUMERIC(10,2) | Preço total calculado |
| status | ENUM | PENDING / CONFIRMED / CANCELLED / COMPLETED |
| passenger_id | BIGINT FK | Referência ao passageiro |
| created_at | TIMESTAMP | Criação automática |
| updated_at | TIMESTAMP | Atualização automática |

**payments**

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGSERIAL PK | Identificador |
| booking_id | BIGINT FK | Referência à reserva |
| method | ENUM | CREDIT_CARD / DEBIT_CARD / PIX / BOLETO |
| amount | NUMERIC(10,2) | Valor cobrado |
| status | ENUM | PENDING / PAID / REFUNDED / FAILED |
| paid_at | TIMESTAMP | Momento do pagamento (nullable) |
| created_at | TIMESTAMP | Criação automática |
| updated_at | TIMESTAMP | Atualização automática |

---

## API REST

Todas as respostas seguem o envelope padrão `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Reserva criada com sucesso!",
  "data": { ... },
  "timestamp": "2025-08-01T14:30:00"
}
```

Em caso de erro:

```json
{
  "success": false,
  "error": "Descrição do problema",
  "timestamp": "2025-08-01T14:30:00"
}
```

### Reservas — `/api/bookings`

**POST `/api/bookings`** — Criar reserva

Request body:
```json
{
  "flightNum":    "VO1234",
  "origin":       "GRU",
  "destination":  "SSA",
  "depDate":      "2025-08-15",
  "flightType":   "ONEWAY",
  "flightClass":  "ECONOMY",
  "seat":         "12A",
  "gate":         "B3",
  "aircraft":     "Boeing 737-800",
  "departure":    "14:30",
  "boarding":     "14:00",
  "payMethod":    "PIX",
  "passengerData": {
    "name":      "Ana Carolina Souza",
    "email":     "ana@email.com",
    "phone":     "(79) 99999-9999",
    "cpf":       "12345678900",
    "birthDate": "1995-05-15",
    "docType":   "CPF",
    "docNumber": "12345678900"
  }
}
```

Response `201 Created`:
```json
{
  "success": true,
  "message": "Reserva criada com sucesso!",
  "data": {
    "id": 1,
    "locator": "VOOXYZ789",
    "flightNum": "VO1234",
    "origin": "GRU",
    "destination": "SSA",
    "depDate": "2025-08-15",
    "flightType": "ONEWAY",
    "flightClass": "ECONOMY",
    "seat": "12A",
    "gate": "B3",
    "aircraft": "Boeing 737-800",
    "departure": "14:30",
    "boarding": "14:00",
    "totalPrice": 350.00,
    "status": "CONFIRMED",
    "passenger": {
      "id": 1,
      "name": "Ana Carolina Souza",
      "email": "ana@email.com",
      "cpf": "12345678900"
    },
    "createdAt": "2025-08-01T10:00:00"
  },
  "timestamp": "2025-08-01T10:00:00"
}
```

**GET `/api/bookings?page=0&size=20`** — Listar reservas paginado

**GET `/api/bookings/{locator}`** — Buscar pelo localizador (ex: `VOOXYZ789`)

**PATCH `/api/bookings/{locator}/cancel`** — Cancelar reserva

### Passageiros — `/api/passengers`

**GET `/api/passengers/{id}`** — Buscar por ID

**GET `/api/passengers/cpf/{cpf}`** — Buscar por CPF

**GET `/api/passengers/cpf/{cpf}/bookings`** — Reservas de um passageiro

### Health — `/api/health`

**GET `/api/health`** — Status da API e conexão com o banco

```json
{
  "status": "ok",
  "service": "vOO Airline API",
  "version": "1.0.0",
  "db": "connected",
  "timestamp": "2025-08-01T10:00:00"
}
```

### Valores aceitos pelos Enums

| Campo | Valores aceitos |
|-------|----------------|
| `flightType` | `ONEWAY`, `ROUNDTRIP` |
| `flightClass` | `ECONOMY`, `PREMIUM_ECONOMY`, `EXECUTIVE` |
| `payMethod` | `CREDIT_CARD`, `DEBIT_CARD`, `PIX`, `BOLETO` |
| `docType` | `CPF`, `RG`, `PASSPORT` |

---

## Regras de Negócio

**Localizador único** — gerado no padrão `VOO` + 6 caracteres alfanuméricos (ex: `VOOABC123`). Se o frontend enviar um localizador, ele é aproveitado desde que não esteja em uso.

**Passageiro reutilizado por CPF** — se o CPF já existe no banco, a nova reserva é vinculada ao cadastro existente em vez de criar um registro duplicado.

**Validação de rota** — origem e destino não podem ser o mesmo aeroporto.

**Validação de datas** — em voos de ida e volta (`ROUNDTRIP`), a data de retorno é obrigatória e deve ser posterior à data de ida.

**Validação de assento** — o sistema verifica se o assento já está ocupado para a mesma rota e data antes de confirmar.

**Preço calculado no backend** — o preço nunca é aceito como entrada do frontend. É calculado pela `PriceStrategy` correspondente à classe escolhida:

| Classe | Preço base | Ida e volta | Desconto de grupo (3+ pax) |
|--------|-----------|-------------|---------------------------|
| Economy | R$ 350 | × 2 | 5% sobre o total |
| Premium Economy | R$ 750 | × 2 | 5% sobre o total |
| Executiva | R$ 1.800 | × 1,8 (10% off na volta) | Sem desconto de grupo |

**Transições de status da reserva:**

```
PENDING → CONFIRMED → COMPLETED
                   ↘ CANCELLED
```

Uma reserva cancelada não pode ser reativada nem concluída. Tentar cancelar uma reserva já cancelada lança erro.

---

## Testes

São 6 classes de teste cobrindo cada camada e padrão de projeto individualmente, sem dependência do banco de dados (Mockito + H2).

| Classe | O que testa |
|--------|-------------|
| `PriceStrategyTest` | As três strategies, Template Method e polimorfismo |
| `PriceStrategyFactoryTest` | Resolução correta de strategy pelo FlightClass |
| `BookingValidatorChainTest` | Cada regra da cadeia de validação isolada |
| `PaymentFactoryTest` | Criação de Payment, validação de Boleto e transições de status |
| `BookingEntityTest` | Encapsulamento, factory methods e transições de estado das entidades |
| `BookingServiceImplTest` | Orquestração do Service: verifica que todos os colaboradores são acionados |

Para executar:

```bash
mvn test

# Relatório de cobertura (se configurado com JaCoCo)
mvn test jacoco:report
```

---

## Integração com o Frontend

O frontend (`index.html`) já contém a função `sendToBackend` que chama `POST /api/bookings`. Para conectar ao backend Java, garanta que:

1. O backend está rodando em `http://localhost:8080`
2. A origem do frontend está na lista de CORS em `application.properties`:
   ```properties
   voo.cors.allowed-origins=http://localhost:3000,http://localhost:5500,http://127.0.0.1:5500
   ```
3. Os valores dos enums enviados pelo frontend estão no formato correto (letras maiúsculas com underscore, ex: `CREDIT_CARD`, não `credit`)
