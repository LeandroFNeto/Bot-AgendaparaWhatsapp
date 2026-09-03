/**
 * Cenário de carga: POST /webhook/whatsapp (WhatsappWebhookDTO).
 *
 * Isolamento multi-tenant: cada VU (__VU) usa um JID distinto, forçando
 * o GerenciadorSessao a manter 50 estados de conversa em paralelo.
 *
 * Importado por main-stress-test.js. Não execute este arquivo isolado:
 *   k6 run src/test/k6/main-stress-test.js
 */
import http from 'k6/http';
import { check, sleep, group } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SESSAO = 'sessao_recanto_01';

export function cenarioWebhook() {
  group('Simulação de Tráfego no Webhook WhatsApp', function () {
    const from = `55119888800${__VU}@c.us`;

    const payload = JSON.stringify({
      session: SESSAO,
      type: 'chat',
      fromMe: false,
      from: from,
      body: 'Oi, quero reservar',
    });

    const res = http.post(`${BASE_URL}/webhook/whatsapp`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
      'status é 200 OK': (r) => r.status === 200,
      'tempo de resposta < 500ms': (r) => r.timings.duration < 500,
    });

    // Pausa humana: o HTTP já retornou; o Observer segue em background.
    sleep(1);
  });
}
