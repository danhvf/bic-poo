package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorClientesFuncionalTest {


    @Test
    @DisplayName("Funcional: Cadastro de Pessoa Física com Sucesso (Caminho Feliz)")
    void funcional_CadastroPessoaFisica_Sucesso() {


        String[] dadosUsuarioReal = {
                "Maria da Silva",      // Nome simples e válido
                "maria@gmail.com",     // Email provedor comum
                "21999998888",         // Celular padrão
                "30",                  // Idade adulta
                "12345678901",         // CPF válido mockado
                "senhaForte123"        // Senha ok
        };

        assertDoesNotThrow(() ->
                        informacoesClientes(dadosUsuarioReal, TiposClientes.CLIENTE_PESSOA),
                "O sistema deve permitir o cadastro de um usuário comum com dados válidos."
        );
    }

    @Test
    @DisplayName("Segurança: Tentativa de SQL Injection no Nome")
    void seguranca_InjecaoSQL_Nome() {

        String payloadAttack = "Robert'); DROP TABLE Clientes;--";

        String[] dadosMaliciosos = {payloadAttack, "hacker@evil.com", "21999999999", "25", "12345678901", "123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dadosMaliciosos, TiposClientes.CLIENTE_PESSOA)
        );


        assertEquals("Por favor, o nome nao deve conter numeros ou caracteres invalidos. Tente novamente", erro.getMessage(),
                "O sistema funcionalmente deve bloquear tentativas de injeção de código no nome."
        );
    }

    @Test
    @DisplayName("Robustez: Uso de Emojis no Nome (Validação de Caracteres)")
    void robustez_NomeComEmojis() {

        String nomeComEmoji = "João 😎 Silva";

        String[] dadosEmoji = {nomeComEmoji, "joao@email.com", "21999999999", "25", "12345678901", "123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dadosEmoji, TiposClientes.CLIENTE_PESSOA)
        );

        assertTrue(erro.getMessage().contains("caracteres invalidos"),
                "O sistema deve rejeitar nomes que não sejam estritamente alfabéticos."
        );
    }

    @Test
    @DisplayName("Funcional: Regra de Idade Mínima para Empresas")
    void funcional_RegraNegocio_IdadeEmpresa() {


        String[] empresaNova = {"Startup Ltda", "contato@start.up", "21999999999", "1", "12345678901234", "123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(empresaNova, TiposClientes.CLIENTE_EMPRESA)
        );

        assertTrue(erro.getMessage().contains("Ter pelo menos 3 anos"),
                "O sistema deve bloquear empresas muito novas conforme regra de compliance."
        );
    }

    @Test
    @DisplayName("Segurança: Teste de Stress (Buffer Overflow) no Endereço")
    void seguranca_BufferOverflow_Endereco() {

        String cepGigante = "1".repeat(50000); // 50 mil caracteres
        String[] entradaMaluca = {cepGigante, "100"};


        boolean resultado = verificarEndereco(entradaMaluca);

        assertFalse(resultado, "O sistema deve rejeitar graciosamente um CEP gigante sem travar.");
    }

    @Test
    @DisplayName("UX: Validação de Campos em Branco (Prevenção de Erro)")
    void funcional_CamposObrigatorios() {

        String[] dadosIncompletos = {"Nome", "email@teste.com", "", "25", "12345678901", "123"};

        ValorInvalido erro = assertThrows(ValorInvalido.class, () ->
                informacoesClientes(dadosIncompletos, TiposClientes.CLIENTE_PESSOA)
        );

        assertEquals("Nenhum dos campos podem ser vazios. Tente novamente", erro.getMessage(),
                "O sistema deve orientar o usuário a preencher todos os campos."
        );
    }
}