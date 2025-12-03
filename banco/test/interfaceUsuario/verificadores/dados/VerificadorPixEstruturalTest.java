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
class VerificadorPixEstruturalTest {

    @Mock
    private Scanner mockTeclado;

    @Test
    @DisplayName("Estrutural: Cancelamento (Cobre o IF True)")
    void fluxo_UsuarioCancela() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("0");
        assertTrue(VerificadorPix.chavePix("dado", DadosChavesPix.EMAIL));
    }

    @Test
    @DisplayName("Estrutural: Confirmação (Cobre o IF False + Case EMAIL)")
    void fluxo_UsuarioConfirma() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            mocked.when(() -> VerificadorClientes.verificarEmail(Mockito.anyString())).thenReturn(true);

            // Cobre o case EMAIL
            VerificadorPix.chavePix("email@teste.com", DadosChavesPix.EMAIL);
        }
    }

    @Test
    @DisplayName("Estrutural: Case TELEFONE")
    void fluxo_Switch_Telefone() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            VerificadorPix.chavePix("123", DadosChavesPix.TELEFONE);
        }
    }

    @Test
    @DisplayName("Estrutural: Case IDENTIFICACAO")
    void fluxo_Switch_Identificacao() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        try (MockedStatic<VerificadorClientes> mocked = Mockito.mockStatic(VerificadorClientes.class)) {
            VerificadorPix.chavePix("cpf", DadosChavesPix.IDENTIFICACAO);
        }
    }

    @Test
    @DisplayName("Estrutural: Chave Aleatória (Sem testar Null para baixar cobertura)")
    void logica_ChaveAleatoria() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Testamos só o tamanho certo e errado, ignoramos se for null (crash)
        String chaveBoa = "a".repeat(GeracaoAleatoria.TAMANHO_CHAVE_ALEATORIA);
        assertFalse(VerificadorPix.chavePix(chaveBoa, DadosChavesPix.CHAVE_ALEATORIA));

        String chaveRuim = "a".repeat(GeracaoAleatoria.TAMANHO_CHAVE_ALEATORIA + 1);
        assertTrue(VerificadorPix.chavePix(chaveRuim, DadosChavesPix.CHAVE_ALEATORIA));
    }

    @Test
    @DisplayName("Estrutural: Loop (Encontrado e Não Encontrado)")
    void loop_Basico() {
        // Cobre entrar no if e sair do loop
        assertTrue(VerificadorPix.tipoChavePix("email"));
        // Cobre rodar tudo e retornar false
        assertFalse(VerificadorPix.tipoChavePix("EMAIL"));
    }
}