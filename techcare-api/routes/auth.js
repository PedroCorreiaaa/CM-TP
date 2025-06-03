const express = require('express');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const router = express.Router();

router.post('/token', (req, res) => {
  const { appSecret } = req.body;

  // Verifica se a chave está correta
  if (appSecret !== process.env.APP_SECRET) {
    return res.status(403).json({ error: 'Chave inválida' });
  }

  // Gera token válido por 30 dias
  const token = jwt.sign({ access: true }, process.env.JWT_SECRET, {
    expiresIn: '30d'
  });

  res.json({ token });
});

module.exports = router;
