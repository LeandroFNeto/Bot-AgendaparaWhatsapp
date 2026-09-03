/**
 * Carga do POST /webhook/whatsapp (swagger.yaml).
 *
 * Foco: estressar o webhook e provar que o HTTP não espera o Observer
 * da Google Agenda (@Async). Mensagens filtradas também devem retornar 200.
 *
 *   k6 run k6/webhook-carga.js
 *   k6 run -e BASE_URL=http://localhost:8080 -e SESSAO=sessao_recanto_01 k6/webhook-carga.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { whatsappWebhookDTO } from './lib/contrato.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SESSAO = __ENV.SESSAO || 'sessao_recanto_01';

const webhookBloqueou = new Rate('webhook_bloqueou_alem_do_sla');
const duracaoWebhook = new Trend('webhook_duration', true);

export const options = {
  scenarios: {
    estresse_webhook: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 20 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<400', 'p(99)<800'],
    webhook_duration: ['p(95)<300'],
    webhook_bloqueou_alem_do_sla: ['rate<0.05'],
    checks: ['rate>0.99'],
  },
};

function payloadAleatorio(vu, iter) {
  const sorteio = Math.random();
  const from = `55119${String(10000000 + vu * 1000 + iter).slice(-8)}@c.us`;

  if (sorteio < 0.15) {
    return whatsappWebhookDTO({
      session: SESSAO,
      fromMe: true,
      from,
      body: 'mensagem do dono',
    });
  }
  if (sorteio < 0.25) {
    return whatsappWebhookDTO({
      session: SESSAO,
      type: 'chat',
      from: '12036399888777@g.us',
      body: 'mensagem de grupo',
    });
  }
  return whatsappWebhookDTO({
    session: SESSAO,
    from,
    body: `Quero reservar o espaço #${vu}-${iter}`,
  });
}

export default function () {
  const payload = payloadAleatorio(__VU, __ITER);
  const res = http.post(`${BASE_URL}/webhook/whatsapp`, JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
    tags: { contrato: 'WhatsappWebhookDTO' },
  });

  duracaoWebhook.add(res.timings.duration);
  webhookBloqueou.add(res.timings.duration > 300);

  check(res, {
    'contrato: webhook responde 200': (r) => r.status === 200,
    'observer: HTTP nao espera Google Agenda': (r) => r.timings.duration < 500,
  });

  sleep(0.1);
}
