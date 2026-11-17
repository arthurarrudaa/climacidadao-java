# ClimaCidadão — API REST Java | Global Solution 2026/2

API desenvolvida para a solução **CitizenTech** da FIAP. O backend mantém dados no **Oracle** por **JDBC**, consulta o **OpenWeatherMap** para clima atual e previsão de cinco dias e é consumido pelo frontend React.

## Integrantes do grupo — Turma 1TDSPF

- Arthur Arruda da Silva — RM 569593
- Enzo Torres Estanguera — RM 568937
- Giovane Dias Chicoli — RM 569750
- Álvaro Morais Holanda — RM 568607
- Dhiego Gozzo Belmonte — RM 571505

**API publicada no Render:** https://climacidadao-java.onrender.com

**Repositório Java:** https://github.com/arthurarrudaa/climacidadao-java

**Repositório React:** https://github.com/enzoestanguera/Global_Solution

**Vídeo de demonstração (YouTube não listado, até 5 minutos):** PENDENTE — inserir o link antes da entrega.

## 1. Objetivo e funcionalidades

O ClimaCidadão é uma API REST desenvolvida em Java para apoiar o projeto CitizenTech, que tem como objetivo facilitar o acesso dos cidadãos a informações meteorológicas e orientações preventivas.

A aplicação permite consultar as condições climáticas atuais de diferentes cidades, visualizar previsões para os próximos cinco dias e receber orientações informativas de acordo com os dados meteorológicos.

O frontend React oferece pesquisa por nome da cidade ou CEP. Na pesquisa por CEP, o React utiliza o **ViaCEP** para identificar o município. Em seguida, a API Java consulta o **OpenWeatherMap** para obter os dados meteorológicos.

Além disso, a API realiza operações de persistência no banco Oracle.

### Funcionalidades principais

- **Usuários:** cadastro, listagem, consulta por ID, atualização e exclusão.
- **Cidades favoritas:** cadastro e gerenciamento de cidades vinculadas aos usuários.
- **Pesquisas climáticas:** armazenamento de registros de pesquisa com cidade, temperatura e data da consulta.
- **Contatos:** recebimento de mensagens enviadas pelo formulário React.
- **Clima atual:** consulta de temperatura, sensação térmica, umidade, vento e descrição das condições meteorológicas.
- **Previsão de cinco dias:** consulta de previsões meteorológicas em intervalos de três horas.
- **Pesquisa por CEP:** identificação da cidade pelo ViaCEP, realizada no frontend.
- **Orientações climáticas:** mensagens informativas geradas no React com base nas condições meteorológicas recebidas.

A API utiliza o Oracle para armazenar usuários, favoritos, pesquisas e contatos.

**Importante:** as consultas realizadas no Painel Climático não são automaticamente gravadas na tabela de pesquisas. Os endpoints de persistência funcionam independentemente das consultas ao OpenWeatherMap.

As orientações climáticas do CitizenTech são informativas e não substituem alertas oficiais da Defesa Civil ou de órgãos meteorológicos.

## 2. Tecnologias utilizadas

- Java 21 no ambiente de desenvolvimento.
- Spring Boot 3.5.6.
- Maven.
- Oracle Database da FIAP.
- JDBC com `DataSource` e `PreparedStatement`.
- ModelMapper.
- Bean Validation.
- API REST com JSON.
- Docker com Dockerfile multi-stage.
- Render para hospedagem da API Java.
- OpenWeatherMap para dados meteorológicos.
- React, TypeScript e Tailwind CSS no frontend.
- ViaCEP para identificação de municípios a partir de CEPs.

O projeto utiliza **JDBC**, sem JPA, Hibernate ORM ou Spring Data para persistência.

## 3. Arquitetura do projeto

O backend foi organizado em pacotes com responsabilidades específicas.

| Pacote | Responsabilidade |
| --- | --- |
| `model` | Entidades do domínio |
| `dao` | Acesso ao banco Oracle utilizando JDBC |
| `dto` | Objetos de entrada e saída da API |
| `controller` | Endpoints REST e tratamento das requisições HTTP |
| `exception` | Exceções específicas do domínio |
| `handler` | Tratamento global de exceções e respostas padronizadas |
| `config` | Configurações da aplicação, incluindo CORS |

O funcionamento das operações com persistência segue o fluxo:

**Frontend React → Controller → DTO → DAO → Oracle Database**

Nas consultas meteorológicas, o funcionamento é:

**Frontend React → ClimaController → OpenWeatherMap → API Java → React**

Essa organização separa o acesso ao banco de dados das responsabilidades dos controllers.

## 4. Modelo de dados e relacionamentos

A API possui quatro entidades principais.

| Entidade | Tabela Oracle | Principais atributos |
| --- | --- | --- |
| Usuário | `tb_usuario` | `id`, `nome`, `email` |
| Favorito | `tb_favorito` | `id`, `usuarioId`, `cidade` |
| Pesquisa | `tb_pesquisa` | `id`, `usuarioId`, `cidade`, `temperatura`, `dataConsulta` |
| Contato | `tb_contato` | `id`, `nome`, `email`, `mensagem` |

### Relacionamentos

A aplicação possui dois relacionamentos do tipo **1:N**:

1. **Usuário → Favoritos:** um usuário pode possuir várias cidades favoritas.
2. **Usuário → Pesquisas:** um usuário pode possuir vários registros de pesquisas climáticas.

As tabelas `tb_favorito` e `tb_pesquisa` possuem chaves estrangeiras que referenciam `tb_usuario`.

As tabelas utilizam chaves primárias e sequences no Oracle para geração de identificadores.

### Regra de negócio

A aplicação impede que um mesmo usuário cadastre a mesma cidade favorita mais de uma vez.

Quando essa regra é violada, a API utiliza uma exceção de negócio e retorna o status HTTP **409 Conflict**.

Também existem validações para impedir o cadastro de dados inválidos.

## 5. Como executar no IntelliJ IDEA

1. Abra a pasta do projeto Java que contém o arquivo `pom.xml`.
2. Aguarde a importação das dependências Maven.
3. Configure a conexão com o banco Oracle da FIAP.
4. Em **Run → Edit Configurations**, selecione `ClimaCidadaoApplication`.
5. Configure as variáveis de ambiente necessárias.
6. Execute a classe `ClimaCidadaoApplication`.
7. Utilize o Postman para testar os endpoints.

### Variáveis de ambiente

| Variável | Descrição |
| --- | --- |
| `DB_URL` | Endereço JDBC do banco Oracle |
| `DB_USERNAME` | Usuário do banco Oracle |
| `DB_PASSWORD` | Senha do banco Oracle |
| `OPENWEATHER_API_KEY` | Chave da API OpenWeatherMap |
| `CORS_ALLOWED_ORIGINS` | Origens autorizadas para o frontend React |
| `PORT` | Porta utilizada pela aplicação, opcional |

Exemplo de formato da URL Oracle:

`jdbc:oracle:thin:@//HOST:1521/SERVICO`

**Importante:** nunca publique senhas, chaves de API ou credenciais reais no GitHub.

### Banco Oracle

O projeto possui o script:

`sql/criar_tabelas_oracle.sql`

Esse arquivo contém as instruções para criação das tabelas e sequences utilizadas pela API.

Execute o script somente em um esquema Oracle novo, no qual as tabelas ainda não existam.

**Não execute novamente o script completo no banco que já está funcionando**, pois isso pode gerar erros de objetos existentes.

### Endereço local

Com a aplicação executando no IntelliJ, a API pode ser acessada em:

http://localhost:8081

Exemplo:

http://localhost:8081/usuarios

## 6. Endpoints da API REST

### Usuários

| Método | Endpoint | Descrição |
| --- | --- | --- |
| GET | `/usuarios` | Listar usuários |
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios/{id}` | Buscar usuário por ID |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário |

### Favoritos

| Método | Endpoint | Descrição |
| --- | --- | --- |
| GET | `/favoritos` | Listar favoritos |
| POST | `/favoritos` | Cadastrar favorito |
| GET | `/favoritos/{id}` | Buscar favorito por ID |
| PUT | `/favoritos/{id}` | Atualizar favorito |
| DELETE | `/favoritos/{id}` | Excluir favorito |
| GET | `/usuarios/{usuarioId}/favoritos` | Listar favoritos de um usuário |
| POST | `/usuarios/{usuarioId}/favoritos` | Cadastrar favorito para um usuário |

### Pesquisas climáticas

| Método | Endpoint | Descrição |
| --- | --- | --- |
| GET | `/pesquisas` | Listar pesquisas |
| POST | `/pesquisas` | Cadastrar pesquisa |
| GET | `/pesquisas/{id}` | Buscar pesquisa por ID |
| PUT | `/pesquisas/{id}` | Atualizar pesquisa |
| DELETE | `/pesquisas/{id}` | Excluir pesquisa |
| GET | `/usuarios/{usuarioId}/pesquisas` | Listar pesquisas de um usuário |
| POST | `/usuarios/{usuarioId}/pesquisas` | Cadastrar pesquisa para um usuário |

### Contatos

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/contatos` | Receber mensagem do formulário de contato |

O endpoint de contato não disponibiliza uma rota pública de listagem das mensagens.

### Consultas meteorológicas

| Método | Endpoint | Descrição |
| --- | --- | --- |
| GET | `/clima?cidade=Campinas,BR` | Consultar condições meteorológicas atuais |
| GET | `/clima/previsao?cidade=Campinas,BR` | Consultar previsão meteorológica de cinco dias |

Os dados meteorológicos são obtidos do OpenWeatherMap, utilizando a chave configurada na API Java.

## 7. Exemplos de requisições e respostas

### Cadastrar usuário

**POST `/usuarios`**

Corpo da requisição:

```json
{
  "nome": "Ana Silva",
  "email": "ana.silva@example.com"
}
```

### Cadastrar cidade favorita

**POST `/usuarios/1/favoritos`**

```json
{
  "cidade": "Campinas"
}
```

### Cadastrar pesquisa climática

**POST `/usuarios/1/pesquisas`**

```json
{
  "cidade": "Campinas",
  "temperatura": 25.5
}
```

A temperatura nesse endpoint de persistência é informada pela requisição. Esse cadastro é independente das consultas meteorológicas realizadas pelo OpenWeatherMap.

### Enviar contato

**POST `/contatos`**

```json
{
  "nome": "Ana Silva",
  "email": "ana.silva@example.com",
  "mensagem": "Gostaria de saber mais sobre o projeto."
}
```

### Consultar clima atual

**GET `/clima?cidade=Campinas,BR`**

Exemplo ilustrativo de resposta:

```json
{
  "cidade": "Campinas",
  "temperatura": 22.89,
  "sensacaoTermica": 23.14,
  "umidade": 73,
  "vento": 4.63,
  "descricao": "céu limpo"
}
```

Os valores meteorológicos são atualizados pelo OpenWeatherMap e podem variar conforme a consulta.

### Consultar previsão meteorológica

**GET `/clima/previsao?cidade=Campinas,BR`**

A resposta contém os seguintes campos:

- `cidade`: nome da cidade consultada.
- `fusoHorarioSegundos`: diferença do fuso horário da cidade em relação ao UTC.
- `previsoes`: lista de registros meteorológicos previstos.

Cada registro de previsão possui:

- `horarioUtc`
- `temperatura`
- `descricao`
- `probabilidadeChuva`
- `vento`

O React utiliza esses registros para apresentar uma previsão geral dos próximos cinco dias, agrupando as informações por data.

## 8. Validações e tratamento de erros

A API utiliza Bean Validation nos DTOs de entrada, além de tratamento global de exceções.

Os principais códigos HTTP são:

| Status | Significado |
| --- | --- |
| 200 OK | Consulta ou atualização realizada com sucesso |
| 201 Created | Recurso cadastrado |
| 204 No Content | Exclusão realizada com sucesso |
| 400 Bad Request | Dados inválidos |
| 404 Not Found | Recurso não encontrado |
| 405 Method Not Allowed | Método HTTP não permitido |
| 409 Conflict | Violação de regra de negócio |
| 500 Internal Server Error | Erro inesperado no servidor |

Os cadastros retornam **201 Created**, com cabeçalho `Location` para identificar o recurso criado.

O tratamento global de exceções retorna mensagens JSON padronizadas, evitando a exposição de detalhes internos do banco de dados.

As consultas meteorológicas também podem apresentar falhas quando o serviço externo estiver indisponível. O tratamento desses casos deve ser conferido nos testes finais.

## 9. Integração com o frontend React

O frontend da solução CitizenTech foi desenvolvido utilizando:

- React.
- TypeScript.
- Vite.
- Tailwind CSS.
- React Router.

O frontend consome a API Java publicada no Render.

### Funcionalidades integradas

**Painel Climático**

O visitante pode informar o nome de uma cidade ou um CEP.

Na pesquisa por CEP, o React consulta o ViaCEP para identificar a cidade.

Depois disso, o frontend faz requisições à API Java:

- `GET /clima`
- `GET /clima/previsao`

A API Java consulta o OpenWeatherMap e retorna os dados climáticos.

O frontend apresenta temperatura atual, sensação térmica, umidade, vento, descrição do tempo, previsão de cinco dias e uma orientação personalizada com base nos dados recebidos.

**Formulário de Contato**

O formulário de contato utiliza:

`POST /contatos`

Os dados enviados pelo visitante são recebidos pela API Java e armazenados no banco Oracle.

Essa integração demonstra operações reais de leitura (**GET**) e escrita (**POST**) entre o frontend React e o backend Java.

**Repositório do frontend:**

https://github.com/enzoestanguera/Global_Solution

## 10. Testes e Docker

### Testes com Maven

Para executar os testes automatizados:

```bash
mvn test
```

Para compilar e empacotar o projeto:

```bash
mvn clean package
```

O projeto possui testes automatizados utilizando MockMvc e DAOs simulados.

O relatório de testes incluído na versão anterior do projeto registrava 12 testes executados sem falhas. É necessário executar os testes novamente antes da entrega final.

### Docker

O projeto possui um Dockerfile multi-stage, utilizado para construir e executar a aplicação Java em contêiner.

Para construir a imagem:

```bash
docker build -t climacidadao .
```

Para executar localmente, configurando as variáveis de ambiente necessárias:

```bash
docker run --rm -p 8080:8080 -e PORT=8080 -e DB_URL="jdbc:oracle:thin:@//HOST:1521/SERVICO" -e DB_USERNAME="USUARIO" -e DB_PASSWORD="SENHA" -e OPENWEATHER_API_KEY="CHAVE" climacidadao
```

Os valores apresentados são exemplos e devem ser substituídos pelas configurações reais do ambiente.

## 11. Publicação no Render

A API Java está publicada no Render, utilizando Docker.

**URL pública:**

https://climacidadao-java.onrender.com

Exemplos de endpoints públicos:

**Usuários:**

https://climacidadao-java.onrender.com/usuarios

**Clima atual:**

https://climacidadao-java.onrender.com/clima?cidade=Campinas,BR

**Previsão meteorológica:**

https://climacidadao-java.onrender.com/clima/previsao?cidade=Campinas,BR

As variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `OPENWEATHER_API_KEY` e `CORS_ALLOWED_ORIGINS` são configuradas no ambiente do Render.

Nenhuma credencial real deve ser incluída no código-fonte ou no Dockerfile.

A API foi testada localmente pelo Postman e também por requisições à versão publicada no Render.

## 12. Entrega FIAP — Global Solution 2026/2

O projeto foi desenvolvido para a disciplina **Domain Driven Design Using Java**, do curso de Análise e Desenvolvimento de Sistemas da FIAP.

### Materiais da entrega

O arquivo ZIP deverá conter:

- Código-fonte completo do projeto Spring Boot.
- Arquivo `pom.xml`.
- Dockerfile.
- Script SQL de criação das tabelas e sequences.
- README com informações do projeto e dos integrantes.
- Documentação técnica em PDF.
- Link do vídeo de demonstração publicado no YouTube como não listado.

Não devem ser incluídas no ZIP as pastas `target`, `.git` ou `.idea`, nem arquivos contendo credenciais reais.

### Documentação técnica

O arquivo PDF está localizado em:

`docs/ClimaCidadao_Documentacao_Tecnica_2026.pdf`

A documentação apresenta:

- Descrição do problema e da solução.
- Diagrama de classes.
- Modelo relacional do banco Oracle.
- Entidades e relacionamentos 1:N.
- Estrutura da aplicação.
- Endpoints REST.
- Validações e regras de negócio.
- Integração com serviços externos.
- Publicação no Render.

### Vídeo de demonstração

**Link:** PENDENTE — inserir o endereço do vídeo antes da entrega.

O vídeo deverá demonstrar o funcionamento do frontend integrado à API Java, incluindo consultas e operações de escrita.

### Situação do projeto

A API Java está desenvolvida e publicada no Render. A integração com o React e o OpenWeatherMap foi implementada e testada.

Antes da entrega definitiva, ainda é necessário executar os testes finais, conferir a documentação e adicionar o link do vídeo.

## 13. Utilização de inteligência artificial

O ChatGPT foi utilizado como ferramenta de apoio na elaboração inicial, revisão e documentação de partes do projeto.

A equipe é responsável por compreender, validar, testar e apresentar todo o código desenvolvido.


## CRUD de Contatos

O sistema permite cadastrar mensagens de contato e gerenciá-las
por meio de endpoints REST conectados ao banco Oracle.

| Método | Endpoint | Acesso | Sucesso |
|---|---|---|---|
| POST | `/contatos` | Público | 201 Created |
| GET | `/contatos` | Administrador | 200 OK |
| GET | `/contatos/{id}` | Administrador | 200 OK |
| PUT | `/contatos/{id}` | Administrador | 200 OK |
| DELETE | `/contatos/{id}` | Administrador | 204 No Content |

### Segurança administrativa

As operações de consulta, atualização e exclusão exigem
o cabeçalho HTTP `X-Admin-Key`.

A chave válida é configurada pela variável de ambiente
`CONTATOS_ADMIN_KEY`, sem armazenar seu valor no código-fonte.

Requisições sem autorização recebem `403 Forbidden`.

O cadastro público de contatos não exige chave administrativa.

### Exemplo de cadastro

POST `/contatos`

```json
{
  "nome": "Usuario Teste",
  "email": "usuario@example.com",
  "mensagem": "Gostaria de saber mais sobre o ClimaCidadao."
}
```

Possíveis respostas:
- `201 Created`: contato cadastrado.
- `400 Bad Request`: dados inválidos.
- `403 Forbidden`: acesso administrativo não autorizado.
- `404 Not Found`: contato não encontrado.

