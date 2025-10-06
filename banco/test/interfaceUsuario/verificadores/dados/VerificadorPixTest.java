package interfaceUsuario.verificadores.dados;

import interfaceUsuario.dados.DadosChavesPix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import utilsBank.GeracaoAleatoria;

import java.util.Scanner;

import static interfaceUsuario.menus.MenuUsuario.TECLADO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificadorPixTest {

    // Mock para simular a entrada do usuário via teclado, isolando o teste da console.
    @Mock
    private Scanner mockTeclado;

    // --- CENÁRIOS 1.1: Testes para o método tipoChavePix ---

    @Test
    @DisplayName("Cenário 1.1.1: Deve retornar true para tipo de chave válido (EMAIL)")
    void tipoChavePix_quandoEntradaValidaEmail_deveRetornarTrue() {
        // Ação
        boolean resultado = VerificadorPix.tipoChavePix(DadosChavesPix.EMAIL);
        // Verificação
        assertTrue(resultado, "Deveria retornar true para um tipo de chave PIX válido.");
    }

    @Test
    @DisplayName("Cenário 1.1.2: Deve retornar true para tipo de chave válido (TELEFONE)")
    void tipoChavePix_quandoEntradaValidaTelefone_deveRetornarTrue() {
        // Ação
        boolean resultado = VerificadorPix.tipoChavePix(DadosChavesPix.TELEFONE);
        // Verificação
        assertTrue(resultado, "Deveria retornar true para um tipo de chave PIX válido.");
    }

    @Test
    @DisplayName("Cenário 1.1.3: Deve retornar false para tipo de chave inválido")
    void tipoChavePix_quandoEntradaInvalida_deveRetornarFalse() {
        // Ação
        boolean resultado = VerificadorPix.tipoChavePix("TIPO_INVALIDO");
        // Verificação
        assertFalse(resultado, "Deveria retornar false para um tipo de chave PIX inválido.");
    }

    @Test
    @DisplayName("Cenário 1.1.4: Deve retornar false para entrada nula")
    void tipoChavePix_quandoEntradaNula_deveRetornarFalse() {
        // Ação
        boolean resultado = VerificadorPix.tipoChavePix(null);
        // Verificação
        assertFalse(resultado, "Deveria retornar false para uma entrada nula.");
    }

    // --- CENÁRIOS 1.2: Testes para o método chavePix (Interação do Usuário) ---

    @Test
    @DisplayName("Cenário 1.2.1: Deve retornar true (para tentar de novo) se o usuário digitar '0'")
    void chavePix_quandoUsuarioQuerTrocarChave_deveRetornarTrue() {
        // Configuração
        // Substituímos o Scanner estático real pelo nosso mock.
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("0");

        // Ação
        boolean resultado = VerificadorPix.chavePix("qualquer.coisa@email.com", DadosChavesPix.EMAIL);

        // Verificação
        assertTrue(resultado, "Deveria retornar true se o usuário indica que quer trocar a chave.");
    }


    // --- CENÁRIOS 1.3: Testes para o método chavePix (Lógica de Validação) ---
    // Nestes testes, simulamos que o usuário confirmou a chave (digitou algo diferente de "0").

    @Test
    @DisplayName("Cenário 1.3.1: Validação EMAIL - Deve retornar false para email válido")
    void chavePix_quandoEmailValidoEUsuarioConfirma_deveRetornarFalse() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1"); // Usuário confirma

        // Usamos mockStatic para controlar o resultado do método estático VerificadorClientes.verificarEmail
        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            mocked.when(() -> VerificadorClientes.verificarEmail("valido@email.com")).thenReturn(true);

            // Ação
            boolean resultado = VerificadorPix.chavePix("valido@email.com", DadosChavesPix.EMAIL);

            // Verificação
            assertFalse(resultado, "Deveria retornar false, pois a chave é válida e o método retorna !valido.");
        }
    }

    @Test
    @DisplayName("Cenário 1.3.2: Validação EMAIL - Deve retornar true para email inválido")
    void chavePix_quandoEmailInvalidoEUsuarioConfirma_deveRetornarTrue() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1"); // Usuário confirma

        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            mocked.when(() -> VerificadorClientes.verificarEmail("invalido")).thenReturn(false);

            // Ação
            boolean resultado = VerificadorPix.chavePix("invalido", DadosChavesPix.EMAIL);

            // Verificação
            assertTrue(resultado, "Deveria retornar true, pois a chave é inválida e o método retorna !invalido.");
        }
    }

    @Test
    @DisplayName("Cenário 1.3.3: Validação TELEFONE - Deve retornar false para telefone válido")
    void chavePix_quandoTelefoneValidoEUsuarioConfirma_deveRetornarFalse() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            mocked.when(() -> VerificadorClientes.verificarTelefone("11987654321")).thenReturn(true);
            // Ação
            boolean resultado = VerificadorPix.chavePix("11987654321", DadosChavesPix.TELEFONE);
            // Verificação
            assertFalse(resultado);
        }
    }

    @Test
    @DisplayName("Cenário 1.3.4: Validação CHAVE_ALEATORIA - Deve retornar false para chave com tamanho correto")
    void chavePix_quandoChaveAleatoriaValidaEUsuarioConfirma_deveRetornarFalse() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Geramos uma chave com o tamanho esperado para simular uma entrada válida
        String chaveValida = "a".repeat(GeracaoAleatoria.TAMANHO_CHAVE_ALEATORIA);

        // Ação
        boolean resultado = VerificadorPix.chavePix(chaveValida, DadosChavesPix.CHAVE_ALEATORIA);
        // Verificação
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Cenário 1.3.5: Validação CHAVE_ALEATORIA - Deve retornar true para chave com tamanho incorreto")
    void chavePix_quandoChaveAleatoriaInvalidaEUsuarioConfirma_deveRetornarTrue() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");
        String chaveInvalida = "tamanho-errado";

        // Ação
        boolean resultado = VerificadorPix.chavePix(chaveInvalida, DadosChavesPix.CHAVE_ALEATORIA);
        // Verificação
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Cenário 1.3.6: Validação - Deve retornar true se o tipo de chave for desconhecido")
    void chavePix_quandoTipoDeChaveDesconhecido_deveRetornarTrue() {
        // Configuração
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Ação
        boolean resultado = VerificadorPix.chavePix("qualquer-coisa", "TIPO_INEXISTENTE");
        // Verificação
        assertTrue(resultado, "Deveria retornar true se o tipo de chave não corresponder a nenhum caso do switch.");
    }
}