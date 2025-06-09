const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const pool = require("./db");
const multer = require('multer');
const upload = multer({ storage: multer.memoryStorage() });
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

const authRoutes = require('./routes/auth');
const verifyToken = require('./middleware/verifyToken');
app.use('/auth', authRoutes);

app.get("/", verifyToken, (req, res) => {
  res.send("API TechCare está online ✅");
});

app.post("/api/login", verifyToken, async (req, res) => {
  const { email, password } = req.body;

  try {
    const userQuery = await pool.query(
      "SELECT * FROM utilizador WHERE email = $1",
      [email]
    );

    const user = userQuery.rows[0];
    if (!user)
      return res.json({ success: false, message: "Utilizador não encontrado." });

    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch)
      return res.json({ success: false, message: "Palavra-passe incorreta." });

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
  const { nome, email, password } = req.body;

  try {
    const emailExists = await pool.query(
      "SELECT * FROM utilizador WHERE email = $1",
      [email]
    );

    if (emailExists.rows.length > 0)
      return res.json({ success: false, message: "Email já registado." });

    const hashed = await bcrypt.hash(password, 10);
    const newUser = await pool.query(
      "INSERT INTO utilizador (id_tipo_utilizador, nome, email, password) VALUES (1, $1, $2, $3) RETURNING *",
      [nome, email, hashed]
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
// Obter todas as avarias
app.get("/api/avarias", verifyToken, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        a.id_avaria,
        a.id_utilizador,
        a.descricao,
        a.descricao_equipamento,
        a.localizacao,
        a.grau_urgencia,
        to_char(a.data_registo, 'YYYY-MM-DD') AS data_registo,
        a.resolucao,
        ea.id_estado_avaria,
        ea.descricao AS estado_descricao
      FROM avaria a
      JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
      ORDER BY a.data_registo DESC
    `);

    const avarias = result.rows.map(row => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: {
        id_estado_avaria: row.id_estado_avaria,
        descricao: row.estado_descricao
      }
    }));

    res.json(avarias);
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ error: "Erro ao obter avarias." });
  }
});

// Criar nova avaria
app.post("/api/avarias", verifyToken, async (req, res) => {
  const {
    id_utilizador,
    descricao,
    descricao_equipamento,
    localizacao,
    grau_urgencia
  } = req.body;

  try {
    const result = await pool.query(
      `INSERT INTO avaria 
        (id_utilizador, id_estado_avaria, descricao, descricao_equipamento, localizacao, grau_urgencia) 
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

// Obter dados de uma avaria
app.get("/api/avarias/:id", verifyToken, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT 
        a.*,
        ea.descricao AS estado_descricao,
        u.nome AS nome_utilizador
      FROM avaria a
      JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
      JOIN utilizador u ON a.id_utilizador = u.id_utilizador
      WHERE a.id_avaria = $1
    `, [id]);

    if (result.rows.length === 0)
      return res.status(404).json({ message: "Avaria não encontrada." });

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
      estado: {
        id_estado_avaria: row.id_estado_avaria,
        descricao: row.estado_descricao
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter a avaria." });
  }
});

// Listar comentários de uma avaria
app.get("/api/avarias/:id/comentarios", verifyToken, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT c.*, u.nome AS nome_utilizador
      FROM comentario c
      JOIN utilizador u ON c.id_utilizador = u.id_utilizador
      WHERE c.id_avaria = $1
      ORDER BY c.data_comentario DESC
    `, [id]);

    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter comentários." });
  }
});

// Criar comentário para uma avaria
app.post("/api/avarias/:id/comentarios", verifyToken, async (req, res) => {
  const { id } = req.params;
  const { id_utilizador, conteudo } = req.body;

  try {
    const result = await pool.query(`
      INSERT INTO comentario (id_utilizador, id_avaria, conteudo)
      VALUES ($1, $2, $3)
      RETURNING *
    `, [id_utilizador, id, conteudo]);

    res.status(201).json({
      success: true,
      comentario: result.rows[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao adicionar comentário." });
  }
});

// Atualizar uma avaria parcialmente
app.patch("/api/avarias/:id", verifyToken, async (req, res) => {
  const { id } = req.params;
  const campos = req.body;

  const chaves = Object.keys(campos);
  const valores = Object.values(campos);

  if (chaves.length === 0)
    return res.status(400).json({ message: "Nenhum campo para atualizar." });

  const setClause = chaves.map((chave, idx) => `${chave} = $${idx + 1}`).join(", ");

  try {
    const result = await pool.query(
      `UPDATE avaria SET ${setClause} WHERE id_avaria = $${chaves.length + 1} RETURNING *`,
      [...valores, id]
    );

    res.json({
      success: true,
      avaria: result.rows[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao atualizar a avaria." });
  }
});

app.get("/api/avarias/user/:id", verifyToken, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT 
        a.id_avaria,
        a.id_utilizador,
        a.descricao,
        a.descricao_equipamento,
        a.localizacao,
        a.grau_urgencia,
        to_char(a.data_registo, 'YYYY-MM-DD') AS data_registo,
        a.resolucao,
        ea.id_estado_avaria,
        ea.descricao AS estado_descricao
      FROM avaria a
      JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
      WHERE a.id_utilizador = $1
      ORDER BY a.data_registo DESC
    `, [id]);

    const avarias = result.rows.map(row => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: {
        id_estado_avaria: row.id_estado_avaria,
        descricao: row.estado_descricao
      }
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
    const result = await pool.query(`
      SELECT 
        a.id_avaria,
        a.id_utilizador,
        a.descricao,
        a.descricao_equipamento,
        a.localizacao,
        a.grau_urgencia,
        to_char(a.data_registo, 'YYYY-MM-DD') AS data_registo,
        a.resolucao,
        ea.id_estado_avaria,
        ea.descricao AS estado_descricao
      FROM avaria a
      JOIN estado_avaria ea ON a.id_estado_avaria = ea.id_estado_avaria
      JOIN avaria_tecnico at ON a.id_avaria = at.id_avaria
      WHERE at.id_tecnico = $1
        AND at.data_atribuicao = (
          SELECT MAX(at2.data_atribuicao)
          FROM avaria_tecnico at2
          WHERE at2.id_avaria = at.id_avaria
        )
      ORDER BY a.data_registo DESC
    `, [id]);

    const avarias = result.rows.map(row => ({
      id_avaria: row.id_avaria,
      id_utilizador: row.id_utilizador,
      descricao: row.descricao,
      descricao_equipamento: row.descricao_equipamento,
      localizacao: row.localizacao,
      grau_urgencia: row.grau_urgencia,
      data_registo: row.data_registo,
      resolucao: row.resolucao,
      estado: {
        id_estado_avaria: row.id_estado_avaria,
        descricao: row.estado_descricao
      }
    }));
    res.json(avarias);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter avarias do técnico." });
  }
});


app.post("/api/avarias/:id/tecnico", verifyToken, async (req, res) => {
  const { id } = req.params;
  const { id_tecnico } = req.body;

  try {
    const result = await pool.query(`
      INSERT INTO avaria_tecnico (id_avaria, id_tecnico, data_atribuicao)
      VALUES ($1, $2, NOW())
      RETURNING *
    `, [id, id_tecnico]);

    res.json({
      success: true,
      message: "Técnico atribuído com sucesso.",
      atribuicao: result.rows[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao atribuir técnico." });
  }
});


// Listar técnicos
app.get("/api/tecnicos", verifyToken, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT * FROM utilizador WHERE id_tipo_utilizador = 2
    `);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter técnicos." });
  }
});

// Registar técnico
app.post("/api/tecnicos", verifyToken, async (req, res) => {
  const { nome, email, password } = req.body;

  try {
    const emailExists = await pool.query(
      "SELECT * FROM utilizador WHERE email = $1",
      [email]
    );

    if (emailExists.rows.length > 0)
      return res.json({ success: false, message: "Email já registado." });

    const hashed = await bcrypt.hash(password, 10);
    const novoTecnico = await pool.query(
      `INSERT INTO utilizador (id_tipo_utilizador, nome, email, password)
       VALUES (2, $1, $2, $3) RETURNING *`,
      [nome, email, hashed]
    );

    res.status(201).json({
      success: true,
      tecnico: novoTecnico.rows[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao registar técnico." });
  }
});

// Notificações
app.get("/api/user/:id/notificacoes", verifyToken, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT DISTINCT n.*
      FROM notificacao n
      JOIN avaria a ON n.id_avaria = a.id_avaria
      LEFT JOIN atribuir_tecnico at ON a.id_avaria = at.id_avaria
      WHERE a.id_utilizador = $1 OR at.id_utilizador = $1
      ORDER BY n.data_notificacao DESC
    `, [id]);

    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao obter notificações." });
  }
});

// Criar notificação
app.post("/api/notificacoes", verifyToken, async (req, res) => {
  const { id_avaria, mensagem } = req.body;

  if (!id_avaria || !mensagem) {
    return res.status(400).json({ message: "Dados em falta (id_avaria, mensagem)." });
  }

  try {
    const result = await pool.query(`
      INSERT INTO notificacao (id_avaria, mensagem)
      VALUES ($1, $2)
      RETURNING *
    `, [id_avaria, mensagem]);

    res.status(201).json({
      success: true,
      notificacao: result.rows[0]
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Erro ao criar notificação." });
  }
});

// Upload de imagem
app.post("/api/avarias/:id/imagem", verifyToken, upload.single('imagem'), async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query(
      `INSERT INTO imagem (id_avaria, imagem, nome, extensao)
       VALUES ($1, $2, $3, $4)`,
      [id, req.file.buffer, req.file.originalname, 'jpg']
    );
    res.status(201).json({ success: true, message: "Imagem enviada com sucesso." });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ success: false, message: "Erro ao enviar imagem." });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () =>
  console.log(`Servidor a correr em http://localhost:${PORT}`)
);