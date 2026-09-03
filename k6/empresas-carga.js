/**
 * Carga leve das rotas de empresa documentadas em swagger.yaml.
 *
 *   k6 run k6/empresas-carga.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { EmpresaCreateDTO } from './lib/contrato.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_TOKEN = __ENV.ADMIN_TOKEN || 'sua-chave-admin';

export const options = {
  vus: 5,
  duration: '20s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<600'],
  },
};

export default function () {
  const sessao = `carga_${__VU}_${__ITER}_${Date.now()}`;
  const create = { ...EmpresaCreateDTO, sessaoWhatsapp: sessao };

  const criado = http.post(`${BASE_URL}/empresas`, JSON.stringify(create), {
    headers: {
      'Content-Type': 'application/json',
      'x-admin-token': ADMIN_TOKEN,
    },
    tags: { contrato: 'EmpresaCreateDTO' },
  });

  check(criado, {
    'POST /empresas 201': (r) => r.status === 201,
    'resposta tem EmpresaResponseDTO': (r) => r.json('sessaoWhatsapp') === sessao,
  });

  const cliente = http.put(
    `${BASE_URL}/empresas/${sessao}/cliente`,
    JSON.stringify({
      nome: 'Recanto Vista Alegre',
      mensagemSaudacao: 'Olá! Como posso ajudar?',
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'x-cliente-token': 'token-cliente',
      },
      tags: { contrato: 'EmpresaUpdateDTO' },
    },
  );

  check(cliente, {
    'PUT cliente 200': (r) => r.status === 200,
  });

  sleep(0.2);
}
