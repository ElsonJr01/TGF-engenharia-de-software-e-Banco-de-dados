# TheClub – Jornal Universitário

TheClub é uma aplicação web de jornal universitário pensada para organizar e divulgar notícias, eventos, editais e projetos acadêmicos, aproximando a comunidade interna e externa da universidade.  
O sistema foi desenvolvido como projeto de Engenharia de Software e Banco de Dados, com foco em boas práticas de arquitetura, autenticação e fluxo editorial.

## Tecnologias

- **Backend:** Java 17, Spring Boot, Spring Data JPA, Spring Security (JWT), Swagger / OpenAPI  
- **Frontend:** React + Vite, React Router, Context API, integração com API REST  
- **Banco de dados:** MySQL  
- **Infra:** Docker / Docker Compose  
- **Ferramentas:** IntelliJ IDEA, VS Code, Git e GitHub

## Funcionalidades principais

- Cadastro, edição e listagem de **notícias, categorias, eventos e editais**  
- Área pública para leitura e busca de conteúdos  
- Área administrativa com:
  - Gerenciamento de usuários e perfis (admin, editor, leitor)
  - Fluxo de publicação de artigos (criação, edição, alteração de status)
  - Moderação de comentários
- Upload de imagens e arquivos associados às matérias
- Autenticação e autorização via **JWT**
- Documentação da API com **Swagger UI**

## Estrutura do projeto

- `src/TheCub/java/app` – controllers REST do backend  
- `src/TheCub/java/dominio` – entidades, DTOs, enums e exceptions  
- `src/TheCub/java/lib` – segurança, JWT, configuração, repositórios  
- `src/TheCub/java/servicos_tecnicos` – camada de serviços  
- `src/TheCub/resources` – `application.properties` e recursos do backend  
- `the-club-frontend/` – projeto React (páginas públicas e área admin)

## Como executar com Docker

1. Configure as variáveis de ambiente (credenciais do banco, JWT secret etc.) nos arquivos `.env` correspondentes.
2. Na raiz do projeto, execute:


3. Acesse:
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (ou similar)
- Frontend: `http://localhost:5173` (porta configurada no Vite)

## Como executar em desenvolvimento (sem Docker)

1. **Backend**
- Configure o MySQL e o `application.properties`.
- Na pasta do backend, execute:

  ```
  mvn spring-boot:run
  ```

2. **Frontend**
- Na pasta `the-club-frontend`:

  ```
  npm install
  npm run dev
  ```

## Status do projeto

O projeto está em desenvolvimento contínuo, com foco em:
- Refinar o fluxo editorial do jornal
- Melhorar a experiência do usuário no frontend
- Integrar novas seções ligadas a extensão, pesquisa e eventos da universidade

Contribuições, issues e sugestões são bem-vindas.
