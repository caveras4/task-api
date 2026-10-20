# task-api

API REST de tarefas (**Todo List**) em Spring Boot com PostgreSQL, Projeto 1 do meu estudo de
Java/Spring. `/tasks` é o CRUD; `/ola` ficou da Semana 1.

## Tecnologias

Java 21 · Spring Boot 4.1.0 (Web MVC, Data JPA, Bean Validation) · PostgreSQL 18 · Maven (wrapper `./mvnw`)

## Como executar

Pré-requisito: PostgreSQL em `localhost:5432` com a base `tasksdb` criada.

A senha do banco vem da variável de ambiente `DB_PASSWORD`. O `application.properties` só declara
`${DB_PASSWORD:postgres}`, então a senha real nunca entra no repositório. No PowerShell:

```powershell
$env:DB_PASSWORD='SUA_SENHA_REAL'; .\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

| Método | Rota          | Sucesso             | Erro      |
|--------|---------------|---------------------|-----------|
| GET    | `/ola`        | `Olá, Mundo!`       | —         |
| GET    | `/ola/{nome}` | `Olá, {nome}`       | —         |
| GET    | `/tasks`      | 200 (lista)         | 400       |
| GET    | `/tasks/{id}` | 200                 | 404       |
| POST   | `/tasks`      | 201 + `Location`    | 400       |
| PUT    | `/tasks/{id}` | 200 (recurso salvo) | 400 / 404 |
| DELETE | `/tasks/{id}` | 204 (sem corpo)     | 404       |

Parâmetros do `GET /tasks`, os dois opcionais e combináveis:

- `concluido`: filtra por `true` ou `false`. Outro valor, como `talvez`, volta **400**.
- `ordenarPor`: ordena por `id`, em ordem crescente, e aceita maiúsculas (`ID`). Outro valor volta **400**.

Sem nenhum dos dois, vem a lista completa, sem filtro e sem ordenação garantida.

```bash
# Só as concluídas, ordenadas por id
curl -i "http://localhost:8080/tasks?concluido=true&ordenarPor=id"
```

Corpo do `POST` e do `PUT`:

- `titulo`: obrigatório, não pode ser em branco, até 100 caracteres
- `descricao`: opcional, até 255 caracteres
- `concluido`: obrigatório

O `PUT` substitui o recurso inteiro, então envie o objeto completo. Erro de validação volta **400** com
um mapa `{"campo": "mensagem"}`. A validação roda antes do controller, por isso um `PUT /tasks/999` com
título em branco dá 400, não 404.

```bash
# Criar — 201 com o header Location apontando para o recurso novo
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"titulo":"estudar spring","descricao":"semana 3","concluido":false}'

# Título em branco — 400 com {"titulo":"não deve estar em branco"}
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"titulo":"   ","descricao":"x","concluido":false}'
```

## Decisões

O porquê de cada escolha (DTOs, mass assignment, `boolean` × `Boolean`, validação), com o teste que
sustenta cada uma: [docs/decisoes.md](docs/decisoes.md).

## Autor

Caio Veras
