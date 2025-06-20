const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const pool = require("./db");
const multer = require("multer");
const upload = multer({ storage: multer.memoryStorage() });
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const authRoutes = require("./routes/auth");
const verifyToken = require("./middleware/verifyToken");
app.use("/auth", authRoutes);

app.get("/", verifyToken, (req, res) => {
  res.send("API TechCare está online");
});

app.post("/api/login", async (req, res) => {
  const { email, password } = req.body;
  try {
    const userQuery = await pool.query("SELECT * FROM utilizador WHERE email = $1", [email]);
    const user = userQuery.rows[0];
    if (!user) return res.json({ success: false, message: "Utilizador não encontrado." });
    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) return res.json({ success: false, message: "Palavra-passe incorreta." });
    res.json({
      success: true,
      message: "Login efetuado com sucesso.",
      user: {
        id_utilizador: user.id_utilizador,
        nome: user.nome,
        email: user.email,
        id_tipo_utilizador: user.id_tipo_utilizador,
      },
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ success: false, message: "Erro no servidor." });
  }
});

app.post("/api/register", verifyToken, async (req, res) => {
  const { nome, email, password, id_tipo_utilizador } = req.body;
  try {
    const emailExists = await pool.query("SELECT * FROM utilizador WHERE email = $1", [email]);
    if (emailExists.rows.length > 0) return res.json({ success: false, message: "Email já registado." });
    const hashed = await bcrypt.hash(password, 10);
    const newUser = await pool.query(
      "INSERT INTO utilizador (id_tipo_utilizador, nome, email, password) VALUES ($1, $2, $3, $4) RETURNING *",
      [id_tipo_utilizador || 1, nome, email, hashed]
    );
    const user = newUser.rows[0];
    res.json({
      success: true,
      message: "Conta criada com sucesso.",
      user: {
        id_utilizador: user.id_utilizador,
        nome: user.nome,
        email: user.email,
        id_tipo_utilizador: user.id_tipo_utilizador,
      },
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ success: false, message: "Erro ao registar." });
  }
});

app.get("/api/avarias", verifyToken, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT a.*, ea.descricao AS estado_descricao
      FROM avaria a
      JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
      ORDER BY a.data_registo DESC
    `);
    const avarias = result.rows.map((row) => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: { id_estado_avaria: row.id_estado_avaria, descricao: row.estado_descricao },
    }));
    res.json(avarias);
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ error: "Erro ao obter avarias." });
  }
});

app.post("/api/avarias", verifyToken, async (req, res) => {
  const { id_utilizador, descricao, descricao_equipamento, localizacao, grau_urgencia = null } = req.body;
  try {
    const result = await pool.query(
      `INSERT INTO avaria (id_utilizador, id_estado_avaria, descricao, descricao_equipamento, localizacao, grau_urgencia) 
       VALUES ($1, 1, $2, $3, $4, $5) 
       RETURNING *`,
      [id_utilizador, descricao, descricao_equipamento, localizacao, grau_urgencia]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ success: false, message: "Erro ao registar a avaria." });
  }
});

app.get("/api/avarias/:id", verifyToken, async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query(
      `SELECT a.*, ea.descricao AS estado_descricao, u.nome AS nome_utilizador
       FROM avaria a
       JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
       JOIN utilizador u ON a.id_utilizador = u.id_utilizador
       WHERE a.id_avaria = $1`,
      [id]
    );
    if (result.rows.length === 0) return res.status(404).json({ message: "Avaria não encontrada." });
    const row = result.rows[0];
    res.json({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      nome_utilizador: row.nome_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: { id_estado_avaria: row.id_estado_avaria, descricao: row.estado_descricao },
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter a avaria." });
  }
});

app.get("/api/avarias/:id/comentarios", verifyToken, async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query(
      `SELECT c.*, u.nome AS nome_utilizador
       FROM comentario c
       JOIN utilizador u ON c.id_utilizador = u.id_utilizador
       WHERE c.id_avaria = $1
       ORDER BY c.data_comentario DESC`,
      [id]
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter comentários." });
  }
});

app.post("/api/avarias/:id/comentarios", verifyToken, async (req, res) => {
  const { id } = req.params;
  const { id_utilizador, conteudo } = req.body;
  try {
    const result = await pool.query(
      `INSERT INTO comentario (id_utilizador, id_avaria, conteudo)
       VALUES ($1, $2, $3)
       RETURNING *`,
      [id_utilizador, id, conteudo]
    );
    res.status(201).json({ success: true, comentario: result.rows[0] });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao adicionar comentário." });
  }
});

app.patch("/api/avarias/:id", verifyToken, async (req, res) => {
  const { id } = req.params;
  const { id_estado_avaria, grau_urgencia, id_responsavel, resolucao } = req.body;

  if (!id_responsavel)
    return res.status(400).json({ message: "id_responsavel é obrigatório." });

  if (!id_estado_avaria && !grau_urgencia && !resolucao)
    return res.status(400).json({ message: "Nenhum campo para atualizar." });

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    await client.query(`SET LOCAL app.id_responsavel = '${parseInt(id_responsavel)}'`);

    const checkState = await client.query(
      `SELECT id_estado_avaria FROM avaria WHERE id_avaria = $1`,
      [id]
    );
    if (checkState.rows[0].id_estado_avaria === 2) {
      await client.query("ROLLBACK");
      return res.status(400).json({ message: "Avaria já resolvida, não pode ser alterada." });
    }

    const result = await client.query(
      `UPDATE avaria 
       SET id_estado_avaria = COALESCE($1, id_estado_avaria), 
           grau_urgencia = COALESCE($2, grau_urgencia),
           resolucao = COALESCE($3, resolucao)
       WHERE id_avaria = $4 
       RETURNING *`,
      [id_estado_avaria, grau_urgencia, resolucao, id]
    );

    if (result.rows.length === 0) {
      await client.query("ROLLBACK");
      return res.status(404).json({ message: "Avaria não encontrada." });
    }

    if (id_estado_avaria === 2) {
      await client.query(
        `UPDATE avaria_tecnico 
         SET data_resolucao = NOW()
         WHERE id_avaria = $1 
         AND id_utilizador = (SELECT id_utilizador FROM avaria_tecnico WHERE id_avaria = $1 ORDER BY data_atribuicao DESC LIMIT 1)`,
        [id]
      );
    }

    await client.query("COMMIT");
    res.json({ success: true, avaria: result.rows[0] });

  } catch (err) {
    await client.query("ROLLBACK");
    console.error(err);
    res.status(500).json({ message: "Erro ao atualizar a avaria." });
  } finally {
    client.release();
  }
});

app.get("/api/avarias/user/:id", verifyToken, async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query(
      `SELECT a.*, ea.descricao AS estado_descricao
       FROM avaria a
       JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
       WHERE a.id_utilizador = $1
       ORDER BY a.data_registo DESC`,
      [id]
    );
    const avarias = result.rows.map((row) => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: { id_estado_avaria: row.id_estado_avaria, descricao: row.estado_descricao },
    }));
    res.json(avarias);
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ error: "Erro ao obter avarias do utilizador." });
  }
});

app.get("/api/avarias/tecnico/:id", verifyToken, async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query(
      `SELECT a.*, ea.descricao AS estado_descricao
       FROM avaria a
       JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
       JOIN avaria_tecnico at ON a.id_avaria = at.id_avaria
       WHERE at.id_utilizador = $1
         AND at.data_atribuicao = (SELECT MAX(at2.data_atribuicao) FROM avaria_tecnico at2 WHERE at2.id_avaria = at.id_avaria)
       ORDER BY a.data_registo DESC`,
      [id]
    );
    const avarias = result.rows.map((row) => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: { id_estado_avaria: row.id_estado_avaria, descricao: row.estado_descricao },
    }));
    res.json(avarias);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter avarias do técnico." });
  }
});

app.post("/api/avarias/:id/tecnico", verifyToken, async (req, res) => {
  const { id } = req.params;
  const { id_utilizador } = req.body;
  try {
    const tecnicoQuery = await pool.query(
      "SELECT * FROM utilizador WHERE id_utilizador = $1 AND id_tipo_utilizador = 2",
      [id_utilizador]
    );
    if (tecnicoQuery.rows.length === 0)
      return res.status(400).json({ message: "ID fornecido não corresponde a um técnico." });
    const result = await pool.query(
      `INSERT INTO avaria_tecnico (id_avaria, id_utilizador, data_atribuicao)
       VALUES ($1, $2, NOW())
       ON CONFLICT ON CONSTRAINT avaria_tecnico_pkey 
       DO UPDATE SET data_atribuicao = EXCLUDED.data_atribuicao
       RETURNING *`,
      [id, id_utilizador]
    );
    res.json({
      success: true,
      message: "Técnico atribuído com sucesso.",
      atribuicao: result.rows[0],
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao atribuir técnico." });
  }
});

app.get("/api/tecnicos", verifyToken, async (req, res) => {
  try {
    const result = await pool.query(
      "SELECT id_utilizador, nome, email, id_tipo_utilizador FROM utilizador WHERE id_tipo_utilizador = 2"
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter técnicos." });
  }
});

app.post("/api/tecnicos", verifyToken, async (req, res) => {
  if (!req.body) {
    return res.status(400).json({ success: false, message: "Corpo da requisição está vazio" });
  }
  const { nome, email, password } = req.body;
  try {
    const emailExists = await pool.query("SELECT * FROM utilizador WHERE email = $1", [email]);
    if (emailExists.rows.length > 0)
      return res.status(400).json({ success: false, message: "Email já registado." });
    const hashed = await bcrypt.hash(password, 10);
    const novoTecnico = await pool.query(
      "INSERT INTO utilizador (id_tipo_utilizador, nome, email, password) VALUES (2, $1, $2, $3) RETURNING *",
      [nome, email, hashed]
    );
    res.status(201).json({ success: true, tecnico: novoTecnico.rows[0] });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao registar técnico." });
  }
});

app.get("/api/user/:id/notificacoes", verifyToken, async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query(
      `SELECT DISTINCT n.*
       FROM notificacao n
       JOIN avaria a ON n.id_avaria = a.id_avaria
       LEFT JOIN avaria_tecnico at ON a.id_avaria = at.id_avaria
       WHERE n.id_utilizador = $1
       ORDER BY n.data_emissao DESC`,
      [id]
    );
    const notificacoes = result.rows.map((row) => ({
      id_notificacao: row.id_notificacao,
      id_avaria: row.id_avaria,
      mensagem: row.mensagem,
      data_emissao: new Date(row.data_emissao).toISOString(),
      id_utilizador: row.id_utilizador,
    }));
    res.json(notificacoes);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter notificações." });
  }
});

app.get("/api/relatorios/estatisticas", verifyToken, async (req, res) => {
  try {
    const { datas, tipoEquipamento, localizacao } = req.query;

    let filtroWhere = "WHERE 1=1";
    const params = [];

    if (datas) {
      const [dataInicio, dataFim] = datas.split(" a ");
      if (dataInicio && dataFim) {
        params.push(dataInicio, dataFim);
        filtroWhere += ` AND a.data_registo BETWEEN $${params.length - 1} AND $${params.length}`;
      }
    }

    if (tipoEquipamento) {
      params.push(`%${tipoEquipamento}%`);
      filtroWhere += ` AND a.descricao_equipamento ILIKE $${params.length}`;
    }

    if (localizacao) {
      params.push(`%${localizacao}%`);
      filtroWhere += ` AND a.localizacao ILIKE $${params.length}`;
    }

    const totalQuery = `SELECT COUNT(*) AS total FROM avaria a ${filtroWhere}`;
    const resolvidasQuery = `SELECT COUNT(*) AS resolvidas FROM avaria a ${filtroWhere} AND a.id_estado_avaria = 2`;
    const tempoMedioQuery = `
      SELECT
        COALESCE(ROUND(AVG(DATE_PART('day', at.data_resolucao - a.data_registo))), 0) AS tempo_medio_dias
      FROM avaria a
      JOIN avaria_tecnico at ON at.id_avaria = a.id_avaria
      ${filtroWhere} AND a.id_estado_avaria = 2
    `;

    // Executa as queries
    const { rows: totalRows } = await pool.query(totalQuery, params);
    const { rows: resolvidasRows } = await pool.query(resolvidasQuery, params);
    const { rows: tempoRows } = await pool.query(tempoMedioQuery, params);

    res.json({
      total_avarias: parseInt(totalRows[0].total, 10),
      resolvidas: parseInt(resolvidasRows[0].resolvidas, 10),
      tempo_medio_dias: parseInt(tempoRows[0].tempo_medio_dias, 10)
    });

  } catch (err) {
    console.error("Erro ao obter estatísticas:", err);
    res.status(500).json({ erro: "Erro ao obter estatísticas" });
  }
});

app.post("/api/avarias/:id/imagem", verifyToken, upload.single("imagem"), async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query(
      `INSERT INTO imagem (id_avaria, imagem, nome, extensao)
       VALUES ($1, $2, $3, $4)`,
      [id, req.file.buffer, req.file.originalname, "jpg"]
    );
    res.status(201).json({ success: true, message: "Imagem enviada com sucesso." });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ success: false, message: "Erro ao enviar imagem." });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Servidor a correr em http://localhost:${PORT}`));