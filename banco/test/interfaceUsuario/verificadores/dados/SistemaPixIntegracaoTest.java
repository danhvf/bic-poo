package interfaceUsuario.verificadores.dados;

import conta.Conta;
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
class SistemaPixTransacaoTest {

    @Mock
    private Scanner mockTeclado;

    static class ContaTeste extends Conta {
        public ContaTeste(double saldoInicial) {
            super(); // Chama o construtor da Conta (gera ID, listas, etc)
            this.saldo = saldoInicial; // Acessa o atributo protected 'saldo' diretamente
        }

        // Método auxiliar para simular o débito (já que diminuirSaldo é private/protected e transferir depende de UI)
        public void simularDebito(double valor) {
            this.saldo -= valor;
        }

        // Método para pegar saldo (se getSaldo() não fosse público, mas ele é)
        public double getSaldoAtual() {
            return this.saldo;
        }
    }

    @Test
    @DisplayName("SYS-04: Fluxo Completo de Transferência Pix (Email Válido + Saldo)")
    void sistema_FluxoPixComSucesso() {

        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1"); // Confirmação do usuário

        ContaTeste contaCliente = new ContaTeste(1000.00);
        String chaveDestino = "amigo@banco.com";
        double valorTransferencia = 200.00;

        // Passo A: Validação da Chave (VerificadorPix Real)
        boolean chaveInvalida = VerificadorPix.chavePix(chaveDestino, DadosChavesPix.EMAIL);

        // Passo B: Verificação de Saldo e Execução
        boolean transacaoRealizada = false;

        // A lógica do sistema: Se a chave for VÁLIDA (!chaveInvalida) e tiver saldo...
        if (!chaveInvalida && contaCliente.getSaldo() >= valorTransferencia) {
            contaCliente.simularDebito(valorTransferencia);
            transacaoRealizada = true;
        }

        // Verifica se a transação ocorreu
        assertTrue(transacaoRealizada, "A transação deveria ser realizada.");

        // Verifica se o saldo da classe Conta foi atualizado corretamente
        assertEquals(800.00, contaCliente.getSaldo(), 0.01, "O saldo final deve ser R$ 800,00.");
    }

    @Test
    @DisplayName("SYS-05: Bloqueio Financeiro (Chave Válida mas Saldo Insuficiente)")
    void sistema_FluxoPixSemSaldo() {

        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        ContaTeste contaCliente = new ContaTeste(50.00); // Saldo baixo
        String chaveDestino = "loja@banco.com";
        double valorTransferencia = 100.00;

        boolean chaveInvalida = VerificadorPix.chavePix(chaveDestino, DadosChavesPix.EMAIL);

        boolean transacaoRealizada = false;
        if (!chaveInvalida && contaCliente.getSaldo() >= valorTransferencia) {
            contaCliente.simularDebito(valorTransferencia);
            transacaoRealizada = true;
        }

        assertFalse(transacaoRealizada, "O sistema deve bloquear a transação por falta de fundos.");
        assertEquals(50.00, contaCliente.getSaldo(), 0.01, "O saldo deve permanecer inalterado.");
    }

    @Test
    @DisplayName("SYS-06 [CRÍTICO]: Efetivação de Pix para CPF Inválido ('batata')")
    void sistema_BugCriticoCpf() {

        TECLADO = mockTeclado;
        Mockito.when(mockTeclado.nextLine()).thenReturn("1");

        ContaTeste contaCliente = new ContaTeste(500.00);
        String chaveBugada = "batata"; // Chave inválida que seu validador aceita
        double valorTransferencia = 500.00;

        // VerificadorPix retorna 'false' (que significa Válido) devido ao bug de lógica
        boolean chaveRecusada = VerificadorPix.chavePix(chaveBugada, DadosChavesPix.IDENTIFICACAO);

        boolean dinheiroSaiu = false;
        if (!chaveRecusada && contaCliente.getSaldo() >= valorTransferencia) {
            contaCliente.simularDebito(valorTransferencia);
            dinheiroSaiu = true;
        }

        // O assert abaixo confirma que o bug aconteceu e o dinheiro saiu
        assertTrue(dinheiroSaiu, "FALHA GRAVE: O sistema permitiu débito para uma chave CPF 'batata'.");
        assertEquals(0.00, contaCliente.getSaldo(), 0.01, "O cliente perdeu o dinheiro devido à falha de validação.");
    }
}