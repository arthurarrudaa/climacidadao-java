-- ClimaCidadao: executar uma vez no Oracle da FIAP, na mesma conta usada pela API.
-- Se as tabelas/sequences ja existem, nao execute novamente sem avaliar os dados.
-- Padrão FIAP: tb_..., cd_..., nm_..., ds_..., dt_..., sq_tb_...

CREATE TABLE tb_usuario (
    cd_usuario NUMBER(10) NOT NULL,
    nm_usuario VARCHAR2(100 CHAR) NOT NULL,
    ds_email   VARCHAR2(150 CHAR) NOT NULL,
    CONSTRAINT pk_tb_usuario PRIMARY KEY (cd_usuario)
);

-- E-mail unico mesmo que seja digitado com letras maiusculas.
CREATE UNIQUE INDEX ux_tb_usuario_email
    ON tb_usuario (LOWER(TRIM(ds_email)));

CREATE TABLE tb_favorito (
    cd_favorito NUMBER(10) NOT NULL,
    cd_usuario  NUMBER(10) NOT NULL,
    nm_cidade   VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_tb_favorito PRIMARY KEY (cd_favorito),
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (cd_usuario)
    REFERENCES tb_usuario (cd_usuario)
);

-- Impede cidade repetida para o mesmo usuario (independente de espacos/caixa).
CREATE UNIQUE INDEX ux_tb_favorito_usuario_cidade
    ON tb_favorito (cd_usuario, UPPER(TRIM(nm_cidade)));

CREATE TABLE tb_pesquisa (
    cd_pesquisa    NUMBER(10) NOT NULL,
    cd_usuario     NUMBER(10) NOT NULL,
    nm_cidade      VARCHAR2(100 CHAR) NOT NULL,
    vl_temperatura NUMBER(5,2) NOT NULL,
    dt_consulta    TIMESTAMP NOT NULL,
    CONSTRAINT pk_tb_pesquisa PRIMARY KEY (cd_pesquisa),
    CONSTRAINT fk_pesquisa_usuario FOREIGN KEY (cd_usuario)
    REFERENCES tb_usuario (cd_usuario),
    CONSTRAINT ck_pesquisa_temperatura CHECK (vl_temperatura BETWEEN -100 AND 70)
);


-- Mensagens enviadas pelo formulário de contato do React.
-- Esta tabela não tem relação 1:N com usuario: contatos são independentes.
CREATE TABLE tb_contato (
    cd_contato NUMBER(10) NOT NULL,
    nm_contato VARCHAR2(100 CHAR) NOT NULL,
    ds_email VARCHAR2(150 CHAR) NOT NULL,
    ds_mensagem VARCHAR2(500 CHAR) NOT NULL,
    CONSTRAINT pk_tb_contato PRIMARY KEY (cd_contato)
);

CREATE SEQUENCE sq_tb_contato START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE sq_tb_usuario START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE sq_tb_favorito START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE sq_tb_pesquisa START WITH 1 INCREMENT BY 1 NOCYCLE;

-- Nao ha ON DELETE CASCADE. Excluir usuario com favoritos/pesquisas retorna 409.
