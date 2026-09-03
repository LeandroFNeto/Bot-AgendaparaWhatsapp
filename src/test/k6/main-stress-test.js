/**
 * Orquestrador de carga (SDD).
 *
 * Este arquivo só define options e dispara os cenários. Cada cenário
 * vive no seu próprio módulo (contrato da rota + payload + checks).
 *
 *   k6 run src/test/k6/main-stress-test.js
 *   k6 run -e BASE_URL=http://localhost:8080 src/test/k6/main-stress-test.js
 *
 * Novo teste: crie cenario-ia.js (export function cenarioIA) e importe abaixo.
 */
import { cenarioWebhook } from './cenario-webhook.js';
// import { cenarioIA } from './cenario-ia.js';
// import { cenarioBanco } from './cenario-banco.js';

export const options = {
  vus: 50,
  duration: '30s',
};

export default function () {
  cenarioWebhook();
  // cenarioIA();
  // cenarioBanco();
}
