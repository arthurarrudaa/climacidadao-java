# ClimaCidadao — API Java simples (FIAP 2026/2)

API REST para **usuarios, cidades favoritas e pesquisas climaticas**, com dois relacionamentos 1:N (usuario -> favoritos; usuario -> pesquisas). O projeto **armazena pesquisas recebidas**: nao consulta OpenWeatherMap nem ViaCEP nesta etapa. Sem login, JWT ou front-end neste ZIP.

## Tecnologia

Java 17 ou superior, Spring Boot, Maven, JDBC (`DataSource` + `PreparedStatement`), Oracle, ModelMapper, Bean Validation. Nao usa JPA/Hibernate/Spring Data.

## Rodar no IntelliJ IDEA

1. Abra a pasta do projeto (a que contem `pom.xml`). Aguarde a importacao Maven.
2. No **Oracle SQL Developer**, conecte na conta Oracle da FIAP e execute `sql/criar_tabelas_oracle.sql` **uma vez**. A API usa esse mesmo usuario/esquema.
3. Na configuracao **Run > Edit Configurations** da classe `ClimaCidadaoApplication`, preencha as variaveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD` com os dados reais disponibilizados pela FIAP (veja `.env.example`). Nao coloque senhas no projeto.
4. Execute `ClimaCidadaoApplication` ou, com Maven instalado: `mvn spring-boot:run`.
5. Abra o Postman e teste os exemplos de `exemplos/requisicoes.http` (base `http://localhost:8080`).

**Importante:** a string `DB_URL` usa o formato `jdbc:oracle:thin:@//HOST:PORTA/SERVICO`. Confirme host, porta, service name, usuario e senha com a FIAP. O servidor precisa ter acesso de rede ao Oracle.

## Endpoints

| Recurso | CRUD | Rotas aninhadas |
| --- | --- | --- |
| Usuarios | `GET/POST /usuarios`, `GET/PUT/DELETE /usuarios/{id}` | — |
| Favoritos | `GET/POST /favoritos`, `GET/PUT/DELETE /favoritos/{id}` | `GET/POST /usuarios/{usuarioId}/favoritos` |
| Pesquisas | `GET/POST /pesquisas`, `GET/PUT/DELETE /pesquisas/{id}` | `GET/POST /usuarios/{usuarioId}/pesquisas` |

POST retorna **201** com cabecalho `Location`; GET e PUT retornam **200**; DELETE retorna **204**. Entradas invalidas retornam **400**, nao encontrado **404**, metodo errado **405**, duplicidade ou exclusao de usuario com filhos **409**, erro interno **500** em JSON padronizado.

### Corpo de um POST /usuarios

```json
{"nome":"Ana Silva","email":"ana.silva@example.com"}
```

### Corpo de um POST /usuarios/1/favoritos

```json
{"cidade":"Sao Paulo"}
```

### Corpo de um POST /usuarios/1/pesquisas

```json
{"cidade":"Sao Paulo","temperatura":25.5}
```

A data da pesquisa e criada pelo servidor se `dataConsulta` nao for enviada. Tambem pode ser informada no formato `2026-11-14T10:30:00`, desde que nao seja futura. Em PUT de pesquisas, omitir `dataConsulta` conserva a data anterior. No POST direto de favoritos ou pesquisas, inclua `usuarioId` no JSON. Para excluir um usuario, exclua antes seus favoritos e pesquisas (a FK impede exclusao em cascata).

## Testes e Docker

- `mvn test` — testes HTTP com controllers e DAOs simulados, sem depender do Oracle.
- `mvn clean package` — gera o JAR (precisa baixar dependencias na primeira vez).
- `docker build -t climacidadao .` — compila usando o Dockerfile multi-stage.
- `docker run --rm -p 8080:8080 -e DB_URL="..." -e DB_USER="..." -e DB_PASSWORD="..." climacidadao` — conecta ao Oracle usando variaveis reais.

## Render e React

O `Dockerfile` esta preparado para deploy no Render. Configure `DB_URL`, `DB_USER`, `DB_PASSWORD` e `CORS_ALLOWED_ORIGINS` (origem do React, sem barra no final). `PORT` e opcional. Verifique se o Oracle FIAP aceita conexoes originadas do Render. O deploy e a integracao com o React **ainda dependem de execucao e testes externos**. Veja `exemplos/fetch-exemplos.js`.

## Entrega FIAP

**Preencha `IDENTIFICACAO_GRUPO.txt`** com os dados verdadeiros do grupo, URL do Render e video depois de publicar. O edital de DDD tambem exige **PDF tecnico e video**; esses materiais nao estao neste ZIP por solicitacao do grupo e deverao ser feitos antes da entrega final.

**IA utilizada:** ChatGPT auxiliou na elaboracao inicial do codigo e arquivos. O grupo precisa ler, entender, revisar e testar o projeto antes de entregar, conforme as regras gerais do edital.
