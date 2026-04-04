# Smart Booking API (WhatsApp + Google Calendar)

Uma API desenvolvida em Spring Boot para automação de reservas. O sistema funciona como o cérebro de um bot de WhatsApp, interagindo com clientes e gerenciando disponibilidades no Google Agenda.

O projeto evoluiu de um protótipo simples para uma arquitetura SaaS Multi-tenant, permitindo atender diferentes negócios (locações, clínicas, barbearias) de forma simultânea e isolada.

## Tecnologias

* **Backend:** Java, Spring Boot
* **Banco de Dados:** PostgreSQL, Hibernate/JPA
* **Mensageria:** WhatsApp Web API (WPPConnect)
* **Infraestrutura:** Docker e Docker Compose

## Arquitetura e Padrões

Projeto estruturado com foco em Clean Architecture e princípios SOLID:
* **Strategy & Factory:** Roteamento dinâmico de atendimento baseado no ramo do cliente, permitindo escalar sem modificar o roteador principal.
* **Observer:** Processamento assíncrono para integração com o Google Agenda, evitando travamentos na comunicação com o usuário no WhatsApp.
* **Singleton:** Gerenciamento de instâncias na memória para otimizar performance e evitar leitura de disco desnecessária.

## Como Executar

1. Clone o repositório.
2. Adicione suas chaves de API na pasta `resources`.
3. Configure as credenciais do Google Agenda.
4. Execute o comando abaixo para iniciar a infraestrutura:

docker-compose up -d

## 📖 Diário de Engenharia

Para entender as decisões arquiteturais por trás deste projeto — como a resolução de vazamentos de memória no Docker (SIGTERM), otimizações de banco de dados (LazyInitializationException) e a migração de ambiente para ganho de performance — acesse a documentação técnica:

⚔️[Ler o Diário de guerra](docs/diário_guerra.md)
