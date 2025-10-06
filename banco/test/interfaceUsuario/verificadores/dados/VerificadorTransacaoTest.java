package interfaceUsuario.verificadores.dados;

import cliente.Cliente;
import conta.Conta;
import conta.ContaStandard;
import interfaceUsuario.InterfaceUsuario;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static interfaceUsuario.menus.MenuUsuario.DEPOSITO;
import static interfaceUsuario.menus.MenuUsuario.TRANSFERENCIA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificadorTransacaoTest {

    @Mock
    private Cliente mockCliente;
    @Mock
    private Conta mockConta;

    @BeforeEach
    void setup() {

        when(mockCliente.getConta()).thenReturn(mockConta);

        InterfaceUsuario.setClienteAtual(mockCliente);
    }

    // --- CENÁRIOS 1.1: Validação de Formato ---

    @Test
    @DisplayName("Cenário 1.1.1: Deve lançar exceção para entrada não numérica")
    void dadosTransacao_quandoEntradaNaoNumerica_deveLancarNumberFormatException() {
        // Ação e Verificação
        assertThrows(NumberFormatException.class, () -> {
            VerificadorTransacao.dadosTransacao("abc", DEPOSITO, VerificadorEntrada.STANDARD);
        }, "Deveria lançar NumberFormatException para entrada inválida.");
    }

    @Test
    @DisplayName("Cenário 1.1.2: Deve lançar ValorInvalido para entrada negativa")
    void dadosTransacao_quandoEntradaNegativa_deveLancarValorInvalido() {
        // Ação e Verificação
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            // O método verificarEntradaValor é chamado internamente e deve lançar a exceção
            VerificadorTransacao.dadosTransacao("-100", TRANSFERENCIA, VerificadorEntrada.STANDARD);
        });
        assertEquals("[ERRO] Valor negativo para operacao", exception.getMessage());
    }

    // --- CENÁRIO 1.2: Validação para Transferência ---

    @Test
    @DisplayName("Cenário 1.2.1: Deve retornar false para transferência com saldo suficiente")
    void dadosTransacao_quandoOperacaoTransferenciaEValorValido_deveRetornarTrue() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(500.0);

        // Ação
        boolean resultado = VerificadorTransacao.dadosTransacao("2000", TRANSFERENCIA, VerificadorEntrada.STANDARD);

        // Verificação
        assertFalse(resultado, "Deveria retornar false por saldo insuficiente.");
    }

    @Test
    @DisplayName("Cenário 1.2.1 (Falha): Deve lançar exceção para transferência com saldo insuficiente")
    void dadosTransacao_quandoOperacaoTransferenciaESaldoInsuficiente_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(100.0);

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.dadosTransacao("200", TRANSFERENCIA, VerificadorEntrada.STANDARD);
        }, "Deveria lançar exceção de valor inválido por falta de saldo.");
    }


    // --- CENÁRIO 1.3.1: Validação para Depósito em Conta Standard ---

    @Test
    @DisplayName("Cenário 1.3.1: Depósito Standard - Deve retornar true para valor válido")
    void dadosTransacao_depositoStandardComValorValido_deveRetornarTrue() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);

        // Ação
        boolean resultado = VerificadorTransacao.dadosTransacao("500", DEPOSITO, VerificadorEntrada.STANDARD);

        // Verificação
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Cenário 1.3.1: Depósito Standard - Deve retornar true para valor no limite")
    void dadosTransacao_depositoStandardComValorNoLimite_deveRetornarTrue() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);
        String limiteStandard = String.valueOf(ContaStandard.DEPOSITO_MAXIMO);

        // Ação
        boolean resultado = VerificadorTransacao.dadosTransacao(limiteStandard, DEPOSITO, VerificadorEntrada.STANDARD);

        // Verificação
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Cenário 1.3.1: Depósito Standard - Deve lançar exceção para valor acima do limite")
    void dadosTransacao_depositoStandardComValorAcimaDoLimite_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);
        String valorAcima = String.valueOf(ContaStandard.DEPOSITO_MAXIMO + 1);

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.dadosTransacao(valorAcima, DEPOSITO, VerificadorEntrada.STANDARD);
        });
    }

    @Test
    @DisplayName("Cenário 1.3.1: Depósito Standard - Deve lançar exceção se soma de depósitos exceder o limite")
    void dadosTransacao_depositoStandardComSomaAcimaDoLimite_deveLancarValorInvalido() {
        // Configuração: Cliente já depositou 900 hoje. O limite é 1000.
        when(mockConta.getSaldoTotalDepositado()).thenReturn(900.0);

        // Ação e Verificação: Tentar depositar mais 101 excederá o limite.
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.dadosTransacao("101", DEPOSITO, VerificadorEntrada.STANDARD);
        });
    }

    // --- CENÁRIOS 1.3.2 (Premium) e 1.3.3 (Diamond) ---
    // Nota: Os testes para Premium e Diamond seguem a mesma lógica dos testes para Standard,
    // apenas mudando a constante do tipo de conta e os valores limite.

    @Test
    @DisplayName("Cenário 1.3.2: Depósito Premium - Deve lançar exceção para valor acima do limite")
    void dadosTransacao_depositoPremiumComValorAcimaDoLimite_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);
        // Usamos um valor fictício alto para garantir que o limite da ContaPremium seja o fator limitante.
        int valorAcimaLimitePremium = 50001;

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.dadosTransacao(String.valueOf(valorAcimaLimitePremium), DEPOSITO, VerificadorEntrada.PREMIUM);
        });
    }

    @Test
    @DisplayName("Cenário 1.3.3: Depósito Diamond - Deve lançar exceção para valor acima do limite")
    void dadosTransacao_depositoDiamondComValorAcimaDoLimite_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);
        // Usamos um valor fictício alto para garantir que o limite da ContaDiamond seja o fator limitante.
        int valorAcimaLimiteDiamond = 80001;

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.dadosTransacao(String.valueOf(valorAcimaLimiteDiamond), DEPOSITO, VerificadorEntrada.DIAMOND);
        });
    }

    // --- CENÁRIO 1.3.4: Validação para Tipo de Conta Inválido ---

    @Test
    @DisplayName("Cenário 1.3.4: Depósito - Deve retornar false para tipo de conta inválido")
    void dadosTransacao_depositoComTipoContaInvalido_deveRetornarFalse() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldoTotalDepositado()).thenReturn(0.0);

        // Ação
        boolean resultado = VerificadorTransacao.dadosTransacao("100", DEPOSITO, "CONTA_INEXISTENTE");

        // Verificação
        assertFalse(resultado, "Deveria retornar false se o tipo de conta não corresponder a nenhum caso do switch.");
    }
}