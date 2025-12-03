package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorClientesIntegracaoTest {

    @Test
    @DisplayName("Integração REGEX: Valida se a Regex Global de Email está sendo aplicada")
    void integracao_ValidacaoEmail_RegexGlobal() {
        String[] dadosEmailRuim = {"Nome Teste", "emailsemarroba.com", "21999999999", "25", "12345678901", "senha123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dadosEmailRuim, TiposClientes.CLIENTE_PESSOA)
        );
        assertEquals("Por favor, Insira um email valido", erro.getMessage());
    }

    @Test
    @DisplayName("Integração TELEFONE: Deve respeitar o limite global de dígitos")
    void integracao_ValidacaoTelefone_LimiteGlobal() {
        String telefoneGigante = "1234567890123456789";
        String[] dados = {"Nome Teste", "teste@email.com", telefoneGigante, "25", "12345678901", "senha123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA)
        );
        assertEquals("Telefone Invalido. Tente novamente", erro.getMessage());
    }

    @Test
    @DisplayName("Integração LÓGICA: Validação Cruzada (Empresa x CPF)")
    void integracao_LogicaTipoCliente_Incoerencia() {
        String[] dadosIncoerentes = {"Empresa X", "empresa@email.com", "21999999999", "5", "12345678901", "senha123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dadosIncoerentes, TiposClientes.CLIENTE_EMPRESA)
        );
        assertTrue(erro.getMessage().contains("CNPJ invalida"));
    }

    @Test
    @DisplayName("Integração CRÍTICA: CPF com letras (Bug Conhecido: Sistema aceita)")
    void integracao_Bug_CpfComLetras() {


        String cpfComLetra = "1234567890a";
        String[] dados = {"Nome", "teste@email.com", "21999999999", "25", cpfComLetra, "senha123"};

        assertDoesNotThrow(() ->
                        informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA),
                "ATENÇÃO: O teste passou confirmando que o sistema ACEITA CPFs inválidos (Bug)."
        );
    }

    @Test
    @DisplayName("Integração SUCESSO: Fluxo completo")
    void integracao_FluxoCompleto_Sucesso() {
        String[] dadosPerfeitos = {"Nome Correto", "email@valido.com", "21999999999", "25", "12345678901", "senha123"};

        assertDoesNotThrow(() ->
                informacoesClientes(dadosPerfeitos, TiposClientes.CLIENTE_PESSOA)
        );
    }
}