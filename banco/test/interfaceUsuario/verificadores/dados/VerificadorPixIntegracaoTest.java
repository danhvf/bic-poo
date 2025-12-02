package interfaceUsuario.verificadores.dados;

import interfaceUsuario.dados.DadosChavesPix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Scanner;

import static interfaceUsuario.menus.MenuUsuario.TECLADO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificadorPixIntegracaoTest {

    @Mock
    private Scanner mockTeclado;

    @Test
    @DisplayName("Integração EMAIL: Deve aceitar emails válidos baseados na Regex de VerificadorEntrada")
    void integracao_ValidacaoEmail_Sucesso() {
        // Configura input "1" (Confirmar)
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String emailReal = "joao.silva@banco.com";

        boolean resultado = VerificadorPix.chavePix(emailReal, DadosChavesPix.EMAIL);

        // Se resultado for false, significa que passou
        assertFalse(resultado, "A Regex real deveria aceitar um email padrão.");
    }

    @Test
    @DisplayName("Integração EMAIL: Deve rejeitar email sem domínio (falha na Regex real)")
    void integracao_ValidacaoEmail_Falha() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String emailRuim = "joao.silva@"; // Inválido pela Regex

        boolean resultado = VerificadorPix.chavePix(emailRuim, DadosChavesPix.EMAIL);

        // Se resultado for true, significa que o sistema detectou o erro e pediu para trocar
        assertTrue(resultado, "A Regex real deveria rejeitar email incompleto.");
    }

    @Test
    @DisplayName("Integração TELEFONE: Deve respeitar o limite de 12 dígitos de VerificadorEntrada")
    void integracao_ValidacaoTelefone_Limite() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // VerificadorEntrada.DIGITOS_MAXIMO_TELEFONE é 12.

        // Teste 1: 12 Dígitos
        String telLimite = "123456789012";
        assertFalse(VerificadorPix.chavePix(telLimite, DadosChavesPix.TELEFONE),
                "12 dígitos deve ser aceito.");

        // Reinicia Mock para próxima leitura
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Teste 2: 13 Dígitos
        String telEstourado = "1234567890123";
        assertTrue(VerificadorPix.chavePix(telEstourado, DadosChavesPix.TELEFONE),
                "13 dígitos deve ser rejeitado (limite é 12).");
    }

    @Test
    @DisplayName("Integração ARRAY: Verifica se 'identificacao' existe em ENTRADAS_CHAVE_PIX")
    void integracao_Bug_ArrayFaltandoIdentificacao() {

        boolean existe = VerificadorPix.tipoChavePix(DadosChavesPix.IDENTIFICACAO);

        assertTrue(existe, "ERRO DE INTEGRAÇÃO: O tipo 'identificacao' não foi cadastrado no array ENTRADAS_CHAVE_PIX em VerificadorEntrada.");
    }

    @Test
    @DisplayName("Integração CRÍTICA: Validação de CPF aceita texto como válido?")
    void integracao_Bug_LogicaIdentificacao() {

        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String cpfFalso = "batata"; // Claramente não é um CPF

        boolean resultado = VerificadorPix.chavePix(cpfFalso, DadosChavesPix.IDENTIFICACAO);

        assertTrue(resultado, "ERRO CRÍTICO: O sistema aceitou texto não-numérico como CPF válido devido a lógica invertida no try-catch.");
    }

    @Test
    @DisplayName("Integração: CPF Real vs Integer Overflow")
    void integracao_Bug_IntegerOverflow() {
        // CPF tem 11 dígitos. Integer.parseInt suporta até ~2 bilhões (10 dígitos).
        // Um CPF real vai causar estouro de Integer, cair no catch, retornar true, e o Pix vai aceitar.
        // Funciona "por acidente", mas é tecnicamente um erro de codificação.

        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String cpfReal = "12345678901"; // 11 dígitos

        boolean resultado = VerificadorPix.chavePix(cpfReal, DadosChavesPix.IDENTIFICACAO);

        // Deve ser false (Válido).
        assertFalse(resultado);
    }
}