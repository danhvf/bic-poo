# Banco BIC: Banco do Instituto de Computação.

**Repositório para o trabalho da disciplina Qualidade e Teste**

# Membros do Grupo
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
- [Apresentação Canva](https://www.canva.com/design/DAEjR5exvtY/OltrLCdcLsvudI5XJOcMng/edit)
- [Plano de Teste](https://github.com/danhvf/bic-poo/blob/main/Relat%C3%B3rio%20de%20Plano%20de%20Testes%20-%20Testlink.pdf)

# Versão do Java 

Utilizamos a [JDK 18](https://jdk.java.net/) para a realização desse trabalho. Deve ser utilizada essa versão.

# Como instalar e executar o BIC
```
git clone https://github.com/Asunnya/bic-poo
cd exec/jar
java -jar bic-poo.jar
```
## Possíveis erros na execução do .jar
Caso você tenha problema ao rodar o .jar, listamos alguns erros conhecidos
```
Error: LinkageError occurred while loading main class Main
        java.lang.UnsupportedClassVersionError: Main has been compiled by a more recent version of the Java Runtime (class file version 62.0), this version of the Java Runtime only recognizes class file versions up to 61.0
```
Caso você tenha o problema acima, utilize essas versões para rodar o -jar.

```
openjdk 18 2022-03-22
OpenJDK Runtime Environment (build 18+36-2087)
OpenJDK 64-Bit Server VM (build 18+36-2087, mixed mode, sharing)
```

# Lib e configurações para o projeto

Para a execução bem sucedida do projeto, crie uma pasta *lib/* na raiz do projeto, baixe os .jar abaixo e salve dentro da pasta lib:
- junit-jupiter-api-5.10.0.jar
- junit-jupiter-engine-5.10.0.jar
- mockito-core-5.11.0.jar
- byte-buddy-1.14.10.jar
- byte-buddy-agent-1.14.10.jar
- objenesis-3.3.jar

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

Após esses passos, o projeto bic-poo estará rodando com JUnit 5 (para testes unitários) e Mockito (para mocks e simulações).
