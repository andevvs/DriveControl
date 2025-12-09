# 🚗 DriveControl

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![Status](https://img.shields.io/badge/Status-Concluído-success?style=for-the-badge)

> **Sistema corporativo para gestão de frota, controle de viagens e monitoramento de manutenções e uso de veículos por motoristas.**

O **DriveControl** é uma aplicação Java desenvolvida como projeto final da disciplina de Programação Orientada a Objetos (POO). O sistema simula o gerenciamento de uma frota empresarial, aplicando rigorosamente os quatro pilares da POO: **Encapsulamento, Herança, Abstração e Polimorfismo**.

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Aplicação dos Pilares de POO](#-aplicação-dos-pilares-de-poo)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Como Executar](#-como-executar)
- [Autores](#-autores)


---

## 📖 Sobre o Projeto

O sistema permite o controle total sobre os ativos de transporte de uma empresa, garantindo integridade de dados e facilidade na operação. Existem dois perfis de acesso distintos:

1.  **Administrador (Gestor):** Responsável pelo cadastro de veículos, gestão de motoristas, controle financeiro de manutenções e auditoria de viagens.
2.  **Motorista:** Responsável por registrar a retirada e devolução de veículos, alimentando o sistema com dados de quilometragem e disponibilidade.

---

## 🚀 Funcionalidades

### 👤 Administrador
* **CRUD de Motoristas:** Cadastro, edição e remoção de motoristas (com validação de CNH e Setor).
* **CRUD de Veículos:** Controle de frota com status (*Disponível, Em Uso, Manutenção, Indisponível*).
* **Gestão de Manutenção:** Agendamento, registro de custos (previsto e real), histórico de oficinas e atualização automática da data de última revisão.
* **Auditoria:** Visualização do histórico completo de uso por placa ou por motorista.
* **Relatório Polimórfico:** Listagem unificada de todos os usuários do sistema, exibindo detalhes específicos de cada cargo.

### 🚙 Motorista
* **Login:** Autenticação segura via username e senha.
* **Consulta de Frota:** Visualização em tempo real de veículos disponíveis.
* **Registro de Uso:** Início de viagem (Check-out) vinculando motorista ao veículo.
* **Devolução:** Fim de viagem (Check-in) com atualização automática da quilometragem do veículo.

---

## 🧠 Aplicação dos Pilares de POO

### 1. Encapsulamento (Proteção de Dados)
- Todos os atributos das classes de modelo (`Veiculo`, `Usuario`, etc.) são `private`.
- O acesso é estritamente via Getters e Setters públicos.
- **Regras de negócio nos Setters:** Ex: não permitir inserir uma quilometragem menor que a atual registrada.

### 2. Herança (Reutilização)
- **Classe Mãe:** `Usuario` (ID, nome, username, senha).
- **Classes Filhas:** `Administrador` (adiciona `cargo`) e `Motorista` (adiciona `cnh`, `setor`).
- Reaproveitamento total de código para métodos comuns como login e identificação.

### 3. Abstração (Simplificação)
- A classe `Usuario` é `abstract`, impedindo a criação de um usuário genérico.
- Métodos abstratos como `exibirMenuPrincipal()` e `getDetalhes()` obrigam as subclasses a implementarem seus próprios comportamentos.

### 4. Polimorfismo (Flexibilidade)
- **No Login:** O método de autenticação retorna um objeto do tipo genérico `Usuario`. O sistema chama `usuario.exibirMenuPrincipal()` e a JVM decide, em tempo de execução, se deve abrir o menu do Admin ou do Motorista.
- **Na Listagem:** Uma única lista `List<Usuario>` é percorrida, e cada objeto exibe seus dados específicos (Cargo ou CNH) sem a necessidade de `if/else` complexos.

---

## 🛠 Tecnologias Utilizadas

* **Linguagem:** Java (JDK 17+)
* **Persistência:** SQLite
* **Driver:** JDBC (`sqlite-jdbc`)
* **Padrões de Projeto:**
    * Singleton (Database Connection)
    * Repository Pattern (DAO)
    * Service Layer
    * Layered Architecture

---

## 📂 Estrutura do Projeto

O código está organizado seguindo o padrão de pacotes reverso (`br.com...`) e arquitetura em camadas:

```text
src
└── br
    └── com
        └── drivecontrol
            ├── app             # Interface com Usuário (Console/Menus/Main)
            │   └── Main.java
            │
            ├── database        # Infraestrutura (Singleton)
            |   └── DatabaseConnection.java 
            |
            ├── model           # Entidades do Domínio (Classes POO)
            │   ├── Administrador.java
            │   ├── Manutencao.java
            │   ├── Motorista.java
            │   ├── RegistroUso.java
            │   ├── StatusVeiculo.java
            │   ├── Usuario.java
            │   └── Veiculo.java
            |
            ├── repository      # Acesso a Dados (SQL/Persistência)
            │   ├── ManutencaoRepository.java
            │   ├── MotoristaRepository.java 
            │   ├── RegistroUsoRepository.java
            |   ├── UsuarioRepository.java
            |   └── VeiculoRepository.java
            │
            └── service         # Regras de Negócio e Validações
                ├── ManutencaoService.java
                ├── MotoristaService.java
                ├── RegistroUsoService.java
                ├── UsuarioService.java
                └── VeiculoService.java

```
---


## ▶️ Como Executar

### ✔️ Pré-requisitos
- Java JDK instalado  
- IDE (VS Code, IntelliJ, Eclipse) **ou** Terminal  



### 🚀 Executando via IDE (Recomendado)

1. Abra a pasta do projeto na sua IDE.  
2. Certifique-se de que as dependências do **Maven (Driver SQLite)** foram baixadas.  
3. Navegue até: src/br/com/DriveControl/app/Main.java
4. Clique em Run.



### 💻 Executando via Terminal
   **Passos:**

1.  Clone este repositório:
    ```bash
    cd Downloads/
    git clone https://github.com/andevvs/DriveControl.git
    cd DriveControl/
    ```
    
2.  Construa o projeto e crie o arquivo `.jar` executável:
    ```bash
    mvn install
    ```
    *Isso irá baixar todas as dependências (como o SQLite) e criar um "uber-jar" na pasta `target/`.*

4.  Execute o programa:
    ```bash
    java -jar target/drivecontrol.jar
    ```
    
5.  O programa iniciará no seu terminal. O banco de dados `drivecontrol.db` será criado automaticamente na raiz do projeto na primeira execução.

---
<br>

## 👨‍💻 Autores

<table>
  <tr>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/106102036?v=4" width="100"><br>
      <b>marcelohdev
         2023011357</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/150745935?v=4" width="100"><br>
      <b>andevvs
         2023022919</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/147336900?v=4" width="100"><br>
      <b>Jacksonrs
         2023011455</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/166414190?v=4" width="100"><br>
      <b>Ruanpabloband
          2023022946</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/209040049?v=4" width="100"><br>
      <b>Juanpablouf
         2024010213</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/166559737?v=4" width="100"><br>
      <b>FabioHenriqued
          2023022690</b>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/179767589?v=4" width="100"><br>
      <b>DaviFreita
         2024010350</b>
    </td>
  </tr>
</table>

<img width=100% src="https://capsule-render.vercel.app/api?type=blur&height=300&color=0b427a&section=footer&descAlign=100&descAlignY=78"/>


