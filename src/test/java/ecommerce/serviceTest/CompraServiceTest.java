package ecommerce.serviceTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

class CompraServiceTest {

    @Mock
    private CarrinhoDeComprasService carrinhoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private IEstoqueExternal estoqueExternal;

    @Mock
    private IPagamentoExternal pagamentoExternal;

    @InjectMocks
    private CompraService compraService;

    private Cliente cliente;
    private CarrinhoDeCompras carrinho;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new Cliente(1l, "teste", "rua teste", TipoCliente.valueOf("OURO"));

        Produto produto = new Produto(1L, "Produto", "Descrição do produto", BigDecimal.valueOf(300), 60, TipoProduto.ELETRONICO);

        ItemCompra itemCompra = new ItemCompra(1L, produto, 1L);

        List<ItemCompra> itens = List.of(itemCompra);

        carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(itens);

        when(clienteService.buscarPorId(anyLong())).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(anyLong(), any(Cliente.class))).thenReturn(carrinho);
    }

    @ParameterizedTest
    @CsvSource({
            // Peso <= 5kg
            "OURO, 4.0, 300.0, 300.0",  // Sem frete, sem desconto
            "OURO, 4.5, 600.0, 540.0",  // Sem frete, 10% desconto
            "OURO, 5.0, 1200.0, 960.0", // Sem frete, 20% desconto
            "PRATA, 4.0, 300.0, 300.0",  // Sem frete, sem desconto
            "PRATA, 4.5, 600.0, 540.0",  // Sem frete, 10% desconto
            "PRATA, 5.0, 1200.0, 960.0", // Sem frete, 20% desconto
            "BRONZE, 4.0, 300.0, 300.0",  // Sem frete, sem desconto
            "BRONZE, 4.5, 600.0, 540.0",  // Sem frete, 10% desconto
            "BRONZE, 5.0, 1200.0, 960.0", // Sem frete, 20% desconto

            // Peso entre 5kg e 10kg (Frete = 2.0 * Peso)
            "OURO, 7.0, 300.0, 300.0",  // Frete grátis, sem desconto
            "OURO, 9.0, 600.0, 540.0",  // Frete grátis, 10% desconto
            "OURO, 10.0, 1200.0, 960.0", // Frete grátis, 20% desconto
            "PRATA, 7.0, 300.0, 307.0",  // Frete com 50% de desconto, sem desconto
            "PRATA, 9.0, 600.0, 549.0",  // Frete com 50% de desconto, 10% desconto
            "PRATA, 10.0, 1200.0, 980.0", // Frete com 50% de desconto, 20% desconto
            "BRONZE, 7.0, 300.0, 314.0",  // Frete integral, sem desconto
            "BRONZE, 9.0, 600.0, 558.0",  // Frete integral, 10% desconto
            "BRONZE, 10.0, 1200.0, 1000.0", // Frete integral, 20% desconto

            // Peso entre 10kg e 50kg (Frete = 4.0 * Peso)
            "OURO, 20.0, 300.0, 300.0",  // Frete grátis, sem desconto
            "OURO, 30.0, 600.0, 540.0",  // Frete grátis, 10% desconto
            "OURO, 40.0, 1200.0, 960.0", // Frete grátis, 20% desconto
            "PRATA, 20.0, 300.0, 340.0",  // Frete com 50% de desconto, sem desconto
            "PRATA, 30.0, 600.0, 600.0",  // Frete com 50% de desconto, 10% desconto
            "PRATA, 40.0, 1200.0, 1040.0", // Frete com 50% de desconto, 20% desconto
            "BRONZE, 20.0, 300.0, 380.0",  // Frete integral, sem desconto
            "BRONZE, 30.0, 600.0, 660.0",  // Frete integral, 10% desconto
            "BRONZE, 40.0, 1200.0, 1120.0", // Frete integral, 20% desconto

            // Peso > 50kg (Frete = 7.0 * Peso)
            "OURO, 55.0, 300.0, 300.0",  // Frete grátis, sem desconto
            "OURO, 60.0, 600.0, 540.0",  // Frete grátis, 10% desconto
            "OURO, 70.0, 1200.0, 960.0", // Frete grátis, 20% desconto
            "PRATA, 55.0, 300.0, 492.5",  // Frete com 50% de desconto, sem desconto
            "PRATA, 60.0, 600.0, 750.0",  // Frete com 50% de desconto, 10% desconto
            "PRATA, 70.0, 1200.0, 1205.0", // Frete com 50% de desconto, 20% desconto
            "BRONZE, 55.0, 300.0, 685.0",  // Frete integral, sem desconto
            "BRONZE, 60.0, 600.0, 960.0",  // Frete integral, 10% desconto
            "BRONZE, 70.0, 1200.0, 1450.0"  // Frete integral, 20% desconto
    })
    void testCalcularCustoTotal(String tipoCliente, double pesoTotal, double valorItens, BigDecimal valorEsperado) {

        Cliente cliente = new Cliente(1L, "Test", "Rua Teste", TipoCliente.valueOf(tipoCliente));

        Produto produto = new Produto(1L, "Produto", "Descrição do produto", BigDecimal.valueOf(valorItens), (int) pesoTotal, TipoProduto.ELETRONICO);

        ItemCompra itemCompra = new ItemCompra(1L, produto, 1L);

        List<ItemCompra> itens = List.of(itemCompra);

        CarrinhoDeCompras carrinhoDeCompras = new CarrinhoDeCompras(1L, cliente, itens, LocalDate.now());

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoDeCompras).setScale(1);
        assertEquals(valorEsperado, custoTotal);
    }

    @Test
    void testFinalizarCompra_Sucesso() {

        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 123L));


        EstoqueBaixaDTO baixaDTO = new EstoqueBaixaDTO(true);
        when(estoqueExternal.darBaixa(anyList(), anyList())).thenReturn(baixaDTO);


        CompraDTO resultado = compraService.finalizarCompra(1L, 1L);


        assertNotNull(resultado);
        assertTrue(resultado.sucesso());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
        assertEquals(123L, resultado.transacaoPagamentoId());
    }

    @Test
    void testFinalizarCompra_EstoqueIndisponivel() {

        // Mock do estoque (produtos indisponíveis)
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(false, List.of(1L)));


        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_PagamentoNaoAutorizado() {


        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

        // Mock do pagamento não autorizado
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(false, null));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_ErroBaixaEstoque() {

        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));


        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 123L));

        // Mock da baixa no estoque (falha)
        EstoqueBaixaDTO baixaDTO = new EstoqueBaixaDTO(false);
        when(estoqueExternal.darBaixa(anyList(), anyList())).thenReturn(baixaDTO);


        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
    }

}
