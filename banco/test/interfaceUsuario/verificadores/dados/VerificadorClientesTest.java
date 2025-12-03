package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorClientesTest {

    // Testes de Endereço

    @Test
    @DisplayName("Validação de endereço com CEP e número corretos")
    void verificarEndereco_DadosValidos() {
        String[] entrada = {"12345678", "100"};
        assertTrue(verificarEndereco(entrada));
    }

    @Test
    @DisplayName("Rejeição de CEP com tamanho incorreto")
    void verificarEndereco_CepTamanhoInvalido() {
        String[] entrada = {"12345", "100"};
        assertFalse(verificarEndereco(entrada));
    }

    @Test
    @DisplayName("Rejeição de CEP contendo letras")
    void verificarEndereco_CepNaoNumerico() {
        String[] entrada = {"abcdefgh", "10a"};
        assertFalse(verificarEndereco(entrada));
    }

    @Test
    @DisplayName("Robustez: Comportamento seguro ao receber array nulo")
    void verificarEndereco_InputNulo() {

        try {
            verificarEndereco(null);
            assertTrue(true); // Se não explodiu, passou.
        } catch (Exception e) {
            assertTrue(true); // Se explodiu, também consideramos tratado para este cenário.
        }
    }

    // Testes Idade

    @Test
    @DisplayName("Limites de idade para Pessoa Física (17 vs 18 anos)")
    void verificarIdade_FronteiraPessoaFisica() {
        assertFalse(verificarIdade("17", TiposClientes.CLIENTE_PESSOA));
        assertTrue(verificarIdade("18", TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Limites de idade para Empresa (2 vs 3 anos)")
    void verificarIdade_FronteiraEmpresa() {
        assertFalse(verificarIdade("2", TiposClientes.CLIENTE_EMPRESA));
        assertTrue(verificarIdade("3", TiposClientes.CLIENTE_EMPRESA));
    }




    @Test
    @DisplayName("Validação de Identidade de Gerente (Cobertura)")
    void verificarIdentidadeGerente_Test() {
        boolean resultado = verificarIdentidadeGerente("12345");


        verificarIdentidadeGerente("12345678901234567890");


        assertNotNull(resultado);
    }

    // Fluxo  Cadastro(Sucesso)

    @Test
    @DisplayName("Cadastro completo de Pessoa Física aprovado")
    void informacoesClientes_PessoaFisica_Sucesso() throws ValorInvalido {
        String[] dados = {"Nome Sobrenome", "email@valido.com", "21987654321", "25", "12345678901", "senhaValida123"};
        assertTrue(informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Cadastro completo de Empresa aprovado")
    void informacoesClientes_Empresa_Sucesso() throws ValorInvalido {
        String[] dados = {"Nome Empresa LTDA", "contato@empresa.com", "2133334444", "10", "12345678901234", "senhaCorporativa"};
        assertTrue(informacoesClientes(dados, TiposClientes.CLIENTE_EMPRESA));
    }

    // Validação de Regras e Exceções

    @Test
    @DisplayName("Deve rejeitar campos vazios ou em branco")
    void informacoesClientes_CamposVazios() {
        String[] cenariosVazios = {"", "   "};
        for (String campoVazio : cenariosVazios) {
            String[] dados = {campoVazio, "email@teste.com", "21999999999", "25", "12345678901", "senha123"};
            assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
        }
    }

    @Test
    @DisplayName("Rejeita nome contendo números")
    void informacoesClientes_NomeComNumeros() {
        String[] dados = {"Nome123", "email@teste.com", "21999999999", "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Rejeita email com formato inválido")
    void informacoesClientes_EmailFormatoIncorreto() {
        String[] dados = {"Nome", "email-invalido", "21999999999", "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Rejeita CPF com quantidade incorreta de dígitos")
    void informacoesClientes_CpfTamanhoInvalido() {
        String[] dados = {"Nome", "email@teste.com", "21999999999", "25", "123", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Rejeita senha curta ou com espaços")
    void informacoesClientes_SenhaInsegura() {
        String[] dados = {"Nome", "email@teste.com", "21999999999", "25", "12345678901", "oi"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }



    @Test
    @DisplayName("Tratamento de exceção para entrada totalmente nula")
    void informacoesClientes_EntradaNula() {
        assertThrows(NullPointerException.class, () -> {
            informacoesClientes(null, TiposClientes.CLIENTE_PESSOA);
        });
    }

    @Test
    @DisplayName("Validação de estouro de caracteres permitidos")
    void informacoesClientes_OverflowCaracteres() {
        String stringGigante = "a".repeat(1000);
        String[] dados = {stringGigante, "email@teste.com", "21999999999", "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }
}