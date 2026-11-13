# FoodBridge — arquitetura técnica atual

> **FB-ARC-ATUAL-001 · 13/11/2026.** Descreve os componentes observados em código e nos workflows homologados. Distingue execução comprovada de decisões não verificadas externamente.

## Escopo da infraestrutura

Estão representados **apenas os componentes usados diretamente pelo FoodBridge**: frontend React, Nginx, API Kotlin/Spring Boot, PostgreSQL, SSH/CI/CD e integrações efetivas do produto. A instância Oracle é compartilhada, porém não é objetivo deste documento inventariar outras aplicações hospedadas nela.

## Visão de camadas

```mermaid
flowchart TB
    U["Pessoa usuária: doador, produtor, distribuidor, ONG ou admin"]
    W["React 19 + TypeScript/JSX + Vite 8\nBrowserRouter, AuthContext e Axios"]
    N["Nginx\narquivos dist do frontend"]
    A["Spring Boot 3.2.5 / Kotlin 2.2.21\nJava 21 • REST • Spring Security"]
    S["Controllers → Services → Repositories JPA"]
    P[("PostgreSQL\nusuários, organizações, doações, solicitações, voluntários")]
    V["ViaCEP externo\nconsulta de CEP no formulário"]
    U --> W
    W --> N
    W -->|"requisições API: base URL configurável"| A
    A --> S
    S --> P
    W -->|"consulta de CEP"| V
```

**Cuidado:** Nginx servindo `dist` e API respondendo em `127.0.0.1:8080` foram comprovados em VM. O encaminhamento público de todos os caminhos API pelo Nginx **não** foi homologado por teste de integração externo abrangente; confirmar a configuração da instância e `VITE_API_BASE_URL` em cada ambiente.

## Arquitetura interna do backend

```mermaid
flowchart LR
    HTTP["HTTP REST"]
    SEC["SecurityFilterChain + JwtFilter\nTEMP / ACCESS"]
    C["9 controllers\n50 endpoints"]
    SERV["Services de negócio\nregras de papel, vínculo e estoque"]
    DTO["DTOs, mapeadores e validações"]
    REPO["7 repositories Spring Data"]
    DB[("PostgreSQL/JPA")]
    HTTP --> SEC --> C
    C <--> DTO
    C --> SERV --> REPO --> DB
    SERV -->|"ContextoAcessoService"| SEC
```

`config/` inclui CORS, segurança, JWT, encoder, inicialização administrativa, guarda de validação de esquema e WebConfig. A app tem `GlobalExceptionHandler`, exceções customizadas e `JwtService`. A validação do tenant acontece nos serviços e consulta vínculos vigentes, não apenas claims JWT.

## Modelo de dados e relacionamentos

```mermaid
erDiagram
    USUARIO ||--o{ USUARIO_ORGANIZACAO : participa
    ORGANIZACAO ||--o{ USUARIO_ORGANIZACAO : vincula
    VOLUNTARIO ||--o{ VOLUNTARIO_ORGANIZACAO : participa
    ORGANIZACAO ||--o{ VOLUNTARIO_ORGANIZACAO : recebe
    ORGANIZACAO ||--o{ DOACAO : oferece
    DOACAO ||--o{ REQUISICAO_DOACAO : recebe
    ORGANIZACAO ||--o{ REQUISICAO_DOACAO : solicita
    VOLUNTARIO o|--o{ REQUISICAO_DOACAO : coleta
```

- `Usuario`: nome, email, senha com encoder, status e vínculos.
- `Organizacao`: CNPJ, nome, email, categoria/papel, estado de aprovação e endereço JSONB.
- `UsuarioOrganizacao`: papel, status e datas da relação usuário-organização.
- `Voluntario`: contato, CPF, endereço JSONB; `VoluntarioOrganizacao`: vínculo e estado.
- `Doacao`: categoria, unidade, quantidade, vencimento, endereço JSONB, janelas de horário JSONB, status, quantidade final coletada e fechamento.
- `RequisicaoDoacao`: doação, organização solicitante, voluntário opcional, quantidade solicitada, status e datas.
- `Endereco` e `JanelaHorario` são objetos de valor, não entidades JPA independentes.

Esquema físico real e constraints adicionais devem ser verificados por inspeção/migrações, não deduzidos integralmente do diagrama lógico. O projeto usa `ddl-auto=validate` ou `none` para produção: o CI cria banco descartável, **não** recria PostgreSQL da Oracle.

## Fluxos principais

### Login, contexto organizacional e acesso

```mermaid
sequenceDiagram
    participant P as Pessoa
    participant UI as Frontend
    participant API as Auth/JWT
    participant DB as PostgreSQL
    P->>UI: email e senha
    UI->>API: POST /auth/login
    API->>DB: validar usuário e vínculos
    API-->>UI: ACCESS ou TEMP
    alt TEMP e organização selecionada
      UI->>API: POST /auth/select-org
      API->>DB: validar status e vínculo
      API-->>UI: ACCESS com papel, organização e vínculo
    end
    UI->>API: GET /dashboards/{perfil} (Bearer ACCESS)
    API->>DB: checar contexto vigente e consultar dados
    API-->>UI: indicadores autorizados
```

### Reserva de estoque até coleta

```mermaid
flowchart TD
    D["Doador publica doação"] --> O["ONG pesquisa e solicita quantidade"]
    O --> L["Transação e bloqueio do registro de doação"]
    L --> Q{"Quantidade ainda disponível?"}
    Q -- "Não" --> ERR["Recusa: evitar sobre-reserva"]
    Q -- "Sim" --> P["Requisição PENDENTE"]
    P --> AP["Doador aprova"]
    AP --> V["ONG vincula voluntário ativo"]
    V --> C["Doador confirma coleta"]
    C --> R["Requisição CONCLUIDO"]
    R --> I["Dashboards e impacto por unidade"]
```

As transições `PENDENTE`, `APROVADO`, `CONCLUIDO`, `REJEITADO` e `CANCELADO` são definidas no código. `DoacaoService` admite fechamento manual e evita totalizar quantidades de unidades incompatíveis.

## Implantação na mesma Oracle E2.1.Micro

```mermaid
flowchart LR
    PR["Pull request"] --> CI["GitHub Actions\nCI isolado"]
    CI --> M["Merge em main"]
    M --> BA["Build/testes API\nPostgreSQL efêmero"]
    M --> BF["Build/lint/testes frontend"]
    BA --> SA["SSH host key pinada + SHA256\nbackup JAR + restart systemd"]
    BF --> SF["SSH host key pinada + SHA256\nbackup dist + teste nginx"]
    SA --> VM["Oracle Linux 9 x86_64\nVM.Standard.E2.1.Micro"]
    SF --> VM
    VM --> DB[("PostgreSQL de produção (preservado)")]
    VM --> N["Nginx: dist"]
    VM --> J["systemd: foodbridge.service/Java"]
```

- Backend: CI `foodbridge-ci-cd.yml` compila e testa em PostgreSQL de teste, depois publica JAR aprovado em push de código na `main`.
- Frontend: `deploy.yml` testa, compila, envia `dist` e confirma Nginx HTTP 200. Repositórios e environments de segredos são separados.
- A E2.1.Micro reconhece ~945 MiB após Kdump ser desabilitado pelo operador; telemetria de memória é alerta, não mais veto de deploy.
- Health: `GET /health/live` leve e `GET /dashboards/publico` com consulta real. Backups prévios e rollback tentado automaticamente em falha.
- Sem Kubernetes, alta disponibilidade, replicação declarada nem HTTPS homologado nestes workflows; tratá-los como futuras melhorias.

## Segurança e decisões técnicas

O frontend guarda sessão em `localStorage`, e isso demanda proteção contra XSS e futura revisão da estratégia de tokens. O backend usa `JwtFilter` + verificação de usuário e vínculo no banco; `Spring Security` exige papéis e tokens. Dados pessoais de usuários e voluntários requerem minimização de acesso e políticas LGPD — o repositório não é certificado de conformidade jurídica.

Para diagramas PlantUML legados editáveis, veja [docs/uml/README.md](uml/README.md); para contratos HTTP, [referência API](REFERENCIA_API_ATUAL.md).
