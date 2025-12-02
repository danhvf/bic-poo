package interfaceUsuario.verificadores.dados;

import interfaceUsuario.dados.DadosChavesPix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import utilsBank.GeracaoAleatoria;

import java.util.Scanner;

import static interfaceUsuario.menus.MenuUsuario.TECLADO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificadorPixFuncionalTest {

    @Mock
    private Scanner mockTeclado;

    @Test
    @DisplayName("Funcional: Fluxo de Cadastro de Email Válido")
    void funcional_CadastroEmailSucesso() {
        // 1. Configurar o ambiente (O usuário digita "1" para confirmar)
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // 2. O usuário insere os dados
        String entradaUsuario = "usuario@gmail.com";

        // 3. Executar a funcionalidade
        boolean precisaTentarNovamente = VerificadorPix.chavePix(entradaUsuario, DadosChavesPix.EMAIL);

        // 4. Validar o resultado esperado de negócio
        // Se retornar false, significa que DEU CERTO (não precisa tentar novamente).
        assertFalse(precisaTentarNovamente, "O sistema deveria aceitar um email válido e finalizar o cadastro.");
    }

    @Test
    @DisplayName("Funcional: Bloqueio de Telefone Inválido (Excedendo caracteres)")
    void funcional_BloqueioTelefoneInvalido() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String telefoneErrado = "1199999888877776666"; // Gigante

        boolean precisaTentarNovamente = VerificadorPix.chavePix(telefoneErrado, DadosChavesPix.TELEFONE);

        // Se retornar true, significa que o sistema BLOQUEOU e pediu para tentar de novo.
        assertTrue(precisaTentarNovamente, "O sistema deveria bloquear telefones com tamanho inválido.");
    }

    @Test
    @DisplayName("Funcional: Fluxo de Desistência/Correção pelo Usuário")
    void funcional_UsuarioDesiste() {
        TECLADO = mockTeclado;
        // O usuário digita "0" quando o sistema pergunta se está correto
        Mockito.when(mockTeclado.nextLine()).thenReturn("0");

        String qualquerCoisa = "dado_errado";

        boolean precisaTentarNovamente = VerificadorPix.chavePix(qualquerCoisa, DadosChavesPix.EMAIL);

        // Deve retornar true (para reiniciar o loop do menu)
        assertTrue(precisaTentarNovamente, "O sistema deve permitir que o usuário cancele a operação.");
    }

    @Test
    @DisplayName("Funcional: Validação de consistência de CPF (Deve falhar se aceitar texto)")
    void funcional_ValidacaoCPF() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1"); // Usuário confirma achando que vai colar

        String cpfFalso = "batata"; // Usuário mal intencionado ou confuso

        boolean precisaTentarNovamente = VerificadorPix.chavePix(cpfFalso, DadosChavesPix.IDENTIFICACAO);

        // O teste funcional espera que o sistema seja inteligente e diga:
        assertTrue(precisaTentarNovamente, "O sistema funcionalmente falhou ao aceitar 'batata' como CPF.");
    }

    @Test
    @DisplayName("Segurança: Tentativa de SQL Injection no Email")
    void seguranca_InjecaoSQL() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Payload comum de ataque
        String payloadMalicioso = "admin' --";

        // O sistema deve rejeitar (retornar true para tentar novamente)
        // ou validar como email inválido.
        boolean resultado = VerificadorPix.chavePix(payloadMalicioso, DadosChavesPix.EMAIL);

        assertTrue(resultado, "O sistema deve rejeitar caracteres de injeção SQL em campos de email.");
    }

    @Test
    @DisplayName("Segurança: Teste de Carga/Stress com String Gigante")
    void seguranca_BufferOverflow() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        // Gerar uma string de 10.000 caracteres
        String chaveGigante = "a".repeat(10000);

        assertDoesNotThrow(() -> {
            boolean resultado = VerificadorPix.chavePix(chaveGigante, DadosChavesPix.CHAVE_ALEATORIA);

            assertTrue(resultado, "Uma chave de 10k caracteres deve ser recusada, mas sem derrubar o sistema.");
        });
    }

    @Test
    @DisplayName("Robustez: Uso de Emojis e Caracteres Especiais")
    void robustez_CaracteresInvalidos() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String chaveEmoji = "😊👍@banco.com";

        boolean resultado = VerificadorPix.chavePix(chaveEmoji, DadosChavesPix.EMAIL);

        // A Regex de email provavelmente não aceita emojis, então deve retornar true (Inválido)
        assertTrue(resultado, "O sistema não deve aceitar emojis em campos formais como email.");
    }

    @Test
    @DisplayName("Robustez: Chave Vazia ou Apenas Espaços")
    void robustez_ChaveEmBranco() {
        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        String chaveVazia = "   "; // Só espaços

        boolean resultado = VerificadorPix.chavePix(chaveVazia, DadosChavesPix.CHAVE_ALEATORIA);

        assertTrue(resultado, "Chaves compostas apenas por espaços devem ser rejeitadas.");
    }
}
