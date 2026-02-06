# PSS MT - API Backend Java Senior

**Processo Seletivo Simplificado - Edital 001/2026/SEPLAG**
**Secretaria de Estado de Planejamento e Gestao de Mato Grosso**

API REST para gerenciamento de artistas e albuns musicais, desenvolvida como projeto de avaliacao tecnica para a vaga de **Analista de Tecnologia da Informacao - Perfil Engenheiro da Computacao (Nivel SENIOR)**.

---

## Dados do Candidato

| Campo | Valor |
|-------|-------|
| **Nome** | Jean Paulo Sassi de Miranda |
| **CPF (6 digitos)** | 015014 |
| **Inscricao** | 16568 |
| **Vaga** | Back-End Java Senior |
| **Edital** | Processo Seletivo Simplificado no 001/2026/SEPLAG |

---

## Arquitetura do Projeto

### Visao Geral

O projeto segue uma **arquitetura em camadas** (Layered Architecture), inspirada nos principios de Clean Architecture e Domain-Driven Design (DDD), proporcionando separacao clara de responsabilidades e facilitando a manutencao e evolucao do codigo.

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   REST      │  │  WebSocket  │  │    DTOs     │             │
│  │  Resources  │  │  Endpoints  │  │  (Request/  │             │
│  │             │  │             │  │  Response)  │             │
│  └──────┬──────┘  └──────┬──────┘  └─────────────┘             │
└─────────┼────────────────┼──────────────────────────────────────┘
          │                │
          ▼                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Services                              │   │
│  │  (ArtistaService, AlbumService, AuthService, etc.)      │   │
│  └──────────────────────────┬──────────────────────────────┘   │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   Models    │  │ Repositories│  │ Exceptions  │             │
│  │  (Artista,  │  │ (Interfaces)│  │  (Business  │             │
│  │   Album)    │  │             │  │   Rules)    │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Security │  │ Storage  │  │  Client  │  │ Exception│       │
│  │  (JWT,   │  │ (MinIO)  │  │(Regionais│  │ Handlers │       │
│  │RateLimit)│  │          │  │   API)   │  │          │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### Justificativas Tecnicas

| Tecnologia | Justificativa |
|------------|---------------|
| **Quarkus 3.17** | Framework Java cloud-native com startup ultrarrapido (~50ms), baixo consumo de memoria e suporte nativo a GraalVM. Ideal para containerizacao e microservicos. |
| **Panache** | Simplifica o acesso a dados com Active Record pattern, reduzindo boilerplate e aumentando produtividade sem perder flexibilidade. |
| **SmallRye JWT** | Implementacao robusta de MicroProfile JWT para autenticacao stateless, compativel com padrao RFC 7519. |
| **Flyway** | Migrations de banco versionadas garantem rastreabilidade e reproducibilidade do schema em qualquer ambiente. |
| **MinIO** | Object storage compativel com S3, permite armazenamento escalavel de imagens sem depender de servicos cloud proprietarios. |
| **BCrypt** | Algoritmo padrao da industria para hash de senhas com salt automatico e work factor configuravel. |

---

## Stack Tecnologica

| Tecnologia | Versao | Descricao |
|------------|--------|-----------|
| Java | 21 LTS | Linguagem de programacao (ultima versao LTS) |
| Quarkus | 3.17.5 | Framework supersonico subatomico |
| PostgreSQL | 16 | Banco de dados relacional |
| MinIO | Latest | Object Storage (S3 Compatible) |
| Flyway | - | Migrations de banco de dados |
| SmallRye JWT | - | Autenticacao JWT (RFC 7519) |
| BCrypt | 0.4 | Hash de senhas (work factor 12) |
| Docker | - | Containerizacao |

---

## Requisitos

- Docker e Docker Compose instalados
- Java 21+ (apenas para desenvolvimento local)
- Maven 3.9+ (apenas para desenvolvimento local)

---

## Inicio Rapido

### 1. Clone o repositorio

```bash
git clone https://github.com/jeanpaulosm/jeanpaulosassidemiranda015014.git
cd jeanpaulossasidemiranda015014
```

### 2. Inicie com Docker Compose

```bash
docker-compose up -d
```

**Pronto!** Nao e necessaria nenhuma configuracao adicional.

Este comando:
1. Constroi a imagem da API
2. **Gera automaticamente as chaves JWT** (RSA 2048 bits)
3. Inicia todos os servicos
4. Executa as migrations do banco
5. Cria o bucket no MinIO

**Servicos iniciados:**
- **API** (porta 8080) - Aplicacao Java Quarkus
- **PostgreSQL** (porta 5432) - Banco de dados
- **MinIO** (portas 9000/9001) - Object Storage

**Log esperado no primeiro inicio:**
```
=== Verificando chaves JWT ===
Chaves JWT nao encontradas. Gerando automaticamente...
Chaves JWT geradas com sucesso!
  - Chave privada: /app/secrets/privateKey.pem
  - Chave publica: /app/secrets/publicKey.pem

=== PSS Backend MT - Iniciando aplicacao ===
...
pss-backend-mt 1.0.0 started in 3.0s. Listening on: http://0.0.0.0:8080
```

### 3. Acesse os servicos

| Servico | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/q/swagger-ui |
| **WebSocket Test** | http://localhost:8080/websocket-test.html |
| Health Check | http://localhost:8080/q/health |
| MinIO Console | http://localhost:9001 |

### 4. Credenciais e Roles

O sistema implementa controle de acesso baseado em roles (RBAC). Foram criadas duas roles para demonstrar a diferenciacao de permissoes:

| Usuario | Senha | Role | Permissoes |
|---------|-------|------|------------|
| `admin` | `admin123` | **ADMIN** | Acesso total (CRUD completo) |
| `user` | `user123` | **USER** | Apenas leitura (GET) |

**Para testar a API, utilize as credenciais do `admin`**, que possui acesso completo a todas as funcionalidades.

O usuario `user` tem acesso limitado - pode apenas consultar (GET) artistas, albuns e regionais, mas nao pode criar, atualizar ou deletar.

**MinIO Console:**
- Usuario: `minioadmin`
- Senha: `minioadmin`

---

## Endpoints da API

### Autenticacao

| Metodo | Endpoint | Descricao | Acesso |
|--------|----------|-----------|--------|
| POST | `/api/v1/auth/login` | Realiza login e retorna JWT | Publico |
| POST | `/api/v1/auth/refresh` | Renova token JWT | Publico |

### Artistas

| Metodo | Endpoint | Descricao | Acesso |
|--------|----------|-----------|--------|
| GET | `/api/v1/artistas` | Lista artistas (paginado, filtros) | USER, ADMIN |
| GET | `/api/v1/artistas/{id}` | Busca artista por ID | USER, ADMIN |
| POST | `/api/v1/artistas` | Cria novo artista | ADMIN |
| PUT | `/api/v1/artistas/{id}` | Atualiza artista | ADMIN |
| DELETE | `/api/v1/artistas/{id}` | Remove artista | ADMIN |

### Albuns

| Metodo | Endpoint | Descricao | Acesso |
|--------|----------|-----------|--------|
| GET | `/api/v1/albuns` | Lista albuns (paginado, filtros) | USER, ADMIN |
| GET | `/api/v1/albuns/{id}` | Busca album por ID | USER, ADMIN |
| POST | `/api/v1/albuns` | Cria novo album | ADMIN |
| PUT | `/api/v1/albuns/{id}` | Atualiza album | ADMIN |
| DELETE | `/api/v1/albuns/{id}` | Remove album | ADMIN |
| POST | `/api/v1/albuns/{id}/imagens` | Upload de imagens | ADMIN |
| GET | `/api/v1/albuns/{id}/imagens` | Lista imagens (presigned URLs) | USER, ADMIN |
| DELETE | `/api/v1/albuns/{albumId}/imagens/{imagemId}` | Remove imagem | ADMIN |

### Regionais

| Metodo | Endpoint | Descricao | Acesso |
|--------|----------|-----------|--------|
| GET | `/api/v1/regionais` | Lista regionais (filtro apenasAtivas) | USER, ADMIN |
| GET | `/api/v1/regionais/{id}` | Busca regional por ID | USER, ADMIN |
| GET | `/api/v1/regionais/estatisticas` | Estatisticas (total, ativas, inativas) | USER, ADMIN |
| POST | `/api/v1/regionais/sincronizar` | Sincroniza com API externa | ADMIN |

### WebSocket

| Metodo | Endpoint | Descricao | Acesso |
|--------|----------|-----------|--------|
| POST | `/api/v1/ws/ticket` | Gera ticket para conexao WebSocket | USER, ADMIN |
| GET | `/api/v1/ws/stats` | Estatisticas do sistema de tickets | ADMIN |
| WS | `ws://host/ws/albuns?ticket=xxx` | WebSocket de notificacoes | Ticket valido |

### Health Checks (Kubernetes Ready)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/q/health/live` | Liveness probe |
| GET | `/q/health/ready` | Readiness probe |
| GET | `/q/health` | Status geral |

**Health Checks Customizados Implementados:**

| Componente | Tipo | Descricao |
|------------|------|-----------|
| PostgreSQL Database | Readiness | Verifica conectividade e executa query de validacao |
| MinIO/S3 Storage | Readiness | Verifica acesso ao bucket de imagens |
| Regionais External API | Readiness | Testa endpoint externo de regionais |
| JWT Configuration | Readiness | Valida presenca das chaves publica/privada |

---

## Exemplos de Uso

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Resposta:**
```json
{
  "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 300
}
```

### Listar Artistas (com filtros)

```bash
curl -X GET "http://localhost:8080/api/v1/artistas?tipo=BANDA&sortField=nome&sortDir=asc&page=0&size=10" \
  -H "Authorization: Bearer <seu_token>"
```

### Criar Album

```bash
curl -X POST http://localhost:8080/api/v1/albuns \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <seu_token>" \
  -d '{
    "titulo": "Novo Album",
    "anoLancamento": 2024,
    "artistasIds": [1, 2]
  }'
```

### Upload de Imagens

```bash
curl -X POST http://localhost:8080/api/v1/albuns/1/imagens \
  -H "Authorization: Bearer <seu_token>" \
  -F "files=@imagem1.jpg" \
  -F "files=@imagem2.png"
```

### Sincronizar Regionais

```bash
curl -X POST http://localhost:8080/api/v1/regionais/sincronizar \
  -H "Authorization: Bearer <seu_token>"
```

---

## Guia de Testes

### 1. Iniciar o Ambiente

```bash
docker-compose up -d
```

Aguarde os servicos ficarem saudaveis (~30 segundos):
```bash
docker-compose ps
```

### 2. Acessar Swagger UI

Abra no navegador: **http://localhost:8080/q/swagger-ui**

O Swagger UI permite testar todos os endpoints da API com interface grafica.

**Para autenticar (use o usuario ADMIN para acesso completo):**
1. Execute `POST /api/v1/auth/login` com:
   ```json
   {"username": "admin", "password": "admin123"}
   ```
2. Copie o `accessToken` da resposta
3. Clique em "Authorize" (cadeado) e cole: `Bearer <seu_token>`

**Nota:** O usuario `user` tem acesso limitado (apenas leitura).

### 3. Testes Especificos (via terminal)

**Rate Limit (10 req/min):**
```bash
# Executar 12 requisicoes - as ultimas retornam 429
for i in {1..12}; do
  curl -s -o /dev/null -w "Request $i: %{http_code}\n" http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
done
```

**Health Checks:**
```bash
curl -s http://localhost:8080/q/health
```

**Metricas Prometheus:**
```bash
curl -s http://localhost:8080/q/metrics | head -50
```

### 4. Limpar Ambiente

```bash
docker-compose down -v
```

---

## WebSocket - Notificacao de Novos Albuns

O WebSocket implementa autenticacao via **Ticket System**, um padrao de seguranca usado por AWS API Gateway, Firebase e outras plataformas enterprise.

### Fluxo de Autenticacao

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Cliente   │         │   REST API  │         │  WebSocket  │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       │ 1. POST /api/v1/ws/ticket                     │
       │   (Authorization: Bearer JWT)                 │
       │──────────────────────>│                       │
       │                       │                       │
       │ 2. { ticket: "uuid",  │                       │
       │      expiresIn: 30 }  │                       │
       │<──────────────────────│                       │
       │                       │                       │
       │ 3. ws://host/ws/albuns?ticket=uuid            │
       │──────────────────────────────────────────────>│
       │                       │                       │
       │                       │  4. Valida ticket     │
       │                       │     (single-use)      │
       │                       │                       │
       │ 5. CONNECTED (username, roles)                │
       │<──────────────────────────────────────────────│
```

### Exemplo de Uso (JavaScript)

```javascript
// 1. Obter ticket (autenticado com JWT)
const response = await fetch('http://localhost:8080/api/v1/ws/ticket', {
  method: 'POST',
  headers: { 'Authorization': 'Bearer ' + jwtToken }
});
const { ticket, websocketUrl } = await response.json();

// 2. Conectar ao WebSocket com ticket
const ws = new WebSocket(websocketUrl);
// Ou: new WebSocket(`ws://localhost:8080/ws/albuns?ticket=${ticket}`);

ws.onopen = () => {
  console.log('Conectado ao WebSocket');
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);

  if (data.type === 'CONNECTED') {
    console.log(`Autenticado como: ${data.data.username}`);
    console.log(`Roles: ${data.data.roles}`);
  }

  if (data.type === 'NEW_ALBUM') {
    console.log('Novo album criado:', data.data);
  }
};

ws.onerror = (error) => {
  console.error('Erro no WebSocket:', error);
};

// Heartbeat (ping/pong)
setInterval(() => {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send('ping');
  }
}, 30000);
```

### Caracteristicas de Seguranca do Ticket System

| Caracteristica | Descricao |
|----------------|-----------|
| **Single-use** | Ticket e invalidado apos primeira conexao |
| **Curta duracao** | Expira em 30 segundos (configuravel) |
| **UUID v4** | 122 bits de entropia (5.3 x 10^36 combinacoes) |
| **Nao expoe JWT** | Token sensivel nunca aparece na URL |
| **Vinculado ao usuario** | Contem username e roles do JWT original |

### Por que Ticket System ao inves de JWT na URL?

| Aspecto | JWT na URL | Ticket System |
|---------|------------|---------------|
| **Exposicao em logs** | JWT completo (5 min validade) | UUID curto (30s validade) |
| **Reutilizavel se vazado** | Sim, ate expirar | Nao, single-use |
| **Tamanho na URL** | ~500 caracteres | 36 caracteres |
| **Seguranca** | Boa | Excelente |

### Metricas de WebSocket

| Metrica | Descricao |
|---------|-----------|
| `websocket_connections_active` | Numero de clientes conectados |
| `websocket_auth_total{status=success}` | Autenticacoes bem sucedidas |
| `websocket_auth_total{status=failure,reason=*}` | Falhas por motivo (expired, not_found, etc) |

---

## Caracteristicas Tecnicas

### Seguranca

| Feature | Descricao |
|---------|-----------|
| **JWT (5 minutos)** | Tokens de curta duracao para maior seguranca |
| **Refresh Token (24h)** | Para renovacao automatica do acesso |
| **Chaves JWT Auto-geradas** | RSA 2048 bits geradas automaticamente no primeiro inicio |
| **Rate Limit Inteligente** | 10 req/min por usuario (autenticado) ou por IP (anonimo) |
| **CORS** | Configurado para bloquear dominios nao autorizados |
| **BCrypt** | Hash de senhas com work factor 12 |
| **Role-Based Access** | Controle de acesso por papeis (ADMIN/USER) |
| **Magic Number Validation** | Validacao de assinatura real de arquivos no upload |

#### Geracao Automatica de Chaves JWT

O sistema implementa geracao automatica de chaves JWT no primeiro inicio, seguindo boas praticas de seguranca:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRIMEIRO INICIO                               │
├─────────────────────────────────────────────────────────────────┤
│  1. Container inicia                                             │
│  2. Entrypoint verifica se chaves existem em /app/secrets        │
│  3. Se NAO existem:                                              │
│     └─ openssl genrsa -out privateKey.pem 2048                  │
│     └─ openssl rsa -pubout -out publicKey.pem                   │
│  4. Chaves sao persistidas em Docker volume (jwt_secrets)        │
│  5. Aplicacao inicia com as chaves geradas                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    INICIOS SUBSEQUENTES                          │
├─────────────────────────────────────────────────────────────────┤
│  1. Container inicia                                             │
│  2. Entrypoint verifica se chaves existem em /app/secrets        │
│  3. Chaves JA existem no volume                                  │
│  4. Aplicacao inicia com as chaves existentes                    │
│  5. Tokens gerados anteriormente continuam validos               │
└─────────────────────────────────────────────────────────────────┘
```

**Vantagens desta abordagem:**

| Aspecto | Beneficio |
|---------|-----------|
| **Zero configuracao** | Nao requer geracao manual de chaves antes do deploy |
| **Persistencia** | Volume Docker garante que chaves sobrevivem a reinicializacoes |
| **Seguranca** | Cada instalacao tem chaves unicas (nao compartilhadas no repo) |
| **Producao-ready** | Suporta Docker Secrets para ambientes enterprise |

**Para ambientes de producao com Docker Secrets:**

```yaml
# docker-compose.prod.yml
services:
  api:
    secrets:
      - jwt_private_key
      - jwt_public_key

secrets:
  jwt_private_key:
    file: ./secrets/privateKey.pem
  jwt_public_key:
    file: ./secrets/publicKey.pem
```

#### Rate Limit - Detalhes de Implementacao

O sistema implementa rate limiting inteligente com duas estrategias:

| Contexto | Identificacao | Comportamento |
|----------|---------------|---------------|
| **Usuario autenticado** | Username do JWT | Cada usuario tem seu proprio bucket de 10 req/min |
| **Usuario anonimo** | IP do cliente | Cada IP tem seu proprio bucket de 10 req/min |

**Por que essa abordagem?**
- Evita que um unico usuario anonimo consuma todas as requisicoes
- Usuarios atras de proxy/NAT compartilham o mesmo limite de IP (comportamento intencional)
- Headers suportados para deteccao de IP: `X-Forwarded-For`, `X-Real-IP`

#### Validacao de Upload - Magic Numbers

O upload de imagens implementa validacao em multiplas camadas:

1. **Content-Type** - Verifica MIME type declarado
2. **Magic Numbers** - Valida assinatura real do arquivo (primeiros bytes)
3. **Tamanho** - Limite de 10MB por arquivo

| Tipo | Magic Numbers (Hex) |
|------|---------------------|
| JPEG/JPG | `FF D8 FF` |
| PNG | `89 50 4E 47 0D 0A 1A 0A` |
| GIF87a | `47 49 46 38 37 61` |
| GIF89a | `47 49 46 38 39 61` |
| WebP | `52 49 46 46` (RIFF) |

**Por que magic numbers?**
- Previne ataques onde arquivos maliciosos sao enviados com Content-Type falsificado
- Garante que o conteudo real corresponde ao tipo declarado
- Metrica de seguranca registrada em caso de tentativa de falsificacao

### Paginacao e Filtros

Todos os endpoints de listagem suportam:
- `page` - Numero da pagina (0-based)
- `size` - Tamanho da pagina
- `sortField` - Campo para ordenacao
- `sortDir` - Direcao (asc/desc)
- Filtros especificos por entidade

### Upload de Imagens

- Armazenamento no MinIO (S3 compatible)
- Suporte a multiplos arquivos
- Presigned URLs com expiracao de 30 minutos
- Tipos suportados: JPEG, PNG, GIF, WebP
- Tamanho maximo: 10MB por arquivo

### Observabilidade e Monitoring

O projeto implementa um stack completo de observabilidade para ambientes de producao:

**Metricas (Prometheus/Micrometer):**
| Endpoint | Descricao |
|----------|-----------|
| `/q/metrics` | Metricas em formato Prometheus |

Metricas customizadas disponiveis:
- `auth_login_total` - Contagem de logins (success/failure)
- `auth_login_duration` - Duracao do processo de login
- `album_created_total` - Albuns criados
- `artista_created_total` - Artistas criados
- `imagem_upload_total` - Imagens enviadas
- `regional_sync_total` - Sincronizacoes realizadas
- `regional_sync_duration` - Duracao da sincronizacao
- `rate_limit_exceeded_total` - Requisicoes bloqueadas
- `security_invalid_magic_number_total` - Uploads rejeitados por magic number invalido
- `websocket_connections_active` - Conexoes WebSocket ativas

**Resiliencia (Fault Tolerance):**
| Pattern | Configuracao |
|---------|--------------|
| **Timeout** | 10 segundos para API externa |
| **Retry** | 3 tentativas com delay de 1s |
| **Circuit Breaker** | Abre apos 50% falhas em 10 req, espera 30s |

**Auditoria:**
- Logs estruturados para operacoes CREATE, UPDATE, DELETE
- Registro de login/logout com username e timestamp
- Rastreamento de sincronizacoes de regionais

---

## Sincronizacao de Regionais

### Estrutura da Tabela - Conforme Edital

A tabela regional segue **EXATAMENTE** a estrutura especificada no edital:

```sql
-- Estrutura conforme edital: regional (id integer, nome varchar(200), ativo boolean)
CREATE TABLE regional (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
```

O campo `id` corresponde diretamente ao ID da regional na API externa, nao sendo auto-gerado.

### Algoritmo Otimizado - Complexidade O(n+m)

A sincronizacao implementa um algoritmo otimizado com complexidade **O(n+m)**, onde:
- **n** = numero de regionais na API externa
- **m** = numero de regionais no banco local

**Logica conforme especificado no edital:**

| Situacao | Acao | Descricao |
|----------|------|-----------|
| Novo no endpoint | INSERT | Regional existe na API mas nao no banco local |
| Ausente no endpoint | UPDATE ativo=false | Regional existe no banco mas nao na API |
| Atributo alterado | UPDATE nome | Nome da regional foi modificado na API |

### Implementacao Tecnica

```java
// 1. Carregar todos os dados em memoria - O(n) + O(m)
Map<Integer, Regional> locaisMap = regionalRepository.findAllAsMap();
Set<Integer> idsExternos = new HashSet<>();

// 2. Processar regionais externas - O(n)
for (RegionalExterna externa : regionaisExternas) {
    idsExternos.add(externa.getId());
    Regional local = locaisMap.get(externa.getId());

    if (local == null) {
        // CASO 1: Nova regional - INSERT
        Regional nova = Regional.fromExternal(externa.getId(), externa.getNome());
        regionalRepository.persist(nova);
    } else if (local.nomeAlterado(externa.getNome())) {
        // CASO 3: Nome alterado - UPDATE
        local.atualizarNome(externa.getNome());
        if (!local.getAtivo()) local.reativar();
    } else if (!local.getAtivo()) {
        // Reativar se estava inativa
        local.reativar();
    }
}

// 3. Inativar ausentes em batch - O(1) query
int inativadas = regionalRepository.inativarAusentes(idsExternos);
```

**Por que esta abordagem?**
- Evita consultas N+1 ao banco
- Usa HashMap para lookups em O(1)
- Inativacao em lote via query unica (eficiente)
- Segue EXATAMENTE a estrutura do edital

### Analise Detalhada de Complexidade

```
Entrada:
- n = regionais da API externa
- m = regionais no banco local

Operacoes:
┌────────────────────────────────────────┬──────────────┬──────────────┐
│ Operacao                               │ Complexidade │ Justificativa│
├────────────────────────────────────────┼──────────────┼──────────────┤
│ 1. Buscar todas regionais (banco)      │ O(m)         │ Query unica  │
│ 2. Criar HashMap local                 │ O(m)         │ Insercao m   │
│ 3. Iterar regionais externas           │ O(n)         │ Loop n vezes │
│ 4. Lookup por ID                       │ O(1)         │ HashMap get  │
│ 5. Comparar/criar/atualizar            │ O(1)         │ Por item     │
│ 6. Inativar ausentes (batch)           │ O(1)         │ Query SQL    │
└────────────────────────────────────────┴──────────────┴──────────────┘

Total: O(m) + O(m) + O(n) = O(n + m)
```

**Comparacao com abordagem ingenua O(n*m):**

| Abordagem | Complexidade | 100 regionais | 1000 regionais |
|-----------|--------------|---------------|----------------|
| Ingenua (loops aninhados) | O(n*m) | 10.000 ops | 1.000.000 ops |
| Otimizada (HashMap) | O(n+m) | 200 ops | 2.000 ops |
| Ganho | - | 50x mais rapido | 500x mais rapido |

A abordagem ingenua requereria um loop aninhado para comparar cada regional externa com cada local, resultando em O(n*m). Nossa implementacao utiliza HashMap para eliminar esse loop interno, garantindo tempo linear.

---

## Desenvolvimento Local

### Executar em modo dev

```bash
./mvnw quarkus:dev
```

### Executar testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes com relatorio de cobertura
./mvnw test jacoco:report

# Executar apenas testes unitarios
./mvnw test -Dtest="*Test"

# Executar apenas testes de integracao
./mvnw test -Dtest="*IT"

# Executar testes de uma classe especifica
./mvnw test -Dtest="RegionalServiceTest"
```

**Nota:** Os testes de integracao utilizam TestContainers para PostgreSQL, garantindo isolamento e reproducibilidade.

### Build do projeto

```bash
./mvnw package -DskipTests
```

### Gerar imagem nativa (opcional)

```bash
./mvnw package -Pnative
```

---

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/gov/mt/seplag/
│   │   ├── application/           # Servicos de aplicacao (casos de uso)
│   │   │   └── service/
│   │   │       ├── AlbumService.java
│   │   │       ├── ArtistaService.java
│   │   │       ├── AuthService.java
│   │   │       └── RegionalService.java
│   │   │
│   │   ├── domain/                # Modelos e regras de negocio
│   │   │   ├── model/
│   │   │   │   ├── Album.java
│   │   │   │   ├── Artista.java
│   │   │   │   └── Regional.java
│   │   │   ├── repository/
│   │   │   └── exception/
│   │   │
│   │   ├── infrastructure/        # Implementacoes tecnicas
│   │   │   ├── audit/             # Servico de auditoria
│   │   │   ├── client/            # Clientes REST externos
│   │   │   ├── exception/         # Exception handlers
│   │   │   ├── health/            # Health checks customizados
│   │   │   ├── logging/           # MDC e logging estruturado
│   │   │   ├── metrics/           # Metricas Prometheus/Micrometer
│   │   │   ├── security/          # JWT, Rate Limit, Password
│   │   │   └── storage/           # MinIO/S3
│   │   │
│   │   └── presentation/          # Camada de apresentacao
│   │       ├── dto/               # Data Transfer Objects
│   │       ├── rest/              # REST Resources
│   │       └── websocket/         # WebSocket endpoints
│   │
│   └── resources/
│       ├── db/migration/          # Flyway migrations
│       └── application.properties
│
└── test/
    └── java/br/gov/mt/seplag/     # Testes unitarios e integracao
        ├── application/
        ├── infrastructure/
        └── presentation/
```

---

## Cobertura de Testes

O projeto inclui testes abrangentes para garantir qualidade de nivel senior:

### Testes de Integracao (REST) - 7 classes
- **AuthResourceTest** - Login, credenciais invalidas, usuario inexistente
- **ArtistaResourceTest** - CRUD completo, autenticacao, autorizacao, filtros
- **AlbumResourceTest** - CRUD completo, filtros, ordenacao, imagens
- **ArtistaDeleteResourceTest** - DELETE com validacoes
- **AlbumDeleteResourceTest** - DELETE com limpeza de imagens
- **RateLimitIntegrationTest** - Resposta 429, headers X-RateLimit-*, limite por IP
- **ValidationErrorTest** - Validacao de campos obrigatorios e formatos

### Testes de Servico (Application Layer) - 6 classes
- **AuthServiceTest** - Autenticacao e geracao de tokens
- **ArtistaServiceTest** - Logica de negocio de artistas
- **AlbumServiceTest** - Logica de negocio de albuns
- **AlbumImagemServiceTest** - Upload, validacao, delecao de imagens
- **AlbumImagemValidationTest** - Validacao de tipos e magic numbers
- **RegionalServiceTest** - Sincronizacao O(n+m) com API externa

### Testes de Infraestrutura - 9 classes
- **RateLimitServiceTest** - Controle de taxa (10 req/min)
- **JwtTokenServiceTest** - Geracao de tokens (5 min / 24h)
- **PasswordEncoderTest** - BCrypt hash e verificacao
- **WebSocketTicketServiceTest** - Sistema de tickets para WebSocket
- **AlbumWebSocketTest** - Notificacoes em tempo real
- **JwtHealthCheckTest** - Verificacao de chaves JWT
- **MetricsServiceTest** - Metricas Prometheus/Micrometer
- **AuditServiceTest** - Servico de auditoria
- **StorageServiceTest** - Integracao com MinIO/S3

### Testes de Resiliencia - 1 classe
- **RegionaisClientFaultToleranceTest** - Circuit breaker, retry, timeout

### Health Checks - 1 classe
- **HealthCheckTest** - Liveness e readiness probes

### Total: 24 classes de teste com 335 metodos de teste

---

## Dados Iniciais (conforme edital)

O sistema ja vem com dados de exemplo conforme especificado no edital:

**Artistas:**
- Serj Tankian (Cantor) - Vocalista do System of a Down
- Mike Shinoda (Cantor) - Co-vocalista do Linkin Park
- Michel Telo (Cantor) - Cantor de musica sertaneja
- Guns N' Roses (Banda) - Banda de hard rock

**Albuns:**
| Artista | Album | Ano |
|---------|-------|-----|
| Serj Tankian | Harakiri | 2012 |
| Serj Tankian | Black Blooms | 2019 |
| Serj Tankian | The Rough Dog | 2020 |
| Mike Shinoda | The Rising Tied | 2005 |
| Mike Shinoda | Post Traumatic | 2018 |
| Mike Shinoda | Post Traumatic EP | 2018 |
| Mike Shinoda | Where'd You Go | 2006 |
| Michel Telo | Bem Sertanejo | 2014 |
| Michel Telo | Bem Sertanejo - O Show (Ao Vivo) | 2015 |
| Michel Telo | Bem Sertanejo - (1a Temporada) - EP | 2014 |
| Guns N' Roses | Use Your Illusion I | 1991 |
| Guns N' Roses | Use Your Illusion II | 1991 |
| Guns N' Roses | Greatest Hits | 2004 |

---

## Checklist de Requisitos do Edital

### Requisitos Gerais

| # | Requisito | Status |
|---|-----------|--------|
| a | Seguranca: bloquear acesso de dominios externos | ✅ Implementado (CORS) |
| b | Autenticacao JWT com expiracao 5 minutos e renovacao | ✅ Implementado |
| c | Implementar POST, PUT, GET | ✅ Implementado + DELETE |
| d | Paginacao na consulta dos albuns | ✅ Implementado |
| e | Consultas parametrizadas (cantores/bandas) | ✅ Implementado |
| f | Consultas por nome com ordenacao asc/desc | ✅ Implementado |
| g | Upload de uma ou mais imagens de capa | ✅ Implementado |
| h | Armazenamento no MinIO (API S3) | ✅ Implementado |
| i | Presigned URLs com expiracao 30 minutos | ✅ Implementado |
| j | Versionar endpoints | ✅ Implementado (/api/v1) |
| k | Flyway Migrations | ✅ Implementado |
| l | Documentar endpoints com OpenAPI/Swagger | ✅ Implementado |

### Requisitos Senior

| # | Requisito | Status |
|---|-----------|--------|
| a | Health Checks e Liveness/Readiness | ✅ Implementado |
| b | Testes unitarios | ✅ Implementado (12 classes) |
| c | WebSocket para notificar novos albuns | ✅ Implementado |
| d | Rate limit: 10 requisicoes por minuto | ✅ Implementado |
| e | Endpoint de regionais com sincronizacao O(n+m) | ✅ Implementado |

### Instrucoes do Edital

| Requisito | Status |
|-----------|--------|
| Projeto em repositorio GitHub | ✅ |
| README.md com documentacao completa | ✅ |
| Codigo como se fosse para producao | ✅ |
| Relacionamento Artista-Album N:N | ✅ |
| Imagens Docker | ✅ |
| Docker Compose (API + MinIO + BD) | ✅ |
| Commits pequenos e incrementais | ✅ |
| Clean Code e legibilidade | ✅ |

---

## O que foi implementado alem dos requisitos

Para demonstrar proficiencia de nivel senior, foram adicionados:

1. **Endpoints DELETE** - CRUD completo para Artistas e Albuns
2. **Validacao de integridade** - Nao permite deletar artista com albuns
3. **Limpeza automatica** - Delete de album remove imagens do MinIO
4. **Testes abrangentes** - 24 classes de teste (335 metodos) cobrindo todas as camadas
5. **Documentacao de arquitetura** - Diagrama e justificativas tecnicas
6. **Tratamento de erros** - Exception handlers globais com respostas padronizadas
7. **Logs estruturados** - JSON logging para producao
8. **Indices de banco otimizados** - Para consultas frequentes
9. **Hash de senhas BCrypt** - Padrao industria com work factor 12
10. **Estatisticas de regionais** - Endpoint para monitoramento (total/ativas/inativas)
11. **Metricas Prometheus** - Observabilidade completa com Micrometer
12. **Health Checks customizados** - MinIO, API externa, JWT, Database
13. **Circuit Breaker** - Resiliencia para chamadas a API externa
14. **Servico de auditoria** - Logs estruturados de operacoes criticas
15. **Documentacao de complexidade** - Analise detalhada O(n+m)
16. **Rate Limit por IP** - Identificacao inteligente para usuarios anonimos
17. **Magic Number Validation** - Validacao de assinatura real de arquivos no upload
18. **Teste de integracao Rate Limit** - Validacao de resposta 429 Too Many Requests
19. **WebSocket Ticket Auth** - Autenticacao segura via tickets temporarios (padrao AWS/Firebase)
20. **Chaves JWT Auto-geradas** - RSA 2048 bits geradas automaticamente no primeiro inicio (zero config)

---

## Decisoes de Design e Trade-offs

Esta secao documenta as principais decisoes arquiteturais e seus trade-offs, demonstrando pensamento critico de nivel senior.

### 1. Arquitetura em Camadas vs Microservices

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Monolito em camadas | ✅ | Adequado para o escopo do projeto. Microservices adicionariam complexidade desnecessaria. |
| Microservices | ❌ | Over-engineering para uma API de gerenciamento de artistas/albuns. |

### 2. Active Record (Panache) vs Repository Pattern

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Panache Active Record | ✅ | Reduz boilerplate, ideal para operacoes CRUD. Quarkus oferece integracao nativa. |
| Repository Pattern puro | ❌ | Adiciona camada de abstracao sem beneficio claro neste contexto. |

### 3. JWT Stateless vs Sessions

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| JWT Stateless | ✅ | Escalabilidade horizontal, sem necessidade de storage de sessao. |
| Sessions com Redis | ❌ | Adiciona dependencia e complexidade operacional. |

### 4. BCrypt vs Argon2

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| BCrypt | ✅ | Padrao consolidado, amplamente auditado, suportado nativamente. |
| Argon2 | ❌ | Mais moderno, mas BCrypt ainda e recomendado para a maioria dos casos. |

### 5. Sincronizacao Sincrona vs Assincrona (Regionais)

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Sincrona | ✅ | Simplicidade. A API externa retorna rapidamente (~100 registros). |
| Assincrona com fila | ❌ | Over-engineering para o volume esperado. |

### 6. Presigned URLs vs Proxy de Imagens

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Presigned URLs | ✅ | Requisito do edital. Reduz carga no servidor, cliente acessa MinIO diretamente. |
| Proxy | ❌ | Aumenta latencia e consumo de recursos do servidor. |

### 7. Chaves JWT Auto-geradas vs Manuais

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Auto-geradas no container | ✅ | Zero configuracao, cada instalacao tem chaves unicas, persistidas em Docker volume. |
| Chaves manuais no repo | ❌ | Risco de seguranca (chaves compartilhadas), requer passos extras antes do deploy. |
| Docker Secrets obrigatorio | ❌ | Complexidade desnecessaria para ambiente de desenvolvimento/teste. Sistema suporta Secrets como opcao para producao. |

### 8. Rate Limit In-Memory vs Distributed Store (Redis)

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| In-Memory (ConcurrentHashMap) | ✅ | Suficiente para single-instance. Menor latencia, zero dependencias externas adicionais. |
| Redis (distributed) | ❌ (recomendado para evolucao) | Necessario ao escalar horizontalmente com multiplas replicas da API. |

**Nota sobre evolucao para ambiente multi-instance:**

A implementacao atual utiliza armazenamento in-memory para o rate limiting (sliding window com `ConcurrentHashMap`). Esta abordagem e adequada para o escopo atual com uma unica instancia da API, porem **nao e compartilhada entre replicas**.

Em um cenario de producao com **escalonamento horizontal** (multiplos containers/pods da API atras de um load balancer), o estado do rate limit ficaria isolado por instancia, permitindo que um mesmo cliente exceda o limite global ao ter suas requisicoes distribuidas entre diferentes replicas.

Para evolucao do projeto, a adocao de um **distributed store como Redis** e essencial para:
- **Rate Limiting centralizado** - Contadores compartilhados entre todas as instancias via `INCR` atomico com `TTL`
- **Cache distribuido** - Evitar consultas redundantes ao banco em cenarios de alta concorrencia
- **Gestao de sessoes WebSocket** - Sincronizar estado de conexoes ativas e tickets entre replicas
- **Pub/Sub para WebSocket** - Propagar notificacoes de novos albuns para clientes conectados em diferentes instancias

A migracao para Redis e de baixo impacto arquitetural, pois a camada de servico ja esta desacoplada da implementacao de storage atraves de interfaces, bastando substituir o provider in-memory por um client Redis (ex: Quarkus Redis ou Lettuce).

### 9. WebSocket Ticket Auth vs Socket.IO / JWT na URL

| Opcao | Escolhida | Justificativa |
|-------|-----------|---------------|
| Ticket-Based Auth (WebSocket nativo) | ✅ | Padrao enterprise (AWS, Firebase, Slack, Discord). Token single-use de 30s, sem exposicao de credenciais sensiveis na URL. |
| Socket.IO | ❌ | Biblioteca do ecossistema Node.js, nao nativa ao Java/Quarkus. Adiciona overhead de protocolo proprietario sobre WebSocket quando o framework ja oferece suporte nativo a RFC 6455. |
| JWT direto na URL do WebSocket | ❌ | Expoe token de longa duracao em logs de acesso, historico do navegador e proxies intermediarios. |

---

## Licenca

Este projeto foi desenvolvido como parte do Processo Seletivo Simplificado (PSS) da SEPLAG-MT, Edital 001/2026.

---

**Desenvolvido por Jean Paulo Sassi de Miranda**
*Vaga: Analista de TI - Engenheiro da Computacao (Senior)*
