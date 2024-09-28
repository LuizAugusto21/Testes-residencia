package ecommerce.controller;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class CompraControllerTest {

    @Mock
    private IEstoqueExternal estoqueExternal;

    @Mock
    private IPagamentoExternal pagamentoExternal;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CarrinhoDeComprasService carrinhoDeComprasService;

    @InjectMocks
    private CompraService compraService;

    @InjectMocks
    private CompraController compraController;

    @Test
    void testFinalizarCompra_Sucesso() {
        // Arrange - Mocking dependencies
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 123L));

        // Act - Calling the controller method
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Assert - Verifying the result
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().sucesso()).isTrue();
        assertThat(response.getBody().transacaoPagamentoId()).isEqualTo(123L);
    }

    @Test
    void testFinalizarCompra_EstoqueIndisponivel() {
        // Arrange - Mocking unavailable stock
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(false, List.of(1L)));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Produto(s) indisponível(is): [1]");
    }

    @Test
    void testFinalizarCompra_PagamentoNaoAutorizado() {
        // Arrange - Mocking successful stock availability but failed payment authorization
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(false, null));

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Pagamento não autorizado.");
    }

    @Test
    void testFinalizarCompra_ExceptionGenerica() {
        // Arrange - Mocking the CompraService to throw an exception
        when(compraService.finalizarCompra(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        // Act - Call the controller with actual values, not matchers
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro ao processar compra.");
    }
}