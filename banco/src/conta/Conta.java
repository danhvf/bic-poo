package conta;

import agencia.Agencia;
import cartao.*;
import cliente.Cliente;
import conta.exceptions.TipoInvalido;
import conta.exceptions.TransacaoNaoRealizadaException;
import funcionalidades.exceptions.EmprestimoException;
import interfaceUsuario.InterfaceUsuario;
import interfaceUsuario.dados.DadosCartao;
import interfaceUsuario.dados.DadosChavesPix;
import interfaceUsuario.dados.DadosTransacao;
import interfaceUsuario.menus.MenuUsuario;
import transacao.Boleto;
import transacao.ChavePix;
import transacao.Transacao;
import transacao.exceptions.TransacaoException;
import utilsBank.GeracaoAleatoria;
import utilsBank.databank.Data;
import utilsBank.databank.DataBank;
import java.util.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Conta implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    protected final String ID_CONTA;
    protected final List<Transacao> TRANSACOES_REALIZADAS;
    protected final List<Transacao> TRANSACOES_AGENDADAS;
    protected final Historico HISTORICO;
    protected final GerenciamentoCartao CARTEIRA;
    protected final ChavePix CHAVES_PIX;
    protected Historico notificacoes;
    protected Double saldo;
    protected Double saldoTotalDepositado;
    protected Double dinheiroGuardado;
    protected Double emprestimo;
    protected Double parcelaEmprestimo;
    private static final Logger LOGGER = Logger.getLogger(Conta.class.getName());

    protected Conta() {
        this.ID_CONTA = GeracaoAleatoria.gerarIdConta(GeracaoAleatoria.TAMANHO_ID_CONTA);
        this.saldo = 0.0;
        this.dinheiroGuardado = 0.0;
        this.saldoTotalDepositado = 0.0;
        this.TRANSACOES_REALIZADAS = new ArrayList<>();
        this.TRANSACOES_AGENDADAS = new ArrayList<>();
        this.notificacoes = new Historico();
        this.HISTORICO = new Historico();
        this.CARTEIRA = new GerenciamentoCartao();
        this.emprestimo = 0.0;
        this.parcelaEmprestimo = 0.0;
        this.CHAVES_PIX = new ChavePix(null, null, null, null);
    }

    /**
     * Com os dados da chave pix tratados na Interface chama CHAVES_PIX.mudarAdicionarChavePix
     *
     * @return @code true ou @code false caso mudou ou não
     */

    public boolean modificarChavePix() {
        DadosChavesPix dadosChavePix = InterfaceUsuario.getDadosChavePix();
        return CHAVES_PIX.mudarAdicionarChavePix(dadosChavePix.getTipoChave(), dadosChavePix);
    }

    public boolean addTransacaoRealizada(Transacao t) {
        if (!TRANSACOES_REALIZADAS.contains(t)) {
            TRANSACOES_REALIZADAS.add(t);
            return true;
        }
        return false;
    }

    public boolean addTransacaoAgendadas(Transacao t) throws TransacaoException {
        if (!TRANSACOES_AGENDADAS.contains(t)) {
            this.HISTORICO.addTransacao(t);
            TRANSACOES_AGENDADAS.add(t);
            return true;
        }
        return false;
    }

    public Transacao agendarTransacao() throws TransacaoException {
        DadosTransacao dadosTransacao = InterfaceUsuario.getDadosTransacao();
        Data dataAgendada = dadosTransacao.getDataAgendada();
        Transacao transacao = Transacao.criarTransacaoAgendada(dadosTransacao, dataAgendada);
        if (addTransacaoAgendadas(transacao)) {
            Agencia.getInstance().addTransacao(transacao);
            return transacao;
        }
        throw new TransacaoNaoRealizadaException("Ocorreu algum erro ao realizar a Transacao. Tente novamente");
    }

    public Transacao realizarTransacaoAgendada(Transacao transacao) throws TransacaoException {
        if (transacao.getDataAgendada() == null) {
            throw new TransacaoException("Essa transacao ja foi realizada");
        }
        transferir(transacao);
        transacao.atualizar();
        return transacao;
    }

    public void apagarTransacaoAgendada(Transacao transacao) throws TransacaoException {
        try {
            this.TRANSACOES_AGENDADAS.remove(transacao);
        } catch (Exception ex) {
            throw new TransacaoException("Transacao nao encontrada");
        }
    }

    private void adicionarHistoricoNotificacao(Transacao transacao) throws TransacaoException {
        transacao.getContaOrigem().addHistorico(transacao);
        transacao.getContaDestino().addHistorico(transacao);
        transacao.getContaDestino().addNotificacao(transacao);
    }

    public void addHistorico(Transacao transacao) throws TransacaoException {
        this.HISTORICO.addTransacao(transacao);
    }

    private void transferir(Transacao transacao) throws TransacaoException {
        Double valorT = transacao.getValor();
        transacao.getContaDestino().aumentarSaldo(valorT);
        transacao.getContaOrigem().diminuirSaldo(valorT);
        transacao.getContaDestino().addHistorico(transacao);
        transacao.getContaDestino().addNotificacao(transacao);
    }

    public Transacao transferir() throws TransacaoException {
        DadosTransacao dadosTransacao = InterfaceUsuario.getDadosTransacao();
        Transacao transacao = new Transacao(dadosTransacao);
        Double valorT = transacao.getValor();

        if (addTransacaoRealizada(transacao)) {
            if (transacao.getContaDestino().equals(transacao.getContaOrigem())) {
                this.setSaldoTotalDepositado(valorT);
                transacao.getContaDestino().aumentarSaldo(valorT);
                transacao.getContaOrigem().addHistorico(transacao);
            } else {
                transacao.getContaDestino().aumentarSaldo(valorT);
                transacao.getContaOrigem().diminuirSaldo(valorT);
                adicionarHistoricoNotificacao(transacao);
            }
            return transacao;
        }

        throw new TransacaoNaoRealizadaException("Ocorreu algum erro ao realizar a Transacao. Tente novamente");
    }

    public Transacao depositar() throws TransacaoException {
        return transferir();
    }

    public void mostrarCartoes() {
        for (Cartao cartao : this.CARTEIRA.getListaDeCartoes()) {
            if (LOGGER.isLoggable(java.util.logging.Level.INFO)) {
                LOGGER.info(cartao.toString());
            }
        }
    }

    public void pagarBoleto(Boleto boleto, Cliente origem) throws TransacaoException {
        int intervalo = DataBank.criarData(DataBank.SEM_HORA).calcularIntervalo(boleto.getDataVencimento());
        Double valorTratado = (intervalo < 0) ? boleto.getMultaPorDias() * -intervalo : boleto.getMultaPorDias();
        valorTratado += boleto.getValor();
        if (this.saldo < valorTratado) {
            throw new TransacaoException("Saldo insuficiente");
        }
        boleto.pagar(origem);
        this.diminuirSaldo(valorTratado);
        boleto.getContaDestino().aumentarSaldo(valorTratado);
        adicionarHistoricoNotificacao(boleto);
    }

    public void criarCartao(String nomeTitular, DadosCartao dadosCartao) {
        Cartao cartao;

        if (this.getClass() == ContaStandard.class) {
            cartao = new CartaoStandard(nomeTitular, dadosCartao);
        } else if (this.getClass() == ContaPremium.class) {
            cartao = new CartaoPremium(nomeTitular, dadosCartao);
        } else if (this.getClass() == ContaDiamond.class) {
            cartao = new CartaoDiamond(nomeTitular, dadosCartao);
        } else {
            throw new TipoInvalido("Tipo do cartao invalido.");
        }

        this.CARTEIRA.adicionarNovoCartao(cartao);
    }

    public void criarEmprestimo(Double valor, Integer parcelas) {
        this.emprestimo = valor;
        this.parcelaEmprestimo = valor / parcelas;
        this.aumentarSaldo(valor);
    }

    public void pagarEmprestimo() throws EmprestimoException {
        if (this.emprestimo <= this.saldo) {
            Agencia.getInstance().addSaldo(this.emprestimo);
            this.saldo -= this.emprestimo;
            this.emprestimo = 0.0;
            this.parcelaEmprestimo = 0.0;
        } else {
            throw new EmprestimoException("Saldo insuficiente");
        }
    }

    public void pagarParcelaEmprestimo() throws EmprestimoException {
        double parcela;
        if (this.emprestimo < this.parcelaEmprestimo) {
            parcela = this.emprestimo;
        } else {
            parcela = this.parcelaEmprestimo;
        }
        if (parcela <= this.saldo) {
            Agencia.getInstance().addSaldo(parcela);
            this.saldo -= parcela;
            this.emprestimo -= parcela;
            if (this.emprestimo == 0) {
                this.parcelaEmprestimo = 0.0;
            }
        } else {
            throw new EmprestimoException("Saldo insuficiente");
        }
    }

    public void pagarFatura(Double valor) {
        this.CARTEIRA.aumentarLimiteAtual(valor);
        this.saldo -= valor;
    }

    public boolean aumentarFatura(Double valor) {
        this.CARTEIRA.diminuirLimiteAtual(valor);
        return true;
    }

    public void aumentarSaldo(Double valor) {
        this.saldo += valor;
    }

    private void diminuirSaldo(Double valor) {
        this.saldo -= valor;
    }

    public boolean hasEmprestimo() {
        return this.emprestimo > 0.0;
    }

    public boolean hasNotificacoes() {
        return !this.notificacoes.getTransacoes().isEmpty();
    }

    public void addNotificacao(Transacao transacao) throws TransacaoException {
        this.notificacoes.addTransacao(transacao);
    }

    public void addNotificacao(Fatura fatura) {
        this.notificacoes.addFaturas(fatura);
    }

    public void resetarNotificacoes() {
        this.notificacoes = new Historico();
    }

    public void setDinheiroGuardado(Double valor, String opcao) {
        if (opcao.equals(MenuUsuario.GUARDAR)) {
            this.saldo -= valor;
            this.dinheiroGuardado += valor;
        } else if (opcao.equals(MenuUsuario.RESGATAR)) {
            this.saldo += valor;
            this.dinheiroGuardado -= valor;
        }

    }

    public String getIdConta() {
        return ID_CONTA;
    }

    public ChavePix getChavesPix() {
        return CHAVES_PIX;
    }

    public Double getSaldoTotalDepositado() {
        return saldoTotalDepositado;
    }

    public void setSaldoTotalDepositado(Double saldoTotalDepositado) {
        this.saldoTotalDepositado = saldoTotalDepositado;
    }

    public Double getDinheiroGuardado() {
        return dinheiroGuardado;
    }

    public GerenciamentoCartao getCARTEIRA() {
        return this.CARTEIRA;
    }

    public Double getSaldo() {
        return saldo;
    }

    public Double getEmprestimo() {
        return emprestimo;
    }

    public Double getParcelaEmprestimo() {
        return this.parcelaEmprestimo;
    }

    public List<Transacao> getNotificacoes() {
        return this.notificacoes.getTransacoes();
    }

    public Historico getHistorico() {
        return HISTORICO;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[CONTA]\n");

        if (ID_CONTA != null) {
            sb.append("ID_CONTA: ").append(ID_CONTA).append("\n");
        }
        if (saldo != null) {
            sb.append("SALDO: ").append(saldo).append("\n");
        }
        if (dinheiroGuardado != null) {
            sb.append("DINHEIRO GUARDADO: ").append(dinheiroGuardado).append("\n");
        }
        if (emprestimo != null) {
            sb.append("EMPRESTIMO: ").append(emprestimo).append("\n");
        }
        if (CHAVES_PIX != null) {
            sb.append(CHAVES_PIX).append("\n");
        }

        sb.append("© TODOS OS DIREITOS RESERVADOS AO BIC\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Conta outra)) return false;
        return this.ID_CONTA.equals(outra.ID_CONTA);
    }

    @Override
    public int hashCode() {
        return ID_CONTA.hashCode();
    }


}
