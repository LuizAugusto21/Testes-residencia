# 🛒 Testes Automatizados em um E-commerce

## 📄 Descrição

Este projeto foi desenvolvido como parte do trabalho para a disciplina **Gerência de Configuração e Testes de Software** ministrada pelo Professor Eiji Adachi. O objetivo é implementar testes automatizados para a funcionalidade de finalização de compra em uma API REST de um e-commerce. A funcionalidade testada inclui:

- Verificação de estoque
- Cálculo do preço total
- Autorização de pagamento
- Atualização de estoque

## 👥 Componentes do Grupo

- Luiz Augusto
- Yan Tavares
- Mariele Oliveira

## 🛠️ Estrutura do Projeto

O projeto segue uma arquitetura organizada em três camadas principais:

- **Controller**: Responsável por lidar com as requisições HTTP.
- **Service**: Contém a lógica de negócio.
- **Repository**: Responsável pela interação com o banco de dados.

## 🧩 Funcionalidade Testada

A funcionalidade de finalização de compra realiza as seguintes operações:

1.  **Consulta ao serviço de estoque**: Verifica se há quantidade suficiente de cada produto.
2.  **Cálculo do preço total**: Aplica descontos e calcula o frete.
3.  **Verificação de pagamento**: Consulta o serviço de pagamentos.
4.  **Atualização do estoque**: Dá baixa no estoque após o pagamento autorizado.

## 📏 Regras de Negócio

### 🚚 **Frete**:
- Até **5 kg**: Frete **gratuito**.
- **5 a 10 kg**: R$ 2,00 por kg.
- **10 a 50 kg**: R$ 4,00 por kg.
- **Acima de 50 kg**: R$ 7,00 por kg.
- Clientes **Ouro**: Frete **gratuito**.
- Clientes **Prata**: **50% de desconto** no frete.
- Clientes **Bronze**: Pagam frete **integral**.

### 🏷️ **Descontos**:
- Compras acima de **R$ 500,00**: **10% de desconto** nos itens (excluindo o frete).
- Compras acima de **R$ 1.000,00**: **20% de desconto** nos itens (excluindo o frete).

# 🚀 Como Executar o Projeto

1. **Clonar o Repositório**:
   - Use o comando abaixo no terminal para clonar o repositório:
     ```bash
     git clone https://github.com/LuizAugusto21/Testes-residencia.git
     ```

2. **Abrir na IDE**:
   - Abra a IDE de sua escolha (IntelliJ IDEA, Eclipse).
   - Selecione a opção para abrir um projeto e navegue até a pasta clonada.


## ✅ Como Executar os Testes

- Após abrir o projeto, navegue até a seguinte pasta:
     ```
     src/test/java
     ```
 - Nesta pasta, você encontrará todos os testes implementados para o projeto, organizados em pacotes que correspondem à estrutura do código principal.



