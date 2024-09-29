package ecommerce.controller;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.repository.ClienteRepository;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CompraControllerTest {

    @Mock
    private CompraService compraService;

    @InjectMocks
    private CompraController compraController;

    private Cliente cliente;
    private CarrinhoDeCompras carrinho;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);


        compraController = new CompraController(compraService);


        cliente = new Cliente(1l, "teste", "rua teste", TipoCliente.valueOf("OURO"));

        Produto produto = new Produto(1L, "Produto", "Descrição do produto", BigDecimal.valueOf(300), 60, TipoProduto.ELETRONICO);

        ItemCompra itemCompra = new ItemCompra(1L, produto, 1L);

        List<ItemCompra> itens = List.of(itemCompra);

        carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(itens);

    }

    @Test
    void testFinalizarCompra_Sucesso() {

        // Simular o comportamento do compraService
        CompraDTO compraDTO = new CompraDTO(true, 123L, "Compra finalizada com sucesso.");
        when(compraService.finalizarCompra(1L, 1L)).thenReturn(compraDTO);


        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(compraDTO, response.getBody());
    }

    @Test
    void testFinalizarCompra_EstoqueIndisponivel() {


        when(compraService.finalizarCompra(anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Itens fora de estoque."));


        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().sucesso());
        assertEquals("Itens fora de estoque.", response.getBody().mensagem());
        assertNull(response.getBody().transacaoPagamentoId());
    }

    @Test
    void testFinalizarCompra_PagamentoNaoAutorizado() {

        when(compraService.finalizarCompra(anyLong(), anyLong())).thenThrow(new IllegalArgumentException("Pagamento não autorizado."));


        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pagamento não autorizado.", response.getBody().mensagem());
    }

    @Test
    void testFinalizarCompra_ExceptionGenerica() {

        when(compraService.finalizarCompra(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro ao processar compra.");
    }
}