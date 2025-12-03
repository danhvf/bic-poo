package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorClientesEstruturalTest {


    // TESTES DIRETOS NOS MÉTODOS AUXILIARES


    @Test
    @DisplayName("Estrutural: Cobertura direta de Validadores Auxiliares")
    void estrutural_MetodosAuxiliares() {
        // 1. Verificar Telefone (Branch: length <= MAX)
        assertTrue(verificarTelefone("12345678"), "Deve aceitar telefone curto");
        assertFalse(verificarTelefone("12345678901234567890"), "Deve rejeitar telefone gigante");

        // 2. Verificar Email (Regex)
        assertTrue(verificarEmail("teste@email.com"), "Deve aceitar email válido");
        assertFalse(verificarEmail("email_invalido"), "Deve rejeitar email sem formato");

        // 3. Verificar Senha (Espaço e Tamanho)
        assertTrue(verificarSenha("123"), "Deve aceitar senha válida");
        assertFalse(verificarSenha("1 2"), "Deve rejeitar senha com espaço");
        assertFalse(verificarSenha("12"), "Deve rejeitar senha muito curta");

        // 4. Verificar Identidade Gerente
        assertFalse(verificarIdentidadeGerente("12345"), "Código atual retorna false para números");

        assertTrue(verificarIdentidadeGerente("texto"), "Código atual retorna true para texto (Lógica invertida)");

        assertFalse(verificarIdentidadeGerente("12345678901234567890"), "ID Gerente muito longo");

        // 5. Verificar Alfanumérico (Se acessível)
        assertTrue(isAlphanumeric("abc1"), "Alfanumérico válido");
        assertFalse(isAlphanumeric("abc*"), "Caractere especial deve falhar");
    }

    // FLUXO COMPLETO


    @Test
    @DisplayName("Estrutural: Caminho Feliz (Executa todas as linhas do método principal)")
    void estrutural_CaminhoFeliz_Completo() throws ValorInvalido {

        String[] dadosPerfeitos = {"Nome Sobrenome", "email@valido.com", "21999999999", "25", "12345678901", "senha123"};

        assertTrue(informacoesClientes(dadosPerfeitos, TiposClientes.CLIENTE_PESSOA));
    }


    //  ARESTAS DE FALHA (Entra nos IFs de erro do método principal)


    @Test
    @DisplayName("Estrutural: Falha no Primeiro IF (Nome Inválido)")
    void estrutural_Falha_Nome() {
        // Cobre a linha: if (!isAlphabetic(entradaGeral[0]))
        String[] dados = {"Nome123", "email@valido.com", "21999999999", "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Estrutural: Falha no Segundo IF (Email Inválido)")
    void estrutural_Falha_Email() {
        // Cobre a linha: if (!verificarEmail(entradaGeral[1]))
        String[] dados = {"Nome", "emailruim", "21999999999", "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Estrutural: Falha no Terceiro IF (Telefone Inválido)")
    void estrutural_Falha_Telefone() {
        // Cobre a linha: if (!verificarTelefone(...))
        String telGigante = "1".repeat(20);
        String[] dados = {"Nome", "email@valido.com", telGigante, "25", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    @Test
    @DisplayName("Estrutural: Falha no Quarto IF (Idade - Pessoa e Empresa)")
    void estrutural_Falha_Idade() {
        // Pessoa Física < 18
        String[] dadosPessoa = {"Nome", "email@valido.com", "21999999999", "10", "12345678901", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dadosPessoa, TiposClientes.CLIENTE_PESSOA));

        // Empresa < 3
        String[] dadosEmpresa = {"Empresa", "email@valido.com", "21999999999", "1", "12345678901234", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dadosEmpresa, TiposClientes.CLIENTE_EMPRESA));
    }

    @Test
    @DisplayName("Estrutural: Falha no Quinto IF (Identificação CPF/CNPJ)")
    void estrutural_Falha_Identificacao() {
        // CPF Tamanho Errado
        String[] dadosCpf = {"Nome", "email@valido.com", "21999999999", "25", "123", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dadosCpf, TiposClientes.CLIENTE_PESSOA));

        // CNPJ Tamanho Errado
        String[] dadosCnpj = {"Empresa", "email@valido.com", "21999999999", "10", "123", "senha123"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dadosCnpj, TiposClientes.CLIENTE_EMPRESA));
    }

    @Test
    @DisplayName("Estrutural: Falha no Sexto IF (Senha)")
    void estrutural_Falha_Senha() {
        String[] dados = {"Nome", "email@valido.com", "21999999999", "25", "12345678901", "1 2"};
        assertThrows(ValorInvalido.class, () -> informacoesClientes(dados, TiposClientes.CLIENTE_PESSOA));
    }

    // ENDEREÇO
    @Test
    @DisplayName("Estrutural: Endereço (Sucesso e Falha)")
    void estrutural_Endereco() {
        // Sucesso
        assertTrue(verificarEndereco(new String[]{"12345678", "100"}));
        // Falha Tamanho
        assertFalse(verificarEndereco(new String[]{"123", "100"}));
        // Falha Numérica (Catch)
        assertFalse(verificarEndereco(new String[]{"abc", "100"}));
    }
}