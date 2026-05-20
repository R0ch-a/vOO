# vOO Airways — Sistema de Reservas e Check-in

<img width="2209" height="1887" alt="Captura de tela 2026-05-20 012315" src="https://github.com/user-attachments/assets/b51fe1aa-769f-4b49-b075-d9742e66051e" />
<img width="2201" height="2016" alt="Captura de tela 2026-05-20 012256" src="https://github.com/user-attachments/assets/cbe105df-35f0-4f7f-a53a-be78e47d38f9" />

Sistema web completo de reservas e check-in de passagens aéreas, desenvolvido como projeto de **Programação Orientada a Objetos** com Java 17 e Spring Boot 3. O backend serve também o frontend HTML/CSS/JS, sem necessidade de servidor separado.

---

## Tecnologias

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.3.0 | Framework web e IoC |
| Spring Data JPA | 3.3.0 | Abstração de persistência |
| Hibernate | 6.x | ORM com suporte a ENUMs nativos do PostgreSQL |
| PostgreSQL | 14+ | Banco de dados relacional |
| Flyway | 10.x | Migrations versionadas do banco |
| Lombok | latest | Redução de boilerplate |
| SpringDoc OpenAPI | 2.5.0 | Documentação Swagger automática |
| JUnit 5 + Mockito | 5.x | Testes unitários e de camada web |
| Maven | 3.9+ | Gerenciamento de dependências e build |

---

## Pré-requisitos

- JDK 17 ou superior
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
mvn spring-boot:run "-Dmaven.test.skip=true"
```

Após iniciar, acesse:

- **Frontend:** http://localhost:8080
- **API:** http://localhost:8080/api/bookings
- **Swagger UI:** http://localhost:8080/swagger-ui.html

O Flyway cria todas as tabelas automaticamente na primeira execução.

**4. Rodar os testes**

```bash
mvn test
```

---

## Páginas do Frontend

| Arquivo | Rota | Descrição |
|---------|------|-----------|
| `index.html` | `/` | Booking flow: busca de voos, seleção de classe/assento, pagamento e boarding pass |
| `my-trips.html` | `/my-trips.html` | Histórico de viagens por CPF com PiPs de boarding pass e gerenciamento |
| `check-in.html` | `/check-in.html` | Check-in em 3 etapas: localizar voo, confirmar assento, boarding pass |

---

## Documentação interativa (Swagger)

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
├── service/                            ← Camada de negócio
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
│   ├── EconomyPriceStrategy.java       ← Tarifa Economy (R$ 320)
│   ├── PremiumEconomyPriceStrategy.java← Tarifa Premium Economy (R$ 980)
│   └── ExecutivePriceStrategy.java     ← Tarifa Executiva (R$ 1.240)
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
├── entity/
│   ├── BookingTest.java                ← cancel(), complete(), isCancellable(), create()
│   └── PassengerTest.java              ← of(), updateContactInfo(), imutabilidade
├── service/impl/
│   └── BookingServiceImplTest.java     ← Orquestração com Mockito (13 testes)
└── controller/
    └── BookingControllerTest.java      ← 5 endpoints via MockMvc (9 testes)
```

---

## Padrões de Projeto

### MVC (Model-View-Controller)

- **Model** — pacote `entity/`: `Booking`, `Passenger` e `Payment` representam o domínio sem lógica HTTP nem SQL.
- **Controller** — pacote `controller/`: recebe requisições HTTP, delega ao Service e devolve a resposta.
- **Service** — orquestra a lógica de negócio, repositórios e padrões de projeto.

O pacote `dto/` separa os contratos da API dos objetos de domínio, evitando que mudanças internas quebrem o contrato com o frontend.

### Strategy

**Onde:** `strategy/pricing/`

O cálculo de preço varia por classe de cabine. A interface `PriceStrategy` define o contrato `calculate(FlightType, int)`:

```
PriceStrategy (interface)
    └── AbstractPriceStrategy (abstract) ← Template Method aqui
            ├── EconomyPriceStrategy         R$ 320 base
            ├── PremiumEconomyPriceStrategy  R$ 980 base
            └── ExecutivePriceStrategy       R$ 1.240 base
```

### Template Method

**Onde:** `AbstractPriceStrategy` e `AbstractBookingValidator`

Em `AbstractPriceStrategy`, o método `calculate()` é `final` e define o esqueleto do algoritmo. As subclasses implementam apenas `getBasePrice()` (abstrato) e podem sobrescrever hooks como `getRoundTripMultiplier()`.

Em `AbstractBookingValidator`, o método `validate()` é `final` e executa `doValidate()` (implementado pela subclasse) e passa para o próximo da cadeia automaticamente.

### Factory

**Onde:** `factory/`

- **PriceStrategyFactory:** recebe um `FlightClass` e retorna a `PriceStrategy` concreta. O Spring injeta automaticamente o mapa de beans `Map<String, PriceStrategy>`.
- **PaymentFactory:** cria instâncias de `Payment` com validações de negócio antes da criação.

### Observer

**Onde:** `observer/`

O `BookingServiceImpl` publica eventos via `ApplicationEventPublisher`:

```java
eventPublisher.publishEvent(new BookingCreatedEvent(this, booking));
```

O `BookingNotificationListener` escuta com `@EventListener` e reage de forma **assíncrona** (`@Async`), sem bloquear a resposta HTTP.

### Chain of Responsibility

**Onde:** `validator/`

Cada validador implementa apenas sua regra. A cadeia é:

```
RouteValidator → RoundTripDateValidator → SeatAvailabilityValidator
```

O Service chama apenas `validatorChain.validateAll(request)`.

---

## Conceitos de Orientação a Objetos

### Encapsulamento

As entidades têm construtores `protected` — criação sempre via factory methods estáticos:

- `Passenger.of(...)` — valida que o nome não é vazio
- `Booking.create(...)` — sempre inicia com status `CONFIRMED`
- `Payment.of(...)` — sempre inicia com status `PENDING`

O status de `Booking` só muda por métodos de negócio:

```java
booking.cancel();    // valida que não está já cancelada
booking.complete();  // valida que está CONFIRMED antes de concluir
```

As listas internas são expostas como cópias imutáveis via `Collections.unmodifiableList()`.

### Herança

- `AbstractEntity` — superclasse de todas as entidades JPA: centraliza `id`, `createdAt`, `updatedAt`, `equals()` e `hashCode()`.
- `AbstractPriceStrategy` — superclasse das strategies, implementa Template Method.
- `AbstractBookingValidator` — superclasse dos validadores, define a cadeia.

### Polimorfismo e Abstração

O `BookingServiceImpl` opera sobre `PriceStrategy` e `AbstractBookingValidator` sem conhecer as implementações concretas. O comportamento correto é resolvido em tempo de execução.

---

## API REST

Todas as respostas seguem o envelope `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Reserva criada com sucesso!",
  "data": { ... },
  "timestamp": "2026-05-20T10:00:00"
}
```

### Reservas — `/api/bookings`

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| `POST` | `/api/bookings` | 201 | Criar nova reserva |
| `GET` | `/api/bookings/{locator}` | 200 / 404 | Buscar pelo localizador |
| `GET` | `/api/bookings?page=0&size=20` | 200 | Listar paginado |
| `PATCH` | `/api/bookings/{locator}/cancel` | 200 / 404 | Cancelar reserva |
| `PATCH` | `/api/bookings/{locator}/complete` | 200 / 404 | Concluir reserva via check-in |

### Passageiros — `/api/passengers`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/passengers/cpf/{cpf}/bookings` | Reservas de um passageiro por CPF |
| `GET` | `/api/passengers/{id}` | Buscar por ID |

### Health — `/api/health`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/health` | Status da API e conexão com o banco |

### Exemplo — POST `/api/bookings`

```json
{
  "flightNum":    "VO1234",
  "origin":       "GIG",
  "destination":  "LHR",
  "depDate":      "2026-08-15",
  "flightType":   "ROUNDTRIP",
  "flightClass":  "EXECUTIVE",
  "seat":         "01A",
  "gate":         "A1",
  "aircraft":     "Boeing 787-9 Dreamliner",
  "departure":    "08:00",
  "boarding":     "07:30",
  "payMethod":    "CREDIT_CARD",
  "passengerData": {
    "name":      "Ana Carolina Souza",
    "email":     "ana@email.com",
    "cpf":       "12345678900",
    "docType":   "PASSPORT",
    "docNumber": "AB123456"
  }
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

**Localizador único** — gerado no padrão `VOO` + 6 caracteres alfanuméricos (ex: `VOOABC123`). O frontend pode sugerir um localizador, mas o backend garante a unicidade.

**Passageiro reutilizado por CPF** — se o CPF já existe no banco, a nova reserva é vinculada ao cadastro existente.

**Preço calculado no backend** — nunca aceito como entrada do frontend:

| Classe | Preço base |
|--------|-----------|
| Economy (Standard) | R$ 320 |
| Premium Economy (Business Plus) | R$ 980 |
| Executive (First Class) | R$ 1.240 |

**Transições de status:**

```
PENDING → CONFIRMED → COMPLETED
                   ↘ CANCELLED
```

Uma reserva cancelada não pode ser reativada nem concluída.

---

## Banco de Dados

O Flyway executa `V1__create_initial_tables.sql` automaticamente na primeira inicialização, criando as tabelas e os tipos ENUM nativos do PostgreSQL.

**passengers:** `id`, `name`, `email`, `phone`, `cpf`, `birth_date`, `doc_type`, `doc_number`, `created_at`, `updated_at`

**bookings:** `id`, `locator` (UNIQUE), `flight_num`, `origin`, `destination`, `dep_date`, `ret_date`, `flight_type`, `flight_class`, `seat`, `gate`, `aircraft`, `departure`, `boarding`, `total_price`, `status`, `passenger_id` (FK), `created_at`, `updated_at`

**payments:** `id`, `booking_id` (FK), `method`, `amount`, `status`, `paid_at`, `created_at`, `updated_at`

---

## Testes

90 testes passando — 0 falhas.

| Classe | Testes | O que cobre |
|--------|--------|-------------|
| `BookingTest` | 10 | `cancel()`, `complete()`, `isCancellable()`, `isRoundTrip()`, `Booking.create()` |
| `PassengerTest` | 7 | `Passenger.of()`, `updateContactInfo()`, imutabilidade de listas |
| `BookingServiceImplTest` | 13 | Orquestração com Mockito: create, find, cancel, complete e eventos |
| `BookingControllerTest` | 9 | 5 endpoints via MockMvc: status HTTP, body JSON e 404 handling |
| Testes existentes | 51 | Strategy, Factory, ValidatorChain, PaymentFactory, entidades |

```bash
mvn test
```
