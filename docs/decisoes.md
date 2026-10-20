# Decisões de design

O porquê de cada escolha do `task-api`, com o teste que a sustenta.

## 1. A resposta é `TaskResponse`, não a entidade `Task`

O formato de `Task` é ditado pelo banco (nome do campo = nome da coluna). O de `TaskResponse` é o
contrato com quem consome a API. Testei: renomear `concluido` para `feito` no `TaskResponse` mudou só o
JSON; na entidade, exigiu `@Column(name = "concluido")` e quebrou a compilação do `fromEntity`. E com o
DTO só sai o que eu escolho: um campo como senha nunca vaza por acidente.

## 2. A entrada é `TaskRequest`, não a entidade `Task`

Recebendo `Task` no `@RequestBody`, um `POST` com `"id": 6` no corpo sobrescreveu a linha 6 e ainda
respondeu `201 Created`. Isso é **mass assignment**. O `TaskRequest` é um record sem `id`: o Jackson
monta o objeto pelo construtor, a chave `id` não tem onde caber e é descartada. Quem decide `INSERT` ou
`UPDATE` é o `save()`, olhando o `id` da entidade: nulo vira `INSERT`. O `201` aparecia nos dois casos
porque está fixo no controller, que nunca perguntou ao banco o que aconteceu.

## 3. O `PUT` não usa `toEntity()`

Testei: com `toEntity()`, o `PUT /tasks/3` criou uma linha nova e respondeu 200 com outro `id`. O
`toEntity()` não conhece a URL, então a entidade sai com `id` nulo e o `save()` faz `INSERT`. No código
final, o `PUT` parte do `findById(id)`: a entidade já vem do banco com `id = 3`, os setters trocam os
outros três campos e o `save()` faz `UPDATE`. É o mesmo `findById` que permite devolver 404.

## 4. `concluido` é `boolean`, não `Boolean`

O erro não some nem aparece: ele anda de camada. Com `boolean`, um corpo sem `concluido` é recusado pelo
Jackson antes do controller, com **400**. Com `Boolean`, o record nasce com `null` dentro, e o erro só
estoura no `setConcluido(boolean)` da entidade, ao desempacotar o `null`: `NullPointerException` e
**500**. A culpa é do cliente nos dois casos, mas só o primitivo responde isso.

## 5. Validar na entrada, e não deixar para o banco

Antes, um título de 300 caracteres era recusado pelo Postgres (`varchar(255)`) e a API respondia
**500**: culpa do cliente virando culpa do servidor. Um título `"     "` o banco aceitava: **201** e lixo
gravado. Esse é o pior, porque o 201 esconde o erro.

- `@NotBlank` e `@Size(max = 100)` no `titulo`: 100 é regra de negócio minha, não limite do banco.
  `@Size(max = 255)` na `descricao`: é o teto da coluna. O limite do DTO nunca pode passar o da coluna,
  senão volta o 500.
- `@Valid` no `POST` e no `PUT`. Sem ele as anotações não são conferidas: testei, e continuou 201.
- `GlobalExceptionHandler`: o `@RestControllerAdvice` vigia todos os controllers, e o
  `@ExceptionHandler(MethodArgumentNotValidException.class)` monta o mapa campo → mensagem. A mensagem
  sai do console da aplicação e volta ao cliente.

Limite conhecido: uma mensagem por campo. Um título de 101 espaços quebra duas regras, e só uma aparece.
A ordem entre elas não é garantida.
