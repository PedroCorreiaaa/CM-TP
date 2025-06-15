-- Criar base de dados
CREATE DATABASE techcare;

-- Conectar à base de dados
\c techcare

-- Tabela: tipo_utilizador
CREATE TABLE tipo_utilizador (
    id_tipo_utilizador SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL
);

-- Dados base
INSERT INTO tipo_utilizador (descricao) VALUES
('Utilizador'), ('Técnico'), ('Gestor');

-- Tabela: utilizador
CREATE TABLE utilizador (
    id_utilizador SERIAL PRIMARY KEY,
    id_tipo_utilizador INT NOT NULL,
    nome TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    FOREIGN KEY (id_tipo_utilizador) REFERENCES tipo_utilizador(id_tipo_utilizador)
);

-- Utilizadores de exemplo
INSERT INTO utilizador (id_tipo_utilizador, nome, email, password) VALUES
(1, 'Afonso Fernandes', 'afonso@techcare.com', '$2b$10$5OacjwJTYgvK52bEjSgcjuy9TReJLvmljf0ankbIWqRqGtlpfJGGm'),
(2, 'Jorge Amorim', 'jorge@techcare.com', '$2b$10$5OacjwJTYgvK52bEjSgcjuy9TReJLvmljf0ankbIWqRqGtlpfJGGm'),
(2, 'Ana', 'ana@techcare.com', '$2b$10$5OacjwJTYgvK52bEjSgcjuy9TReJLvmljf0ankbIWqRqGtlpfJGGm'),
(3, 'Pedro Correia', 'pedro@techcare.com', '$2b$10$5OacjwJTYgvK52bEjSgcjuy9TReJLvmljf0ankbIWqRqGtlpfJGGm');

-- Tabela: estado_avaria
CREATE TABLE estado_avaria (
    id_estado_avaria SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL
);

-- Estados possíveis
INSERT INTO estado_avaria (descricao) VALUES
('Pendente'), ('Resolvido'), ('Em Progresso');

-- Tabela: avaria
CREATE TABLE avaria (
    id_avaria SERIAL PRIMARY KEY,
    id_utilizador INT NOT NULL,
    id_estado_avaria INT NOT NULL,
    data_registo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grau_urgencia TEXT,
    descricao_equipamento TEXT,
    descricao TEXT,
    localizacao TEXT,
    resolucao TEXT,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id_utilizador),
    FOREIGN KEY (id_estado_avaria) REFERENCES estado_avaria(id_estado_avaria)
);

-- Tabela: avaria_tecnico
CREATE TABLE avaria_tecnico (
    id_avaria INT,
    id_utilizador INT,
    data_atribuicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_resolucao TIMESTAMP,
    PRIMARY KEY (id_avaria, id_utilizador),
    FOREIGN KEY (id_avaria) REFERENCES avaria(id_avaria),
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id_utilizador)
);

-- Tabela: imagem
CREATE TABLE imagem (
    id_imagem SERIAL PRIMARY KEY,
    id_avaria INT NOT NULL,
    imagem BYTEA,
    nome TEXT,
    extensao TEXT,
    FOREIGN KEY (id_avaria) REFERENCES avaria(id_avaria)
);

-- Tabela: notificacao
CREATE TABLE notificacao (
    id_notificacao SERIAL PRIMARY KEY,
    id_avaria INT NOT NULL,
    mensagem TEXT NOT NULL,
    data_emissao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_utilizador INT NOT NULL,
    FOREIGN KEY (id_avaria) REFERENCES avaria(id_avaria),
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id_utilizador)
);

-- Tabela: estado_avaria_log
CREATE TABLE estado_avaria_log (
    id_estado_avaria_log SERIAL PRIMARY KEY,
    id_estado_avaria_antigo INT,
    id_estado_avaria_novo INT NOT NULL,
    id_avaria INT NOT NULL,
    id_responsavel INT NOT NULL,
    data TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estado_avaria_antigo) REFERENCES estado_avaria(id_estado_avaria),
    FOREIGN KEY (id_estado_avaria_novo) REFERENCES estado_avaria(id_estado_avaria),
    FOREIGN KEY (id_avaria) REFERENCES avaria(id_avaria),
    FOREIGN KEY (id_responsavel) REFERENCES utilizador(id_utilizador)
);

-- Função: inserir_log_estado_avaria
CREATE OR REPLACE FUNCTION inserir_log_estado_avaria()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  estado_antigo INT;
  estado_novo INT;
  id_responsavel INT;
  id_tecnico INT;
  nome_tecnico TEXT;
  v_descricao_equipamento TEXT;
  v_id_utilizador INT;
BEGIN
  BEGIN
    id_responsavel := current_setting('app.id_responsavel', true)::INT;
  EXCEPTION
    WHEN OTHERS THEN
      RAISE EXCEPTION 'Variável app.id_responsavel não está definida.';
  END;

  estado_antigo := OLD.id_estado_avaria;
  estado_novo := NEW.id_estado_avaria;

  SELECT descricao_equipamento, id_utilizador
  INTO v_descricao_equipamento, v_id_utilizador
  FROM avaria
  WHERE id_avaria = NEW.id_avaria;

  IF estado_antigo IS DISTINCT FROM estado_novo THEN
    INSERT INTO estado_avaria_log (
      id_estado_avaria_antigo,
      id_estado_avaria_novo,
      id_avaria,
      id_responsavel,
      data
    ) VALUES (
      estado_antigo,
      estado_novo,
      NEW.id_avaria,
      id_responsavel,
      now()
    );

    IF estado_antigo = 1 AND estado_novo = 3 THEN
      SELECT at.id_utilizador, u.nome
      INTO id_tecnico, nome_tecnico
      FROM avaria_tecnico at
      JOIN utilizador u ON u.id_utilizador = at.id_utilizador
      WHERE at.id_avaria = NEW.id_avaria
      ORDER BY at.data_atribuicao DESC
      LIMIT 1;

      IF id_tecnico IS NOT NULL THEN
        INSERT INTO notificacao (id_avaria, mensagem, id_utilizador, data_emissao)
        VALUES (
          NEW.id_avaria,
          'Foi-lhe atribuída a avaria ' || v_descricao_equipamento,
          id_tecnico,
          now()
        );

        INSERT INTO notificacao (id_avaria, mensagem, id_utilizador, data_emissao)
        VALUES (
          NEW.id_avaria,
          'O técnico ' || nome_tecnico || ' foi atribuído à avaria ' || v_descricao_equipamento,
          v_id_utilizador,
          now()
        );
      END IF;

    ELSIF estado_novo = 2 THEN
      INSERT INTO notificacao (id_avaria, mensagem, id_utilizador, data_emissao)
      VALUES (
        NEW.id_avaria,
        'A avaria ' || v_descricao_equipamento || ' foi resolvida',
        v_id_utilizador,
        now()
      );
    END IF;
  END IF;

  RETURN NEW;
END;
$$;

-- Trigger de update em avaria
DROP TRIGGER IF EXISTS trg_log_estado_avaria ON avaria;
CREATE TRIGGER trg_log_estado_avaria
AFTER UPDATE ON avaria
FOR EACH ROW
WHEN (OLD.id_estado_avaria IS DISTINCT FROM NEW.id_estado_avaria)
EXECUTE FUNCTION inserir_log_estado_avaria();

-- Função: notificar gestores após inserção de avaria
CREATE OR REPLACE FUNCTION notificar_gestores_nova_avaria()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  gestor RECORD;
  v_descricao_equipamento TEXT;
BEGIN
  v_descricao_equipamento := COALESCE(NEW.descricao_equipamento, '[sem descrição]');
  FOR gestor IN
    SELECT id_utilizador FROM utilizador WHERE id_tipo_utilizador = 3
  LOOP
    INSERT INTO notificacao (id_avaria, mensagem, id_utilizador, data_emissao)
    VALUES (
      NEW.id_avaria,
      'Nova avaria registada: ' || v_descricao_equipamento,
      gestor.id_utilizador,
      now()
    );
  END LOOP;
  RETURN NEW;
END;
$$;

-- Trigger de criação de nova avaria
DROP TRIGGER IF EXISTS trg_nova_avaria_notificacao ON avaria;
CREATE TRIGGER trg_nova_avaria_notificacao
AFTER INSERT ON avaria
FOR EACH ROW
EXECUTE FUNCTION notificar_gestores_nova_avaria();

-- Função: atribuir automaticamente ao técnico com menos avarias
CREATE OR REPLACE FUNCTION atribuir_tecnico_com_menos_avarias()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
  v_tecnico_id INT;
BEGIN
  SELECT u.id_utilizador
  INTO v_tecnico_id
  FROM utilizador u
  WHERE u.id_tipo_utilizador = 2
  ORDER BY (
    SELECT COUNT(*)
    FROM avaria_tecnico at
    JOIN avaria a ON a.id_avaria = at.id_avaria
    WHERE at.id_utilizador = u.id_utilizador
      AND a.id_estado_avaria != 2
      AND at.data_resolucao IS NULL
  ) ASC, u.id_utilizador
  LIMIT 1;

  IF v_tecnico_id IS NOT NULL THEN
    UPDATE avaria
    SET grau_urgencia = 'Baixa'
    WHERE id_avaria = NEW.id_avaria AND grau_urgencia IS NULL;

    INSERT INTO avaria_tecnico (id_avaria, id_utilizador, data_atribuicao)
    VALUES (NEW.id_avaria, v_tecnico_id, now());

    INSERT INTO notificacao (id_avaria, mensagem, id_utilizador, data_emissao)
    VALUES (
      NEW.id_avaria,
      'Foi-lhe atribuída automaticamente uma nova avaria.',
      v_tecnico_id,
      now()
    );
  END IF;
  RETURN NEW;
END;
$$;

-- Trigger: atribui automaticamente ao técnico com menos avarias
DROP TRIGGER IF EXISTS trg_atribuir_tecnico ON avaria;
CREATE TRIGGER trg_atribuir_tecnico
AFTER INSERT ON avaria
FOR EACH ROW
EXECUTE FUNCTION atribuir_tecnico_com_menos_avarias();


