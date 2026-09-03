// Payloads alinhados a swagger.yaml / especificacao/schemas
export const EmpresaCreateDTO = {
  nome: 'Recanto Vista Alegre',
  sessaoWhatsapp: 'sessao_recanto_01',
  ramoDeAtuacao: 'LOCACAO',
  precoBase: 500.0,
  modulosIniciais: ['IA_GEMINI', 'GOOGLE_CALENDAR'],
};

export function whatsappWebhookDTO({
  session = 'sessao_recanto_01',
  type = 'chat',
  fromMe = false,
  from = '5511999999999@c.us',
  body = 'Olá, gostaria de reservar',
} = {}) {
  return { session, type, fromMe, from, body };
}
