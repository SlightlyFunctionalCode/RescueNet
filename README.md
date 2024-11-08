# RescueNet

## Descrição do Projeto

Este projeto é uma aplicação distribuída desenvolvida em Java para aprimorar a coordenação e comunicação entre diversas entidades em operações de emergência durante catástrofes naturais, como terremotos e inundações. A aplicação foi projetada para suportar cenários de crise, oferecendo uma plataforma segura e eficiente que conecta equipes de resposta em tempo real.

## Funcionalidades

* **Registo e Autenticação**: Cada entidade (com identificador, nome e perfil) deve-se registar e realizar login para ter acesso ao sistema.
* **Envio de Mensagens**: Enviar mensagens para outros utilizadores e grupos, independentemente da presença online dos destinatários.
* **Canais de Comunicação**: Criação e participação em canais específicos para a troca de informações.
* **Pedidos Hierárquicos**: Aprovações de ações específicas devem seguir a hierarquia dos perfis, com autorizações solicitadas e recebidas conforme a estrutura definida.
* **Notificações**: O sistema deve permitir o envio de notificações de alertas graves para todos os perfis ou grupos definidos.
* **Relatórios Periódicos**: O servidor deve enviar relatórios em intervalos definidos sobre as operações realizadas e o status dos utilizadores ativos.

### Tipos de Operações Específicas e Aprovação Hierárquica

* **Operação de Evacuação**: Esta operação requer aprovação de um perfil de nível alto, como um coordenador regional, para ser iniciada e executada por perfis de nível médio.
* **Ativação de Comunicações de Emergência**: A ativação e manutenção de canais de comunicação de emergência devem ser autorizadas por perfis de nível médio.
* **Distribuição de Recursos de Emergência**: A aprovação para esta operação deve ser feita por um perfil de nível baixo.

### Requisitos Técnicos
* **Servidor**: Capacidade de lidar com várias conexões simultâneas, com registo persistente de todas as transações.
* **Protocolo de Comunicação**: Implementação de um protocolo claro e documentado, que suporte a comunicação por sockets.
* **Multithreading e Controlo de Recursos**: Utilização de múltiplas threads para gestão de concorrêcia e controlo de acesso a recursos partilhados.
* **Interface de Utilizador**: Uma interface intuitiva para facilitar o uso do sistema.
* **Segurança**: Implementação de métodos de autenticação e integridade dos dados.
* **Histórico de Operações**: Registo das mensagens e notificações trocadas.

## Tecnologias

### Linguagem de Programação

* Java, para uma aplicação robusta e escalável.

### Arquitetura Distribuída

* Suporte a uma infraestrutura distribuída para alta disponibilidade e resiliência.

## Objetivo

A aplicação visa auxiliar a gestão de crises, garantindo uma resposta mais rápida, eficiente e organizada, contribuindo para salvar vidas e otimizar a mobilização de recursos em situações de alto risco.
