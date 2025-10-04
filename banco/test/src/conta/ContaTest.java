package conta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import funcionalidades.exceptions.EmprestimoException;
import conta.exceptions.TipoInvalido;
import interfaceUsuario.dados.DadosCartao;

// meus testes unitários foram focados nos métodos pagarEmprestimo() e pagarParcelaEmprestimo()

public class ContaTest {

    private Conta conta;

    @BeforeEach // executado antes de cada caso de teste
    public void setUp() {
        conta = new Conta();
    }

    @Test
    // pagar empréstimo quando saldo for suficiente
    public void pagarEmprestimo_SeSaldoSuficiente() throws EmprestimoException {
    // cenário: criar empréstimo de 500,00 com 5 parcelas
    conta.criarEmprestimo(500.0, 5);
    // saldo atual deve ser 500,00
    assertEquals(500.0, conta.getSaldo(), 0.0001, "Saldo após criarEmprestimo deve ser igual ao valor do empréstimo");

    // pagar o empréstimo inteiro
        conta.pagarEmprestimo();

    // validações: empréstimo zera e saldo diminui
        assertEquals(0.0,conta.getEmprestimo(), 0.0001, "Empréstimo deve ser 0 após pagamento");
        assertEquals(0.0, conta.getSaldo(), 0.0001, "Saldo deve reduzir pelo valor do empréstimo");
        assertEquals(0.0, conta.getParcelaEmprestimo(), 0.0001, "Nenhuma parcela pendente. Deve ser 0");
    }

    @Test
    public void pagarEmprestimo_LancarSeSaldoInsuficiente(){
        // cenário: pagar empréstimo e não ter saldo suficiente para quitar
        conta.emprestimo = 1000.0;
        conta.saldo = 100.0;

        // valida que a exceção é lançada quando saldo for menor que valor do empréstimo
        assertThrows(EmprestimoException.class, () -> conta.pagarEmprestimo());
    }

    @Test
    // teste para pagarParcelaEmprestimo()
    public void pagarParcelaEmprestimo_ParcelaNormal() throws EmprestimoException {
        // cenário: empréstimo = 600,00, em 6 parcelas de 100,00
        conta.criarEmprestimo(600.0, 6);
        // saldo inicial após criação: 600,00. criarEmprestimo também aumenta o saldo
        assertEquals(600.0, conta.getSaldo(), 0.0001);

        // pagar uma parcela (100,00)
        conta.pagarEmprestimo();

        // validações: após pagar, saldo diminui de 600,00 para 500,00, e empréstimo também
        assertEquals(500.0, conta.getSaldo(), 0.0001, "Saldo diminui em 1 parcela");
        assertEquals(500.0, conta.getEmprestimo(), 0.0001, "empréstimo reduz em uma parcela");
        assertEquals(100.0, conta.getParcelaEmprestimo(), 0.0001, "enquanto emprestimo > 0, parcela permanece igual");
    }

    @Test
    public void pagarParcelaEmprestimo_PagarSomenteORestanteDoEmprestimo() throws EmprestimoException{
        // cenário: quando temos uma última parcela restante (exemplo, 50,00) e ela é menor que a parcela normal (100,00)
        conta.emprestimo = 50.0;
        conta.parcelaEmprestimo = 100.0;
        conta.saldo = 100.0; // garante ter saldo suficiente para pagar os 50,00

        // paga a última parcela (50,00)
        conta.pagarEmprestimo();

        // validações: empréstimo zera e parcelaEmprestimo também zera, e o saldo reduz em 50,00
        assertEquals(0.0, conta.getEmprestimo(), 0.0001, "Empréstimo zera quando paga o restante");
        assertEquals(0.0, conta.getParcelaEmprestimo(), 0.0001, "ParcelaEmprestimo = 0 se emprestimo = 0");
        assertEquals(50.0, conta.getSaldo(), 0.0001, "Valor restante da parcela é subtraido do saldo");
    }


}
