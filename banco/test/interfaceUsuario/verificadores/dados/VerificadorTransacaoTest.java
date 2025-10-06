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
import conta.GerenciamentoCartao;
import interfaceUsuario.MenuUsuarioConstantes;

import static interfaceUsuario.menus.MenuUsuario.DEPOSITO;
import static interfaceUsuario.menus.MenuUsuario.TRANSFERENCIA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificadorTransacaoTest {

    @Mock
    private Cliente mockCliente;
    @Mock
    private Conta mockConta;
    @Mock
    private GerenciamentoCartao mockCarteira;

    @BeforeEach
    void setup() {

        lenient().when(mockCliente.getConta()).thenReturn(mockConta);

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
    void dadosTransacao_quandoOperacaoTransferenciaESaldoInsuficiente_deveLancarfalse() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(500.0);

        // Ação
        boolean resultado = VerificadorTransacao.dadosTransacao("2000", TRANSFERENCIA, VerificadorEntrada.STANDARD);

        assertFalse(resultado, "Deveria retornar false por saldo insuficiente.");
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

// --- CENÁRIOS 2.1: Validação para Pagamento de Fatura ---

    @Test
    @DisplayName("Cenário 2.1.1: Deve retornar true para pagamento de fatura válido")
    void valorFatura_quandoPagaFaturaComValorValido_deveRetornarTrue() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(1000.0); // Saldo suficiente
        when(mockCarteira.getFatura()).thenReturn(500.0); // Fatura existente

        // Ação
        boolean resultado = VerificadorTransacao.valorFatura("400", MenuUsuarioConstantes.PAGAR_FATURA, mockCarteira);

        // Verificação
        assertTrue(resultado, "Deveria retornar true para um pagamento de fatura válido.");
    }

    @Test
    @DisplayName("Cenário 2.1.2: Deve lançar ValorInvalido para pagamento de fatura com saldo insuficiente")
    void valorFatura_quandoPagaFaturaComSaldoInsuficiente_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(100.0); // Saldo insuficiente
        when(mockCarteira.getFatura()).thenReturn(500.0);

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.valorFatura("200", MenuUsuarioConstantes.PAGAR_FATURA, mockCarteira);
        }, "Deveria lançar exceção ao tentar pagar fatura com saldo insuficiente.");
    }

    @Test
    @DisplayName("Cenário 2.1.3: Deve lançar ValorInvalido para pagamento maior que a fatura")
    void valorFatura_quandoPagaValorMaiorQueFatura_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(1000.0);
        when(mockCarteira.getFatura()).thenReturn(500.0);

        // Ação e Verificação
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.valorFatura("600", MenuUsuarioConstantes.PAGAR_FATURA, mockCarteira);
        });

        // Verificação da mensagem
        assertEquals("[ERRO] Valor de pagamento maior que o valor da fatura", exception.getMessage());
    }

// --- CENÁRIOS 2.2: Validação para Aumento de Fatura ---

    @Test
    @DisplayName("Cenário 2.2.1: Deve retornar false para aumento de fatura válido")
    void valorFatura_quandoAumentaFaturaComValorValido_deveRetornarFalse() throws ValorInvalido {
        // Configuração
        when(mockCarteira.getLimiteRestante()).thenReturn(1000.0);

        // Ação
        boolean resultado = VerificadorTransacao.valorFatura("500", MenuUsuarioConstantes.AUMENTAR_FATURA, mockCarteira);

        // Verificação (o método retorna false neste caminho, conforme a lógica original)
        assertFalse(resultado, "Deveria retornar false para um aumento de fatura válido.");
    }

    @Test
    @DisplayName("Cenário 2.2.2: Deve lançar ValorInvalido para aumento de fatura maior que o limite")
    void valorFatura_quandoAumentaFaturaAcimaDoLimite_deveLancarValorInvalido() throws ValorInvalido {
        // Configuração
        when(mockCarteira.getLimiteRestante()).thenReturn(1000.0);

        // Ação e Verificação
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.valorFatura("1200", MenuUsuarioConstantes.AUMENTAR_FATURA, mockCarteira);
        });

        // Verificação da mensagem
        assertEquals("[ERRO] Valor inserido maior que o seu limite", exception.getMessage());
    }


// --- CENÁRIOS 3: Método agendamentoTransacao ---

    @Test
    @DisplayName("Cenário 3.1: Deve retornar true para agendamento de transação válido")
    void agendamentoTransacao_quandoDadosValidos_deveRetornarTrue() throws ValorInvalido {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(1000.0); // Saldo suficiente para a validação interna
        String[] entrada = {"500", "25/12/2025"}; // Valor e data válidos

        // Ação
        boolean resultado = VerificadorTransacao.agendamentoTransacao(entrada, VerificadorEntrada.STANDARD);

        // Verificação
        assertTrue(resultado, "Deveria retornar true para um agendamento com dados válidos.");
    }

    @Test
    @DisplayName("Cenário 3.2: Deve retornar false para agendamento com valor inválido")
    void agendamentoTransacao_quandoValorInvalido_deveRetornarFalse() {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(100.0); // Saldo insuficiente
        String[] entrada = {"200", "25/12/2025"}; // Valor > Saldo

        // Ação e Verificação

        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.agendamentoTransacao(entrada, VerificadorEntrada.STANDARD);
        }, "Deveria lançar ValorInvalido pois o saldo é insuficiente.");
    }

    @Test
    @DisplayName("Cenário 3.3: Deve lançar ValorInvalido para agendamento com data inválida")
    void agendamentoTransacao_quandoDataInvalida_deveLancarValorInvalido() {
        // Configuração
        when(mockConta.getSaldo()).thenReturn(1000.0);
        String[] entrada = {"500", "32/12/2025"}; // Data inválida

        // Ação e Verificação
        assertThrows(ValorInvalido.class, () -> {
            VerificadorTransacao.agendamentoTransacao(entrada, VerificadorEntrada.STANDARD);
        }, "Deveria lançar ValorInvalido por causa da data incorreta.");
    }
}