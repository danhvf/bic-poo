# Banco BIC: Banco do Instituto de Computação.

**Repositório para o trabalho da disciplina Qualidade e Teste.**

# Integrantes do Grupo
- Daniel Fontoura
- Daniele Pimenta
- Ihrael Chagas
- João Gabriel Otogali

# O trabalho

Nosso trabalho consiste em projetar casos de testes unitários de pelo menos uma classe, cuja complexidade ciclomática é acima de 10. 

Para este trabalho, utilizamos como referência o repositório [BIC-POO](https://github.com/Asunnya/bic-poo) e trabalhamos nas seguintes classes:
- Daniel Fontoura: [VerificadorTransacao.java](banco/src/interfaceUsuario/verificadores/dados/VerificadorTransacao.java) 
- Daniele Pimenta: [Conta.java](banco/src/conta/Conta.java)
- Irhael Chagas: [VerificadorClientes.java](banco/src/interfaceUsuario/verificadores/dados/VerificadorClientes.java) 
- João Gabriel Otogali: [VerificadorPix.java](banco/src/interfaceUsuario/verificadores/dados/VerificadorPix.java) 

Classes Testes implementadas para o trabalho:
- Daniel Fontoura: [VerificadorTransacaoTest.java](banco/test/interfaceUsuario/verificadores/dados/VerificadorTransacaoTest.java)
- Daniele Pimenta: [ContaTest.java](banco/test/src/conta/ContaTest.java)
- Irhael Chagas: [VerificadorClientesTest.java](banco/test/interfaceUsuario/verificadores/dados/VerificadorClientesTest.java)
- João Gabriel Otogali: [VerificadorPixTest.java](banco/test/interfaceUsuario/verificadores/dados/VerificadorPixTest.java)

# Ferramentas utilizadas para o trabalho
- [IDE InteliJJ](https://www.jetbrains.com/idea/)
- JUnit 5
- Mockito
- [TestLink](http://vania.ic.uff.br/testlink/index.php) - Projeto de teste: BIC: Testes do BIC-POO 
- [Apresentação Canva](https://www.canva.com/design/DAEjR5exvtY/obrGM-nTaEFjgt6qsD5TBA/view?utm_content=DAEjR5exvtY&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=h3837c19ef7)
- [Plano de Teste](https://github.com/danhvf/bic-poo/blob/main/Relat%C3%B3rio%20da%20Execu%C3%A7%C3%A3o%20do%20Plano%20de%20Testes.pdf)
- [Relatório de Entrega 1](https://docs.google.com/document/d/14gag5CEUvWrJpq426JLpU_opf_MbEO9E4zlias3nC8w](https://docs.google.com/document/d/14gag5CEUvWrJpq426JLpU_opf_MbEO9E4zlias3nC8w/edit?usp=sharing )
- Relatório de Entrega 2 - PENDENTE

# Versão do Java 

Utilizamos a [JDK 18](https://jdk.java.net/) para a realização desse trabalho. Deve ser utilizada essa versão.

# Como Executar o BIC

Dentro do IntelliJ Idea clonar o seguinte repositório: https://github.com/danhvf/bic-poo

# Lib e configurações para o projeto

Para a execução bem sucedida do projeto, crie uma pasta *lib/* na raiz do projeto, baixe os .jar abaixo e salve dentro da pasta lib:
- junit-jupiter:5.8.1.jar
- junit-jupiter-engine-5.10.0.jar
- mockito-core-5.11.0.jar
- mockito-junit-jupiter:5.11.0
- byte-buddy-1.14.10.jar
- byte-buddy-agent-1.14.10.jar
- objenesis-3.3.jar
- system-lambda-1.2.1.jar (Maven: com.github.stefanbirkner:system-lambda:1.2.1)

Em seguida, no IntelliJ IDEA vá em:
* Menu File → Project Structure → Modules → Dependencies → + → JARs or directories...
* Selecione todos os .jar da pasta lib, clique em Apply e depois dê OK.

# Estrutura final da lib/

```
bic-poo/
 ├── src/                 (código-fonte do sistema)         
 ├── test/                (código dos testes)
 ├── lib/                 (bibliotecas externas)
 │    ├── junit-jupiter-api-5.10.0.jar
 │    ├── junit-jupiter-engine-5.10.0.jar
 │    ├── mockito-core-5.11.0.jar
 │    ├── byte-buddy-1.14.10.jar
 │    ├── byte-buddy-agent-1.14.10.jar
 │    └── objenesis-3.3.jar
 └── (demais pastas e arquivos do projeto)
```

Após esses passos, o projeto Banco BIC estará rodando com JUnit 5 e Mockito.
