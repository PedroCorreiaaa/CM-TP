const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const pool = require("./db");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
  res.send("API TechCare está online ✅");
});

app.post("/api/auth/login", async (req, res) => {
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

app.post("/api/auth/register", async (req, res) => {
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

app.get("/api/avarias", async (req, res) => {
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

app.post("/api/avarias", async (req, res) => {
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

    const novaAvaria = result.rows[0];

    res.status(201).json({
      success: true,
      message: "Avaria registada com sucesso.",
      avaria: novaAvaria
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({
      success: false,
      message: "Erro ao registar a avaria."
    });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () =>
  console.log(`Servidor a correr em http://localhost:${PORT}`)
);