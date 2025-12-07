# 📰 TheClub – Jornal Universitário

TheClub é uma aplicação web de jornal universitário desenvolvida para organizar e divulgar notícias, eventos, editais e projetos acadêmicos da universidade.  
O sistema apoia a comunicação institucional e estudantil, permitindo que diferentes perfis (admin, editor, leitor) participem do fluxo editorial de forma segura e estruturada.

---

## ✨ Visão Geral

- Portal público para leitura de notícias, eventos e editais
- Painel administrativo para gestão de conteúdo e usuários
- Backend em **Java / Spring Boot** com autenticação via **JWT**
- Frontend em **React + Vite**, consumindo uma API REST
- Banco de dados relacional (**MySQL**) com mapeamento via JPA
- Contêineres de infraestrutura orquestrados com **Docker Compose**

---

## 🧱 Arquitetura

**Stack principal**

- **Linguagem:** Java 17, JavaScript/TypeScript (frontend)
- **Backend:** Spring Boot, Spring Web, Spring Data JPA, Spring Security (JWT)
- **Documentação da API:** OpenAPI/Swagger UI
- **Frontend:** React, Vite, React Router, Context API / hooks
- **Banco:** MySQL
- **Build:** Maven
- **Infra:** Docker, Docker Compose
- **Ferramentas:** IntelliJ IDEA, VS Code, Git e GitHub

---

## 📂 Estrutura do Projeto

TheClub/
├─ pom.xml
├─ docker-compose.yml
├─ src/
│ └─ TheCub/
│ ├─ java/
│ │ ├─ app/ # Controllers REST (Artigos, Categorias, Eventos, Usuários etc.)
│ │ ├─ dominio/
│ │ │ ├─ entidades/ # Entidades JPA (Artigo, Usuario, Categoria, Comentario...)
│ │ │ ├─ dto/ # DTOs de request/response
│ │ │ ├─ enums/ # Enums de domínio (StatusArtigo, TipoUsuario etc.)
│ │ │ ├─ exception/ # Exceptions e GlobalExceptionHandler
│ │ │ └─ model/ # Modelos auxiliares de autenticação
│ │ ├─ lib/
│ │ │ ├─ config/ # Configurações (CORS, Swagger, upload, WebConfig)
│ │ │ ├─ repository/ # Repositórios Spring Data JPA
│ │ │ ├─ security/JWT # JwtService, filtros, CustomUserDetails, SecurityConfig
│ │ │ └─ ... # Outras libs internas
│ │ ├─ servicos_tecnicos/ # Serviços (ArtigoService, UsuarioService etc.)
│ │ └─ ui/ # Telas Java (caso use interface desktop/admin)
│ └─ resources/
│ └─ application.properties
└─ the-club-frontend/
├─ package.json
├─ src/
│ ├─ api/ # Configuração base da API
│ ├─ auth/ # Contexto de autenticação, rotas protegidas
│ ├─ components/ # Header, Footer, CardNoticia etc.
│ ├─ pages/ # Páginas públicas
│ ├─ pages/admin/ # Páginas da área administrativa (Dashboard, NovaNoticia...)
│ ├─ routes/ # Definição das rotas com React Router
│ └─ styles/ # CSS / estilos globais
└─ public/


---

## 🔐 Funcionalidades

### Área pública

- Listagem de notícias por categoria
- Página de detalhes da notícia (conteúdo, autor, data, comentários)
- Listagem de eventos e editais
- Busca e filtragem de conteúdos
- Layout responsivo para desktop e mobile

### Área administrativa

- Login com **JWT** (roles: ADMIN, EDITOR, etc.)
- CRUD de:
  - Artigos (com status de publicação)
  - Categorias
  - Eventos
  - Editais
  - Usuários
- Moderação de comentários
- Upload de imagens/arquivos associados às matérias

### Backend

- API RESTful organizada por recursos (`/artigos`, `/categorias`, `/usuarios` etc.)
- Validações com Bean Validation
- Tratamento centralizado de erros com `GlobalExceptionHandler`
- Repositórios Spring Data JPA e queries especializadas
- Configuração de CORS para integração com o frontend
- Documentação automática com Swagger/OpenAPI (ex.: `/swagger-ui.html` ou `/swagger-ui/index.html`)

---

## 🐳 Executando com Docker

Pré-requisitos:

- Docker
- Docker Compose

Passos:

1. Clonar o repositório
git clone https://github.com/SEU_USUARIO/SEU_REPO.git
cd SEU_REPO

2. Ajustar variáveis de ambiente (banco, JWT, etc.) se necessário
3. Subir tudo com Docker
docker-compose up --build


Acessos padrão (ajuste se usar outras portas):

- Backend: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html  
- Frontend: http://localhost:5173  

---

## 🚀 Executando em Desenvolvimento (sem Docker)

### Backend (Spring Boot)

Pré-requisitos:

- JDK 17+
- Maven
- MySQL rodando (e banco configurado em `application.properties`)

Na raiz do backend (onde está o pom.xml)
mvn spring-boot:run


### Frontend (React + Vite)

Pré-requisitos:

- Node.js (LTS)
- npm ou yarn


O Vite geralmente sobe em `http://localhost:5173`.

---

## 🧪 Testes

- Testes de backend podem ser executados com:


---

## 🗺️ Roadmap / próximos passos

Algumas possíveis melhorias:

- 🔎 Filtro avançado por tags, autores e datas
- 📝 Editor rich text mais completo para criação de artigos
- 📊 Dashboard com métricas de acesso/leitura
- 🌐 Suporte a multilíngue (PT/EN)
- 📱 PWA para acesso offline em dispositivos móveis

---

## 🤝 Contribuição

Contribuições são muito bem-vindas!  

1. Faça um **fork** do repositório  
2. Crie um branch para sua feature: `git checkout -b feature/minha-feature`  
3. Commit suas mudanças: `git commit -m "feat: minha nova feature"`  
4. Faça push do branch: `git push origin feature/minha-feature`  
5. Abra um **Pull Request**

---

## 📄 Licença

Defina aqui a licença desejada (MIT, Apache 2.0 etc.).  
Exemplo:

> Este projeto é distribuído sob a licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.

---

## 🙋 Sobre o projeto

TheClub foi desenvolvido como parte de atividades de **Engenharia de Software** e **Banco de Dados**, conectando teoria e prática em um cenário real de comunicação universitária. Ele também se integra a ações de extensão, visitas técnicas e eventos acadêmicos, aproximando estudantes, professores e comunidade por meio de tecnologia e jornalismo digital.  


